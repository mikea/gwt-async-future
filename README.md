## Summary

The GWT Async Future package provides an alternative model to using `AsyncCallback` in GWT for performing asynchronous operations. It is partly inspired by the `java.util.concurrent.Future` interface and related classes. This github project is a fork of original one: http://code.google.com/p/gwt-async-future/

## Overview

The basic concept is defined by the `Future<T>` interface. This interface is a container for a result that will be set by an operation at some future point (possibly never). It's concrete implementations `FutureResult<T>` and `FutureAction<T>` also implement `AsyncCallback<T>` so they can be used anywhere in GWT you would normally use a callback, e.g.

    FutureResult<Boolean> futureBoolean = new FutureResult<Boolean>();
    MyRemoteInterfaceAsync service = GWT.create(MyRemoteInterface.class);
    service.callMethodThatReturnsBoolean(futureBoolean);

When the service call completes the `onSuccess()` method of `FutureResult` will bind a value to the result by calling the `set()` method. This value can be obtained using the `get()` method. e.g.

    // At some later point
    if (futureBoolean.isDone() && futureBoolean().get()) {
          Window.alert("Success");
    }

If an exception occurred then a call to `futureBoolean().get()` will throw that exception if it is unchecked or an instance of `ExecutionException` with the checked exception as the cause.

It is important to note that without the `futureBoolean.isDone()` guard above then a call to `futureBoolean().get()` may also fail with an `IncompleteResultException` if the result has not yet been set. In addition to this method the `getAsync` method may be called passing a standard `AsyncCallback` to be called when the result becomes available.

This in of itself is not all that useful, it is only when combined with other results that the power of this approach becomes clear. To do this we need to examine the `FutureAction` subclass, e.g.

    final FutureResult<Boolean> futureBoolean = new FutureResult<Boolean>();
    MyRemoteInterfaceAsync service = GWT.create(MyRemoteInterface.class);
    service.callMethodThatReturnsBoolean(futureBoolean);

    final FutureResult<Boolean> otherFutureBoolean = new FutureResult<Boolean>();
    service.callOtherMethodThatReturnsBoolean(otherFutureBoolean);

    FutureAction<Date> timeOfResult = new FutureAction<Boolean> {
        public void run() {
            if (futureBoolean.get() && otherFutureBoolean.get()) {
                set(new Date());
                return;
            }
            throw new IllegalStateException("Unexpected failure");
        }
    }

    timeOfResult.getAsync(new AsyncCallback<Date>() {
        public void onFailure(Throwable t) { ... }
        public void onSuccess(Date result) {
            Window.alert("Got result at: " + result);
        }
    });

A `FutureAction` represents an action that may be run at some future point (possibly never). It has a `run()` method that specifies the action to perform when run. There are some constraints on the `run()` method: either it must be side-effect free (ie. there must be no method calls which perform an action that cannot be performed multiple times, e.g displaying a result or calling a service), or it must ensure that any references to other `FutureResult` instances happen before any side-effects. This quirk comes about because of the way a `FutureAction` evaluates its run method. You can see that we do not have any if `isDone()` guards before the `get()` result. This means that this method may throw `IncompleteResultException` including a reference to the `FutureResult` which is incomplete. The `FutureAction` adds a callback so that when this value becomes available then it will recall the run method. If there are multiple dependencies this continues until all are satisfied and then the side effects can be run. A `FutureResult` can only be assigned to once so this ensures that once a result is available a call to get is idempotent, and once the run method completes successfully (or fails) then it will not be run again.

It is a very important to note that creating a `FutureAction` does not cause the action to be run. This will only happen if either the `get()`, `getAsync()` or the `eval()` method (essentially get the result asynchronously but don't wait) is called. Thus we have to add an additional call to `getAsync()` above with a callback to actually get the result. The effect of this is that calling of chained asynchronous actions optimized so that they are only run if they are needed and this approach automatically handles for example boolean shortcutting while resolving dependencies. e.g.

    FutureAction<Boolean> succeeds = new FutureAction<Boolean>() {
        public void run() {
            set(true);
        }
    }

    FutureAction<Boolean> neverCalled = new FutureAction<Boolean>() {
        public void run() {
            throw new AssertionError("Should never be called");
        }
    }

    FutureAction<Boolean> result = new FutureAction<Boolean>() {
        public void run() {
            set(succeeds.get() || fails.get()); // Note: fails will never be run!
        }
    }

In addition, a given action may call a dependent `FutureResult` as many times as it likes but it will only be evaluated once and the same result returned thereafter.

It should be noted that the `FutureAction` must either call set or pass itself to a method which will call its onSuccess method on a successful result. If this does not happen then the action will never complete successfully.

To set an exception the action can either just throw the exception if it is unchecked or else it can call the `setException()` method.

`FutureAction` is far more useful than `FutureResult` and in most cases you are better off wrapping a call to a method that takes a callback with a `FutureAction` rather than passing a bare `FutureResult`, this is because while:

    final FutureResult<Boolean> futureBoolean = new FutureResult<Boolean>();
    MyRemoteInterfaceAsync service = GWT.create(MyRemoteInterface.class);
    service.callMethodThatReturnsBoolean(futureBoolean);

and

    final FutureResult<Boolean> futureBoolean = new FutureAction<Boolean>() {
        public void run() {
            MyRemoteInterfaceAsync service = GWT.create(MyRemoteInterface.class);
            service.callMethodThatReturnsBoolean(futureBoolean);
        }
    };

look functionally equivalent, the former will always be run, whereas the latter will only be run if there is another `FutureAction`/`FutureResult` depending on it. This makes the code much easier to maintain. The bare `FutureResult` is more useful when you want to query a result with best effort, e.g. you will use the results of the operation if available, but will skip it if not and wait for them to become available later. This is particularly useful if the call is long lived e.g.:

    FutureResult<Statistics> result = getUpdatedStatisticsWhenAvailable();

    // Update some application metrics if available, otherwise pick it up next time.
    if (result.isDone()) {
        updateStatisics(result.get());
        result = getUpdatedStatisticsWhenAvailable();
    }

### Summary

* A `Future` is container for the result of an operation that may happen at some point in the future (potentially never).
* A `FutureResult` is a concrete implementation that can be used in place of an AsyncCallback to store a result.
* A `FutureAction` is a subclass of `FutureResult` that may perform an action based on the results of zero or more other `FutureResult` or `FutureActions` and set its own result. This is done by implementing `FutureAction`'s `run()` method.
* A `FutureAction` is not run when created but only if it receives a subsequent `get()`, `getAsync()` or `eval()` call.
* A `FutureAction` must either call set or setException directly, throw an unchecked exception, or pass itself as a callback to another function which will call the `onSuccess` or `onFailure` method. If these rules are not followed then the action will never complete and this may manifest itself either as an unexpected `IncompleteResultException` or simply nothing happening.
* A `FutureAction` run method which relies on other `FutureActions` or `FutureResults` must ensure that it is side-effect free until it has obtained all its dependencies by calling `get()`.

### Cancelling

A `FutureAction` or `FutureResult` may be cancelled by calling the `cancel()` method. Simply this will cause the `FutureResult` to fail with a `CancelledException` which will be propogated to all dependent `FutureResults`. This can be queried by calling `isCancelled()`. I am contemplating adding a `CancellableAsyncCallback` so this can be handled in a more fine-grained way by callers rather than having to process this exception in the onFailure method. Not everything can be cancelled easily (e.g. RPC calls), however when a chain includes things like dialogs to prompt for parameters with a cancel button this is essential.