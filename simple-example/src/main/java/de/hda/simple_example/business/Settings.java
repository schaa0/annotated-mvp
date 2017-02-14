package de.hda.simple_example.business;

/**
 * Created by Andy on 28.12.2016.
 */

public interface Settings {
    void saveLastQuery(String query);
    String readLastQuery();
}
