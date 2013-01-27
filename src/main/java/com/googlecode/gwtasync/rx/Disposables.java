package com.googlecode.gwtasync.rx;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @author mike.aizatsky@gmail.com
 */
public class Disposables implements Disposable {
  private List<Disposable> list = newArrayList();
  private boolean disposed = false;

  public Disposables() {
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
  public static void dispose(List<Disposable> disposables) {
    for (Disposable disposable : disposables) {
      disposable.dispose();
    }
  }
}
