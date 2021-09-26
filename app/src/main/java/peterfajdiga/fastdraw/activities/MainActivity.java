package peterfajdiga.fastdraw.activities;

import static peterfajdiga.fastdraw.launcher.LaunchManager.PERMISSION_REQUEST_CODE;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Telephony;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import peterfajdiga.fastdraw.PrefMap;
import peterfajdiga.fastdraw.Preferences;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.SettableBoolean;
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
import peterfajdiga.fastdraw.views.Drawables;
import peterfajdiga.fastdraw.views.NestedScrollParent;
import peterfajdiga.fastdraw.views.animators.NavigationBarColorAnimator;
import peterfajdiga.fastdraw.views.animators.ViewElevationAnimator;
import peterfajdiga.fastdraw.widgets.WidgetManager;

public class MainActivity extends FragmentActivity implements
    Launcher.Listener,
    InstallAppReceiver.Owner,
    DropZoneRemoveShortcut.Owner<LauncherItem>,
    DropZoneCategory.Owner<LauncherItem>,
    DropZoneNewCategory.Owner<LauncherItem>,
    NewCategoryDialog.Listener,
    RenameCategoryDialog.Listener {

    public static final int INSTALL_SHORTCUT_REQUEST = 2143;
    public static final int PICK_WIDGET_REQUEST = 2144;
    public static final int CREATE_WIDGET_REQUEST = 2145;

    private static final int DROPZONE_TRANSITION_DURATION = 200;

    private InstallAppReceiver installAppReceiver;

    private ValueAnimator dragBgAnimator;

    private static WeakReference<MainActivity> instance;
    private final LaunchManager launchManager = new LaunchManager(this);
    private Launcher launcher;
    private WidgetManager widgetManager;

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

        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        setupWidgets();
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

    private void setupWidgets() {
        widgetManager = new WidgetManager(this, 1, PICK_WIDGET_REQUEST, CREATE_WIDGET_REQUEST);
        widgetManager.startListening();

        loadPersistedWidget();

        final FrameLayout frameLayout = findViewById(R.id.widget_container);
        frameLayout.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            final AppWidgetHostView widgetView = getCurrentWidgetView(frameLayout);
            if (widgetView != null) {
                final int widthPx = frameLayout.getWidth() - frameLayout.getPaddingLeft() - frameLayout.getPaddingRight();
                final int heightPx = frameLayout.getHeight() - frameLayout.getPaddingTop() - frameLayout.getPaddingBottom();
                final float dp = getResources().getDisplayMetrics().density;
                final int widthDp = Math.round(widthPx / dp);
                final int heightDp = Math.round(heightPx / dp);
                widgetView.updateAppWidgetSize(null, widthDp, heightDp, widthDp, heightDp);
            }
        });
    }

    private void setupAppsPager() {
        ViewPager appsPager = findViewById(R.id.apps_pager);
        launcher = new Launcher(launchManager, this, appsPager);

        setupWallpaperMovement(appsPager);
        setupHeader(appsPager);

        loadLauncherItems();
        launcher.showCategory(Launcher.HOME_CATEGORY_NAME);

        final NestedScrollParent scrollParent = findViewById(R.id.scroll_parent);
        scrollParent.setScrollChildManager(launcher.getScrollChildManager());

        final SettableBoolean dirty = new SettableBoolean(false);
        appsPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {}

            @Override
            public void onPageSelected(final int position) {
                dirty.value = true;
                scrollParent.setScrollChildManager(launcher.getScrollChildManager());
            }

            @Override
            public void onPageScrollStateChanged(final int state) {}
        });

        final View scrollExpand = findViewById(R.id.scroll_expand);
        scrollParent.setOnMeasureListener(() -> scrollExpand.setMinimumHeight(
            scrollParent.getMeasuredHeight() - scrollParent.getPaddingTop() - scrollParent.getPaddingBottom()
        )); // TODO: handle orientation change
        scrollParent.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)(v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (dirty.value) {
                if (scrollY < oldScrollY) {
                    launcher.scrollUpAllExceptCurrent();
                }
                dirty.value = false;
            }
        });
    }

    private void setupWallpaperMovement(final ViewPager pager) {
        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
                final PagerAdapter adapter = pager.getAdapter();
                final float xOffset;
                if (adapter == null || adapter.getCount() <= 1) {
                    xOffset = 0.5f;
                } else {
                    xOffset = (position + positionOffset) / (adapter.getCount() - 1.0f);
                }
                wallpaperManager.setWallpaperOffsets(pager.getWindowToken(), xOffset, 0.5f);
            }

            @Override
            public void onPageSelected(final int position) {}

            @Override
            public void onPageScrollStateChanged(final int state) {}
        });
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

        header.setBackground(Drawables.createHeaderBackground(
            getResources(),
            Preferences.headerBgColor,
            Preferences.headerBgColorExpanded,
            !Preferences.headerOnBottom,
            Preferences.headerSeparator && !Preferences.headerOnBottom
        ));

        // header animator
        dragBgAnimator = ValueAnimator.ofArgb(Preferences.headerBgColor, Preferences.headerBgColorExpanded);
        dragBgAnimator.setDuration(DROPZONE_TRANSITION_DURATION);
        if (Preferences.headerOnBottom) {
            dragBgAnimator.addUpdateListener(new NavigationBarColorAnimator(getWindow()));
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

        setupDropZones();
    }

    private void setupSystemBarsScrim() {
        final View scrollParent = findViewById(android.R.id.content);
        scrollParent.setBackground(Drawables.createScrimBackground(
            getResources(),
            Preferences.headerBgColor,
            !Preferences.headerOnBottom && (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || hasNavigationBar())
        ));
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private boolean hasNavigationBar() {
        final WindowInsets windowInsets = getWindow().getDecorView().getRootWindowInsets();
        if (windowInsets == null) {
            throw new RuntimeException("Window is not attached");
        }
        return windowInsets.getTappableElementInsets().bottom > 0;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setupSystemBarsScrim();
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

    private void loadPersistedWidget() {
        final PrefMap widgetPrefs = new PrefMap(this, "widgets");
        final int widgetId = widgetPrefs.getInt("widget_id", -1);
        if (widgetId != -1) {
            final AppWidgetHostView widgetView = widgetManager.createWidgetView(widgetId);
            if (widgetView != null) {
                replaceWidgetView(widgetView);
            }
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
            final Launcher pager = launcher;
            pager.showInitialCategory();

            // scroll to top
            final NestedScrollParent scrollParent = findViewById(R.id.scroll_parent);
            scrollParent.smoothScrollTo(0, 0);
            launcher.smoothScrollUpCurrent();
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

        try {
            widgetManager.stopListening();
        } catch (final NullPointerException ex) {
            Log.w("MainActivity", "problem while stopping AppWidgetHost during Launcher destruction", ex);
        }
    }

    @Override
    public void onBackPressed() { }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case INSTALL_SHORTCUT_REQUEST: {
                if (resultCode == RESULT_OK) {
                    final ShortcutItem newShortcut = ShortcutItemManager.shortcutFromIntent(this, data);
                    addShortcut(newShortcut, launcher.getCurrentCategoryName());
                }
                return;
            }
            case PICK_WIDGET_REQUEST: {
                final int widgetId = data.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
                assert widgetId > -1;
                switch (resultCode) {
                    case RESULT_OK: {
                        final AppWidgetHostView widgetView = widgetManager.createOrConfigureWidgetView(widgetId);
                        if (widgetView != null) {
                            setWidget(widgetView);
                        }
                        break;
                    }
                    case RESULT_CANCELED:{
                        widgetManager.deleteWidget(widgetId);
                        break;
                    }
                }
                return;
            }
            case CREATE_WIDGET_REQUEST: {
                final int widgetId = data.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
                assert widgetId > -1;
                switch (resultCode) {
                    case RESULT_OK: {
                        final AppWidgetHostView widgetView = widgetManager.createWidgetView(widgetId);
                        if (widgetView != null) {
                            setWidget(widgetView);
                        }
                        break;
                    }
                    case RESULT_CANCELED: {
                        widgetManager.deleteWidget(widgetId);
                        break;
                    }
                }
                return;
            }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
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

    private void setWidget(@NonNull AppWidgetHostView widgetView) {
        replaceWidgetView(widgetView);
        final PrefMap widgetPrefs = new PrefMap(this, "widgets"); // TODO: "widgets" constant
        widgetPrefs.putInt("widget_id", widgetView.getAppWidgetId()); // TODO: "widget_id" constant
    }

    private void replaceWidgetView(@NonNull AppWidgetHostView widgetView) {
        final FrameLayout widgetContainer = findViewById(R.id.widget_container);
        final AppWidgetHostView oldWidgetView = getCurrentWidgetView(widgetContainer);
        if (oldWidgetView != null) {
            widgetManager.deleteWidget(oldWidgetView.getAppWidgetId());
            widgetContainer.removeView(oldWidgetView);
        }

        final float height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            Preferences.widgetHeight,
            getResources().getDisplayMetrics()
        ); // TODO: limit max height

        widgetContainer.addView(widgetView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            Math.round(height),
            Gravity.CENTER
        ));
    }

    @Nullable
    private static AppWidgetHostView getCurrentWidgetView(final FrameLayout widgetContainer) {
        final int n = widgetContainer.getChildCount();
        for (int i = 0; i < n; i++) {
            final View child = widgetContainer.getChildAt(i);
            if (child instanceof AppWidgetHostView) {
                return (AppWidgetHostView)child;
            }
        }
        return null;
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

    public void showCreateWidgetDialog() {
        widgetManager.pickWidget();
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
        silhouettePaint.setColorFilter(new LightingColorFilter(Color.BLACK, Color.BLACK));
        draggedView.setLayerType(View.LAYER_TYPE_SOFTWARE, silhouettePaint);

        this.draggedView = draggedView;
        this.draggedItem = draggedItem;

        // show drop zones
        findViewById(R.id.apps_pager).animate().alpha(0.2f);
        findViewById(R.id.widget_container).animate().alpha(0.2f);
        findViewById(R.id.category_drop_zone_container).setVisibility(View.VISIBLE);

        // show type specific drop zones
        findViewById(R.id.drop_zone_app_info).setVisibility(draggedItem instanceof AppItem ? View.VISIBLE : View.GONE);
        findViewById(R.id.drop_zone_remove_shortcut).setVisibility(draggedItem instanceof ShortcutItem ? View.VISIBLE : View.GONE);

        // set header background
        dragBgAnimator.start();
        final Drawable background = findViewById(R.id.header).getBackground();
        if (background instanceof TransitionDrawable) {
            final TransitionDrawable backgroundTransition = (TransitionDrawable)background;
            backgroundTransition.startTransition(DROPZONE_TRANSITION_DURATION);
        }
    }

    public void onDragEnded1() {
        if (draggedItem != null) {
            // hide drop zones
            findViewById(R.id.apps_pager).animate().alpha(1.0f);
            findViewById(R.id.widget_container).animate().alpha(1.0f);
            findViewById(R.id.category_drop_zone_container).setVisibility(View.GONE);
            draggedItem = null;

            // reset header background
            dragBgAnimator.reverse();
            final Drawable background = findViewById(R.id.header).getBackground();
            if (background instanceof TransitionDrawable) {
                final TransitionDrawable backgroundTransition = (TransitionDrawable)background;
                backgroundTransition.reverseTransition(DROPZONE_TRANSITION_DURATION);
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
