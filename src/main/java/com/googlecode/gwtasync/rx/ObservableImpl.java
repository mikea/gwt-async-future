package com.googlecode.gwtasync.rx;

import javax.annotation.Nonnull;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @author mike.aizatsky@gmail.com
 */
public abstract class ObservableImpl<T> extends ObservableBase<T> implements Observable<T>, Disposable {
  private List<Observer<T>> observers = newArrayList();
  private boolean disposed = false;
  private boolean done = false;

  protected void onStart() {
  }

  @Override
  public Disposable subscribe(@Nonnull final Observer<T> observer) {
    List<Observer<T>> newObservers = newArrayList(observers);
    newObservers.add(observer);
    observers = newObservers;

    if (observers.size() == 1) {
      onStart();
    }

    return new Disposable() {
      @Override
      public void dispose() {
        unsubscribe(observer);
      }
    };
  }

  private void unsubscribe(Observer<T> observer) {
    List<Observer<T>> newObservers = newArrayList(observers);
    newObservers.remove(observer);
    observers = newObservers;
    if (observers.isEmpty()) {
      dispose();
    }
  }

  protected void onNext(T value) {
    checkState(!done && !disposed);
    for (Observer<T> observer : observers) {
      observer.onNext(value);
    }
  }

  protected void onCompleted() {
    checkState(!done && !disposed);
    for (Observer<T> observer : observers) {
      observer.onCompleted();
    }
    dispose();
  }

  protected void onError(Throwable t) {
    checkState(!done && !disposed);
    for (Observer<T> observer : observers) {
      observer.onError(t);
    }
    dispose();
  }

  protected boolean isDone() {
    return done;
  }

  @Override
  public void dispose() {
    if (disposed) return;
    checkState(!done);
    done = true;
    disposed = false;
    observers.clear();
  }
}
