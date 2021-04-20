package com.berthold.covidinfo.ui.home;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.berthold.covidinfo.R;

import java.util.ArrayList;
import java.util.List;


public class FragmentFavCovidDataView extends Fragment {

    // ViewModel
    static FragmentFavCovidDataViewModel fragmentFavCovidDataView;
    static FragmentSearchViewModel fragmentSearchViewModel;

    // UI
    TextView townView,bezView,bundeslandView,casesPer10KView,lasUpdateView;

    public FragmentFavCovidDataView() {
        // Empty!
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_show_fav_data, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fragmentFavCovidDataView = ViewModelProviders.of(requireActivity()).get(FragmentFavCovidDataViewModel.class);
        fragmentSearchViewModel=ViewModelProviders.of(requireActivity()).get(FragmentSearchViewModel.class);

        townView =view.findViewById(R.id.town);
        bezView=view.findViewById(R.id.bez);
        bundeslandView=view.findViewById(R.id.bundesland);
        casesPer10KView=view.findViewById(R.id.cases_per_10K);
        lasUpdateView=view.findViewById(R.id.last_update);

        /**
         * Receive update on fav location
         *
         * Invoked by {@link fragmentFavCovidDataView}
         */
        fragmentFavCovidDataView.updateFavLocation().observe(getViewLifecycleOwner(), new Observer<SearchResultData>() {
            @Override
            public void onChanged(SearchResultData r) {
                townView.setText(r.getName());
                bezView.setText(r.getBez());
                bundeslandView.setText(r.getBundesland());
                casesPer10KView.setText(r.getCasesPer10K()+"");
                lasUpdateView.setText(r.getLastUpdate());
            }
        });
    }
}

