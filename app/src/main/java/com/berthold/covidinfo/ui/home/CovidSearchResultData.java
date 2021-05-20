package com.berthold.covidinfo.ui.home;

public class CovidSearchResultData {

    private String recordID, bundesland, name, bez;
    private double casesPer1K;
    private String lastUpdate;

    private boolean beenHere;
    private int casesPer1KColorCode;


    public CovidSearchResultData(String datasetID, String bundesland, String name, String bez, double casesPer1K, String lastUpdate) {
        this.recordID = datasetID;
        this.bundesland = bundesland;
        this.name = name;
        this.bez = bez;
        this.casesPer1K = casesPer1K;
        this.lastUpdate = lastUpdate;

        casesPer1KColorCode = CovidDataCasesColorCoder.getColor((int) casesPer1K);
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

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public int getCasesPer1KColorCode() {
        return casesPer1KColorCode;
    }

    public void setCasesPer1KColorCode(int casesPer1KColorCode) {
        this.casesPer1KColorCode = casesPer1KColorCode;
    }

    public String getRecordID() {
        return recordID;
    }

    public void setRecordID(String recordID) {
        this.recordID = recordID;
    }

    public boolean beenHere() {
        return beenHere;
    }

    public void setBeenHere(boolean beenHere) {
        this.beenHere = beenHere;
    }
}


