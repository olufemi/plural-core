CREATE TABLE IF NOT EXISTS bo_backoffice_notification (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  recipient_admin_id BIGINT NOT NULL,
  category VARCHAR(64) NOT NULL,
  severity VARCHAR(16) NOT NULL,
  status VARCHAR(16) NOT NULL,
  title VARCHAR(190) NOT NULL,
  message VARCHAR(500) NOT NULL,
  entity_type VARCHAR(120) NULL,
  entity_ref VARCHAR(190) NULL,
  metadata_json MEDIUMTEXT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  read_at DATETIME NULL,
  CONSTRAINT fk_bo_notification_recipient FOREIGN KEY(recipient_admin_id) REFERENCES bo_admin_user(id),
  INDEX idx_bo_notification_recipient_status (recipient_admin_id, status, created_at),
  INDEX idx_bo_notification_recipient_read (recipient_admin_id, read_at, created_at),
  INDEX idx_bo_notification_entity (entity_type, entity_ref)
);

INSERT INTO bo_permission(module, sub_module, action, code, description)
SELECT 'NOTIFICATION', 'INBOX', 'VIEW', 'notification.view', 'View and mark backoffice notifications'
WHERE NOT EXISTS (
  SELECT 1 FROM bo_permission WHERE code = 'notification.view'
);

INSERT INTO bo_role_permission(role_id, permission_id)
SELECT r.id, p.id
FROM bo_admin_role r
JOIN bo_permission p ON p.code = 'notification.view'
WHERE r.name = 'SUPER_ADMIN'
  AND NOT EXISTS (
    SELECT 1
    FROM bo_role_permission rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );
