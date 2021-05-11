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
public class FragmentSearchViewModel extends ViewModel {

    // progressbar
    private MutableLiveData<Boolean> isUpdating = new MutableLiveData<>();
    public MutableLiveData<Boolean> searchListIsUpdating() {
        return isUpdating;
    }

    // Update info below the progressbar
    private MutableLiveData<String> updateInfo=new MutableLiveData<>();
    public MutableLiveData<String> getUpdateInfo(){
        return updateInfo;
    }

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
    public MutableLiveData<Set<String>> searchHistory;

    public LiveData<Set<String>> refreshSearchHistory() {
        if (searchHistory == null) {
            searchHistory = new MutableLiveData<>();
            return searchHistory;
        }
        return searchHistory;
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
        searchHistory=new MutableLiveData<Set<String>>();
        searchHistory.setValue(sp.getStringSet("history", searchHistory.getValue()));
    }
}
