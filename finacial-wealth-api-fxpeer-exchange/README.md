# finacial-wealth-api-fxpeer-exchange

## Featured Services API Handoff

This document explains the `featured services` APIs that power curated cards for mobile and the admin configuration endpoints used by backoffice.

## Quick Split

- Mobile `Top Deals` / curated feature cards: `GET /fxothers/services/featured`
- Mobile full FX marketplace list: `GET /api/market/offers`
- Backoffice featured config read: `GET /fxothers/admin/featured-services-config`
- Backoffice featured config save: `POST /fxothers/admin/featured-services-config`

## Mobile Usage

### 1. Top Deals / featured cards

Use:

`GET /fxothers/services/featured`

Purpose:
- returns curated cards resolved from admin configuration
- for FX this is the best endpoint for `Top Deals`
- for investments this can also return featured investment products

How mobile should use it:
- call this endpoint when loading the dashboard or services/trade entry screen
- render returned `FX` cards inside the `Top Deals` area
- navigate using `target.screen` and `target.id`
- do not use this endpoint for the full marketplace list

Example response:

```json
{
  "statusCode": 200,
  "description": "Featured services fetched successfully.",
  "data": [
    {
      "featureKey": "fx-top-deal",
      "featureGroup": "FX",
      "featureType": "OFFER",
      "strategy": "BEST_RATE",
      "title": "Trade FX",
      "subtitle": "Featured FX offer",
      "badge": "FEATURED",
      "ctaLabel": "Trade now",
      "target": {
        "screen": "FX_OFFER_DETAIL",
        "id": "145"
      },
      "payload": {
        "offerId": 145,
        "sellerUserId": 88,
        "currencySell": "CAD",
        "currencyReceive": "NGN",
        "rate": 1250.00,
        "qtyAvailable": 1000.00,
        "qtyTotal": 1000.00,
        "sellerStats": {
          "completedTrades": 21,
          "rating": 4.8
        }
      }
    }
  ]
}
```

Mapping for the mobile screen in your screenshot:
- `Top Deals (1)`: use `GET /fxothers/services/featured` and render the returned `FX` card(s)
- `Marketplace listings`: use the normal market browse endpoint below

### 2. Marketplace listings

Use:

`GET /api/market/offers`

Purpose:
- returns the normal paginated FX marketplace offers
- this is the right endpoint for the `Marketplace listings` section

Example:

```http
GET /api/market/offers?ccySell=CAD&ccyRecv=NGN&page=0&size=20&sort=bestRate
```

Supported query params:
- `ccySell`
- `ccyRecv`
- `rateMin`
- `rateMax`
- `amountMin`
- `page`
- `size`
- `sort`

Notes:
- use `/fxothers/services/featured` for the single promoted deal or small curated set
- use `/api/market/offers` for the full scrollable market list

## Backoffice Usage

### 1. Read current featured config

`GET /fxothers/admin/featured-services-config`

Purpose:
- returns the raw config saved by backoffice
- used to populate the admin configuration screen

Example response:

```json
{
  "statusCode": 200,
  "description": "Featured services config fetched successfully.",
  "data": {
    "items": [
      {
        "featureKey": "fx-top-deal",
        "featureGroup": "FX",
        "strategy": "BEST_RATE",
        "fallbackStrategy": "MOST_BOUGHT_OFFER",
        "enabled": true,
        "priority": 10,
        "titleOverride": "Top Deal",
        "subtitleOverride": "Best available trade",
        "badge": "TOP DEAL",
        "ctaLabel": "Trade now",
        "targetScreen": "FX_OFFER_DETAIL",
        "filters": {
          "currencySell": "CAD",
          "currencyReceive": "NGN",
          "lookbackDays": 7
        }
      }
    ]
  }
}
```

### 2. Save featured config

`POST /fxothers/admin/featured-services-config`

Request body:

```json
{
  "items": [
    {
      "featureKey": "fx-top-deal",
      "featureGroup": "FX",
      "strategy": "BEST_RATE",
      "fallbackStrategy": "MOST_BOUGHT_OFFER",
      "enabled": true,
      "priority": 10,
      "titleOverride": "Top Deal",
      "subtitleOverride": "Best available trade",
      "badge": "TOP DEAL",
      "ctaLabel": "Trade now",
      "targetScreen": "FX_OFFER_DETAIL",
      "filters": {
        "currencySell": "CAD",
        "currencyReceive": "NGN",
        "lookbackDays": 7
      }
    },
    {
      "featureKey": "investment-highlight",
      "featureGroup": "INVESTMENT",
      "strategy": "LOW_RISK_FEATURED",
      "fallbackStrategy": "HIGHEST_YIELD",
      "enabled": true,
      "priority": 20,
      "titleOverride": "Featured Investment",
      "subtitleOverride": "Low-risk product",
      "badge": "FEATURED",
      "ctaLabel": "Invest now",
      "targetScreen": "INVESTMENT_PRODUCT_DETAIL",
      "filters": {
        "currency": "NGN"
      }
    }
  ]
}
```

Minimal valid payload:

```json
{
  "items": [
    {
      "featureKey": "fx-top-deal",
      "featureGroup": "FX",
      "strategy": "MOST_BOUGHT_OFFER"
    }
  ]
}
```

Validation rules:
- `featureKey` is required
- `featureGroup` is required
- `strategy` is required
- `manualTargetId` is required when `strategy = ADMIN_SELECTED`

## Enum Guide

### featureGroup
- `FX`: foreign exchange curated cards
- `INVESTMENT`: investment curated cards

### featureType
- `OFFER`: FX offer card
- `PRODUCT`: investment product card

### FX strategies
- `ADMIN_SELECTED`: use a manually selected offer id
- `BEST_RATE`: highest-priority best-rate FX offer
- `MOST_BOUGHT_OFFER`: most purchased offer in the lookback window
- `TOP_SELLER_OFFER`: offer from top-performing seller
- `NEW_OFFER_BOOST`: new recent offer
- `FIRST_TIMER_SELLER_BOOST`: recent offer from a first-time seller

### Investment strategies
- `ADMIN_SELECTED`: use a manually selected product
- `HIGHEST_YIELD`: highest yield product
- `MOST_SUBSCRIBED_PRODUCT`: product with most subscription count
- `HIGHEST_SUBSCRIPTION_VOLUME`: product with highest subscription value
- `NEW_PRODUCT_BOOST`: newest eligible product
- `LOW_RISK_FEATURED`: low-risk product from metadata

## Filter Notes

FX filters may include:
- `currencySell`
- `currencyReceive`
- `lookbackDays`
- `boostHours`

Investment filters may include:
- `currency`
- `lookbackDays`

## Storage Note

Featured-services config is stored in `app_config.config_value` as JSON.

If saving config fails with a MySQL error like `Data too long for column 'config_value'`, widen the column in the database and align the entity mapping. Recommended schema type:

```sql
ALTER TABLE app_config
MODIFY COLUMN config_value LONGTEXT;
```

## Source References

- Featured controller: `FxOtherServicesController`
- Featured logic: `FeaturedServicesService`
- Marketplace browse controller: `MarketplaceController`
