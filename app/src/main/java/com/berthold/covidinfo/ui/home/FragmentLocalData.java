package com.berthold.covidinfo.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.berthold.covidinfo.R;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;


public class FragmentLocalData extends Fragment implements LocationListener {

    private static final int REQUEST_PERMISSION_RESULT_LOCATION = 1;
    private Location location;
    private LocationManager locationManager;
    private String provider;

    // Location updates will be send by the location manager either if
    // the device is moved more than the specified value or the specified time period
    // has elapsed.
    private static final int UPDATE_LOCATION_EVERY_MS = 10000;
    private static final int UPDATE_WHEN_DEV_IS_MOVED_MORE_THAN_X_METERS = 100;

    // ViewModel
    static FragmentLocalDataViewModel fragmentLocalDataViewModel;

    // UI
    TextView townView, bezView, bundeslandView, casesPer10KView, lasUpdateView, localAddressView, localStatisticsView;
    View waiting;

    public FragmentLocalData() {
        // Empty!
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_show_local_data, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // The view model(s)
        fragmentLocalDataViewModel = ViewModelProviders.of(requireActivity()).get(FragmentLocalDataViewModel.class);

        // UI
        waiting = view.findViewById(R.id.waiting);
        waiting.setVisibility(View.VISIBLE);

        townView = view.findViewById(R.id.town);
        bezView = view.findViewById(R.id.bez);
        bundeslandView = view.findViewById(R.id.bundesland);
        casesPer10KView = view.findViewById(R.id.cases_per_10K);
        lasUpdateView = view.findViewById(R.id.last_update);
        localAddressView = view.findViewById(R.id.current_address);
        localStatisticsView = view.findViewById(R.id.statistics);

        //
        // When clicked, open web browser an try to find local information.
        //
        townView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url;
                String town = townView.getText().toString();
                if (!town.isEmpty()) {
                    url = fragmentLocalDataViewModel.getURLForCurrentLocation();
                    Intent Getintent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(Getintent);
                }
            }
        });


        // @rem:Shows how to check if permissions are granted and how to show a dialog to tell the user to do so....@@
        // @rem:If the permissions are granted, then the Android- System changes that by itself and@@
        // @rem:invokes the appropriate callback. No further coding needed@@
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // For Activity:
            // ActivityCompat.requestPermissions(this, permissionsList, REQUEST_CODE);
            // For Fragment:
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_RESULT_LOCATION);
        }
        //@@

        /*
         * Live data receivers.
         *
         *
         *
         *
         *
         *
         *
         */
        //
        //Receives the covid data from the network.
        //
        fragmentLocalDataViewModel.updateCovidData().observe(getViewLifecycleOwner(), new Observer<List<CovidSearchResultData>>() {
            @Override
            public void onChanged(@Nullable List<CovidSearchResultData> covidSearchResultData) {


                if (covidSearchResultData != null) {

                    waiting.setVisibility(View.GONE);
                    // ToDo Strangely the waiting- view does not disappear when vew.gone is used... this does....
                    waiting.setAlpha(0);


                    for (CovidSearchResultData r : covidSearchResultData) {
                        townView.setText(r.getName());
                        bezView.setText(r.getBez());
                        bundeslandView.setText(r.getBundesland());

                        casesPer10KView.setText(r.getCasesPer10K() + "");
                        casesPer10KView.setTextColor(CovidDataCasesColorCoder.getColor((int) r.getCasesPer10K()));

                        lasUpdateView.setText(r.getLastUpdate());

                    }

                    //
                    // When clicked, show detail view for this location
                    //
                    casesPer10KView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FragmentManager fm = requireActivity().getSupportFragmentManager();
                            FragmentLocationDetailView newChallenge = FragmentLocationDetailView.newInstance("local");
                            newChallenge.show(fm, "fragment_location_detail_view");
                        }
                    });
                }
            }
        });

        //
        // Receives the full address of your location
        //
        // Is invoked whenever the localAddress- var inside the view model is
        // changed by using it's 'setValue' method.
        //
        fragmentLocalDataViewModel.localAddress().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                localAddressView.setText(s);
            }
        });

        //
        // Receives statistics (older data saved inside the database) for the current location
        //
        fragmentLocalDataViewModel.getStatisticsData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String result) {
                if (result.isEmpty())
                    localStatisticsView.setText(HtmlCompat.fromHtml("Keine daten vorhanden....", 0));
                else
                    localStatisticsView.setText(HtmlCompat.fromHtml(result, 0));
            }
        });
    }

    /**
     * Receives the result of the user dialog which asks to grant
     * the required permission(s).
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        // Location access granted?
        if (requestCode == REQUEST_PERMISSION_RESULT_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.v("PERMISSION__", " OK");
                startLocationUpdates();
            } else {
                // toDo Show appropriate message....
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (locationManager != null)
            locationManager.removeUpdates(this);
    }

    /**
     * Tells the Android system to start location updates.....
     */
    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //
            // Get current location
            //
            locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            provider = locationManager.getBestProvider(criteria, false);

            location = locationManager.getLastKnownLocation(provider);

            // Initialize the location fields
            if (location != null) {
                onLocationChanged(location);
            } else {
                Log.v("LOCLOC_UP", "Location==null");
            }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_LOCATION_EVERY_MS, UPDATE_WHEN_DEV_IS_MOVED_MORE_THAN_X_METERS, this);
            Log.v("LOCLOC_UP", "started");
        }
    }

    /**
     * Geocoder updates.
     * <p>
     * Position updates from the location manager are received here.
     */
    @Override
    public void onLocationChanged(Location location) {

        Log.v("LOCLOC", "Changed...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                String addressLine;

                double lat = 54, lng = 0;

                final String adminArea;
                final String city;

                try {
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(3);

                    if (location != null) {
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                    }
                    Geocoder geocoder;
                    geocoder = new Geocoder(getContext(), Locale.getDefault());

                    List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                    addressLine = addresses.get(0).getAddressLine(0);
                    city = addresses.get(0).getLocality();
                    adminArea = addresses.get(0).getAdminArea();

                    Log.v("ADDRESSES", "Size" + addresses.size() + "");


                    for (int n = 0; n <= addresses.size() - 1; n++)
                        Log.v("ADDRESSES", addresses.get(n).getAddressLine(n));

                    // Gets the covid data from the network connection
                    // ToDo: What if two matches are found? e.g. Karlsruhe (Stadtkreis) and Karlsruhe (landkreis) => BUG!!!
                    // First match is send as network request. How can I get the right infomration from the geocoder result?
                    fragmentLocalDataViewModel.getCovidData(city + " " + adminArea);

                    // This invokes the associated observer
                    fragmentLocalDataViewModel.localAddress().postValue(addressLine);

                } catch (Exception e) {
                    fragmentLocalDataViewModel.localAddress().postValue("Ups, an error occured where it should not..." + e.toString());
                }
            }
        }).start();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
