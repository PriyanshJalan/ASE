package com.example.minutedublin;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
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
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
//import com.mapbox.geojson.Point;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "sk.eyJ1IjoidG93ZXJtYWx0YSIsImEiOiJjazZreHNsODYwOHF6M2xucGlubzd3bzl0In0.ZA2ILAXrTZSF8dskXwK6aQ");
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);



    }

    public  void startNavigationBtnClick(View v)
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

        this.mapboxMap.setMinZoomPreference(15);

        mapboxMap.setStyle(getString(R.string.navigation_guidance_day), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

                enableLocationComponent(style);
                addDestinationIconLayer(style);
                mapboxMap.addOnMapClickListener(MainActivity.this);
            }
        });

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
