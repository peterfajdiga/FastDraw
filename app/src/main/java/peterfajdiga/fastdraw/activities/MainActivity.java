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
import android.os.Build;
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
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import peterfajdiga.fastdraw.PrefMap;
import peterfajdiga.fastdraw.Preferences;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.dialogs.ActionsSheet;
import peterfajdiga.fastdraw.dialogs.NewCategoryDialog;
import peterfajdiga.fastdraw.dialogs.RenameCategoryDialog;
import peterfajdiga.fastdraw.dragdrop.DropZoneAppInfo;
import peterfajdiga.fastdraw.dragdrop.DropZoneCategory;
import peterfajdiga.fastdraw.dragdrop.DropZoneNewCategory;
import peterfajdiga.fastdraw.dragdrop.DropZoneRemoveShortcut;
import peterfajdiga.fastdraw.launcher.AppItemManager;
import peterfajdiga.fastdraw.launcher.LaunchManager;
import peterfajdiga.fastdraw.launcher.Launcher;
import peterfajdiga.fastdraw.launcher.ShortcutItemManager;
import peterfajdiga.fastdraw.launcher.launcheritem.AppItem;
import peterfajdiga.fastdraw.launcher.launcheritem.BitmapShortcutItem;
import peterfajdiga.fastdraw.launcher.launcheritem.LauncherItem;
import peterfajdiga.fastdraw.launcher.launcheritem.OreoShortcutItem;
import peterfajdiga.fastdraw.launcher.launcheritem.ResShortcutItem;
import peterfajdiga.fastdraw.launcher.launcheritem.ShortcutItem;
import peterfajdiga.fastdraw.receivers.InstallAppReceiver;
import peterfajdiga.fastdraw.views.CategoryTabLayout;
import peterfajdiga.fastdraw.views.animators.NavigationBarColorAnimator;
import peterfajdiga.fastdraw.views.animators.ViewBgColorAnimator;
import peterfajdiga.fastdraw.views.animators.ViewElevationAnimator;

