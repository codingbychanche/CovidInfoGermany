package com.berthold.covidinfo.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.berthold.covidinfo.MainActivity;

import java.util.List;

/**
 * This view model handles all search query's
 */
public class FragmentFavCovidDataViewModel extends ViewModel implements ThreadGetCovidData.getCovidDataInterface{

    public MutableLiveData<SearchResultData> favCovidDataLocation;
    public MutableLiveData<List<SearchResultData>> covidDataAsJson;

    public MutableLiveData<SearchResultData> updateFavLocation() {
        if (favCovidDataLocation==null){
          favCovidDataLocation=new MutableLiveData<>();
        }
        return favCovidDataLocation;
    }

    /**
     * Gets the data via the api from the network.
     */
    public void getCovidData(String searchQuery) {
        String apiAddressStadtkreise = "https://public.opendatasoft.com/api/records/1.0/search/?dataset=covid-19-germany-landkreise&q={0}&facet=last_update&facet=name&facet=rs&facet=bez&facet=bl";

        // Starts the database connection, gets the data and invokes the
        // dedicated interface when completed.
        ThreadGetCovidData gd = ThreadGetCovidData.getInstance(this, apiAddressStadtkreise, searchQuery);
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
    public void receive(List<SearchResultData> covidData) {
            covidDataAsJson.postValue(covidData);
    }

    /**
     * Saves all data needed for the next session to the shared preferences.
     * Invoked by the {@link MainActivity}'s life- cycle callback methods.
     *
     * @param mainActivity The context of the activity which invokes this method.
     */
    public void storeToSHaredPreffs(MainActivity mainActivity) {
        Log.v("SHAREDSHARED", "SAVE");
        SharedPreferences sp;
        sp = mainActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("favLocation",favCovidDataLocation.toString());
        editor.commit();
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
        String f =sp.getString("favLocation", null);
        Log.v("RESTOREDFAV",f+"-");
    }
}
