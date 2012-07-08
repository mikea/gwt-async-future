package com.googlecode.futuretests;

import org.junit.Test;

import com.googlecode.future.ConstantResult;
import com.googlecode.future.ExecutionException;
import com.googlecode.future.Future;
import com.googlecode.future.FutureAction;
import com.googlecode.future.FutureResult;
import com.googlecode.future.IncompleteResultException;

import static org.junit.Assert.*;


/**
 * Test of FutureTask
 * 
 * @author Dean Povey
 *
 */
public class FutureTest {
    
    @Test
    public void canEvaluateSimpleFutureTask() {
        FutureAction<Boolean> simple = new FutureAction<Boolean>() {
            public void run() {
               returnResult(true);             
            }
        };
        assertTrue(simple.result());        
        assertTrue(simple.isComplete());

    }
    
    @Test(expected=AssertionError.class)
    public void canEvaluateSimpleFailureForUncheckedException() throws Exception {
        try {
            FutureAction<Boolean> simpleFailure = new FutureAction<Boolean>() {
                public void run() {
                    failWithException(new AssertionError());            
                }
            };
            assertTrue(simpleFailure.result());
        } catch(ExecutionException e) {
            e.rethrowUncheckedCause();
        }
    }
    
    @SuppressWarnings("serial")
    private static class CheckedException extends Exception {}
    
    @SuppressWarnings("unchecked")
    @Test(expected=CheckedException.class)
    public void canEvaluateSimpleFailureForCheckedException() throws Exception {
        try {
            FutureAction<Boolean> simpleFailure = new FutureAction<Boolean>() {
                public void run() {
                    failWithException(new CheckedException());            
                }
            };
            assertTrue(simpleFailure.result());
        } catch(ExecutionException e) {
            throw e.getCheckedCauseOrRethrow(CheckedException.class);
        }
    }
    
    @Test
    public void whenFutureHasDependencyThatIsRunFirst() {
        final FutureAction<Boolean> first = new FutureAction<Boolean>() {
            public void run() {
                returnResult(true);
            }
        };
        
        FutureAction<Boolean> second = new FutureAction<Boolean>() {
            public void run() {
                returnResult(first.result());
            }
        };
       
        assertTrue(second.result());
    }
    
    @Test
    public void whenResultsAreDelayedCanEvaluateSimpleFutureTask() {        
        final RunLoopSimulator runloop = new RunLoopSimulator();
        FutureAction<Boolean> simple = new FutureAction<Boolean>() {
            public void run() {
               runloop.setValueLater(true, callback()); 
            }
        };
        FutureResult<Boolean> result = new FutureResult<Boolean>();
        simple.addCallback(result);
        assertFalse(simple.isComplete());                
        runloop.run();
        assertTrue(result.result());
    }    
    
    @Test
    public void whenResultsAreDelayedCanEvaluateSimpleFutureTaskWithDependency() {        
        final RunLoopSimulator runloop = new RunLoopSimulator();
        final FutureAction<Boolean> first = new FutureAction<Boolean>() {
            public void run() {
               runloop.setValueLater(true, callback()); 
            }
        };
                
        FutureAction<Boolean> second = new FutureAction<Boolean>() {
            public void run() {
                returnResult(first.result());
            }
        };
       
        FutureResult<Boolean> last = new FutureResult<Boolean>();
        second.addCallback(last);
        assertFalse(first.isComplete());
        assertFalse(second.isComplete());
        runloop.run();
        assertTrue(last.result());
    }
    
    @Test
    public void whenResultsAreDelayedcanEvaluateFutureTaskWithMultipleDependenciesOnSameValue() {        
        final RunLoopSimulator runloop = new RunLoopSimulator();
        final FutureAction<Boolean> first = new FutureAction<Boolean>() {
            public void run() {
               runloop.setValueLater(true, callback()); 
            }
        };
                
        final FutureAction<Boolean> second = new FutureAction<Boolean>() {
            public void run() {
                runloop.setValueLater(first.result(), callback());
            }
        };
        
        final FutureAction<Boolean> third = new FutureAction<Boolean>() {
            public void run() {
                returnResult(second.result() && first.result());
            }
        };        
       
        FutureResult<Boolean> last = new FutureResult<Boolean>();
        third.addCallback(last);
        assertFalse(first.isComplete());
        assertFalse(second.isComplete());
        assertFalse(third.isComplete());
        runloop.run();
        assertTrue(last.result());
    }
    
