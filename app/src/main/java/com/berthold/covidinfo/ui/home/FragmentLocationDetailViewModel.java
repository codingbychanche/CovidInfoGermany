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
    public LiveData<List<CovidSearchResultData>> getPastCovidData() {
        if (covidDataAsJson == null)
            covidDataAsJson = new MutableLiveData<>();
        return covidDataAsJson;
    }

    public void getPastDataForThisLocation(String name,String state,String county){
        Connection covidDataBase = MainActivity.covidDataBase;

        List <CovidSearchResultData> d=new ArrayList<>();
        d=CovidDataBase.getEntry(name,state,county,covidDataBase);

        if (covidDataAsJson!=null) {
            covidDataAsJson.postValue(d);
        }
    }

    public String[] buildPastDataList(List<CovidSearchResultData> c){
        List <String> pastDataList=new ArrayList<>();

        for (CovidSearchResultData r :c) {
            String beenHere;
            if (r.beenHere())
                beenHere="Besucht";
            else
                beenHere="-";

            pastDataList.add(r.getLastUpdate() + "  " + r.getCasesPer10K()+"  "+beenHere);
        }
        String [] p=pastDataList.toArray(new String[0]);
        return p;
    }
}
