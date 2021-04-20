package com.berthold.covidinfo.ui.dashboard;

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

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private TextView covidDataView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Get view model of the activity this fragment belongs to....
        dashboardViewModel = ViewModelProviders.of(requireActivity()).get(DashboardViewModel.class);

        View root = inflater.inflate(R.layout.fragment_show_local_data, container, false);
       // covidDataView = root.findViewById(R.id.text_dashboard);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }
}

