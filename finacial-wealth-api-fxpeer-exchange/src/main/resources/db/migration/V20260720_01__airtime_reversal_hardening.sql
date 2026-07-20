-- Airtime/VAS reversal hardening.
-- All columns are nullable/backwards-compatible for existing rollback rows.

ALTER TABLE airtime_rollback_log
    ADD COLUMN IF NOT EXISTS fulfilment_status VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS reversal_eligibility VARCHAR(255) NULL,
    ADD COLUMN IF NOT EXISTS provider_status_response VARCHAR(1000) NULL,
    ADD COLUMN IF NOT EXISTS provider_status_checked_at DATETIME(6) NULL,
    ADD COLUMN IF NOT EXISTS processing_claimed_at DATETIME(6) NULL,
    ADD COLUMN IF NOT EXISTS processing_claimed_by VARCHAR(100) NULL;

CREATE INDEX idx_airtime_rollback_log_processing
    ON airtime_rollback_log (status, processing_claimed_at);

CREATE INDEX idx_airtime_rollback_log_process_leg
    ON airtime_rollback_log (process_id, leg_key);
