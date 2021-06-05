package com.berthold.covidinfo.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.berthold.covidinfo.CovidDataBase;
import com.berthold.covidinfo.MainActivity;

import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This view model handles all search query's
 */
public class FragmentFavCovidDataViewModel extends ViewModel implements FavCovidLocationData {
    //
    // Convenience fields for easy access to the local data....
    //
    private String localName,localState,localCounty;
    private String statistics;

    // progressbar
    private MutableLiveData<Boolean> isUpdating = new MutableLiveData<>();

    public MutableLiveData<Boolean> getIsUpdating() {
        return isUpdating;
    }

    //
    // Live data
    //
    // Covid data
    public MutableLiveData<CovidSearchResultData> favCovidDataLocation;
    public MutableLiveData<CovidSearchResultData> updateFavLocation() {
        if (favCovidDataLocation == null) {
            favCovidDataLocation = new MutableLiveData<>();
        }
        return favCovidDataLocation;
    }

    // Statistics
    private MutableLiveData<String> statisticsData = new MutableLiveData<>();
    public MutableLiveData<String> getStatisticsData() {
        return statisticsData;
    }

    /**
     * Gets the data via the api from the network.
     */
    public void getFavLocationCovidData(String searchQuery) {
        String apiAddressStadtkreise = "https://public.opendatasoft.com/api/records/1.0/search/?dataset=covid-19-germany-landkreise&q={0}&facet=last_update&facet=name&facet=rs&facet=bez&facet=bl";

        isUpdating.setValue(true);

        ThreadGetFavLocCovidData t = new ThreadGetFavLocCovidData().getInstance(apiAddressStadtkreise, searchQuery, this);
        t.get();
    }


    /**
     * Receives the covid data from the network
     * <p>
     * todo DRY! (see FragmentLocalViewModel)
     *
     * @param covidData
     */
    @Override
    public void getFavLocation(List<CovidSearchResultData> covidData) {
        isUpdating.postValue(false);
        if (covidData != null) {
            CovidSearchResultData r = covidData.get(0);
            favCovidDataLocation.postValue(r);
        }

        Connection covidDataBase = MainActivity.covidDataBase;
        boolean beenHere = false;

        String name = covidData.get(0).getName();
        String bundesland = covidData.get(0).getBundesland();
        String bez = covidData.get(0).getBez();
        String date = covidData.get(0).getLastUpdate();
        float cases100K = (float) covidData.get(0).getCasesPer10K();

        // Init convenience fields
        localName=name;
        localState=bundesland;
        localCounty=bez;

        // Data base entries are only created when date last updated does not exist
        // for any entry matching name, bundesland and bez....
        if (CovidDataBase.covidDataForThisDateExists(name, bundesland, bez, date, covidDataBase))
            Log.v("DBMAKE", " Exists");
        else {
            CovidDataBase.insert(name, bundesland, bez, cases100K, date, beenHere, covidDataBase);
            Log.v("DBMAKE", "Entry:" + name + " for:" + date + " inserted....");
        }

        // Get and publish entries for this location
        String result=CovidDataEvaluate.getTrend(name, bundesland, bez, covidDataBase);
        statisticsData.postValue(result);
        statistics=result; // Set convenience field
    }

    /**
     * Returns a url that searches the network for covid info
     * regarding the users favourite covid location.
     *
     * @return
     */
    public String getURLForFavLocation() {

        String town = favCovidDataLocation.getValue().getName();
        String url = new String(MessageFormat.format("https://de.search.yahoo.com/yhs/search?hspart=ddc&hsimp=yhs-linuxmint&type=__alt__ddc_linuxmint_com&p={0}+covid+regeln)", town));
        return url;
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

        if (favCovidDataLocation != null) {
            editor.putString("favLocation", favCovidDataLocation.getValue().getName() + " " + favCovidDataLocation.getValue().getBez() + " " + favCovidDataLocation.getValue().getBundesland());
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
        SharedPreferences sp;
        sp = mainActivity.getPreferences(Context.MODE_PRIVATE);
        String searchQuery = sp.getString("favLocation", null);
        getFavLocationCovidData(searchQuery);
    }

    /**
     * Getters for convenience fields.
     */
    public String getLocalName() {
        return localName;
    }

    public String getLocalState(){
        return localState;
    }

    public String getLocalCounty(){
        return localCounty;
    }

    public String getLocalStatistics(){return statistics;}
}
