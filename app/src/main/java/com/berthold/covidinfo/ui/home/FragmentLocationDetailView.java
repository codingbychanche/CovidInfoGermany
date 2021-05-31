package com.berthold.covidinfo.ui.home;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.berthold.covidinfo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Opens an info dialog which shows some additional, detailed
 * information about either the current location or the
 * favourite location...
 */
public class FragmentLocationDetailView extends DialogFragment {

    // ViewModel
    static FragmentLocationDetailViewModel fragmentLocationDetailViewModel;
    static FragmentLocalDataViewModel fragmentLocalDataViewModel;

    // Fragments UI components
    TextView infoText;
    Button okButton;
    ListView pastDataListView;

    public FragmentLocationDetailView() {
        // Constructor must be empty....
    }

    public static FragmentLocationDetailView newInstance(String titel) {
        FragmentLocationDetailView frag = new FragmentLocationDetailView();
        Bundle args = new Bundle();
        args.putString("titel", titel);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    // Inflate fragment layout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location_detail_view, container);
    }

    // This fills the layout with data
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // The view model(s)
        fragmentLocationDetailViewModel = ViewModelProviders.of(requireActivity()).get(FragmentLocationDetailViewModel.class);
        fragmentLocalDataViewModel = ViewModelProviders.of(requireActivity()).get(FragmentLocalDataViewModel.class);

        // These are the fragments UI components
        // Gets all objects (Buttons, EditText etc..) and set's them on
        // their listeners.....
        infoText = view.findViewById(R.id.fragment_location_detail_view_info_text);

        // Data
        String[] pastDataList = {"eins", "zwei"};

        String name = fragmentLocalDataViewModel.getLocalName();
        String state = fragmentLocalDataViewModel.getLocalState();
        String county = fragmentLocalDataViewModel.getLocalCounty();
        fragmentLocationDetailViewModel.getPastDataForThisLocation(name, state, county);

        // Finish
        okButton = (Button) view.findViewById(R.id.close_location_info);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss();
            }
        });


        //
        //
        // Live data observers
        //
        //

        //
        // Receives statistics (older data saved inside the database) for the current location
        //
        fragmentLocalDataViewModel.getStatisticsData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String result) {
                infoText.setText(HtmlCompat.fromHtml(result, 0));
            }
        });

        //
        // Receive past data for this location
        //
        fragmentLocationDetailViewModel.getCovidForDetailView().observe(getViewLifecycleOwner(), new Observer<List<CovidSearchResultData>>() {
            @Override
            public void onChanged(@Nullable List<CovidSearchResultData> covidSearchResultData) {

                if (covidSearchResultData != null) {
                    for (CovidSearchResultData r : covidSearchResultData)

                        Log.v("LOCALLOCAL", r.getLastUpdate() + "," + r.getCasesPer10K());

                    pastDataListView = view.findViewById(R.id.past_data_list);
                    ArrayAdapter pastListViewAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, pastDataList);
                    pastDataListView.setAdapter(pastListViewAdapter);
                }
            }
        });
    }
}
