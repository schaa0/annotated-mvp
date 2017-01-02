package de.hda.simple_example.business;

import android.content.SharedPreferences;
import android.util.Log;

import com.mvp.EventBus;
import com.mvp.annotation.Event;

import javax.inject.Inject;

import de.hda.simple_example.event.Contract;

/**
 * Created by Andy on 26.12.2016.
 */

public class CustomService {

    private EventBus eventBus;

    @Inject
    public CustomService(EventBus eventBus){
        this.eventBus = eventBus;
    }

    @Event
    public void onSearchRepositories(Contract.SearchRepositoriesEvent event){
        Log.e(CustomService.class.getName(), "Query: " + event.getQuery());
    }

    public void onCreate() {
        eventBus.register(this);
    }

    public void onDestroy() {
        eventBus.unregister(this);
    }
}