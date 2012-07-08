package com.googlecode.future;

import com.google.gwt.junit.client.GWTTestCase;

public abstract class AsyncFutureGWTTestCase extends GWTTestCase {
    @Override
    public String getModuleName() {
        return "com.googlecode.future.Tests";
    }
}
