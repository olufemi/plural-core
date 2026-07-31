INSERT INTO app_config (config_name, config_description, config_value)
SELECT 'investment.redemption.approval-mode',
       'Controls redemption settlement mode: AUTO, MANUAL, or THRESHOLD',
       'AUTO'
WHERE NOT EXISTS (
  SELECT 1 FROM app_config WHERE LOWER(config_name) = LOWER('investment.redemption.approval-mode')
);

INSERT INTO app_config (config_name, config_description, config_value)
SELECT 'investment.redemption.auto-approval-threshold',
       'Maximum redemption amount that can settle automatically when approval mode is THRESHOLD',
       '0'
WHERE NOT EXISTS (
  SELECT 1 FROM app_config WHERE LOWER(config_name) = LOWER('investment.redemption.auto-approval-threshold')
);

INSERT INTO app_config (config_name, config_description, config_value)
SELECT 'investment.redemption.scheduler-enabled',
       'Enables or disables the redemption liquidation scheduler',
       'true'
WHERE NOT EXISTS (
  SELECT 1 FROM app_config WHERE LOWER(config_name) = LOWER('investment.redemption.scheduler-enabled')
);

INSERT INTO app_config (config_name, config_description, config_value)
SELECT 'investment.redemption.scheduler-cron',
       'Cron expression for redemption liquidation scheduler',
       '0 */5 * * * *'
WHERE NOT EXISTS (
  SELECT 1 FROM app_config WHERE LOWER(config_name) = LOWER('investment.redemption.scheduler-cron')
);

INSERT INTO bo_app_config_registry
  (config_name, owner_service, value_type, editable, is_sensitive, validation_regex, description)
SELECT 'investment.redemption.approval-mode',
       'FXPEER',
       'STRING',
       b'1',
       b'0',
       '^(AUTO|MANUAL|THRESHOLD)$',
       'Maker-checker governed redemption settlement mode'
WHERE NOT EXISTS (
  SELECT 1 FROM bo_app_config_registry WHERE LOWER(config_name) = LOWER('investment.redemption.approval-mode')
);

INSERT INTO bo_app_config_registry
  (config_name, owner_service, value_type, editable, is_sensitive, validation_regex, description)
SELECT 'investment.redemption.auto-approval-threshold',
       'FXPEER',
       'NUMBER',
       b'1',
       b'0',
       '^[0-9]+(\\.[0-9]{1,4})?$',
       'Maker-checker governed automatic redemption approval limit'
WHERE NOT EXISTS (
  SELECT 1 FROM bo_app_config_registry WHERE LOWER(config_name) = LOWER('investment.redemption.auto-approval-threshold')
);

INSERT INTO bo_app_config_registry
  (config_name, owner_service, value_type, editable, is_sensitive, validation_regex, description)
SELECT 'investment.redemption.scheduler-enabled',
       'FXPEER',
       'BOOLEAN',
       b'1',
       b'0',
       '^(true|false)$',
       'Maker-checker governed redemption scheduler switch'
WHERE NOT EXISTS (
  SELECT 1 FROM bo_app_config_registry WHERE LOWER(config_name) = LOWER('investment.redemption.scheduler-enabled')
);

INSERT INTO bo_app_config_registry
  (config_name, owner_service, value_type, editable, is_sensitive, validation_regex, description)
SELECT 'investment.redemption.scheduler-cron',
       'FXPEER',
       'STRING',
       b'1',
       b'0',
       NULL,
       'Maker-checker governed redemption scheduler cron'
WHERE NOT EXISTS (
  SELECT 1 FROM bo_app_config_registry WHERE LOWER(config_name) = LOWER('investment.redemption.scheduler-cron')
);

UPDATE bo_app_config_registry
SET owner_service = 'FXPEER',
    value_type = 'STRING',
    editable = b'1',
    is_sensitive = b'0',
    validation_regex = '^(AUTO|MANUAL|THRESHOLD)$',
    description = 'Maker-checker governed redemption settlement mode'
WHERE LOWER(config_name) = LOWER('investment.redemption.approval-mode');

UPDATE bo_app_config_registry
SET owner_service = 'FXPEER',
    value_type = 'NUMBER',
    editable = b'1',
    is_sensitive = b'0',
    validation_regex = '^[0-9]+(\\.[0-9]{1,4})?$',
    description = 'Maker-checker governed automatic redemption approval limit'
WHERE LOWER(config_name) = LOWER('investment.redemption.auto-approval-threshold');

UPDATE bo_app_config_registry
SET owner_service = 'FXPEER',
    value_type = 'BOOLEAN',
    editable = b'1',
    is_sensitive = b'0',
    validation_regex = '^(true|false)$',
    description = 'Maker-checker governed redemption scheduler switch'
WHERE LOWER(config_name) = LOWER('investment.redemption.scheduler-enabled');

UPDATE bo_app_config_registry
SET owner_service = 'FXPEER',
    value_type = 'STRING',
    editable = b'1',
    is_sensitive = b'0',
    validation_regex = NULL,
    description = 'Maker-checker governed redemption scheduler cron'
WHERE LOWER(config_name) = LOWER('investment.redemption.scheduler-cron');

INSERT INTO bo_approval_policy
  (action_code, module, sub_module, description, approval_required, checker_permission, sla_hours, active, created_at, updated_at)
SELECT 'APP_CONFIG_UPDATE', 'CONFIG', 'APP_CONFIG', 'Update governed app_config value', TRUE, 'app_config.manage', 24, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM bo_approval_policy WHERE action_code = 'APP_CONFIG_UPDATE');

UPDATE bo_approval_policy
SET approval_required = TRUE,
    checker_permission = 'app_config.manage',
    updated_at = CURRENT_TIMESTAMP
WHERE action_code = 'APP_CONFIG_UPDATE';
