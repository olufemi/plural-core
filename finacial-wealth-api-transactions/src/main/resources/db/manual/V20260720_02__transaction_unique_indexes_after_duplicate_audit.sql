-- Run these audits before creating production unique indexes.
-- Do not apply the ALTER statements until every query below returns zero rows.

SELECT transaction_id, COUNT(*) duplicate_count
FROM success_debit_log
WHERE transaction_id IS NOT NULL AND transaction_id <> ''
GROUP BY transaction_id
HAVING COUNT(*) > 1;

SELECT reversal_idempotency_key, COUNT(*) duplicate_count
FROM success_debit_log
WHERE reversal_idempotency_key IS NOT NULL AND reversal_idempotency_key <> ''
GROUP BY reversal_idempotency_key
HAVING COUNT(*) > 1;

-- Apply after duplicate cleanup:
ALTER TABLE success_debit_log
    ADD UNIQUE KEY ux_success_debit_log_transaction_id (transaction_id);

ALTER TABLE success_debit_log
    ADD UNIQUE KEY ux_success_debit_log_reversal_idempotency_key (reversal_idempotency_key);
