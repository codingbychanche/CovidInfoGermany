package com.berthold.covidinfo.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.berthold.covidinfo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * View Model for the search fragment
 */
public class FragmentSearch extends Fragment implements AdapterCovidDataSearchResultList.CovidDataSearchResult {

    // UI
    private FragmentSearchViewModel fragmentSearchViewModel;
    private FragmentFavCovidDataViewModel fragmentFavCovidDataViewModel;

    private ProgressBar waitingForCovidDataLoadedFromNetwork;
    private SearchView searchView;
    private TextView waitingForCovidDataLoadedFromNetworkUpdateInfo;

    private RecyclerView covidDataRecyclerView;
    private RecyclerView.LayoutManager covidDataLayoutManager;
    private AdapterCovidDataSearchResultList adapterCovidDataSearchResultList;
    private List<CovidSearchResultData> covidDataList = new ArrayList<>();

    // API
    private String apiAddressStadtkreise = "https://public.opendatasoft.com/api/records/1.0/search/?dataset=covid-19-germany-landkreise&q={0}&facet=last_update&facet=name&facet=rs&facet=bez&facet=bl&rows=100";

    // Async task responsible for building a list of search suggestions....
    private AsyncTaskBuildCovidDataSearchSuggestionsList getCovidData = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflate
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get view model of the activity this fragment belongs to....
        fragmentSearchViewModel = ViewModelProviders.of(requireActivity()).get(FragmentSearchViewModel.class);
        fragmentFavCovidDataViewModel = ViewModelProviders.of(requireActivity()).get(FragmentFavCovidDataViewModel.class);

        covidDataRecyclerView = (RecyclerView) view.findViewById(R.id.covid_data);
        covidDataRecyclerView.setHasFixedSize(true);
        covidDataLayoutManager = new LinearLayoutManager(getActivity());
        covidDataRecyclerView.setLayoutManager(covidDataLayoutManager);

        adapterCovidDataSearchResultList = new AdapterCovidDataSearchResultList(covidDataList, getContext(), this);
        covidDataRecyclerView.setAdapter(adapterCovidDataSearchResultList);

        // UI
        waitingForCovidDataLoadedFromNetwork = view.findViewById(R.id.progress_waiting_for_data);
        waitingForCovidDataLoadedFromNetworkUpdateInfo = view.findViewById(R.id.search_update_info);
        searchView = view.findViewById(R.id.search);

        // UI
        //
        // The search view which enables the user to search covid data
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String sq) {

                if (!sq.isEmpty()) {
                    if (getCovidData != null)
                        getCovidData.cancel(true);
                    getCovidData = new AsyncTaskBuildCovidDataSearchSuggestionsList(apiAddressStadtkreise, sq, adapterCovidDataSearchResultList, fragmentSearchViewModel);
                    getCovidData.execute();
                } else {
                    if (getCovidData != null)
                        getCovidData.cancel(true);

                    // Async task is responsible for showing this progressbar.....
                    fragmentSearchViewModel.searchListIsUpdating().postValue(false);
                    fragmentSearchViewModel.getUpdateInfo().postValue("");
                }
                return false;
            }
        });

        //
        // Is invoked when the searchViews close button (the 'x') is pressed....
        //
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if (getCovidData != null)
                    getCovidData.cancel(true);
                fragmentSearchViewModel.searchListIsUpdating().postValue(false);
                return false;
            }
        });


        //
        //
        // Life data observers....
        //
        //
        /**
         * Show progress.
         */
        fragmentSearchViewModel.searchListIsUpdating().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isUpdating) {
                if (isUpdating)
                    waitingForCovidDataLoadedFromNetwork.setVisibility(View.VISIBLE);
                else {
                    waitingForCovidDataLoadedFromNetwork.setVisibility(View.GONE);
                    waitingForCovidDataLoadedFromNetworkUpdateInfo.setText(" ");
                }
            }
        });

        /**
         * Show update info...
         */
        fragmentSearchViewModel.getUpdateInfo().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                waitingForCovidDataLoadedFromNetworkUpdateInfo.setText(s);
            }
        });
    }

    /**
     * Invoked by {@link AdapterCovidDataSearchResultList} when an element inside the
     * list of search results was clicked.
     *
     * @param covidSearchResultData
     */
    @Override
    public void listItemClicked(CovidSearchResultData covidSearchResultData) {
        fragmentFavCovidDataViewModel.getFavLocationCovidData(covidSearchResultData.getName() + " " + covidSearchResultData.getBez() + " " + covidSearchResultData.getBundesland());
    }
}