    @Test
    public void whenResultsAreDelayedcanEvaluateFutureTaskWithMultipleDependenciesOnMultipleValues() {        
        final RunLoopSimulator runloop = new RunLoopSimulator();
        final FutureAction<Boolean> first = new FutureAction<Boolean>() {
            public void run() {
               runloop.setValueLater(true, callback()); 
            }
        };
                
        final FutureAction<Boolean> second = new FutureAction<Boolean>() {
            public void run() {
                runloop.setValueLater(first.result(), callback());
            }
        };
        
        final FutureAction<Boolean> third = new FutureAction<Boolean>() {
            public void run() {
                returnResult(first.result());
            }
        };        
        
        final FutureAction<Boolean> fourth = new FutureAction<Boolean>() {
            public void run() {
                returnResult(second.result());
            }
        };
       
        FutureResult<Boolean> result1 = new FutureResult<Boolean>();
        third.addCallback(result1);
        FutureResult<Boolean> result2 = new FutureResult<Boolean>();
        fourth.addCallback(result2);
        assertFalse(first.isComplete());
        assertFalse(second.isComplete());
        assertFalse(third.isComplete());
        runloop.run();
        assertTrue(result1.result());
        assertTrue(result2.result());
    }
    
    @Test
    public void whenResultsAreDelayedLoopsInUnresolvedDependenciesAreDetected() {        
        final RunLoopSimulator runloop = new RunLoopSimulator();
        final FutureAction<Boolean> first = new FutureAction<Boolean>() {
            public void run() {
               runloop.setValueLater(true, callback()); 
            }
        };
                
        final FutureAction<Boolean> second = new FutureAction<Boolean>() {
            public void run() {
                runloop.setValueLater(first.result(), callback());
            }
        };
        
        final FutureAction<Boolean> third = new FutureAction<Boolean>() {
            public void run() {
                returnResult(first.result());
            }
        };        
        
        final FutureAction<Boolean> fourth = new FutureAction<Boolean>() {
            public void run() {
                returnResult(second.result());
            }
        };
       
        FutureResult<Boolean> result1 = new FutureResult<Boolean>();
        third.addCallback(result1);
        FutureResult<Boolean> result2 = new FutureResult<Boolean>();
        fourth.addCallback(result2);
        assertFalse(first.isComplete());
        assertFalse(second.isComplete());
        assertFalse(third.isComplete());
        runloop.run();
        assertTrue(result1.result());
        assertTrue(result2.result());
    }
    
    @Test(expected=IllegalStateException.class)
    public void cannotSetBothValueAndException() {
        Future<Boolean> future = new FutureResult<Boolean>();
        future.setResult(true);
        future.failWithException(new Exception());
    }
    
    @Test(expected=IllegalStateException.class)
    public void cannotSetValueTwice() {
        Future<Boolean> future = new FutureResult<Boolean>();
        future.setResult(true);
        future.setResult(false);
    }
    
    @Test
    public void whenRunMethodThrowsExceptionItIsTrappedAndSetExceptionCalled() {
        FutureAction<Boolean> future = new FutureAction<Boolean>() {            
            public void run() {
                throw new NullPointerException();
            }
        };
        FutureResult<Boolean> result = new FutureResult<Boolean>();
        future.addCallback(result);
        assertTrue(future.isFailure());
        assertTrue(future.exception() instanceof NullPointerException);
    }
    
    @Test
    public void canCancelActionAndChainedResultsAreAlsoCancelled() {
        Future<Boolean> actionThatNeverCompletes = new FutureAction<Boolean>() {            
            public void run() {
                return;
            }
        };
        
        FutureResult<Boolean> result = new FutureResult<Boolean>();
        actionThatNeverCompletes.addCallback(result);
        actionThatNeverCompletes.cancel();
        assertTrue(result.isCancelled());
        
    }
    
