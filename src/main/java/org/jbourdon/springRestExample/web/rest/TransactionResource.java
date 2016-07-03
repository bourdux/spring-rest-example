package org.jbourdon.springRestExample.web.rest;

import com.fasterxml.jackson.annotation.JsonView;
import org.jbourdon.springRestExample.domain.Transaction;
import org.jbourdon.springRestExample.domain.TransactionView;
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
     * POST  /transaction : Create a new transaction.
     *
     * @param transactionRestWrapper the wrapper representing the transaction to create
     * @return the ResponseEntity with status 201 (Created) and with body the new transaction, or with status 400 (Bad Request) if the transaction has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/transaction",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdateStatus> createTransaction(@RequestBody TransactionRestWrapper transactionRestWrapper) throws URISyntaxException {
        log.debug("REST request to save Transaction : {}", transactionRestWrapper);
        UpdateStatus updateStatus = new UpdateStatus("ok");
        try {
            Transaction result = transactionService.save(transactionRestWrapper);
            return ResponseEntity.created(new URI("//transactionservice/transaction/" + result.getId()))
                    .headers(HeaderUtil.createEntityCreationAlert("transaction", result.getId().toString()))
                    .body(updateStatus);
        } catch (IllegalStateException e) {
            updateStatus = new UpdateStatus("error");
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("transaction", "illegalstate", e.getMessage())).body(updateStatus);
        }

    }

    /**
     * PUT  /transaction/:id : Updates the "id" transaction
     *
     * @param transactionRestWrapper the wrapper representing the transaction to update
     * @return the ResponseEntity with status 200 (OK) and with body the ok status,
     * or with status 400 (Bad Request) if the transaction is not valid (e.g. cause a cycle in the tree)
     * or with status 500 (Internal Server Error) if the transaction couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/transaction/{id}",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UpdateStatus> updateTransaction(@PathVariable Long id, @RequestBody TransactionRestWrapper transactionRestWrapper) throws URISyntaxException {
        log.debug("REST request to update Transaction : {}, id: {}", transactionRestWrapper, id);
        if (id == null) {
            return createTransaction(transactionRestWrapper);
        }
        UpdateStatus updateStatus = new UpdateStatus("ok");
        try {
            Transaction result = transactionService.save(transactionRestWrapper, id);
            return ResponseEntity.ok()
                    .headers(HeaderUtil.createEntityUpdateAlert("transaction", result.getId().toString()))
                    .body(updateStatus);
        } catch (IllegalStateException e) {
            updateStatus = new UpdateStatus("error");
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("transaction", "illegalstate", e.getMessage())).body(updateStatus);
        }

    }

    /**
     * GET  /transaction/:id : get the "id" transaction.
     *
     * @param id the id of the transaction to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the transaction, or with status 404 (Not Found)
     */
    @JsonView(TransactionView.Rest.class)
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
     * DELETE  /transaction/:id : delete the "id" transaction.
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
