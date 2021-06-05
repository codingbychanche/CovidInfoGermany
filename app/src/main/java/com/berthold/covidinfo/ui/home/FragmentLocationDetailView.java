package com.berthold.covidinfo.ui.home;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
    static FragmentFavCovidDataViewModel fragmentFavCovidDataViewModel;

    // Fragments UI components
    TextView townView, infoTextView;
    Button okButtonView;
    ListView pastDataListView;

    public FragmentLocationDetailView() {
        // Constructor must be empty....
    }

    public static FragmentLocationDetailView newInstance(String showForFragment) {
        FragmentLocationDetailView frag = new FragmentLocationDetailView();
        Bundle args = new Bundle();
        args.putString("showForFragment", showForFragment);
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
        fragmentFavCovidDataViewModel=ViewModelProviders.of(requireActivity()).get(FragmentFavCovidDataViewModel.class);

        // These are this fragments UI components
        // Gets all objects (Buttons, EditText etc..) and set's them on
        // their listeners.....
        infoTextView = view.findViewById(R.id.fragment_location_detail_view_info_text);
        townView =view.findViewById(R.id.town);

        // This argument, passed when invoking this fragment, is a conditional
        // to decide to show detailed data either for local or for the fav location...
        String showForFragment = getArguments().getString("showForFragment");

        //
        // Show data for local- location
        //
        if(showForFragment.equals("local")) {
            String name = fragmentLocalDataViewModel.getLocalName();
            String state = fragmentLocalDataViewModel.getLocalState();
            String county = fragmentLocalDataViewModel.getLocalCounty();

            fragmentLocationDetailViewModel.getPastDataForThisLocation(name, state, county);
            townView.setText(fragmentLocalDataViewModel.getLocalName());
            infoTextView.setText(HtmlCompat.fromHtml(fragmentFavCovidDataViewModel.getLocalStatistics(),0));
        }

        //
        // Show data for fav location
        //
        if(showForFragment.equals("fav")){
            String name = fragmentFavCovidDataViewModel.getLocalName();
            String state = fragmentFavCovidDataViewModel.getLocalState();
            String county = fragmentFavCovidDataViewModel.getLocalCounty();

            fragmentLocationDetailViewModel.getPastDataForThisLocation(name, state, county);
            townView.setText(fragmentFavCovidDataViewModel.getLocalName());
            infoTextView.setText(HtmlCompat.fromHtml(fragmentFavCovidDataViewModel.getLocalStatistics(),0));
        }

        //
        // Finish this fragment
        //
        okButtonView = (Button) view.findViewById(R.id.close_location_info);
        okButtonView.setOnClickListener(new View.OnClickListener() {
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
        // Receive past data for this location
        //
        fragmentLocationDetailViewModel.getPastCovidData().observe(getViewLifecycleOwner(), new Observer<List<CovidSearchResultData>>() {
            @Override
            public void onChanged(@Nullable List<CovidSearchResultData> covidSearchResultData) {


                if (covidSearchResultData != null) {

                    String [] p=fragmentLocationDetailViewModel.buildPastDataList(covidSearchResultData);
                    pastDataListView = view.findViewById(R.id.past_data_list);
                    ArrayAdapter pastListViewAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, p);
                    pastDataListView.setAdapter(pastListViewAdapter);
                }
            }
        });
    }
}
