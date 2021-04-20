package com.berthold.covidinfo.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

/**
 * This view model handles all search query's
 */
public class LocalViewViewModel extends ViewModel implements ThreadGetCovidData.getCovidDataInterface {

    public MutableLiveData<List<SearchResultData>> covidDataAsJson;

    /**
     * Covid data search results.
     *
     * @return A list of {@link SearchResultData} containing the search results.
     */
    public LiveData<List<SearchResultData>> updateCovidData() {
        if (covidDataAsJson == null)
            covidDataAsJson = new MutableLiveData<>();
        return covidDataAsJson;
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
}
