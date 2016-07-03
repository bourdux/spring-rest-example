package org.jbourdon.springRestExample.service;

import org.jbourdon.springRestExample.domain.Transaction;
import org.jbourdon.springRestExample.repository.TransactionRepository;
import org.jbourdon.springRestExample.web.rest.TransactionRestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionService {

    private final Logger log = LoggerFactory.getLogger(TransactionService.class);

    @Inject
    private TransactionRepository transactionRepository;

    /**
     * Save a transaction.
     *
     * @param transaction the entity to save
     * @return the persisted entity
     */
    public Transaction save(Transaction transaction) {
        log.debug("Request to save transaction : {}", transaction);
        return transactionRepository.save(transaction);
    }

    public Transaction save(TransactionRestWrapper transactionRestWrapper) {
        return this.save(transactionRestWrapper, null);
    }

    /**
     *
     * @param transactionRestWrapper the wrapper representing the transaction we are trying to save
     * @param transactionId the tentative id for the transaction
     * @return the saved transaction
     *
     * @throws IllegalStateException if a cycle is detected for the transaction we are attempting to save
     */
    public Transaction save(TransactionRestWrapper transactionRestWrapper, Long transactionId) {
        log.debug("Request to save transaction with wrapper: {} with id: {}", transactionRestWrapper, transactionId);
        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setAmount(transactionRestWrapper.getAmount());
        transaction.setType(transactionRestWrapper.getType());
        if (transactionRestWrapper.getParentId() != null) {
            transaction.setParent(this.findOne(transactionRestWrapper.getParentId()));
        }
        if (transaction.hasCycle()) {
            throw new IllegalStateException("Cycle detected in the transaction tree");
        }
        return this.save(transaction);
    }

    /**
     * Get one transaction by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Transaction findOne(Long id) {
        log.debug("Request to get transaction : {}", id);
        return transactionRepository.findOne(id);
    }

    /**
     * Get all transaction ids of the same type
     *
     * @param type the transaction type
     * @return the transactions of the given type
     */
    @Transactional(readOnly = true)
    public List<Long> findTransactionIdsOfType(String type) {
        log.debug("Request to get transactions of type : {}", type);
        return transactionRepository.findAllByType(type).stream().map(Transaction::getId).collect(Collectors.toList());
    }

    /**
     * Sum the value of all the children of a transaction
     *
     * @param id the id of the transaction
     * @return the sum of the children of the transaction with the given id
     */
    @Transactional(readOnly = true)
    public Double sumChildren(Long id) {
        log.debug("Request to get sum of transaction: {}", id);
        Transaction transaction = this.findOne(id);
        return transaction.flattened().map(Transaction::getAmount).reduce((a, b) -> a + b).orElse(0d);
    }

    /**
     * Delete a transaction
     *
     * @param id the id of the transaction to delete
     */
    public void delete(Long id) {
        log.debug("Request to delete transaction {}", id);
        transactionRepository.delete(id);
    }

}
