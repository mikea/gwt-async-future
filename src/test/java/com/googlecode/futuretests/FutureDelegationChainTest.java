package com.googlecode.futuretests;

import org.junit.Test;

import com.googlecode.future.FutureDelegationChain;

import static org.junit.Assert.*;
import static com.googlecode.future.ConstantResult.*;


public class FutureDelegationChainTest {
    
    @SuppressWarnings("unchecked")
    @Test
    public void withNoDelegatesReturnsNull() {
        FutureDelegationChain<Boolean> noDelegateChain = new FutureDelegationChain<Boolean>();
        assertNull(noDelegateChain.result());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void withSingleDelegateReturnsResult() {
        FutureDelegationChain<Boolean> singleDelegateChain = 
            new FutureDelegationChain<Boolean>(constant(true));
        assertTrue(singleDelegateChain.result());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void withMultipleDelegatesReturnsFirstNonNullResult() {
        FutureDelegationChain<Integer> getFirstNonNull = 
            new FutureDelegationChain<Integer>(constant((Integer)null), constant(1), constant(2));
        assertEquals(1, (int)getFirstNonNull.result());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void withCustomIsFinishedReturnsFirstValidResult() {
        FutureDelegationChain<Integer> getFirstValid = 
            new FutureDelegationChain<Integer>(constant(1), constant(2), constant(3)) {
            @Override
            public boolean isResult(Integer result) {
                return result > 2; 
            }
        };
        assertEquals(3, (int)getFirstValid.result());
    }

}
