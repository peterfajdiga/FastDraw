package peterfajdiga.fastdraw.activities;

import static peterfajdiga.fastdraw.launcher.LaunchManager.PERMISSION_REQUEST_CODE;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.WallpaperColors;
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
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.provider.Telephony;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.tabs.TabLayout;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import peterfajdiga.fastdraw.Category;
import peterfajdiga.fastdraw.R;
import peterfajdiga.fastdraw.RunnableQueue;
import peterfajdiga.fastdraw.SettableBoolean;
import peterfajdiga.fastdraw.WallpaperColorUtils;
import peterfajdiga.fastdraw.dialogs.ActionsSheet;
import peterfajdiga.fastdraw.dialogs.CategorySelectionDialog;
import peterfajdiga.fastdraw.dialogs.DialogUtils;
import peterfajdiga.fastdraw.launcher.AppItemManager;
import peterfajdiga.fastdraw.launcher.DropZone;
import peterfajdiga.fastdraw.launcher.LaunchManager;
import peterfajdiga.fastdraw.launcher.Launcher;
import peterfajdiga.fastdraw.launcher.ShortcutItemManager;
import peterfajdiga.fastdraw.launcher.launcheritem.AppItem;
import peterfajdiga.fastdraw.launcher.launcheritem.BitmapShortcutItem;
import peterfajdiga.fastdraw.launcher.launcheritem.FiledShortcutItem;
import peterfajdiga.fastdraw.launcher.launcheritem.LauncherItem;
import peterfajdiga.fastdraw.launcher.launcheritem.OreoShortcutItem;
import peterfajdiga.fastdraw.launcher.launcheritem.ResShortcutItem;
import peterfajdiga.fastdraw.prefs.PrefMap;
import peterfajdiga.fastdraw.prefs.Preferences;
import peterfajdiga.fastdraw.receivers.InstallAppReceiver;
import peterfajdiga.fastdraw.views.CategoryTabLayout;
import peterfajdiga.fastdraw.views.Drawables;
import peterfajdiga.fastdraw.views.GestureInterceptor;
import peterfajdiga.fastdraw.views.NestedScrollParent;
import peterfajdiga.fastdraw.views.animators.ViewElevationAnimator;
import peterfajdiga.fastdraw.views.gestures.LongPress;
import peterfajdiga.fastdraw.views.gestures.OnTouchListenerMux;
import peterfajdiga.fastdraw.views.gestures.Swipe;
import peterfajdiga.fastdraw.widgets.WidgetManager;

public class MainActivity extends FragmentActivity implements CategorySelectionDialog.OnCategorySelectedListener {
    public static final int INSTALL_SHORTCUT_REQUEST = 2143;
    public static final int PICK_WIDGET_REQUEST = 2144;
    public static final int CREATE_WIDGET_REQUEST = 2145;

    private static final String PREFS_WIDGETS = "widgets";
    private static final String PREF_KEY_WIDGET_ID = "widget_id";

    private static final String CATEGORY_DIALOG_ACTION_NEW = "NewCategoryDialog";
    private static final String CATEGORY_DIALOG_ACTION_RENAME = "RenameCategoryDialog";
    private static final String LAUNCHER_ITEM_ID_KEY = "launcherItemId";
    private static final String INITIAL_CATEGORY_NAME_KEY = "categoryName";

    private static final int DROPZONE_TRANSITION_DURATION = 200;

    private InstallAppReceiver installAppReceiver;

    private ValueAnimator dragHeaderElevationAnimator;

    private static WeakReference<MainActivity> instance;
    private final LaunchManager launchManager = new LaunchManager(this);
    private final RunnableQueue dragEndService = new RunnableQueue();
    private Launcher launcher;
    private WidgetManager widgetManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = new WeakReference<>(this);
        onFirstRun();
        Preferences.loadPreferences(this);
        migrateCategoryNames();
        setContentView(Preferences.headerOnBottom ? R.layout.activity_main_headerbtm : R.layout.activity_main_headertop);
        if (Preferences.allowOrientation) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
            decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        final NestedScrollParent scrollParent = findViewById(R.id.scroll_parent);
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        final View.OnTouchListener longPressListener = new LongPress(displayMetrics, this::openActionsMenu);
        final View.OnTouchListener gesturesListener = new OnTouchListenerMux(
            longPressListener,
            new Swipe(displayMetrics, Swipe.Direction.DOWN, this::expandNotificationsPanel, () -> scrollParent.getScrollY() == 0)
        );

