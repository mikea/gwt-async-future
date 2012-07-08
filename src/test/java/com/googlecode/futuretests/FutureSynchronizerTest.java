package com.googlecode.futuretests;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.googlecode.future.FutureAction;
import com.googlecode.future.FutureResult;
import com.googlecode.future.FutureSynchronizer;

import static org.junit.Assert.*;

import static com.googlecode.future.ConstantResult.constant;

public class FutureSynchronizerTest {
    @Test
    public void canSynchronizedWithSimpleResult() {
        FutureResult<Boolean> resultToSynchronizeWith = constant(true);
        FutureSynchronizer result = new FutureSynchronizer(resultToSynchronizeWith);
        assertTrue(result.result());
        assertTrue(resultToSynchronizeWith.result());
    }
    
    @Test
    public void canSynchronizedWithMultipleResults() {
        List<FutureResult<Integer>> resultsToSynchronizeWith = new ArrayList<FutureResult<Integer>>();
        for (int i=0; i<10; i++) {
            resultsToSynchronizeWith.add(constant(i));
        }
        FutureSynchronizer result = new FutureSynchronizer(resultsToSynchronizeWith);
        assertTrue(result.result());
        for (int i=0; i<10; i++) {
            assertEquals(i, (int)resultsToSynchronizeWith.get(i).result());
        }
    }
    
    @Test
    public void canSynchronizedWithMultipleResultsWhenResultsDelayed() {
        final RunLoopSimulator runloop = new RunLoopSimulator();
        List<FutureResult<Integer>> resultsToSynchronizeWith = new ArrayList<FutureResult<Integer>>();
        for (int i=0; i<10; i++) {
            final Integer value = i;
            resultsToSynchronizeWith.add(new FutureAction<Integer>() {
                public void run() {
                    runloop.setValueLater(value, this);
                }
            });
        }
        FutureSynchronizer result = new FutureSynchronizer(resultsToSynchronizeWith);
        result.start();
        runloop.run();
        assertTrue(result.result());
        for (int i=0; i<10; i++) {
            assertEquals(i, (int)resultsToSynchronizeWith.get(i).result());
        }
    }
    @Test
    public void whenASynchronizedResultIsCancelledFutureSynchronizerIsAlsoCancelled() {
        final RunLoopSimulator runloop = new RunLoopSimulator();
        List<FutureResult<Integer>> resultsToSynchronizeWith = new ArrayList<FutureResult<Integer>>();
        for (int i=0; i<2; i++) {
            final Integer value = i;
            resultsToSynchronizeWith.add(new FutureAction<Integer>() {
                public void run() {
                    runloop.setValueLater(value, this);
                }
            });
        }
        resultsToSynchronizeWith.add(new FutureAction<Integer>() {
            public void run() {
                runloop.cancelLater(this);
            }
        });
        FutureSynchronizer result = new FutureSynchronizer(resultsToSynchronizeWith);
        result.start();
        runloop.run();
        assertTrue(result.isCancelled());
        // Everything but the last result is completed.
        for (int i=0; i<2; i++) {
            assertEquals(i, (int)resultsToSynchronizeWith.get(i).result());
        }
    }
    
    @Test
    public void whenASynchronizedResultFailsFutureSsynchronizerSucceeds() {
        final int NUMBER_OF_RESULTS_TO_SYNCHRONIZE_AFTER_FAILURE = 2;
        
        List<FutureResult<Integer>> resultsToSynchronizeWith = new ArrayList<FutureResult<Integer>>();
        resultsToSynchronizeWith.add(new FutureAction<Integer>() {
            public void run() {
                throw new NullPointerException();                
            }
        });
        // Results which will not evaluated due to above failure
        for (int i=0; i<NUMBER_OF_RESULTS_TO_SYNCHRONIZE_AFTER_FAILURE; i++) {
            final Integer value = i;
            resultsToSynchronizeWith.add(new FutureAction<Integer>() {
                public void run() {
                    returnResult(value);
                }
            });
        }
        FutureSynchronizer result = new FutureSynchronizer(resultsToSynchronizeWith);
        assertTrue(result.result());
        // Make sure no result after the first was evaulated
        for (int i=1; i<NUMBER_OF_RESULTS_TO_SYNCHRONIZE_AFTER_FAILURE + 1; i++) {
            assertTrue(resultsToSynchronizeWith.get(i).isComplete());
        }
    }

}
