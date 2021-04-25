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
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.berthold.covidinfo.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class FragmentLocalData extends Fragment implements LocationListener {

    private LocationManager locationManager;
    private String provider;

    // ViewModel
    static LocalViewViewModel localViewViewModel;

    // UI
    Handler handler;
    TextView townView, bezView, bundeslandView, casesPer10KView, lasUpdateView,localAddressView;
    ProgressBar updatingView;

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
        localViewViewModel = ViewModelProviders.of(requireActivity()).get(LocalViewViewModel.class);

        // UI
        updatingView = view.findViewById(R.id.is_updating_progress);
        townView = view.findViewById(R.id.town);
        bezView = view.findViewById(R.id.bez);
        bundeslandView = view.findViewById(R.id.bundesland);
        casesPer10KView = view.findViewById(R.id.cases_per_10K);
        lasUpdateView = view.findViewById(R.id.last_update);
        localAddressView=view.findViewById(R.id.current_address);

        // Get the location manager
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
        locationManager.requestLocationUpdates(provider, 400, 1, this);

        /*
         * Live data receivers.
         *
         * Receives the covid data from the network.
         */
        localViewViewModel.updateCovidData().observe(getViewLifecycleOwner(), new Observer<List<SearchResultData>>() {
            @Override
            public void onChanged(@Nullable List<SearchResultData> searchResultData) {

                if (searchResultData != null) {
                    for (SearchResultData r : searchResultData) {
                        townView.setText(r.getName());
                        bezView.setText(r.getBez());
                        bundeslandView.setText(r.getBundesland());
                        casesPer10KView.setText(r.getCasesPer10K() + "");
                        lasUpdateView.setText(r.getLastUpdate());
                    }
                }
            }
        });

        /*
         * Receives status updates.
         */
        localViewViewModel.updating().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isUpdating) {
                if (isUpdating)
                    updatingView.setVisibility(View.VISIBLE);
                else
                    updatingView.setVisibility(View.GONE);
            }
        });

        /*
         * Receives the full address of your location
         *
         * Is invoked whenever the localAddress- var inside the view model is
         * changed by using it's 'setValue' method.
         */
        localViewViewModel.localAddress().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Log.v("ADDRESS",s);
                localAddressView.setText(s);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /*
     * Geocoder updates.
     */
    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        String addressLine,adminArea,city;

        Geocoder geocoder;
        geocoder = new Geocoder(getContext(), Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            addressLine = addresses.get(0).getAddressLine(0);
            city=addresses.get(0).getLocality();
            adminArea=addresses.get(0).getAdminArea();
            Log.v("LOCLOC", adminArea+"   "+city);

            // Gets the covid data from the network connection
            localViewViewModel.getCovidData(city+" "+adminArea);

            // This invokes the associated observer
            localViewViewModel.localAddress().setValue(addressLine);

        } catch (Exception e) {

        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
