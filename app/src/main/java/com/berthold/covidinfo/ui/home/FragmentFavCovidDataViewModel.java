package com.berthold.covidinfo.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.berthold.covidinfo.MainActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This view model handles all search query's
 */
public class FragmentFavCovidDataViewModel extends ViewModel implements FavCovidLocationData {

    ThreadGetFavLocCovidData t;
    // progressbar
    private MutableLiveData<Boolean> isUpdating = new MutableLiveData<>();
    public MutableLiveData<Boolean> getIsUpdating() {
        return isUpdating;
    }

    // Covid data
    public MutableLiveData<SearchResultData> favCovidDataLocation;

    public MutableLiveData<SearchResultData> updateFavLocation() {
        if (favCovidDataLocation == null) {
            favCovidDataLocation = new MutableLiveData<>();
        }
        return favCovidDataLocation;
    }

    /**
     * Gets the data via the api from the network.
     */
    public void getFavLocationCovidData(String searchQuery) {
        String apiAddressStadtkreise = "https://public.opendatasoft.com/api/records/1.0/search/?dataset=covid-19-germany-landkreise&q={0}&facet=last_update&facet=name&facet=rs&facet=bez&facet=bl";

       //isUpdating.postValue(true);

        ThreadGetFavLocCovidData t = new ThreadGetFavLocCovidData().getInstance(apiAddressStadtkreise, searchQuery, this);
        t.get();
    }


    /**
     * Receives the covid data from the network
     *
     * @param covidData
     */
    @Override
    public void getFavLocation(List<SearchResultData> covidData) {
        Log.v("THREADTHREAD", "Got it");
        isUpdating.postValue(false);
        if (covidData != null) {
            SearchResultData r = covidData.get(0);
            favCovidDataLocation.postValue(r);
        }
    }

    /**
     * Saves all data needed for the next session to the shared preferences.
     * Invoked by the {@link MainActivity}'s life- cycle callback methods.
     *
     * @param mainActivity The context of the activity which invokes this method.
     */
    public void storeToSHaredPreffs(MainActivity mainActivity) {
        SharedPreferences sp;
        sp = mainActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (favCovidDataLocation!= null) {
            editor.putString("favLocation", favCovidDataLocation.getValue().getName());
            editor.apply();
        }
    }

    /**
     * Get all data saved in a previous session from the shared preferences.
     * Invoked by the {@link MainActivity}'s life- cycle callback methods.
     *
     * @param mainActivity Context.
     */
    public void restoreFromSharedPreferences(MainActivity mainActivity) {
        Log.v("SHAREDSHARED", "GET");
        SharedPreferences sp;
        sp = mainActivity.getPreferences(Context.MODE_PRIVATE);
        String searchQuery = sp.getString("favLocation", null);
        getFavLocationCovidData(searchQuery);
    }
}
