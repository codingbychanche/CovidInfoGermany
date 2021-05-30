package com.berthold.covidinfo.ui.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.berthold.covidinfo.CovidDataBase;
import com.berthold.covidinfo.MainActivity;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class FragmentLocationDetailViewModel extends ViewModel {

    //
    // Live data
    //
    // Covid data
    private MutableLiveData<List<CovidSearchResultData>> covidDataAsJson;
    public LiveData<List<CovidSearchResultData>> getCovidForDetailView() {
        if (covidDataAsJson == null)
            covidDataAsJson = new MutableLiveData<>();
        return covidDataAsJson;
    }

    public void getPastDataForThisLocation(String name,String state,String county){
        Connection covidDataBase = MainActivity.covidDataBase;

        List <CovidSearchResultData> d=new ArrayList<>();
        d=CovidDataBase.getEntry(name,state,county,covidDataBase);
        Log.v("LOCALLOCAL",d.get(0).getName());
        covidDataAsJson.postValue(d);
    }
}
