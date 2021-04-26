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
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.Locale;

/**
 * This view model handles all search query's
 */
public class FragmentLocalDataViewModel extends ViewModel implements ThreadSearchCovidData.getCovidDataInterface{

    //
    // Live data
    //
    // Progressbar showing updates for the network connection when fetching covid data
    private MutableLiveData<Boolean> networkIsUpdating =new MutableLiveData<>();
    public MutableLiveData<Boolean> unetworkIsUpdating(){
        return networkIsUpdating;
    }

    // Progressbar showing location updates
    private  MutableLiveData<Boolean> locationIsUpdating=new MutableLiveData<>();
    public MutableLiveData<Boolean> getLocationIsUpdating(){ return locationIsUpdating;}

    // Covid data
    private MutableLiveData<List<FragmentSearchResultData>> covidDataAsJson;
    public LiveData<List<FragmentSearchResultData>> updateCovidData() {
        if (covidDataAsJson == null)
            covidDataAsJson = new MutableLiveData<>();
        return covidDataAsJson;
    }

    // Geo data
    private MutableLiveData<String> localAddress=new MutableLiveData<>();
    public MutableLiveData<String> localAddress(){
        return localAddress;
    }

    /**
     * Gets the data via the api from the network.
     */
    public void getCovidData(String searchQuery) {

        networkIsUpdating.postValue(true);

        String apiAddressStadtkreise = "https://public.opendatasoft.com/api/records/1.0/search/?dataset=covid-19-germany-landkreise&q={0}&facet=last_update&facet=name&facet=rs&facet=bez&facet=bl";

        // Starts the database connection, gets the data and invokes the
        // dedicated interface when completed.
        ThreadSearchCovidData gd = ThreadSearchCovidData.getInstance(this, apiAddressStadtkreise, searchQuery);
        if (gd != null) {
            gd.cancel();
            gd.getCovid();
        }
    }

    /**
     * Receives the data from the network connection.
     *
     * @param covidData
     */
    @Override
    public void receive(List<FragmentSearchResultData> covidData) {
        networkIsUpdating.postValue(false);
        covidDataAsJson.postValue(covidData);
    }
}
