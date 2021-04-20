package com.berthold.covidinfo.ui.home;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.berthold.covidinfo.R;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CovidDataAdapter extends RecyclerView.Adapter<CovidDataAdapter.ViewHolder> {

    private List<SearchResultData> searchResultDataList = new ArrayList<>();
    private Context context;
    private Resources resources;
    private CovidDataSearchResult clicked;


    public CovidDataAdapter(List<SearchResultData> searchResultDataList, Context context, CovidDataSearchResult clicked) {
        this.searchResultDataList = searchResultDataList;
        this.context = context;
        this.clicked=clicked;

        resources = context.getResources();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mView;

        public ViewHolder(View v) {
            super(v);
            mView = v;
        }
    }

    interface CovidDataSearchResult {
        void listItemClicked(SearchResultData resultData);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CovidDataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.covid_data_row_view, parent, false);
        CovidDataAdapter.ViewHolder vh = new CovidDataAdapter.ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicked.listItemClicked(searchResultDataList.get(position));
            }
        });

        TextView town = holder.mView.findViewById(R.id.town);
        TextView bez = holder.mView.findViewById(R.id.bez);
        TextView bundesland = holder.mView.findViewById(R.id.bundesland);
        TextView updated = holder.mView.findViewById(R.id.last_update);
        TextView casesPer10K = holder.mView.findViewById(R.id.cases_per_10K);

        town.setText(searchResultDataList.get(position).getName());
        bez.setText(searchResultDataList.get(position).getBez());
        bundesland.setText(searchResultDataList.get(position).getBundesland());
        updated.setText(searchResultDataList.get(position).getLastUpdate());
        casesPer10K.setText(searchResultDataList.get(position).getCasesPer10K() + "");
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (searchResultDataList != null)
            return searchResultDataList.size();
        return 0;
    }
}
