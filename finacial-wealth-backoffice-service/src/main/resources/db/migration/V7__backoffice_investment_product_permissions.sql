INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'INVESTMENT', 'PRODUCT', 'VIEW', 'investment.product.view', 'View investment product configuration'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'investment.product.view');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'INVESTMENT', 'PRODUCT', 'MANAGE', 'investment.product.manage', 'Create and update investment product configuration'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'investment.product.manage');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'INVESTMENT', 'PRODUCT', 'APPROVE', 'investment.product.approve', 'Approve investment product configuration changes'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'investment.product.approve');

INSERT INTO bo_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM bo_admin_role r
JOIN bo_permission p ON p.code IN ('investment.product.view', 'investment.product.manage', 'investment.product.approve')
WHERE r.name = 'SUPER_ADMIN'
  AND NOT EXISTS (
    SELECT 1
    FROM bo_role_permission rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );
