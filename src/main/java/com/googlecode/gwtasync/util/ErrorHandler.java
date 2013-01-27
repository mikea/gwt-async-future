package com.googlecode.gwtasync.util;

/**
 * @author mike.aizatsky@gmail.com
 */
public interface ErrorHandler {
  void onFailure(Throwable caught);
}
