package com.finacial.wealth.api.profiling.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**

 @author victorakinola
 */
@Data
@AllArgsConstructor
public class WalletUserRequest {

    private String walletNo;
    private String productCode;
    private String token;

}
