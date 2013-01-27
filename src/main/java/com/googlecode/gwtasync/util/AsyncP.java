package com.googlecode.gwtasync.util;

import com.googlecode.gwtasync.future.Future;

/**
* @author mike.aizatsky@gmail.com
*/
public interface AsyncP<I, O> {
  Future<O> process(I input);
}
