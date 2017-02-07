package de.hda.simple_example.business;

import android.content.SharedPreferences;
import android.util.Log;

import com.mvp.EventBus;
import com.mvp.annotation.Event;

import javax.inject.Inject;

import de.hda.simple_example.event.Contract;

public class CustomService {

    private EventBus eventBus;

    @Inject
    public CustomService(EventBus eventBus){
        this.eventBus = eventBus;
    }

    @Event(thread = Event.BACKGROUND_THREAD)
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
