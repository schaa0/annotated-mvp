package com.mvp.annotation.processor.unittest;

import java.io.Serializable;
import java.util.List;

public class ComponentCache implements Serializable
{
    private final List<GraphCache> mGraphCache;

    public ComponentCache(List<GraphCache> mGraphCache) {
        this.mGraphCache = mGraphCache;
    }

    public List<GraphCache> getCache()
    {
        return mGraphCache;
    }

}
