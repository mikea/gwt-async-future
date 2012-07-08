package com.googlecode.futuretests;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.googlecode.future.CancelledException;
import com.googlecode.future.FutureResult;

/**
 * Utility class that simulates running actions in a run loop.
 * 
 * @author Dean Povey
 *
 */
public class RunLoopSimulator implements Runnable {
    List<AsyncCallback<Object>> callbacks = new ArrayList<AsyncCallback<Object>>();
    List<Object> values = new ArrayList<Object>();
    
    public <T> void setValueLater(T value, AsyncCallback<T> callback) {
        assert !(value instanceof Throwable) : "Cannot specify Throwable as value. Use failLater"; 
        addCallbackWithValue(value, callback);
    }

    @SuppressWarnings("unchecked")
    private <T> void addCallbackWithValue(T value, AsyncCallback<?> callback) {
        callbacks.add((AsyncCallback<Object>) callback);
        values.add(value);
        assert callbacks.size() == values.size();
    }
    
    public <T> void failLater(Throwable t, AsyncCallback<?> callback) {
        addCallbackWithValue(t, callback);        
    }
    
    public void cancelLater(FutureResult<?> result) {
        failLater(new CancelledException(), result);
    }

    public void run() {
        assert callbacks.size() == values.size();
        for (int i=0; i<callbacks.size(); i++) {
            Object v = values.get(i);
            if (v instanceof Throwable) callbacks.get(i).onFailure((Throwable)v);
            else callbacks.get(i).onSuccess(v);
        }
    }


}
