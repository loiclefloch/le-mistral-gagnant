# Pragmatic RESTful API Design Guidelines

**Version**: 3.1  
**Date**: November 13, 2025  
**Based On**: OCTO Technology + Pragmatic Experience + Community Feedback  
**Philosophy**: Balance REST principles with practical development needs while addressing modern challenges

---

## 🎯 Core Philosophy

### 1. API as Product Mindset

**Your API is a product, and developers are your users.**

- **Developer Experience (DX)**: Design for developer experience, not just technical correctness
- **Generic & Reusable**: Build once, use many times across different clients
- **Iterative Evolution**: Ship early, gather feedback, improve continuously
- **Documentation as UX**: Clear docs are as important as the code itself
- **Lifecycle Management**: APIs have a lifetime - plan for versioning, deprecation, and support

**Time to First API Call (TTFAC)**: Target < 10 minutes from discovery to successful first call.

### 2. The Four Principles of Good API Design

1. **Abstract Design**: Your API is NOT your database schema - expose business concepts, not tables
2. **Self-Documenting**: Intuitive enough that documentation is rarely needed
3. **Consistent**: Pick conventions and stick to them religiously
4. **No Surprises**: Follow established patterns, don't reinvent the wheel

### 3. Design Process Workflow

```
1. Define Use Cases → 2. Model Resources → 3. Design URLs → 4. Choose Methods
   ↓                    ↓                   ↓               ↓
5. Document First  → 6. Review & Iterate → 7. Implement → 8. Test & Ship
```

**Key Practices**:
- Start with user stories, not technical specs
- Involve frontend/mobile developers early
- Create OpenAPI spec before implementation
- Test with real consumers during alpha phase
- Measure actual usage patterns

---

## 📊 API Quality Scoring System

### Scoring Categories & Weights

| Category               | Weight | Mandatory?     | Context                    |
|------------------------|--------|----------------|----------------------------|
| **Security**           | 18%    | ✅ Always       | Zero Trust, OAuth 2.0      |
| **Error Handling**     | 10%    | ✅ Always       | RFC 9457, clear messages   |
| **Versioning**         | 10%    | ⚠️ Conditional | Mobile apps, ext. clients  |
| **URL Structure**      | 8%     | ✅ Always       | Resource-oriented design   |
| **HTTP Methods**       | 8%     | ✅ Always       | Idempotency matters        |
| **Status Codes**       | 7%     | ✅ Always       | Standard + 422, 429        |
| **Pagination**         | 7%     | ⚠️ Conditional | Large collections only     |
| **Rate Limiting**      | 6%     | ✅ Always       | Protect infrastructure     |
| **Content Negotiation**| 4%     | ⚠️ Conditional | Multiple formats needed    |
| **Caching**            | 5%     | ✅ Always       | ETags, Cache-Control       |
| **HATEOAS**            | 3%     | ❌ Optional     | AI/discovery scenarios     |
| **Documentation**      | 10%    | ✅ Always       | OpenAPI + examples         |
| **Observability**      | 4%     | ✅ Always       | Logs, metrics, traces      |

### Target Scores by Project Type

| Project Type           | Minimum | Target | Excellence |
|------------------------|---------|--------|------------|
| **MVP/Prototype**      | 50%     | 60%    | 70%        |
| **Startup API**        | 65%     | 75%    | 85%        |
| **Production API**     | 75%     | 85%    | 90%        |
| **Platform/Public API**| 85%     | 90%    | 95%        |

---

## 🔐 Security: Zero Trust Model

### The Three Pillars

1. **Authenticate** users and applications
2. **Authorize** with fine-grained permissions
3. **Audit** all actions with complete traceability

### Zero Trust: "Never trust, always verify"

- Every request must be authenticated, even internal ones
- No implicit trust based on network location
- Verify identity and permissions at each layer
- Assume breach - minimize blast radius

### HTTPS is Mandatory

