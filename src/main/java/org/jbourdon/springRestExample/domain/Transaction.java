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
 * A Transaction.
 */
@Entity
@Table(name = "transaction")
public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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
        transaction.addChild(this);
    }


    public void addChild(Transaction child) {
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

    public Stream<Transaction> flattened() {
        return Stream.concat(Stream.of(this), children.stream().flatMap(Transaction::flattened));
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
        if (transaction.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, transaction.id);
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
