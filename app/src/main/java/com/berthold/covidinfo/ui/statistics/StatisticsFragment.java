package com.berthold.covidinfo.ui.statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.berthold.covidinfo.R;

public class StatisticsFragment extends Fragment {

    private StatisticsViewModel statisticsViewModel;
    private TextView covidDataView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Get view model of the activity this fragment belongs to....
        statisticsViewModel = ViewModelProviders.of(requireActivity()).get(StatisticsViewModel.class);

        View root = inflater.inflate(R.layout.fragment_statistics, container, false);
       // covidDataView = root.findViewById(R.id.text_dashboard);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }
}

