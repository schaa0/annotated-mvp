package com.mvp;


public class Dispatcher<P> implements IDispatcher<P> {

    private IMvpEventBus eventBus;
    private P data;

    public Dispatcher(IMvpEventBus eventBus){
        this.eventBus = eventBus;
    }

    @Override
    public IDispatcher<P> dispatchEvent(P data){
        this.data = data;
        return this;
    }

    @Override
    public void to(Class<? extends IMvpPresenter<?>>... targets){
        this.eventBus.dispatchEvent(data, targets);
    }

    @Override
    public void toAny(){
        this.eventBus.dispatchEvent(data);
    }

}
