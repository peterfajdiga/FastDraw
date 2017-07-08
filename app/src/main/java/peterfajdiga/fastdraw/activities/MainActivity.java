package peterfajdiga.fastdraw.activities;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.io.File;

import peterfajdiga.fastdraw.NavigationBarAnimator;
import peterfajdiga.fastdraw.Preferences;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.ViewBgAnimator;
import peterfajdiga.fastdraw.dialogs.ActionsSheet;
import peterfajdiga.fastdraw.dialogs.NewCategoryDialog;
import peterfajdiga.fastdraw.dialogs.RenameCategoryDialog;
import peterfajdiga.fastdraw.launcher.AppItemManager;
import peterfajdiga.fastdraw.listeners.DragStartListener;
import peterfajdiga.fastdraw.listeners.DropZoneAppInfo;
import peterfajdiga.fastdraw.listeners.DropZoneCategory;
import peterfajdiga.fastdraw.listeners.DropZoneNewCategory;
import peterfajdiga.fastdraw.listeners.DropZoneRemoveShortcut;
import peterfajdiga.fastdraw.listeners.InstallAppReceiver;
import peterfajdiga.fastdraw.listeners.InstallShortcutReceiver;
import peterfajdiga.fastdraw.launcher.item.AppItem;
import peterfajdiga.fastdraw.launcher.item.LauncherItem;
import peterfajdiga.fastdraw.launcher.item.ShortcutItem;
import peterfajdiga.fastdraw.launcher.LauncherPager;
import peterfajdiga.fastdraw.views.TabContainer;

public class MainActivity extends FragmentActivity implements
        LauncherPager.Owner,
        InstallAppReceiver.Owner,
        InstallShortcutReceiver.Owner,
        DropZoneRemoveShortcut.Owner<LauncherItem>,
        DropZoneCategory.Owner<LauncherItem>,
        DropZoneNewCategory.Owner<LauncherItem>,
        DragStartListener.Owner<LauncherItem>,
        NewCategoryDialog.Owner,
        RenameCategoryDialog.Owner {

    public static final int INSTALL_SHORTCUT_REQUEST = 2143;

    private static final int DROPZONE_TRANSITION_DURATION = 200;

    private InstallShortcutReceiver installShortcutReceiver;
    private InstallAppReceiver installAppReceiver;

    private ValueAnimator dragBgAnimator;

    private static MainActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
        Preferences.loadPreferences(this);
        setContentView(Preferences.mainLayoutResource);

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

        ViewPager appsPager = (ViewPager)findViewById(R.id.apps_pager);

        TabContainer tabContainer = (TabContainer)findViewById(R.id.tab_container);
        tabContainer.setupWithViewPager(appsPager);

        loadLauncherItems();


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

    @Override
    public void onPause() {
        super.onPause();
        LauncherItem.saveDirty(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        forgetDeletedApps();

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
        AppItemManager.addAppItems(this, getPager(), true);

        // shortcuts
        File shortcutsDir = ShortcutItem.getShortcutsDir(this);
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

    private void forgetDeletedApps() {
        SharedPreferences prefs = getSharedPreferences("categories", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();

        for (String appItemId : prefs.getAll().keySet()) {
            String packageName = appItemId.substring(0, appItemId.indexOf('\0'));
            if (!doesPackageExist(packageName)) {
                prefsEditor.remove(appItemId);
            }
        }
        prefsEditor.apply();
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
        AppItemManager.addAppItems(this, getPager(), packageName, false);
    }

    @Override
    public void onAppChange(String packageName) {
        AppItemManager.removeAppItems(this, getPager(), packageName);
        AppItemManager.addAppItems(this, getPager(), packageName, false);
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

    private LauncherItem draggedItem = null;
    private LauncherItem newCategoryDroppedItem = null;

    @Override
    public void onDragEnded() {
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
    public void onDragStarted(LauncherItem draggedItem) {
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
