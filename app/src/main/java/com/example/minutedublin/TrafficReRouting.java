package com.example.minutedublin;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.turf.models.LineIntersectsResult;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

public class TrafficReRouting extends AppCompatActivity implements MapboxMap.OnMapClickListener {


    private static final String DOT_SOURCE_ID = "dot-source-id";
    private static final String LINE_SOURCE_ID = "line-source-id";

    private MapView mapView;
    private MapboxMap mapboxMap;

    private GeoJsonSource pointSource;
    private GeoJsonSource lineSource;
    private List<Point> routeCoordinateList;
    private List<Point> markerLinePointList = new ArrayList<>();
    private int routeIndex;

    /*
    private Point direc_originPoint = Point.fromLngLat(-6.2450408935546875, 53.35372769822772);
    private Point direc_destinationPoint = Point.fromLngLat(-6.33109717302, 53.4116303481);
    private Point emer_originPoint = Point.fromLngLat(-6.33463571889, 53.4305245967);
    private Point emer_destinationPoint = Point.fromLngLat(-6.29261846108, 53.3920439993);
     */
    private Point direc_originPoint = Point.fromLngLat(-6.2450408935546875, 53.35372769822772);
    private Point direc_destinationPoint = Point.fromLngLat(-6.33109717302, 53.4116303481);
    private Point emer_originPoint = Point.fromLngLat(-6.2450408935546875, 53.35372769822772);
    private Point emer_destinationPoint = Point.fromLngLat(-6.33109717302, 53.4116303481);



    private Animator currentAnimator;
    private LatLng markerIconCurrentLocation;
    private GeoJsonSource dotGeoJsonSource;
    private ValueAnimator markerIconAnimator;
    private Handler handler;
    private Runnable runnable;
    int count=0;
    int clkCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.moving_icon_with_trailing_line);

        // Initialize the mapboxMap view
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                TrafficReRouting.this.mapboxMap = mapboxMap;
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                        ///////show healthcenter
                        try {
                            URI geoJsonUrl = new URI("http://ec2-46-51-146-5.eu-west-1.compute.amazonaws.com:8080/report/fetch_reports");
                            //Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_marker_icon_default);
                            Drawable drawable = ResourcesCompat.getDrawable(getResources(),R.drawable.ic_alert, null);
                            Bitmap mBitmap = BitmapUtils.getBitmapFromDrawable(drawable);
                            style.addImage("reportone-geojson", mBitmap);

                            GeoJsonSource geoJsonSource = new GeoJsonSource("reportone-geojson-source", geoJsonUrl);
                            //geoJsonSource.
                            style.addSource(geoJsonSource);
                            SymbolLayer reportoneSymbolLayer = new SymbolLayer("reportone-symbol-layer-id","reportone-geojson-source");
                            reportoneSymbolLayer.setProperties(PropertyFactory.iconImage("reportone-geojson"));
                            reportoneSymbolLayer.withProperties(iconImage("reportone-geojson"),iconAllowOverlap(true),
                                    iconIgnorePlacement(true));
                            style.addLayer(reportoneSymbolLayer);
                        } catch (URISyntaxException exception) {
                            Log.d("Error: ", exception.getMessage());
                        }


                        MapboxDirections client = MapboxDirections.builder()
                                .origin(direc_originPoint)
                                .destination(direc_destinationPoint)
                                .overview(DirectionsCriteria.OVERVIEW_FULL)
                                .profile(DirectionsCriteria.PROFILE_CYCLING)
                                //.profile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
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
                                DirectionsRoute currentRoute = response.body().routes().get(0);
                                        // Retrieve and update the source designated for showing the directions route
                                        GeoJsonSource source = new GeoJsonSource("vehicle");
                                        LineLayer routeLayer = new LineLayer("vehicle-layer", "vehicle");

                                        // Add the LineLayer to the map. This layer will display the directions route.
                                        routeLayer.setProperties(
                                                lineCap(Property.LINE_CAP_ROUND),
                                                lineJoin(Property.LINE_JOIN_ROUND),
                                                lineWidth(5f),
                                                lineColor(Color.parseColor("#009688"))
                                        );
                                        style.addSource(source);
                                        style.addLayer(routeLayer);
                                        // Create a LineString with the directions route's geometry and
                                        // reset the GeoJSON source for the route LineLayer source
                                        if (source != null) {
                                            source.setGeoJson(LineString.fromPolyline(currentRoute.geometry(), PRECISION_6));
                                        }
                                mapboxMap.addOnMapClickListener(TrafficReRouting.this);


                            }

                            @Override
                            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                                Timber.e("Error: %s", throwable.getMessage());
                                Toast.makeText(TrafficReRouting.this, "Error: " + throwable.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
            }
        });
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        clkCount++;

        Toast.makeText(TrafficReRouting.this,"Intersection", Toast.LENGTH_SHORT).show();
        if(clkCount == 1) {
            getRoute(emer_originPoint,emer_destinationPoint);
        }
        return false;
    }

    private static LineIntersectsResult lineIntersects(Point start1, Point end1, Point start2, Point end2) {
        return lineIntersects(start1.longitude(),
                start1.latitude(),
                end1.longitude(),
                end1.latitude(),
                start2.longitude(),
                start2.latitude(),
                end2.longitude(),
                end2.latitude());
    }

    private static LineIntersectsResult lineIntersects(double line1StartX, double line1StartY,
                                                       double line1EndX, double line1EndY,
                                                       double line2StartX, double line2StartY,
                                                       double line2EndX, double line2EndY) {

        LineIntersectsResult result = LineIntersectsResult.builder()
                .onLine1(false)
                .onLine2(false)
                .build();

        double denominator = ((line2EndY - line2StartY) * (line1EndX - line1StartX))
                - ((line2EndX - line2StartX) * (line1EndY - line1StartY));
        if (denominator == 0) {
            if (result.horizontalIntersection() != null && result.verticalIntersection() != null) {
                return result;
            } else {
                return null;
            }
        }
        double varA = line1StartY - line2StartY;
        double varB = line1StartX - line2StartX;
        double numerator1 = ((line2EndX - line2StartX) * varA) - ((line2EndY - line2StartY) * varB);
        double numerator2 = ((line1EndX - line1StartX) * varA) - ((line1EndY - line1StartY) * varB);
        varA = numerator1 / denominator;
        varB = numerator2 / denominator;

        // if we cast these lines infinitely in both directions, they intersect here:
        result = result.toBuilder().horizontalIntersection(line1StartX
                + (varA * (line1EndX - line1StartX))).build();
        result = result.toBuilder().verticalIntersection(line1StartY
                + (varA * (line1EndY - line1StartY))).build();

        // if line1 is a segment and line2 is infinite, they intersect if:
        if (varA > 0 && varA < 1) {
            result = result.toBuilder().onLine1(true).build();
        }
        // if line2 is a segment and line1 is infinite, they intersect if:
        if (varB > 0 && varB < 1) {
            result = result.toBuilder().onLine2(true).build();
        }
        // if line1 and line2 are segments, they intersect if both of the above are true
        if (result.onLine1() && result.onLine2()) {
            return result;
        } else {
            return null;
        }
    }

    /**
     * Add data to the map once the GeoJSON has been loaded
     *
     * @param featureCollection returned GeoJSON FeatureCollection from the Directions API route request
     */
    private void initData(Style fullyLoadedStyle, @NonNull FeatureCollection featureCollection) {
        if (featureCollection.features() != null) {
            LineString lineString = ((LineString) featureCollection.features().get(0).geometry());
            if (lineString != null) {
                routeCoordinateList = lineString.coordinates();
                initSources(fullyLoadedStyle, featureCollection);
                initSymbolLayer(fullyLoadedStyle);
                initDotLinePath(fullyLoadedStyle);
                animate();
            }
        }
    }

