package com.berthold.covidinfo.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.berthold.covidinfo.MainActivity;
import com.berthold.covidinfo.R;

import java.util.List;


public class FragmentFavCovidDataView extends Fragment {

    // ViewModel
    static FragmentFavCovidDataViewModel fragmentFavCovidDataViewModel;
    static FragmentSearchViewModel fragmentSearchViewModel;


    // UI
    TextView townView, bezView, bundeslandView, casesPer10KView, lasUpdateView, localStatisticsView;
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

        // View models
        fragmentFavCovidDataViewModel = ViewModelProviders.of(requireActivity()).get(FragmentFavCovidDataViewModel.class);
        fragmentSearchViewModel = ViewModelProviders.of(requireActivity()).get(FragmentSearchViewModel.class);

        // UI
        updatingView = view.findViewById(R.id.fav_loction_is_updating);
        townView = view.findViewById(R.id.town);
        bezView = view.findViewById(R.id.bez);
        bundeslandView = view.findViewById(R.id.bundesland);
        casesPer10KView = view.findViewById(R.id.cases_per_10K);
        lasUpdateView = view.findViewById(R.id.last_update);
        localStatisticsView = view.findViewById(R.id.statistics);

        //
        // When clicked, open webbrowser and try to find local information.
        //
        townView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url;
                String town = townView.getText().toString();
                if (!town.isEmpty()) {
                    url = fragmentFavCovidDataViewModel.getURLForFavLocation();
                    Intent Getintent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(Getintent);
                }
            }
        });

        //
        // When clicked, show detail view for this location
        //
        casesPer10KView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = requireActivity().getSupportFragmentManager();
                FragmentLocationDetailView newChallenge = FragmentLocationDetailView.newInstance("fav");
                newChallenge.show(fm, "fragment_location_detail_view");
            }
        });


        /*
         * Live data receivers.
         *
         *
         *
         *
         *
         *
         *
         */
        /**
         * Receive update on fav location
         *
         * Invoked by {@link fragmentFavCovidDataViewModel}
         */
        fragmentFavCovidDataViewModel.updateFavLocation().observe(getViewLifecycleOwner(), new Observer<CovidSearchResultData>() {
            @Override
            public void onChanged(CovidSearchResultData r) {

                townView.setText(r.getName());
                bezView.setText(r.getBez());
                bundeslandView.setText(r.getBundesland());

                casesPer10KView.setText(r.getCasesPer10K() + "");
                casesPer10KView.setTextColor(getResources().getColor(CovidDataCasesColorCoder.getColor((int)r.getCasesPer10K())));
                lasUpdateView.setText(r.getLastUpdate());

                // ToDo, DRY!! See View models.....
                SharedPreferences sp= requireActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("favLocation", r.getName() + " " + r.getBez() + " " + r.getBundesland());
                editor.apply();
            }
        });

        //
        // Receives statistics (older data saved inside the database) for the current location
        //
        fragmentFavCovidDataViewModel.getStatisticsData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String result) {

                localStatisticsView.setText(HtmlCompat.fromHtml(result,0));
            }
        });


        /**
         * Show progress.
         */
        fragmentFavCovidDataViewModel.getIsUpdating().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isUpdating) {
                if (isUpdating)
                    updatingView.setVisibility(View.VISIBLE);
                else
                    updatingView.setVisibility(View.GONE);
            }
        });
    }
}

