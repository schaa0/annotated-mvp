package de.hda.simple_example.business;

import android.content.SharedPreferences;

public class Settings
{

    private static final String LAST_QUERY_KEY = "lastQuery";
    private final SharedPreferences sharedPreferences;

    public Settings(SharedPreferences sharedPreferences){
        this.sharedPreferences = sharedPreferences;
    }

    public void saveLastQuery(String query) {
        sharedPreferences.edit().putString(LAST_QUERY_KEY, query).apply();
    }

    public String readLastQuery() {
        return sharedPreferences.getString(LAST_QUERY_KEY, "");
    }
}
