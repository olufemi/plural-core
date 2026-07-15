INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'CUSTOMER', 'PROFILE', 'MANAGE', 'customer.profile.manage', 'Block and unblock customer profiles'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'customer.profile.manage');

INSERT INTO bo_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM bo_admin_role r
JOIN bo_permission p ON p.code = 'customer.profile.manage'
WHERE r.name = 'SUPER_ADMIN'
  AND NOT EXISTS (
    SELECT 1
    FROM bo_role_permission rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );
