CREATE TABLE bo_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  module VARCHAR(64) NOT NULL,
  sub_module VARCHAR(64) NOT NULL,
  action VARCHAR(64) NOT NULL,
  code VARCHAR(128) NOT NULL UNIQUE,
  description VARCHAR(255) NULL
);

CREATE TABLE bo_role_permission (
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY(role_id, permission_id),
  CONSTRAINT fk_bo_role_permission_role FOREIGN KEY(role_id) REFERENCES bo_admin_role(id),
  CONSTRAINT fk_bo_role_permission_permission FOREIGN KEY(permission_id) REFERENCES bo_permission(id)
);

CREATE TABLE bo_approval_request (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  module VARCHAR(32) NOT NULL,
  sub_module VARCHAR(32) NOT NULL,
  entity_type VARCHAR(32) NOT NULL,
  entity_ref VARCHAR(128) NOT NULL,
  action_type VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL,
  maker_admin_id BIGINT NULL,
  checker_admin_id BIGINT NULL,
  current_assignee_admin_id BIGINT NULL,
  requester_email VARCHAR(190) NULL,
  rejection_reason VARCHAR(255) NULL,
  remediation_notes VARCHAR(255) NULL,
  payload_json MEDIUMTEXT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  submitted_at DATETIME NULL,
  approved_at DATETIME NULL,
  rejected_at DATETIME NULL,
  resubmitted_at DATETIME NULL,
  UNIQUE KEY uk_bo_approval_entity (entity_type, entity_ref),
  INDEX idx_bo_approval_status (status, created_at),
  INDEX idx_bo_approval_module (module, sub_module, status)
);

CREATE TABLE bo_approval_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  approval_request_id BIGINT NOT NULL,
  event_type VARCHAR(32) NOT NULL,
  actor_admin_id BIGINT NULL,
  notes VARCHAR(255) NULL,
  metadata_json MEDIUMTEXT NULL,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_bo_approval_event_request FOREIGN KEY(approval_request_id) REFERENCES bo_approval_request(id),
  INDEX idx_bo_approval_event_request (approval_request_id, created_at)
);

INSERT INTO bo_permission(module, sub_module, action, code, description) VALUES
 ('INVESTMENT', 'LIQUIDATION', 'VIEW', 'investment.liquidation.view', 'View liquidation approval queues'),
 ('INVESTMENT', 'LIQUIDATION', 'APPROVE', 'investment.liquidation.approve', 'Approve or reject liquidation approvals'),
 ('INVESTMENT', 'LIQUIDATION', 'REMEDIATE', 'investment.liquidation.remediate', 'Remediate and resubmit liquidation approvals'),
 ('INVESTMENT', 'ORDER', 'VIEW', 'investment.order.view', 'View investment and top-up order queues'),
 ('CUSTOMER', 'PROFILE', 'VIEW', 'customer.profile.view', 'View customer profile and investment summaries'),
 ('APPROVAL', 'INBOX', 'VIEW', 'approval.inbox.view', 'View approval inbox'),
 ('ROLE', 'MANAGEMENT', 'MANAGE', 'role.manage', 'Create roles and manage permissions'),
 ('USER', 'MANAGEMENT', 'MANAGE', 'user.manage', 'Create and manage backoffice users'),
 ('AUDIT', 'LOG', 'VIEW', 'audit.view', 'View audit logs');

INSERT INTO bo_role_permission(role_id, permission_id)
SELECT r.id, p.id
FROM bo_admin_role r
JOIN bo_permission p
WHERE r.name = 'SUPER_ADMIN';
