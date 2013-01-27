package com.googlecode.gwtasync.util;

/**
* @author mike.aizatsky@gmail.com
*/
public interface Fn<I, O> {
  O apply(I input);
}
