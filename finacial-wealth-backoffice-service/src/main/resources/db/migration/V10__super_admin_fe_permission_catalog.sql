INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'TRANSACTIONS', 'TRANSACTIONS', 'VIEW', 'transactions.view', 'View transactions'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'transactions.view');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'TRANSACTIONS', 'TRANSACTIONS', 'FILTER', 'transactions.filter', 'Filter transactions'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'transactions.filter');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'TRANSACTIONS', 'TRANSACTIONS', 'VIEW_DETAILS', 'transactions.viewDetails', 'View transaction details'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'transactions.viewDetails');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'TRANSACTIONS', 'FEES', 'VIEW', 'transactions.viewFees', 'View transaction fees'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'transactions.viewFees');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'TRANSACTIONS', 'CUTOFF', 'VIEW_PRE', 'transactions.viewPreCutoff', 'View pre-cutoff transactions'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'transactions.viewPreCutoff');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'TRANSACTIONS', 'CUTOFF', 'VIEW_POST', 'transactions.viewPostCutoff', 'View post-cutoff transactions'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'transactions.viewPostCutoff');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'TRANSACTIONS', 'PLACEMENT', 'PLACE', 'transactions.place', 'Place transactions'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'transactions.place');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'TRANSACTIONS', 'PLACEMENT', 'CONFIRM', 'transactions.confirmPlacement', 'Confirm transaction placement'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'transactions.confirmPlacement');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'TRANSACTIONS', 'STATUS', 'UPDATE', 'transactions.updateStatus', 'Update transaction status'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'transactions.updateStatus');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'TRANSACTIONS', 'STATUS', 'BULK_UPDATE', 'transactions.bulkUpdate', 'Bulk update transactions'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'transactions.bulkUpdate');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'TRANSACTIONS', 'ROLLOVER', 'ROLLOVER', 'transactions.rollover', 'Rollover transactions'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'transactions.rollover');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'PRODUCTS', 'PRODUCTS', 'VIEW', 'products.view', 'View products'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'products.view');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'PRODUCTS', 'PRODUCTS', 'MANAGE', 'products.manage', 'Manage products'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'products.manage');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'USER', 'MANAGEMENT', 'VIEW', 'userManagement.view', 'View backoffice users'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'userManagement.view');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'USER', 'MANAGEMENT', 'MANAGE', 'userManagement.manage', 'Manage backoffice users'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'userManagement.manage');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'USER', 'MANAGEMENT', 'ASSIGN_ROLES', 'userManagement.assignRoles', 'Assign backoffice roles'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'userManagement.assignRoles');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'USER', 'MANAGEMENT', 'EDIT_SUPER_ADMIN', 'userManagement.editSuperAdmin', 'Edit super admin users'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'userManagement.editSuperAdmin');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'REPORTS', 'REPORTS', 'VIEW', 'reports.view', 'View reports'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'reports.view');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'REPORTS', 'REPORTS', 'EXPORT', 'reports.export', 'Export reports'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'reports.export');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'SETTINGS', 'SETTINGS', 'MANAGE', 'settings.manage', 'Manage settings'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'settings.manage');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'AUDIT', 'LOG', 'EXPORT', 'audit.export', 'Export audit logs'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'audit.export');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'APPROVAL', 'DECISION', 'MANAGE', 'approval.decision.manage', 'Approve or reject approval requests'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'approval.decision.manage');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'INTERBANK', 'NAME_ENQUIRY', 'EXECUTE', 'interbank.nameEnquiry.execute', 'Execute interbank name enquiry'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'interbank.nameEnquiry.execute');

INSERT INTO bo_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM bo_admin_role r
JOIN bo_permission p ON p.code IN (
  'transactions.view',
  'transactions.filter',
  'transactions.viewDetails',
  'transactions.viewFees',
  'transactions.viewPreCutoff',
  'transactions.viewPostCutoff',
  'transactions.place',
  'transactions.confirmPlacement',
  'transactions.updateStatus',
  'transactions.bulkUpdate',
  'transactions.rollover',
  'products.view',
  'products.manage',
  'userManagement.view',
  'userManagement.manage',
  'userManagement.assignRoles',
  'userManagement.editSuperAdmin',
  'reports.view',
  'reports.export',
  'settings.manage',
  'audit.view',
  'audit.export',
  'approval.inbox.view',
  'approval.decision.manage',
  'approval.policy.manage',
  'investment.product.manage',
  'investment.product.approve',
  'investment.liquidation.approve',
  'investment.liquidation.remediate',
  'investment.order.view',
  'investment.liquidation.view',
  'reversal.exception.view',
  'reversal.manual.request',
  'reversal.manual.approve',
  'referral.program.view',
  'referral.program.manage',
  'app_config.manage',
  'campaign.approve',
  'customer.profile.view',
  'customer.profile.manage',
  'role.manage',
  'interbank.nameEnquiry.execute'
)
WHERE r.name = 'SUPER_ADMIN'
  AND NOT EXISTS (
    SELECT 1
    FROM bo_role_permission rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );
