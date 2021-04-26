package com.berthold.covidinfo.ui.home;

import android.os.Bundle;
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

import com.berthold.covidinfo.R;

import java.util.ArrayList;
import java.util.List;

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
    private List<FragmentSearchResultData> covidDataList = new ArrayList<>();

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
        fragmentFavCovidDataViewModel=ViewModelProviders.of(requireActivity()).get(FragmentFavCovidDataViewModel.class);

        covidDataRecyclerView = (RecyclerView) view.findViewById(R.id.covid_data);
        covidDataRecyclerView.setHasFixedSize(true);
        covidDataLayoutManager = new LinearLayoutManager(getActivity());
        covidDataRecyclerView.setLayoutManager(covidDataLayoutManager);

        covidDataAdapter = new CovidDataAdapter(covidDataList, getContext(),this);
        covidDataRecyclerView.setAdapter(covidDataAdapter);

        // UI
        waitingForCovidDataView = view.findViewById(R.id.progress_waiting_for_data);
        doSearch = view.findViewById(R.id.do_search);
        deleteInput = view.findViewById(R.id.clear_search);
        waitingForCovidDataView.setVisibility(View.GONE);

        // Auto complete text view representing the search history
        searchQueryView = (AutoCompleteTextView) view.findViewById(R.id.search_querry);

        // This init's the search history for the first time.
        //
        // All successful search query's are saved to the search history list.
        // This list is saved/ restored to/ from shared preferences when the app
        // is closed or started. See mainActivity...
        if (fragmentSearchViewModel.searchHistory != null) {
            searchSuggestions = fragmentSearchViewModel.searchHistory.toArray(new String[fragmentSearchViewModel.searchHistory.size()]);
            ArrayAdapter<String> searchHistoryAdapter =
                    new ArrayAdapter<String>(getActivity(), R.layout.simple_list_row, R.id.suggestion_text, searchSuggestions);
            searchQueryView.setAdapter(searchHistoryAdapter);
        }

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
         * Receive result of network query.
         * Updates the search history.
         *
         * Invoked vy {@link fragmentSearchViewModel.getCovidData()}
         */
        fragmentSearchViewModel.updateCovidData().observe(getViewLifecycleOwner(), new Observer<List<FragmentSearchResultData>>() {
            @Override
            public void onChanged(@Nullable List<FragmentSearchResultData> fragmentSearchResultData) {

                covidDataList.clear();

                if (fragmentSearchResultData != null) {
                    for (FragmentSearchResultData r : fragmentSearchResultData) {
                        covidDataList.add(r);
                    }
                }
                waitingForCovidDataView.setVisibility(View.GONE);
                covidDataAdapter.notifyDataSetChanged();
                searchSuggestions = fragmentSearchViewModel.updateSearchHistory(fragmentSearchResultData);

                ArrayAdapter<String> searchHistoryAdapter =
                        new ArrayAdapter<String>(getActivity(), R.layout.simple_list_row, R.id.suggestion_text, searchSuggestions);

                searchQueryView.setAdapter(searchHistoryAdapter);
                searchQueryView.setThreshold(2); // Autocomplete starts with the first character entered....
            }
        });
    }

    @Override
    public void listItemClicked(FragmentSearchResultData fragmentSearchResultData){
        fragmentFavCovidDataViewModel.getFavLocationCovidData(fragmentSearchResultData.getName());
    }
}