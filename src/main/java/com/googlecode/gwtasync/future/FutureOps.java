package com.googlecode.gwtasync.future;

import com.googlecode.gwtasync.rx.Observable;
import com.googlecode.gwtasync.util.AsyncFn;
import com.googlecode.gwtasync.util.ErrorHandler;
import com.googlecode.gwtasync.util.Fn;
import com.googlecode.gwtasync.util.P;

/**
 * @author mike.aizatsky@gmail.com
 */
public interface FutureOps<T> {
  <S> Future<S> transform(AsyncFn<T, S> fn);
  <S> Future<S> transform(Fn<T, S> fn);

  <S> Observable<S> transformToObservable(Fn<T, Observable<S>> fn);

  void process(P<T> p, ErrorHandler errorHandler);

  Observable<T> asObservable();
}
