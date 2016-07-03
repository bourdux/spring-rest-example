package org.jbourdon.springRestExample.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A Transaction entity.
 */
@Entity
@Table(name = "transaction")
public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Column(name = "amount")
    @JsonView(TransactionView.Minimal.class)
    private Double amount;

    @Column(name = "type")
    @JsonView(TransactionView.Minimal.class)
    private String type;

    @ManyToOne
    @JsonView(TransactionView.Full.class)
    private Transaction parent;

    @OneToMany(mappedBy = "parent")
    @JsonIgnore
    private Set<Transaction> children = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Transaction getParent() {
        return parent;
    }

    @JsonProperty(value = "parent_id")
    @JsonView(TransactionView.Rest.class)
    public Long getParentId() {
        return Optional.ofNullable(parent).map(Transaction::getId).orElse(null);
    }

    public void setParent(Transaction transaction) {
        this.parent = transaction;
        if (transaction != null) {
            transaction.addChild(this);
        }
    }

    void addChild(Transaction child) {
        this.children.add(child);
        if (child.getParent() == null) {
            child.setParent(this);
        }
    }

    public Set<Transaction> getChildren() {
        return children;
    }

    public void setChildren(Set<Transaction> transactions) {
        this.children = transactions;
    }

    /**
     * Get a flattened stream of this transaction and its children.
     * This method can be used to do further operations such as sum or filtering
     * @return the stream of this transaction and its children
     */
    public Stream<Transaction> flattened() {
        return Stream.concat(Stream.of(this), children.stream().flatMap(Transaction::flattened));
    }

    /**
     * Detect if a transaction creates a cycle in the parent-child transaction tree structure
     * @return true if we detect a cycle
     */
    public boolean hasCycle() {
        /* Detect if transaction is transitively its own parent */
        Transaction parent = this.getParent();
        while (parent != null) {
            if (this.equals(parent)) {
                return true;
            }
            parent = parent.getParent();
        }
        /* Detect if transaction is transitively its own child */
        return this.flattened().filter(this::equals).count() > 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Transaction transaction = (Transaction) o;
        return !(transaction.id == null || id == null) && Objects.equals(id, transaction.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", amount='" + amount + "'" +
                ", type='" + type + "'" +
                ", parent='" + parent + "'" +
                '}';
    }
}
