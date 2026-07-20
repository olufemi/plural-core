-- Run these audits before creating production unique indexes.
-- Do not apply the ALTER statements until every query below returns zero rows.

SELECT process_id, leg_key, COUNT(*) duplicate_count
FROM airtime_rollback_log
WHERE process_id IS NOT NULL AND process_id <> ''
  AND leg_key IS NOT NULL AND leg_key <> ''
GROUP BY process_id, leg_key
HAVING COUNT(*) > 1;

SELECT rollback_transaction_id, COUNT(*) duplicate_count
FROM airtime_rollback_log
WHERE rollback_transaction_id IS NOT NULL AND rollback_transaction_id <> ''
GROUP BY rollback_transaction_id
HAVING COUNT(*) > 1;

-- Apply after duplicate cleanup:
ALTER TABLE airtime_rollback_log
    ADD UNIQUE KEY ux_airtime_rollback_log_process_leg (process_id, leg_key);

ALTER TABLE airtime_rollback_log
    ADD UNIQUE KEY ux_airtime_rollback_log_rollback_txn (rollback_transaction_id);
