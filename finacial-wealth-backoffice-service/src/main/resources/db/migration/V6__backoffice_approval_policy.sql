CREATE TABLE IF NOT EXISTS bo_approval_policy (
  action_code VARCHAR(128) NOT NULL PRIMARY KEY,
  module VARCHAR(64) NOT NULL,
  sub_module VARCHAR(64) NOT NULL,
  description VARCHAR(255) NULL,
  approval_required BOOLEAN NOT NULL DEFAULT FALSE,
  threshold_amount DECIMAL(19,4) NULL,
  threshold_currency VARCHAR(8) NULL,
  checker_permission VARCHAR(128) NULL,
  sla_hours INT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  updated_by_admin_id BIGINT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_bo_approval_policy_module (module, sub_module),
  KEY idx_bo_approval_policy_required (approval_required, active)
);

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'APPROVAL', 'POLICY', 'VIEW', 'approval.policy.view', 'View maker-checker approval policies'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'approval.policy.view');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'APPROVAL', 'POLICY', 'MANAGE', 'approval.policy.manage', 'Manage maker-checker approval policies'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'approval.policy.manage');

INSERT INTO bo_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM bo_admin_role r
JOIN bo_permission p ON p.code IN ('approval.policy.view', 'approval.policy.manage')
WHERE r.name = 'SUPER_ADMIN'
  AND NOT EXISTS (
    SELECT 1
    FROM bo_role_permission rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );

INSERT INTO bo_approval_policy
  (action_code, module, sub_module, description, approval_required, checker_permission, sla_hours, active)
SELECT 'INVESTMENT_PRODUCT_CREATE', 'INVESTMENT', 'PRODUCT', 'Create investment product', TRUE, 'investment.product.approve', 24, TRUE
WHERE NOT EXISTS (SELECT 1 FROM bo_approval_policy WHERE action_code = 'INVESTMENT_PRODUCT_CREATE');

INSERT INTO bo_approval_policy
  (action_code, module, sub_module, description, approval_required, checker_permission, sla_hours, active)
SELECT 'INVESTMENT_PRODUCT_UPDATE', 'INVESTMENT', 'PRODUCT', 'Update investment product', TRUE, 'investment.product.approve', 24, TRUE
WHERE NOT EXISTS (SELECT 1 FROM bo_approval_policy WHERE action_code = 'INVESTMENT_PRODUCT_UPDATE');

INSERT INTO bo_approval_policy
  (action_code, module, sub_module, description, approval_required, checker_permission, sla_hours, active)
SELECT 'REFERRAL_PROGRAM_CHANGE', 'REFERRAL', 'PROGRAM', 'Create or update referral program', FALSE, 'referral.program.manage', 24, TRUE
WHERE NOT EXISTS (SELECT 1 FROM bo_approval_policy WHERE action_code = 'REFERRAL_PROGRAM_CHANGE');

INSERT INTO bo_approval_policy
  (action_code, module, sub_module, description, approval_required, checker_permission, sla_hours, active)
SELECT 'CAMPAIGN_CHANGE', 'CAMPAIGN', 'MARKETING', 'Create, update, stop, cancel, or restart campaign', FALSE, 'campaign.approve', 24, TRUE
WHERE NOT EXISTS (SELECT 1 FROM bo_approval_policy WHERE action_code = 'CAMPAIGN_CHANGE');

INSERT INTO bo_approval_policy
  (action_code, module, sub_module, description, approval_required, checker_permission, sla_hours, active)
SELECT 'CUSTOMER_BLOCK_UNBLOCK', 'CUSTOMER', 'PROFILE', 'Block or unblock customer', FALSE, 'customer.profile.manage', 24, TRUE
WHERE NOT EXISTS (SELECT 1 FROM bo_approval_policy WHERE action_code = 'CUSTOMER_BLOCK_UNBLOCK');

INSERT INTO bo_approval_policy
  (action_code, module, sub_module, description, approval_required, checker_permission, sla_hours, active)
SELECT 'APP_CONFIG_UPDATE', 'CONFIG', 'APP_CONFIG', 'Update governed app_config value', FALSE, 'app_config.manage', 24, TRUE
WHERE NOT EXISTS (SELECT 1 FROM bo_approval_policy WHERE action_code = 'APP_CONFIG_UPDATE');

INSERT INTO bo_approval_policy
  (action_code, module, sub_module, description, approval_required, checker_permission, sla_hours, active)
SELECT 'REVERSAL_MANUAL_REQUEST', 'REVERSAL', 'MANUAL', 'Submit manual reversal approval request', TRUE, 'reversal.manual.approve', 4, TRUE
WHERE NOT EXISTS (SELECT 1 FROM bo_approval_policy WHERE action_code = 'REVERSAL_MANUAL_REQUEST');

INSERT INTO bo_approval_policy
  (action_code, module, sub_module, description, approval_required, checker_permission, sla_hours, active)
SELECT 'LIQUIDATION_APPROVAL', 'INVESTMENT', 'LIQUIDATION', 'Approve liquidation request', TRUE, 'investment.liquidation.approve', 8, TRUE
WHERE NOT EXISTS (SELECT 1 FROM bo_approval_policy WHERE action_code = 'LIQUIDATION_APPROVAL');
