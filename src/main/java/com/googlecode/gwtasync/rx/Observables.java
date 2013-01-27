package com.googlecode.gwtasync.rx;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.googlecode.gwtasync.future.Future;

import java.util.Iterator;
import java.util.List;

/**
 * @author mike.aizatsky@gmail.com
 */
public class Observables {
  public static <T> Observable<T> merge(Iterable<Observable<T>> sources) {
    final Disposables connections = new Disposables();
    final ObservableResult<T> result = new ObservableResult<T>(connections);

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
      connections.add(source.subscribe(observer));
    }

    return result;
  }

  public static <T> Observable<T> merge(Observable<Observable<T>> sources) {
    final Disposables connections = new Disposables();
    final ObservableResult<T> result = new ObservableResult<T>(connections);

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

    connections.add(sources.subscribe(new Observer<Observable<T>>() {
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
        connections.add(value.subscribe(leafObserver));
      }
    }));

    return result;
  }

  public static <T> Observable<T> asObservable(Future<T> future) {
    final ObservableResult<T> result = new ObservableResult<T>();
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
    return result;
  }

  public static <T> Observable<T> asObservable(Iterable<T> iterable) {
    ObservableResult<T> result = new ObservableResult<T>();
    for (T t : iterable) {
      result.onNext(t);
    }
    result.onCompleted();
    return result;
  }

  public static <T> ObservableBase<T> merge(Observable<List<T>> observable) {
    final ObservableResult<T> result = new ObservableResult<T>();

    Disposable subscription = observable.subscribe(new Observer<List<T>>() {
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
    });

    result.setDisposable(subscription);
    return result;
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
