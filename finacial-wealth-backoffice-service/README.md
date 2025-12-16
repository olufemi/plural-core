# finacial-wealth-backoffice-service (starter)

## Run
1. Ensure MySQL running and user/password in application.properties
2. Ensure Eureka running (or disable eureka by removing dependency)
3. Start:
   mvn spring-boot:run

## Default Seed
- email: superadmin@finacialwealth.com
- password: Password123!

## Auth flows
- POST /bo/auth/login
- If MFA enabled -> POST /bo/auth/mfa/verify
- Setup MFA: POST /bo/auth/mfa/setup/{adminUserId}
- Confirm: POST /bo/auth/mfa/confirm/{adminUserId}?code=123456

## Example BO endpoints (require JWT)
- POST /bo/group-savings/delete
- POST /bo/interbank/name-enquiry
- POST /bo/fxpeer/offers/update
- GET /bo/investments/products
- GET /bo/investments/products/export.csv
