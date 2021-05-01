package com.berthold.covidinfo.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.berthold.covidinfo.MainActivity;
import com.berthold.covidinfo.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * View Model for the search fragment
 */
public class FragmentSearch extends Fragment implements CovidDataAdapter.CovidDataSearchResult {

    // UI
    private FragmentSearchViewModel fragmentSearchViewModel;
    private FragmentFavCovidDataViewModel fragmentFavCovidDataViewModel;

    private ProgressBar waitingForCovidDataView;
    private ImageButton doSearch, deleteInput;

    private RecyclerView covidDataRecyclerView;
    private RecyclerView.LayoutManager covidDataLayoutManager;
    private CovidDataAdapter covidDataAdapter;
    private List<CovidSearchResultData> covidDataList = new ArrayList<>();

    private AutoCompleteTextView searchQueryView;

    // A list of previously, successful search query's
    String[] searchSuggestions;

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

        covidDataAdapter = new CovidDataAdapter(covidDataList, getContext(), this);
        covidDataRecyclerView.setAdapter(covidDataAdapter);

        // UI
        waitingForCovidDataView = view.findViewById(R.id.progress_waiting_for_data);
        doSearch = view.findViewById(R.id.do_search);
        deleteInput = view.findViewById(R.id.clear_search);
        waitingForCovidDataView.setVisibility(View.GONE);

        // Auto complete text view representing the search history
        searchQueryView = (AutoCompleteTextView) view.findViewById(R.id.search_querry);


        // UI
        //
        // Get Covid data from network.
        doSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sq = searchQueryView.getText().toString();
                if (!sq.isEmpty()) {
                    fragmentSearchViewModel.lastSearchQueryEntered = sq;
                    waitingForCovidDataView.setVisibility(View.VISIBLE);
                    fragmentSearchViewModel.searchCovidData(sq);
                }
            }
        });

        deleteInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchQueryView.setText("");
                covidDataList.clear();
                covidDataAdapter.notifyDataSetChanged();
            }
        });

        /**
         * Receives result of network query.
         * Updates the search history.
         *
         * Invoked by {@link fragmentSearchViewModel.getCovidData()}
         */
        fragmentSearchViewModel.updateCovidData().observe(getViewLifecycleOwner(), new Observer<List<CovidSearchResultData>>() {
            @Override
            public void onChanged(@Nullable List<CovidSearchResultData> covidSearchResultData) {

                covidDataList.clear();

                if (covidSearchResultData != null) {
                    for (CovidSearchResultData r : covidSearchResultData) {
                        covidDataList.add(r);
                    }
                }
                waitingForCovidDataView.setVisibility(View.GONE);
                covidDataAdapter.notifyDataSetChanged();

                fragmentSearchViewModel.addToSearchHistory(covidSearchResultData);

            }
        });

        /**
         * Gets an update of the search history for this session.
         *
         * The search history contains all succesful search results.
         * It is kept inside this fragments view model and will be saved
         * to the shared preferences.
         */
        fragmentSearchViewModel.refreshSearchHistory().observe(getViewLifecycleOwner(), new Observer<Set<String>>() {
            @Override
            public void onChanged(Set<String>updatedSearchHistory) {

                searchSuggestions =updatedSearchHistory.toArray(new String [updatedSearchHistory.size()]);

                ArrayAdapter<String> searchHistoryAdapter =
                        new ArrayAdapter<String>(getActivity(), R.layout.simple_list_row, R.id.suggestion_text, searchSuggestions);
                searchQueryView.setAdapter(searchHistoryAdapter);
            }
        });
    }

    /**
     * Invoked by {@link CovidDataAdapter} when an element inside the
     * list of search results was clicked.
     *
     * @param covidSearchResultData
     */
    @Override
    public void listItemClicked(CovidSearchResultData covidSearchResultData) {
        fragmentFavCovidDataViewModel.getFavLocationCovidData(covidSearchResultData.getName() + " " + covidSearchResultData.getBez() + " " + covidSearchResultData.getBundesland());
    }
}