package com.example.minutedublin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.JsonObject;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
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

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

import static com.mapbox.geojson.Point.fromLngLat;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        MapboxMap.OnMapClickListener, PermissionsListener {

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

    //////search box
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private CarmenFeature home;
    private CarmenFeature work;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);

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


    //////////////three dots
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.threedots_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

///
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        if (item.getItemId()==R.id.show_transportation){
            Loadgeojsonfile();
            //startActivity(new Intent(MainActivity.this, TransActivity.class));
        }

//        if (item.getItemId()==R.id.show_traffic){
//        }
//
//        if (item.getItemId()==R.id.show_reports){
//        }

        return super.onOptionsItemSelected(item);
    }

    public void UserMenuSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){
            case R.id.p2penter:
                startActivity(new Intent(MainActivity.this, Wifip2pAnimation.class));
                break;
            case R.id.nav_about:
                break;
            case R.id.nav_dummy1:
                break;
            case R.id.nav_dummy2:
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
        if(granted)
        {
            enableLocationComponent(mapboxMap.getStyle());
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Permission not granted", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        com.mapbox.geojson.Point  destinationPoint = com.mapbox.geojson.Point.fromLngLat(point.getLongitude(),point.getLatitude());
        com.mapbox.geojson.Point  originPoint = com.mapbox.geojson.Point .fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                locationComponent.getLastKnownLocation().getLatitude());

        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");

        if(source != null)
        {
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
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if(response.body()!=null && response.body().routes().size()>0)
                        {
                            currentRoute = response.body().routes().get(0);

                            if(navigationMapRoute!=null)
                            {
                                navigationMapRoute.removeRoute();
                            }
                            else
                            {
                                navigationMapRoute = new NavigationMapRoute(null, mapView,mapboxMap,R.style.NavigationMapRoute);
                            }

                            navigationMapRoute.addRoute(currentRoute);

                        }
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {

                    }
                });
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        this.mapboxMap.setMinZoomPreference(13);

        mapboxMap.setStyle(getString(R.string.navigation_guidance_day), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                //////search box
                initSearchFab();
                style.addImage(symbolIconId, BitmapFactory.decodeResource(
                        MainActivity.this.getResources(), R.drawable.mapbox_marker_icon_default));
                // Create an empty GeoJSON source using the empty feature collection
                setUpSource(style);
                // Set up a new symbol layer for displaying the searched location's feature coordinates
                setupLayer(style);

                enableLocationComponent(style);
                addDestinationIconLayer(style);
                mapboxMap.addOnMapClickListener(MainActivity.this);

                ///geojson file read
                try {
                    URI geoJsonUrl = new URI("http://ec2-46-51-146-5.eu-west-1.compute.amazonaws.com/bus_stops/bus_stops_geo_json");
                    //Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_marker_icon_default);
                    style.addImage("bus-geojson",
                            BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.ic_bus_stop)),
                            true);
//                    style.addImage("bus-geojson", BitmapFactory.decodeResource(
//                            MainActivity.this.getResources(), R.drawable.ic_bus_stop));
                    GeoJsonSource geoJsonSource = new GeoJsonSource("bus-geojson-source", geoJsonUrl);
                    style.addSource(geoJsonSource);
                    SymbolLayer busSymbolLayer = new SymbolLayer("bus-symbol-layer-id","bus-geojson-source");
                    busSymbolLayer.setProperties(PropertyFactory.iconImage("bus-geojson"));
                    busSymbolLayer.withProperties(iconImage("bus-geojson"),iconAllowOverlap(true),
                            iconIgnorePlacement(true));
                    style.addLayer(busSymbolLayer);
                } catch (URISyntaxException exception) {
                    Log.d("Error: ", exception.getMessage());
                }



            }
        });

    }

    private void Loadgeojsonfile() {
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

//    private void addUserLocations() {
//        home = CarmenFeature.builder().text("Mapbox SF Office")
//                .geometry(Point.fromLngLat(-122.3964485, 37.7912561))
//                .placeName("50 Beale St, San Francisco, CA")
//                .id("mapbox-sf")
//                .properties(new JsonObject())
//                .build();
//
//        work = CarmenFeature.builder().text("Mapbox DC Office")
//                .placeName("740 15th Street NW, Washington DC")
//                .geometry(Point.fromLngLat(-77.0338348, 38.899750))
//                .id("mapbox-dc")
//                .properties(new JsonObject())
//                .build();
//    }

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
                    GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[] {Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }
                    getRoute(originPoint,destinationPoint);

// Move map camera to the selected location
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                            ((Point) selectedCarmenFeature.geometry()).longitude()))
                                    .zoom(14)
                                    .build()), 4000);


//                    com.mapbox.geojson.Point  destinationPoint = com.mapbox.geojson.Point.fromLngLat(((Point) selectedCarmenFeature.geometry()).longitude(), ((Point) selectedCarmenFeature.geometry()).latitude());
//                    com.mapbox.geojson.Point  originPoint = com.mapbox.geojson.Point .fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
//                            locationComponent.getLastKnownLocation().getLatitude());
//                    getRoute(originPoint,destinationPoint);
                }
            }
        }
    }

    private void addDestinationIconLayer(Style style)
    {
        style.addImage("destination-icon-id",
                BitmapFactory.decodeResource(this.getResources(),R.drawable.mapbox_marker_icon_default));

        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        style.addSource(geoJsonSource);

        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id","destination-source-id");
        destinationSymbolLayer.withProperties(iconImage("destination-icon-id"),iconAllowOverlap(true),
                iconIgnorePlacement(true));

        style.addLayer(destinationSymbolLayer);

    }

    private void enableLocationComponent(@NonNull Style loadedMapStyle)
    {
        if(PermissionsManager.areLocationPermissionsGranted(this))
        {
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this,loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);

            locationComponent.setCameraMode(CameraMode.TRACKING);

        }
        else
        {
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
