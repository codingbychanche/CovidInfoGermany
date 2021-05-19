package com.berthold.covidinfo.ui.home;

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
    MainActivity m;
    //
    // Live data
    //
    // Progressbar showing updates for the network connection when fetching covid data
    private MutableLiveData<Boolean> networkIsUpdating = new MutableLiveData<>();
    public MutableLiveData<Boolean> networkIsUpdating() {
        return networkIsUpdating;
    }

    // Progressbar showing location updates
    private MutableLiveData<Boolean> locationIsUpdating = new MutableLiveData<>();
    public MutableLiveData<Boolean> getLocationIsUpdating() {
        return locationIsUpdating;
    }

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
    private MutableLiveData<List<CovidSearchResultData>> statisticsData =new MutableLiveData<>();
    public MutableLiveData<List<CovidSearchResultData>> getStatisticsData(){
        return statisticsData;
    }

    /**
     * Get's the data via the api from the network.
     */
    public void getCovidData(String searchQuery) {

        networkIsUpdating.postValue(true);

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
     * Receives the data from the network connection, checks the data was already saved inside the
     * database and if not, saves it there.
     *
     * Database is searched and, if entries for this location where saved, information is shown....
     *
     * @param covidData
     */
    @Override
    public void receive(List<CovidSearchResultData> covidData) {
        networkIsUpdating.postValue(false);
        covidDataAsJson.postValue(covidData);

        Connection covidDataBase = MainActivity.covidDataBase;
        boolean beenHere = true;

        String name = covidData.get(0).getName();
        String bundesland = covidData.get(0).getBundesland();
        String bez = covidData.get(0).getBez();
        String date = covidData.get(0).getLastUpdate();
        float cases100K = (float)covidData.get(0).getCasesPer10K();

        // Data base entries are only created when the the either not already exist or
        // date last updated does not exist for any entry matching name, bundesland and bez....
        if (CovidDataBase.covidDataForThisDateExists(name, bundesland, bez, date, covidDataBase))
            Log.v("DBMAKE", " Exists");
        else {
            CovidDataBase.insert(name,bundesland,bez,cases100K,date,beenHere,covidDataBase);
            Log.v("DBMAKE","Entry:"+name+" for:"+date+" inserted....");
        }

        // Get and publish entries for this location
        List<CovidSearchResultData> statistcs=new ArrayList<>();
        statistcs=CovidDataBase.getEntry(name,bundesland,bez,covidDataBase);
        statisticsData.postValue(statistcs);
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
}
