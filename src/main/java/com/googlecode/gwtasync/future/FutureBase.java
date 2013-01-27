package com.googlecode.gwtasync.future;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.googlecode.gwtasync.util.AsyncFn;
import com.googlecode.gwtasync.util.Fn;

import static com.googlecode.gwtasync.future.FutureResult.newFutureResult;

/**
 * @author mike.aizatsky@gmail.com
 */
public abstract class FutureBase<T> implements Future<T> {
  @Override
  public <S> Future<S> transform(final AsyncFn<T, S> fn) {
    final Future<T> self = this;
    return newFutureResult(new FutureResult.WhenNeeded<S>() {
      @Override
      public void run(final AsyncCallback<S> result) {
        self.addCallback(new AsyncCallback<T>() {
          @Override
          public void onFailure(Throwable caught) {
            result.onFailure(caught);
          }

          @Override
          public void onSuccess(T t) {
            fn.apply(t).addCallback(new AsyncCallback<S>() {
              @Override
              public void onFailure(Throwable caught) {
                result.onFailure(caught);
              }

              @Override
              public void onSuccess(S s) {
                result.onSuccess(s);
              }
            });
          }
        });
      }
    });
  }

  @Override
  public <S> Future<S> transform(final Fn<T, S> fn) {
    final Future<T> self = this;
    return newFutureResult(new FutureResult.WhenNeeded<S>() {
      @Override
      public void run(final AsyncCallback<S> result) {
        self.addCallback(new AsyncCallback<T>() {
          @Override
          public void onFailure(Throwable caught) {
            result.onFailure(caught);
          }

          @Override
          public void onSuccess(T t) {
            try {
              result.onSuccess(fn.apply(t));
            } catch (Exception e) {
              result.onFailure(e);
            }
          }
        });
      }
    });
  }
}
