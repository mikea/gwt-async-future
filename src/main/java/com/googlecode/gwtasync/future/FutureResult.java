package com.googlecode.gwtasync.future;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author mike.aizatsky@gmail.com
 */
public class FutureResult<T> extends FutureBase<T> {
  private WhenNeeded<T> whenNeeded;
  private List<AsyncCallback<T>> callbacks = newArrayList();
  private boolean done = false;
  private Throwable caught;
  private T result;

  public FutureResult(WhenNeeded<T> whenNeeded) {
    this.whenNeeded = whenNeeded;
  }

  public static <T> FutureResult<T> newFutureResult(WhenNeeded<T> whenNeeded) {
    return new FutureResult<T>(whenNeeded);
  }

  @Override
  public void addCallback(AsyncCallback<T> asyncCallback) {
    if (done) {
      if (caught != null) {
        asyncCallback.onFailure(caught);
      } else {
        asyncCallback.onSuccess(result);
      }
      return;
    }

    callbacks.add(asyncCallback);

    if (callbacks.size() == 1 && whenNeeded != null) {
      whenNeeded.run(new AsyncCallback<T>() {
        @Override
        public void onFailure(Throwable caught) {
          FutureResult.this.onFailure(caught);
        }

        @Override
        public void onSuccess(T result) {
          FutureResult.this.onSuccess(result);
        }
      });
      whenNeeded = null;
    }
  }

  private void onFailure(Throwable caught) {
    done = true;
    this.caught = caught;
    for (AsyncCallback<T> callback : callbacks) {
      callback.onFailure(caught);
    }
    callbacks.clear();
  }

  private void onSuccess(T t) {
    done = true;
    result = t;
    for (AsyncCallback<T> callback : callbacks) {
      callback.onSuccess(t);
    }
    callbacks.clear();
  }

  public interface WhenNeeded<T> {
    void run(AsyncCallback<T> result);
  }
}
