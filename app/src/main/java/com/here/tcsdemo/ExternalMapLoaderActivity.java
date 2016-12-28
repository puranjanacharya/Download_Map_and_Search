package com.here.tcsdemo;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.MapEngine;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapView;
import com.here.android.mpa.odml.MapLoader;
import com.here.android.mpa.odml.MapPackage;
import com.here.android.mpa.search.ErrorCode;
import com.here.android.mpa.search.GeocodeRequest;
import com.here.android.mpa.search.Location;
import com.here.android.mpa.search.ResultListener;
import com.here.odnp.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ExternalMapLoaderActivity extends Activity {

    private final static String TAG = ExternalMapLoaderActivity.class.getSimpleName();

    private MapView mapView;
    private Map map;

    private ArrayList<MapPackage> currentInstalledMaps;
    private String currentInstalledMapsString;
    private ProgressBar downloadProgressBar;
    private static final Integer THRESHOLD = 2;
    private CustomAutoCompleteTextView mGeoAutocomplete;
    private MapFragment mMapFragment;
    private Map mMap;
    private GeocompleteAdapter mGeoAutoCompleteAdapter;

    // Position Listener
    PositioningManager.OnPositionChangedListener mPositionListener = new PositioningManager.OnPositionChangedListener() {

        @Override
        public void onPositionUpdated(PositioningManager.LocationMethod method, GeoPosition position,
                                      boolean isMapMatched) {
            if (position != null) {
                mGeoAutoCompleteAdapter.setPosition(position);
            }
        }

        @Override
        public void onPositionFixChanged(PositioningManager.LocationMethod method, PositioningManager.LocationStatus status) {

        }
    };
    private PositioningManager mPositionManager;
    private MapMarker mMarker;
    protected ResultListener<List<Location>> m_listener = new ResultListener<List<Location>>() {
        @Override
        public void onCompleted(List<Location> data, ErrorCode error) {
            if (error == ErrorCode.NONE) {
                if (data != null && data.size() > 0) {
                    addMarker(data.get(0).getCoordinate());
                }
            }
        }
    };

    @Override
  protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_external_map_loader);
        mapView = (MapView) findViewById(R.id.ext_mapview);
        downloadProgressBar = (ProgressBar) findViewById(R.id.ext_progressBar);

        MapEngine.getInstance().init(this, engineInitHandler);

        mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapfragment);
        mMapFragment.init(new OnEngineInitListener() {

            @Override
            public void onEngineInitializationCompleted(Error error) {
                if (error == Error.NONE) {
                    mMap = mMapFragment.getMap();
                    mMap.setProjectionMode(Map.Projection.MERCATOR);
                    mMap.getPositionIndicator().setVisible(true);
                    mPositionManager = PositioningManager.getInstance();
                    mPositionManager
                            .addListener(new WeakReference<>(mPositionListener));
                    mPositionManager.start(PositioningManager.LocationMethod.GPS_NETWORK);
                }
            }
        });

        // UI customization
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setIcon(android.R.color.transparent);
        LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.action_bar, null);
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.WRAP_CONTENT);
        actionBar.setCustomView(v, layoutParams);

        mGeoAutocomplete = (CustomAutoCompleteTextView) v.findViewById(R.id.geo_autocomplete);
        mGeoAutocomplete.setThreshold(THRESHOLD);
        mGeoAutocomplete.setLoadingIndicator((android.widget.ProgressBar) v
                .findViewById(R.id.pb_loading_indicator));

        mGeoAutoCompleteAdapter = new GeocompleteAdapter(this);
        mGeoAutocomplete.setAdapter(mGeoAutoCompleteAdapter);

        mGeoAutocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String result = (String) adapterView.getItemAtPosition(position);
                mGeoAutocomplete.setText(result);
                GeocodeRequest req = new GeocodeRequest(result);
                req.setSearchArea(mMap.getBoundingBox());
                req.execute(m_listener);
            }
        });

        mGeoAutocomplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"".equals(mGeoAutocomplete.getText().toString())) {
                    GeocodeRequest req = new GeocodeRequest(mGeoAutocomplete.getText().toString());
                    req.setSearchArea(mMap.getBoundingBox());
                    req.execute(m_listener);
                }
            }
        });
    }

    /**
     * Add marker on map.
     *
     * @param geoCoordinate GeoCoordinate for marker to be added.
     */
    private void addMarker(GeoCoordinate geoCoordinate) {
        if (mMarker == null) {
            Image image = new Image();
            try {
                image.setImageResource(R.drawable.pin);
            } catch (final IOException e) {
                e.printStackTrace();
            }
            mMarker = new MapMarker(geoCoordinate, image);
            mMarker.setAnchorPoint(new PointF(image.getWidth() / 2, image.getHeight()));
            mMap.addMapObject(mMarker);
        } else {
            mMarker.setCoordinate(geoCoordinate);
        }
        mMap.setCenter(geoCoordinate, Map.Animation.BOW);
    }

