package com.berthold.covidinfo.ui.home;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


interface FavCovidLocationData {
    void getFavLocation(List<SearchResultData> result);
}

public class ThreadGetFavLocCovidData {

    ThreadGetFavLocCovidData threadGetFavLocCovidData;

    private static FavCovidLocationData favCovidLocationData;
    private static String apiAddressStadtkreise, favLocName;

    public ThreadGetFavLocCovidData getInstance(String apiAddressStadtkreise, String favLocName, FavCovidLocationData fL) {

        this.favCovidLocationData = fL;
        this.favLocName = favLocName;
        this.apiAddressStadtkreise = apiAddressStadtkreise;
        threadGetFavLocCovidData = new ThreadGetFavLocCovidData();

        return threadGetFavLocCovidData;

    }

    public void get() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                StringBuffer covidDataBuffer = new StringBuffer();

                // Get Covid data from the server...
                try {
                    URL _url = new URL(MessageFormat.format(apiAddressStadtkreise, favLocName));
                    HttpURLConnection urlConnection = (HttpURLConnection) _url.openConnection();

                    InputStreamReader isr = new InputStreamReader(urlConnection.getInputStream());
                    BufferedReader br = new BufferedReader(isr);

                    String line;

                    List<SearchResultData> result = new ArrayList<>();

                    line = br.readLine();
                    covidDataBuffer.append(line);

                    result = DecodeJsonResult.getResult(covidDataBuffer.toString());
                    favCovidLocationData.getFavLocation(result);

                    Thread.sleep(50);

                    isr.close();

                } catch (Exception e) {
                    Log.v("ERRORERROR----", e + "");
                }
            }
        }).start();
    }
}
