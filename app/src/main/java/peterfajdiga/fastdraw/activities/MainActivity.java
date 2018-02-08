package peterfajdiga.fastdraw.activities;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.internal.util.Predicate;

import java.io.File;

import peterfajdiga.fastdraw.NavigationBarAnimator;
import peterfajdiga.fastdraw.PrefMap;
import peterfajdiga.fastdraw.Preferences;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.ViewBgAnimator;
import peterfajdiga.fastdraw.dialogs.ActionsSheet;
import peterfajdiga.fastdraw.dialogs.NewCategoryDialog;
import peterfajdiga.fastdraw.dialogs.RenameCategoryDialog;
import peterfajdiga.fastdraw.launcher.AppItemManager;
import peterfajdiga.fastdraw.launcher.LauncherPager;
import peterfajdiga.fastdraw.launcher.item.AppItem;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;
import peterfajdiga.fastdraw.launcher.item.ShortcutItem;
import peterfajdiga.fastdraw.dragdrop.DropZoneAppInfo;
import peterfajdiga.fastdraw.dragdrop.DropZoneCategory;
import peterfajdiga.fastdraw.dragdrop.DropZoneNewCategory;
import peterfajdiga.fastdraw.dragdrop.DropZoneRemoveShortcut;
import peterfajdiga.fastdraw.receivers.InstallAppReceiver;
import peterfajdiga.fastdraw.receivers.InstallShortcutReceiver;
import peterfajdiga.fastdraw.views.TabContainer;

