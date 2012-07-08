package com.googlecode.future;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;

public class GwtTstIncrementalFuture extends AsyncFutureGWTTestCase {
    
    public void testWhenIncrementalCommandIsADependencyDependantIsResolvedCorrectly() {        
        final Future<Integer> dependency = new FutureIncrementalAction<Integer>() {
            int i = 0;
            public void run() {               
                if (++i == 2) returnResult(i);
            }
        };
        
        final Future<Boolean> dependant = new FutureAction<Boolean>() {            
            public void run() {
                returnResult(dependency.result() == 2);
            }
        };
        new AutoFutureAction<Void>() {
            public void run() {
                assertTrue(dependant.result());
                finishTest();
            }
        };        
        delayTestFinish(30000);
        
    }
    
    public void testCanRunASimpleIncrementalCommand() {
        final FutureIncrementalAction<Boolean> simple = new FutureIncrementalAction<Boolean>("incremental") {
            public void run() {                
                returnResult(true);
            }
        };
        new AutoFutureAction<Void>("resultChecker") {
            public void run() {
                assertTrue(simple.result());                
                finishTest();
            }
        };        
        delayTestFinish(30000);        
    }
    
    public void testCanRunTwoStepIncrementalCommand() {
        final FutureIncrementalAction<Integer> simple = new FutureIncrementalAction<Integer>() {
            int i = 0;
            public void run() {
                if (++i == 2) returnResult(i);
            }
        };
        new AutoFutureAction<Void>() {
            public void run() {
                assertEquals(2, (int)simple.result());
                finishTest();
            }
        };        
        delayTestFinish(30000);        
    }
    
    public void testCanRunIncrementalCommandWithDependency() {
        final int expectedResult = 2;
        final Future<Integer> dependency = new FutureAction<Integer>() {
            public void run() {
                returnResult(expectedResult);
            }
        };
        final Future<Integer> simple = new FutureIncrementalAction<Integer>() {
            int i = 0;
            public void run() {
                int nrRuns = dependency.result(); 
                if (++i == nrRuns) returnResult(i);
            }
        };        
        new AutoFutureAction<Void>() {
            public void run() {
                assertEquals(2, (int)simple.result());
                finishTest();
            }
        };        
        delayTestFinish(30000);
    }
    
    public void testCanRunChunkedFutureWithEmptyList() {
        List<String> itemsToCount = new ArrayList<String>();        
        final Future<Integer> itemCounter = new FutureChunkedIncrementalAction<Integer, String>(itemsToCount) {
            int total = 0;
            public void chunk(List<String> chunk) {                
                total++;
            }
            @Override
            public void after() { returnResult(total); }
        };
        
        new AutoFutureAction<Void>() {
            public void run() {
                final Integer nrItems = itemCounter.result();
                assertEquals(0, (int)nrItems);
                finishTest();
            }
        };        
        delayTestFinish(30000);
    }
    
    public void testCanRunChunkedFuture() {
        List<String> itemsToCount = new ArrayList<String>();
        Collections.addAll(itemsToCount, new String[]{ "a", "list", "of", "strings" });
        final Future<Integer> itemCounter = new FutureChunkedIncrementalAction<Integer, String>(itemsToCount) {
            int total = 0;
            public void chunk(List<String> chunk) {
                total++;
            }
            @Override
            public void after() { returnResult(total); }
        };
        
        new AutoFutureAction<Void>() {
            public void run() {
                assertEquals(4, (int)itemCounter.result());
                finishTest();
            }
        };        
        delayTestFinish(30000);
        
    }
    
    public void testCanRunChunkedFutureWithDependency() {
        List<String> itemsToCount = new ArrayList<String>();
        Collections.addAll(itemsToCount, new String[]{ "a", "list", "of", "strings" });
        final Future<Integer> itemCounter = new FutureChunkedIncrementalAction<Integer, String>(itemsToCount) {
            int total = 0;
            public void chunk(List<String> chunk) {
                total++;
            }
            @Override
            public void after() { returnResult(total); }
        };
        
        final FutureAction<Boolean> resultIsFour = new FutureAction<Boolean>() {            
            public void run() {
                returnResult(itemCounter.result() == 4);
            }
        };
        
        new AutoFutureAction<Void>() {
            public void run() {
                assertTrue(resultIsFour.result());
                finishTest();
            }
        };        
        delayTestFinish(30000);
        
    }
    
