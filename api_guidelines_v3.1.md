# Pragmatic RESTful API Design Guidelines

**Version**: 3.1
**Date**: November 13, 2025
**Based On**: OCTO Technology + Pragmatic Experience + Community Feedback  
**Philosophy**: Balance REST principles with practical development needs while addressing modern challenges

**What's New in 3.1**:
- ✨ Complete Testing section
- 🔐 Enhanced Security (rate limiting, validation, CORS)
- 💾 Caching & Concurrency control
- 🔔 Webhooks design patterns
- 📁 File handling best practices
- ⚠️ Anti-patterns section
- 🚀 Expanded Performance guidelines

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

---

## 📊 API Quality Scoring System

### Scoring Categories & Weights

| Category            | Weight | Mandatory?     | Context                    |
|---------------------|--------|----------------|----------------------------|
| **Security**        | 18%    | ✅ Always       | Zero Trust, OAuth 2.0      |
| **Error Handling**  | 10%    | ✅ Always       | RFC9457, clear messages    |
| **Versioning**      | 12%    | ⚠️ Conditional | Mobile apps, ext. clients  |
| **URL Structure**   | 8%     | ✅ Always       | Resource-oriented design   |
| **HTTP Methods**    | 8%     | ✅ Always       | Idempotency matters        |
| **Status Codes**    | 7%     | ✅ Always       | Standard + 422, 429        |
| **Pagination**      | 7%     | ⚠️ Conditional | Large collections only     |
| **Caching**         | 5%     | ✅ Always       | ETags, Cache-Control       |
| **HATEOAS**         | 3%     | ❌ Optional     | AI/discovery scenarios     |
| **Documentation**   | 8%     | ✅ Always       | OpenAPI + examples         |
| **Testing**         | 5%     | ✅ Always       | Contract, load, security   |
| **Observability**   | 5%     | ✅ Always       | Logs, metrics, traces      |
| **Performance**     | 7%     | ✅ Always       | Response times, compression|
| **Architecture**    | 5%     | ⚠️ Conditional | Proper API types           |

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

### HTTPS is Mandatory

