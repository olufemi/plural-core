-- Airtime/VAS reversal hardening.
-- Startup-safe for existing databases: columns/indexes are created only when missing.

DELIMITER $$

DROP PROCEDURE IF EXISTS add_column_if_missing$$
CREATE PROCEDURE add_column_if_missing(IN p_table VARCHAR(128), IN p_column VARCHAR(128), IN p_ddl TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = p_table
          AND column_name = p_column
    ) THEN
        SET @ddl = p_ddl;
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DROP PROCEDURE IF EXISTS add_index_if_missing$$
CREATE PROCEDURE add_index_if_missing(IN p_table VARCHAR(128), IN p_index VARCHAR(128), IN p_ddl TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = p_table
          AND index_name = p_index
    ) THEN
        SET @ddl = p_ddl;
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

CALL add_column_if_missing('airtime_rollback_log', 'fulfilment_status',
    'ALTER TABLE airtime_rollback_log ADD COLUMN fulfilment_status VARCHAR(100) NULL');
CALL add_column_if_missing('airtime_rollback_log', 'reversal_eligibility',
    'ALTER TABLE airtime_rollback_log ADD COLUMN reversal_eligibility VARCHAR(255) NULL');
CALL add_column_if_missing('airtime_rollback_log', 'provider_status_response',
    'ALTER TABLE airtime_rollback_log ADD COLUMN provider_status_response VARCHAR(1000) NULL');
CALL add_column_if_missing('airtime_rollback_log', 'provider_status_checked_at',
    'ALTER TABLE airtime_rollback_log ADD COLUMN provider_status_checked_at DATETIME(6) NULL');
CALL add_column_if_missing('airtime_rollback_log', 'processing_claimed_at',
    'ALTER TABLE airtime_rollback_log ADD COLUMN processing_claimed_at DATETIME(6) NULL');
CALL add_column_if_missing('airtime_rollback_log', 'processing_claimed_by',
    'ALTER TABLE airtime_rollback_log ADD COLUMN processing_claimed_by VARCHAR(100) NULL');

CALL add_index_if_missing('airtime_rollback_log', 'idx_airtime_rollback_log_processing',
    'CREATE INDEX idx_airtime_rollback_log_processing ON airtime_rollback_log (status, processing_claimed_at)');
CALL add_index_if_missing('airtime_rollback_log', 'idx_airtime_rollback_log_process_leg',
    'CREATE INDEX idx_airtime_rollback_log_process_leg ON airtime_rollback_log (process_id, leg_key)');

DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;
