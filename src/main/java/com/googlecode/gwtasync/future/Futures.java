package com.googlecode.gwtasync.future;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.googlecode.gwtasync.rx.Disposer;
import com.googlecode.gwtasync.rx.Observable;
import com.googlecode.gwtasync.rx.ObservableResult;
import com.googlecode.gwtasync.rx.Observer;
import com.googlecode.gwtasync.util.AsyncFn;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.googlecode.gwtasync.future.FutureResult.newFutureResult;
import static com.googlecode.gwtasync.rx.ObservableResult.newObservableResult;

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

  static <T> Observable<T> lift(final Future<Observable<T>> future) {
    return newObservableResult(new ObservableResult.WhenNeeded<T>() {
      @Override
      public void run(final ObservableResult.Result<T> result, Disposer disposer) {
        future.addCallback(new AsyncCallback<Observable<T>>() {
          @Override
          public void onFailure(Throwable caught) {
            result.onError(caught);
          }

          @Override
          public void onSuccess(Observable<T> o) {
            // todo: dispose
            o.subscribe(new Observer<T>() {
              @Override
              public void onCompleted() {
                result.onCompleted();
              }

              @Override
              public void onError(Throwable t) {
                result.onError(t);
              }

              @Override
              public void onNext(T value) {
                result.onNext(value);
              }
            });
          }
        });
      }
    });
  }

  public static <T> Future<List<T>> collect(final List<Future<T>> futures) {
    return newFutureResult(new FutureResult.WhenNeeded<List<T>>() {
      @Override
      public void run(final AsyncCallback<List<T>> result) {
        final List<T> futureResults = new ArrayList<T>(futures.size());
        for (Future ignored : futures) {
          futureResults.add(null);
        }

        final int[] completedFutures = {0};
        for (int i = 0; i < futures.size(); i++) {
          final int idx = i;
          Future<T> future = futures.get(i);
          future.addCallback(new AsyncCallback<T>() {
            @Override
            public void onFailure(Throwable caught) {
              // todo: cancel other futures?
              result.onFailure(caught);
            }

            @Override
            public void onSuccess(T t) {
              futureResults.set(idx, t);
              completedFutures[0]++;
              if (completedFutures[0] == futures.size()) {
                result.onSuccess(futureResults);
              }
            }
          });
        }
      }
    });
  }

  public static <I, O> List<Future<O>> transform(final Iterable<I> data, final AsyncFn<I, O> fn) {
    List<Future<O>> result = newArrayList();
    for (I i : data) {
      result.add(fn.apply(i));
    }
    return result;
  }

  public static <I, O> List<Future<O>> transform(final I[] data, final AsyncFn<I, O> fn) {
    List<Future<O>> result = newArrayList();
    for (I i : data) {
      result.add(fn.apply(i));
    }
    return result;
  }
}
