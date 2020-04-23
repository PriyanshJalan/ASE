package com.example.minutedublin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.JsonObject;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.traffic.TrafficPlugin;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
//import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import android.app.Activity;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

import static com.mapbox.geojson.Point.fromLngLat;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener {

    MapView mapView;
    MapboxMap mapboxMap;
    LocationComponent locationComponent;
    PermissionsManager permissionsManager;
    DirectionsRoute currentRoute;
    NavigationMapRoute navigationMapRoute;

    //init variable
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private MenuItem p2penter;
    private FloatingActionButton sendreport;
    private TrafficPlugin trafficPlugin;
    /////notification
    private TextView notification;
    //////search box
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private CarmenFeature home;
    private CarmenFeature work;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";
    public Point passpoint;
    ////////receive data from sendreport
    public static double relat;
    public static double relng;
    public static int flag;


    private com.mapbox.geojson.Point  userPoint;

    //////for show or disshow the marker
    private int trans = 0;
    private int trans1 = 0;
    private int trans2 = 0;
    private ImageView clearview;


    /////symblelayer
    SymbolLayer busSymbolLayer;
    SymbolLayer trainSymbolLayer;
    SymbolLayer reportSymbolLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);
        initView();
        ///////
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerLayout = findViewById(R.id.drawer_layout);    ///navigation
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView = findViewById(R.id.navigation_view);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                UserMenuSelected(menuItem);
                return false;
            }
        });


        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);



    }

    private void initView() {
        ////////notification
        notification = (TextView) this.findViewById(R.id.notification);
        notification.setSelected(true);
        sendreport = (FloatingActionButton) findViewById(R.id.report);
        sendreport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, 1);
                }
                 */
                userPoint = com.mapbox.geojson.Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                        locationComponent.getLastKnownLocation().getLatitude());
                Intent intent =  new Intent(MainActivity.this,SendReport.class);
                SendReport.alertlat = userPoint.latitude();
                SendReport.alertlng = userPoint.longitude();

                Intent a= new Intent(MainActivity.this,SendReport.class);
                startActivity(a);
            }
        });

        ////clear button
        clearview = (ImageView) findViewById(R.id.clear);
        clearview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(trafficPlugin.isVisible()) {
                    trafficPlugin.setVisibility(false);
                }
                if(trans==1) {
                    busSymbolLayer.setProperties(visibility(NONE));
                }
                if(trans1==1) {
                    trainSymbolLayer.setProperties(visibility(NONE));
                }
                if(trans2==1) {
                    reportSymbolLayer.setProperties(visibility(NONE));
                }
            }
        });

        //notification.setText("here comes a disaster event, the location is: (-6.237489, 53.322813) ");
    }

    //////////////three dots
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.threedots_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        if (item.getItemId()==R.id.show_transportation){
            if (mapboxMap != null) {
                Style style = mapboxMap.getStyle();
                if (style != null) {
                    /////////////bus stop
                    try {
                        if(trans == 0)
                        {
                            URI geoJsonUrl = new URI("http://ec2-46-51-146-5.eu-west-1.compute.amazonaws.com:8080/bus_stops/bus_stops_geo_json");
                            //Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_marker_icon_default);
                            Drawable drawable = ResourcesCompat.getDrawable(getResources(),R.drawable.ic_bus_stop, null);
                            Bitmap mBitmap = BitmapUtils.getBitmapFromDrawable(drawable);
                            style.addImage("bus-geojson", mBitmap);
//                    style.addImage("bus-geojson", BitmapFactory.decodeResource(
//                            MainActivity.this.getResources(), R.drawable.ic_bus_stop2));
                            GeoJsonSource geoJsonSource = new GeoJsonSource("bus-geojson-source", geoJsonUrl);
                            style.addSource(geoJsonSource);
                            busSymbolLayer = new SymbolLayer("bus-symbol-layer-id","bus-geojson-source");
                            busSymbolLayer.setProperties(PropertyFactory.iconImage("bus-geojson"));
                            busSymbolLayer.withProperties(iconImage("bus-geojson"),iconAllowOverlap(true),
                                    iconIgnorePlacement(true));
                            style.addLayer(busSymbolLayer);
                            trans=1;
                        }
                        busSymbolLayer.setProperties(visibility(VISIBLE));
                    } catch (URISyntaxException exception) {
                        Log.d("Error: ", exception.getMessage());
                    }

                    /////////////////train stop

                    try {
                        if(trans1 == 0) {
                            URI geoJsonUrl = new URI("http://ec2-46-51-146-5.eu-west-1.compute.amazonaws.com:8080/stop/train_geo_json");
                            //Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_marker_icon_default);
                            style.addImage("train-geojson",
                                    BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.ic_train)),
                                    true);
                            GeoJsonSource geoJsonSource = new GeoJsonSource("train-geojson-source", geoJsonUrl);
                            style.addSource(geoJsonSource);
                            trainSymbolLayer = new SymbolLayer("train-symbol-layer-id", "train-geojson-source");
                            trainSymbolLayer.setProperties(PropertyFactory.iconImage("train-geojson"));
                            trainSymbolLayer.withProperties(iconImage("train-geojson"), iconAllowOverlap(true),
                                    iconIgnorePlacement(true));
                            style.addLayer(trainSymbolLayer);
                            trans1=1;
                        }
                        trainSymbolLayer.setProperties(visibility(VISIBLE));
                    } catch (URISyntaxException exception) {
                        Log.d("Error: ", exception.getMessage());
                    }


                }
            }
            //Loadgeojsonfile();
            //startActivity(new Intent(MainActivity.this, TransActivity.class));
        }
        if (item.getItemId()==R.id.show_traffic){
            trafficPlugin.setVisibility(true);
        }
        if (item.getItemId()==R.id.show_reports){
            if (mapboxMap != null) {
                Style style = mapboxMap.getStyle();
                if (style != null) {
                    try {
                        if(trans2 == 0)
                        {
                            URI geoJsonUrl = new URI("http://ec2-46-51-146-5.eu-west-1.compute.amazonaws.com:8080/report/fetch_reports");
                            //Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_marker_icon_default);
                            Drawable drawable = ResourcesCompat.getDrawable(getResources(),R.drawable.ic_alert, null);
                            Bitmap mBitmap = BitmapUtils.getBitmapFromDrawable(drawable);
                            style.addImage("report-geojson", mBitmap);

                            GeoJsonSource geoJsonSource = new GeoJsonSource("report-geojson-source", geoJsonUrl);
                            //geoJsonSource.
                            style.addSource(geoJsonSource);
                            reportSymbolLayer = new SymbolLayer("report-symbol-layer-id","report-geojson-source");
                            reportSymbolLayer.setProperties(PropertyFactory.iconImage("report-geojson"));
                            reportSymbolLayer.withProperties(iconImage("report-geojson"),iconAllowOverlap(true),
                                    iconIgnorePlacement(true));
                            style.addLayer(reportSymbolLayer);
                            trans2=1;
                        }
                        reportSymbolLayer.setProperties(visibility(VISIBLE));
                    } catch (URISyntaxException exception) {
                        Log.d("Error: ", exception.getMessage());
                    }
                }
            }
        }

        if (item.getItemId()==R.id.show_ways){
            startActivity(new Intent(MainActivity.this, Evacuate.class));
        }
        if (item.getItemId()==R.id.show_rescue){
            startActivity(new Intent(MainActivity.this, MovingIconWithTrailingLineActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    public void UserMenuSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){
            case R.id.p2penter:
                startActivity(new Intent(MainActivity.this, Wifip2pAnimation.class));
                break;
            case R.id.nav_about:
                startActivity(new Intent(MainActivity.this, AboutUs.class));
                break;
            case R.id.nav_setting:
                break;
        }
    }


    public void startNavigationBtnClick(View v)
    {
        boolean simulateRoute = true;
        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                .directionsRoute(currentRoute)
                .shouldSimulateRoute(simulateRoute)
                .build();

        NavigationLauncher.startNavigation(MainActivity.this,options);
    }


    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if(granted) {
            enableLocationComponent(mapboxMap.getStyle());
        }
        else {
            Toast.makeText(getApplicationContext(),"Permission not granted", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        com.mapbox.geojson.Point  destinationPoint = com.mapbox.geojson.Point.fromLngLat(point.getLongitude(),point.getLatitude());
        com.mapbox.geojson.Point  originPoint = com.mapbox.geojson.Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
               locationComponent.getLastKnownLocation().getLatitude());
        //passpoint = originPoint;
        Intent intent =  new Intent(MainActivity.this,DirectionsProfileActivity.class);
        DirectionsProfileActivity.orilat = originPoint.latitude();
        DirectionsProfileActivity.orilng = originPoint.longitude();
        DirectionsProfileActivity.deslat = destinationPoint.latitude();
        DirectionsProfileActivity.deslng = destinationPoint.longitude();
        //SendReport.alertlat = originPoint.latitude();
        //SendReport.alertlng = originPoint.longitude();
        //intent.putExtra("orilat", originPoint.latitude());
        //intent.putExtra("orilng", originPoint.longitude());
        //intent.putExtra("deslat", destinationPoint.latitude());
        //intent.putExtra("deslng", destinationPoint.longitude());
        startActivity(intent); ////////////////////////////////////////////can commend

        //com.mapbox.geojson.Point  originPoint = com.mapbox.geojson.Point.fromLngLat(-6.254572, 53.343792);
        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
        if(source != null) {
            source.setGeoJson(Feature.fromGeometry(destinationPoint));
        }
        getRoute(originPoint,destinationPoint);
        return true;
    }

    private void getRoute(com.mapbox.geojson.Point originPoint, com.mapbox.geojson.Point destinationPoint)
    {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(originPoint)
                .destination(destinationPoint)
                .profile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if(response.body()!=null && response.body().routes().size()>0) {
                            currentRoute = response.body().routes().get(0);
                            if(navigationMapRoute!=null) {
                                navigationMapRoute.removeRoute();
                            }
                            else {
                                navigationMapRoute = new NavigationMapRoute(null, mapView,mapboxMap,R.style.NavigationMapRoute);
                            }
                            navigationMapRoute.addRoute(currentRoute);
                        }
                    }
                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Timber.e("Error: %s", t.getMessage());
                        Toast.makeText(MainActivity.this, "Error: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        this.mapboxMap.setMinZoomPreference(6);
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                trafficPlugin = new TrafficPlugin(mapView, mapboxMap, style);
                //////search box
                initSearchFab();
                style.addImage(symbolIconId, BitmapFactory.decodeResource(
                        MainActivity.this.getResources(), R.drawable.mapbox_marker_icon_default));
                // Create an empty GeoJSON source using the empty feature collection
                setUpSource(style);
                // Set up a new symbol layer for displaying the searched location's feature coordinates
                setupLayer(style);
                enableLocationComponent(style);
                locationComponent.setCameraMode(CameraMode.TRACKING);
                locationComponent.setRenderMode(RenderMode.COMPASS);
                addDestinationIconLayer(style);
                mapboxMap.addOnMapClickListener(MainActivity.this);


                ///geojson file read
                /////send user location
                //////user’s original location

                //relat = userPoint.latitude();
                //relng = userPoint.longitude();
                /*
                MapboxGeocoding reverseGeocode = MapboxGeocoding.builder()
                        .accessToken(getString(R.string.access_token))
                        .query(Point.fromLngLat(userPoint.longitude(), userPoint.latitude()))
                        .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
                        .build();
                intent.putExtra("place", reverseGeocode.toString());
                 */
                Intent re = getIntent();
                if(flag==1) {
                    StringBuilder textchange = new StringBuilder();
                    String typeis = re.getStringExtra("type");
                    String commentis = re.getStringExtra("comment");
                    //Point point = Point.fromLngLat(relng, alertlat);
                    textchange.append("Here reports an event, please pay attention! the location is (").append(relng).
                            append(",").append("  ").append(relat).append(")").append(", the event type is ").append(typeis)
                    .append(", the comment of this event is: ").append(commentis);
                    ////original value is 0.0
                    notification.setText(textchange);
                }
            }
        });

    }

    private void Loadgeojsonfile(Style style) {
//        GeoJsonSource source = new GeoJsonSource("geojson", geoJsonString);
//        mapboxMap.addSource(source);
//        mapboxMap.addLayer(new LineLayer("geojson", "geojson"));

    }

    private void initSearchFab() {
        findViewById(R.id.fab_location_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() :
                                getString(R.string.access_token))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
//                                .addInjectedFeature(home)
//                                .addInjectedFeature(work)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(MainActivity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });
    }


    private void setUpSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(geojsonSourceLayerId));

    }

    private void setupLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addLayer(new SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId).withProperties(
                iconImage(symbolIconId),
                iconOffset(new Float[] {0f, -8f})
        ));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {
// Retrieve selected location's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);
// Create a new FeatureCollection and add a new Feature to it using selectedCarmenFeature above.
// Then retrieve and update the source designated for showing a selected location's symbol layer icon
            if (mapboxMap != null) {
                Style style = mapboxMap.getStyle();
                if (style != null) {
                    com.mapbox.geojson.Point  destinationPoint = com.mapbox.geojson.Point.fromLngLat(((Point) selectedCarmenFeature.geometry()).longitude(),
                            ((Point) selectedCarmenFeature.geometry()).latitude());
                    com.mapbox.geojson.Point  originPoint = com.mapbox.geojson.Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                            locationComponent.getLastKnownLocation().getLatitude());
                    //om.mapbox.geojson.Point  originPoint = com.mapbox.geojson.Point.fromLngLat(-6.254572, 53.343792);
                    Intent intent =  new Intent(MainActivity.this,DirectionsProfileActivity.class);
                    DirectionsProfileActivity.orilat = originPoint.latitude();
                    DirectionsProfileActivity.orilng = originPoint.longitude();
                    DirectionsProfileActivity.deslat = destinationPoint.latitude();
                    DirectionsProfileActivity.deslng = destinationPoint.longitude();
                    //SendReport.alertlat = originPoint.latitude();
                    //SendReport.alertlng = originPoint.longitude();
                    GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[] {Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }
                    getRoute(originPoint,destinationPoint);
// Move map camera to the selected location
                    //final LatLng markerLocation = origin
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                            ((Point) selectedCarmenFeature.geometry()).longitude()))
                                    .zoom(14)
                                    .build()), 1);
//                    com.mapbox.geojson.Point  destinationPoint = com.mapbox.geojson.Point.fromLngLat(((Point) selectedCarmenFeature.geometry()).longitude(), ((Point) selectedCarmenFeature.geometry()).latitude());
//                    com.mapbox.geojson.Point  originPoint = com.mapbox.geojson.Point .fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
//                            locationComponent.getLastKnownLocation().getLatitude());
//                    getRoute(originPoint,destinationPoint);
                }
            }
        }
    }

    private void addDestinationIconLayer(Style style) {
        style.addImage("destination-icon-id",
                BitmapFactory.decodeResource(this.getResources(),R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        style.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id","destination-source-id");
        destinationSymbolLayer.withProperties(iconImage("destination-icon-id"),iconAllowOverlap(true),
                iconIgnorePlacement(true));
        style.addLayer(destinationSymbolLayer);
    }

    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        if(PermissionsManager.areLocationPermissionsGranted(this)) {
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this,loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
        }
        else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


}
