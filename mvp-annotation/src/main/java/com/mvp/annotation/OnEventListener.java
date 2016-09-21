package com.mvp.annotation;

public interface OnEventListener<T> {
    void onEvent(T data, Class<?>... targets);
    void onDestroy();
    void setNext(Object nextEventListener);
    boolean hasNext();
    OnEventListener<?> getNext();
    void clearNext();
    Class<T> getDataClass();
}
