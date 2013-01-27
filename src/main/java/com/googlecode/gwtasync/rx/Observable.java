package com.googlecode.gwtasync.rx;

import javax.annotation.Nonnull;

/**
 * @author mike.aizatsky@gmail.com
 */
public interface Observable<T> extends ObservableOps<T> {
  Disposable subscribe(@Nonnull Observer<T> observer);
}
