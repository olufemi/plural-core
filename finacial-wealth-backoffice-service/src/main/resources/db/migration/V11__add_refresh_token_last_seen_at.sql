SET @add_last_seen_at_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE bo_refresh_token ADD COLUMN last_seen_at DATETIME NULL',
    'SELECT 1'
  )
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'bo_refresh_token'
    AND column_name = 'last_seen_at'
);

PREPARE add_last_seen_at_stmt FROM @add_last_seen_at_sql;
EXECUTE add_last_seen_at_stmt;
DEALLOCATE PREPARE add_last_seen_at_stmt;

UPDATE bo_refresh_token
SET last_seen_at = created_at
WHERE last_seen_at IS NULL;
