package de.hda.simple_example.container;


import java.io.IOException;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Andy on 16.12.2016.
 */

public abstract class FailingCallAdapter<T> implements Call<T> {

    @Override
    public Response<T> execute() throws IOException {
        return buildResponse();
    }

    public abstract Response<T> buildResponse();

    @Override
    public void enqueue(Callback<T> callback) {

    }

    @Override
    public boolean isExecuted() {
        return false;
    }

    @Override
    public void cancel() {

    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public Call<T> clone() {
        return null;
    }

    @Override
    public Request request() {
        return null;
    }
}
