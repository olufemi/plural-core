package com.finacial.wealth.api.profiling.client.model;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**

 @author victorakinola
 */
@Data
public class CreateUserRequest {

    @NotBlank
    private String productName;
    @NotBlank
    private String password;
    @NotBlank
    private String confPassword;
    @NotBlank
    private String emailAddress;
    @NotBlank
    private String clearanceId;

}
