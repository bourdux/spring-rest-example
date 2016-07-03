package org.jbourdon.springRestExample.web.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jbourdon.springRestExample.domain.Transaction;

/**
 * Object used to map the specifications to a real Transaction entity
 */
public class TransactionRestWrapper {

    private Double amount;
    private String type;
    @JsonProperty(value = "parent_id")
    private Long parentId;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    @Override
    public String toString() {
        return "TransactionRestWrapper{" +
                "amount=" + amount +
                ", type='" + type + '\'' +
                ", parentId=" + parentId +
                '}';
    }

    static TransactionRestWrapper fromTransaction(Transaction transaction) {
        TransactionRestWrapper transactionRestWrapper = new TransactionRestWrapper();
        transactionRestWrapper.setAmount(transaction.getAmount());
        transactionRestWrapper.setType(transaction.getType());
        if (transaction.getParent() != null) {
            transactionRestWrapper.setParentId(transaction.getParentId());
        }
        return transactionRestWrapper;
    }

}
