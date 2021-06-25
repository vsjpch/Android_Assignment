package com.example.mapboxassignment;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.JsonObject;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.ui.PlaceAutocompleteFragment;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;


import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;



/**
 * Display Markers on the map and also open the Places search dialogue when user clicks on the button.
 */

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback {

        private static final String SOURCE_ID = "SOURCE_ID";
        private static final String ICON_ID = "ICON_ID";
        private static final String LAYER_ID = "LAYER_ID";
        private MapView mapView;
        private MapboxMap mapboxMap;
        private Button placesbutton;
        private CarmenFeature location1;
        private CarmenFeature location2;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

// Mapbox access token is configured here. This needs to be called either in your application
// object or in the same activity which contains the mapview.
            Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

// This contains the MapView in XML and needs to be called after the access token is configured.
            setContentView(R.layout.activity_main);

            mapView = findViewById(R.id.mapView);
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);

        }

        @Override
        public void onMapReady(@NonNull final MapboxMap mapboxMap) {
            this.mapboxMap = mapboxMap;

            List<Feature> symbolLayerIconFeatureList = new ArrayList<>();
            symbolLayerIconFeatureList.add(Feature.fromGeometry(
                    Point.fromLngLat(-57.225365, -33.213144)));
            symbolLayerIconFeatureList.add(Feature.fromGeometry(
                    Point.fromLngLat(-54.14164, -33.981818)));
            symbolLayerIconFeatureList.add(Feature.fromGeometry(
                    Point.fromLngLat(-56.990533, -30.583266)));

            mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")

// Add the SymbolLayer icon image to the map style
                    .withImage(ICON_ID, BitmapFactory.decodeResource(
                            MainActivity.this.getResources(), R.drawable.mapbox_marker_icon_default))

// Adding a GeoJson source for the SymbolLayer icons.
                    .withSource(new GeoJsonSource(SOURCE_ID,
                            FeatureCollection.fromFeatures(symbolLayerIconFeatureList)))

// Adding the actual SymbolLayer to the map style. An offset is added that the bottom of the red
// marker icon gets fixed to the coordinate, rather than the middle of the icon being fixed to
// the coordinate point. This is offset is not always needed and is dependent on the image
// that you use for the SymbolLayer icon.
                    .withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
                            .withProperties(
                                    iconImage(ICON_ID),
                                    iconAllowOverlap(true),
                                    iconIgnorePlacement(true)
                            )
                    ), new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {

// Map is set up and the style has loaded. Now you can add additional data or make other map adjustments.

                }
            });

            placesbutton = findViewById(R.id.startButton);

            // Create 2 locations

            location1 = CarmenFeature.builder().text("Mapbox SF Office")
                    .geometry(Point.fromLngLat(-122.3964485, 37.7912561))
                    .placeName("50 Beale St, San Francisco, CA")
                    .id("mapbox-sf")
                    .properties(new JsonObject())
                    .build();

            location2 = CarmenFeature.builder().text("Mapbox DC Office")
                    .placeName("740 15th Street NW, Washington DC")
                    .geometry(Point.fromLngLat(-77.0338348, 38.899750))
                    .id("mapbox-dc")
                    .properties(new JsonObject())
                    .build();

            // Ceate Intent to call Places plugin
            Intent intent = new PlaceAutocomplete.IntentBuilder()
                    .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.mapbox_access_token))
                    .placeOptions(PlaceOptions.builder()
                            .backgroundColor(Color.parseColor("#EEEEEE"))
                            .limit(10)
                            .addInjectedFeature(location1)
                            .addInjectedFeature(location2)
                            .build(PlaceOptions.MODE_CARDS))
                    .build(MainActivity.this);
            //Show Places UI only when use clicks on "OPEN PLACES SEARCH" button
            placesbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startActivityForResult(intent, 1);
                }

            });

        }

        // Now display the locations retrieved from the Places search UI.
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == MainActivity.RESULT_OK && requestCode == 1) {
                CarmenFeature selectedCarmenfeature = PlaceAutocomplete.getPlace(data);
                if (mapboxMap != null) {
                    Style style = mapboxMap.getStyle();
                    if (style != null) {
                        GeoJsonSource source = style.getSourceAs("geojsonSourceLayerId");
                        if (source != null) {
                            source.setGeoJson(FeatureCollection.fromFeatures(new Feature[]{Feature.fromJson(selectedCarmenfeature.toJson())}));
                        }
                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(new LatLng(((Point) selectedCarmenfeature.geometry()).latitude(),
                                                ((Point) selectedCarmenfeature.geometry()).longitude()))
                                        .zoom(14)
                                        .build()), 4000);
                        // Add marker for the LatLng received from Places search UI.
                        mapboxMap.addMarker(new MarkerOptions()
                                .position(new LatLng(((Point) selectedCarmenfeature.geometry()).latitude(),
                                        ((Point) selectedCarmenfeature.geometry()).longitude())));
                    }
                }
            }
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
            mapView.onDestroy();
        }

        @Override
        protected void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            mapView.onSaveInstanceState(outState);
        }
}