- ✅ Valid TLS certificates (Let's Encrypt)
- ✅ TLS 1.3 preferred, minimum TLS 1.2
- ✅ Enable HSTS with max-age=31536000
- ✅ Certificate pinning for mobile apps
- ❌ Never self-signed certs in production
- ❌ No mixed content (HTTP resources on HTTPS)

### Authentication Methods

#### OAuth 2.0 Flow Selection

| Flow Type                  | Use Case                        | Example                    |
|---------------------------|---------------------------------|----------------------------|
| **Client Credentials**    | Server-to-server, M2M           | Backend service to API     |
| **Authorization Code+PKCE**| Web/mobile apps with users     | SPA, mobile app            |
| **Device Grant**          | IoT devices, smart TVs          | Smart TV, CLI tools        |
| **Resource Owner Password**| Legacy only (deprecated)       | ⚠️ Avoid for new projects  |

#### API Keys

**When to use**: Simple auth for partners, internal tools, webhooks

**Best practices**:
```http
# Header (preferred)
X-API-Key: your-api-key-here
Authorization: Bearer api-key-here

# Query param (only if header not possible)
GET /api/data?api_key=your-key
```

- ✅ Generate cryptographically random keys (min 32 chars)
- ✅ Hash keys before storing
- ✅ Support key rotation without downtime
- ✅ Allow multiple keys per account
- ❌ Never log full API keys

### JWT Best Practices

**Validation checklist**:
- ✅ Signature (using JWKS from well-known endpoint)
- ✅ Expiration (exp) - reject expired tokens
- ✅ Issuer (iss) - verify trusted issuer
- ✅ Audience (aud) - ensure token is for your API
- ✅ Not Before (nbf) - prevent premature usage
- ✅ JWT ID (jti) - for revocation tracking

**Token Lifetimes**:
- Access tokens: 5-15 minutes
- Refresh tokens: 7-30 days
- ID tokens: Match access token

**Claims Structure**:
```json
{
  "sub": "user-123",
  "iss": "https://auth.example.com",
  "aud": "api.example.com",
  "exp": 1708723200,
  "iat": 1708722300,
  "scope": "read:orders write:orders",
  "permissions": ["order.view", "order.create"]
}
```

**Scopes vs Permissions**:
- **Scopes**: What application CAN do (OAuth consent)
- **Permissions**: What user IS ALLOWED to do (RBAC/ABAC)
- **Effective Access** = User permissions ∩ Client scopes

### Input Validation

**Validate everything**:
- ✅ Data types (string, number, boolean)
- ✅ Format (email, URL, UUID)
- ✅ Length (min/max)
- ✅ Range (numeric boundaries)
- ✅ Allowed values (enums)
- ✅ Business rules (e.g., end_date > start_date)

**Fail fast with clear errors**:
```json
{
  "type": "VALIDATION_ERROR",
  "status": 400,
  "errors": [
    {
      "field": "email",
      "value": "invalid-email",
      "message": "Must be a valid email address",
      "code": "INVALID_FORMAT"
    }
  ]
}
```

### OWASP API Security Top 10 (2023)

1. **Broken Object Level Authorization**: Check user owns resource
2. **Broken Authentication**: Use proven auth frameworks
3. **Broken Object Property Level Authorization**: Validate input fields
4. **Unrestricted Resource Consumption**: Rate limiting mandatory
5. **Broken Function Level Authorization**: Check permissions per action
6. **Unrestricted Business Flow Access**: Rate limit by business logic
7. **Server Side Request Forgery**: Validate/sanitize URLs
8. **Security Misconfiguration**: Secure defaults, disable debug
9. **Improper Inventory Management**: Maintain API inventory
10. **Unsafe API Consumption**: Validate third-party responses

---

## 🏗️ URL Structure

### Path Structure

```
https://api.example.com/[domain]/v1/[resource]/[id]/[sub-resource]
                         ────────  ──  ────────  ───  ─────────────
                         optional  ver required  id   nested resource
```

**Examples**:
```
GET /v1/products/123
GET /v1/products/123/reviews
GET /payments/v1/invoices/456/line-items
GET /v1/users/789/orders?status=pending
```

### Essential Rules

1. **Nouns, not verbs**: `GET /products` not `GET /getProducts`
2. **Plural collections**: `GET /orders/123` not `GET /order/123`
3. **Consistent naming**: Choose kebab-case, camelCase, or snake_case - stick to it
4. **Lowercase URLs**: `/products` not `/Products`
5. **No trailing slashes**: `/products` not `/products/`
6. **Use hyphens**: `/product-reviews` not `/product_reviews` (in URLs)

### Nested Resources

**Shallow nesting** (max 2 levels):
```
✅ GET /users/123/orders
✅ GET /orders/456/items
❌ GET /users/123/orders/456/items/789/options (too deep)
```

**Alternative for deep nesting**:
```
GET /order-items/789?order_id=456
GET /order-items/789/options
```

### Pragmatic Action Endpoints

Actions that don't fit CRUD:
```
✅ POST /orders/123/cancel
✅ POST /orders/123/refund
✅ POST /users/456/reset-password
✅ POST /invoices/789/send
✅ POST /documents/101/publish
```

**Pattern**: `POST /{resource}/{id}/{action}`

### Query Parameters

**Conventions**:
```http
# Filtering
GET /products?category=electronics&price_max=100

# Sorting
GET /products?sort=price:asc,name:desc

# Field selection
GET /products?fields=id,name,price

# Search
GET /products?q=laptop

# Pagination
GET /products?page=2&limit=25
```

### Resource Granularity

**Start coarse-grained**, split when needed:
```
# Coarse: One endpoint, all data
GET /users/123  
→ {id, name, email, profile, preferences, stats}

# Fine-grained: Multiple endpoints when needed
GET /users/123/profile
GET /users/123/preferences
GET /users/123/stats

# Field selection as alternative
GET /users/123?fields=id,name,email
```

**When to split**:
- Different access patterns (public vs private data)
- Performance issues (large payloads)
- Different update frequencies
- Security boundaries

---

## 🔧 HTTP Methods & Status Codes

### HTTP Methods

| Method   | Use           | Idempotent | Safe | Body | Example              |
|----------|---------------|------------|------|------|----------------------|
| GET      | Read          | ✅          | ✅    | ❌    | `GET /products/123`  |
| POST     | Create/Action | ❌          | ❌    | ✅    | `POST /orders`       |
| PUT      | Replace       | ✅          | ❌    | ✅    | `PUT /products/123`  |
| PATCH    | Update        | ❌*         | ❌    | ✅    | `PATCH /users/456`   |
| DELETE   | Delete        | ✅          | ❌    | ❌    | `DELETE /orders/789` |
| HEAD     | Metadata      | ✅          | ✅    | ❌    | `HEAD /products/123` |
| OPTIONS  | Capabilities  | ✅          | ✅    | ❌    | `OPTIONS /orders`    |

*PATCH can be idempotent with proper design

### PUT vs PATCH

**PUT** - Full replacement:
```http
PUT /products/123
{
  "name": "Laptop",
  "price": 999,
  "category": "electronics",
  "stock": 10
}
```

**PATCH** - Partial update:
```http
PATCH /products/123
{
  "price": 899,
  "stock": 8
}
```

**PATCH Strategies**:

1. **JSON Merge Patch** (RFC 7396):
```http
PATCH /products/123
Content-Type: application/merge-patch+json

{"price": 899}
```

2. **JSON Patch** (RFC 6902):
```http
PATCH /products/123
Content-Type: application/json-patch+json

[
  {"op": "replace", "path": "/price", "value": 899},
  {"op": "add", "path": "/tags/-", "value": "sale"}
]
```

### HTTP Status Codes

#### Success (2xx)

| Code | Meaning         | When to Use                          | Example Response         |
|------|-----------------|--------------------------------------|--------------------------|
| 200  | OK              | Successful GET, PUT, PATCH           | Resource data            |
| 201  | Created         | Successful POST (resource created)   | New resource + Location  |
| 202  | Accepted        | Async processing started             | Job ID or status URL     |
| 204  | No Content      | Successful DELETE or update          | Empty body               |
| 206  | Partial Content | Range request fulfilled              | Partial data + headers   |

#### Client Error (4xx)

| Code | Meaning                | When to Use                          | RFC 9457 type                |
|------|------------------------|--------------------------------------|------------------------------|
| 400  | Bad Request            | Malformed JSON, invalid syntax       | INVALID_REQUEST              |
| 401  | Unauthorized           | Missing or invalid authentication    | AUTHENTICATION_REQUIRED      |
| 403  | Forbidden              | Valid auth but insufficient perms    | INSUFFICIENT_PERMISSIONS     |
| 404  | Not Found              | Resource doesn't exist               | RESOURCE_NOT_FOUND           |
| 405  | Method Not Allowed     | Valid resource, wrong method         | METHOD_NOT_ALLOWED           |
| 409  | Conflict               | State conflict (e.g., duplicate)     | RESOURCE_CONFLICT            |
| 410  | Gone                   | Permanently deleted                  | RESOURCE_GONE                |
| 422  | Unprocessable Entity   | Valid JSON, business validation fail | VALIDATION_FAILED            |
| 429  | Too Many Requests      | Rate limit exceeded                  | RATE_LIMIT_EXCEEDED          |

#### Server Error (5xx)

| Code | Meaning                | When to Use                          |
|------|------------------------|--------------------------------------|
| 500  | Internal Server Error  | Unexpected server error              |
| 502  | Bad Gateway            | Upstream service error               |
| 503  | Service Unavailable    | Temporary downtime, maintenance      |
| 504  | Gateway Timeout        | Upstream timeout                     |

### Idempotency

**Idempotent operations**: Same result regardless of how many times called

**For POST idempotency**, use Idempotency-Key header:
```http
POST /payments
Idempotency-Key: unique-id-12345
Content-Type: application/json

{
  "amount": 100,
  "currency": "USD",
  "account": "acc_123"
}
```

**Implementation**:
1. Store key with request hash
2. Return cached response if duplicate detected
3. Expire keys after 24-48 hours
4. Return 409 if key reused with different data

---

## 📝 Error Handling (RFC 9457)

### Standard Error Format

```json
{
  "type": "https://api.example.com/errors/insufficient-funds",
  "title": "Insufficient Funds",
  "status": 422,
  "detail": "Account balance is 30 EUR, but transaction requires 100 EUR",
  "instance": "/payments/123",
  "trace_id": "abc-123-def-456"
}
```

**Required fields**:
- `type`: URI identifying the problem type
- `title`: Short, human-readable summary
- `status`: HTTP status code (for convenience)

**Optional but recommended**:
- `detail`: Human-readable explanation
- `instance`: URI identifying specific occurrence
- `trace_id`: For debugging and support

### Multiple Validation Errors

```json
{
  "type": "VALIDATION_FAILED",
  "title": "Validation Failed",
  "status": 400,
  "detail": "The request contains 3 validation errors",
  "errors": [
    {
      "field": "email",
      "message": "Invalid email format",
      "code": "INVALID_FORMAT"
    },
    {
      "field": "age",
      "message": "Must be 18 or older",
      "code": "MIN_VALUE",
      "constraint": {"min": 18}
    },
    {
      "field": "password",
      "message": "Must contain at least one number",
      "code": "INVALID_PATTERN"
    }
  ]
}
```

### Error Response Headers

```http
HTTP/1.1 429 Too Many Requests
Content-Type: application/problem+json
Retry-After: 60
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1708723200

{
  "type": "RATE_LIMIT_EXCEEDED",
  "title": "Rate Limit Exceeded",
  "status": 429,
  "detail": "You have exceeded the rate limit of 100 requests per minute"
}
```

### Error Codes Catalog

**Maintain internal catalog**:
```json
{
  "INVALID_FORMAT": {
    "http_status": 400,
    "message_template": "Field {field} has invalid format",
    "resolution": "Check the field format in the documentation"
  },
  "INSUFFICIENT_PERMISSIONS": {
    "http_status": 403,
    "message_template": "Permission {permission} required",
    "resolution": "Contact your administrator for access"
  }
}
```

---

## 📄 Pagination & Filtering

### Pagination Strategies

#### 1. Page-Based Pagination

**Best for**: User-facing lists, stable datasets

```http
GET /products?page=2&limit=25

{
  "items": [...],
  "pagination": {
    "page": 2,
    "limit": 25,
    "total": 1234,
    "total_pages": 50,
    "has_previous": true,
    "has_next": true
  },
  "links": {
    "first": "/products?page=1&limit=25",
    "previous": "/products?page=1&limit=25",
    "next": "/products?page=3&limit=25",
    "last": "/products?page=50&limit=25"
  }
}
```

**Pros**: Simple, allows jumping to any page  
**Cons**: Performance degrades on large offsets, inconsistent with concurrent modifications

#### 2. Cursor-Based Pagination

**Best for**: Real-time feeds, large datasets, consistent results

```http
GET /messages?cursor=eyJ...&limit=50

{
  "items": [...],
  "pagination": {
    "next_cursor": "eyJpZCI6MTIzLCJ0cyI6MTcwODcyMzIwMH0=",
    "previous_cursor": "eyJpZCI6NzAsInRzIjoxNzA4NzIzMTAwfQ==",
    "has_next": true,
    "has_previous": true
  }
}
```

**Cursor content** (base64 encoded):
```json
{"id": 123, "timestamp": 1708723200}
```

**Pros**: Consistent results, better performance  
**Cons**: Can't jump to arbitrary page

#### 3. Keyset Pagination

**Best for**: Performance-critical APIs, time-series data

```http
GET /events?after_id=12345&limit=100

{
  "items": [
    {"id": 12346, "created_at": "2025-02-13T10:00:00Z", ...},
    {"id": 12347, "created_at": "2025-02-13T10:01:00Z", ...}
  ],
  "pagination": {
    "after_id": 12445,
    "has_more": true
  }
}
```

### Pagination Headers

```http
HTTP/1.1 200 OK
Content-Type: application/json
X-Total-Count: 1234
X-Page-Number: 2
X-Page-Size: 25
Link: </products?page=1>; rel="first",
      </products?page=1>; rel="prev",
      </products?page=3>; rel="next",
      </products?page=50>; rel="last"
```

### Filtering

#### Simple Filters

```http
GET /products?category=electronics&status=active
GET /users?role=admin&created_after=2025-01-01
GET /orders?min_amount=100&max_amount=1000
```

#### Complex Filters (JSON)

```http
POST /products/search
{
  "filters": {
    "category": {"in": ["electronics", "computers"]},
    "price": {"gte": 100, "lte": 1000},
    "stock": {"gt": 0},
    "tags": {"contains": "sale"}
  },
  "sort": [
    {"field": "price", "order": "asc"},
    {"field": "name", "order": "asc"}
  ],
  "page": 1,
  "limit": 25
}
```

### Sorting

```http
# Single field
GET /products?sort=price:asc

# Multiple fields
GET /products?sort=category:asc,price:desc,name:asc

# Alternative syntax
GET /products?sort_by=price&order=asc
```

### Field Selection (Sparse Fieldsets)

**Reduce payload size by selecting specific fields**:

```http
# Minimal fields
GET /users?fields=id,name,email

# Nested fields
GET /orders?fields=id,status,user(id,name),items(id,product_name,price)

Response:
{
  "id": "order_123",
  "status": "pending",
  "user": {"id": "user_456", "name": "John Doe"},
  "items": [
    {"id": "item_1", "product_name": "Laptop", "price": 999}
  ]
}
```

### Search

```http
# Simple search
GET /products?q=laptop

# Advanced search
GET /products?q=laptop&search_fields=name,description,tags

# Full-text search with filters
POST /products/search
{
  "query": "gaming laptop",
  "filters": {"category": "electronics"},
  "highlight": true
}
```

---

## 🔄 API Versioning

### When Versioning is Mandatory

- ✅ Mobile apps in production (can't force updates)
- ✅ External API consumers (> 10 organizations)
- ✅ Public API
- ✅ Breaking changes needed
- ✅ SLA commitments

### When You Can Skip Versioning

- Internal APIs with coordinated deployments
- MVP/prototype phase
- < 5 known consumers with direct communication
- Backward-compatible changes only

### Versioning Strategies

#### 1. URL Path (Recommended)

```http
GET /v1/products/123
GET /v2/products/123
```

**Pros**: Clear, simple, works everywhere  
**Cons**: URL pollution

#### 2. Query Parameter

```http
GET /products/123?version=1
GET /products/123?v=2
```

**Pros**: Same base URL  
**Cons**: Easy to forget, caching issues

#### 3. Custom Header

```http
GET /products/123
X-API-Version: 1

# Or Accept header
Accept: application/vnd.api.v1+json
```

**Pros**: Clean URLs  
**Cons**: Less visible, harder to test in browser

#### 4. Content Negotiation (Sophisticated)

```http
GET /products/123
Accept: application/vnd.myapi+json; version=1
```

**Pros**: RESTful, supports multiple formats  
**Cons**: Complex, harder for developers

### Version Lifecycle Management

**Timeline example**:
```
v1 Launch → v2 Announced (6 mo) → v2 Released → v1 Deprecated (6 mo) → v1 Sunset
  T+0         T+6mo                 T+12mo        T+18mo                   T+24mo
```

**Best practices**:
1. Announce new version 6 months ahead
2. Maintain max 2 major versions simultaneously
3. Monitor usage before deprecation
4. Provide migration guide with code examples
5. Support deprecated version for 6-12 months
6. Send deprecation warnings in responses

**Deprecation Headers**:
```http
HTTP/1.1 200 OK
Deprecation: true
Sunset: Sat, 31 Dec 2025 23:59:59 GMT
Link: </docs/migration/v2>; rel="deprecation"
```

### Breaking vs Non-Breaking Changes

**Breaking changes** (require new version):
- Removing endpoints or fields
- Changing field types
- Renaming fields
- Adding required fields
- Changing URL structure
- Modifying authentication

**Non-breaking changes** (same version):
- Adding optional fields
- Adding new endpoints
- Adding optional query parameters
- Making required fields optional
- Expanding enum values (if client handles unknowns)

---

## 🚀 Rate Limiting & Throttling

### Why Rate Limiting Matters

1. **Protect infrastructure** from overload
2. **Ensure fair usage** across clients
3. **Prevent abuse** and DoS attacks
4. **Control costs** (especially with cloud APIs)

### Rate Limit Strategies

#### 1. Fixed Window

Simple, but allows bursts at window boundaries.

```
100 requests per minute
Window: 10:00:00 - 10:00:59

Can make 100 requests at 10:00:59, then 100 more at 10:01:00
```

#### 2. Sliding Window

More accurate, smoother distribution.

```
100 requests per rolling 60 seconds
At 10:00:30, counts requests from 09:59:30 - 10:00:30
```

#### 3. Token Bucket

Allows bursts while maintaining average rate.

```
Bucket capacity: 100 tokens
Refill rate: 10 tokens/second
Burst: Can use 100 tokens immediately, then 10/sec sustained
```

#### 4. Leaky Bucket

Smooths out bursts, enforces steady rate.

```
Processes requests at fixed rate
Excess requests wait in queue or get rejected
```

### Rate Limit Headers (RFC Draft)

```http
HTTP/1.1 200 OK
RateLimit-Limit: 100
RateLimit-Remaining: 75
RateLimit-Reset: 1708723200

# Alternative headers (legacy)
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 75
X-RateLimit-Reset: 1708723200
```

### Rate Limit Response

```http
HTTP/1.1 429 Too Many Requests
Content-Type: application/problem+json
Retry-After: 60
RateLimit-Limit: 100
RateLimit-Remaining: 0
RateLimit-Reset: 1708723260

{
  "type": "RATE_LIMIT_EXCEEDED",
  "title": "Rate Limit Exceeded",
  "status": 429,
  "detail": "You have exceeded 100 requests per minute. Try again in 60 seconds.",
  "retry_after": 60
}
```

### Tiered Rate Limits

```
Free tier:     100 requests/minute
Basic tier:    1,000 requests/minute
Premium tier:  10,000 requests/minute
Enterprise:    Custom limits
```

### Rate Limiting by Scope

Different limits for different endpoints:

```
POST /orders:        10 requests/minute (expensive)
GET /products:       1000 requests/minute (cheap)
POST /batch:         1 request/minute (very expensive)
```

---

## 📦 Content Negotiation & Formats

### Accept Header

```http
# JSON (default)
Accept: application/json

# JSON with specific version
Accept: application/vnd.api.v1+json

# XML
Accept: application/xml

# Multiple preferences
Accept: application/json, application/xml; q=0.8, */*; q=0.5
```

### Response Formats

#### JSON (Default)

```json
{
  "id": "123",
  "name": "Product Name",
  "price": 99.99
}
```

#### JSON:API (Structured)

```json
{
  "data": {
    "type": "products",
    "id": "123",
    "attributes": {
      "name": "Product Name",
      "price": 99.99
    },
    "relationships": {
      "category": {
        "data": {"type": "categories", "id": "456"}
      }
    }
  }
}
```

#### HAL (Hypertext Application Language)

```json
{
  "_links": {
    "self": {"href": "/products/123"},
    "category": {"href": "/categories/456"}
  },
  "id": "123",
  "name": "Product Name",
  "price": 99.99
}
```

### Compression

```http
# Request compression support
Accept-Encoding: gzip, deflate, br

# Response with compression
HTTP/1.1 200 OK
Content-Encoding: gzip
Content-Type: application/json
```

**When to compress**:
- Responses > 1KB
- Text-based formats (JSON, XML, CSV)
- High-traffic endpoints

**Skip compression for**:
- Already compressed (images, videos)
- Very small responses (< 500 bytes)
- Real-time streaming

---

## 💾 Caching Strategies

### Cache-Control Header

```http
# Public, cacheable for 1 hour
Cache-Control: public, max-age=3600

# Private, client cache only
Cache-Control: private, max-age=300

# No caching
Cache-Control: no-store, no-cache, must-revalidate

# Conditional requests allowed
Cache-Control: public, max-age=3600, must-revalidate
```

### ETags for Validation

```http
# Initial request
GET /products/123

HTTP/1.1 200 OK
ETag: "33a64df551425fcc55e4d42a148795d9f25f89d4"
Cache-Control: public, max-age=3600
Content-Type: application/json

{"id": "123", "name": "Product", ...}

# Conditional request
GET /products/123
If-None-Match: "33a64df551425fcc55e4d42a148795d9f25f89d4"

HTTP/1.1 304 Not Modified
ETag: "33a64df551425fcc55e4d42a148795d9f25f89d4"
```

### Last-Modified Header

```http
GET /products/123

HTTP/1.1 200 OK
Last-Modified: Wed, 13 Feb 2025 10:00:00 GMT
Cache-Control: public, max-age=3600

# Conditional request
GET /products/123
If-Modified-Since: Wed, 13 Feb 2025 10:00:00 GMT

HTTP/1.1 304 Not Modified
```

### Caching Best Practices

| Resource Type       | Cache Strategy                           | Duration   |
|---------------------|------------------------------------------|------------|
| **Static assets**   | `public, immutable, max-age=31536000`    | 1 year     |
| **User profile**    | `private, max-age=300`                   | 5 minutes  |
| **Product catalog** | `public, max-age=3600, must-revalidate`  | 1 hour     |
| **Real-time data**  | `no-store, no-cache`                     | No caching |
| **Search results**  | `public, max-age=60`                     | 1 minute   |

**Vary Header** for different representations:
```http
Vary: Accept-Encoding, Accept-Language
```

---

## 🏛️ API Architecture Patterns

### API Types

1. **Core APIs**: Expose business capabilities, master of data
2. **Facade APIs**: Wrapper over legacy systems (transitional)
3. **Process APIs**: Orchestrate multiple core APIs
4. **Experience APIs (BFF)**: Tailored for specific client needs

### When to Use Each

**Core APIs** - Use when:
- Building new services
- Defining domain boundaries
- Need to be master of data
- Long-term strategic APIs

**Facade APIs** - Use when:
- Integrating legacy systems
- Temporary wrapper needed
- Gradual migration strategy
- Cannot modify underlying system

**Process APIs** - Use when:
- Complex workflows spanning domains
- Need orchestration logic
- Business process automation
- Transaction coordination

**BFF (Backend for Frontend)** - Use when:
- Mobile vs web have different needs
- Need to aggregate multiple APIs
- Client-specific optimizations
- Reduce chattiness

### Microservices vs Monolith

**Start with Modular Monolith**, extract to microservices only when:
- Team size > 10 developers
- Clear bounded contexts exist
- Independent scaling needed
- DevOps maturity achieved
- Deployment independence required

**Microservices Benefits**:
- Independent deployment
- Technology diversity
- Team autonomy
- Scalability

**Microservices Costs**:
- Operational complexity
- Distributed debugging
- Data consistency challenges
- Network latency

---

## 🔄 Sync vs Async Integration

### Use REST (Sync) When

- Real-time response needed (< 5 seconds)
- Simple request-response pattern
- Frontend to backend communication
- External partner integrations
- User-facing operations

### Use Messaging (Async) When

- Long-running processes (> 10 seconds)
- High volume data transfer
- Event-driven architecture
- Decoupled microservices
- Order matters (event sourcing)

### REST vs Other Protocols

| Protocol   | Use Case                           | Pros                          | Cons                      |
|------------|-------------------------------------|-------------------------------|---------------------------|
| **REST**   | Default choice                      | Universal, simple, cacheable  | Overfetching, chattiness  |
| **GraphQL**| BFF scenarios, flexible queries     | Precise data fetching         | Complexity, caching       |
| **gRPC**   | Internal microservices              | Performance, type safety      | Limited browser support   |
| **WebSocket**| Real-time bidirectional           | Low latency, push capability  | Scaling, debugging        |
| **SSE**    | Server-to-client streaming          | Simple, HTTP-based            | One-way only              |
| **SOAP**   | Legacy only                         | Standards, tooling            | ⚠️ Don't start new projects|

### Webhooks for Event Notifications

**When to provide webhooks**:
- Long-running async operations
- Event notifications (order created, payment received)
- Third-party integrations
- Real-time updates

**Webhook best practices**:
```http
POST https://customer-webhook-url.com/endpoint
Content-Type: application/json
X-Webhook-Signature: sha256=abc123...
X-Webhook-ID: evt_123

{
  "event": "order.created",
  "timestamp": "2025-02-13T10:00:00Z",
  "data": {
    "order_id": "order_123",
    "status": "pending",
    "total": 99.99
  }
}
```

**Security**:
- Sign webhook payloads (HMAC-SHA256)
- Verify signatures on receipt
- Use HTTPS only
- Support retry with exponential backoff
- Provide webhook logs/dashboard

---

## ⚡ Resilience Patterns

### Timeouts

Set timeouts for all external calls:
- **API calls**: 2-3 seconds (user-facing)
- **Database**: 1 second (queries)
- **Third-party APIs**: 5-10 seconds
- **Background jobs**: 30-60 seconds

**Implementation**:
```
Connection timeout: 2s (time to establish connection)
Read timeout: 5s (time to receive response)
Total timeout: 7s (maximum)
```

### Retries with Exponential Backoff

**Pattern**: Wait progressively longer between retries

```
Attempt 1: Immediate
Attempt 2: 100ms delay
Attempt 3: 200ms delay
Attempt 4: 400ms delay
Attempt 5: 800ms delay (then stop)
```

**With jitter** (randomization):
```
delay = min(max_delay, base_delay * 2^attempt + random(0, jitter))
```

**Retry only for**:
- Network errors
- 408 Request Timeout
- 429 Too Many Requests
- 500, 502, 503, 504

**Never retry**:
- 400, 401, 403, 404 (client errors)
- Non-idempotent operations (unless using Idempotency-Key)

### Circuit Breaker

Prevents cascade failures by failing fast:

**States**:
1. **Closed** (normal): Requests pass through
2. **Open** (failing): Reject immediately without calling service
3. **Half-Open** (testing): Allow limited requests to test recovery

**Configuration**:
```
Failure threshold: 5 consecutive failures
Timeout: 60 seconds (before half-open)
Success threshold: 2 successes (to close)
```

### Bulkhead Pattern

Isolate resources to prevent total failure:

**Resource isolation**:
- Separate thread pools per service
- Dedicated connection pools
- Container-level isolation
- Rate limiting per tenant

**Example**:
```
Total threads: 100
- Payment service: 20 threads
- Email service: 10 threads
- Search service: 30 threads
- Default pool: 40 threads
```

### Fallback Strategies

**Options when primary fails**:
1. **Cached data**: Return stale data
2. **Default value**: Reasonable fallback
3. **Degraded mode**: Reduced functionality
4. **Queue for later**: Async processing
5. **Fail gracefully**: Clear error message

---

## 📊 Observability & Monitoring

### The Three Pillars

1. **Logs**: What happened (events)
2. **Metrics**: How much (numbers)
3. **Traces**: Where (request flow)

### Essential Metrics

**Technical (RED Method)**:
- **Rate**: Requests per second
- **Errors**: Error rate (< 1%)
- **Duration**: Response time (p50, p95, p99)

**Additional**:
- Uptime (target: > 99.9%)
- CPU/Memory usage
- Database query time
- Cache hit rate

**Product/Business**:
- Time to First API Call (< 10 minutes)
- API consumers count
- Active API keys
- Support tickets created
- Revenue per API (if applicable)

### Logging Strategy

**Structured logs** with correlation IDs:
```json
{
  "timestamp": "2025-02-13T10:00:00Z",
  "level": "INFO",
  "service": "api-gateway",
  "correlation_id": "abc-123-def-456",
  "trace_id": "xyz-789",
  "method": "GET",
  "path": "/orders/123",
  "status": 200,
  "duration_ms": 45,
  "user_id": "user_456",
  "ip": "192.168.1.1"
}
```

**Log Levels**:
- **ERROR**: Failures requiring immediate attention
- **WARN**: Degraded functionality, retries
- **INFO**: Important business events
- **DEBUG**: Detailed diagnostic info (not in production)

**What to log**:
- ✅ API requests/responses (without sensitive data)
- ✅ Authentication events
- ✅ Errors and exceptions
- ✅ Business events (order created, payment processed)
- ❌ Passwords, API keys, credit cards
- ❌ Verbose debug in production

### Health & Readiness Endpoints

```http
GET /health

HTTP/1.1 200 OK
{
  "status": "healthy",
  "version": "1.2.3",
  "uptime": 3600,
  "checks": {
    "database": "healthy",
    "cache": "healthy",
    "external_api": "degraded"
  }
}
```

```http
GET /ready

HTTP/1.1 200 OK  # Ready to accept traffic
HTTP/1.1 503 Service Unavailable  # Not ready
```

```http
GET /version

HTTP/1.1 200 OK
{
  "version": "1.2.3",
  "commit": "abc123def456",
  "build_time": "2025-02-13T10:00:00Z",
  "environment": "production"
}
```

### Distributed Tracing

**Correlation across services**:
```http
# Gateway → Service A → Service B

X-Correlation-ID: unique-request-id
X-Request-ID: unique-request-id
X-Trace-ID: trace-abc-123
X-Span-ID: span-def-456
```

**OpenTelemetry format**:
```http
traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01
tracestate: congo=t61rcWkgMzE
```

### Alerting Strategy

**Alert on symptoms, not causes**:
- ❌ "Database CPU high" (cause)
- ✅ "API response time > 1s" (symptom)

**Alert hierarchy**:
1. **Critical** (page on-call): Service down, data loss
2. **High**: Error rate > 5%, response time > 2s
3. **Medium**: Error rate > 1%, cache miss rate high
4. **Low**: Disk space 80%, certificate expiring

**Runbooks for alerts**:
```markdown
## Alert: High API Error Rate

**Symptom**: Error rate > 5% for 5 minutes
**Impact**: Users experiencing failures
**Investigation**:
1. Check error logs for patterns
2. Verify downstream services
3. Check recent deployments
**Resolution**:
- If deployment issue: Rollback
- If downstream issue: Enable fallback
```

---

## 📚 Documentation Strategy

### Spec-First vs Code-First

**Spec-First** (recommended for):
- Public APIs
- External partners
- Large teams (> 5 developers)
- Stable contracts
- Consumer-driven contracts

**Code-First** (acceptable for):
- Internal APIs
- Rapid prototyping
- Small teams (< 5 developers)
- Frequent changes

### OpenAPI Documentation

**Minimum viable**:
- ✅ Authentication method
- ✅ Base URL
- ✅ All endpoints with descriptions
- ✅ Request/response examples
- ✅ Error codes

**Production-ready**:
- ✅ Interactive documentation (Swagger UI, Redoc)
- ✅ Code examples (cURL, Python, JavaScript)
- ✅ Postman/Bruno collections
- ✅ Migration guides
- ✅ Changelog
- ✅ Rate limits and quotas

**OpenAPI Example**:
```yaml
openapi: 3.1.0
info:
  title: Products API
  version: 1.0.0
  description: API for managing products

servers:
  - url: https://api.example.com/v1
    description: Production
  - url: https://api-staging.example.com/v1
    description: Staging

paths:
  /products:
    get:
      summary: List products
      description: Returns a paginated list of products
      parameters:
        - name: page
          in: query
          schema:
            type: integer
            default: 1
        - name: limit
          in: query
          schema:
            type: integer
            default: 25
            maximum: 100
      responses:
        '200':
          description: Success
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProductList'
              examples:
                default:
                  $ref: '#/components/examples/ProductList'

components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
  
  schemas:
    Product:
      type: object
      required: [id, name, price]
      properties:
        id:
          type: string
          example: "prod_123"
        name:
          type: string
          example: "Laptop"
        price:
          type: number
          format: decimal
          example: 999.99

security:
  - bearerAuth: []
```

### Developer Portal

**For public/partner APIs**:
- ✅ API catalog (all available APIs)
- ✅ Interactive sandbox (try API)
- ✅ Self-service enrollment
- ✅ API key management
- ✅ Usage dashboards
- ✅ Status page
- ✅ Support/feedback channel

**Getting started guide**:
1. Authentication setup
2. First API call example
3. Common use cases
4. Best practices
5. Troubleshooting

### Code Examples

**Provide examples in multiple languages**:

```bash
# cURL
curl -X GET https://api.example.com/v1/products \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json"
```

```python
# Python
import requests

response = requests.get(
    'https://api.example.com/v1/products',
    headers={'Authorization': 'Bearer YOUR_API_KEY'}
)
products = response.json()
```

```javascript
// JavaScript
const response = await fetch('https://api.example.com/v1/products', {
  headers: {
    'Authorization': 'Bearer YOUR_API_KEY',
    'Content-Type': 'application/json'
  }
});
const products = await response.json();
```

### Changelog

**Maintain clear changelog**:
```markdown
## [1.2.0] - 2025-02-13

### Added
- New `/products/search` endpoint for advanced filtering
- Support for cursor-based pagination

### Changed
- Increased rate limit to 1000 requests/minute for premium tier
- Updated error response format to RFC 9457

### Deprecated
- `/products?filter=` query param (use `/products/search` instead)
- Will be removed in v2.0 (2025-08-13)

### Fixed
- Fixed race condition in order creation
- Corrected timezone handling in date filters
```

---

## 🔗 HATEOAS: When It Matters

### Skip for Most APIs

**Reality**: Human developers prefer documentation over link discovery.

**When to skip**:
- Internal APIs
- Mobile apps (predefined flows)
- Simple CRUD APIs
- Performance-critical paths

### Use for AI Agents & Discovery

**When it helps**:
- AI can follow links dynamically
- Self-documenting workflows
- State machines (order lifecycle)
- Complex permission-based actions

**Example**:
```json
{
  "id": "order_123",
  "status": "pending",
  "total": 99.99,
  "_links": {
    "self": {
      "href": "/orders/123",
      "method": "GET"
    },
    "cancel": {
      "href": "/orders/123/cancel",
      "method": "POST",
      "title": "Cancel this order"
    },
    "payment": {
      "href": "/orders/123/payment",
      "method": "POST",
      "title": "Process payment"
    }
  },
  "_actions": {
    "cancel": {
      "href": "/orders/123/cancel",
      "method": "POST",
      "requires": ["permission:order.cancel"]
    }
  }
}
```

**State-based links**:
```json
// Order status: pending
"_links": {
  "cancel": {...},
  "payment": {...}
}

// Order status: paid
"_links": {
  "refund": {...},
  "ship": {...}
}

// Order status: shipped
"_links": {
  "track": {...}
}
```

---

## 🤖 APIs in the AI Era

### Design for LLMs

**AI agents need**:
1. **Clear descriptions** in OpenAPI spec
2. **Rich examples** for each endpoint
3. **Structured errors** for auto-correction
4. **Discovery endpoints** for dynamic exploration
5. **Predictable patterns** (consistent naming, structure)

### MCP (Model Context Protocol)

**Standard for LLM-to-tool communication**:
- Unified interface for AI agents
- Built on JSON-RPC 2.0
- Supports OAuth 2.0 security
- Similar architecture to REST APIs
- Growing ecosystem of MCP servers

**When to provide MCP**:
- API designed for AI consumption
- Tools for LLM workflows
- Integration with AI assistants

### AI-Specific Considerations

**Token optimization**:
- Minimize response size (field selection)
- Compress repeated structures
- Use abbreviations in internal fields

**Latency tolerance**:
- AI calls can be slower (5-10s OK)
- Async processing acceptable
- Streaming responses for long operations

**Non-deterministic behavior**:
- Plan for retry logic
- Provide clear error recovery
- Support idempotency

**Documentation as code**:
- AI reads specs at runtime
- OpenAPI is the source of truth
- Examples teach AI correct usage

### Dynamic Context Loading

**Discovery endpoint**:
```http
GET /api/describe

{
  "version": "1.0.0",
  "endpoints": [
    {
      "path": "/products",
      "methods": ["GET", "POST"],
      "description": "Manage products"
    },
    {
      "path": "/orders",
      "methods": ["GET", "POST"],
      "description": "Manage orders"
    }
  ]
}
```

**Endpoint details**:
```http
GET /api/describe/orders

{
  "path": "/orders",
  "methods": {
    "GET": {
      "description": "List all orders",
      "parameters": ["page", "limit", "status"],
      "example_request": "GET /orders?status=pending",
      "example_response": {...}
    },
    "POST": {
      "description": "Create new order",
      "required_fields": ["items", "customer_id"],
      "example_request": {...}
    }
  }
}
```

---

## ✅ Implementation Checklist

### Phase 1: MVP (50-60% Score)

**Goal**: Launch quickly with basics in place

- [ ] HTTPS only with valid certificate
- [ ] Basic authentication (API keys or OAuth)
- [ ] Resource-based URLs (nouns, not verbs)
- [ ] Standard HTTP methods (GET, POST, PUT, DELETE)
- [ ] RFC 9457 error format
- [ ] Basic input validation
- [ ] README documentation with examples
- [ ] Health endpoint (`/health`)

**Timeline**: 1-2 weeks for new API

### Phase 2: Production (70-80% Score)

**Goal**: Production-ready, reliable API

- [ ] OAuth 2.0 / JWT authentication
- [ ] Comprehensive input validation
- [ ] Pagination for large collections
- [ ] Rate limiting (basic tier limits)
- [ ] OpenAPI 3.x specification
- [ ] Error logging with correlation IDs
- [ ] Basic monitoring (uptime, latency)
- [ ] Timeouts on external calls
- [ ] API versioning strategy defined

**Timeline**: 1-2 months for mature API

### Phase 3: Scale (80-90% Score)

**Goal**: Handle growth, external consumers

- [ ] API versioning implemented
- [ ] Circuit breakers for resilience
- [ ] Distributed tracing
- [ ] Comprehensive monitoring dashboard
- [ ] SLA monitoring and alerts
- [ ] Developer portal (for external APIs)
- [ ] Multiple pagination strategies
- [ ] Caching with ETags
- [ ] Webhook support (if needed)
- [ ] Idempotency for POST operations

**Timeline**: 3-6 months for platform API

### Phase 4: Excellence (90%+ Score)

**Goal**: World-class API, competitive advantage

- [ ] HATEOAS for AI agents (if applicable)
- [ ] Multiple client SDKs
- [ ] Advanced security (mTLS, field-level encryption)
- [ ] Full observability stack (logs, metrics, traces)
- [ ] API governance and standards
- [ ] Automated testing (contract, integration)
- [ ] Performance optimization (< 100ms p95)
- [ ] Multi-region deployment
- [ ] GraphQL/gRPC alternatives (if needed)
- [ ] AI-optimized documentation

**Timeline**: 6-12 months for enterprise API

---

## 🎓 Key Takeaways

### The 5 Non-Negotiables

1. **Security**: Zero Trust + OAuth 2.0 + Input validation
2. **Consistency**: One convention, applied everywhere
3. **Errors**: RFC 9457 format with actionable messages
4. **Documentation**: OpenAPI + examples + getting started
5. **Pragmatism**: Context over dogma, iterate based on feedback

### Design Philosophy

- **API as Product**: Developers are your users - optimize for DX
- **Abstract Design**: Business concepts, not database tables
- **Zero Trust**: Verify everything, trust nothing, assume breach
- **Fail Fast**: Timeouts, circuit breakers, clear errors, quick feedback

### When to Break Rules

✅ **OK to deviate**:
- Action endpoints for non-CRUD operations (`/orders/123/cancel`)
- Skip HATEOAS for human-only APIs
- Delay versioning for internal APIs with few consumers
- Simple pagination over cursor-based for small datasets
- API keys instead of OAuth for simple partner integrations

❌ **Never compromise**:
- Security (HTTPS, authentication, authorization, validation)
- Consistency (pick patterns, document them, stick to them)
- Error clarity (proper status codes, clear messages, actionable)
- Documentation (at least OpenAPI basics + examples)

### Progressive Enhancement

**Start simple, add complexity only when needed**:
1. Begin with MVP (secure, consistent, documented)
2. Gather real usage data
3. Identify actual pain points
4. Add advanced features based on evidence
5. Iterate continuously

---

## 📚 References & Resources

### Standards & RFCs

- [RFC 9110](https://www.rfc-editor.org/rfc/rfc9110.html): HTTP Semantics
- [RFC 9111](https://www.rfc-editor.org/rfc/rfc9111.html): HTTP Caching
- [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457.html): Problem Details for HTTP APIs
- [RFC 6749](https://www.rfc-editor.org/rfc/rfc6749.html): OAuth 2.0 Authorization Framework
- [RFC 7519](https://www.rfc-editor.org/rfc/rfc7519.html): JSON Web Token (JWT)
- [RFC 7396](https://www.rfc-editor.org/rfc/rfc7396.html): JSON Merge Patch
- [RFC 6902](https://www.rfc-editor.org/rfc/rfc6902.html): JSON Patch
- [OpenAPI 3.1](https://spec.openapis.org/oas/v3.1.0): API Specification

### Security Resources

- [OWASP API Security Top 10](https://owasp.org/www-project-api-security/)
- [OAuth 2.1 Draft](https://oauth.net/2.1/)
- [JWT Best Practices](https://datatracker.ietf.org/doc/html/rfc8725)

### Inspirational APIs

- **Stripe**: Excellent documentation, DX-first, clear errors
- **Twilio**: Great onboarding, comprehensive examples
- **GitHub**: Consistent patterns, well-versioned, good conventions
- **Discord**: Balance of simplicity and power
- **Slack**: Intuitive, well-documented, responsive support

### API Guidelines

- [OCTO Technology](https://blog.octo.com/)
- [Microsoft REST API Guidelines](https://github.com/microsoft/api-guidelines)
- [Zalando RESTful API Guidelines](https://opensource.zalando.com/restful-api-guidelines/)
- [Google API Design Guide](https://cloud.google.com/apis/design)
- [Adidas API Guidelines](https://adidas.gitbook.io/api-guidelines/)

### Books & Articles

- "REST API Design Rulebook" by Mark Masse
- "Web API Design" by Brian Mulloy
- "APIs: A Strategy Guide" by Daniel Jacobson

### Tools

- **Documentation**: Swagger UI, Redoc, Stoplight
- **Testing**: Postman, Insomnia, Bruno, REST Client
- **Mocking**: Prism, Mockoon, WireMock
- **Monitoring**: Datadog, New Relic, Grafana
- **Security**: OWASP ZAP, Burp Suite

---

## 📋 Version History

### v3.1 (November 13, 2025)

**Added**:
- Design Process Workflow section
- Enhanced authentication methods (API keys guidance)
- Rate limiting strategies and implementation
- Content negotiation and caching strategies
- Expanded pagination with 3 strategies
- PUT vs PATCH detailed comparison
- Enhanced webhook guidance
- Improved implementation checklist
- Progressive enhancement philosophy

**Improved**:
- Security section with input validation
- HTTP status codes with more examples
- Error handling with additional patterns
- Versioning with lifecycle management
- Observability with structured approach
- Documentation strategy with OpenAPI examples

**Reorganized**:
- Quality scoring system (updated weights)
- Better category organization
- Clearer conditional vs mandatory requirements

### v3.0 (November 13, 2025)
- Initial comprehensive guidelines
- Core philosophy and principles
- Security, architecture, and patterns
- AI era considerations

---

**License**: Creative Commons CC-BY-4.0 - Free to use and adapt  
**Feedback**: Contributions and suggestions welcome  
**Philosophy**: *"Perfect is the enemy of good. Ship secure, consistent, documented APIs. Iterate based on real usage."*

---

*These guidelines balance REST principles with practical software development. Use them as a framework for decision-making, not as rigid rules. The best API is one that is secure, works reliably for your users, and can evolve with your needs.*

**Questions?** Start with the implementation checklist for your project type, focus on the non-negotiables, and progressively enhance based on actual requirements.