public class MainActivity extends FragmentActivity implements
    Launcher.Listener,
    InstallAppReceiver.Owner,
    DropZoneRemoveShortcut.Owner<LauncherItem>,
    DropZoneCategory.Owner<LauncherItem>,
    DropZoneNewCategory.Owner<LauncherItem>,
    NewCategoryDialog.Listener,
    RenameCategoryDialog.Listener {

    public static final int INSTALL_SHORTCUT_REQUEST = 2143;

    private static final int DROPZONE_TRANSITION_DURATION = 200;

    private InstallAppReceiver installAppReceiver;

    private ValueAnimator dragBgAnimator;

    private static WeakReference<MainActivity> instance;
    private final LaunchManager launchManager = new LaunchManager(this);
    private Launcher launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = new WeakReference<>(this);
        onFirstRun();
        Preferences.loadPreferences(this);
        setContentView(Preferences.headerOnBottom ? R.layout.activity_main_headerbtm : R.layout.activity_main_headertop);
        if (Preferences.allowOrientation) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setupAppsPager();

        setupInstallAppReceiver();

        final Intent intent = getIntent();
        if (intent != null) {
            final String action = intent.getAction();
            if (action != null && action.equals(LauncherApps.ACTION_CONFIRM_PIN_SHORTCUT) && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                final OreoShortcutItem newShortcut = ShortcutItemManager.oreoShortcutFromIntent(intent);
                if (newShortcut != null) {
                    final String shortcutCategoryName = getString(R.string.default_shortcut_category);
                    addOreoShortcut(newShortcut, shortcutCategoryName);
                    launcher.showCategory(shortcutCategoryName);
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

    private void setupAppsPager() {
        ViewPager appsPager = findViewById(R.id.apps_pager);
        launcher = new Launcher(launchManager, this, appsPager);

        setupHeader(appsPager);

        loadLauncherItems();
        launcher.showCategory(Launcher.HOME_CATEGORY_NAME);
    }

    private void setupHeader(final ViewPager appsPager) {
        final CategoryTabLayout tabContainer = findViewById(R.id.tab_container);
        tabContainer.setupWithViewPager(appsPager);

        final ViewGroup header = findViewById(R.id.header);

        // custom (faster) animate layout changes
        LayoutTransition lt = new LayoutTransition();
        lt.setStartDelay(LayoutTransition.APPEARING, 0);
        //lt.setStartDelay(LayoutTransition.CHANGE_APPEARING, DROPZONE_TRANSITION_DURATION);
        lt.setDuration(LayoutTransition.CHANGE_APPEARING, DROPZONE_TRANSITION_DURATION);
        lt.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
        lt.setDuration(LayoutTransition.CHANGE_DISAPPEARING, DROPZONE_TRANSITION_DURATION);
        header.setLayoutTransition(lt);

        // header animator
        dragBgAnimator = ValueAnimator.ofArgb(Preferences.headerBgColor, Preferences.headerBgColorExpanded);
        dragBgAnimator.setDuration(DROPZONE_TRANSITION_DURATION);
        dragBgAnimator.addUpdateListener(new ViewBgColorAnimator(header));
        if (Preferences.headerOnBottom) {
            dragBgAnimator.addUpdateListener(new NavigationBarColorAnimator(getWindow()));
            getWindow().setStatusBarColor(Preferences.headerBgColor);
        } else {
            getWindow().setNavigationBarColor(Preferences.headerBgColor);
        }
        if (Preferences.headerShadow) {
            final float elevationStart = getResources().getDimension(R.dimen.pager_header_elevation);
            final float elevationEnd = getResources().getDimension(R.dimen.pager_header_expanded_elevation);
            dragBgAnimator.addUpdateListener(new ViewElevationAnimator(header, elevationStart, elevationEnd));
        }
        dragBgAnimator.setCurrentPlayTime(0);

        // header preferences
        if (Preferences.scrollableTabs) {
            tabContainer.setTabMode(TabLayout.MODE_SCROLLABLE);
        }
        if (Preferences.headerSeparator) {
            findViewById(R.id.header_separator).setVisibility(View.VISIBLE);
        }
        if (Preferences.statusBarDarker) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setupDropZones();
    }

    private void setupDropZones() {
        findViewById(R.id.drop_zone_new_category).setOnDragListener(new DropZoneNewCategory());
        findViewById(R.id.drop_zone_app_info).setOnDragListener(new DropZoneAppInfo());
        findViewById(R.id.drop_zone_remove_shortcut).setOnDragListener(new DropZoneRemoveShortcut());

        // immediate reaction to drag end
        findViewById(android.R.id.content).setOnDragListener((v, event) -> {
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
        });
    }

    private void setupInstallAppReceiver() {
        installAppReceiver = new InstallAppReceiver(this);
        IntentFilter appChangeFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        appChangeFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        appChangeFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        appChangeFilter.addDataScheme("package");
        registerReceiver(installAppReceiver, appChangeFilter);
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
            final Launcher pager = launcher;
            pager.showInitialCategory();
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
        if (instance != null && instance.get() == this) { // should be the only instance because of singleInstance
            instance = null;
        }

        // App installation BroadcastReceivers
        unregisterReceiver(installAppReceiver);

        // cleanUnusedPrefKeysAppsCategories(); // This is disabled as a test to see if apps' categories stop being forgotten // TODO: re-enable eventually
    }

    @Override
    public void onBackPressed() { }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INSTALL_SHORTCUT_REQUEST && resultCode == RESULT_OK) {
            final ShortcutItem newShortcut = ShortcutItemManager.shortcutFromIntent(this, data);
            addShortcut(newShortcut, launcher.getCurrentCategoryName());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addShortcut(@NonNull final ShortcutItem shortcutItem, @NonNull final String categoryName) {
        ShortcutItemManager.saveShortcut(this, shortcutItem);
        launcher.moveItem(shortcutItem, categoryName, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addOreoShortcut(@NonNull final OreoShortcutItem shortcutItem, @NonNull final String categoryName) {
        ShortcutItemManager.saveShortcut(this, shortcutItem);
        launcher.moveItem(shortcutItem, categoryName, false);
    }

    private void loadLauncherItems() {
        final LauncherItem[] items = Stream.concat(
            AppItemManager.getAppItems(getPackageManager()),
            ShortcutItemManager.getShortcutItems(this)
        ).toArray(LauncherItem[]::new);

        launcher.addItemsStartup(getString(R.string.default_category), items);
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
        final PrefMap categoriesMap = new PrefMap(this, "categories");
        categoriesMap.putString(appItem.getID(), Launcher.HOME_CATEGORY_NAME);
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
                case BitmapShortcutItem.TYPE_KEY:
                case ResShortcutItem.TYPE_KEY:
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
        final Launcher pager = launcher;
        final PrefMap categoryOrder = new PrefMap(this, "categoryorder");
        categoryOrder.clean(categoryName -> !pager.doesCategoryExist(categoryName));
    }

    private boolean doesPackageExist(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    public static void forceFinish() {
        if (instance != null) {
            instance.get().finish();
            instance = null;
        }
    }

    @Nullable
    public static MainActivity getInstance() {
        if (instance == null) {
            return null;
        }
        return instance.get();
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
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT); // don't keep wallpaper picker in recent apps
        startActivity(Intent.createChooser(intent, getString(R.string.select_wallpaper)));
    }

    public void showCreateShortcutDialog() {
        final Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.add_shortcut)), MainActivity.INSTALL_SHORTCUT_REQUEST);
    }

    public void renameCurrentCategory() {
        final RenameCategoryDialog dialog = new RenameCategoryDialog(
            this,
            launcher.getCurrentCategoryName(),
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
    public void onLongpress() {
        performAction(Preferences.longclickAction);
    }

    @Override
    public void onDoubletap() {
        performAction(Preferences.doubleclickAction);
    }

    @Override
    public void onPinch() {
        performAction(Preferences.pinchAction);
    }

    @Override
    public void onUnpinch() {
        performAction(Preferences.unpinchAction);
    }

    @Override
    public void onSwipeUp2F() {
        performAction(Preferences.swipeUpAction2F);
    }

    @Override
    public void onSwipeDown2F() {
        performAction(Preferences.swipeDownAction2F);
    }

    // app management

    @Override
    public void onAppInstall(String packageName) {
        final AppItem[] appItems = AppItemManager.getAppItems(getPackageManager(), packageName).toArray(AppItem[]::new);
        launcher.addItems(getString(R.string.default_category), appItems);
    }

    @Override
    public void onAppChange(String packageName) {
        final Stream<AppItem> updatedAppItems = AppItemManager.getAppItems(getPackageManager(), packageName);
        AppItemManager.updatePackageItems(launcher, packageName, updatedAppItems, getString(R.string.default_category));
    }

    @Override
    public void onAppRemove(String packageName) {
        AppItemManager.removePackageItems(this, launcher, packageName, false);
    }

    public void onShortcutReceived(final ShortcutItem newShortcut) {
        launcher.addItems(getString(R.string.default_shortcut_category), newShortcut);
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
        findViewById(R.id.drop_zone_remove_shortcut).setVisibility(draggedItem instanceof ShortcutItem ? View.VISIBLE : View.GONE);

        // set header background
        dragBgAnimator.start();

        // hide status or navigation bar
        if (!Preferences.headerOnBottom) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
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
            if (!Preferences.headerOnBottom) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
        ShortcutItem shortcutItem = (ShortcutItem)draggedItem;
        launcher.removeItem(shortcutItem, true);
        ShortcutItemManager.deleteShortcut(this, shortcutItem);
    }

    @Override
    public void onDraggedItemChangeCategory(String newCategoryName) {
        launcher.moveItem(draggedItem, newCategoryName, true);
    }

    @Override
    public void onDraggedItemNewCategory() {
        newCategoryDroppedItem = draggedItem;
        NewCategoryDialog dialog = new NewCategoryDialog(this, getString(R.string.new_category), getString(R.string.create));
        dialog.show(getSupportFragmentManager(), "NewCategoryDialog");
    }

    @Override
    public void onNewCategoryDialogSuccess(String newCategoryName) {
        launcher.moveItem(newCategoryDroppedItem, newCategoryName, true);
        newCategoryDroppedItem = null;
    }

    @Override
    public void onRenameCategoryDialogSuccess(String oldCategoryName, String newCategoryName) {
        final Launcher pager = launcher;
        boolean followItem = oldCategoryName.equals(pager.getCurrentCategoryName());
        for (LauncherItem item : pager.getItems(oldCategoryName)) {
            pager.moveItem(item, newCategoryName, followItem);
        }
    }
}
