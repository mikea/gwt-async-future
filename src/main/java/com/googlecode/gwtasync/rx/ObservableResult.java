package com.googlecode.gwtasync.rx;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkState;

/**
 * todo: should promote lazy initialization like FutureResult does.
 *
 * @author mike.aizatsky@gmail.com
 */
public class ObservableResult<T> extends ObservableImpl<T> {
  private Disposable disposable;

  public ObservableResult() {
    this(null);
  }

  public ObservableResult(@Nullable Disposable disposable) {
    this.disposable = disposable;
  }

  public void setDisposable(Disposable disposable) {
    checkState(disposable == null);
    this.disposable = disposable;
  }

  @Override
  public void dispose() {
    super.dispose();
    if (disposable != null) {
      disposable.dispose();
    }
  }

  @Override
  public boolean isDone() { return super.isDone(); }

  @Override
  public final void onNext(T value) { super.onNext(value); }

  @Override
  public final void onCompleted() { super.onCompleted(); }

  @Override
  public final void onError(Throwable t) { super.onError(t); }
}
