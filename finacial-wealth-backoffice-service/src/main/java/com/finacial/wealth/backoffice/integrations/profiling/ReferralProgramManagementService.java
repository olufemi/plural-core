package com.finacial.wealth.backoffice.integrations.profiling;

import com.finacial.wealth.backoffice.model.ApiResponseModel;
import com.finacial.wealth.backoffice.referral.model.CreateReferralProgramRequest;
import com.finacial.wealth.backoffice.referral.model.ReferralProgramAuditDto;
import com.finacial.wealth.backoffice.referral.model.ReferralProgramDto;
import com.finacial.wealth.backoffice.referral.model.UpdateReferralProgramRequest;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReferralProgramManagementService {

    private final ReferralProgramManagementClient client;

    public ReferralProgramManagementService(ReferralProgramManagementClient client) {
        this.client = client;
    }

    public ApiResponseModel createReferralProgram(CreateReferralProgramRequest payload, String userId) {
        return ok(client.createReferralProgram(payload, userId));
    }

    public ApiResponseModel updateReferralProgram(Long id, UpdateReferralProgramRequest payload, String userId) {
        return ok(client.updateReferralProgram(id, payload, userId));
    }

    public ApiResponseModel activateReferralProgram(Long id, String userId) {
        return ok(client.activateReferralProgram(id, userId));
    }

    public ApiResponseModel pauseReferralProgram(Long id, String userId) {
        return ok(client.pauseReferralProgram(id, userId));
    }

    public ApiResponseModel endReferralProgram(Long id, String userId) {
        return ok(client.endReferralProgram(id, userId));
    }

    public ApiResponseModel listReferralPrograms(String productType) {
        List<ReferralProgramDto> list = client.listReferralPrograms(productType);
        return ok(list);
    }

    public ApiResponseModel getReferralProgram(Long id) {
        return ok(client.getReferralProgram(id));
    }

    public ApiResponseModel getReferralProgramAudit(Long id) {
        List<ReferralProgramAuditDto> list = client.getReferralProgramAudit(id);
        return ok(list);
    }

    public ApiResponseModel getActiveReferralProgram(String productType) {
        return ok(client.getActiveReferralProgram(productType));
    }

    private ApiResponseModel ok(Object data) {
        ApiResponseModel res = new ApiResponseModel();
        res.setStatusCode(200);
        res.setDescription("OK");
        res.setData(data);
        return res;
    }
}
