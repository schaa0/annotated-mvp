package com.mvp;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Andy on 22.01.2017.
 */

public class MyExecutorService extends AbstractExecutorService
{
    @Override
    public void shutdown()
    {

    }

    @NonNull
    @Override
    public List<Runnable> shutdownNow()
    {
        return new ArrayList<>();
    }

    @Override
    public boolean isShutdown()
    {
        return false;
    }

    @Override
    public boolean isTerminated()
    {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
    {
        return false;
    }

    @Override
    public void execute(Runnable command)
    {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(command);
    }
}
