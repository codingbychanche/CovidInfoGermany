package com.berthold.covidinfo.ui.home;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.berthold.covidinfo.R;

import java.util.List;


public class FragmentLocalData extends Fragment  {

    // Interface


    // ViewModel
    static LocalViewViewModel localViewViewModel;

    // UI
    TextView townView,bezView,bundeslandView,casesPer10KView,lasUpdateView;

    public FragmentLocalData() {
        // Empty!
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_show_local_data, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        localViewViewModel= ViewModelProviders.of(requireActivity()).get(LocalViewViewModel.class);
        localViewViewModel.getCovidData("Landau Stadt");
        townView =view.findViewById(R.id.town);
        bezView=view.findViewById(R.id.bez);
        bundeslandView=view.findViewById(R.id.bundesland);
        casesPer10KView=view.findViewById(R.id.cases_per_10K);
        lasUpdateView=view.findViewById(R.id.last_update);

        //
        // Live data observers
        //
        // Receives the search result.
        //

        localViewViewModel.updateCovidData().observe(getViewLifecycleOwner(), new Observer<List<SearchResultData>>() {
            @Override
            public void onChanged(@Nullable List<SearchResultData> searchResultData) {

                if (searchResultData != null) {
                    for (SearchResultData r : searchResultData) {
                        townView.setText(r.getName());
                        bezView.setText(r.getBez());
                        bundeslandView.setText(r.getBundesland());
                        casesPer10KView.setText(r.getCasesPer10K()+"");
                        lasUpdateView.setText(r.getLastUpdate());
                    }

                }
            }
        });
    }
}
