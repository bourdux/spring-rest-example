package org.jbourdon.springRestExample.web.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This POJO is used to return the status after an update operation
 */
public class UpdateStatus {

    @JsonProperty
    private final String status;

    public UpdateStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

}
