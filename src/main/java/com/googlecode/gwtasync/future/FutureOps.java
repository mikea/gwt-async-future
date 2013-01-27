package com.googlecode.gwtasync.future;

import com.googlecode.gwtasync.util.AsyncFn;
import com.googlecode.gwtasync.util.Fn;

/**
 * @author mike.aizatsky@gmail.com
 */
public interface FutureOps<T> {
  <S> Future<S> transform(AsyncFn<T, S> fn);
  <S> Future<S> transform(Fn<T, S> fn);
}
