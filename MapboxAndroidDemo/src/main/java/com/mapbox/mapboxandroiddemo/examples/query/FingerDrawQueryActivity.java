package com.mapbox.mapboxandroiddemo.examples.query;

import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

/**
 * Use the Android system {@link android.view.View.OnTouchListener} to draw
 * an polygon and/or a line. Also perform a search for data points within the drawn polygon area.
 */
public class FingerDrawQueryActivity extends AppCompatActivity {

  private static final String SEARCH_DATA_SYMBOL_LAYER_SOURCE_ID = "SEARCH_DATA_SYMBOL_LAYER_SOURCE_ID";
  private static final String MARKER_SYMBOL_LAYER_SOURCE_ID = "MARKER_SYMBOL_LAYER_SOURCE_ID";
  private static final String SEARCH_DATA_SYMBOL_LAYER_ID = "SEARCH_DATA_SYMBOL_LAYER_ID";
  private static final String SEARCH_DATA_MARKER_ID = "SEARCH_DATA_MARKER_ID";

  private MapView mapView;
  private MapboxMap mapboxMap;
  private boolean showSearchDataLocations = true;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_finger_drag_draw);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
          @Override
          public void onStyleLoaded(@NonNull Style style) {

            FingerDrawQueryActivity.this.mapboxMap = mapboxMap;

            if (showSearchDataLocations) {
              //new LoadGeoJson(FingerDrawQueryActivity.this).execute();
            } else {
              setUpExample(null);
            }

            findViewById(R.id.fetch_bus_stops_for_dublin)
                    .setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View view) {

                        new DownloadFileFromURL(FingerDrawQueryActivity.this).execute("http://ec2-3-86-225-133.compute-1.amazonaws.com/get_all_bus_stops");
                      }
                    });
          }
        });
      }
    });
  }

  /**
   * Enable moving the map
   */
  private void enableMapMovement() {
    mapView.setOnTouchListener(null);
  }

  private void setUpExample(FeatureCollection searchDataFeatureCollection) {

    mapboxMap.getStyle(new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style loadedStyle) {
        loadedStyle.addImage(SEARCH_DATA_MARKER_ID, BitmapFactory.decodeResource(
          FingerDrawQueryActivity.this.getResources(), R.drawable.blue_marker_view));

        // Add sources to the map
        loadedStyle.addSource(new GeoJsonSource(SEARCH_DATA_SYMBOL_LAYER_SOURCE_ID,
          searchDataFeatureCollection));
        loadedStyle.addSource(new GeoJsonSource(MARKER_SYMBOL_LAYER_SOURCE_ID));

        loadedStyle.addLayer(new SymbolLayer(SEARCH_DATA_SYMBOL_LAYER_ID,
          SEARCH_DATA_SYMBOL_LAYER_SOURCE_ID).withProperties(
          iconImage(SEARCH_DATA_MARKER_ID),
          iconAllowOverlap(true),
          iconOffset(new Float[] {0f, -8f}),
          iconIgnorePlacement(true))
        );

        Toast.makeText(FingerDrawQueryActivity.this,
          getString(R.string.draw_instruction), Toast.LENGTH_SHORT).show();
      }
    });
  }

  /**
   * Use an AsyncTask to retrieve GeoJSON data from a file in the assets folder.
   */
  private static class LoadGeoJson extends AsyncTask<Void, Void, FeatureCollection> {

    private WeakReference<FingerDrawQueryActivity> weakReference;

    LoadGeoJson(FingerDrawQueryActivity activity) {
      this.weakReference = new WeakReference<>(activity);
    }

    @Override
    protected FeatureCollection doInBackground(Void... voids) {
      try {
        FingerDrawQueryActivity activity = weakReference.get();
        if (activity != null) {
          InputStream inputStream = activity.getAssets().open("bus_stops.geojson");
          return FeatureCollection.fromJson(convertStreamToString(inputStream));
        }
      } catch (Exception exception) {
        Timber.e("Exception Loading GeoJSON: %s", exception.toString());
      }
      return null;
    }

    static String convertStreamToString(InputStream is) {
      Scanner scanner = new Scanner(is).useDelimiter("\\A");
      return scanner.hasNext() ? scanner.next() : "";
    }


    @Override
    protected void onPostExecute(@Nullable FeatureCollection featureCollection) {
      super.onPostExecute(featureCollection);
      FingerDrawQueryActivity activity = weakReference.get();
      if (activity != null && featureCollection != null) {
        activity.setUpExample(featureCollection);
      }
    }
  }

  /**
   * Background Async Task to download file
   * */
  class DownloadFileFromURL extends AsyncTask<String, String, FeatureCollection> {

    private WeakReference<FingerDrawQueryActivity> weakReference;
    private ProgressDialog dialog;

    DownloadFileFromURL(FingerDrawQueryActivity activity) {
      this.weakReference = new WeakReference<>(activity);
      dialog = new ProgressDialog(activity);
    }

    /**
     * Before starting background thread Show Progress Bar Dialog
     * */
    @Override
    protected void onPreExecute() {
      super.onPreExecute();

      dialog.setMessage("Fetching bus stops.");
      dialog.show();
    }

    /**
     * Downloading file in background thread
     * */
    @Override
    protected FeatureCollection doInBackground(String... f_url) {
      int count;
      try {
        FingerDrawQueryActivity activity = weakReference.get();
        if (activity != null) {
          URL url = new URL(f_url[0]);
          String filepath = activity.getFilesDir().toString()
                  + f_url[0].substring(f_url[0].lastIndexOf('/') + 1);
          URLConnection conection = url.openConnection();
          conection.connect();

          // this will be useful so that you can show a tipical 0-100%
          // progress bar
          int lenghtOfFile = conection.getContentLength();

          // download the file
          InputStream input = new BufferedInputStream(url.openStream(),
                  8192);

          // Output stream
          OutputStream output = new FileOutputStream(filepath);

          byte data[] = new byte[1024];

          long total = 0;

          while ((count = input.read(data)) != -1) {
            total += count;
            // publishing the progress....
            // After this onProgressUpdate will be called
            publishProgress("" + (int) ((total * 100) / lenghtOfFile));

            // writing data to file
            output.write(data, 0, count);
          }

          // flushing output
          output.flush();

          // closing streams
          output.close();
          input.close();

          String geojson = new String(Files.readAllBytes(Paths.get(filepath)));
          return FeatureCollection.fromJson(geojson);
        }

      } catch (Exception e) {
        Log.e("Error: ", e.getMessage());
      }

      return null;
    }

    /**
     * Updating progress bar
     * */
    protected void onProgressUpdate(String progress) {
      if (dialog.isShowing()) {
        dialog.setMessage(progress);
      }
    }

    /**
     * After completing background task Dismiss the progress dialog
     * **/
    @Override
    protected void onPostExecute(@Nullable FeatureCollection featureCollection) {
      super.onPostExecute(featureCollection);

      if (dialog.isShowing()) {
        dialog.dismiss();
      }

      FingerDrawQueryActivity activity = weakReference.get();
      if (activity != null && featureCollection != null) {
        activity.setUpExample(featureCollection);
      }
    }

  }

  // Add the mapView lifecycle to the activity's lifecycle methods
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
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}