    public void testCanRunChunkedFutureWithStepOf2() {
        List<String> itemsToCount = new ArrayList<String>();
        Collections.addAll(itemsToCount, new String[]{ "a", "list", "of", "strings" });
        final Future<Integer> itemCounter = 
            new FutureChunkedIncrementalAction<Integer, String>(itemsToCount, 2) {
            int total = 0;
            public void chunk(List<String> chunk) {
                total += 2;
            }
            @Override
            public void after() { returnResult(total); }
        };
        
        new AutoFutureAction<Void>() {
            public void run() {
                assertEquals(4, (int)itemCounter.result());
                finishTest();
            }
        };        
        delayTestFinish(30000);
        
    }
    
    public void testWhenRunningChunkedFutureBeforeAndAfterAreRun() {
        List<String> itemsToCount = new ArrayList<String>();
        Collections.addAll(itemsToCount, new String[]{ "a", "list", "of", "strings" });
        
        final Future<Integer> itemCounter = 
            new FutureChunkedIncrementalAction<Integer, String>(itemsToCount) {
            int total;
            @Override public void before() { total = 0; }  
            public void chunk(List<String> chunk) {  total ++; }
            @Override public void after() { returnResult(total); }
        };
        
        new AutoFutureAction<Void>() {
            public void run() {
                assertEquals(4, (int)itemCounter.result());
                finishTest();
            }
        };        
        delayTestFinish(30000);
        
    }
    
    public void testWhenChunkSizeIsNotAMultipleOfDataLastChunkIsHandledCorrectly() {
        List<String> itemsToCount = new ArrayList<String>();
        Collections.addAll(itemsToCount, new String[]{ "a", "list", "of", "strings", "with one left over" });
        
        final Future<Integer> itemCounter = 
            new FutureChunkedIncrementalAction<Integer, String>(itemsToCount, 2) {
            int total;
            @Override public void before() { total = 0; }  
            public void chunk(List<String> chunk) {  total += 2; }
            @Override public void last(List<String> chunk) {
                assertEquals("with one left over", chunk.get(0));
                total++;
            }  
            @Override public void after() { returnResult(total); }
        };
        
        new AutoFutureAction<Void>() {
            public void run() {
                assertEquals(5, (int)itemCounter.result());
                finishTest();
            }
        };        
        delayTestFinish(30000);
        
    }
    
    public void testWhenChunkedFutureHasADependencyThisIsHandled() {
        List<String> itemsToCount = new ArrayList<String>();
        final Future<Integer> step = new FutureAction<Integer>("Get step") {
            public void run() {
               Command cmd = new Command() {
                public void execute() {
                    returnResult(2);                    
                }                   
               };
               DeferredCommand.addCommand(cmd);
            }            
        };
        Collections.addAll(itemsToCount, new String[]{ "a", "list", "of", "strings" });
        final Future<Integer> itemCounter = new FutureChunkedIncrementalAction<Integer, String>(
                "Count items and multiply by step", itemsToCount) {
            int total = 0;
            public void chunk(List<String> chunk) {                
                total += step.result();
            }
            @Override
            public void after() { returnResult(total); }
        };
        final FutureAction<Boolean> resultIsEight = new FutureAction<Boolean>("resultIsEight") {            
            public void run() {
                returnResult(itemCounter.result() == 8);
            }
        };        
        new AutoFutureAction<Void>() {
            public void run() {
                assertTrue(resultIsEight.result());
                finishTest();
            }
        };        
        delayTestFinish(30000);
    }
    
    public void testWhenDataIsAFutureItIsEvaulatedCorrectly() {        
        final Future<List<String>> getItemsToCount = new FutureAction<List<String>>("Get items to count") {
            public void run() {
                List<String> itemsToCount = new ArrayList<String>();  
                Collections.addAll(itemsToCount, new String[]{ "a", "list", "of", "strings" });
                returnResult(itemsToCount);
            }
        };
        
        final Future<Integer> itemCounter = 
            new FutureChunkedIncrementalAction<Integer, String>("Item counter", getItemsToCount, 1) {
            int total = 0;
            public void chunk(List<String> chunk) {
                total++;
            }
            @Override
            public void after() { returnResult(total); }
        };        
        new AutoFutureAction<Void>() {
            public void run() {
                assertEquals(4, (int)itemCounter.result());
                finishTest();
            }
        };        
        delayTestFinish(30000);
        
    }
}
