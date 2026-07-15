INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'REFERRAL', 'PROGRAM', 'VIEW', 'referral.program.view', 'View referral programs and referral audit'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'referral.program.view');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'REFERRAL', 'PROGRAM', 'MANAGE', 'referral.program.manage', 'Create and manage referral programs'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'referral.program.manage');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'CAMPAIGN', 'MARKETING', 'VIEW', 'campaign.view', 'View campaigns and campaign audit'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'campaign.view');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'CAMPAIGN', 'MARKETING', 'MANAGE', 'campaign.manage', 'Create and manage campaigns'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'campaign.manage');

INSERT INTO bo_permission (module, sub_module, action, code, description)
SELECT 'CAMPAIGN', 'MARKETING', 'APPROVE', 'campaign.approve', 'Approve campaigns'
WHERE NOT EXISTS (SELECT 1 FROM bo_permission WHERE code = 'campaign.approve');

INSERT INTO bo_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM bo_admin_role r
JOIN bo_permission p ON p.code IN (
  'referral.program.view',
  'referral.program.manage',
  'campaign.view',
  'campaign.manage',
  'campaign.approve'
)
WHERE r.name = 'SUPER_ADMIN'
  AND NOT EXISTS (
    SELECT 1
    FROM bo_role_permission rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );
