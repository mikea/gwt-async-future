package com.googlecode.future;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FutureListResult<T> extends FutureResult<List<T>> implements FutureList<T> {
  private List<T> list = new ArrayList<T>();
  private List<ProgressListener<T>> listeners = null;

  @Override
  public void addProgressListener(ProgressListener<T> listener) {
    List<T> uList = Collections.unmodifiableList(list);

    if (isComplete()) {
      listener.onElementsAdded(uList, uList);
      listener.done(uList);
    } else {
      if (listeners == null) {
        listeners = new ArrayList<ProgressListener<T>>();
        listeners.add(listener);
      }
      listener.onElementsAdded(uList, uList);
    }
  }

  public void addAll(List<T> items) {
    list.addAll(items);
    if (listeners != null) {
      List<T> uItems = Collections.unmodifiableList(items);
      List<T> uList = Collections.unmodifiableList(list);

      for (ProgressListener<T> listener : listeners) {
        listener.onElementsAdded(uItems, uList);
      }
    }
  }

  public void done() {
    List<T> uList = Collections.unmodifiableList(list);
    if (listeners != null) {
      for (ProgressListener<T> listener : listeners) {
        listener.done(uList);
      }
    }
    super.onSuccess(uList);
  }

  @Override
  public void onSuccess(List<T> result) {
    throw new RuntimeException("Use addAll & done method instead");
  }
}