/*
   @Override
    protected void onResume() {
        super.onResume();
        if (mPositionManager != null && !mPositionManager.isActive()) {
            mPositionManager
                    .addListener(new WeakReference<>(mPositionListener));
            mPositionManager.start(PositioningManager.LocationMethod.GPS_NETWORK);
        }
    }
*/

/*
    @Override
    protected void onPause() {
        super.onPause();
        if (mPositionManager != null) {
            mPositionManager.removeListener(mPositionListener);
            mPositionManager.stop();
        }
    }
*/






    private MapLoader.Listener mapLoaderHandler = new MapLoader.Listener() {

        @Override
        public void onProgress(int progress) {
            Log.i(TAG, "Progress " + progress + "%");
            downloadProgressBar.setProgress(progress);
        }

        @Override
        public void onInstallationSize(long diskSize, long networkSize) {
            Log.i(TAG, "Map data require " + diskSize);
        }


        @Override
        public void onGetMapPackagesComplete(MapPackage rootMapPackage,
                                             MapLoader.ResultCode resultCode) {
            if (resultCode == MapLoader.ResultCode.OPERATION_SUCCESSFUL) {
                Log.i(TAG, "Map packages received successful: " + rootMapPackage.getTitle());

                currentInstalledMaps = new ArrayList<>(1);
                populateInstalledMaps(rootMapPackage);
            } else {
                Log.e(TAG, "Can't retrieve map packages: " + resultCode.name());
                Toast.makeText(ExternalMapLoaderActivity.this,
                        "Error: " + resultCode.name(), Toast.LENGTH_SHORT).show();
                return;
            }

            StringBuilder sb = new StringBuilder();
            for (MapPackage pac : currentInstalledMaps) {
                sb.append(pac.getTitle());
                sb.append("\n");
            }

            currentInstalledMapsString = sb.toString();
        }


        private void populateInstalledMaps(MapPackage pac) {
            // only take installed root package, so if e.g. Germany is installed,
            // don't check for children states
            if (pac.getInstallationState() == MapPackage.InstallationState.INSTALLED) {
                Log.i(TAG, "Installed package found: " + pac.getTitle() + " id " + pac.getId());
                currentInstalledMaps.add(pac);
            } else if (pac.getChildren() != null && pac.getChildren().size() > 0) {
                for (MapPackage p : pac.getChildren()) {
                    populateInstalledMaps(p);
                }
            }
        }


        @Override
        public void onCheckForUpdateComplete(boolean updateAvailable, String currentMapVersion,
                                             String newestMapVersion, MapLoader.ResultCode resultCode) {
            Log.i(TAG, "onCheckForUpdateComplete. Update available: " + updateAvailable +
                    " current version: " + currentMapVersion);

            AlertDialog.Builder builder = new AlertDialog.Builder(
                    ExternalMapLoaderActivity.this, R.style.AppCompatAlertDialogStyle);

            builder.setTitle("Map version checked");
            builder.setMessage("Current map version: " + currentMapVersion + "\n\n"
                    + (updateAvailable ? "Update found to " + newestMapVersion : "No update found")
                    + "\n\n" + "Installed maps:\n" + currentInstalledMapsString);
            builder.setNegativeButton("close", null);

            if (updateAvailable) {
                builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "Update triggered");
                        downloadProgressBar.setProgress(0);
                        downloadProgressBar.setVisibility(View.VISIBLE);
                        MapLoader.getInstance().performMapDataUpdate();
                    }
                });
            }

            builder.show();
        }


        @Override
        public void onPerformMapDataUpdateComplete(MapPackage rootMapPackage,
                                                   MapLoader.ResultCode resultCode) {
            Log.i(TAG, "onPerformMapDataUpdateComplete");
            downloadProgressBar.setVisibility(View.INVISIBLE);
            String message;
            if (resultCode == MapLoader.ResultCode.OPERATION_SUCCESSFUL) {
                message = "Map updated successfully";
            } else {
                message = "Map update failed: " + resultCode.name();
            }
            Toast.makeText(ExternalMapLoaderActivity.this, message, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onInstallMapPackagesComplete(MapPackage rootMapPackage,
                                                 MapLoader.ResultCode resultCode) {
            downloadProgressBar.setVisibility(View.INVISIBLE);
            String message;
            if (resultCode == MapLoader.ResultCode.OPERATION_SUCCESSFUL) {
                message = "Maps installed successfully";
                Log.i(TAG, "Package is installed: "
                        + rootMapPackage.getId() + " " + rootMapPackage.getTitle());
                MapLoader.getInstance().getMapPackages();
            } else {
                message = "Map installation failed: " + resultCode.name();
                Log.e(TAG, "Failed to install package: "
                        + rootMapPackage.getId() + " " + rootMapPackage.getTitle());
            }
            Toast.makeText(ExternalMapLoaderActivity.this, message, Toast.LENGTH_SHORT).show();
        }


        @Override
        public void onUninstallMapPackagesComplete(MapPackage rootMapPackage,
                                                   MapLoader.ResultCode resultCode) {
            Log.i(TAG, "onUninstallMapPackagesComplete");
            String message;
            if (resultCode == MapLoader.ResultCode.OPERATION_SUCCESSFUL) {
                message = "Maps removed successfully";
            } else {
                message = "Map removal failed: " + resultCode.name();
            }
            Toast.makeText(ExternalMapLoaderActivity.this, message, Toast.LENGTH_SHORT).show();

            // update packages and get installation state
            MapLoader.getInstance().getMapPackages();
        }
    };
    private OnEngineInitListener engineInitHandler = new OnEngineInitListener() {
        @Override
        public void onEngineInitializationCompleted(Error error) {
            if (error == Error.NONE) {
                map = new Map();
                mapView.setMap(map);

                // more map settings
                map.setProjectionMode(Map.Projection.GLOBE);  // globe projection
                map.setExtrudedBuildingsVisible(true);
                map.setLandmarksVisible(false);
                map.setZoomLevel(map.getMinZoomLevel());

                MapLoader.getInstance().addListener(mapLoaderHandler);

                // update packages and get installation state
                MapLoader.getInstance().getMapPackages();
            } else {
                Log.e(TAG, "ERROR: Cannot initialize Map Fragment " + error);
            }
        }
    };

/*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_external_map_loader);

        mapView = (MapView) findViewById(R.id.ext_mapview);
        downloadProgressBar = (ProgressBar) findViewById(R.id.ext_progressBar);

        MapEngine.getInstance().init(this, engineInitHandler);
    }*/

    @Override
    public void onResume() {
        super.onResume();
        MapEngine.getInstance().onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        if (mapView != null) {
            mapView.onPause();
        }
        MapEngine.getInstance().onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        map = null;
        super.onDestroy();
    }
    public void onDownloadButtonClicked(View view)
    {
        //SearchRequest searchRequest = new SearchRequest("Kathmandu");


        Log.d(TAG, "Downloading new map data...");

        List<Integer> downloadList = new ArrayList<>(1);
        downloadList.add(120002);    // Berlin/Brandenburg id 120002

        downloadProgressBar.setProgress(0);
        downloadProgressBar.setVisibility(View.VISIBLE);
        MapLoader.getInstance().installMapPackages(downloadList);
    }

    public void onRemoveAllButtonClicked(View view) {
        Log.d(TAG, "Removing all map data...");

        List<Integer> removalList = new ArrayList<>(1);

        if (currentInstalledMaps == null || currentInstalledMaps.size() <= 0)
        return;

        for (MapPackage pac : currentInstalledMaps)
            removalList.add(pac.getId());

        MapLoader.getInstance().uninstallMapPackages(removalList);
    }

    public void onCheckMapUpdatesButtonClicked(View view) {
        MapLoader.getInstance().checkForMapDataUpdate();
    }
/*public class SearchRequest {
    public SearchRequest


}*/



}
