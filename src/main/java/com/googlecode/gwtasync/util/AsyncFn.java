package com.googlecode.gwtasync.util;

import com.googlecode.gwtasync.future.Future;

/**
* @author mike.aizatsky@gmail.com
*/
public interface AsyncFn<I, O> {
  Future<O> apply(I input);
}
