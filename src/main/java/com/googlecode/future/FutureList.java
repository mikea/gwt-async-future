package com.googlecode.future;

import java.util.List;

public interface FutureList<T> extends Future<List<T>> {
  void addProgressListener(ProgressListener<T> listener);

  interface ProgressListener<T> {
    void onElementsAdded(List<T> batch, List<T> fullListSoFar);
    void done(List<T> fullList);
  }
}
