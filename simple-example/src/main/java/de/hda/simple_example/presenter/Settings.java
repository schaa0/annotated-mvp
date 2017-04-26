package de.hda.simple_example.presenter;

import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Settings
{

    private final SharedPreferences sharedPreferences;

    @Inject
    public Settings(SharedPreferences sharedPreferences){
        this.sharedPreferences = sharedPreferences;
    }

    public static final String LAST_QUERY_KEY = "lastQuery";

    public void saveLastQuery(String query) {
        sharedPreferences.edit().putString(LAST_QUERY_KEY, query).apply();
    }

    public String readLastQuery() {
        return sharedPreferences.getString(LAST_QUERY_KEY, "");
    }
}
