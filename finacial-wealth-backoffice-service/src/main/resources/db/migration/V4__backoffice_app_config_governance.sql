CREATE TABLE IF NOT EXISTS bo_app_config_registry (
  id BIGINT NOT NULL AUTO_INCREMENT,
  config_name VARCHAR(190) NOT NULL,
  owner_service VARCHAR(80) NOT NULL,
  value_type VARCHAR(40) NOT NULL,
  editable BIT(1) NOT NULL DEFAULT b'0',
  is_sensitive BIT(1) NOT NULL DEFAULT b'0',
  validation_regex VARCHAR(500) NULL,
  description VARCHAR(500) NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_bo_app_config_registry_name (config_name)
);

CREATE TABLE IF NOT EXISTS bo_app_config_change_history (
  id BIGINT NOT NULL AUTO_INCREMENT,
  app_config_id BIGINT NULL,
  config_name VARCHAR(190) NOT NULL,
  old_value LONGTEXT NULL,
  new_value LONGTEXT NULL,
  actor_admin_id BIGINT NULL,
  reason VARCHAR(500) NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_bo_app_config_history_name_created (config_name, created_at),
  KEY idx_bo_app_config_history_actor (actor_admin_id)
);

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'CONFIG', 'APP_CONFIG', 'VIEW', 'app_config.view', 'View governed app_config values'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'app_config.view');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'CONFIG', 'APP_CONFIG', 'MANAGE', 'app_config.manage', 'Register and update governed app_config values'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'app_config.manage');

INSERT INTO bo_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM bo_admin_role r
JOIN bo_permission p ON p.code IN ('app_config.view', 'app_config.manage')
WHERE r.name = 'SUPER_ADMIN'
  AND NOT EXISTS (
    SELECT 1
    FROM bo_role_permission rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );
