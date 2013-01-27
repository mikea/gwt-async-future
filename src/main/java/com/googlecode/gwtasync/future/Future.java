package com.googlecode.gwtasync.future;

/**
 * @author mike.aizatsky@gmail.com
 */
public interface Future<T> extends FutureOps<T>, HasAsyncCallback<T> {
}
