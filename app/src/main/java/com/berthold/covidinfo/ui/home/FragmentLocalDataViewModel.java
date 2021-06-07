package com.berthold.covidinfo.ui.home;

import android.media.session.PlaybackState;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.berthold.covidinfo.CovidDataBase;
import com.berthold.covidinfo.MainActivity;

import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class FragmentLocalDataViewModel extends ViewModel implements ThreadSearchLocalCovidData.getCovidDataInterface {

    //
    // Convenience fields for easy access to the local data....
    //
    private String localStatistics;
    private CovidSearchResultData localLocationCovidData;

    //
    // Live data
    //
    // Covid data
    private MutableLiveData<List<CovidSearchResultData>> covidDataAsJson;
    public LiveData<List<CovidSearchResultData>> updateCovidData() {
        if (covidDataAsJson == null)
            covidDataAsJson = new MutableLiveData<>();
        return covidDataAsJson;
    }

    // Geo data
    private MutableLiveData<String> localAddress = new MutableLiveData<>();
    public MutableLiveData<String> localAddress() {
        return localAddress;
    }

    // Statistics
    private MutableLiveData<String> statisticsData =new MutableLiveData<>();
    public MutableLiveData<String> getStatisticsData(){
        return statisticsData;
    }

    /**
     * Get's the data via the api from the network.
     */
    public void getCovidData(String searchQuery) {

        String apiAddressStadtkreise = "https://public.opendatasoft.com/api/records/1.0/search/?dataset=covid-19-germany-landkreise&q={0}&facet=last_update&facet=name&facet=rs&facet=bez&facet=bl";

        // Starts the database connection, gets the data and invokes the
        // dedicated interface when completed.
        ThreadSearchLocalCovidData gd = ThreadSearchLocalCovidData.getInstance(this, apiAddressStadtkreise, searchQuery);
        if (gd != null) {
            gd.cancel();
            gd.getCovid();
        }
    }

    /**
     * Receives the data from the network connection and decides if the entry should be
     * added to the data base or just updated.
     *
     * The logic behind the decision to save or not to save an entry assumes the following:
     *
     * -    The date the covid dataset was updated=the current date. This is not always true because
     *      the covid database is updated daily at around 12h pm. So if one visits a location, say
     *      on the 5. of June before that time and the covid dataset was updated on the 4. of june
     *      then the entry of the visit will be made for the 4 of June....
     *      toDo: Thats a bug in the current version.....
     *
     * -    If an entry for the current date and location already exists it is assumed that
     *      this entry was made by the {@link FragmentFavCovidDataViewModel}. In that case
     *      this entry is updated, the beenHere flag is set to true.
     *
     * -    The  {@link FragmentFavCovidDataViewModel} on the other hand will only create an
     *      entry into the database, if the same entry not already exists. This would be the case
     *      if the fav. location selected is was or is visited at the same date.
     *
     * Database is searched and, if entries for this location where saved, information is shown....
     *
     * @param covidData
     */
    @Override
    public void receive(List<CovidSearchResultData> covidData) {

        Connection covidDataBase = MainActivity.covidDataBase;
        boolean beenHere = true;

        String name = covidData.get(0).getName();
        String bundesland = covidData.get(0).getBundesland();
        String bez = covidData.get(0).getBez();
        String date = covidData.get(0).getLastUpdate();
        float cases100K = (float)covidData.get(0).getCasesPer10K();

        // No name, then there is no dataset for the current location in the database on the server.
        if (name.isEmpty()){
            covidData.get(0).setName("Keine Date für diesen Ort gefunden.");
            covidDataAsJson.postValue(covidData);
            statisticsData.postValue("-");
        }else {
            // Publish result.
            covidDataAsJson.postValue(covidData);

            // Add to database
            if (CovidDataBase.covidDataForThisDateExists(name, bundesland, bez, date, covidDataBase)) {
                // Update existing entry...
                Log.v("DBMAKE", " Exists, update");
                CovidDataBase.updateExistingEntry(name, bundesland, bez, date, beenHere, covidDataBase);
            } else {
                // Create a new entry
                CovidDataBase.insert(name, bundesland, bez, cases100K, date, beenHere, covidDataBase);
                Log.v("DBMAKE", "Entry:" + name + " for:" + date + " inserted....");
            }

            // Init convenience fields
            localLocationCovidData = covidData.get(0);

            // Get and publish entries for this location
            String result = CovidDataEvaluate.getTrend(name, bundesland, bez, covidDataBase);
            statisticsData.postValue(result);
            localStatistics = result;
        }
    }

    /**
     * Returns an url that searches the network for covid info
     * regarding the users favourite location.
     *
     * @return
     */
    public String getURLForCurrentLocation() {

        String town = covidDataAsJson.getValue().get(0).getName();
        String url = (MessageFormat.format("https://de.search.yahoo.com/yhs/search?hspart=ddc&hsimp=yhs-linuxmint&type=__alt__ddc_linuxmint_com&p={0}+covid+regeln)", town));
        return url;
    }

    /**
     * Getters for convenience fields.
     */
    public String getLocalStatistics(){return localStatistics;}

    public CovidSearchResultData getlocalLocationCovidData(){return localLocationCovidData;}

}