    @Test
    public void canOverideCancel() {
        Future<Boolean> trueIfItSucceedsFalseIfCancelled = new FutureAction<Boolean>() {            
            public void run() {
                return;
            }

            @Override
            public void onCancel() {
                returnResult(true);
            }
            
        };
        
        FutureResult<Boolean> result = new FutureResult<Boolean>();
        trueIfItSucceedsFalseIfCancelled.addCallback(result);
        trueIfItSucceedsFalseIfCancelled.cancel();
        assertFalse(result.isCancelled());
        assertTrue(result.result());
        
    }
    
    @Test
    public void whenActionIsCancelledSubsequentChainedActionsAreNotRun() {
        final RunLoopSimulator runloop = new RunLoopSimulator();
        final FutureAction<Boolean> first = new FutureAction<Boolean>() {
            public void run() {
               cancel(); 
            }
        };
                
        final FutureAction<Boolean> second = new FutureAction<Boolean>() {
            public void run() {
                runloop.setValueLater(first.result(), callback());
            }
        };
        
        final FutureAction<Boolean> third = new FutureAction<Boolean>() {
            public void run() {
                returnResult(first.result());
                throw new AssertionError("Should not be reached");
            }
        };        
        
        final FutureAction<Boolean> fourth = new FutureAction<Boolean>() {
            public void run() {
                returnResult(second.result());
                throw new AssertionError("Should not be reached");
            }
        };
       
        FutureResult<Boolean> result1 = new FutureResult<Boolean>();
        third.addCallback(result1);
        FutureResult<Boolean> result2 = new FutureResult<Boolean>();
        fourth.addCallback(result2);
        assertTrue(first.isCancelled());
        runloop.run();
        assertTrue(result1.isCancelled());
        assertTrue(result2.isCancelled());
        
    }
    
    @Test
    public void canCreatePresentResult() {
        final ConstantResult<Boolean> existing = ConstantResult.constant(true);
        FutureAction<Boolean> result = new FutureAction<Boolean>() {            
            public void run() {
                returnResult(existing.result());
            }
        };
        assertTrue(result.result());
    }
    
    @Test
    public void whenEvalCalledResultEvaluatedButNotReturned() {
        FutureAction<Boolean> action = new FutureAction<Boolean>() {
            public void run() {
                returnResult(true);
            }
        };
        
        // Run action but do not set callback on result
        action.start();        
        assertTrue(action.result());
        
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void uncheckedExceptionInDependencyIsPropogated() {
        final FutureAction<Boolean> failure = new FutureAction<Boolean>() {
            public void run() {                
            }
        };
        
        FutureAction<Boolean> dependent = new FutureAction<Boolean>() {
            public void run() {
                failure.result();
                throw new AssertionError("Unexpected success");
            }
        };
        
        try {
            dependent.result();
        } catch(IncompleteResultException e) {
            // Squash
        }
        failure.failWithException(new UnsupportedOperationException());
        dependent.result();
        
    }
    
    @Test
    public void canCatchException() {
        final FutureAction<Boolean> failure = new FutureAction<Boolean>() {
            public void run() {                
            }
        };
        
        final FutureAction<Boolean> catcher = new FutureAction<Boolean>() {

            public void run() {
                boolean result = false;
                try {
                    failure.result();
                } catch(UnsupportedOperationException e) {
                    result = true;
                }
                returnResult(result);
            }
            
            @Override
            public Throwable catchException(Throwable t) {
                if (t instanceof UnsupportedOperationException) return null;
                return t;
            }
        };
        
        FutureAction<Boolean> dependent = new FutureAction<Boolean>() {
            public void run() {
                returnResult(catcher.result());                
            }
        };
        
        try {
            dependent.result();
        } catch(IncompleteResultException e) {
            // Squash
        }
        failure.failWithException(new UnsupportedOperationException());
        assertTrue(dependent.result());
        
    }
        
}
