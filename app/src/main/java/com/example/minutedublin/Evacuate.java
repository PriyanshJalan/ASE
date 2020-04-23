package com.example.minutedublin;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.location.Location;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;

import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_ROUND;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_BEVEL;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * Query the building layer to draw an outline around the building that is in the middle of the map
 */
public class Evacuate extends AppCompatActivity implements
        PermissionsListener, MapboxMap.OnCameraMoveListener, MapboxMap.OnCameraIdleListener {

    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private PermissionsManager permissionsManager;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private LocationEngine locationEngine;
    private LocationChangeListeningActivityLocationCallback callback =
            new LocationChangeListeningActivityLocationCallback(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_query_building_outline);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
                                @Override
                                public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                                    Evacuate.this.mapboxMap = mapboxMap;
                                    mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                                        @Override
                                        public void onStyleLoaded(@NonNull Style style) {
                                            enableLocationComponent(style);
                                            setUpLineLayer(style);
                                            mapboxMap.addOnCameraMoveListener(Evacuate.this);
                                            mapboxMap.addOnCameraIdleListener(Evacuate.this);
                                            updateOutline(style);
                                        }
                                    });
                                }
                            }
        );
    }



    /**
     * Sets up the source and layer for drawing the building outline
     */
    private void setUpLineLayer(@NonNull Style loadedMapStyle) {
        // Create a GeoJSONSource from an empty FeatureCollection
        loadedMapStyle.addSource(new GeoJsonSource("source",
                FeatureCollection.fromFeatures(new Feature[]{})));

        // Use runtime styling to adjust the look of the building outline LineLayer
        loadedMapStyle.addLayer(new LineLayer("lineLayer", "source").withProperties(
                lineColor(Color.RED),
                lineWidth(6f),
                lineCap(LINE_CAP_ROUND),
                lineJoin(LINE_JOIN_BEVEL)
        ));
        updateOutline(loadedMapStyle);
    }

//  @Override
//  public void OnCameraMove() {
//    mapboxMap.getStyle(new Style.OnStyleLoaded() {
//      @Override
//      public void onStyleLoaded(@NonNull Style style) {
//        updateOutline(style);
//      }
//    });
//  }

    /**
     * Query the map for a building Feature in the map's building layer. The query happens in the middle of the
     * map ("the target"). If there's a building Feature in the middle of the map, its coordinates are turned
     * into a list of Point objects so that a LineString can be created.
     *
     * @return the LineString built via the building's coordinates
     */
    private LineString getBuildingFeatureOutline(@NonNull Style style) {
        // Retrieve the middle of the map
        final PointF pixel = mapboxMap.getProjection().toScreenLocation(new LatLng(
                mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude(),
                mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude()
        ));

        List<Point> pointList = new ArrayList<>();

        // Check whether the map style has a building layer
        if (style.getLayer("building") != null) {

            // Retrieve the building Feature that is displayed in the middle of the map
            List<Feature> features = mapboxMap.queryRenderedFeatures(pixel, "building");
            if (features.size() > 0) {
                if (features.get(0).geometry() instanceof Polygon) {
                    Polygon buildingFeature = (Polygon) features.get(0).geometry();
                    // Build a list of Point objects from the building Feature's coordinates
                    if (buildingFeature != null) {
                        for (int i = 0; i < buildingFeature.coordinates().size(); i++) {
                            for (int j = 0;
                                 j < buildingFeature.coordinates().get(i).size(); j++) {
                                pointList.add(Point.fromLngLat(
                                        buildingFeature.coordinates().get(i).get(j).longitude(),
                                        buildingFeature.coordinates().get(i).get(j).latitude()
                                ));
                            }
                        }
                    }
                }
                // Create a LineString from the list of Point objects
            }
        } else {
            Toast.makeText(this, "This map style doesn\\'t have a building layer", Toast.LENGTH_SHORT).show();
        }
        return LineString.fromLngLats(pointList);
    }

    /**
     * Update the FeatureCollection used by the building outline LineLayer. Then refresh the map.
     */
    private void updateOutline(@NonNull Style style) {
        // Update the data source used by the building outline LineLayer and refresh the map
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new Feature[]
                {Feature.fromGeometry(getBuildingFeatureOutline(style))});
        GeoJsonSource source = style.getSourceAs("source");
        if (source != null) {
            source.setGeoJson(featureCollection);
        }
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            // Set the LocationComponent activation options
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .useDefaultLocationEngine(false)
                            .build();

            // Activate with the LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(locationComponentActivationOptions);

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            initLocationEngine();
            updateOutline(loadedMapStyle);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }
    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */
    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "This app needs location permissions in order to show its functionality", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, "You didn\\'t grant location permissions", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onCameraMove() {
        mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                updateOutline(style);
            }
        });

    }

    @Override
    public void onCameraIdle() {
        mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                updateOutline(style);
            }
        });
    }

    private static class LocationChangeListeningActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<Evacuate> activityWeakReference;

        LocationChangeListeningActivityLocationCallback(Evacuate activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        @Override
        public void onSuccess(LocationEngineResult result) {
            Evacuate activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();

                if (location == null) {
                    return;
                }

                // Pass the new location to the Maps SDK's LocationComponent
                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());

                }
            }
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can't be captured
         *
         * @param exception the exception message
         */
        @Override
        public void onFailure(@NonNull Exception exception) {
            Evacuate activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Prevent leaks
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}

