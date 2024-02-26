package com.example.ourplanet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback, MapboxMap.OnMapClickListener {

    private MapView mapView;
    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String ICON_ID = "ICON_ID";
    private static final String LAYER_ID = "LAYER_ID";
    private MapboxMap mapboxMap;
    ImageView back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, "pk.eyJ1IjoidGFraXN2b3V0b3MiLCJhIjoiY2tpN2xzMDZyMGVsNDJ4czA5azl6eG9mbyJ9.-wtvCytMW7ZiEj6__q88eg");

        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        back = findViewById(R.id.map_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backGroup = new Intent (MapActivity.this, MainActivity.class);
                startActivity(backGroup);
            }
        });
    }
    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {

        MapActivity.this.mapboxMap = mapboxMap;

        List<Feature> symbolLayerIconFeatureList = new ArrayList<>();
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(23.7275, 37.9838)));

        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")
                .withImage(ICON_ID, BitmapFactory.decodeResource(
                        MapActivity.this.getResources(), R.drawable.mapbox_marker_icon_default))
                .withSource(new GeoJsonSource(SOURCE_ID,
                        FeatureCollection.fromFeatures(symbolLayerIconFeatureList)))
                .withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
                        .withProperties(
                                iconImage(ICON_ID),
                                iconAllowOverlap(true),
                                iconIgnorePlacement(true)
                        )
                ), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
            }
        });

        mapboxMap.addOnMapClickListener(this);
    }

    @Override
    public boolean onMapClick(@NonNull final LatLng point) {

        final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);
        List<Feature> symbolLayerIconFeatureList = new ArrayList<>();
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(point.getLongitude(), point.getLatitude())));
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        mapboxMap.clear();
        try {
            addresses = geocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            mapboxMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title("Address")
                    .snippet(address));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")
//                .withImage(ICON_ID, BitmapFactory.decodeResource(
//                        MapActivity.this.getResources(), R.drawable.mapbox_marker_icon_default))
//                .withSource(new GeoJsonSource(SOURCE_ID,
//                        FeatureCollection.fromFeatures(symbolLayerIconFeatureList)))
//                .withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
//                        .withProperties(
//                                iconImage(ICON_ID),
//                                iconAllowOverlap(true),
//                                iconIgnorePlacement(true)
//                        )
//                ), new Style.OnStyleLoaded() {
//            @Override
//            public void onStyleLoaded(@NonNull Style style) {
//            }
//        });

        List<Feature> features = mapboxMap.queryRenderedFeatures(pixel,"water");
        ///37.9838° N, 23.7275° E
        List<Feature> newFeatureList = new ArrayList<>();
        for (Feature singleFeature : features) {
            newFeatureList.add(singleFeature);
        }
        Log.e("","+++++++++++++++++"+point);
        Log.e("","+++++++++++++++++"+newFeatureList.toString());

        if (features.size() > 0) {

//            mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")
//                    .withImage(ICON_ID, BitmapFactory.decodeResource(
//                            MapActivity.this.getResources(), R.drawable.mapbox_marker_icon_default))
//                    .withSource(new GeoJsonSource(SOURCE_ID,
//                            FeatureCollection.fromFeatures(newFeatureList)))
//                    .withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
//                            .withProperties(
//                                    iconImage(ICON_ID),
//                                    iconAllowOverlap(true),
//                                    iconIgnorePlacement(true)
//                            )
//                    ), new Style.OnStyleLoaded() {
//                @Override
//                public void onStyleLoaded(@NonNull Style style) {
//                }
//            });
        }
        return true;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
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
}