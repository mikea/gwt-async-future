package com.googlecode.gwtasync.rx;

/**
 * @author mike.aizatsky@gmail.com
 */
public abstract class ObserverBase<T> implements Observer<T> {
  @Override
  public void onCompleted() {
  }

  @Override
  public void onError(Throwable t) {
    throw new RuntimeException(t);
  }

  @Override
  public void onNext(T value) {
  }
}
