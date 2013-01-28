package com.googlecode.gwtasync.rx;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * todo: should promote lazy initialization like FutureResult does.
 *
 * @author mike.aizatsky@gmail.com
 */
public class ObservableResult<T> extends ObservableImpl<T> {
  private Disposer disposer = new Disposer();
  private final WhenNeeded<T> whenNeeded;

  private ObservableResult(WhenNeeded<T> whenNeeded) {
    this.whenNeeded = whenNeeded;
  }

  @Override
  public void dispose() {
    super.dispose();
    disposer.dispose();
  }

  @Override
  protected void onStart() {
    super.onStart();

    final ObservableResult<T> self = this;

    whenNeeded.run(new Result<T>() {
      @Override
      public void onNext(T value) {
        self.onNext(value);
      }

      @Override
      public void onCompleted() {
        self.onCompleted();
      }

      @Override
      public void onError(Throwable t) {
        self.onError(t);
      }

      @Override
      public boolean isDone() {
        return self.isDone();
      }
    }, disposer);
  }

  public static <T> ObservableResult<T> newObservableResult(WhenNeeded<T> whenNeeded) {
    return new ObservableResult<T>(checkNotNull(whenNeeded));
  }

  public interface WhenNeeded<T> {
    void run(Result<T> result, Disposer disposer);
  }

  public interface Result<T> {
    void onNext(T value);

    void onCompleted();

    void onError(Throwable t);

    boolean isDone();
  }
}
