package com.googlecode.futuretests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.googlecode.future.Future;
import com.googlecode.future.FutureAction;
import com.googlecode.future.FutureChunkedIncrementalAction;
import com.googlecode.future.FutureIncrementalAction;

import static org.junit.Assert.*;

public class FutureIncrementalActionTest {
    
    @Test
    public void whenIncrementalCommandIsADependencyDependantIsResolvedCorrectly() {        
        final FutureIncrementalAction<Integer> dependency = new FutureIncrementalAction<Integer>() {
            int i = 0;
            public void run() {               
                if (++i == 2) returnResult(i);
            }
        };
        
        FutureAction<Boolean> dependent = new FutureAction<Boolean>() {            
            public void run() {
                returnResult(dependency.result() == 2);
            }
        };
        assertTrue(dependent.result());
    }
    
    @Test
    public void canRunASimpleIncrementalCommand() {
        FutureIncrementalAction<Boolean> simple = new FutureIncrementalAction<Boolean>() {
            public void run() {
                returnResult(true);
            }
        };
        assertTrue(simple.result());
    }
    
    @Test
    public void canRunTwoStepIncrementalCommand() {
        FutureIncrementalAction<Integer> simple = new FutureIncrementalAction<Integer>() {
            int i = 0;
            public void run() {
                if (++i == 2) returnResult(i);
            }
        };
        assertEquals(2, (int)simple.result());
    }
    
    @Test
    public void canRunIncrementalCommandWithDependency() {
        final int expectedResult = 2;
        final RunLoopSimulator runloop = new RunLoopSimulator();
        final Future<Integer> dependency = new FutureAction<Integer>() {
            public void run() {
                runloop.setValueLater(expectedResult, callback());
            }
        };
        Future<Integer> simple = new FutureIncrementalAction<Integer>() {
            int i = 0;
            public void run() {
                int nrRuns = dependency.result(); 
                if (++i == nrRuns) returnResult(i);
            }
        };        
        simple.start();
        runloop.run();
        assertEquals(2, (int)simple.result());
    }
    
    @Test
    public void canRunChunkedFutureWithEmptyList() {
        List<String> itemsToCount = new ArrayList<String>();        
        final Future<Integer> itemCounter = new FutureChunkedIncrementalAction<Integer, String>(itemsToCount) {
            int total = 0;
            public void chunk(List<String> chunk) {
                total++;
            }
            @Override
            public void after() { returnResult(total); }
        };
        assertEquals(0, (int)itemCounter.result());
        
    }
    
    @Test
    public void canRunChunkedFuture() {
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
        assertEquals(4, (int)itemCounter.result());
        
    }
    
    @Test
    public void canRunChunkedFutureWithDependency() {
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
        FutureAction<Boolean> resultIsFour = new FutureAction<Boolean>() {            
            public void run() {
                returnResult(itemCounter.result() == 4);
            }
        };
        assertTrue(resultIsFour.result());
        
    }
    
    @Test
    public void canRunChunkedFutureWithStepOf2() {
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
        assertEquals(4, (int)itemCounter.result());
        
    }
    
    @Test
    public void whenRunningChunkedFutureBeforeAndAfterAreRun() {
        List<String> itemsToCount = new ArrayList<String>();
        Collections.addAll(itemsToCount, new String[]{ "a", "list", "of", "strings" });
        
        final Future<Integer> itemCounter = 
            new FutureChunkedIncrementalAction<Integer, String>(itemsToCount) {
            int total;
            @Override public void before() { total = 1; }  
            public void chunk(List<String> chunk) {  total ++; }
            @Override public void after() { returnResult(total); }
        };
        assertEquals(5, (int)itemCounter.result());
        
    }
    
    @Test
    public void whenChunkSizeIsNotAMultipleOfDataLastChunkIsHandledCorrectly() {
        List<String> itemsToCount = new ArrayList<String>();
        Collections.addAll(itemsToCount, new String[]{ "a", "list", "of", "strings", "with one left over" });
        
        final Future<Integer> itemCounter = 
            new FutureChunkedIncrementalAction<Integer, String>(itemsToCount, 2) {
            int total;
            @Override public void before() { total = 1; }  
            public void chunk(List<String> chunk) {  total += 2; }
            @Override public void last(List<String> chunk) {
                assertEquals("with one left over", chunk.get(0));
                total++;
            }  
            @Override public void after() { returnResult(total); }
        };
        assertEquals(6, (int)itemCounter.result());
        
    }
    
    @Test
    public void whenChunkedFutureHasADependencyThisIsHandled() {
        List<String> itemsToCount = new ArrayList<String>();
        final RunLoopSimulator runloop = new RunLoopSimulator();
        final Future<Integer> step = new FutureAction<Integer>("Get step") {
            public void run() {
               runloop.setValueLater(2, callback());               
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
        FutureAction<Boolean> resultIsEight = new FutureAction<Boolean>("resultIsEight") {            
            public void run() {
                returnResult(itemCounter.result() == 8);
            }
        };
        resultIsEight.start();
        runloop.run();
        assertTrue(resultIsEight.result());
    }
    
    @Test
    public void whenDataIsAFutureItIsEvaulatedCorrectly() {        
        final RunLoopSimulator runloop = new RunLoopSimulator();
        final Future<List<String>> getItemsToCount = new FutureAction<List<String>>("Get items to count") {
            public void run() {
                List<String> itemsToCount = new ArrayList<String>();  
                Collections.addAll(itemsToCount, new String[]{ "a", "list", "of", "strings" });
                runloop.setValueLater(itemsToCount, callback());
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
        FutureAction<Boolean> resultIsFour = new FutureAction<Boolean>() {            
            public void run() {              
                returnResult(itemCounter.result() == 4);
            }
        };
        resultIsFour.start();
        runloop.run();
        assertTrue(resultIsFour.result());
        
    }
}
