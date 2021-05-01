package com.berthold.covidinfo.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.berthold.covidinfo.MainActivity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This view model handles all search query's
 */
public class FragmentSearchViewModel extends ViewModel implements ThreadSearchCovidData.getCovidDataInterface {

    public String lastSearchQueryEntered;

    /**
     * Covid data search results.
     *
     * @return A list of {@link CovidSearchResultData} containing the search results.
     */
    public MutableLiveData<List<CovidSearchResultData>> covidDataAsJson;

    public LiveData<List<CovidSearchResultData>> updateCovidData() {
        if (covidDataAsJson == null)
            covidDataAsJson = new MutableLiveData<>();
        return covidDataAsJson;
    }

    /**
     * The search history
     */
    private Set<String> s=new HashSet();
    public MutableLiveData<Set<String>> searchHistory;
    public LiveData<Set<String>> refreshSearchHistory() {
        if (searchHistory == null) {
            searchHistory = new MutableLiveData<>();
            searchHistory.setValue(s);
            return searchHistory;
        }
        return searchHistory;
    }

    /**
     * Gets the data via the api from the network.
     */
    public void searchCovidData(String searchQuery) {
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
     * Receives the data from the network connection and
     * updates the UI via the observer....
     *
     * @param covidData
     */
    @Override
    public void receive(List<CovidSearchResultData> covidData) {
        covidDataAsJson.postValue(covidData);
    }

    /**
     * This updates a list of previous successful search query's.
     * Invoked each time a successful search took place.
     *
     * @param covidSearchResultData Search result.
     * @return Updated search history.
     */
    public void addToSearchHistory(List<CovidSearchResultData> covidSearchResultData) {

        if (covidSearchResultData.size() > 0) {

            for (CovidSearchResultData r : covidSearchResultData) {
                s.add(r.getName() + ", " + r.getBez() + ", " + r.getBundesland());
            }
            searchHistory.postValue(s);
        }
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

        Set<String> s = searchHistory.getValue();

        editor.putStringSet("history", s);
        editor.commit();
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

        s = sp.getStringSet("history", null);
        Log.v("LOGLOG",s.toString());

    }
}
