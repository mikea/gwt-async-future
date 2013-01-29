package com.googlecode.gwtasync.future;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.googlecode.gwtasync.rx.Disposer;
import com.googlecode.gwtasync.rx.Observable;
import com.googlecode.gwtasync.rx.ObservableResult;
import com.googlecode.gwtasync.util.AsyncFn;
import com.googlecode.gwtasync.util.ErrorHandler;
import com.googlecode.gwtasync.util.Fn;
import com.googlecode.gwtasync.util.P;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.googlecode.gwtasync.future.FutureResult.newFutureResult;
import static com.googlecode.gwtasync.rx.ObservableResult.newObservableResult;

/**
 * @author mike.aizatsky@gmail.com
 */
public abstract class FutureBase<T> implements Future<T> {
  private static final Logger LOG = Logger.getLogger(FutureBase.class.getName());

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
              LOG.log(Level.SEVERE, "Error transforming future", e);
              result.onFailure(e);
            }
          }
        });
      }
    });
  }

  @Override
  public void process(final P<T> p, final ErrorHandler errorHandler) {
    addCallback(new AsyncCallback<T>() {
      @Override
      public void onFailure(Throwable caught) {
        errorHandler.onFailure(caught);
      }

      @Override
      public void onSuccess(T result) {
        p.process(result);
      }
    });
  }

  @Override
  public Observable<T> asObservable() {
    final Future<T> self = this;
    return newObservableResult(new ObservableResult.WhenNeeded<T>() {
      @Override
      public void run(final ObservableResult.Result<T> result, Disposer disposer) {
        // todo: dispose
        self.addCallback(new AsyncCallback<T>() {
          @Override
          public void onFailure(Throwable caught) {
            result.onError(caught);
          }

          @Override
          public void onSuccess(T t) {
            result.onNext(t);
            result.onCompleted();
          }
        });
      }
    });
  }

  @Override
  public <S> Observable<S> transformToObservable(final Fn<T, Observable<S>> fn) {
    return Futures.lift(transform(fn));
  }
}