- ✅ Valid TLS certificates (Let's Encrypt)
- ✅ TLS 1.3 preferred, minimum TLS 1.2
- ✅ Enable HSTS with preload: `Strict-Transport-Security: max-age=31536000; includeSubDomains; preload`
- ✅ Disable TLS 1.0/1.1
- ❌ Never self-signed certs in production

### OAuth 2.0 Flow Selection

**Client Credentials**: Server-to-server, machine-to-machine  
**Authorization Code + PKCE**: Web/mobile apps with users (mandatory in OAuth 2.1)  
**Device Grant**: IoT devices, smart TVs  
**Refresh Token Rotation**: Implement for mobile apps

### JWT Best Practices

**Validation checklist**:
- ✅ Signature (using JWKS)
- ✅ Expiration (exp)
- ✅ Issuer (iss)
- ✅ Audience (aud)
- ✅ Not Before (nbf)
- ✅ JWT ID (jti) for revocation tracking

**Token Management**:
- Access tokens: Short-lived (15 min - 1 hour)
- Refresh tokens: Long-lived (7-30 days) with rotation
- Store sensitive data server-side, not in JWT

**Scopes vs Permissions**:
- **Scopes**: What application CAN do
- **Permissions**: What user IS ALLOWED to do
- Effective = User permissions ∩ Client scopes

### Rate Limiting

**Essential for all APIs** to prevent abuse and ensure fair usage.

**Strategies**:
1. **Token Bucket**: Burst allowed, refills over time
2. **Sliding Window**: More accurate but complex
3. **Fixed Window**: Simple but can have edge cases

**Implementation**:
```http
# Headers to include in ALL responses
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1672531200

# On rate limit exceeded
HTTP/1.1 429 Too Many Requests
Retry-After: 3600
```

**Recommended Limits**:
- **Unauthenticated**: 10 req/min per IP
- **Authenticated**: 1000 req/hour per user
- **Premium tier**: 10,000 req/hour
- **Burst**: Allow 2x normal rate for 10 seconds

**Per-Endpoint Limits**:
```
POST /orders: 10/min (expensive operation)
GET /products: 100/min (read-only, cacheable)
POST /auth/login: 5/min (prevent brute force)
```

### Input Validation

**Never trust client input**. Validate everything server-side.

**Validation Layers**:
1. **Schema validation**: JSON Schema, Zod, Yup
2. **Business rules**: Custom validators
3. **Sanitization**: Remove/escape dangerous content

**Example with JSON Schema**:
```json
{
  "type": "object",
  "required": ["email", "password"],
  "properties": {
    "email": {
      "type": "string",
      "format": "email",
      "maxLength": 255
    },
    "password": {
      "type": "string",
      "minLength": 12,
      "maxLength": 128,
      "pattern": "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])"
    },
    "age": {
      "type": "integer",
      "minimum": 18,
      "maximum": 150
    }
  },
  "additionalProperties": false
}
```

**Common Vulnerabilities**:
- ❌ SQL Injection: Use parameterized queries
- ❌ NoSQL Injection: Validate MongoDB operators
- ❌ XSS: Escape HTML in responses
- ❌ Path Traversal: Validate file paths
- ❌ Command Injection: Never pass user input to shell

**Content-Type Validation**:
```javascript
// Only accept expected content types
if (req.headers['content-type'] !== 'application/json') {
  return res.status(415).json({
    type: "UNSUPPORTED_MEDIA_TYPE",
    title: "Unsupported Media Type",
    status: 415,
    detail: "Content-Type must be application/json"
  });
}
```

### CORS Configuration

**Essential for browser-based clients**.

**Restrictive Configuration** (recommended):
```http
Access-Control-Allow-Origin: https://app.example.com
Access-Control-Allow-Methods: GET, POST, PUT, DELETE
Access-Control-Allow-Headers: Content-Type, Authorization
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 86400
```

**Never use in production**:
```http
Access-Control-Allow-Origin: *  # ❌ Too permissive
Access-Control-Allow-Headers: *  # ❌ Security risk
```

**Preflight Handling**:
```javascript
// OPTIONS request for preflight
app.options('/api/*', (req, res) => {
  res.header('Access-Control-Allow-Origin', 'https://app.example.com');
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE');
  res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization');
  res.status(204).send();
});
```

### API Keys Management

**For server-to-server integrations**.

**Best Practices**:
- Generate cryptographically random keys (min 32 bytes)
- Hash keys before storage (bcrypt, Argon2)
- Support multiple keys per client
- Enable key rotation without downtime
- Allow scope restrictions per key

**Key Formats**:
```
sk_live_51HxT2jKZv...  # Stripe-style prefix
api_key_abc123def456   # Generic format
```

**Rotation Process**:
```http
# 1. Generate new key
POST /api-keys
→ {"key": "new_key_xyz", "expires_at": "2026-11-13"}

# 2. Update clients to use new key (grace period)

# 3. Revoke old key
DELETE /api-keys/old_key_abc
```

### Security Headers

**Add to all responses**:
```http
Strict-Transport-Security: max-age=31536000; includeSubDomains; preload
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Content-Security-Policy: default-src 'self'
Referrer-Policy: strict-origin-when-cross-origin
Permissions-Policy: geolocation=(), microphone=(), camera=()
```

### Secrets Management

**Never hardcode secrets**. Use secret management services.

**Recommended Tools**:
- AWS Secrets Manager / Systems Manager Parameter Store
- HashiCorp Vault
- Azure Key Vault
- Google Secret Manager

**Environment Variables**:
```bash
# ❌ Bad
DATABASE_URL=postgresql://user:password@localhost/db

# ✅ Good - use secret reference
DATABASE_URL=${SECRET:db-connection-string}
```

### Data Encryption

**At Rest**:
- Encrypt PII fields in database (AES-256)
- Use database-level encryption (TDE)
- Encrypt backups

**In Transit**:
- TLS 1.3 for all connections
- mTLS for service-to-service
- End-to-end encryption for sensitive data

### OWASP API Top 10 (2023)

1. **Broken Object Level Authorization**: Always verify user owns resource
2. **Broken Authentication**: Implement OAuth 2.0 + MFA
3. **Broken Object Property Authorization**: Validate writable fields
4. **Unrestricted Resource Consumption**: Rate limiting + pagination limits
5. **Broken Function Level Authorization**: Check permissions per endpoint
6. **Unrestricted Business Flow Access**: Prevent workflow bypassing
7. **Server Side Request Forgery**: Validate URLs, use allowlists
8. **Security Misconfiguration**: Harden defaults, remove debug endpoints
9. **Improper Inventory Management**: Maintain API catalog, deprecate old versions
10. **Unsafe API Consumption**: Validate third-party API responses

---

## 🏗️ URL Structure

### Path Structure

```
https://api.example.com/[domain]/v1/[resource]
```

### Essential Rules

1. **Nouns, not verbs**: `GET /products` not `GET /getProducts`
2. **Plural collections**: `GET /orders/123` not `GET /order/123`
3. **Consistent naming**: Choose kebab-case, camelCase, or snake_case - stick to it
4. **Max 2 levels nesting**: `GET /users/123/orders` OK, deeper nesting creates tight coupling

### Pragmatic Exceptions

Actions that don't fit CRUD:
```
✅ POST /orders/123/cancel
✅ POST /users/456/reset-password
✅ POST /subscriptions/789/pause
✅ POST /documents/abc/publish
```

### Resource Granularity

**Start coarse-grained**, split when needed:
```
GET /users/123  # All user data
GET /users/123?fields=id,name,email  # Selective fields (preferred)
GET /users/123/profile  # Separate resource (if complex)
```

### Query Parameters Standards

**Filtering**:
```
GET /products?category=electronics&price_min=100&price_max=500
GET /users?status=active&role=admin
GET /orders?created_after=2025-01-01&created_before=2025-12-31
```

**Sorting**:
```
GET /products?sort=price:asc
GET /products?sort=name:asc,created_at:desc  # Multiple fields
```

**Field Selection**:
```
GET /users?fields=id,name,email
GET /users?fields=*,-password,-secret  # Exclude fields
```

**Search**:
```
GET /products?q=laptop  # Simple search
GET /products?search=title:laptop OR description:laptop  # Advanced
```

---

## 🔧 HTTP Methods & Status Codes

### HTTP Methods

| Method   | Use           | Idempotent | Safe | Example              |
|----------|---------------|------------|------|----------------------|
| GET      | Read          | ✅          | ✅   | `GET /products/123`  |
| POST     | Create/Action | ❌          | ❌   | `POST /orders`       |
| PUT      | Replace       | ✅          | ❌   | `PUT /products/123`  |
| PATCH    | Update        | ❌          | ❌   | `PATCH /users/456`   |
| DELETE   | Delete        | ✅          | ❌   | `DELETE /orders/789` |
| HEAD     | Metadata      | ✅          | ✅   | `HEAD /file.pdf`     |
| OPTIONS  | Capabilities  | ✅          | ✅   | `OPTIONS /products`  |

### Comprehensive Status Codes

**2xx Success**:
- **200 OK**: Standard success response with body
- **201 Created**: Resource created, include `Location` header
- **202 Accepted**: Async processing started
- **204 No Content**: Success with no response body (DELETE)
- **206 Partial Content**: Range request (file streaming)

**3xx Redirection**:
- **301 Moved Permanently**: Resource URL changed forever
- **302 Found**: Temporary redirect
- **304 Not Modified**: Client cache is still valid (with ETags)
- **307 Temporary Redirect**: Keep HTTP method on redirect
- **308 Permanent Redirect**: Keep HTTP method, permanent

**4xx Client Errors**:
- **400 Bad Request**: Malformed request syntax
- **401 Unauthorized**: Missing or invalid authentication
- **403 Forbidden**: Authenticated but not authorized
- **404 Not Found**: Resource doesn't exist
- **405 Method Not Allowed**: HTTP method not supported for endpoint
- **406 Not Acceptable**: Cannot produce requested Accept format
- **409 Conflict**: Resource state conflict (duplicate, version mismatch)
- **410 Gone**: Resource permanently deleted
- **412 Precondition Failed**: If-Match/If-None-Match failed
- **413 Payload Too Large**: Request body exceeds limit
- **415 Unsupported Media Type**: Wrong Content-Type
- **422 Unprocessable Entity**: Validation errors
- **423 Locked**: Resource is locked (WebDAV)
- **429 Too Many Requests**: Rate limit exceeded

**5xx Server Errors**:
- **500 Internal Server Error**: Generic server error
- **501 Not Implemented**: Feature not supported
- **502 Bad Gateway**: Invalid response from upstream
- **503 Service Unavailable**: Temporary outage, include `Retry-After`
- **504 Gateway Timeout**: Upstream timeout

### Idempotency

**For POST idempotency**, use Idempotency-Key header:
```http
POST /payments
Idempotency-Key: unique-id-12345
Content-Type: application/json

{
  "amount": 10000,
  "currency": "EUR"
}
```

**Server handling**:
```javascript
// Check if request already processed
const existing = await redis.get(`idempotency:${idempotencyKey}`);
if (existing) {
  return res.status(existing.status).json(existing.body);
}

// Process request
const result = await processPayment(data);

// Store result for 24h
await redis.setex(`idempotency:${idempotencyKey}`, 86400, {
  status: 201,
  body: result
});

return res.status(201).json(result);
```

---

## 📝 Error Handling (RFC 9457)

### Standard Error Format

```json
{
  "type": "https://api.example.com/errors/insufficient-funds",
  "title": "Insufficient Funds",
  "status": 422,
  "detail": "Account balance is 30 EUR, but transaction requires 100 EUR",
  "instance": "/transactions/tx_123456",
  "error_code": "INSUFFICIENT_FUNDS",
  "trace_id": "abc-123-xyz-789",
  "timestamp": "2025-11-13T14:30:00Z",
  "retryable": false
}
```

### Error Response Fields

**Required**:
- `type`: URI identifying error type (documentation link preferred)
- `title`: Short, human-readable summary
- `status`: HTTP status code
- `detail`: Specific explanation for this occurrence

**Recommended**:
- `instance`: URI identifying this specific occurrence
- `error_code`: Application-specific code (e.g., `USER_NOT_FOUND`)
- `trace_id`: Correlation ID for logging/debugging
- `timestamp`: ISO 8601 timestamp
- `retryable`: Boolean indicating if retry makes sense

### Multiple Validation Errors

```json
{
  "type": "https://api.example.com/errors/validation-failed",
  "title": "Validation Failed",
  "status": 400,
  "detail": "Request body contains 3 validation errors",
  "errors": [
    {
      "field": "email",
      "code": "INVALID_FORMAT",
      "message": "Must be a valid email address",
      "rejected_value": "not-an-email"
    },
    {
      "field": "age",
      "code": "OUT_OF_RANGE",
      "message": "Must be between 18 and 150",
      "rejected_value": 15
    },
    {
      "field": "password",
      "code": "TOO_WEAK",
      "message": "Must contain at least one uppercase, lowercase, digit, and special character"
    }
  ]
}
```

### Application Error Codes

**Establish consistent error code taxonomy**:

```
# Format: DOMAIN_CATEGORY_SPECIFIC
USER_AUTH_INVALID_CREDENTIALS
USER_AUTH_TOKEN_EXPIRED
USER_VALIDATION_EMAIL_TAKEN
ORDER_BUSINESS_INSUFFICIENT_STOCK
ORDER_PAYMENT_DECLINED
SYSTEM_RATE_LIMIT_EXCEEDED
SYSTEM_MAINTENANCE_IN_PROGRESS
```

### Internationalization

**Support multiple languages**:

```http
GET /users/999
Accept-Language: fr-FR

{
  "type": "https://api.example.com/errors/not-found",
  "title": "Ressource non trouvée",
  "status": 404,
  "detail": "L'utilisateur avec l'ID 999 n'existe pas",
  "error_code": "USER_NOT_FOUND"
}
```

### Development vs Production

**Development** (include stack traces):
```json
{
  "type": "https://api.example.com/errors/internal-error",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "Database connection failed",
  "trace_id": "abc-123",
  "stack_trace": [
    "at Database.connect (db.js:42)",
    "at UserService.findById (user.service.js:15)"
  ],
  "query": "SELECT * FROM users WHERE id = $1"
}
```

**Production** (sanitized):
```json
{
  "type": "https://api.example.com/errors/internal-error",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "An unexpected error occurred. Please try again later.",
  "trace_id": "abc-123",
  "support_contact": "support@example.com"
}
```

### Error Tracking Integration

**Include trace ID for correlation**:
```javascript
const traceId = generateTraceId();

// Log to Sentry/Rollbar
Sentry.captureException(error, {
  tags: { trace_id: traceId, endpoint: '/users' },
  extra: { user_id: req.user?.id }
});

// Return to client
res.status(500).json({
  type: "INTERNAL_ERROR",
  title: "Internal Server Error",
  status: 500,
  trace_id: traceId,
  detail: "Reference this ID when contacting support"
});
```

### Retry Guidance

**For transient errors, guide clients**:
```http
HTTP/1.1 503 Service Unavailable
Retry-After: 120

{
  "type": "SERVICE_UNAVAILABLE",
  "title": "Service Temporarily Unavailable",
  "status": 503,
  "detail": "Service is under maintenance",
  "retryable": true,
  "retry_after_seconds": 120
}
```

---

## 💾 Caching & Concurrency Control

### HTTP Caching Headers

**Cache-Control**:
```http
# Public, cacheable for 1 hour
Cache-Control: public, max-age=3600

# Private (user-specific), 5 minutes
Cache-Control: private, max-age=300

# No caching
Cache-Control: no-store, no-cache, must-revalidate

# Revalidate with origin before serving stale
Cache-Control: max-age=3600, must-revalidate

# Serve stale while revalidating in background
Cache-Control: max-age=3600, stale-while-revalidate=86400
```

**Recommended by Resource Type**:
```
GET /products (public catalog): public, max-age=3600
GET /users/me (user data): private, max-age=300
GET /cart (session data): private, no-store
GET /static/logo.png: public, max-age=31536000, immutable
```

### ETags for Conditional Requests

**Server generates ETag**:
```http
GET /users/123

HTTP/1.1 200 OK
ETag: "v1.2.3"
Cache-Control: max-age=300

{"id": 123, "name": "John", "email": "john@example.com"}
```

**Client conditional request**:
```http
GET /users/123
If-None-Match: "v1.2.3"

# If unchanged
HTTP/1.1 304 Not Modified
ETag: "v1.2.3"

# If changed
HTTP/1.1 200 OK
ETag: "v2.0.0"
{...updated data...}
```

**ETag Generation Strategies**:
```javascript
// 1. Hash of content
const etag = crypto.createHash('md5').update(JSON.stringify(data)).digest('hex');

// 2. Version number
const etag = `"v${resource.version}"`;

// 3. Last modified timestamp
const etag = `"${resource.updatedAt.getTime()}"`;

// 4. Weak ETag (for approximately equivalent)
const etag = `W/"${hash}"`;
```

### Optimistic Locking

**Prevent concurrent update conflicts**:

```http
# 1. Client fetches resource
GET /users/123

HTTP/1.1 200 OK
ETag: "v1"
{"id": 123, "name": "John", "version": 1}

# 2. Client updates with If-Match
PUT /users/123
If-Match: "v1"

{"name": "John Smith"}

# Success
HTTP/1.1 200 OK
ETag: "v2"
{"id": 123, "name": "John Smith", "version": 2}

# Conflict (someone else updated)
HTTP/1.1 412 Precondition Failed
{
  "type": "CONFLICT",
  "title": "Resource Modified",
  "status": 412,
  "detail": "Resource has been modified by another user",
  "current_version": 3,
  "your_version": 1
}
```

**Server implementation**:
```javascript
app.put('/users/:id', async (req, res) => {
  const ifMatch = req.headers['if-match'];
  
  const user = await db.users.findById(req.params.id);
  if (!user) return res.status(404).json({...});
  
  if (ifMatch && user.version !== parseInt(ifMatch.replace(/"/g, ''))) {
    return res.status(412).json({
      type: "CONFLICT",
      status: 412,
      detail: "Resource has been modified",
      current_version: user.version
    });
  }
  
  user.name = req.body.name;
  user.version++;
  await user.save();
  
  res.setHeader('ETag', `"${user.version}"`);
  res.json(user);
});
```

### Cache Invalidation

**Strategies**:

1. **Time-based**: Set appropriate max-age
2. **Event-based**: Invalidate on updates
3. **Tag-based**: Group related resources

**Cache-Tag header** (Fastly, Cloudflare):
```http
HTTP/1.1 200 OK
Cache-Control: public, max-age=3600
Cache-Tag: user-123, org-456

# Invalidate all resources with tag
PURGE /purge
Cache-Tags: user-123
```

**Surrogate-Key** (Varnish):
```http
HTTP/1.1 200 OK
Surrogate-Key: user-123 org-456

# Purge by key
PURGE /
X-Surrogate-Key: user-123
```

### CDN Integration

**Optimizing for CDN caching**:

```http
# Long cache for immutable resources
GET /assets/app-v1.2.3.js
Cache-Control: public, max-age=31536000, immutable

# Vary by Accept header for content negotiation
Vary: Accept, Accept-Encoding

# CDN-specific headers
CDN-Cache-Control: max-age=86400  # CDN caches longer than browser
Cloudflare-CDN-Cache-Control: max-age=86400
```

### Distributed Locking

**For critical operations** (payments, inventory):

```javascript
// Using Redis
const lock = await redis.set(
  `lock:order:${orderId}`,
  'locked',
  'EX', 10,  // 10 seconds expiry
  'NX'       // Only if not exists
);

if (!lock) {
  return res.status(409).json({
    type: "RESOURCE_LOCKED",
    status: 409,
    detail: "Order is being processed by another request"
  });
}

try {
  await processOrder(orderId);
} finally {
  await redis.del(`lock:order:${orderId}`);
}
```

---

## 📄 Pagination & Filtering

### Page-Based Pagination

**Best for**: Small datasets, UIs with page numbers

```http
GET /products?page=2&limit=25

HTTP/1.1 200 OK
Link: <https://api.example.com/products?page=1&limit=25>; rel="first",
      <https://api.example.com/products?page=1&limit=25>; rel="prev",
      <https://api.example.com/products?page=3&limit=25>; rel="next",
      <https://api.example.com/products?page=50&limit=25>; rel="last"

{
  "items": [...],
  "pagination": {
    "page": 2,
    "limit": 25,
    "total": 1234,
    "total_pages": 50,
    "_links": {
      "first": "/products?page=1&limit=25",
      "prev": "/products?page=1&limit=25",
      "next": "/products?page=3&limit=25",
      "last": "/products?page=50&limit=25"
    }
  }
}
```

**Performance Warning**: `COUNT(*)` can be expensive on large tables.

**Solution**:
```sql
-- Estimate count for large datasets
SELECT reltuples::bigint AS estimate FROM pg_class WHERE relname = 'products';

-- Or paginate without total
{
  "pagination": {
    "page": 2,
    "limit": 25,
    "has_next": true,
    "has_prev": true
  }
}
```

### Cursor-Based Pagination

**Best for**: Large datasets, real-time feeds, infinite scroll

```http
GET /messages?cursor=eyJpZCI6MTIzLCJ0cyI6MTYzOTQ3MjAwMH0&limit=50

{
  "items": [...],
  "pagination": {
    "next_cursor": "eyJpZCI6MTczLCJ0cyI6MTYzOTQ3MzAwMH0",
    "prev_cursor": "eyJpZCI6NzMsInRzIjoxNjM5NDcxMDAwfQ",
    "has_next": true,
    "has_prev": true,
    "limit": 50
  }
}
```

**Cursor encoding**:
```javascript
// Encode cursor (base64 of JSON)
const cursor = Buffer.from(JSON.stringify({
  id: lastItem.id,
  timestamp: lastItem.created_at
})).toString('base64url');

// Decode cursor
const decoded = JSON.parse(Buffer.from(cursor, 'base64url').toString());

// Use in query
SELECT * FROM messages 
WHERE (created_at, id) < (?, ?)
ORDER BY created_at DESC, id DESC
LIMIT 50
```

**Advantages**:
- Consistent results even with inserts/deletes
- Works with real-time data
- No expensive COUNT query needed
- Better performance on large datasets

### Seek Pagination (Keyset)

**Best for**: Extremely large datasets, best performance

```http
GET /logs?since_id=1000&limit=100

{
  "items": [...],
  "pagination": {
    "since_id": 1000,
    "until_id": 1100,
    "limit": 100,
    "has_next": true
  }
}
```

**SQL Query**:
```sql
-- Forward pagination
SELECT * FROM logs WHERE id > 1000 ORDER BY id ASC LIMIT 100;

-- Backward pagination
SELECT * FROM logs WHERE id < 1000 ORDER BY id DESC LIMIT 100;
```

### Pagination Limits

**Enforce maximum page size** to prevent abuse:
```javascript
const limit = Math.min(req.query.limit || 25, 100); // Max 100 items
```

**Return appropriate error**:
```http
GET /products?limit=1000

HTTP/1.1 400 Bad Request
{
  "type": "INVALID_PARAMETER",
  "status": 400,
  "detail": "Limit must be between 1 and 100",
  "max_limit": 100
}
```

### Filtering & Sorting

**Complex Filters**:
```http
# Range filters
GET /products?price_min=100&price_max=500

# Multiple values (OR)
GET /products?category=electronics,books

# Nested filters
GET /products?attributes.color=red&attributes.size=large

# Date ranges
GET /orders?created_after=2025-01-01T00:00:00Z&created_before=2025-12-31T23:59:59Z

# Text search
GET /products?q=laptop&fields=title,description
```

**Sorting Best Practices**:
```http
# Multiple fields
GET /products?sort=category:asc,price:desc

# Always include stable sort field (e.g., id)
GET /products?sort=name:asc,id:asc
```

---

## 🔄 API Versioning

### When Mandatory

- Mobile apps in production (can't force updates)
- External API consumers (>10)
- Public API
- Breaking changes needed
- SLA commitments

### Strategies

**URL Path** (recommended): `GET /v1/products`  
**Query Param**: `GET /products?v=1`  
**Header**: `Accept: application/vnd.api+json; version=1`  
**Custom Header**: `X-API-Version: 1`

**Pros/Cons**:
```
URL Path:
  ✅ Clear, visible, cacheable
  ❌ Changes all URLs

Header:
  ✅ Clean URLs
  ❌ Invisible, harder to test, cache complexity

Query Param:
  ✅ Fallback friendly
  ❌ Clutters URLs
```

### Version Lifecycle

**Deprecation Process**:

1. **Announce** (T-6 months):
```http
GET /v1/products

HTTP/1.1 200 OK
Deprecation: true
Sunset: Wed, 11 May 2026 23:59:59 GMT
Link: <https://api.example.com/v2/docs>; rel="successor-version"
```

2. **Warning Period** (T-3 months):
```http
Warning: 299 - "API v1 is deprecated. Migrate to v2 by May 2026"
```

3. **Grace Period** (T-1 month):
- Send emails to active consumers
- Provide migration guide
- Offer support calls

4. **Shutdown**:
```http
GET /v1/products

HTTP/1.1 410 Gone
{
  "type": "VERSION_DEPRECATED",
  "status": 410,
  "detail": "API v1 was shut down on May 11, 2026",
  "migration_guide": "https://docs.example.com/migrate-v1-to-v2"
}
```

### Backward Compatibility

**Non-breaking changes** (safe to deploy):
- ✅ Adding optional fields to requests
- ✅ Adding fields to responses
- ✅ Adding new endpoints
- ✅ Adding new optional query parameters
- ✅ Making required fields optional

**Breaking changes** (require new version):
- ❌ Removing fields from responses
- ❌ Renaming fields
- ❌ Changing field types
- ❌ Making optional fields required
- ❌ Changing validation rules (stricter)
- ❌ Changing error responses
- ❌ Removing endpoints

### Semantic Versioning for APIs

```
v2.5.3
│ │ │
│ │ └─ Patch: Bug fixes, no API changes
│ └─── Minor: New features, backward compatible
└───── Major: Breaking changes
```

### Version Management

**Maintain max 2 versions simultaneously**:
```
v3 (current) - Full support
v2 (legacy)  - Security fixes only
v1 (deprecated) - Scheduled for removal
```

---

## 🏛️ API Architecture Patterns

### API Types

1. **Core APIs**: Expose business capabilities, master of data
2. **Facade APIs**: Wrapper over legacy systems (transitional)
3. **Process APIs**: Orchestrate multiple core APIs
4. **Experience APIs (BFF)**: Tailored for specific client needs

### When to Use Each

**Core APIs**:
- Own domain data
- Single responsibility
- Used by multiple consumers
- Example: User Service, Product Service

**Facade APIs**:
- Temporary wrapper over legacy SOAP/mainframe
- Gradually modernize backend
- Translation layer
- Example: Legacy-to-REST adapter

**Process APIs**:
- Orchestrate business workflows
- Span multiple domains
- Stateless coordination
- Example: Order Fulfillment (inventory + payment + shipping)

**BFF (Backend for Frontend)**:
- One per client platform
- Aggregate multiple API calls
- Optimize payload for specific UI
- Example: Mobile BFF, Web BFF, Smart TV BFF

### Microservices vs Monolith

**Start with Modular Monolith**, extract to microservices only when:
- Team size > 10 developers
- Clear bounded contexts exist
- Independent scaling needed
- DevOps maturity achieved (CI/CD, monitoring, orchestration)

**Conway's Law**: "Organizations design systems that mirror their communication structure"

**When NOT to use microservices**:
- Small team (<5 developers)
- Simple domain
- Tight coupling between features
- Immature DevOps practices

### API Gateway Pattern

**Responsibilities**:
- Authentication & authorization
- Rate limiting
- Request routing
- Response aggregation
- Protocol translation (REST → gRPC)
- Caching
- Logging & monitoring

**Popular solutions**: Kong, AWS API Gateway, Azure API Management, Traefik

---

## 🔄 Sync vs Async Integration

### Use REST (Sync) When

- Real-time response needed (< 5 seconds)
- Simple request-response pattern
- Frontend to backend communication
- External partner integrations
- Read operations

### Use Messaging (Async) When

- Long-running processes (> 10 seconds)
- High volume data transfer (>1000 msg/sec)
- Event-driven architecture
- Decoupled microservices
- Fire-and-forget operations
- Eventual consistency acceptable

### REST vs Other Protocols

**REST**:
- ✅ Universal compatibility
- ✅ Easy to debug (curl, Postman)
- ✅ Stateless, cacheable
- ❌ Over-fetching/under-fetching
- ❌ Multiple round-trips

**GraphQL**:
- ✅ Flexible queries, fetch exactly what you need
- ✅ Single endpoint
- ✅ Strong typing
- ❌ Caching complexity
- ❌ Learning curve
- **Use for**: BFF scenarios, mobile apps

**gRPC**:
- ✅ High performance (binary protocol)
- ✅ Strong contracts (Protobuf)
- ✅ Bidirectional streaming
- ❌ Not browser-friendly (needs grpc-web)
- ❌ Harder to debug
- **Use for**: Internal microservices, high-throughput

**WebSockets**:
- ✅ Real-time, bidirectional
- ✅ Low latency
- ❌ Stateful (harder to scale)
- ❌ Load balancer complexity
- **Use for**: Chat, live updates, gaming

**Server-Sent Events (SSE)**:
- ✅ Simple, HTTP-based
- ✅ Auto-reconnection
- ✅ Works through proxies
- ❌ Unidirectional only
- **Use for**: Live feeds, notifications

---

## ⚡ Resilience Patterns

### Timeouts

**Set timeouts for ALL external calls**:
```javascript
// API calls
const response = await fetch(url, { 
  signal: AbortSignal.timeout(3000) // 3 seconds
});

// Database queries
await db.query('SELECT * FROM users WHERE id = ?', [id], {
  timeout: 1000 // 1 second
});

// Third-party APIs
const stripe = await stripe.charges.create({...}, {
  timeout: 10000 // 10 seconds
});
```

**Recommended timeouts**:
- Internal API: 2-3 seconds
- Database: 1 second
- Third-party API: 5-10 seconds
- Payment gateway: 30 seconds

### Retries with Exponential Backoff

```javascript
async function retryWithBackoff(fn, maxRetries = 3) {
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await fn();
    } catch (error) {
      if (i === maxRetries - 1) throw error;
      
      // Exponential backoff: 100ms, 200ms, 400ms
      const delay = Math.min(100 * Math.pow(2, i), 1000);
      await sleep(delay);
    }
  }
}

// Only retry on transient errors
if (error.code === 'ECONNRESET' || error.status === 503) {
  await retryWithBackoff(() => fetchData());
}
```

**Retry-After header**:
```http
HTTP/1.1 503 Service Unavailable
Retry-After: 60

# Respect server's guidance
const retryAfter = parseInt(response.headers.get('retry-after'));
await sleep(retryAfter * 1000);
```

### Circuit Breaker

**Prevents cascade failures**:

```javascript
class CircuitBreaker {
  constructor(threshold = 5, timeout = 60000) {
    this.failureCount = 0;
    this.threshold = threshold;
    this.timeout = timeout;
    this.state = 'CLOSED'; // CLOSED, OPEN, HALF_OPEN
    this.nextAttempt = Date.now();
  }
  
  async call(fn) {
    if (this.state === 'OPEN') {
      if (Date.now() < this.nextAttempt) {
        throw new Error('Circuit breaker is OPEN');
      }
      this.state = 'HALF_OPEN';
    }
    
    try {
      const result = await fn();
      this.onSuccess();
      return result;
    } catch (error) {
      this.onFailure();
      throw error;
    }
  }
  
  onSuccess() {
    this.failureCount = 0;
    this.state = 'CLOSED';
  }
  
  onFailure() {
    this.failureCount++;
    if (this.failureCount >= this.threshold) {
      this.state = 'OPEN';
      this.nextAttempt = Date.now() + this.timeout;
    }
  }
}

// Usage
const breaker = new CircuitBreaker();
await breaker.call(() => fetch('https://api.example.com'));
```

### Bulkhead Pattern

**Isolate resources to prevent total failure**:

```javascript
// Separate thread pools per service
const userServicePool = new ConnectionPool({ max: 10 });
const paymentServicePool = new ConnectionPool({ max: 5 });
const notificationServicePool = new ConnectionPool({ max: 20 });

// If payment service is down, user service still works
```

**Container-level isolation**:
```yaml
# Docker resource limits
services:
  api:
    cpus: '2'
    mem_limit: 2gb
  worker:
    cpus: '4'
    mem_limit: 4gb
```

### Graceful Degradation

**Fallback to cached/default values**:
```javascript
async function getUserRecommendations(userId) {
  try {
    return await recommendationService.get(userId);
  } catch (error) {
    logger.warn('Recommendation service down, using fallback');
    // Fallback to popular items
    return await getPopularItems();
  }
}
```

---

## 🔔 Webhooks Design

### What Are Webhooks?

**Webhooks = Reverse APIs**: Server calls client when event occurs.

**Use cases**:
- Payment confirmations (Stripe)
- Order updates (Shopify)
- CI/CD triggers (GitHub)
- Real-time notifications

### Webhook Registration

```http
POST /webhooks
Content-Type: application/json

{
  "url": "https://your-app.com/webhooks/payment",
  "events": ["payment.succeeded", "payment.failed"],
  "secret": "whsec_abc123..."
}

HTTP/1.1 201 Created
{
  "id": "wh_123",
  "url": "https://your-app.com/webhooks/payment",
  "events": ["payment.succeeded", "payment.failed"],
  "status": "active",
  "created_at": "2025-11-13T14:00:00Z"
}
```

### Webhook Payload

```http
POST https://your-app.com/webhooks/payment
Content-Type: application/json
X-Webhook-ID: wh_123
X-Webhook-Event: payment.succeeded
X-Webhook-Signature: sha256=abc123...
X-Webhook-Delivery-ID: del_456
X-Webhook-Timestamp: 1699888800

{
  "id": "evt_789",
  "type": "payment.succeeded",
  "created_at": "2025-11-13T14:30:00Z",
  "data": {
    "payment_id": "pay_123",
    "amount": 10000,
    "currency": "EUR",
    "status": "succeeded"
  }
}
```

### Security: HMAC Signatures

**Verify webhook authenticity**:

```javascript
// Server: Generate signature
const crypto = require('crypto');

function generateSignature(payload, secret) {
  return crypto
    .createHmac('sha256', secret)
    .update(JSON.stringify(payload))
    .digest('hex');
}

// Client: Verify signature
function verifyWebhook(req, secret) {
  const signature = req.headers['x-webhook-signature'].replace('sha256=', '');
  const expected = generateSignature(req.body, secret);
  
  if (!crypto.timingSafeEqual(Buffer.from(signature), Buffer.from(expected))) {
    throw new Error('Invalid signature');
  }
}

// Usage
app.post('/webhooks/payment', (req, res) => {
  try {
    verifyWebhook(req, process.env.WEBHOOK_SECRET);
    // Process webhook
    res.sendStatus(200);
  } catch (error) {
    res.sendStatus(401);
  }
});
```

### Replay Protection

**Prevent replay attacks with timestamps**:

```javascript
function verifyTimestamp(timestamp, maxAge = 300) {
  const now = Math.floor(Date.now() / 1000);
  if (Math.abs(now - timestamp) > maxAge) {
    throw new Error('Webhook timestamp too old');
  }
}
```

### Retry Strategy

**Implement exponential backoff**:

```
Attempt 1: Immediate
Attempt 2: 1 minute later
Attempt 3: 5 minutes later
Attempt 4: 30 minutes later
Attempt 5: 2 hours later
Attempt 6: 6 hours later (final)
```

**Response requirements**:
- Client must respond within 5 seconds
- 2xx status = success
- 4xx status = don't retry (client error)
- 5xx status = retry (server error)

### Webhook Management

**Dashboard for consumers**:
- List webhook events
- View delivery attempts
- Retry failed deliveries
- Pause/resume webhooks
- View payload history

**Example API**:
```http
# List deliveries
GET /webhooks/wh_123/deliveries

# Retry delivery
POST /webhooks/deliveries/del_456/retry

# Test webhook
POST /webhooks/wh_123/test
```

### Idempotency

**Clients must handle duplicate deliveries**:

```javascript
app.post('/webhooks/payment', async (req, res) => {
  const eventId = req.body.id;
  
  // Check if already processed
  const existing = await db.processedEvents.findOne({ event_id: eventId });
  if (existing) {
    return res.sendStatus(200); // Already processed
  }
  
  // Process event
  await processPayment(req.body.data);
  
  // Mark as processed
  await db.processedEvents.create({ event_id: eventId });
  
  res.sendStatus(200);
});
```

---

## 📁 File Upload & Download

### File Upload Strategies

**1. Direct Upload (Small Files < 10MB)**:

```http
POST /documents
Content-Type: multipart/form-data
Content-Length: 5242880

--boundary
Content-Disposition: form-data; name="file"; filename="document.pdf"
Content-Type: application/pdf

<binary data>
--boundary--

HTTP/1.1 201 Created
Location: /documents/doc_123
{
  "id": "doc_123",
  "filename": "document.pdf",
  "size": 5242880,
  "content_type": "application/pdf",
  "url": "https://api.example.com/documents/doc_123"
}
```

**2. Presigned URLs (Large Files > 10MB)**:

```http
# Step 1: Request presigned URL
POST /documents/upload-url
{
  "filename": "video.mp4",
  "content_type": "video/mp4",
  "size": 104857600
}

HTTP/1.1 200 OK
{
  "upload_url": "https://s3.amazonaws.com/bucket/key?signature=...",
  "document_id": "doc_456",
  "expires_at": "2025-11-13T15:00:00Z"
}

# Step 2: Client uploads directly to S3
PUT https://s3.amazonaws.com/bucket/key?signature=...
Content-Type: video/mp4
<binary data>

# Step 3: Confirm upload
POST /documents/doc_456/confirm
```

**3. Chunked Upload (Very Large Files > 1GB)**:

```http
# Initialize multipart upload
POST /documents/multipart
{
  "filename": "large-file.zip",
  "content_type": "application/zip",
  "size": 5368709120
}

HTTP/1.1 201 Created
{
  "upload_id": "upload_789",
  "chunk_size": 5242880,
  "total_chunks": 1024
}

# Upload chunks
PUT /documents/upload_789/chunks/1
Content-Range: bytes 0-5242879/5368709120
<chunk data>

PUT /documents/upload_789/chunks/2
Content-Range: bytes 5242880-10485759/5368709120
<chunk data>

# Complete upload
POST /documents/upload_789/complete
{
  "chunks": [1, 2, ..., 1024]
}
```

### File Download

**Simple download**:
```http
GET /documents/doc_123

HTTP/1.1 200 OK
Content-Type: application/pdf
Content-Disposition: attachment; filename="document.pdf"
Content-Length: 5242880
<binary data>
```

**Range requests (streaming)**:
```http
GET /videos/video_123
Range: bytes=0-1048575

HTTP/1.1 206 Partial Content
Content-Type: video/mp4
Content-Range: bytes 0-1048575/104857600
Content-Length: 1048576
<chunk data>
```

### File Validation

**Security checks**:

```javascript
const allowedTypes = ['image/jpeg', 'image/png', 'application/pdf'];
const maxSize = 10 * 1024 * 1024; // 10MB

if (!allowedTypes.includes(file.mimetype)) {
  return res.status(415).json({
    type: "UNSUPPORTED_FILE_TYPE",
    status: 415,
    detail: `File type ${file.mimetype} is not allowed`,
    allowed_types: allowedTypes
  });
}

if (file.size > maxSize) {
  return res.status(413).json({
    type: "FILE_TOO_LARGE",
    status: 413,
    detail: `File size ${file.size} exceeds limit of ${maxSize}`,
    max_size: maxSize
  });
}

// Verify file content matches extension
const fileType = await FileType.fromBuffer(file.buffer);
if (fileType.mime !== file.mimetype) {
  return res.status(400).json({
    type: "FILE_TYPE_MISMATCH",
    status: 400,
    detail: "File content does not match declared type"
  });
}
```

### Virus Scanning

**Integrate antivirus**:
```javascript
const ClamScan = require('clamscan');
const clamscan = await new ClamScan().init();

const { isInfected, viruses } = await clamscan.scanFile(filePath);
if (isInfected) {
  return res.status(400).json({
    type: "MALWARE_DETECTED",
    status: 400,
    detail: "File contains malware",
    viruses: viruses
  });
}
```

---

## 🧪 Testing

### Contract Testing

**Ensure API matches specification**:

```javascript
// Using Pact
const { Pact } = require('@pact-foundation/pact');

describe('User API', () => {
  const provider = new Pact({
    consumer: 'FrontendApp',
    provider: 'UserAPI'
  });
  
  it('should get user by ID', async () => {
    await provider.addInteraction({
      state: 'user 123 exists',
      uponReceiving: 'a request for user 123',
      withRequest: {
        method: 'GET',
        path: '/users/123'
      },
      willRespondWith: {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
        body: {
          id: 123,
          name: 'John Doe',
          email: 'john@example.com'
        }
      }
    });
    
    const response = await fetch('http://localhost:1234/users/123');
    expect(response.status).toBe(200);
  });
});
```

**OpenAPI validation**:
```javascript
// Using express-openapi-validator
const OpenApiValidator = require('express-openapi-validator');

app.use(
  OpenApiValidator.middleware({
    apiSpec: './openapi.yaml',
    validateRequests: true,
    validateResponses: true
  })
);
```

### Load Testing

**Using k6**:

```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '1m', target: 10 },   // Ramp up
    { duration: '3m', target: 100 },  // Peak load
    { duration: '1m', target: 0 },    // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% < 500ms
    http_req_failed: ['rate<0.01'],   // < 1% errors
  },
};

export default function () {
  const res = http.get('https://api.example.com/products');
  
  check(res, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });
  
  sleep(1);
}
```

**Run**:
```bash
k6 run --vus 100 --duration 5m load-test.js
```

### Security Testing

**Using OWASP ZAP**:

```bash
# Run ZAP baseline scan
docker run -v $(pwd):/zap/wrk:rw -t owasp/zap2docker-stable \
  zap-baseline.py -t https://api.example.com -r report.html
```

**Manual security checklist**:
- [ ] SQL injection attempts
- [ ] XSS payloads in inputs
- [ ] Authentication bypass
- [ ] Authorization escalation
- [ ] Rate limit bypass
- [ ] CORS misconfiguration
- [ ] Sensitive data exposure

### Integration Testing

```javascript
// Using Supertest
const request = require('supertest');
const app = require('../app');

describe('Order API', () => {
  let authToken;
  
  beforeAll(async () => {
    // Login
    const res = await request(app)
      .post('/auth/login')
      .send({ email: 'test@example.com', password: 'password' });
    authToken = res.body.token;
  });
  
  it('should create order', async () => {
    const res = await request(app)
      .post('/orders')
      .set('Authorization', `Bearer ${authToken}`)
      .send({
        items: [{ product_id: 123, quantity: 2 }],
        shipping_address: {...}
      });
    
    expect(res.status).toBe(201);
    expect(res.body).toHaveProperty('id');
    expect(res.body.status).toBe('pending');
  });
  
  it('should get order by ID', async () => {
    const orderId = '...';
    const res = await request(app)
      .get(`/orders/${orderId}`)
      .set('Authorization', `Bearer ${authToken}`);
    
    expect(res.status).toBe(200);
    expect(res.body.id).toBe(orderId);
  });
});
```

### Chaos Engineering

**Test resilience under failure**:

```javascript
// Using chaos-monkey
const chaos = require('chaos-monkey');

// Random latency
chaos.addLatency({
  probability: 0.1,  // 10% of requests
  delay: 5000        // 5 second delay
});

// Random failures
chaos.addError({
  probability: 0.05,  // 5% of requests
  statusCode: 503
});

app.use(chaos.middleware);
```

### Smoke Tests

**Post-deployment validation**:

```bash
#!/bin/bash
# smoke-test.sh

API_URL="https://api.example.com"

# Health check
if ! curl -sf $API_URL/health > /dev/null; then
  echo "Health check failed"
  exit 1
fi

# Test authentication
TOKEN=$(curl -sf -X POST $API_URL/auth/login \
  -d '{"email":"test@example.com","password":"test"}' \
  -H "Content-Type: application/json" | jq -r '.token')

if [ -z "$TOKEN" ]; then
  echo "Authentication failed"
  exit 1
fi

# Test critical endpoint
if ! curl -sf -H "Authorization: Bearer $TOKEN" $API_URL/users/me > /dev/null; then
  echo "User endpoint failed"
  exit 1
fi

echo "All smoke tests passed"
```

---

## ⚡ Performance Optimization

### Compression

**Enable Gzip/Brotli**:

```javascript
const compression = require('compression');

app.use(compression({
  level: 6,  // Compression level (0-9)
  threshold: 1024,  // Only compress if > 1KB
  filter: (req, res) => {
    if (req.headers['x-no-compression']) {
      return false;
    }
    return compression.filter(req, res);
  }
}));
```

**Response headers**:
```http
HTTP/1.1 200 OK
Content-Encoding: gzip
Content-Length: 1234 (compressed)
Vary: Accept-Encoding
```

### HTTP/2 & HTTP/3

**HTTP/2 benefits**:
- Multiplexing (multiple requests over single connection)
- Header compression
- Server push
- Binary protocol

**Enable HTTP/2**:
```javascript
const http2 = require('http2');
const fs = require('fs');

const server = http2.createSecureServer({
  key: fs.readFileSync('key.pem'),
  cert: fs.readFileSync('cert.pem')
});

server.on('stream', (stream, headers) => {
  stream.respond({
    'content-type': 'application/json',
    ':status': 200
  });
  stream.end(JSON.stringify({ message: 'Hello HTTP/2' }));
});
```

### Database Query Optimization

**N+1 Query Problem**:

```javascript
// ❌ Bad: N+1 queries
const users = await User.findAll();
for (const user of users) {
  user.orders = await Order.findAll({ where: { userId: user.id } });
}

// ✅ Good: Single query with join
const users = await User.findAll({
  include: [{ model: Order }]
});
```

**Use connection pooling**:
```javascript
const pool = new Pool({
  host: 'localhost',
  database: 'mydb',
  max: 20,  // Max connections
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 2000
});
```

### Response Optimization

**Minimize payload size**:

```javascript
// ❌ Bad: Return everything
{
  "id": 123,
  "name": "John",
  "email": "john@example.com",
  "password_hash": "$2b$10$...",  // ❌ Never send
  "created_at": "2025-01-01T00:00:00Z",
  "updated_at": "2025-11-13T14:00:00Z",
  "last_login": "2025-11-13T10:00:00Z",
  "settings": {...},  // Large nested object
  "audit_log
