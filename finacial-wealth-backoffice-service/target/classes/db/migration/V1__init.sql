CREATE TABLE bo_admin_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(190) NOT NULL UNIQUE,
  full_name VARCHAR(190) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  status VARCHAR(32) NOT NULL,
  failed_attempts INT NOT NULL DEFAULT 0,
  locked_until DATETIME NULL,
  last_login_at DATETIME NULL,
  mfa_enabled TINYINT(1) NOT NULL DEFAULT 0,
  totp_secret_enc TEXT NULL,
  totp_secret_iv VARCHAR(64) NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE TABLE bo_admin_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL UNIQUE
);

CREATE TABLE bo_admin_user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY(user_id, role_id),
  CONSTRAINT fk_bo_ur_user FOREIGN KEY(user_id) REFERENCES bo_admin_user(id),
  CONSTRAINT fk_bo_ur_role FOREIGN KEY(role_id) REFERENCES bo_admin_role(id)
);

CREATE TABLE bo_refresh_token (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  admin_user_id BIGINT NOT NULL,
  token_hash VARCHAR(255) NOT NULL,
  expires_at DATETIME NOT NULL,
  revoked TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_bo_rt_user FOREIGN KEY(admin_user_id) REFERENCES bo_admin_user(id),
  INDEX idx_bo_rt_user(admin_user_id),
  INDEX idx_bo_rt_hash(token_hash)
);

CREATE TABLE bo_audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  admin_user_id BIGINT NULL,
  action VARCHAR(120) NOT NULL,
  entity_type VARCHAR(120) NULL,
  entity_id VARCHAR(120) NULL,
  request_id VARCHAR(80) NULL,
  ip VARCHAR(64) NULL,
  user_agent VARCHAR(255) NULL,
  reason VARCHAR(255) NULL,
  before_json MEDIUMTEXT NULL,
  after_json MEDIUMTEXT NULL,
  created_at DATETIME NOT NULL,
  INDEX idx_bo_audit_created(created_at),
  INDEX idx_bo_audit_action(action)
);

INSERT INTO bo_admin_role(name) VALUES
 ('SUPER_ADMIN'),
 ('ADMIN'),
 ('OPERATIONS'),
 ('FINANCE');
