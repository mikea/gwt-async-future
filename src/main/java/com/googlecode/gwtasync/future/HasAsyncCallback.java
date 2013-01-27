package com.googlecode.gwtasync.future;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author mike.aizatsky@gmail.com
 */
public interface HasAsyncCallback<T> {
  // todo: return disposable
  void addCallback(AsyncCallback<T> asyncCallback);
}
