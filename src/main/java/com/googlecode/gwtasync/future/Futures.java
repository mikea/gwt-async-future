package com.googlecode.gwtasync.future;

import com.google.gwt.user.client.rpc.AsyncCallback;

import static com.googlecode.gwtasync.future.FutureResult.newFutureResult;

/**
 * @author mike.aizatsky@gmail.com
 */
public class Futures {
  public static <T> Future<T> constant(final T t) {
    return newFutureResult(new FutureResult.WhenNeeded<T>() {
      @Override
      public void run(AsyncCallback<T> result) {
        result.onSuccess(t);
      }
    });
  }
}
