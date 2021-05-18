package com.berthold.covidinfo.ui.home;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.lifecycle.ViewModel;

import com.berthold.covidinfo.ui.home.CovidDataAdapter;
import com.berthold.covidinfo.ui.home.CovidSearchResultData;
import com.berthold.covidinfo.ui.home.FragmentSearchViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javaUtils.FormatTimeStamp;

import static com.berthold.covidinfo.ui.home.DecodeJsonResult.getHits;
import static com.berthold.covidinfo.ui.home.DecodeJsonResult.getRecordNr;

/**
 * Searches the network for covid data and builds a list with the suggested results.......
 */
// First parameter is what you pass when invoking the execute- command from inside
// the class using this task. It contains as many parameters of the type specified here.
//
// Second paramter is what you pass when invoking the publishProgress() method.
//
// Third paramter is what is passed to the onPostExecute() method when doInBackground() has
// finished. Therefore the return Type of the doInBackground()- method must be of the same type!
public class AsyncTaskBuildCovidDataSearchSuggestionsList extends AsyncTask<String, CovidSearchResultData, CovidSearchResultData> {

    private String url, searchQuery;
    private CovidDataAdapter covidDataAdapter;
    private FragmentSearchViewModel waitingForCovidDataLoadedFromNetowrk;

    public AsyncTaskBuildCovidDataSearchSuggestionsList(String url, String searchQuery, CovidDataAdapter covidDataAdapter, FragmentSearchViewModel waitingForCovidDataLoadedFromNetwork) {
        this.url = url;
        this.searchQuery = searchQuery;
        this.covidDataAdapter = covidDataAdapter;
        this.waitingForCovidDataLoadedFromNetowrk = waitingForCovidDataLoadedFromNetwork;
    }

    @Override
    protected void onPreExecute() {
        // Setup progress bars etc......
        // waitingForCovidDataLoadedFromNetowrk.setVisibility(View.VISIBLE);
        waitingForCovidDataLoadedFromNetowrk.searchListIsUpdating().postValue(true);
        covidDataAdapter.clear();
        covidDataAdapter.notifyDataSetChanged();
    }

    /**
     * Does all the work in the background
     * Rule! => Never change view elements of the UI- thread from here! Do it in 'onPublish'!
     */
    @Override
    // Return type is the third generic data type specified when extending, the
    // result of the long running task which is executed in background.
    protected CovidSearchResultData doInBackground(String... params) {
        String jsonResultOfCovidData = null;
        CovidSearchResultData decodedResult = new CovidSearchResultData("-", "-", "-", 0, "");

        waitingForCovidDataLoadedFromNetowrk.getUpdateInfo().postValue ("Network......");

        // Get Covid data from the server...

        try {

            // Reads the data from the network connection
            // This is the fast part!
            URL _url = new URL(MessageFormat.format(url, searchQuery));
            HttpURLConnection urlConnection = (HttpURLConnection) _url.openConnection();

            InputStreamReader isr = new InputStreamReader(urlConnection.getInputStream());
            BufferedReader br = new BufferedReader(isr);

            jsonResultOfCovidData = br.readLine();

            isr.close();


        } catch (Exception e) {
            Log.v("ASYNC_ERRORERROR", e + "");
        }

        //
        //
        // Decode the obtained data
        // This is the time consuming part....
        //
        //
        List<CovidSearchResultData> result = new ArrayList<>();

        try {

            JSONObject json = new JSONObject(jsonResultOfCovidData);
            int hits = json.getInt("nhits");


            for (int n = 0; n <= hits - 1; n++) {

                // This is important!
                // If you miss to do this here, the class which created
                // this object has no way to end the async task started!
                // => This means, no matter how often you call task.cancel(true)
                // the async task will not stop! You have to take
                // care here to react and run the code that cancels!
                if (isCancelled())
                    break;


                json = new JSONObject(getRecordNr(jsonResultOfCovidData, n));
                String bl = json.getString("bl");
                String name = json.getString("name");
                String bez = json.getString("bez");
                int casesPer100K = json.getInt("cases7_per_100k");

                String lastUpdate = json.getString("last_update");
                String lastUpdateFormated = FormatTimeStamp.german(lastUpdate, true);

                CovidSearchResultData d = new CovidSearchResultData(bl, name, bez, casesPer100K, lastUpdateFormated);
                result.add(d);

                waitingForCovidDataLoadedFromNetowrk.getUpdateInfo().postValue ("Rufe ab:"+n+" / "+hits);


                publishProgress(d);


            }


            // Network search is done.....
            waitingForCovidDataLoadedFromNetowrk.searchListIsUpdating().postValue(false);

        } catch (JSONException e) {
            Log.v("JSONJSON", e.toString());
        }

        //
        // This is passed to onPostExecute()
        //
        return decodedResult;
    }

    /**
     * Update UI- thread
     * <p>
     * This runs on the UI thread. Not handler's and 'post'
     * needed here
     */
    @Override
    // This receives the 2. paramter
    protected void onProgressUpdate(CovidSearchResultData... e) {
        super.onProgressUpdate();

        // When we reach this, data has already been optained from the network,
        // so we hide the progressbar and show the update status from now
        // on..
        // toDo: Display a dedicated progressbar showing percentage of data decoded...
        //waitingForCovidDataLoadedFromNetowrk.setVisibility(View.GONE);

        // Update result
        Log.v("ASYNCASYNC", "UPDATE" + e[0].getName() + " " + e[0].getLastUpdate());
        covidDataAdapter.add(e[0]);
        covidDataAdapter.notifyDataSetChanged();
    }

    /**
     * Invoked when the doInBackground()- method has finished.
     *
     * @param result
     */
    // This receives the 3. paramter
    @Override
    protected void onPostExecute(CovidSearchResultData result) {
        if (result != null)
            // waitingForCovidDataLoadedFromNetowrk.setVisibility(View.GONE);
            waitingForCovidDataLoadedFromNetowrk.searchListIsUpdating().postValue(false);

        waitingForCovidDataLoadedFromNetowrk.getUpdateInfo().postValue ("");
    }
}
