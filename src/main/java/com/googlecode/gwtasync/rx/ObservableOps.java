package com.googlecode.gwtasync.rx;

import com.googlecode.gwtasync.future.Future;
import com.googlecode.gwtasync.util.ErrorHandler;
import com.googlecode.gwtasync.util.Fn;
import com.googlecode.gwtasync.util.P;

import java.util.List;

/**
 * @author mike.aizatsky@gmail.com
 */
public interface ObservableOps<T> {
  <S> Observable<S> transform(final Fn<T, S> f);

  void process(final P<T> p, ErrorHandler errorHandler);

  <S> Observable<S> selectMany(Fn<T, Observable<S>> f);

  Future<List<T>> asFuture();

  Observable<T> proxy(Observer<T> observer);
}