//    /**
//     * Set up the repeat logic for moving the icon along the route.
//     */
//    private void animate() {
//        // Check if we are at the end of the points list
//        if ((routeCoordinateList.size() - 1 > routeIndex)) {
//            Point indexPoint = routeCoordinateList.get(routeIndex);
//            Point newPoint = Point.fromLngLat(indexPoint.longitude(), indexPoint.latitude());
//            currentAnimator = createLatLngAnimator(indexPoint, newPoint);
//            currentAnimator.start();
//            routeIndex++;
//        }
//    }
private void animate() {
    // Animating the marker requires the use of both the ValueAnimator and a handler.
    // The ValueAnimator is used to move the marker between the GeoJSON points, this is
    // done linearly. The handler is used to move the marker along the GeoJSON points.
    handler = new Handler();
    runnable = new Runnable() {
        @Override
        public void run() {
            // Check if we are at the end of the points list, if so we want to stop using
            // the handler.
            if ((routeCoordinateList.size() - 1 > count)) {

                Point nextLocation = routeCoordinateList.get(count + 1);

                if (markerIconAnimator != null && markerIconAnimator.isStarted()) {
                    markerIconCurrentLocation = (LatLng) markerIconAnimator.getAnimatedValue();
                    markerIconAnimator.cancel();
                }
                markerIconAnimator = ObjectAnimator
                        .ofObject(latLngEvaluator, count == 0 || markerIconCurrentLocation == null
                                        ? new LatLng(37.61501, -122.385374)
                                        : markerIconCurrentLocation,
                                new LatLng(nextLocation.latitude(), nextLocation.longitude()))
                        .setDuration(300);
                markerIconAnimator.setInterpolator(new LinearInterpolator());

                markerIconAnimator.addUpdateListener(animatorUpdateListener);
                markerIconAnimator.start();

                // Keeping the current point count we are on.
                count++;

                // Once we finish we need to repeat the entire process by executing the
                // handler again once the ValueAnimator is finished.
                handler.postDelayed(this, 300);
            }
        }
    };
    handler.post(runnable);
}

    /**
     * Listener interface for when the ValueAnimator provides an updated value
     */
    private final ValueAnimator.AnimatorUpdateListener animatorUpdateListener =
            new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    LatLng animatedPosition = (LatLng) valueAnimator.getAnimatedValue();
                    if (dotGeoJsonSource != null) {
                        dotGeoJsonSource.setGeoJson(Point.fromLngLat(
                                animatedPosition.getLongitude(), animatedPosition.getLatitude()));
                    }
                }
            };

    private static class PointEvaluator implements TypeEvaluator<Point> {

        @Override
        public Point evaluate(float fraction, Point startValue, Point endValue) {
            return Point.fromLngLat(
                    startValue.longitude() + ((endValue.longitude() - startValue.longitude()) * fraction),
                    startValue.latitude() + ((endValue.latitude() - startValue.latitude()) * fraction)
            );
        }
    }

    private Animator createLatLngAnimator(Point currentPosition, Point targetPosition) {
        ValueAnimator latLngAnimator = ValueAnimator.ofObject(new TrafficReRouting.PointEvaluator(), currentPosition, targetPosition);
//    latLngAnimator.setDuration((long) TurfMeasurement.distance(currentPosition, targetPosition, "kilometres"));
        latLngAnimator.setDuration((long)160);
        latLngAnimator.setInterpolator(new LinearInterpolator());
        latLngAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animate();
            }
        });
        latLngAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Point point = (Point) animation.getAnimatedValue();
                pointSource.setGeoJson(point);
                markerLinePointList.add(point);
                lineSource.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(markerLinePointList)));
            }
        });

        return latLngAnimator;
    }

    /**
     * Make a request to the Mapbox Directions API. Once successful, pass the route to the
     * route layer.
     *
     * @param origin      the starting point of the route
     * @param destination the desired finish point of the route
     */
    private void getRoute(final Point origin, final Point destination) {
        MapboxDirections client = MapboxDirections.builder()
                .origin(origin)
                .destination(destination)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                //.profile(DirectionsCriteria.PROFILE_CYCLING)
                .profile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
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
                DirectionsRoute currentRoute = response.body().routes().get(0);
                mapboxMap.getStyle(new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(
                                new LatLngBounds.Builder()
                                        .include(new LatLng(origin.latitude(), origin.longitude()))
                                        .include(new LatLng(destination.latitude(), destination.longitude()))
                                        .build(), 450), 10);

                        initData(style,FeatureCollection.fromFeature(
                                Feature.fromGeometry(LineString.fromPolyline(currentRoute.geometry(), PRECISION_6))));
                    }
                });
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Timber.e("Error: %s", throwable.getMessage());
                Toast.makeText(TrafficReRouting.this, "Error: " + throwable.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Add various sources to the map.
     */
    private void initSources(@NonNull Style loadedMapStyle, @NonNull FeatureCollection featureCollection) {
        dotGeoJsonSource = new GeoJsonSource(DOT_SOURCE_ID, featureCollection);
        loadedMapStyle.addSource(dotGeoJsonSource);
//    loadedMapStyle.addSource(pointSource = new GeoJsonSource(DOT_SOURCE_ID, featureCollection));
        loadedMapStyle.addSource(new GeoJsonSource(LINE_SOURCE_ID, featureCollection));
    }

    /**
     * Add the marker icon SymbolLayer.
     */
    private void initSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("moving-red-marker", Objects.requireNonNull(BitmapUtils.getBitmapFromDrawable(
                getResources().getDrawable(R.drawable.ic_car))));
        loadedMapStyle.addLayer(new SymbolLayer("symbol-layer-id", DOT_SOURCE_ID).withProperties(
                iconImage("moving-red-marker"),
                iconSize(1f),
                iconOffset(new Float[] {5f, 0f}),
                iconIgnorePlacement(true),
                iconAllowOverlap(true)
        ));
    }

    /**
     * Add the LineLayer for the marker icon's travel route. Adding it under the "road-label" layer, so that the
     * this LineLayer doesn't block the street name.
     */
    private void initDotLinePath(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addLayerBelow(new LineLayer("line-layer-id", LINE_SOURCE_ID).withProperties(
                lineColor(Color.parseColor("#F13C6E")),
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(4f)), "road-label");
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
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
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }
        if (mapboxMap != null) {
            mapboxMap.removeOnMapClickListener(this);
        }
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * Method is used to interpolate the SymbolLayer icon animation.
     */
    private static final TypeEvaluator<LatLng> latLngEvaluator = new TypeEvaluator<LatLng>() {

        private final LatLng latLng = new LatLng();

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            latLng.setLatitude(startValue.getLatitude()
                    + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
            latLng.setLongitude(startValue.getLongitude()
                    + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
            return latLng;
        }
    };


}
