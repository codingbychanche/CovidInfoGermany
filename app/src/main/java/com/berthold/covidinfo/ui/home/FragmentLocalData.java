package com.berthold.covidinfo.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.berthold.covidinfo.R;

import java.util.List;
import java.util.Locale;


public class FragmentLocalData extends Fragment implements LocationListener {

    private LocationManager locationManager;
    private String provider;

    // ViewModel
    static FragmentLocalDataViewModel fragmentLocalDataViewModel;

    // UI
    TextView townView, bezView, bundeslandView, casesPer10KView, lasUpdateView, localAddressView;
    ProgressBar networkIsUpdating, locationIsUpdating;

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
        networkIsUpdating = view.findViewById(R.id.network_is_updating_progress);
        locationIsUpdating = view.findViewById(R.id.location_is_updating_progress);
        townView = view.findViewById(R.id.town);
        bezView = view.findViewById(R.id.bez);
        bundeslandView = view.findViewById(R.id.bundesland);
        casesPer10KView = view.findViewById(R.id.cases_per_10K);
        lasUpdateView = view.findViewById(R.id.last_update);
        localAddressView = view.findViewById(R.id.current_address);

        //
        // Get current location
        //
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);


        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            onLocationChanged(location);
        } else {
            // TODO show message.
        }

        fragmentLocalDataViewModel.getLocationIsUpdating().setValue(true);



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
        fragmentLocalDataViewModel.updateCovidData().observe(getViewLifecycleOwner(), new Observer<List<FragmentSearchResultData>>() {
            @Override
            public void onChanged(@Nullable List<FragmentSearchResultData> fragmentSearchResultData) {

                fragmentLocalDataViewModel.getLocationIsUpdating().setValue(false);

                if (fragmentSearchResultData != null) {
                    for (FragmentSearchResultData r : fragmentSearchResultData) {
                        townView.setText(r.getName());
                        bezView.setText(r.getBez());
                        bundeslandView.setText(r.getBundesland());
                        casesPer10KView.setText(r.getCasesPer10K() + "");
                        lasUpdateView.setText(r.getLastUpdate());
                    }
                }
            }
        });

        //
        // Receives status update when covid date is fetched from the network
        //
        fragmentLocalDataViewModel.unetworkIsUpdating().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isUpdating) {
                if (isUpdating)
                    networkIsUpdating.setVisibility(View.VISIBLE);
                else
                    networkIsUpdating.setVisibility(View.GONE);
            }
        });

        //
        // Receives status updates when the location manager is working.
        //
        fragmentLocalDataViewModel.getLocationIsUpdating().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isUpdating) {
                if (isUpdating)
                    locationIsUpdating.setVisibility(View.VISIBLE);
                else
                    locationIsUpdating.setVisibility(View.GONE);
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
    }

    @Override
    public void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    /*
     * Geocoder updates.
     */
    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        String addressLine, adminArea, city;

        Geocoder geocoder;
        geocoder = new Geocoder(getContext(), Locale.getDefault());

        try {
            fragmentLocalDataViewModel.getLocationIsUpdating().setValue(true);

            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            addressLine = addresses.get(0).getAddressLine(0);
            city = addresses.get(0).getLocality();
            adminArea = addresses.get(0).getAdminArea();
            Log.v("LOCLOC", adminArea + "   " + city);

            // Gets the covid data from the network connection
            fragmentLocalDataViewModel.getCovidData(city + " " + adminArea);

            // This invokes the associated observer
            fragmentLocalDataViewModel.localAddress().setValue(addressLine);

        } catch (Exception e) {

        }

        fragmentLocalDataViewModel.getLocationIsUpdating().setValue(false);
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
