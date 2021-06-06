package com.berthold.covidinfo.ui.home;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.berthold.covidinfo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the searc results
 *
 */
public class AdapterCovidDataSearchResultList extends RecyclerView.Adapter<AdapterCovidDataSearchResultList.ViewHolder> {

    private List<CovidSearchResultData> covidSearchResultDataList = new ArrayList<>();
    private Context context;
    private Resources resources;
    private CovidDataSearchResult clicked;


    public AdapterCovidDataSearchResultList(List<CovidSearchResultData> covidSearchResultDataList, Context context, CovidDataSearchResult clicked) {
        this.covidSearchResultDataList = covidSearchResultDataList;
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
        void listItemClicked(CovidSearchResultData resultData);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AdapterCovidDataSearchResultList.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.covid_data_row_view, parent, false);
        AdapterCovidDataSearchResultList.ViewHolder vh = new AdapterCovidDataSearchResultList.ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicked.listItemClicked(covidSearchResultDataList.get(position));
            }
        });

        TextView town = holder.mView.findViewById(R.id.town);
        TextView bez = holder.mView.findViewById(R.id.bez);
        TextView bundesland = holder.mView.findViewById(R.id.bundesland);
        TextView updated = holder.mView.findViewById(R.id.last_update);
        TextView casesPer10K = holder.mView.findViewById(R.id.cases_per_10K);

        town.setText(covidSearchResultDataList.get(position).getName());
        bez.setText(covidSearchResultDataList.get(position).getBez());
        bundesland.setText(covidSearchResultDataList.get(position).getBundesland());
        updated.setText(covidSearchResultDataList.get(position).getLastUpdate());
        casesPer10K.setText(covidSearchResultDataList.get(position).getCasesPer10K() + "");
        casesPer10K.setTextColor(resources.getColor(covidSearchResultDataList.get(position).getCasesPer1KColorCode()));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (covidSearchResultDataList != null)
            return covidSearchResultDataList.size();
        return 0;
    }

    public void add(CovidSearchResultData d){
        covidSearchResultDataList.add(0,d);
    }

    public void clear(){covidSearchResultDataList.clear();}
}
