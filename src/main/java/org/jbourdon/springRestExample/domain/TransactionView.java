package org.jbourdon.springRestExample.domain;

/**
 * View of the transaction entity for transaction REST service
 */
public class TransactionView {

    interface Minimal {}
    interface Full extends Minimal {}
    public interface Rest extends Minimal {}
}
