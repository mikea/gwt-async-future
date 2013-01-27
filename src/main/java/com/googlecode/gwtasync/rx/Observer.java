package com.googlecode.gwtasync.rx;

/**
 * @author mike.aizatsky@gmail.com
 */
public interface Observer<T> {
  void onCompleted();
  void onError(Throwable t);
  void onNext(T value);
}
