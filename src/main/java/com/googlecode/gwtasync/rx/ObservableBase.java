package com.googlecode.gwtasync.rx;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.googlecode.gwtasync.future.Future;
import com.googlecode.gwtasync.future.FutureResult;
import com.googlecode.gwtasync.util.ErrorHandler;
import com.googlecode.gwtasync.util.Fn;
import com.googlecode.gwtasync.util.P;

import javax.annotation.Nonnull;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.googlecode.gwtasync.future.FutureResult.newFutureResult;

/**
 * @author mike.aizatsky@gmail.com
 */
public abstract class ObservableBase<T> implements Observable<T> {
  @Override
  public <S> Observable<S> transform(final Fn<T, S> f) {
    final ObservableBase<T> self = this;

    return new ObservableBase<S>() {
      @Override
      public Disposable subscribe(@Nonnull final Observer<S> observer) {
        return self.subscribe(new Observer<T>() {
          @Override
          public void onCompleted() {
            observer.onCompleted();
          }

          @Override
          public void onError(Throwable t) {
            observer.onError(t);
          }

          @Override
          public void onNext(T value) {
            try {
              observer.onNext(f.apply(value));
            } catch (Exception e) {
              observer.onError(e);
            }
          }
        });
      }
    };
  }

  @Override
  public void process(final P<T> p, final ErrorHandler errorHandler) {
    subscribe(new Observer<T>() {
      @Override
      public void onCompleted() {
      }

      @Override
      public void onError(Throwable t) {
        errorHandler.onFailure(t);
      }

      @Override
      public void onNext(T value) {
        try {
          p.process(value);
        } catch (Exception e) {
          onError(e);
        }
      }
    });
  }

  @Override
  public <S> Observable<S> selectMany(Fn<T, Observable<S>> f) {
    return Observables.merge(transform(f));
  }

  @Override
  public Future<List<T>> asFuture() {
    return newFutureResult(new FutureResult.WhenNeeded<List<T>>() {
      @Override
      public void run(final AsyncCallback<List<T>> result) {
        final List<T> list = newArrayList();

        // todo: dispose?
        subscribe(new ObserverBase<T>() {
          @Override
          public void onNext(T value) {
            list.add(value);
          }

          @Override
          public void onCompleted() {
            result.onSuccess(list);
          }

          @Override
          public void onError(Throwable t) {
          result.onFailure(t);
          }
        });
      }
    });
  }
}
