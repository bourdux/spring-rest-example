package org.jbourdon.springRestExample.web.rest;

import org.jbourdon.springRestExample.domain.SumResult;
import org.jbourdon.springRestExample.domain.Transaction;
import org.jbourdon.springRestExample.service.TransactionService;
import org.jbourdon.springRestExample.web.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Transaction.
 */

@RestController
@RequestMapping("/transactionservice")
public class TransactionResource {

    private final Logger log = LoggerFactory.getLogger(TransactionResource.class);

    @Inject
    private TransactionService transactionService;

    /**
     * POST  /transactions : Create a new transaction.
     *
     * @param transaction the transaction to create
     * @return the ResponseEntity with status 201 (Created) and with body the new transaction, or with status 400 (Bad Request) if the transaction has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/transaction",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) throws URISyntaxException {
        log.debug("REST request to save Transaction : {}", transaction);
        if (transaction.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("transaction", "idexists", "A new transaction cannot already have an ID")).body(null);
        }
        Transaction result = transactionService.save(transaction);
        return ResponseEntity.created(new URI("//transactionservice/transaction/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert("transaction", result.getId().toString()))
                .body(result);
    }

    /**
     * PUT  /transactions : Updates an existing transaction.
     *
     * @param transaction the transaction to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated transaction,
     * or with status 400 (Bad Request) if the transaction is not valid,
     * or with status 500 (Internal Server Error) if the transaction couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/transaction",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Transaction> updateTransaction(@RequestBody Transaction transaction) throws URISyntaxException {
        log.debug("REST request to update Transaction : {}", transaction);
        if (transaction.getId() == null) {
            return createTransaction(transaction);
        }
        Transaction result = transactionService.save(transaction);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert("transaction", transaction.getId().toString()))
                .body(result);
    }

    /**
     * GET  /transactions/:id : get the "id" transaction.
     *
     * @param id the id of the transaction to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the transaction, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/transaction/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Transaction> getTransaction(@PathVariable Long id) {
        log.debug("REST request to get Transaction : {}", id);
        Transaction transaction = transactionService.findOne(id);
        return Optional.ofNullable(transaction)
                .map(result -> new ResponseEntity<>(
                        result,
                        HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /transactions/:id : delete the "id" transaction.
     *
     * @param id the id of the transaction to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/transaction/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        log.debug("REST request to delete Transaction : {}", id);
        transactionService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("transaction", id.toString())).build();
    }

    @RequestMapping(value = "/types/{type}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Long> getTransactionIdsWithType(@PathVariable String type) {
        log.debug("REST request to get transaction ids of type : {}", type);
        return transactionService.findTransactionIdsOfType(type);
    }

    @RequestMapping(value = "/sum/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SumResult> getSumOfChildrenOfTransaction(@PathVariable Long id) {
        log.debug("REST request to get sum of children of transaction : {}", id);
        Double sum = transactionService.sumChildren(id);
        return new ResponseEntity<>(new SumResult(sum), HttpStatus.OK);
    }
}
