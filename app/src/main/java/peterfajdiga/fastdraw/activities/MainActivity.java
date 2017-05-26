package peterfajdiga.fastdraw.activities;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.io.File;

import peterfajdiga.fastdraw.R;
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
import peterfajdiga.fastdraw.views.LauncherPagerHeader;

public class MainActivity extends Activity implements
        InstallAppReceiver.Owner,
        InstallShortcutReceiver.Owner,
        DropZoneRemoveShortcut.Owner<LauncherItem>,
        DropZoneCategory.Owner<LauncherItem>,
        DropZoneNewCategory.Owner<LauncherItem>,
        DragStartListener.Owner<LauncherItem>,
        NewCategoryDialog.Owner,
        RenameCategoryDialog.Owner {

    public static final int INSTALL_SHORTCUT_REQUEST = 2143;

    private InstallShortcutReceiver installShortcutReceiver;
    private InstallAppReceiver installAppReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        // custom (faster) animate layout changes
        ViewGroup header = (ViewGroup)findViewById(R.id.header);
        LayoutTransition lt = new LayoutTransition();
        lt.setStartDelay(LayoutTransition.APPEARING, 0);
//        lt.setStartDelay(LayoutTransition.CHANGE_APPEARING, 200);
        lt.setDuration(LayoutTransition.CHANGE_APPEARING, 200);
        lt.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
        lt.setDuration(LayoutTransition.CHANGE_DISAPPEARING, 200);
        header.setLayoutTransition(lt);

        findViewById(R.id.drop_zone_new_category).setOnDragListener(new DropZoneNewCategory());
        findViewById(R.id.drop_zone_app_info).setOnDragListener(new DropZoneAppInfo());
        findViewById(R.id.drop_zone_remove_shortcut).setOnDragListener(new DropZoneRemoveShortcut());

        ViewPager appsPager = (ViewPager)findViewById(R.id.apps_pager);

        LauncherPagerHeader pagerHeader = (LauncherPagerHeader)findViewById(R.id.apps_pager_header);
        pagerHeader.setupWithViewPager(appsPager);

        loadLauncherItems();


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
        AppItemManager.addAppItems(this, getPager());

        // shortcuts
        File shortcutsDir = ShortcutItem.getShortcutsDir(this);
        shortcutsDir.mkdir();
        for (File file : shortcutsDir.listFiles()) {
            try {
                getPager().addLauncherItem(ShortcutItem.fromFile(this, file));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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



    // actions

    public void openWallpaperPicker() {
        Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
        startActivity(Intent.createChooser(intent, getString(R.string.wallpaper)));
    }



    // app management

    @Override
    public void onAppInstall(String packageName) {
        AppItemManager.addAppItems(this, getPager(), packageName);
    }

    @Override
    public void onAppChange(String packageName) {
        AppItemManager.removeAppItems(getPager(), packageName);
        AppItemManager.addAppItems(this, getPager(), packageName);
    }

    @Override
    public void onAppRemove(String packageName) {
        AppItemManager.removeAppItems(getPager(), packageName);
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
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            findViewById(R.id.apps_pager).animate().alpha(1.0f);
            findViewById(R.id.category_drop_zone_container).setVisibility(View.GONE);
            draggedItem = null;
        }
    }

    @Override
    public void onDragStarted(LauncherItem draggedItem) {
        this.draggedItem = draggedItem;

        // show drop zones
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        findViewById(R.id.apps_pager).animate().alpha(0.2f);
        findViewById(R.id.category_drop_zone_container).setVisibility(View.VISIBLE);

        // show type specific drop zones
        findViewById(R.id.drop_zone_app_info)       .setVisibility(draggedItem instanceof AppItem      ? View.VISIBLE : View.GONE);
        findViewById(R.id.drop_zone_remove_shortcut).setVisibility(draggedItem instanceof ShortcutItem ? View.VISIBLE : View.GONE);
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
