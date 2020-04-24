package com.example.minutedublin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.isochrone.IsochroneCriteria;
import com.mapbox.api.isochrone.MapboxIsochrone;
import com.mapbox.api.tilequery.MapboxTilequery;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMapClickListener;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.api.directions.v5.DirectionsCriteria.PROFILE_WALKING;
import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.expressions.Expression.concat;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.geometryType;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.toColor;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_ROUND;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_BEVEL;
import static com.mapbox.mapboxsdk.style.layers.Property.SYMBOL_PLACEMENT_LINE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.symbolPlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textFont;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textHaloColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textHaloWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textLetterSpacing;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textMaxAngle;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textPadding;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

public class Evacuate extends AppCompatActivity implements
        PermissionsListener, MapboxMap.OnMapClickListener{

    //location component
    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private PermissionsManager permissionsManager;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private LocationEngine locationEngine;
    private LocationChangeListeningActivityLocationCallback callback =
            new LocationChangeListeningActivityLocationCallback(this);

    // query exit points & contours
    private static final String ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID = "ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID";
    private static final String ISOCHRONE_LINE_LAYER = "ISOCHRONE_LINE_LAYER";
    private static final String TIME_LABEL_LAYER_ID = "TIME_LABEL_LAYER_ID";
    private static final String MAP_CLICK_SOURCE_ID = "MAP_CLICK_SOURCE_ID";
    private static final String MAP_CLICK_MARKER_LAYER_ID = "MAP_CLICK_MARKER_LAYER_ID";
    private static final String[] contourColors = new String[]{"FFFF00", "80f442"};
    private static final int[] contourMinutes = new int[]{2, 5};
    private Location lastSelectedLatLng;
    private boolean usePolygon = false;

    //navigation
    private DirectionsRoute currentRoute;
    private NavigationMapRoute navigationMapRoute;
    private Point origin;
    private Point destination;

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
                                    mapboxMap.setStyle(new Style.Builder().fromUri(Style.LIGHT)
                                                    //Add a SymbolLayer to the map so that the map click point has a visual marker. This is where the
                                                    // Isochrone API information radiates from.
                                                    .withSource(new GeoJsonSource(MAP_CLICK_SOURCE_ID))
                                                    .withSource(new GeoJsonSource(ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID))
                                                    .withLayer(new SymbolLayer(MAP_CLICK_MARKER_LAYER_ID, MAP_CLICK_SOURCE_ID).withProperties())
                                            , new Style.OnStyleLoaded() {
                                        @Override
                                        public void onStyleLoaded(@NonNull Style style) {
                                            enableLocationComponent(style);
                                            setUpLineLayer(style);
                                            updateOutline(style);
                                            lastSelectedLatLng= mapboxMap.getLocationComponent().getLastKnownLocation();
                                            assert lastSelectedLatLng != null;
                                            initLineLayer(style);
                                            makeIsochroneApiCall(style, lastSelectedLatLng);
                                            addClickLayer(style);
                                            addResultLayer(style);

                                            GeoJsonSource clickLocationSource = style.getSourceAs("CLICK_CENTER_GEOJSON_SOURCE_ID");
                                            if (clickLocationSource != null) {
                                                makeTilequeryApiCall(style,lastSelectedLatLng);
                                            }
                                            mapboxMap.addOnMapClickListener(Evacuate.this);


                                        }
                                    });
                                }
                            }
        );
    }


    /**
     * Use the Java SDK's MapboxTilequery class to build a API request and use the API response
     *
     * @param point the center point that the the tilequery will originate from.
     */
    private void makeTilequeryApiCall(@NonNull Style style, @NonNull Location point) {
        MapboxTilequery tilequery = MapboxTilequery.builder()
                .accessToken(getString(R.string.access_token))
                .tilesetIds("mapbox.mapbox-streets-v7")
                .query(Point.fromLngLat(point.getLongitude(), point.getLatitude()))
                .radius(200)
                .limit(4)
                .geometry("linestring")
                .dedupe(true)
                .layers("road")
                .build();

        tilequery.enqueueCall(new Callback<FeatureCollection>() {
            @Override
            public void onResponse(Call<FeatureCollection> call, Response<FeatureCollection> response) {
                if (response.body() != null) {
                    FeatureCollection responseFeatureCollection = response.body();
                            GeoJsonSource resultSource = style.getSourceAs("RESULT_GEOJSON_SOURCE_ID");
                            if (resultSource != null && responseFeatureCollection.features() != null) {
                                List<Feature> featureList = responseFeatureCollection.features();

                                if (featureList.isEmpty()) {
                                    Toast.makeText(Evacuate.this,
                                            "No Escape Routes detected", Toast.LENGTH_SHORT).show();
                                } else {
                                    resultSource.setGeoJson(FeatureCollection.fromFeatures(featureList));
                                    origin = Point.fromLngLat(
                                            mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude(),
                                            mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude());
                                    destination = Point.fromLngLat(
                                            (((Point)featureList.get(0).geometry()).coordinates()).get(0),
                                            (((Point)featureList.get(0).geometry()).coordinates()).get(1)
                                    );

//                                    getRoute(style,Point.fromLngLat(-6.2409210205078125, 53.342763063131976),
//                                            Point.fromLngLat(-6.307868957519531, 53.33948337211111));
                                }

                            }
                            else {
                                Toast.makeText(Evacuate.this,
                                        "response layer null", Toast.LENGTH_SHORT).show();
                            }
                }
            }

            @Override
            public void onFailure(Call<FeatureCollection> call, Throwable throwable) {
                Timber.d("Request failed: %s", throwable.getMessage());
                Toast.makeText(Evacuate.this, "API Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getRoute(Style style, final Point origin, final Point destination) {
        MapboxDirections client = MapboxDirections.builder()
                .origin(origin)
                .destination(destination)
                .profile(DirectionsCriteria.PROFILE_WALKING)
                .accessToken(getString(R.string.access_token))
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                System.out.println(call.request().url().toString());

                // You can get the generic HTTP info about the response
                Timber.d("Response code: %s", response.code());
                if (response.body() == null) {
                    Timber.e("No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().routes().size() < 1) {
                    Timber.e("No routes found");
                    return;
                }

                // Get the directions route
                currentRoute = response.body().routes().get(0);
                style.addSource(new GeoJsonSource("ROUTE_SOURCE_ID", LineString.fromPolyline(currentRoute.geometry(),
                        PRECISION_6)));
                style.addLayer(new LineLayer("linelayer", "line-source")
                        .withProperties(PropertyFactory.lineCap(Property.LINE_CAP_SQUARE),
                                PropertyFactory.lineJoin(Property.LINE_JOIN_MITER),
                                PropertyFactory.lineOpacity(.7f),
                                PropertyFactory.lineWidth(7f),
                                PropertyFactory.lineColor(Color.parseColor("#3bb2d0"))));

            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Timber.e("Error: %s", throwable.getMessage());
                Toast.makeText(Evacuate.this, "Error: " + throwable.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Make a request to the Mapbox Isochrone API
     *
     * @param mapClickPoint The center point of the isochrone. It is part of the API request.
     */
    private void makeIsochroneApiCall(@NonNull Style style, @NonNull Location mapClickPoint) {

        MapboxIsochrone mapboxIsochroneRequest = MapboxIsochrone.builder()
                .accessToken(getString(R.string.access_token))
                .profile(IsochroneCriteria.PROFILE_WALKING)
                .addContoursMinutes(contourMinutes[0], contourMinutes[1])
                .addContoursColors(contourColors[0], contourColors[1])
                .generalize(2f)
                .denoise(.4f)
                .coordinates(Point.fromLngLat(mapClickPoint.getLongitude(), mapClickPoint.getLatitude()))
                .build();

        mapboxIsochroneRequest.enqueueCall(new Callback<FeatureCollection>() {
            @Override
            public void onResponse(Call<FeatureCollection> call, Response<FeatureCollection> response) {
                // Redraw Isochrone information based on response body
                if (response.body() != null && response.body().features() != null) {
                    GeoJsonSource source = style.getSourceAs(ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID);
                    if (source != null && response.body().features().size() > 0) {
                        source.setGeoJson(response.body());
                    }

                    if (!usePolygon) {
                        mapboxMap.getStyle(new Style.OnStyleLoaded() {
                            @Override
                            public void onStyleLoaded(@NonNull Style style) {

                                SymbolLayer timeLabelSymbolLayer;

                                // Check to see whether the LineLayer for time labels has already been created
                                if (style.getLayerAs(TIME_LABEL_LAYER_ID) == null) {
                                    timeLabelSymbolLayer = new SymbolLayer(TIME_LABEL_LAYER_ID,
                                            ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID);

                                    styleLineLayer(timeLabelSymbolLayer);

                                    // Add the time label LineLayer to the map
                                    style.addLayer(timeLabelSymbolLayer);

                                } else {
                                    styleLineLayer(style.getLayerAs(TIME_LABEL_LAYER_ID));
                                }
                            }
                        });
                    }

                }
            }

            @Override
            public void onFailure(Call<FeatureCollection> call, Throwable throwable) {
                Timber.d("Request failed: %s", throwable.getMessage());
            }
        });
    }


    /**
     * Add a map layer which will show a marker icon where the map was clicked
     */
    private void addClickLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource("CLICK_CENTER_GEOJSON_SOURCE_ID",
                FeatureCollection.fromFeatures(new Feature[] {})));
        loadedMapStyle.addLayer(new SymbolLayer("click-layer", "CLICK_CENTER_GEOJSON_SOURCE_ID").withProperties());
    }

    /**
     * Add a map layer which will show marker icons for all of the Tilequery API results
     */
    private void addResultLayer(@NonNull Style loadedMapStyle) {
        // Add the marker image to map
        loadedMapStyle.addImage("RESULT_ICON_ID", Objects.requireNonNull(BitmapUtils.getBitmapFromDrawable(
                getResources().getDrawable(R.drawable.map_marker_dark))));

        // Retrieve GeoJSON information from the Mapbox Tilequery API
        loadedMapStyle.addSource(new GeoJsonSource("RESULT_GEOJSON_SOURCE_ID"));

        loadedMapStyle.addLayer(new SymbolLayer("LAYER_ID", "RESULT_GEOJSON_SOURCE_ID").withProperties(
                iconImage("RESULT_ICON_ID"),
                iconOffset(new Float[] {0f, -12f}),
                iconIgnorePlacement(true),
                iconAllowOverlap(false)
        ));

//        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
//                .directionsRoute(currentRoute)
//                .shouldSimulateRoute(true)
//                .build();
//// Call this method with Context from within an Activity
//        NavigationLauncher.startNavigation(Evacuate.this, options);
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
    public boolean onMapClick(@NonNull LatLng point) {
        Intent intent =  new Intent(Evacuate.this,DirectionsProfileActivity.class);
        DirectionsProfileActivity.orilat = lastSelectedLatLng.getLatitude();
        DirectionsProfileActivity.orilng = lastSelectedLatLng.getLongitude();
        DirectionsProfileActivity.deslat = point.getLatitude();
        DirectionsProfileActivity.deslng = point.getLongitude();
        startActivity(intent);
        return false;
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
                    activity.mapboxMap.getStyle(new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {
                            activity.updateOutline(style);
                            activity.lastSelectedLatLng = result.getLastLocation();
                            activity.makeIsochroneApiCall(style, activity.lastSelectedLatLng);
                            activity.makeTilequeryApiCall(style, activity.lastSelectedLatLng);
                        }
                });
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



    private SymbolLayer styleLineLayer(@NonNull SymbolLayer timeLabelLayerToStyle) {
        // Use the Maps SDK's data-driven styling properties to style the
        // the time label LineLayer
        timeLabelLayerToStyle.setProperties(
                visibility(Property.VISIBLE),
                textField(concat(get("contour"), literal(" MIN"))),
                textFont(new String[]{"DIN Offc Pro Bold", "Roboto Black"}),
                symbolPlacement(SYMBOL_PLACEMENT_LINE),
                textAllowOverlap(true),
                textPadding(1f),
                textMaxAngle(90f),
                textSize(interpolate(linear(), literal(1.2f),
                        stop(2, 14),
                        stop(8, 18),
                        stop(22, 30)
                )),
                textLetterSpacing(0.1f),
                textHaloColor(Color.parseColor("#343332")),
                textColor(toColor(get("color"))),
                textHaloWidth(4f)
        );
        return timeLabelLayerToStyle;
    }


    /**
     * Add a LineLayer so that that lines returned by the Isochrone API response can be displayed
     */
    private void initLineLayer(@NonNull Style style) {
        // Create and style a LineLayer based on information in the Isochrone API response
        LineLayer isochroneLineLayer = new LineLayer(ISOCHRONE_LINE_LAYER, ISOCHRONE_RESPONSE_GEOJSON_SOURCE_ID);
        isochroneLineLayer.setProperties(
                lineColor(get("color")),
                lineWidth(5f),
                lineOpacity(.8f)); // You could also pass in get("opacity")) instead of a hardcoded value
        isochroneLineLayer.setFilter(eq(geometryType(), literal("LineString")));
        style.addLayerBelow(isochroneLineLayer, MAP_CLICK_MARKER_LAYER_ID);
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
        if (mapboxMap != null) {
            mapboxMap.removeOnMapClickListener(this);
        }
//        // Prevent leaks
//        if (locationEngine != null) {
//            locationEngine.removeLocationUpdates(callback);
//        }
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}

