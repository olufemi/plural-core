UPDATE bo_approval_policy
SET approval_required = TRUE,
    checker_permission = 'investment.product.approve',
    sla_hours = 24,
    active = TRUE
WHERE action_code IN ('INVESTMENT_PRODUCT_CREATE', 'INVESTMENT_PRODUCT_UPDATE');
