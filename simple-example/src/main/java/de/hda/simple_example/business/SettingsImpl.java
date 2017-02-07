package de.hda.simple_example.business;

import android.content.SharedPreferences;

public class SettingsImpl implements Settings {

    private static final String LAST_QUERY_KEY = "lastQuery";
    private final SharedPreferences sharedPreferences;

    public SettingsImpl(SharedPreferences sharedPreferences){
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public void setLastQuery(String query) {
        sharedPreferences.edit().putString(LAST_QUERY_KEY, query).apply();
    }

    @Override
    public String getLastQuery() {
        return sharedPreferences.getString(LAST_QUERY_KEY, "");
    }
}
