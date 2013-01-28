package com.googlecode.gwtasync.rx;

import com.google.web.bindery.event.shared.HandlerRegistration;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @author mike.aizatsky@gmail.com
 */
public class Disposer implements Disposable {
  private List<Disposable> list = newArrayList();
  private boolean disposed = false;

  public Disposer() {
  }

  @Override
  public void dispose() {
    checkState(!disposed);
    dispose(list);
    list.clear();
  }

  public void add(Disposable disposable) {
    checkState(!disposed);
    list.add(disposable);
  }

  public void add(final HandlerRegistration registration) {
    add(new Disposable() {
      @Override
      public void dispose() {
        registration.removeHandler();
      }
    });
  }

  public static void dispose(List<Disposable> disposables) {
    for (Disposable disposable : disposables) {
      disposable.dispose();
    }
  }
}
