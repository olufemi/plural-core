INSERT INTO bo_role_permission(role_id, permission_id)
SELECT r.id, p.id
FROM bo_admin_role r
JOIN bo_permission p ON p.code = 'investment.order.view'
WHERE r.name = 'EXTERNAL_COMPLIANCE'
  AND NOT EXISTS (
    SELECT 1
    FROM bo_role_permission rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );
