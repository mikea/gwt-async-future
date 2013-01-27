package com.googlecode.gwtasync.future;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author mike.aizatsky@gmail.com
 */
public abstract class FutureImpl<T> extends FutureBase<T> {
  @Override
  public void addCallback(AsyncCallback<T> asyncCallback) {
    // todo: not implemented
    throw new UnsupportedOperationException();
  }

}
