package com.googlecode.futuretests;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.googlecode.future.FutureAction;
import com.googlecode.future.FutureResult;
import com.googlecode.future.FutureSequencer;

import static org.junit.Assert.*;
import static com.googlecode.future.ConstantResult.constant;

public class FutureSequencerTest {

    @Test
    public void canSequenceSingleResult() {
        FutureResult<Boolean> single = constant(true);
        FutureSequencer sequence = new FutureSequencer(single);
        assertTrue(sequence.result());
        assertTrue(single.result());
    }
    
    @Test
    public void canSequenceMultipleResults() {
        List<FutureResult<Integer>> multiple = new ArrayList<FutureResult<Integer>>();
        for (int i=0; i<10; i++) {
            multiple.add(constant(i));
        }
        FutureSequencer sequence = new FutureSequencer(multiple);
        assertTrue(sequence.result());
        for (int i=0; i<10; i++) {
            assertEquals(i, (int)multiple.get(i).result());
        }
    }
    
    @Test
    public void canSequenceMultipleResultsWhenResultsDelayed() {
        final RunLoopSimulator runloop = new RunLoopSimulator();
        List<FutureResult<Integer>> multipleDelayed = new ArrayList<FutureResult<Integer>>();
        for (int i=0; i<10; i++) {
            final Integer value = i;
            multipleDelayed.add(new FutureAction<Integer>() {
                public void run() {
                    runloop.setValueLater(value, callback());
                }
            });
        }
        FutureSequencer sequence = new FutureSequencer(multipleDelayed);
        sequence.start();
        runloop.run();
        assertTrue(sequence.result());
        for (int i=0; i<10; i++) {
            assertEquals(i, (int)multipleDelayed.get(i).result());
        }
    }
    
    @Test
    public void whenASequencedResultIsCancelledFutureSequencerIsAlsoCancelled() {
        final RunLoopSimulator runloop = new RunLoopSimulator();
        List<FutureResult<Integer>> resultsToSequence = new ArrayList<FutureResult<Integer>>();
        for (int i=0; i<2; i++) {
            final Integer value = i;
            resultsToSequence.add(new FutureAction<Integer>() {
                public void run() {
                    runloop.setValueLater(value, callback());
                }
            });
        }
        resultsToSequence.add(new FutureAction<Integer>() {
            public void run() {
                runloop.cancelLater(this);
            }
        });
        FutureSequencer sequence = new FutureSequencer(resultsToSequence);
        sequence.start();
        runloop.run();
        assertTrue(sequence.isCancelled());
        // Everything but the last result is completed.
        for (int i=0; i<2; i++) {
            assertEquals(i, (int)resultsToSequence.get(i).result());
        }
    }
    
    @Test
    public void whenASequencedResultFailsFutureSequencerAlsoFails() {
        final int NUMBER_OF_RESULTS_TO_SEQUENCE_AFTER_FAILURE = 2;
        
        List<FutureResult<Integer>> resultsToSequence = new ArrayList<FutureResult<Integer>>();
        resultsToSequence.add(new FutureAction<Integer>() {
            public void run() {
                throw new NullPointerException();                
            }
        });
        // Results which will not evaluated due to above failure
        for (int i=0; i<NUMBER_OF_RESULTS_TO_SEQUENCE_AFTER_FAILURE; i++) {
            final Integer value = i;
            resultsToSequence.add(new FutureAction<Integer>() {
                public void run() {
                    returnResult(value);
                }
            });
        }
        FutureSequencer sequence = new FutureSequencer(resultsToSequence);
        sequence.start();
        assertTrue(sequence.isFailure());
        // Make sure no result after the first was evaulated
        for (int i=1; i<NUMBER_OF_RESULTS_TO_SEQUENCE_AFTER_FAILURE + 1; i++) {
            assertFalse(resultsToSequence.get(i).isComplete());
        }
    }


}
