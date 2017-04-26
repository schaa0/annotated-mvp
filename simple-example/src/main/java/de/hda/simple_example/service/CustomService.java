package de.hda.simple_example.service;

import android.util.Log;

import com.mvp.EventBus;
import com.mvp.annotation.ActivityScope;
import com.mvp.annotation.Event;

import javax.inject.Inject;
import javax.inject.Singleton;

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

    public void register() {
        eventBus.register(this);
    }

    public void unregister() {
        eventBus.unregister(this);
    }
}
