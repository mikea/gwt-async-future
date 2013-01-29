package com.googlecode.gwtasync.rx;

import static com.googlecode.gwtasync.rx.ObservableResult.newObservableResult;

/**
 * @author mike.aizatsky@gmail.com
 */
public class Observables {
  public static <T> Observable<T> merge(final Iterable<Observable<T>> sources) {
    return newObservableResult(new ObservableResult.WhenNeeded<T>() {
      @Override
      public void run(final ObservableResult.Result<T> result, Disposer disposer) {
        Observer<T> observer = new Observer<T>() {
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
        };

        for (Observable<T> source : sources) {
          disposer.add(source.subscribe(observer));
        }
      }
    });
  }

  public static <T> Observable<T> merge(final Observable<Observable<T>> sources) {
    return newObservableResult(new ObservableResult.WhenNeeded<T>() {
      @Override
      public void run(final ObservableResult.Result<T> result, final Disposer disposer) {
        final Observer<T> leafObserver = new Observer<T>() {
          @Override
          public void onCompleted() {
            // todo: not implemented
            throw new UnsupportedOperationException();
          }

          @Override
          public void onError(Throwable t) {
            // todo: not implemented
            throw new UnsupportedOperationException();
          }

          @Override
          public void onNext(T value) {
            result.onNext(value);
          }
        };

        disposer.add(sources.subscribe(new Observer<Observable<T>>() {
          @Override
          public void onCompleted() {
            result.onCompleted();
          }

          @Override
          public void onError(Throwable t) {
            result.onError(t);
          }

          @Override
          public void onNext(Observable<T> value) {
            disposer.add(value.subscribe(leafObserver));
          }
        }));
      }
    });
  }

  public static <T> Observable<T> constant(final T t) {
    return newObservableResult(new ObservableResult.WhenNeeded<T>() {
      @Override
      public void run(ObservableResult.Result<T> result, Disposer disposer) {
        result.onNext(t);
        result.onCompleted();
      }
    });
  }
}
