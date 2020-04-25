package com.example.minutedublin;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import com.mapbox.turf.TurfMeasurement;

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
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * Make a directions request with the Mapbox Directions API and then draw a line behind a moving
 * SymbolLayer icon which moves along the Directions response route.
 */
public class MovingIconWithTrailingLineActivity extends AppCompatActivity {

  private static final String DOT_SOURCE_ID = "dot-source-id";
  private static final String LINE_SOURCE_ID = "line-source-id";

  private MapView mapView;
  private MapboxMap mapboxMap;

  private GeoJsonSource pointSource;
  private GeoJsonSource lineSource;
  private List<Point> routeCoordinateList;
  private List<Point> markerLinePointList = new ArrayList<>();
  private int routeIndex;
  //private Point originPoint = Point.fromLngLat(-6.2450408935546875, 53.35372769822772);
  //private Point destinationPoint = Point.fromLngLat(-6.294994354248047, 53.34850191547604);
  private Animator currentAnimator;


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
        MovingIconWithTrailingLineActivity.this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {
            // Use the Mapbox Directions API to get a directions route
//            getRoute(originPoint, destinationPoint);
            getRoute(
                    Point.fromLngLat(-6.2635216607018, 53.3955275266125),
                    Point.fromLngLat(-6.2674, 53.3547)
            );

            ///////show healthcenter
            try {
              URI geoJsonUrl = new URI("http://ec2-46-51-146-5.eu-west-1.compute.amazonaws.com:8080/emergency_center/fetch_healthcare");
              //Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_marker_icon_default);
              Drawable drawable = ResourcesCompat.getDrawable(getResources(),R.drawable.ic_hospital, null);
              Bitmap mBitmap = BitmapUtils.getBitmapFromDrawable(drawable);
              style.addImage("health-geojson", mBitmap);
              GeoJsonSource geoJsonSource = new GeoJsonSource("health-geojson-source", geoJsonUrl);
              style.addSource(geoJsonSource);
              SymbolLayer healthSymbolLayer = new SymbolLayer("health-symbol-layer-id","health-geojson-source");
              healthSymbolLayer.setProperties(PropertyFactory.iconImage("health-geojson"));
              healthSymbolLayer.withProperties(iconImage("health-geojson"),iconAllowOverlap(true),
                      iconIgnorePlacement(true));
              style.addLayer(healthSymbolLayer);
            } catch (URISyntaxException exception) {
              Log.d("Error: ", exception.getMessage());
            }

            ///////fire station
            try {
              URI geoJsonUrl = new URI("http://ec2-46-51-146-5.eu-west-1.compute.amazonaws.com:8080/emergency_center/fetch_fire_brigade");

              Drawable drawable = ResourcesCompat.getDrawable(getResources(),R.drawable.ic_firestation, null);
              Bitmap mBitmap = BitmapUtils.getBitmapFromDrawable(drawable);
              style.addImage("fire-geojson", mBitmap);
              GeoJsonSource geoJsonSource = new GeoJsonSource("fire-geojson-source", geoJsonUrl);
              style.addSource(geoJsonSource);
              SymbolLayer fireSymbolLayer = new SymbolLayer("fire-symbol-layer-id","fire-geojson-source");
              fireSymbolLayer.setProperties(PropertyFactory.iconImage("fire-geojson"));
              fireSymbolLayer.withProperties(iconImage("fire-geojson"),iconAllowOverlap(true),
                      iconIgnorePlacement(true));
              style.addLayer(fireSymbolLayer);
            } catch (URISyntaxException exception) {
              Log.d("Error: ", exception.getMessage());
            }

            ///////show garda
            try {
              URI geoJsonUrl = new URI("http://ec2-46-51-146-5.eu-west-1.compute.amazonaws.com:8080/emergency_center/fetch_garda");

              Drawable drawable = ResourcesCompat.getDrawable(getResources(),R.drawable.ic_garda, null);
              Bitmap mBitmap = BitmapUtils.getBitmapFromDrawable(drawable);
              style.addImage("garda-geojson", mBitmap);
              GeoJsonSource geoJsonSource = new GeoJsonSource("garda-geojson-source", geoJsonUrl);
              style.addSource(geoJsonSource);
              SymbolLayer gardaSymbolLayer = new SymbolLayer("garda-symbol-layer-id","garda-geojson-source");
              gardaSymbolLayer.setProperties(PropertyFactory.iconImage("garda-geojson"));
              gardaSymbolLayer.withProperties(iconImage("garda-geojson"),iconAllowOverlap(true),
                      iconIgnorePlacement(true));
              style.addLayer(gardaSymbolLayer);
            } catch (URISyntaxException exception) {
              Log.d("Error: ", exception.getMessage());
            }


            ////show report
            try {
              URI geoJsonUrl = new URI("http://ec2-46-51-146-5.eu-west-1.compute.amazonaws.com:8080/report/fetch_reports");
              //Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_marker_icon_default);
              Drawable drawable = ResourcesCompat.getDrawable(getResources(),R.drawable.ic_alert, null);
              Bitmap mBitmap = BitmapUtils.getBitmapFromDrawable(drawable);
              style.addImage("reporttwo-geojson", mBitmap);

              GeoJsonSource geoJsonSource = new GeoJsonSource("reporttwo-geojson-source", geoJsonUrl);
              //geoJsonSource.
              style.addSource(geoJsonSource);
              SymbolLayer reporttwoSymbolLayer = new SymbolLayer("reporttwo-symbol-layer-id","reporttwo-geojson-source");
              reporttwoSymbolLayer.setProperties(PropertyFactory.iconImage("reporttwo-geojson"));
              reporttwoSymbolLayer.withProperties(iconImage("reporttwo-geojson"),iconAllowOverlap(true),
                      iconIgnorePlacement(true));
              style.addLayer(reporttwoSymbolLayer);
            } catch (URISyntaxException exception) {
              Log.d("Error: ", exception.getMessage());
            }



          }
        });
      }
    });
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

  /**
   * Set up the repeat logic for moving the icon along the route.
   */
  private void animate() {
    // Check if we are at the end of the points list
    if ((routeCoordinateList.size() - 1 > routeIndex)) {
      Point indexPoint = routeCoordinateList.get(routeIndex);
      Point newPoint = Point.fromLngLat(indexPoint.longitude(), indexPoint.latitude());
      currentAnimator = createLatLngAnimator(indexPoint, newPoint);
      currentAnimator.start();
      routeIndex++;
    }
  }

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
    ValueAnimator latLngAnimator = ValueAnimator.ofObject(new PointEvaluator(), currentPosition, targetPosition);
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
        Toast.makeText(MovingIconWithTrailingLineActivity.this, "Error: " + throwable.getMessage(),
            Toast.LENGTH_SHORT).show();
      }
    });
  }

  /**
   * Add various sources to the map.
   */
  private void initSources(@NonNull Style loadedMapStyle, @NonNull FeatureCollection featureCollection) {
    loadedMapStyle.addSource(pointSource = new GeoJsonSource(DOT_SOURCE_ID, featureCollection));
    loadedMapStyle.addSource(lineSource = new GeoJsonSource(LINE_SOURCE_ID));
  }

  /**
   * Add the marker icon SymbolLayer.
   */
  private void initSymbolLayer(@NonNull Style loadedMapStyle) {
    loadedMapStyle.addImage("moving-red-marker", Objects.requireNonNull(BitmapUtils.getBitmapFromDrawable(
            getResources().getDrawable(R.drawable.ic_ambulance))));
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
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}

