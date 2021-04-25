package com.berthold.covidinfo.ui.home;

public class SearchResultData {

    private String bundesland,name,bez;
    private double casesPer1K;
    private String lastUpdate;

    public SearchResultData(String bundesland, String name, String bez, double casesPer1K,String lastUpdate) {
        this.bundesland = bundesland;
        this.name = name;
        this.bez = bez;
        this.casesPer1K = casesPer1K;
        this.lastUpdate=lastUpdate;
    }

    public String getBundesland() {
        return bundesland;
    }

    public void setBundesland(String bundesland) {
        this.bundesland = bundesland;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBez() {
        return bez;
    }

    public void setBez(String bez) {
        this.bez = bez;
    }

    public double getCasesPer10K() {
        return casesPer1K;
    }

    public void setCasesPer10K(double casesPer10K) {
        this.casesPer1K = casesPer10K;
    }

    public void setLastUpdate(String lastUpdate){ this.lastUpdate=lastUpdate;}

    public String getLastUpdate (){return lastUpdate;}
}


