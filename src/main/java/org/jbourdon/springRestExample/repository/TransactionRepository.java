package org.jbourdon.springRestExample.repository;

import org.jbourdon.springRestExample.domain.Transaction;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Transaction entity.
 */
@SuppressWarnings("unused")
public interface TransactionRepository extends JpaRepository<Transaction,Long> {

    List<Transaction> findAllByType(String type);
}