        setupWidgets(gesturesListener);
        setupAppsPager(scrollParent, longPressListener);
        setupInstallAppReceiver();

        final View contentView = findViewById(android.R.id.content);
        contentView.post(() -> {
            if (contentView.isAttachedToWindow()) {
                setupSystemBarsPadding(contentView);
                setupSystemBarsBgGradient(contentView);
            }
        });
        contentView.setOnApplyWindowInsetsListener((v, insets) -> {
            // this fixes the bug with missing top padding after exiting split screen mode
            // this fixes the bug with scroll_parent's height being limited after starting Fast Draw with the keyboard open
            contentView.post(() -> {
                if (contentView.isAttachedToWindow()) {
                    setupSystemBarsPadding(contentView);
                    setupSystemBarsBgGradient(contentView);
                }
            });
            return insets;
        });

        final Intent intent = getIntent();
        if (intent != null) {
            final String action = intent.getAction();
            if (action != null && action.equals(LauncherApps.ACTION_CONFIRM_PIN_SHORTCUT) && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                final OreoShortcutItem newShortcut = ShortcutItemManager.oreoShortcutFromIntent(this, intent);
                if (newShortcut != null) {
                    final String shortcutCategoryName = Category.shortcutsCategory;
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

    private void setupWidgets(final View.OnTouchListener gesturesListener) {
        widgetManager = new WidgetManager(this, 1, PICK_WIDGET_REQUEST, CREATE_WIDGET_REQUEST);
        widgetManager.startListening();

        loadPersistedWidget();

        final GestureInterceptor widgetContainer = findViewById(R.id.widget_container);
        widgetContainer.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            final AppWidgetHostView widgetView = getCurrentWidgetView(widgetContainer);
            if (widgetView != null) {
                final int widthPx = widgetView.getWidth();
                final int heightPx = widgetView.getHeight();
                final float dp = getResources().getDisplayMetrics().density;
                final int widthDp = Math.round(widthPx / dp);
                final int heightDp = Math.round(heightPx / dp);
                widgetView.updateAppWidgetSize(null, widthDp, heightDp, widthDp, heightDp);
            }
        });
        widgetContainer.setOnInterceptTouchListener(gesturesListener);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupAppsPager(final NestedScrollParent scrollParent, final View.OnTouchListener longPressListener) {
        final ViewPager appsPager = findViewById(R.id.apps_pager);
        launcher = new Launcher(launchManager, dragEndService, longPressListener, appsPager);

        setupWallpaperParallax(appsPager);
        setupHeader(appsPager);

        loadLauncherItems();
        launcher.showInitialCategory();

        scrollParent.setScrollChildManager(launcher.getScrollChildManager());
        scrollParent.setOnTouchListener(longPressListener);
        scrollParent.setOnOverScrollUpListener(this::expandNotificationsPanel);

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
        scrollParent.setOnMeasureListener((widthMeasureSpec, heightMeasureSpec) -> scrollExpand.setMinimumHeight(
            View.MeasureSpec.getSize(heightMeasureSpec) - scrollParent.getPaddingTop() - scrollParent.getPaddingBottom()
        ));

        scrollParent.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)(v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (dirty.value) {
                if (scrollY < oldScrollY) {
                    launcher.scrollUpAllExceptCurrent();
                }
                dirty.value = false;
            }
        });
    }

    private void setupWallpaperParallax(final ViewPager pager) {
        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);

        if (Preferences.wallpaperParallax) {
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
                    final PagerAdapter adapter = pager.getAdapter();
                    final float xOffset;
                    if (adapter == null || adapter.getCount() <= 1) {
                        xOffset = 0.5f;
                    } else {
                        xOffset = (position + positionOffset)/(adapter.getCount() - 1.0f);
                    }
                    wallpaperManager.setWallpaperOffsets(pager.getWindowToken(), xOffset, 0.5f);
                }

