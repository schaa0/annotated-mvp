package de.hda.simple_example.business;

import android.content.SharedPreferences;

/**
 * Created by Andy on 28.12.2016.
 */

public class SettingsImpl implements Settings {

    private final SharedPreferences sharedPreferences;

    public SettingsImpl(SharedPreferences sharedPreferences){
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public void setLastQuery(String query) {
        sharedPreferences.edit().putString("lastQuery", query).apply();
    }

    @Override
    public String getLastQuery() {
        return sharedPreferences.getString("lastQuery", "");
    }
}
