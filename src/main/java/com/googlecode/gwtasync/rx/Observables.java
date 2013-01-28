package com.googlecode.gwtasync.rx;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.googlecode.gwtasync.future.Future;

import java.util.Iterator;
import java.util.List;

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

  public static <T> Observable<T> asObservable(final Future<T> future) {
    return newObservableResult(new ObservableResult.WhenNeeded<T>() {
      @Override
      public void run(final ObservableResult.Result<T> result, Disposer disposer) {
        // todo: dispose
        future.addCallback(new AsyncCallback<T>() {
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

  public static <T> Observable<T> asObservable(final Iterable<T> iterable) {
    return newObservableResult(new ObservableResult.WhenNeeded<T>() {
      @Override
      public void run(ObservableResult.Result<T> result, Disposer disposer) {
        for (T t : iterable) {
          result.onNext(t);
        }
        result.onCompleted();
      }
    });
  }

  public static <T> ObservableBase<T> merge(final Observable<List<T>> observable) {
    return newObservableResult(new ObservableResult.WhenNeeded<T>() {
      @Override
      public void run(final ObservableResult.Result<T> result, Disposer disposer) {
        disposer.add(observable.subscribe(new Observer<List<T>>() {
          @Override
          public void onCompleted() {
            result.onCompleted();
          }

          @Override
          public void onError(Throwable t) {
            result.onError(t);
          }

          @Override
          public void onNext(List<T> value) {
            for (T t : value) {
              result.onNext(t);
            }
          }
        }));
      }
    });
  }

  public static <T> Iterator<T> asIterator(Observable<T> observable) {
    return new Iterator<T>() {
      @Override
      public boolean hasNext() {
        // todo: not implemented
        throw new UnsupportedOperationException();
      }

      @Override
      public T next() {
        // todo: not implemented
        throw new UnsupportedOperationException();
      }

      @Override
      public void remove() {
        // todo: not implemented
        throw new UnsupportedOperationException();
      }
    };
  }
}
