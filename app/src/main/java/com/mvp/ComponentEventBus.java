package com.mvp;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {ModuleCustomEventBus.class})
@Singleton
public interface ComponentEventBus {
    EventBus eventBus();
}
