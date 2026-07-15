-- Optional go-live script.
-- Run only when the approval inbox/checker workflow has been tested end-to-end.
-- This is intentionally not a Flyway migration so pilot deployments do not
-- automatically block direct admin flows.

UPDATE bo_approval_policy
SET approval_required = TRUE
WHERE action_code IN (
  'INVESTMENT_PRODUCT_CREATE',
  'INVESTMENT_PRODUCT_UPDATE',
  'APP_CONFIG_UPDATE',
  'REFERRAL_PROGRAM_CHANGE',
  'CAMPAIGN_CHANGE',
  'CUSTOMER_BLOCK_UNBLOCK'
);

SELECT action_code, approval_required, checker_permission, active
FROM bo_approval_policy
WHERE action_code IN (
  'INVESTMENT_PRODUCT_CREATE',
  'INVESTMENT_PRODUCT_UPDATE',
  'APP_CONFIG_UPDATE',
  'REFERRAL_PROGRAM_CHANGE',
  'CAMPAIGN_CHANGE',
  'CUSTOMER_BLOCK_UNBLOCK',
  'REVERSAL_MANUAL_REQUEST',
  'LIQUIDATION_APPROVAL'
)
ORDER BY action_code;