                @Override
                public void onPageSelected(final int position) {}

                @Override
                public void onPageScrollStateChanged(final int state) {}
            });
        } else {
            pager.post(() -> wallpaperManager.setWallpaperOffsets(pager.getWindowToken(), 0.5f, 0.5f));
        }
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

        setupHeaderBackground(header);

        // header elevation animator
        dragHeaderElevationAnimator = ValueAnimator.ofFloat(0.0f, getResources().getDimension(R.dimen.pager_header_expanded_elevation));
        dragHeaderElevationAnimator.setDuration(DROPZONE_TRANSITION_DURATION);
        dragHeaderElevationAnimator.addUpdateListener(new ViewElevationAnimator(header));
        dragHeaderElevationAnimator.setCurrentPlayTime(0);

        // header preferences
        if (Preferences.scrollableTabs) {
            tabContainer.setTabMode(TabLayout.MODE_SCROLLABLE);
        }
        if (Preferences.headerSeparator) {
            findViewById(R.id.header_separator).setVisibility(View.VISIBLE);
        }

        setupDropZones(tabContainer);
    }

    /**
     * use this instead of fitsSystemWindows=true, which causes weird glitches on
     * Samsung Galaxy S20 FE, Android 11 (perhaps on other devices as well)
     */
    private void setupSystemBarsPadding(final View contentView) {
        final WindowInsets windowInsets = getWindow().getDecorView().getRootWindowInsets();
        if (windowInsets == null) {
            throw new RuntimeException("Window is not attached");
        }
        contentView.setPadding(
            windowInsets.getSystemWindowInsetLeft(),
            windowInsets.getSystemWindowInsetTop(),
            windowInsets.getSystemWindowInsetRight(),
            windowInsets.getSystemWindowInsetBottom()
        );
    }

    @ColorInt
    private int applyBgGradientOpacity(@ColorInt final int color) {
        return color & ((Preferences.bgGradientOpacity << 24) | 0xffffff);
    }

    private void setupSystemBarsBgGradient(final View contentView) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1 && Preferences.bgGradientColorFromWallpaper) {
            final WallpaperManager wallpaperManager = (WallpaperManager)getSystemService(WALLPAPER_SERVICE);
            final WallpaperColors wallpaperColors = wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM);
            updateSystemBarsBgGradientColor(contentView, applyBgGradientOpacity(WallpaperColorUtils.getDarkColor(wallpaperColors)));
        } else {
            updateSystemBarsBgGradientColor(contentView, applyBgGradientOpacity(getBgGradientColor()));
        }
    }

    @ColorInt
    private int getBgGradientColor() {
        return MaterialColors.getColor(this, R.attr.bgGradientColor, Color.GRAY);
    }

    private void updateSystemBarsBgGradientColor(final View contentView, @ColorInt final int color) {
        final Resources res = getResources();
        final int bgGradientHeight = Math.round(res.getDimension(R.dimen.system_bar_bg_gradient_height));

        final boolean hasNavigationBar = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || hasNavigationBar();
        final int bgGradientHeightBottom = hasNavigationBar ? (
            Preferences.headerOnBottom ? Math.round(res.getDimension(R.dimen.system_bar_bg_gradient_height_large)) : bgGradientHeight
        ) : (
            Preferences.headerOnBottom ? bgGradientHeight : 0
        );

        contentView.setBackground(Drawables.createBgGradientDrawable(
            color,
            bgGradientHeight,
            bgGradientHeightBottom
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
        final View contentView = findViewById(android.R.id.content);
        setupSystemBarsPadding(contentView);
        setupSystemBarsBgGradient(contentView);
    }

    @Override
    public void onCategorySelected(final CategorySelectionDialog dialog, final String categoryName) {
        final Bundle args = dialog.requireArguments();
        final String tag = dialog.getTag();
        if (tag == null) {
            throw new RuntimeException("category dialog tag is null");
        }
        switch (tag) {
            case CATEGORY_DIALOG_ACTION_NEW: {
                launcher.moveItem(categoryName, args.getString(LAUNCHER_ITEM_ID_KEY));
                break;
            }
            case CATEGORY_DIALOG_ACTION_RENAME: {
                launcher.moveCategory(args.getString(INITIAL_CATEGORY_NAME_KEY), categoryName);
                break;
            }
            default: {
                throw new RuntimeException("Invalid category dialog tag: " + tag);
            }
        }
    }

    private void setupDropZones(final CategoryTabLayout tabContainer) {
        tabContainer.setOnDropListener((draggedItem, categoryName) -> launcher.moveItems(categoryName, draggedItem));
        tabContainer.setOnTabLongClickListener(categoryName -> {
            final CategorySelectionDialog dialog = CategorySelectionDialog.newInstance(getString(R.string.change_category_icon));
            DialogUtils.modifyArguments(dialog, args -> args.putString(INITIAL_CATEGORY_NAME_KEY, categoryName));
            dialog.show(getSupportFragmentManager(), CATEGORY_DIALOG_ACTION_RENAME);
            return true;
        });

        findViewById(R.id.drop_zone_new_category).setOnDragListener(new DropZone(
            (draggedItem) -> {
                final CategorySelectionDialog dialog = CategorySelectionDialog.newInstance(getString(R.string.new_category));
                DialogUtils.modifyArguments(dialog, args -> args.putString(LAUNCHER_ITEM_ID_KEY, draggedItem.getId()));
                dialog.show(getSupportFragmentManager(), CATEGORY_DIALOG_ACTION_NEW);
            },
            false
        ));

        findViewById(R.id.drop_zone_hide).setOnDragListener(new DropZone(
            (draggedItem) -> {
                launcher.moveItems(Category.hiddenCategory, draggedItem);
            },
            false
        ));

        findViewById(R.id.drop_zone_app_info).setOnDragListener(new DropZone(
            (draggedItem) -> {
                final AppItem appItem = (AppItem)draggedItem;
                appItem.openAppDetails(this);
            },
            false
        ));

        findViewById(R.id.drop_zone_remove_shortcut).setOnDragListener(new DropZone(
            (draggedItem) -> {
                final FiledShortcutItem shortcutItem = (FiledShortcutItem)draggedItem;
                launcher.removeItem(shortcutItem, true);
                ShortcutItemManager.deleteShortcut(this, shortcutItem);
            },
            true
        ));

        findViewById(android.R.id.content).setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED: {
                    if (event.getLocalState() instanceof LauncherItem) {
                        startDrag((LauncherItem)event.getLocalState());
                        return true;
                    }
                    return false;
                }
                case DragEvent.ACTION_DROP: { // fires immediately, but not always
                    endDrag(); // end drag immediately, without waiting for ACTION_DRAG_ENDED
                    return false;
                }
                case DragEvent.ACTION_DRAG_ENDED: { // fires always, but only after the drag shadow animation (the dragged item returning to its position) finishes
                    endDrag(); // end drag definitely
                    dragEndService.runAll();
                    return true;
                }
                default: {
                    return false;
                }
            }
        });
    }

    private void setupHeaderBackground(@NonNull final View header) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            final WallpaperManager wallpaperManager = (WallpaperManager)getSystemService(WALLPAPER_SERVICE);
            final WallpaperColors wallpaperColors = wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM);
            @ColorInt final int bgGradientColor = applyBgGradientOpacity(
                Preferences.bgGradientColorFromWallpaper ?
                WallpaperColorUtils.getDarkColor(wallpaperColors) :
                getBgGradientColor()
            );
            @ColorInt final int expandedHeaderColor = WallpaperColorUtils.getDarkAccentColor(wallpaperColors);
            updateHeaderColor(header, bgGradientColor, expandedHeaderColor);
            setupWallpaperColorListener(header, wallpaperManager);
        } else {
            @ColorInt final int bgGradientColor = getBgGradientColor();
            updateHeaderColor(header, bgGradientColor, bgGradientColor);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    private void setupWallpaperColorListener(@NonNull final View header, @NonNull final WallpaperManager wallpaperManager) {
        wallpaperManager.addOnColorsChangedListener((colors, which) -> {
            if ((which & WallpaperManager.FLAG_SYSTEM) != 0) {
                @ColorInt final int bgGradientColor = applyBgGradientOpacity(
                    Preferences.bgGradientColorFromWallpaper ?
                    WallpaperColorUtils.getDarkColor(colors) :
                    getBgGradientColor()
                );
                @ColorInt final int expandedHeaderColor = WallpaperColorUtils.getDarkAccentColor(colors);
                updateHeaderColor(header, bgGradientColor, expandedHeaderColor);
                if (header.isAttachedToWindow()) {
                    updateSystemBarsBgGradientColor(findViewById(android.R.id.content), bgGradientColor);
                }
            }
        }, null);
    }

    private void updateHeaderColor(@NonNull final View header, @ColorInt final int bgGradientColor, @ColorInt final int expandedColor) {
        header.setBackground(Drawables.createHeaderBackground(
            getResources(),
            bgGradientColor,
            expandedColor,
            !Preferences.headerOnBottom,
            Preferences.headerSeparator
        ));
    }

    private void setupInstallAppReceiver() {
        installAppReceiver = new InstallAppReceiver(new InstallAppReceiver.Owner() {
            @Override
            public void onAppInstall(final String packageName) {
                final AppItem[] appItems = AppItemManager.getAppItems(getPackageManager(), packageName).toArray(AppItem[]::new);
                launcher.addItems(appItems);
            }

            @Override
            public void onAppChange(final String packageName) {
                final Stream<AppItem> updatedAppItems = AppItemManager.getAppItems(getPackageManager(), packageName);
                AppItemManager.updatePackageItems(launcher, packageName, updatedAppItems);
            }

            @Override
            public void onAppRemove(final String packageName) {
                AppItemManager.removePackageItems(launcher, packageName);
            }
        });

        final IntentFilter appChangeFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        appChangeFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        appChangeFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        appChangeFilter.addDataScheme("package");
        registerReceiver(installAppReceiver, appChangeFilter);
    }

    private void loadPersistedWidget() {
        final PrefMap widgetPrefs = new PrefMap(this, PREFS_WIDGETS);
        final int widgetId = widgetPrefs.getInt(PREF_KEY_WIDGET_ID, -1);
        if (widgetId != -1) {
            final AppWidgetHostView widgetView = widgetManager.createWidgetView(widgetId);
            if (widgetView != null) {
                final ViewGroup widgetContainer = findViewById(R.id.widget_container);
                replaceWidgetView(widgetContainer, widgetView);
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
            launcher.showInitialCategory();

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

        cleanUnusedPrefKeysAppsCategories();

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
                    final FiledShortcutItem newShortcut = ShortcutItemManager.shortcutFromIntent(this, data);
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

    private void addShortcut(@NonNull final FiledShortcutItem shortcutItem, @NonNull final String categoryName) {
        ShortcutItemManager.saveShortcut(this, shortcutItem);
        launcher.moveItems(categoryName, shortcutItem);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addOreoShortcut(@NonNull final OreoShortcutItem shortcutItem, @NonNull final String categoryName) {
        launcher.moveItems(categoryName, shortcutItem);
    }

    private void setWidget(@NonNull AppWidgetHostView widgetView) {
        final ViewGroup widgetContainer = findViewById(R.id.widget_container);
        replaceWidgetView(widgetContainer, widgetView);
        final PrefMap widgetPrefs = new PrefMap(this, PREFS_WIDGETS);
        widgetPrefs.putInt(PREF_KEY_WIDGET_ID, widgetView.getAppWidgetId());
    }

    public void removeWidget() {
        final ViewGroup widgetContainer = findViewById(R.id.widget_container);
        removeWidgetView(widgetContainer);
        final PrefMap widgetPrefs = new PrefMap(this, PREFS_WIDGETS);
        widgetPrefs.remove(PREF_KEY_WIDGET_ID);
    }

    private void replaceWidgetView(@NonNull final ViewGroup widgetContainer, @NonNull final AppWidgetHostView widgetView) {
        removeWidgetView(widgetContainer);

        final Resources res = getResources();
        final float height = Math.min(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                Preferences.widgetHeight,
                res.getDisplayMetrics()
            ),
            res.getDisplayMetrics().heightPixels * 0.75f // TODO: handle landscape orientation
        );
        final int margin = Math.round(res.getDimension(R.dimen.widget_margin));

        final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            Math.round(height),
            Gravity.CENTER
        );
        layoutParams.topMargin = margin;
        layoutParams.bottomMargin = margin;
        layoutParams.leftMargin = margin;
        layoutParams.rightMargin = margin;

        widgetContainer.addView(widgetView, layoutParams);
    }

    private void removeWidgetView(@NonNull final ViewGroup widgetContainer) {
        final AppWidgetHostView oldWidgetView = getCurrentWidgetView(widgetContainer);
        if (oldWidgetView != null) {
            widgetManager.deleteWidget(oldWidgetView.getAppWidgetId());
            widgetContainer.removeView(oldWidgetView);
        }
    }

    @Nullable
    private static AppWidgetHostView getCurrentWidgetView(final ViewGroup widgetContainer) {
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

        launcher.addItemsStartup(items);
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
        categoriesMap.putString(appItem.getId(), Launcher.HOME_CATEGORY_NAME);
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
            final MainActivity activity = instance.get();
            if (activity != null) {
                activity.finish();
            }
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

    public void setHiddenVisibility(final boolean visible) {
        // TODO: refactor
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("hideHidden", !visible).apply();
        recreate();
    }

    public void openSettings() {
        final Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void expandNotificationsPanel() {
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

    public void onShortcutReceived(final FiledShortcutItem newShortcut) {
        launcher.addItems(newShortcut);
    }

    private void startDrag(final LauncherItem draggedItem) {
        // show type specific drop zones
        findViewById(R.id.drop_zone_app_info).setVisibility(draggedItem instanceof AppItem ? View.VISIBLE : View.GONE);
        findViewById(R.id.drop_zone_hide).setVisibility(draggedItem instanceof AppItem ? View.VISIBLE : View.GONE);
        findViewById(R.id.drop_zone_remove_shortcut).setVisibility(draggedItem instanceof FiledShortcutItem ? View.VISIBLE : View.GONE);

        // show drop zones
        findViewById(R.id.apps_pager).animate().alpha(0.2f);
        findViewById(R.id.widget_container).animate().alpha(0.2f);
        findViewById(R.id.category_drop_zone_container).setVisibility(View.VISIBLE);

        // set header background
        dragHeaderElevationAnimator.start();
        final Drawable background = findViewById(R.id.header).getBackground();
        if (background instanceof TransitionDrawable) {
            final TransitionDrawable backgroundTransition = (TransitionDrawable)background;
            backgroundTransition.startTransition(DROPZONE_TRANSITION_DURATION);
        }
    }

    private void endDrag() {
        // hide drop zones
        findViewById(R.id.apps_pager).animate().alpha(1.0f);
        findViewById(R.id.widget_container).animate().alpha(1.0f);
        findViewById(R.id.category_drop_zone_container).setVisibility(View.GONE);

        // reset header background
        if (dragHeaderElevationAnimator.getAnimatedFraction() > 0.0f) {
            dragHeaderElevationAnimator.reverse();
            final Drawable background = findViewById(R.id.header).getBackground();
            if (background instanceof TransitionDrawable) {
                final TransitionDrawable backgroundTransition = (TransitionDrawable)background;
                backgroundTransition.reverseTransition(DROPZONE_TRANSITION_DURATION);
            }
        }
    }

    private void migrateCategoryNames() {
        final PrefMap itemCategoryMap = new PrefMap(this, "categories");
        for (final String key : itemCategoryMap.getKeys()) {
            final String currentCategoryName = itemCategoryMap.getString(key, "");
            final String newCategoryName = Category.oldToNewCategoryName(currentCategoryName);
            if (!newCategoryName.equals(currentCategoryName)) {
                itemCategoryMap.putString(key, newCategoryName);
            }
        }
    }
}
