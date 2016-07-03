package org.jbourdon.springRestExample.domain;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test of the transaction entity
 */
public class TransactionTest {

    private Transaction transaction;

    @Before
    public void setUp() {
        transaction = new Transaction();
    }

    @Test
    public void has_cycle_should_return_false_for_a_single_transaction() {
        assertFalse(transaction.hasCycle());
    }

    @Test
    public void has_cycle_should_return_true_if_parent_of_self() {
        transaction.setParent(transaction);
        assertTrue(transaction.hasCycle());
    }

    @Test
    public void has_cycle_should_return_true_if_grandparent_of_self() {
        Transaction parentTransaction = new Transaction();
        transaction.setParent(parentTransaction);
        parentTransaction.setParent(transaction);
        assertTrue(transaction.hasCycle());
    }

    @Test
    public void has_cycle_should_return_true_if_grandchild_of_self() {
        Transaction childTransaction = new Transaction();
        transaction.addChild(childTransaction);
        childTransaction.addChild(transaction);
        assertTrue(transaction.hasCycle());
    }

}
