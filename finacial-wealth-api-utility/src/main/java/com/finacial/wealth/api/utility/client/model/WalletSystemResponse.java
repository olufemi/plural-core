package com.finacial.wealth.api.utility.client.model;

import lombok.Data;

/**
 *
 * @author victorakinola
 */
@Data
public class WalletSystemResponse {

    private Integer statusCode;
    private String description;
    private WalletSystemUserDetails data;

    public WalletSystemResponse() {
    }

    public WalletSystemResponse(Integer statusCode, String description) {
        this.statusCode = statusCode;
        this.description = description;
    }

}
