INSERT INTO app_config (config_name, config_description, config_value)
SELECT 'investment.redemption.auto-approval-threshold.NGN',
       'Maximum NGN redemption amount that can settle automatically when approval mode is THRESHOLD',
       '0'
WHERE NOT EXISTS (
  SELECT 1 FROM app_config WHERE LOWER(config_name) = LOWER('investment.redemption.auto-approval-threshold.NGN')
);

INSERT INTO app_config (config_name, config_description, config_value)
SELECT 'investment.redemption.auto-approval-threshold.CAD',
       'Maximum CAD redemption amount that can settle automatically when approval mode is THRESHOLD',
       '0'
WHERE NOT EXISTS (
  SELECT 1 FROM app_config WHERE LOWER(config_name) = LOWER('investment.redemption.auto-approval-threshold.CAD')
);

INSERT INTO bo_app_config_registry
  (config_name, owner_service, value_type, editable, is_sensitive, validation_regex, description)
SELECT 'investment.redemption.auto-approval-threshold.NGN',
       'FXPEER',
       'NUMBER',
       b'1',
       b'0',
       '^[0-9]+(\\.[0-9]{1,4})?$',
       'Maker-checker governed automatic NGN redemption approval limit'
WHERE NOT EXISTS (
  SELECT 1 FROM bo_app_config_registry WHERE LOWER(config_name) = LOWER('investment.redemption.auto-approval-threshold.NGN')
);

INSERT INTO bo_app_config_registry
  (config_name, owner_service, value_type, editable, is_sensitive, validation_regex, description)
SELECT 'investment.redemption.auto-approval-threshold.CAD',
       'FXPEER',
       'NUMBER',
       b'1',
       b'0',
       '^[0-9]+(\\.[0-9]{1,4})?$',
       'Maker-checker governed automatic CAD redemption approval limit'
WHERE NOT EXISTS (
  SELECT 1 FROM bo_app_config_registry WHERE LOWER(config_name) = LOWER('investment.redemption.auto-approval-threshold.CAD')
);

UPDATE bo_app_config_registry
SET owner_service = 'FXPEER',
    value_type = 'NUMBER',
    editable = b'1',
    is_sensitive = b'0',
    validation_regex = '^[0-9]+(\\.[0-9]{1,4})?$',
    description = 'Maker-checker governed automatic NGN redemption approval limit'
WHERE LOWER(config_name) = LOWER('investment.redemption.auto-approval-threshold.NGN');

UPDATE bo_app_config_registry
SET owner_service = 'FXPEER',
    value_type = 'NUMBER',
    editable = b'1',
    is_sensitive = b'0',
    validation_regex = '^[0-9]+(\\.[0-9]{1,4})?$',
    description = 'Maker-checker governed automatic CAD redemption approval limit'
WHERE LOWER(config_name) = LOWER('investment.redemption.auto-approval-threshold.CAD');
