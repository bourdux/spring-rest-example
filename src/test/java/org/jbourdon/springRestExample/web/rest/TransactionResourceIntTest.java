package org.jbourdon.springRestExample.web.rest;

import org.jbourdon.springRestExample.TransactionsApp;
import org.jbourdon.springRestExample.domain.Transaction;
import org.jbourdon.springRestExample.repository.TransactionRepository;
import org.jbourdon.springRestExample.service.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the TransactionResource REST controller.
 *
 * @see TransactionResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TransactionsApp.class)
@WebAppConfiguration
@IntegrationTest
public class TransactionResourceIntTest {

    private static final Double DEFAULT_AMOUNT = 1D;
    private static final Double UPDATED_AMOUNT = 2D;
    private static final Double ALTERNATIVE_AMOUNT = 3D;
    private static final String DEFAULT_TYPE = "AAAAA";
    private static final String UPDATED_TYPE = "BBBBB";
    private static final String ALTERNATIVE_TYPE = "CCCCC";

    @Inject
    private TransactionRepository transactionRepository;

    @Inject
    private TransactionService transactionService;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restTransactionMockMvc;

    private Transaction transaction;
    private Transaction childTransaction1;
    private Transaction childTransaction2;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        TransactionResource transactionResource = new TransactionResource();
        ReflectionTestUtils.setField(transactionResource, "transactionService", transactionService);
        this.restTransactionMockMvc = MockMvcBuilders.standaloneSetup(transactionResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        transaction = new Transaction();
        transaction.setAmount(DEFAULT_AMOUNT);
        transaction.setType(DEFAULT_TYPE);
        childTransaction1 = new Transaction();
        childTransaction1.setAmount(DEFAULT_AMOUNT);
        childTransaction1.setType(DEFAULT_TYPE);
        childTransaction1.setParent(transaction);
        childTransaction2 = new Transaction();
        childTransaction2.setAmount(ALTERNATIVE_AMOUNT);
        childTransaction2.setType(ALTERNATIVE_TYPE);
        childTransaction2.setParent(transaction);
    }

    @Test
    @Transactional
    public void createTransaction() throws Exception {
        int databaseSizeBeforeCreate = transactionRepository.findAll().size();

        // Create the Transaction
        restTransactionMockMvc.perform(post("/transactionservice/transaction")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(TransactionRestWrapper.fromTransaction(transaction))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ok"));

        // Validate the Transaction in the database
        List<Transaction> transactions = transactionRepository.findAll();
        assertThat(transactions).hasSize(databaseSizeBeforeCreate + 1);
        Transaction testTransaction = transactions.get(transactions.size() - 1);
        assertThat(testTransaction.getAmount()).isEqualTo(DEFAULT_AMOUNT);
        assertThat(testTransaction.getType()).isEqualTo(DEFAULT_TYPE);
    }

    @Test
    @Transactional
    public void getTransaction() throws Exception {
        // Initialize the database
        transactionService.save(transaction);
        transactionService.save(childTransaction1);
        // Get the transaction
        restTransactionMockMvc.perform(get("/transactionservice/transaction/{id}", transaction.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.amount").value(DEFAULT_AMOUNT))
                .andExpect(jsonPath("$.type").value(DEFAULT_TYPE));

        restTransactionMockMvc.perform(get("/transactionservice/transaction/{id}", childTransaction1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.amount").value(DEFAULT_AMOUNT))
                .andExpect(jsonPath("$.type").value(DEFAULT_TYPE))
                .andExpect(jsonPath("$.parent_id").value(transaction.getId().intValue()));

    }

    @Test
    @Transactional
    public void getNonExistingTransaction() throws Exception {
        // Get the transaction
        restTransactionMockMvc.perform(get("/transactionservice/transaction/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateTransaction() throws Exception {
        // Initialize the database
        transactionService.save(transaction);
        int databaseSizeBeforeUpdate = transactionRepository.findAll().size();

        // Update the transaction
        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setId(transaction.getId());
        updatedTransaction.setAmount(UPDATED_AMOUNT);
        updatedTransaction.setType(UPDATED_TYPE);

        restTransactionMockMvc.perform(put("/transactionservice/transaction/" + updatedTransaction.getId())
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(TransactionRestWrapper.fromTransaction(updatedTransaction)))
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));

        // Validate the Transaction in the database
        List<Transaction> transactions = transactionRepository.findAll();
        assertThat(transactions).hasSize(databaseSizeBeforeUpdate);
        Transaction testTransaction = transactions.get(transactions.size() - 1);
        assertThat(testTransaction.getAmount()).isEqualTo(UPDATED_AMOUNT);
        assertThat(testTransaction.getType()).isEqualTo(UPDATED_TYPE);
    }

    @Test
    @Transactional
    public void deleteTransaction() throws Exception {
        // Initialize the database
        transactionService.save(transaction);
        int databaseSizeBeforeDelete = transactionRepository.findAll().size();

        // Get the transaction
        restTransactionMockMvc.perform(delete("/transactionservice/transaction/{id}", transaction.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Transaction> transactions = transactionRepository.findAll();
        assertThat(transactions).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void transactionType() throws Exception {
        // Initialize the database
        transactionService.save(transaction);
        transactionService.save(childTransaction1);
        transactionService.save(childTransaction2);

        // Get the transaction ids list
        restTransactionMockMvc.perform(get("/transactionservice/types/{type}", DEFAULT_TYPE)
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @Transactional
    public void sumChildrenTransaction() throws Exception {
        // Initialize the database
        transactionService.save(transaction);
        transactionService.save(childTransaction1);
        transactionService.save(childTransaction2);

        restTransactionMockMvc.perform(get("/transactionservice/sum/" + transaction.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.sum").value(transaction.getAmount() + childTransaction1.getAmount() + childTransaction2.getAmount()));
    }

}
