package com.berthold.covidinfo.ui.home;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.berthold.covidinfo.R;


public class FragmentFavCovidDataView extends Fragment {

    // ViewModel
    static FragmentFavCovidDataViewModel fragmentFavCovidDataViewModel;
    static FragmentSearchViewModel fragmentSearchViewModel;

    // UI
    TextView townView, bezView, bundeslandView, casesPer10KView, lasUpdateView;
    ProgressBar updatingView;

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
        fragmentFavCovidDataViewModel = ViewModelProviders.of(requireActivity()).get(FragmentFavCovidDataViewModel.class);
        fragmentSearchViewModel = ViewModelProviders.of(requireActivity()).get(FragmentSearchViewModel.class);

        updatingView=view.findViewById(R.id.is_updating_progress);
        townView = view.findViewById(R.id.town);
        bezView = view.findViewById(R.id.bez);
        bundeslandView = view.findViewById(R.id.bundesland);
        casesPer10KView = view.findViewById(R.id.cases_per_10K);
        lasUpdateView = view.findViewById(R.id.last_update);

        /**
         * Receive update on fav location
         *
         * Invoked by {@link fragmentFavCovidDataViewModel}
         */
        fragmentFavCovidDataViewModel.updateFavLocation().observe(getViewLifecycleOwner(), new Observer<SearchResultData>() {
            @Override
            public void onChanged(SearchResultData r) {

                fragmentFavCovidDataViewModel.getFavLocationCovidData(r.getName());

                townView.setText(r.getName());
                bezView.setText(r.getBez());
                bundeslandView.setText(r.getBundesland());
                casesPer10KView.setText(r.getCasesPer10K() + "");
                lasUpdateView.setText(r.getLastUpdate());
            }
        });

        /**
         * Show progress.
         */
        fragmentFavCovidDataViewModel.getIsUpdating().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isUpdating) {
                if(isUpdating)
                    updatingView.setVisibility(View.VISIBLE);
                else
                    updatingView.setVisibility(View.GONE);
            }
        });
    }
}

