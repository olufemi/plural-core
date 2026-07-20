-- Transaction reversal hardening.
-- Run before disabling Hibernate DDL auto-update in production.
-- All columns are nullable/backwards-compatible for existing rows.

ALTER TABLE success_debit_log
    ADD COLUMN IF NOT EXISTS fulfilment_status VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS reversal_eligibility VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS reversal_idempotency_key VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS provider_reference VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS provider_status_checked_at DATETIME(6) NULL,
    ADD COLUMN IF NOT EXISTS provider_status_response VARCHAR(2000) NULL,
    ADD COLUMN IF NOT EXISTS processing_claimed_at DATETIME(6) NULL,
    ADD COLUMN IF NOT EXISTS processing_claimed_by VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS manual_override_approval_id BIGINT NULL;

CREATE INDEX idx_success_debit_log_reversal_status
    ON success_debit_log (mark_for_roll_back, reversal_status, processing_claimed_at);

CREATE INDEX idx_success_debit_log_transaction_id
    ON success_debit_log (transaction_id);
