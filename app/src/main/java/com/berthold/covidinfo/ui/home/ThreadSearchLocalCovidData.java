package com.berthold.covidinfo.ui.home;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class ThreadSearchLocalCovidData {

    private static int numberOfThreads = 0;
    private static boolean isCancheled;

    private static ThreadSearchLocalCovidData getCovidData;
    private static String apiAddressStadtkreise, searchQuery;
    private static getCovidDataInterface gC;

    public ThreadSearchLocalCovidData() {
    }

    /**
     * Creates a new instance of this class if not another one already exists
     * or the passed search query parameter is not empty. If this
     * conditions do not apply, the {@link getCovidDataInterface} is invoked
     * right away, without getting any data from the network.
     *
     * @param g  {@link getCovidDataInterface}
     * @param sK Network address.
     * @param sQ The search query.
     * @return An instance of this class or null.
     */
    public static ThreadSearchLocalCovidData getInstance(getCovidDataInterface g, String sK, String sQ) {
        if (numberOfThreads == 0 && !sQ.isEmpty()) {
            isCancheled = false;
            gC = g;
            apiAddressStadtkreise = sK;
            searchQuery = sQ;
            numberOfThreads++;
            getCovidData = new ThreadSearchLocalCovidData();
            return getCovidData;
        }
        Log.v("CANCELCANCEL", "No Thread");
        //gC.receive(null);
        return null;
    }

    interface getCovidDataInterface {
        public void receive(List<CovidSearchResultData> result);
    }

    /**
     * Starts a new thread which try's to obtain covid data from the network.
     * <p>
     * API: https://public.opendatasoft.com/explore/dataset/covid-19-germany-landkreise/api/?q=landau&location=6,51.32924,10.45403&basemap=jawg.streets
     */
    public void getCovid() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                StringBuffer covidDataBuffer = new StringBuffer();



                // Get Covid data from the server...
                try {
                    URL _url = new URL(MessageFormat.format(apiAddressStadtkreise, searchQuery));
                    HttpURLConnection urlConnection = (HttpURLConnection) _url.openConnection();

                    InputStreamReader isr = new InputStreamReader(urlConnection.getInputStream());
                    BufferedReader br = new BufferedReader(isr);

                    String line;

                    while ((line = br.readLine()) != null) {
                        covidDataBuffer.append(line);
                        if (isCancheled) {
                            List<CovidSearchResultData> result = new ArrayList<>();
                            result = DecodeJsonResult.getResult(covidDataBuffer.toString());
                            gC.receive(result);
                            numberOfThreads = 0;
                            break;
                        }
                        Thread.sleep(5);
                    }
                    isr.close();
                } catch (Exception e) {
                    Log.v("ERRORERROR", e + "");
                }
            }
        }).start();
    }

    /**
     * Stop this thread.
     */
    public void cancel() {
        isCancheled = true;
        numberOfThreads = 0;
    }
}
