package com.googlecode.futuretests;

import junit.framework.Test;
import junit.framework.TestCase;

import com.google.gwt.junit.tools.GWTTestSuite;
import com.googlecode.future.GwtTstIncrementalFuture;

public class GwtTestSuiteAsyncFuture extends TestCase {

    public static Test suite() {
        GWTTestSuite suite = new GWTTestSuite("Unit tests for GWT Async Future code");        
        suite.addTestSuite(GwtTstIncrementalFuture.class);
        return suite;
    }
}