public class MainActivity extends FragmentActivity implements
        LauncherPager.Owner,
        InstallAppReceiver.Owner,
        InstallShortcutReceiver.Owner,
        DropZoneRemoveShortcut.Owner<LauncherItem>,
        DropZoneCategory.Owner<LauncherItem>,
        DropZoneNewCategory.Owner<LauncherItem>,
        NewCategoryDialog.Owner,
        RenameCategoryDialog.Owner {

    public static final int INSTALL_SHORTCUT_REQUEST = 2143;

    private static final int DROPZONE_TRANSITION_DURATION = 200;

    private InstallShortcutReceiver installShortcutReceiver;
    private InstallAppReceiver installAppReceiver;

    private ValueAnimator dragBgAnimator;

    private static MainActivity instance;
    private Intent launchIntent = null;
    private Bundle launchOpts = null;

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
//        lt.setStartDelay(LayoutTransition.CHANGE_APPEARING, DROPZONE_TRANSITION_DURATION);
        lt.setDuration(LayoutTransition.CHANGE_APPEARING, DROPZONE_TRANSITION_DURATION);
        lt.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
        lt.setDuration(LayoutTransition.CHANGE_DISAPPEARING, DROPZONE_TRANSITION_DURATION);
        header.setLayoutTransition(lt);

        findViewById(R.id.drop_zone_new_category).setOnDragListener(new DropZoneNewCategory());
        findViewById(R.id.drop_zone_app_info).setOnDragListener(new DropZoneAppInfo());
        findViewById(R.id.drop_zone_remove_shortcut).setOnDragListener(new DropZoneRemoveShortcut());

        LauncherPager appsPager = getPager();

        TabContainer tabContainer = (TabContainer)findViewById(R.id.tab_container);
        tabContainer.setupWithViewPager(appsPager);

        loadLauncherItems();
        appsPager.showCategory("HOME");


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

        // BroadcastReceivers
        installShortcutReceiver = new InstallShortcutReceiver(this);
        registerReceiver(installShortcutReceiver, new IntentFilter("com.android.launcher.action.INSTALL_SHORTCUT"));

        installAppReceiver = new InstallAppReceiver(this);
        IntentFilter appChangeFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        appChangeFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        appChangeFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        appChangeFilter.addDataScheme("package");
        registerReceiver(installAppReceiver, appChangeFilter);
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
        LauncherItem.saveDirty(this);
        cleanUnusedPrefKeysUrgent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanUnusedPrefKeysLazy();

        // BroadcastReceivers
        unregisterReceiver(installShortcutReceiver);
        unregisterReceiver(installAppReceiver);
    }

    @Override
    public void onBackPressed() { }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INSTALL_SHORTCUT_REQUEST && resultCode == RESULT_OK) {
            ShortcutItem newShortcut = ShortcutItem.shortcutFromIntent(this, data);
            getPager().moveLauncherItem(newShortcut, getPager().getCurrentCategoryName(), false);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadLauncherItems() {
        // apps
        AppItemManager.addAppItems(this, getPager());

        // shortcuts
        final File shortcutsDir = ShortcutItem.getShortcutsDir(this);
        shortcutsDir.mkdir();
        for (File file : shortcutsDir.listFiles()) {
            try {
                getPager().addLauncherItemBulk(ShortcutItem.fromFile(this, file));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        getPager().finishBulk();
    }

    private boolean addDefaultAppToHome(@NonNull final Intent intent) {
        // return true if successful
        final ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent, 0);
        return resolveInfo != null && addAppToHome(resolveInfo.activityInfo.packageName);
    }
    private void addAppToHome(@NonNull final AppItem appItem) {
        // always successful
        appItem.setCategory("HOME");
        appItem.persist(this);
    }
    private boolean addAppToHome(@NonNull final Intent launcherIntent) {
        // return true if successful
        final ResolveInfo resolveInfo = getPackageManager().resolveActivity(launcherIntent, 0);
        if (resolveInfo == null) {
            return false;
        }
        addAppToHome(new AppItem(resolveInfo.activityInfo));
        return true;
    }
    private boolean addAppToHome(@NonNull final String packageName) {
        // return true if successful
        final Intent launcherIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        return launcherIntent != null && addAppToHome(launcherIntent);
    }

    private void cleanUnusedPrefKeysLazy() {
        final PrefMap categories = new PrefMap(this, "categories");
        categories.clean(new Predicate<String>() {
            @Override
            public boolean apply(String s) {
                String packageName = s.substring(0, s.indexOf('\0'));
                return !doesPackageExist(packageName);
            }
        });
    }
    private void cleanUnusedPrefKeysUrgent() {
        final LauncherPager pager = getPager();
        final PrefMap categoryOrder = new PrefMap(this, "categoryorder");
        categoryOrder.clean(new Predicate<String>() {
            @Override
            public boolean apply(String s) {
                return !pager.doesCategoryExist(s);
            }
        });
    }

    private boolean doesPackageExist(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName,PackageManager.GET_META_DATA);
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

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        if (requestCode != LauncherPager.LAUNCH_PERMISSION || launchIntent == null) {
            return;
        }
        // check permissions
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                // only calling requires permission
                // if this changes, TODO: fix string no_permission
                final String errMessage = getString(R.string.no_permission);
                final Toast toast = Toast.makeText(this, errMessage, Toast.LENGTH_LONG);
                toast.show();
                return;
            }
        }
        // launch
        startActivity(launchIntent, launchOpts);
        this.launchIntent = null;
        this.launchOpts   = null;
    }

    @Override
    public void setDelayedLaunchIntent(@NonNull final Intent launchIntent, @NonNull final Bundle launchOpts) {
        this.launchIntent = launchIntent;
        this.launchOpts = launchOpts;
    }



    // actions

    public void openActionsMenu() {
        final ActionsSheet dialog = new ActionsSheet();
        dialog.show(getSupportFragmentManager(), "ActionsSheet");
    }
    public void openWallpaperPicker() {
        final Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
        startActivity(Intent.createChooser(intent, getString(R.string.wallpaper)));
    }
    public void showCreateShortcutDialog() {
        final Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.shortcut)), MainActivity.INSTALL_SHORTCUT_REQUEST);
    }
    public void renameCurrentCategory() {
        final RenameCategoryDialog dialog = new RenameCategoryDialog();
        final Bundle args = new Bundle();
        args.putString("categoryName", getPager().getCurrentCategoryName());
        dialog.setArguments(args);
        dialog.show(getFragmentManager(), "RenameCategoryDialog");
    }
    public void openSettings() {
        final Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    public void performAction(final int action) {
        switch (action) {
            case Preferences.ACTION_MENU:      openActionsMenu(); break;
            case Preferences.ACTION_WALLPAPER: openWallpaperPicker(); break;
            case Preferences.ACTION_SHORTCUT:  showCreateShortcutDialog(); break;
            case Preferences.ACTION_RENAME_CATEGORY: renameCurrentCategory(); break;
            case Preferences.ACTION_SETTINGS:  openSettings(); break;
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
        AppItemManager.addAppItems(this, getPager(), packageName);
    }

    @Override
    public void onAppChange(String packageName) {
        AppItemManager.removeAppItems(this, getPager(), packageName);
        AppItemManager.addAppItems(this, getPager(), packageName);
    }

    @Override
    public void onAppRemove(String packageName) {
        AppItemManager.removeAppItems(this, getPager(), packageName);
    }

    @Override
    public void onShortcutReceived(ShortcutItem newShortcut) {
        getPager().addLauncherItem(newShortcut);
    }



    // LauncherItem dragging
    private View draggedView = null;
    private LauncherItem draggedItem = null;
    private LauncherItem newCategoryDroppedItem = null;

    @Override
    public void onDragEnded() {
        if (draggedView != null) {
            draggedView.setAlpha(1.0f);
        }
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
//                case R.layout.activity_main_headerbtm: {
//                    getWindow().getDecorView().setSystemUiVisibility(0);
//                    break;
//                }
            }
        }
    }

    @Override
    public void onDragStarted(final View draggedView, final LauncherItem draggedItem) {
        draggedView.setAlpha(0.0f);
        this.draggedView = draggedView;
        this.draggedItem = draggedItem;

        // show drop zones
        findViewById(R.id.apps_pager).animate().alpha(0.2f);
        findViewById(R.id.category_drop_zone_container).setVisibility(View.VISIBLE);

        // show type specific drop zones
        findViewById(R.id.drop_zone_app_info)       .setVisibility(draggedItem instanceof AppItem      ? View.VISIBLE : View.GONE);
        findViewById(R.id.drop_zone_remove_shortcut).setVisibility(draggedItem instanceof ShortcutItem ? View.VISIBLE : View.GONE);

        // set header background
        dragBgAnimator.start();

        // hide status or navigation bar
        switch (Preferences.mainLayoutResource) {
            case R.layout.activity_main_headertop: {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;
            }
//            case R.layout.activity_main_headerbtm: {
//                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//                break;
//            }
        }
    }

    @Override
    public LauncherItem getDraggedItem() {
        return draggedItem;
    }

    @Override
    public void onDraggedItemRemove() {
        //assert draggedItem instanceof ShortcutItem;
        ShortcutItem shortcutItem = (ShortcutItem)draggedItem;
        getPager().removeLauncherItem(shortcutItem);
        shortcutItem.delete();
    }

    @Override
    public void onDraggedItemChangeCategory(String newCategoryName) {
        getPager().moveLauncherItem(draggedItem, newCategoryName, true);
    }

    @Override
    public void onDraggedItemNewCategory() {
        newCategoryDroppedItem = draggedItem;
        NewCategoryDialog dialog = new NewCategoryDialog();
        dialog.show(getFragmentManager(), "NewCategoryDialog");
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
