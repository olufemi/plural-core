package com.finacial.wealth.api.profiling.models;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**

 @author victorakinola
 */
@Data
public class ChangeAdminPwdReq {

    @NotNull(message = "the field \"newPassword\" is required to fulfill this request!")
    private String newPassword;
    @NotNull(message = "the field \"oldPassword\" is required to fulfill this request!")
    private String oldPassword;
    @NotNull(message = "the field \"requestId\" is required to fulfill this request!")
    private String requestId;
    @NotNull(message = "the field \"otp\" is required to fulfill this request!")
    private int otp;
}
