package com.googlecode.futuretests;

import org.junit.Test;

import com.googlecode.future.AutoFuture;
import com.googlecode.future.AutoFutureAction;
import com.googlecode.future.FutureAction;

import static org.junit.Assert.*;
import static com.googlecode.future.Auto.auto;

public class AutoFutureTest {
    @Test
    public void wrappedAutoFutureIsAlwaysEvaluated() {
        AutoFuture<Boolean> auto = auto(new FutureAction<Boolean>() {
            public void run() {
                returnResult(true);
            }
        });
        assertTrue(auto.isComplete());
        assertTrue(auto.result());       
    }
    
    @Test
    public void autoFutureActionIsAlwaysEvaluated() {
        AutoFuture<Boolean> auto = new AutoFutureAction<Boolean>() {
            public void run() {
                returnResult(true);
            }
        };
        assertTrue(auto.isComplete());
        assertTrue(auto.result());       
    }
}
