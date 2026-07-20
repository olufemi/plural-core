package com.finacial.wealth.backoffice.reversal.dto;

public record ManualReversalRequest(
        String notes,
        String reason,
        String evidenceReference,
        String endToEndTransactionId,
        String providerReference,
        String providerStatus,
        String providerStatusEvidence
) {
}
