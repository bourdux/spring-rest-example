package org.jbourdon.springRestExample.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class representing a transaction SumResult
 */
public class SumResult {

    private final Double sum;

    public SumResult(Double sum) {
        this.sum = sum;
    }

    @JsonProperty(value = "sum")
    public Double getSum() {
        return this.sum;
    }

}
