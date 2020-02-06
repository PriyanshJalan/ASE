package com.mapbox.mapboxandroiddemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.perf.metrics.AddTrace;
import com.mapbox.mapboxandroiddemo.account.LandingActivity;
import com.mapbox.mapboxandroiddemo.adapter.ExampleAdapter;
import com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker;
import com.mapbox.mapboxandroiddemo.commons.FirstTimeRunChecker;
import com.mapbox.mapboxandroiddemo.examples.basics.KotlinSimpleMapViewActivity;
import com.mapbox.mapboxandroiddemo.examples.basics.KotlinSupportMapFragmentActivity;
import com.mapbox.mapboxandroiddemo.examples.basics.MapboxMapOptionActivity;
import com.mapbox.mapboxandroiddemo.examples.basics.SimpleMapViewActivity;
import com.mapbox.mapboxandroiddemo.examples.basics.SupportMapFragmentActivity;
import com.mapbox.mapboxandroiddemo.examples.camera.AnimateMapCameraActivity;
import com.mapbox.mapboxandroiddemo.examples.camera.BoundingBoxCameraActivity;
import com.mapbox.mapboxandroiddemo.examples.camera.RestrictCameraActivity;
import com.mapbox.mapboxandroiddemo.examples.camera.SlowlyRotatingCameraActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.AddRainFallStyleActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.BathymetryActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.ChoroplethJsonVectorMixActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.ChoroplethZoomChangeActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.CircleLayerClusteringActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.CircleRadiusActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.CircleToIconTransitionActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.CreateHotspotsActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.DrawGeojsonLineActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.DrawPolygonActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.ExpressionIntegrationActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.HeatmapActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.ImageClusteringActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.InfoWindowSymbolLayerActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.KotlinStyleCirclesCategoricallyActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.LineGradientActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.MultipleGeometriesActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.MultipleHeatmapStylingActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.PolygonHolesActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.PolygonSelectToggleActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.RevealedPolygonHoleOutlineActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.SatelliteLandSelectActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.StyleCirclesCategoricallyActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.StyleLineIdentityPropertyActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.SymbolCollisionDetectionActivity;
import com.mapbox.mapboxandroiddemo.examples.dds.SymbolSwitchOnZoomActivity;
import com.mapbox.mapboxandroiddemo.examples.extrusions.AdjustExtrusionLightActivity;
import com.mapbox.mapboxandroiddemo.examples.extrusions.Indoor3DMapActivity;
import com.mapbox.mapboxandroiddemo.examples.extrusions.MarathonExtrusionActivity;
import com.mapbox.mapboxandroiddemo.examples.extrusions.PopulationDensityExtrusionActivity;
import com.mapbox.mapboxandroiddemo.examples.extrusions.RotationExtrusionActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.DirectionsActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.DirectionsGradientLineActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.ElevationQueryActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.GeocodingActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.IsochroneActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.IsochroneSeekbarActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.MapMatchingActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.MatrixApiActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.MultipleGeometriesDirectionsRouteActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.OptimizationActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.SimplifyPolylineActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.StaticImageActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.TilequeryActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.TurfLineDistanceActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.TurfPhysicalCircleActivity;
import com.mapbox.mapboxandroiddemo.examples.javaservices.TurfRingActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.AnimatedImageGifActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.AnimatedMarkerActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.CalendarIntegrationActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.ChangeAttributionColorActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.DashedLineDirectionsPickerActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.HomeScreenWidgetActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.IndoorMapActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.InsetMapActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.LocationPickerActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.MagicWindowKotlinActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.MapFogBackgroundActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.MarkerFollowingRouteActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.MovingIconWithTrailingLineActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.PictureInPictureActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.PulsingLayerOpacityColorActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.RecyclerViewDirectionsActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.RecyclerViewOnMapActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.SharedPreferencesActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.SnakingDirectionsRouteActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.SpaceStationLocationActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.SpinningSymbolLayerIconActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.SymbolLayerMapillaryActivity;
import com.mapbox.mapboxandroiddemo.examples.labs.ValueAnimatorIconAnimationActivity;
import com.mapbox.mapboxandroiddemo.examples.location.KotlinLocationComponentActivity;
import com.mapbox.mapboxandroiddemo.examples.location.LocationChangeListeningActivity;
import com.mapbox.mapboxandroiddemo.examples.location.LocationComponentActivity;
import com.mapbox.mapboxandroiddemo.examples.location.LocationComponentCameraOptionsActivity;
import com.mapbox.mapboxandroiddemo.examples.location.LocationComponentFragmentActivity;
import com.mapbox.mapboxandroiddemo.examples.location.LocationComponentOptionsActivity;
import com.mapbox.mapboxandroiddemo.examples.offline.CacheManagementActivity;
import com.mapbox.mapboxandroiddemo.examples.offline.OfflineManagerActivity;
import com.mapbox.mapboxandroiddemo.examples.offline.SimpleOfflineMapActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.BuildingPluginActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.LocalizationPluginActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.MarkerViewPluginActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.PlacesPluginActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.ScalebarPluginActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.SymbolListenerActivity;
import com.mapbox.mapboxandroiddemo.examples.plugins.TrafficPluginActivity;
import com.mapbox.mapboxandroiddemo.examples.query.BuildingOutlineActivity;
import com.mapbox.mapboxandroiddemo.examples.query.ClickOnLayerActivity;
import com.mapbox.mapboxandroiddemo.examples.query.FeatureCountActivity;
import com.mapbox.mapboxandroiddemo.examples.query.FingerDrawQueryActivity;
import com.mapbox.mapboxandroiddemo.examples.query.HighlightedLineActivity;
import com.mapbox.mapboxandroiddemo.examples.query.QueryFeatureActivity;
import com.mapbox.mapboxandroiddemo.examples.query.RedoSearchInAreaActivity;
import com.mapbox.mapboxandroiddemo.examples.query.SelectBuildingActivity;
import com.mapbox.mapboxandroiddemo.examples.snapshot.SnapshotNotificationActivity;
import com.mapbox.mapboxandroiddemo.examples.snapshot.SnapshotShareActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.AddWmsSourceActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.AdjustLayerOpacityActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.BasicSymbolLayerActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.ClickToAddImageActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.ColorSwitcherActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.DefaultStyleActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.GeojsonLayerInStackActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.HillShadeActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.IconSizeChangeOnClickActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.ImageSourceActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.ImageSourceTimeLapseActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.LanguageSwitchActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.LineLayerActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.LocalStyleSourceActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.MapboxStudioStyleActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.MissingIconActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.RotatingTextAnchorPositionActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.RuntimeStylingActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.SatelliteOpacityOnZoomActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.ShowHideLayersActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.TextFieldFormattingActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.TextFieldMultipleFormatsActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.TransparentBackgroundActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.VariableLabelPlacementActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.VectorSourceActivity;
import com.mapbox.mapboxandroiddemo.examples.styles.ZoomDependentFillColorActivity;
import com.mapbox.mapboxandroiddemo.model.ExampleItemModel;
import com.mapbox.mapboxandroiddemo.utils.ItemClickSupport;
import com.mapbox.mapboxandroiddemo.utils.SettingsDialogView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.CLICKED_ON_INFO_DIALOG_NOT_NOW;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.CLICKED_ON_INFO_DIALOG_START_LEARNING;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.CLICKED_ON_INFO_MENU_ITEM;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.CLICKED_ON_SETTINGS_IN_NAV_DRAWER;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.OPENED_APP;
import static com.mapbox.mapboxandroiddemo.commons.AnalyticsTracker.SKIPPED_ACCOUNT_CREATION;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.FROM_LOGIN_SCREEN_MENU_ITEM_KEY;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.SKIPPED_KEY;
import static com.mapbox.mapboxandroiddemo.commons.StringConstants.TOKEN_SAVED_KEY;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
  ItemClickSupport.OnItemClickListener {
  // Used to track internal navigation to the Snapshotter section
  private static final String EXTRA_NAV = "EXTRA_NAV";
  private static final String STATE_CURRENT_CATEGORY = "STATE_CURRENT_CATEGORY";
  private static final String STATE_TOOLBAR_TITLE = "STATE_TOOLBAR_TITLE";
  private static final String STATE_SHOW_JAVA = "STATE_SHOW_JAVA";
  private static final String TAG = "MainActivity";

  private final ArrayList<ExampleItemModel> exampleItemModels = new ArrayList<>();

  private Toolbar toolbar;
  private String categoryTitleForToolbar;

  private AnalyticsTracker analytics;

  private ExampleAdapter adapter;
  private RecyclerView recyclerView;
  private TextView noExamplesTv;
  private ItemClickSupport itemClickSupport;

  private boolean loggedIn;
  private int currentCategory = R.id.nav_query_map;
  private boolean showJavaExamples = true;

  @Override
  @AddTrace(name = "onCreateMainActivity")
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    loggedIn = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
      .getBoolean(TOKEN_SAVED_KEY, false);

    toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    noExamplesTv = findViewById(R.id.no_examples_tv);

    analytics = AnalyticsTracker.getInstance(this, false);

    initializeModels();

    // Create the adapter to convert the array to views
    adapter = new ExampleAdapter(this);
    // Attach the adapter to a ListView
    recyclerView = findViewById(R.id.details_list);
    if (recyclerView != null) {
      recyclerView.setHasFixedSize(true);
      recyclerView.setLayoutManager(new LinearLayoutManager(this));
      recyclerView.setAdapter(adapter);
    }
    if (savedInstanceState != null) {
      currentCategory = savedInstanceState.getInt(STATE_CURRENT_CATEGORY);
      categoryTitleForToolbar = savedInstanceState.getString(STATE_TOOLBAR_TITLE);
      showJavaExamples = savedInstanceState.getBoolean(STATE_SHOW_JAVA);
      toolbar.setTitle(categoryTitleForToolbar);
      listItems(currentCategory);
    } else {
      listItems(R.id.nav_query_map);
    }

    // Item click listener
    itemClickSupport = ItemClickSupport.addTo(recyclerView);

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
      this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    if (drawer != null) {
      drawer.addDrawerListener(toggle);
    }
    toggle.syncState();

    NavigationView navigationView = findViewById(R.id.nav_view);
    if (navigationView != null) {
      navigationView.setNavigationItemSelectedListener(this);
    }


    if (loggedIn) {
      analytics.setMapboxUsername();
      analytics.viewedScreen(MainActivity.class.getSimpleName(), loggedIn);
      checkForFirstTimeOpen();
    } else {
      analytics.trackEvent(SKIPPED_ACCOUNT_CREATION, false);
      PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
        .putBoolean(SKIPPED_KEY, true)
        .apply();
    }
    analytics.trackEvent(OPENED_APP, loggedIn);
  }

  @Override
  public void onItemClicked(RecyclerView recyclerView, int position, View view) {
    ExampleItemModel model = adapter.getItemAt(position);

    // in case it's an info tile
    if (model != null) {

      if (showJavaExamples) {
        startActivity(model.getJavaActivity());
      } else {
        startActivity(model.getKotlinActivity());
      }

      analytics.clickedOnIndividualExample(getString(model.getTitle()), loggedIn);
      analytics.viewedScreen(getString(model.getTitle()), loggedIn);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    itemClickSupport.setOnItemClickListener(this);
  }

  @Override
  protected void onPause() {
    super.onPause();
    itemClickSupport.setOnItemClickListener(null);
  }

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    if (drawer != null) {
      if (drawer.isDrawerOpen(GravityCompat.START)) {
        drawer.closeDrawer(GravityCompat.START);
      } else {
        moveTaskToBack(true);
      }
    }
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    // Handle navigation view item clicks here.
    int id = item.getItemId();

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    if (drawer != null) {
      drawer.closeDrawer(GravityCompat.START);
    }
    return true;
  }

  @AddTrace(name = "listItems")
  private void listItems(int id) {
    List<ExampleItemModel> models = new ArrayList<>();
    for (ExampleItemModel model : exampleItemModels) {
      if (model.getCategoryId() == id && verifySdkVersion(model)) {
        if ((showJavaExamples && model.getJavaActivity() != null)
          || !showJavaExamples && model.getKotlinActivity() != null) {
          models.add(model);
        }
      }

    }

    adapter.updateDataSet(models, currentCategory);

    // Scrolls recycler view back to top.
    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    layoutManager.scrollToPositionWithOffset(0, 0);

    noExamplesTv.setVisibility(models.size() == 0 ? View.VISIBLE : View.GONE);
  }

  private boolean verifySdkVersion(ExampleItemModel model) {
    return model == null || Build.VERSION.SDK_INT >= model.getMinSdkVersion();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate toolbar items
    getMenuInflater().inflate(R.menu.menu_activity_main, menu);
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    MenuItem item = menu.findItem(R.id.action_show_other_language);
    if (showJavaExamples) {
      item.setTitle(R.string.examples_language_kotlin);
    } else {
      item.setTitle(R.string.examples_language_java);
    }
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_info) {
      analytics.trackEvent(CLICKED_ON_INFO_MENU_ITEM,
        loggedIn);
      new MaterialStyledDialog.Builder(MainActivity.this)
        .setTitle(getString(R.string.info_dialog_title))
        .setDescription(getString(R.string.info_dialog_description))
        .setHeaderColor(R.color.mapboxBlue)
        .withDivider(true)
        .setPositiveText(getString(R.string.info_dialog_positive_button_text))
        .onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override
          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            analytics.trackEvent(CLICKED_ON_INFO_DIALOG_START_LEARNING, false);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://mapbox.com/android-sdk"));
            startActivity(intent);
          }
        })
        .setNegativeText(getString(R.string.info_dialog_negative_button_text))
        .onNegative(new MaterialDialog.SingleButtonCallback() {
          @Override
          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            analytics.trackEvent(CLICKED_ON_INFO_DIALOG_NOT_NOW, loggedIn);
          }
        })
        .show();

      return true;
    } else if (id == R.id.action_show_other_language) {
      if (showJavaExamples) {
        setExamplesLanguage(false);
        item.setTitle(R.string.examples_language_java);
      } else {
        setExamplesLanguage(true);
        item.setTitle(R.string.examples_language_kotlin);
      }
    }
    return super.onOptionsItemSelected(item);
  }

  private void setExamplesLanguage(boolean showJava) {
    showJavaExamples = showJava;
    listItems(currentCategory);
  }

  private void checkForFirstTimeOpen() {
    FirstTimeRunChecker firstTimeRunChecker = new FirstTimeRunChecker(this);
    if (firstTimeRunChecker.firstEverOpen()) {
      analytics.openedAppForFirstTime(getResources().getBoolean(R.bool.isTablet), loggedIn);
    }
    firstTimeRunChecker.updateSharedPrefWithCurrentVersion();
  }

  private void buildSettingsDialog() {
    analytics.trackEvent(CLICKED_ON_SETTINGS_IN_NAV_DRAWER, loggedIn);
    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    final View customView = inflater.inflate(R.layout.settings_dialog_layout, null);
    Switch analyticsOptOutSwitch = customView.findViewById(R.id.analytics_opt_out_switch);
    Switch alwaysShowLandingSwitch = customView.findViewById(R.id.login_or_create_account_switch);
    analyticsOptOutSwitch.setChecked(!analytics.isAnalyticsEnabled());

    final SettingsDialogView dialogView = new SettingsDialogView(customView,
      this, analyticsOptOutSwitch, alwaysShowLandingSwitch, analytics, loggedIn);

    dialogView.buildDialog();

    Button logOutOfMapboxAccountButton = customView.findViewById(R.id.log_out_of_account_button);

    if (!loggedIn) {
      logOutOfMapboxAccountButton.setVisibility(View.GONE);
    } else {
      logOutOfMapboxAccountButton.setOnClickListener(view -> {
        dialogView.logOut(loggedIn);
      });
    }
  }

  private void shareApp() {
    try {
      Intent intent = new Intent(Intent.ACTION_SEND);
      intent.setType("text/plain");
      intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_app_subject));
      intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_text));
      startActivity(Intent.createChooser(intent, getString(R.string.share_app_choose_one_instruction)));
    } catch (Exception exception) {
      Timber.d(exception, "shareApp: exception = %s", exception.getMessage());
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(STATE_CURRENT_CATEGORY, currentCategory);
    outState.putString(STATE_TOOLBAR_TITLE, categoryTitleForToolbar);
    outState.putBoolean(STATE_SHOW_JAVA, showJavaExamples);
  }

  private void initializeModels() {

    // TODO: The example below is currently commented out because it crashes due
    //  to incompatibility between the Mapbox Places Plugin and this app's usage
    //  of AndroidX. This is being tracked at:
    //  https://github.com/mapbox/mapbox-plugins-android/issues/908
    /* exampleItemModels.add(new ExampleItemModel(
    R.id.nav_plugins,
    R.string.activity_plugins_place_picker_plugin_title,
    R.string.activity_plugins_place_picker_plugin_description,
    new Intent(MainActivity.this, PlaceSelectionPluginActivity.class),
    null, R.string.activity_plugins_place_picker_plugin_url, false, BuildConfig.MIN_SDK_VERSION)
    );*/

    exampleItemModels.add(new ExampleItemModel(
      R.id.nav_query_map,
      R.string.activity_lab_drag_draw_title,
      R.string.activity_lab_drag_draw_description,
      new Intent(MainActivity.this, FingerDrawQueryActivity.class),
      null,
      R.string.activity_lab_drag_draw_url, false, BuildConfig.MIN_SDK_VERSION));
  }
}