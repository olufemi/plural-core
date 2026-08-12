CREATE TABLE IF NOT EXISTS bo_saved_view (
  id BIGINT NOT NULL AUTO_INCREMENT,
  admin_user_id BIGINT NOT NULL,
  module_key VARCHAR(80) NOT NULL,
  name VARCHAR(120) NOT NULL,
  filters_json LONGTEXT NOT NULL,
  default_view BIT(1) NOT NULL DEFAULT b'0',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  INDEX idx_bo_saved_view_admin_module (admin_user_id, module_key),
  INDEX idx_bo_saved_view_default (admin_user_id, module_key, default_view)
) ENGINE=InnoDB;
