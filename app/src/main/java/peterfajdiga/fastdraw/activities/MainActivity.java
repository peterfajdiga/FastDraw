package peterfajdiga.fastdraw.activities;

import static peterfajdiga.fastdraw.launcher.LaunchManager.PERMISSION_REQUEST_CODE;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Telephony;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.util.Predicate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.tabs.TabLayout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import peterfajdiga.fastdraw.NavigationBarAnimator;
import peterfajdiga.fastdraw.PrefMap;
import peterfajdiga.fastdraw.Preferences;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.ViewBgAnimator;
import peterfajdiga.fastdraw.dialogs.ActionsSheet;
import peterfajdiga.fastdraw.dialogs.NewCategoryDialog;
import peterfajdiga.fastdraw.dialogs.RenameCategoryDialog;
import peterfajdiga.fastdraw.dragdrop.DropZoneAppInfo;
import peterfajdiga.fastdraw.dragdrop.DropZoneCategory;
import peterfajdiga.fastdraw.dragdrop.DropZoneNewCategory;
import peterfajdiga.fastdraw.dragdrop.DropZoneRemoveShortcut;
import peterfajdiga.fastdraw.launcher.AppItemManager;
import peterfajdiga.fastdraw.launcher.LaunchManager;
import peterfajdiga.fastdraw.launcher.LauncherPager;
import peterfajdiga.fastdraw.launcher.ShortcutItemManager;
import peterfajdiga.fastdraw.launcher.item.AppItem;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;
import peterfajdiga.fastdraw.launcher.item.OreoShortcutItem;
import peterfajdiga.fastdraw.launcher.item.Saveable;
import peterfajdiga.fastdraw.launcher.item.ShortcutItem;
import peterfajdiga.fastdraw.receivers.InstallAppReceiver;
import peterfajdiga.fastdraw.views.CategoryTabLayout;

public class MainActivity extends FragmentActivity implements
    LauncherPager.Owner,
    InstallAppReceiver.Owner,
    DropZoneRemoveShortcut.Owner<LauncherItem>,
    DropZoneCategory.Owner<LauncherItem>,
    DropZoneNewCategory.Owner<LauncherItem>,
    NewCategoryDialog.Owner,
    RenameCategoryDialog.Owner {

    public static final int INSTALL_SHORTCUT_REQUEST = 2143;

    private static final int DROPZONE_TRANSITION_DURATION = 200;

    private InstallAppReceiver installAppReceiver;

    private ValueAnimator dragBgAnimator;

    private static MainActivity instance;
    private LaunchManager launchManager = new LaunchManager(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
        onFirstRun();
        Preferences.loadPreferences(this);
        setContentView(Preferences.mainLayoutResource);
        if (Preferences.allowOrientation) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        // custom (faster) animate layout changes
        ViewGroup header = (ViewGroup)findViewById(R.id.header);
        LayoutTransition lt = new LayoutTransition();
        lt.setStartDelay(LayoutTransition.APPEARING, 0);
        //lt.setStartDelay(LayoutTransition.CHANGE_APPEARING, DROPZONE_TRANSITION_DURATION);
        lt.setDuration(LayoutTransition.CHANGE_APPEARING, DROPZONE_TRANSITION_DURATION);
        lt.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
        lt.setDuration(LayoutTransition.CHANGE_DISAPPEARING, DROPZONE_TRANSITION_DURATION);
        header.setLayoutTransition(lt);

        findViewById(R.id.drop_zone_new_category).setOnDragListener(new DropZoneNewCategory());
        findViewById(R.id.drop_zone_app_info).setOnDragListener(new DropZoneAppInfo());
        findViewById(R.id.drop_zone_remove_shortcut).setOnDragListener(new DropZoneRemoveShortcut());

        LauncherPager appsPager = getPager();
        appsPager.setLaunchManager(launchManager);
        appsPager.setOwner(this);

        CategoryTabLayout tabContainer = findViewById(R.id.tab_container);
        tabContainer.setupWithViewPager(appsPager);

        loadLauncherItems();
        appsPager.showCategory("HOME");

        // immediate reaction to drag end
        findViewById(android.R.id.content).setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(final View v, final DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED: {
                        return draggedItem != null;
                    }
                    case DragEvent.ACTION_DROP: {
                        onDragEnded1();
                        return false;
                    }
                    case DragEvent.ACTION_DRAG_ENDED: {
                        onDragEnded1();
                        onDragEnded2();
                        return true;
                    }
                    default: {
                        return true;
                    }
                }
            }
        });

        // header color animator
        dragBgAnimator = ValueAnimator.ofArgb(Preferences.headerBgColor, Preferences.headerBgColorExpanded);
        dragBgAnimator.setDuration(DROPZONE_TRANSITION_DURATION);
        dragBgAnimator.addUpdateListener(new ViewBgAnimator(header));
        if (Preferences.mainLayoutResource == R.layout.activity_main_headerbtm) {
            dragBgAnimator.addUpdateListener(new NavigationBarAnimator(getWindow()));
            getWindow().setStatusBarColor(Preferences.headerBgColor);
        } else {
            getWindow().setNavigationBarColor(Preferences.headerBgColor);
        }
        dragBgAnimator.setCurrentPlayTime(0);

        // header preferences
        if (Preferences.scrollableTabs) {
            tabContainer.setTabMode(TabLayout.MODE_SCROLLABLE);
        }
        if (Preferences.headerSeparator) {
            findViewById(R.id.header_separator).setVisibility(View.VISIBLE);
        }
        if (!Preferences.headerShadow) {
            header.setElevation(0);
        }
        if (Preferences.statusBarDarker) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        // App installation BroadcastReceiver
        installAppReceiver = new InstallAppReceiver(this);
        IntentFilter appChangeFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        appChangeFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        appChangeFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        appChangeFilter.addDataScheme("package");
        registerReceiver(installAppReceiver, appChangeFilter);

        final Intent intent = getIntent();
        if (intent != null) {
            final String action = intent.getAction();
            if (action.equals(LauncherApps.ACTION_CONFIRM_PIN_SHORTCUT) && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                final OreoShortcutItem newShortcut = ShortcutItemManager.oreoShortcutFromIntent(this, intent);
                if (newShortcut != null) {
                    final String shortcutCategoryName = getString(R.string.default_shortcut_category);
                    addShortcut(newShortcut, shortcutCategoryName);
                    getPager().showCategory(shortcutCategoryName);
                }
            }
        }
    }

    private void onFirstRun() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("firstRun", true)) {

            // Fast Draw Preferences
            final ActivityInfo fastdrawPrefsInfo = new ActivityInfo();
            fastdrawPrefsInfo.packageName = getPackageName();
            fastdrawPrefsInfo.name = SettingsActivity.class.getName();
            addAppToHome(new AppItem(fastdrawPrefsInfo));

            // phone app
            addAppToHome(new Intent(Intent.ACTION_DIAL));

            // sms app
            String smsAppPackage = Telephony.Sms.getDefaultSmsPackage(this);
            if (smsAppPackage == null) {
                smsAppPackage = Settings.Secure.getString(getContentResolver(), "sms_default_application");
            }
            if (smsAppPackage == null || !addAppToHome(smsAppPackage)) {
                final Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
                launcherIntent.setType("vnd.android-dir/mms-sms");
                addAppToHome(launcherIntent);
            }

            // contacts
            final Intent contactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            addDefaultAppToHome(contactIntent);

            // browser
            final Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"));
            addDefaultAppToHome(urlIntent);

            // settings
            addAppToHome(new Intent(Settings.ACTION_SETTINGS));

            prefs.edit().putBoolean("firstRun", false).apply();
        }
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);

        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) == 0) {
            // close actions menu if open
            final Fragment actionsSheet = getSupportFragmentManager().findFragmentByTag("ActionsSheet");
            if (actionsSheet != null) {
                getSupportFragmentManager().beginTransaction().remove(actionsSheet).commit();
            }

            // show home category
            final LauncherPager pager = getPager();
            if (!pager.showCategory("HOME")) {
                pager.setCurrentItem(0);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // TODO: save shortcuts async
        cleanUnusedPrefKeysCategoryOrder();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (instance == this) { // should be the only one, because of singleInstance
            instance = null;
        }

        // App installation BroadcastReceivers
        unregisterReceiver(installAppReceiver);

        cleanUnusedPrefKeysAppsCategories();
    }

    @Override
    public void onBackPressed() { }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INSTALL_SHORTCUT_REQUEST && resultCode == RESULT_OK) {
            final ShortcutItem newShortcut = ShortcutItemManager.shortcutFromIntent(this, data);
            addShortcut(newShortcut, getPager().getCurrentCategoryName());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addShortcut(@NonNull final ShortcutItem shortcutItem, @NonNull final String categoryName) {
        ShortcutItemManager.saveShortcut(this, shortcutItem);
        getPager().moveLauncherItem(shortcutItem, categoryName, false);
    }

    private void addShortcut(@NonNull final OreoShortcutItem shortcutItem, @NonNull final String categoryName) {
        ShortcutItemManager.saveShortcut(this, shortcutItem);
        getPager().moveLauncherItem(shortcutItem, categoryName, false);
    }

    private void loadLauncherItems() {
        final AppItem[] appItems = AppItemManager.getAppItems(getPackageManager());
        getPager().addLauncherItems(getString(R.string.default_category), appItems);

        final List<LauncherItem> shortcutItems = ShortcutItemManager.getShortcutItems(this);
        getPager().addLauncherItems("LOST&FOUND", shortcutItems.toArray(new LauncherItem[0]));
    }

    /**
     * @param intent the intent that the app should be able to perform
     * @return true if successful
     */
    private boolean addDefaultAppToHome(@NonNull final Intent intent) {
        final ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent, 0);
        return resolveInfo != null && addAppToHome(resolveInfo.activityInfo.packageName);
    }

    /**
     * Always successful
     */
    private void addAppToHome(@NonNull final AppItem appItem) {
        final String category = "HOME";
        final PrefMap categoriesMap = new PrefMap(this, "categories");
        categoriesMap.putString(appItem.getID(), category);
    }

    /**
     * @return true if successful
     */
    private boolean addAppToHome(@NonNull final Intent launcherIntent) {
        final ResolveInfo resolveInfo = getPackageManager().resolveActivity(launcherIntent, 0);
        if (resolveInfo == null) {
            return false;
        }
        addAppToHome(new AppItem(resolveInfo.activityInfo));
        return true;
    }

    /**
     * @return true if successful
     */
    private boolean addAppToHome(@NonNull final String packageName) {
        final Intent launcherIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        return launcherIntent != null && addAppToHome(launcherIntent);
    }

    /**
     * Remove unused apps
     * This only serves to clean up the pref file
     */
    private void cleanUnusedPrefKeysAppsCategories() {
        final PrefMap categories = new PrefMap(this, "categories");
        categories.clean(id -> {
            final int separatorIndex = id.indexOf('\0');
            final String type = id.substring(0, separatorIndex);
            final String tail = id.substring(separatorIndex + 1);
            switch (type) {
                case AppItem.TYPE_KEY:
                    final String packageName = tail.substring(0, tail.indexOf('\0'));
                    return !doesPackageExist(packageName);
                case ShortcutItem.TYPE_KEY:
                case OreoShortcutItem.TYPE_KEY:
                    return false;
                default:
                    return true;
            }
        });
    }

    /**
     * Remove unused categories (for category ordering)
     * This needs to be done, so that nonexistent categories don't show up in category order settings
     */
    private void cleanUnusedPrefKeysCategoryOrder() {
        final LauncherPager pager = getPager();
        final PrefMap categoryOrder = new PrefMap(this, "categoryorder");
        categoryOrder.clean(new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return !pager.doesCategoryExist(s);
            }
        });
    }

    private boolean doesPackageExist(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    public LauncherPager getPager() {
        return (LauncherPager)findViewById(R.id.apps_pager);
    }

    public static void forceFinish() {
        if (instance != null) {
            instance.finish();
            instance = null;
        }
    }

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            launchManager.onRequestPermissionsResult(permissions, grantResults);
        }
    }

    // actions

    public void openActionsMenu() {
        final ActionsSheet dialog = new ActionsSheet();
        dialog.show(getSupportFragmentManager(), "ActionsSheet");
    }

    public void openWallpaperPicker() {
        final Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
        startActivity(Intent.createChooser(intent, getString(R.string.select_wallpaper)));
    }

    public void showCreateShortcutDialog() {
        final Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.add_shortcut)), MainActivity.INSTALL_SHORTCUT_REQUEST);
    }

    public void renameCurrentCategory() {
        final RenameCategoryDialog dialog = new RenameCategoryDialog(
            getPager().getCurrentCategoryName(),
            getString(R.string.rename_category),
            getString(R.string.rename)
        );
        dialog.show(getSupportFragmentManager(), "RenameCategoryDialog");
    }

    public void openSettings() {
        final Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    public void expandNotificationsPanel() {
        @SuppressLint("WrongConstant") final Object statusBarService = getSystemService("statusbar");
        try {
            final Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
            final Method expandMethod = statusBarManager.getMethod("expandNotificationsPanel");
            expandMethod.invoke(statusBarService);
        } catch (final ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error_expand_notifications_panel, Toast.LENGTH_LONG).show();
        }
    }

    public void performAction(final int action) {
        switch (action) {
            case Preferences.ACTION_MENU:            openActionsMenu(); break;
            case Preferences.ACTION_WALLPAPER:       openWallpaperPicker(); break;
            case Preferences.ACTION_SHORTCUT:        showCreateShortcutDialog(); break;
            case Preferences.ACTION_RENAME_CATEGORY: renameCurrentCategory(); break;
            case Preferences.ACTION_SETTINGS:        openSettings(); break;
            case Preferences.ACTION_NOTIFICATIONS:   expandNotificationsPanel(); break;
        }
    }

    @Override
    public void onPagerLongpress() {
        performAction(Preferences.longclickAction);
    }

    @Override
    public void onPagerDoubletap() {
        performAction(Preferences.doubleclickAction);
    }

    @Override
    public void onPagerPinch() {
        performAction(Preferences.pinchAction);
    }

    @Override
    public void onPagerUnpinch() {
        performAction(Preferences.unpinchAction);
    }


    // app management

    @Override
    public void onAppInstall(String packageName) {
        final AppItem[] appItems = AppItemManager.getAppItems(getPackageManager(), packageName);
        getPager().addLauncherItems(getString(R.string.default_category), appItems); // TODO (BUG): app item may already be there. check before adding
    }

    @Override
    public void onAppChange(String packageName) {
        AppItemManager.removePackageItems(this, getPager(), packageName, false);

        final AppItem[] appItems = AppItemManager.getAppItems(getPackageManager(), packageName);
        getPager().addLauncherItems(getString(R.string.default_category), appItems);
    }

    @Override
    public void onAppRemove(String packageName) {
        AppItemManager.removePackageItems(this, getPager(), packageName, true);
    }

    public void onShortcutReceived(final ShortcutItem newShortcut) {
        getPager().addLauncherItems(getString(R.string.default_shortcut_category), newShortcut);
    }

    // LauncherItem dragging
    private View draggedView = null;
    private LauncherItem draggedItem = null;
    private LauncherItem newCategoryDroppedItem = null;

    @Override
    public void onDragStarted(@NonNull final View draggedView, @NonNull final LauncherItem draggedItem) {
        final Paint silhouettePaint = new Paint();
        silhouettePaint.setColorFilter(new LightingColorFilter(0xff000000, 0xff000000));
        draggedView.setLayerType(View.LAYER_TYPE_SOFTWARE, silhouettePaint);

        this.draggedView = draggedView;
        this.draggedItem = draggedItem;

        // show drop zones
        findViewById(R.id.apps_pager).animate().alpha(0.2f);
        findViewById(R.id.category_drop_zone_container).setVisibility(View.VISIBLE);

        // show type specific drop zones
        findViewById(R.id.drop_zone_app_info).setVisibility(draggedItem instanceof AppItem ? View.VISIBLE : View.GONE);
        findViewById(R.id.drop_zone_remove_shortcut).setVisibility(draggedItem instanceof Saveable ? View.VISIBLE : View.GONE);

        // set header background
        dragBgAnimator.start();

        // hide status or navigation bar
        if (Preferences.mainLayoutResource == R.layout.activity_main_headertop) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } /*else if (Preferences.mainLayoutResource == R.layout.activity_main_headerbtm) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }*/
    }

    public void onDragEnded1() {
        if (draggedItem != null) {
            // hide drop zones
            findViewById(R.id.apps_pager).animate().alpha(1.0f);
            findViewById(R.id.category_drop_zone_container).setVisibility(View.GONE);
            draggedItem = null;

            // reset header background
            dragBgAnimator.reverse();

            // show status or navigation bar
            switch (Preferences.mainLayoutResource) {
                case R.layout.activity_main_headertop: {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    break;
                }
                /*case R.layout.activity_main_headerbtm: {
                    getWindow().getDecorView().setSystemUiVisibility(0);
                    break;
                }*/
            }
        }
    }

    public void onDragEnded2() {
        if (draggedView != null) {
            draggedView.setLayerType(View.LAYER_TYPE_NONE, null);
            draggedView = null;
        }
    }

    @Override
    public LauncherItem getDraggedItem() {
        return draggedItem;
    }

    @Override
    public void onDraggedItemRemove() {
        assert draggedItem instanceof Saveable;
        LauncherItem shortcutItem = draggedItem;
        getPager().removeLauncherItem(shortcutItem, true);
        ShortcutItemManager.deleteShortcut(this, (Saveable)shortcutItem);
    }

    @Override
    public void onDraggedItemChangeCategory(String newCategoryName) {
        getPager().moveLauncherItem(draggedItem, newCategoryName, true);
    }

    @Override
    public void onDraggedItemNewCategory() {
        newCategoryDroppedItem = draggedItem;
        NewCategoryDialog dialog = new NewCategoryDialog(getString(R.string.new_category), getString(R.string.create));
        dialog.show(getSupportFragmentManager(), "NewCategoryDialog");
    }

    @Override
    public void onNewCategoryDialogSuccess(String newCategoryName) {
        getPager().moveLauncherItem(newCategoryDroppedItem, newCategoryName, true);
        newCategoryDroppedItem = null;
    }

    @Override
    public void onRenameCategoryDialogSuccess(String oldCategoryName, String newCategoryName) {
        final LauncherPager pager = getPager();
        boolean followItem = oldCategoryName.equals(pager.getCurrentCategoryName());
        for (LauncherItem item : pager.getLauncherItems(oldCategoryName)) {
            pager.moveLauncherItem(item, newCategoryName, followItem);
        }
    }
}
