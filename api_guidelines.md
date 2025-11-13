# RESTful API Design Guidelines

## 🎯 Purpose
This document teaches how to design clean, consistent, and developer-friendly RESTful APIs based on industry best practices, OCTO Technology standards, and real-world experience from Web Giants.

**Target Audience**: AI assistants, developers, and architects designing REST APIs  
**Philosophy**: Balance REST purity with pragmatic usability

---

## 🧭 Core Philosophy & Architecture

### Primary Design Principles (Priority Order)

1. **👥 Design for developers, NOT your database**
   - Think about how clients will consume your API
   - Optimize for developer experience (DX)
   - Hide internal complexity

2. **💡 Keep it Simple (KISS)**
   - API must be self-explanatory without documentation
   - Use intuitive, predictable patterns
   - Minimize cognitive load

3. **🎯 One way to do things**
   - Never allow multiple approaches for same task
   - Eliminates confusion and decision paralysis
   - Easier to learn and maintain

4. **🗣️ Use shared vocabulary**
   - Standard, concrete terms only
   - No internal jargon or business acronyms
   - Terms everyone understands immediately

5. **🚀 Prioritize main use cases first**
   - 80/20 rule: optimize for common scenarios
   - Handle edge cases and exceptions later
   - Don't let rare cases complicate common ones

6. **⚖️ Balance purity with pragmatism**
   - Follow REST principles where sensible
   - Deviate when it improves usability
   - Document intentional deviations

### Web Oriented Architecture (WOA)

Build fast, scalable, and secure APIs based on:

- ✅ **REST principles** (stateless, resource-oriented)
- ✅ **HATEOAS** (hypermedia-driven discoverability)
- ✅ **Stateless microservices** (decoupled architecture)
- ✅ **Asynchronous patterns** (non-blocking operations)
- ✅ **OAuth2** (authorization) + **OpenID Connect** (authentication)
- ✅ **HTTPS everywhere** (mandatory security)

### Success Criteria

Your API is successful when it's:

| Quality | Description |
|---------|-------------|
| **Intuitive** | Developers understand it without reading docs |
| **Consistent** | Patterns repeat predictably throughout |
| **Discoverable** | Clients can explore via hypermedia links |
| **Secure** | Protected by default, not as afterthought |
| **Fast** | Optimized responses, efficient pagination |
| **Well-documented** | Clear examples, edge cases covered |

---

## 🌐 URL Structure & Naming

### Resource Naming Rules

| Rule | ✅ Correct | ❌ Incorrect | Why |
|------|-----------|-------------|-----|
| **Nouns, not verbs** | `GET /orders` | `GET /getAllOrders` | REST = resources, not RPC calls |
| **Plural forms** | `/users`, `/users/007` | `/user/007` | Consistency for collections & instances |
| **Spinal-case** | `/specific-orders` | `/specificOrders` | Some servers ignore case |
| **No trailing slash** | `/orders` | `/orders/` | Cleaner, more consistent |

**Examples**:
```http
✅ GET /orders
✅ GET /users
✅ GET /products
✅ POST /specific-orders

❌ GET /getAllOrders
❌ GET /user/007
❌ GET /specificOrders
```

### Attribute & Parameter Naming

**Choose one convention and stick to it:**

| Convention | Example Query | Example JSON |
|------------|---------------|--------------|
| **snake_case** | `?id_user=007` | `{"id_user":"007"}` |
| **camelCase** | `?idUser=007` | `{"idUser":"007"}` |

⚠️ **Never mix both** - pick one for entire API

### URL Hierarchy (Resource Relationships)

Reflect composition/aggregation through URL structure:

```http
GET /orders/1234/products/1
# "products belong to orders"

GET /users/007/orders
# "orders belong to user 007"

GET /companies/42/departments/5/employees
# "employees belong to department 5 of company 42"
```

**Maximum nesting: 2 levels deep**

```http
✅ /users/007/orders
✅ /orders/1234/items

❌ /users/007/orders/1234/items/5/reviews
```

### Versioning (Mandatory from Day 1)

**Include version at top of URL path:**

```http
✅ GET /v1/orders
✅ GET /v2/orders
✅ POST /v1/users

❌ GET /orders?version=1
❌ GET /orders (no version)
```

**Guidelines**:
- Version at highest scope
- Use major versions only (v1, v2, not v1.2.3)
- Support max **2 versions simultaneously**
- Consider native app update cycles (can't force updates)

**Alternative** (less common):
```http
Accept: application/vnd.api.v1+json
```

### Resource Granularity

**Use medium-grained resources** (not fine, not coarse):

```json
GET /users/007

✅ Medium-grained (good):
{
  "id": "007",
  "first_name": "James",
  "name": "Bond",
  "address": {
    "street": "Horsen Ferry Road",
    "city": {"name": "London"}
  }
}

❌ Too fine-grained (bad):
# Requires multiple calls
GET /users/007
GET /users/007/first-name
GET /users/007/address
GET /users/007/address/city

❌ Too coarse-grained (bad):
# Returns massive payload with everything
{
  "id": "007",
  "first_name": "James",
  ...[50 more fields]...,
  "orders": [...100 orders...],
  "preferences": {...},
  "activity_log": [...]
}
```

**Rule**: Maximum 2 nested levels in response

---

## 🧩 HTTP Methods (CRUD Operations)

### Complete Method Matrix

| HTTP Verb | Collection: `/orders` | Instance: `/orders/{id}` | Idempotent? | Safe? |
|-----------|----------------------|--------------------------|-------------|-------|
| **GET** | List orders (200) | Get single order (200) | ✅ | ✅ |
| **POST** | Create order (201) | ❌ Not used | ❌ | ❌ |
| **PUT** | ❌ Not used | Full replace (200) / Create with ID (201) | ✅ | ❌ |
| **PATCH** | ❌ Not used | Partial update (200) | ❌ | ❌ |
| **DELETE** | ❌ Not used | Delete order (204) | ✅ | ❌ |

**Definitions**:
- **Idempotent**: Same result when called multiple times
- **Safe**: Read-only, no side effects

### GET - Read Resources

```http
# Read collection
GET /orders HTTP/1.1
Host: api.example.com
Accept: application/json

HTTP/1.1 200 OK
Content-Type: application/json

[
  {"id":"1234", "state":"paid", "total":42.50},
  {"id":"5678", "state":"running", "total":99.99}
]
```

```http
# Read single instance
GET /orders/1234 HTTP/1.1
Host: api.example.com

HTTP/1.1 200 OK
Content-Type: application/json

{"id":"1234", "state":"paid", "total":42.50, "user_id":"007"}
```

**Key Points**:
- No side effects (safe)
- Can be cached
- Idempotent (same result every time)

### POST - Create Resource (Server Assigns ID)

```http
POST /orders HTTP/1.1
Host: api.example.com
Content-Type: application/json

{"state":"running", "user_id":"007", "total":42.50}

HTTP/1.1 201 Created
Location: https://api.example.com/v1/orders/1234
Content-Type: application/json

{"id":"1234", "state":"running", "user_id":"007", "total":42.50}
```

**Key Points**:
- Server generates ID
- Returns `201 Created`
- `Location` header contains new resource URI
- NOT idempotent (creates new resource each time)

### PUT - Create with Client ID OR Full Replace

```http
# Create with client-specified ID
PUT /orders/1234 HTTP/1.1
Host: api.example.com
Content-Type: application/json

{"id":"1234", "state":"running", "user_id":"007", "total":42.50}

HTTP/1.1 201 Created
Location: https://api.example.com/v1/orders/1234
```

```http
# Full replacement (ALL fields required)
PUT /orders/1234 HTTP/1.1
Host: api.example.com
Content-Type: application/json

{"id":"1234", "state":"paid", "user_id":"007", "total":42.50}

HTTP/1.1 200 OK
Content-Type: application/json

{"id":"1234", "state":"paid", "user_id":"007", "total":42.50}
```

**Key Points**:
- Idempotent (same result if called multiple times)
- Requires ALL fields (full replacement)
- Use for upsert pattern (create if not exists, replace if exists)

### PATCH - Partial Update

```http
PATCH /orders/1234 HTTP/1.1
Host: api.example.com
Content-Type: application/json

{"state":"paid"}

HTTP/1.1 200 OK
Content-Type: application/json

{"id":"1234", "state":"paid", "user_id":"007", "total":42.50}
```

**Key Points**:
- Only send changed fields
- NOT idempotent (depends on current state)
- More efficient than PUT for small changes

### DELETE - Remove Resource

```http
DELETE /orders/1234 HTTP/1.1
Host: api.example.com

HTTP/1.1 204 No Content
```

**Key Points**:
- Returns `204 No Content` (success with no body)
- Idempotent (deleting twice = same result)
- Second DELETE may return `404 Not Found`

---

## 🔢 HTTP Status Codes

### Success Codes (2xx)

| Code | Name | When to Use | Example Scenario |
|------|------|-------------|------------------|
| **200** | OK | General success (GET, PUT, PATCH) | Retrieved user data |
| **201** | Created | Resource created (POST, PUT new) | New order created |
| **202** | Accepted | Async processing started | Job queued for processing |
| **204** | No Content | Success, no body to return (DELETE) | Order deleted successfully |
| **206** | Partial Content | Paginated/range response | Page 2 of 50 |

### Client Error Codes (4xx)

| Code | Name | Meaning | When Client Can Fix |
|------|------|---------|---------------------|
| **400** | Bad Request | Malformed request, invalid params | ✅ Fix request format |
| **401** | Unauthorized | No credentials or invalid token | ✅ Provide authentication |
| **403** | Forbidden | Authenticated but insufficient rights | ❌ Need permission grant |
| **404** | Not Found | Resource doesn't exist | ✅ Check resource ID/URL |
| **405** | Method Not Allowed | HTTP method not supported | ✅ Use different method |
| **406** | Not Acceptable | Can't satisfy Accept headers | ✅ Request different format |
| **409** | Conflict | Request conflicts with current state | ✅ Resolve conflict |
| **422** | Unprocessable Entity | Valid syntax but semantic errors | ✅ Fix business logic error |
| **429** | Too Many Requests | Rate limit exceeded | ✅ Wait and retry |

### Server Error Codes (5xx)

| Code | Name | Meaning | Client Action |
|------|------|---------|---------------|
| **500** | Internal Server Error | Unexpected server problem | Retry later, contact support |
| **502** | Bad Gateway | Upstream service failed | Retry later |
| **503** | Service Unavailable | Temporary overload/maintenance | Retry with backoff |
| **504** | Gateway Timeout | Upstream service timeout | Retry later |

### Standardized Error Response Format

**Always use consistent JSON structure:**

```json
{
  "error": "error_code",
  "error_description": "Human-readable explanation"
}
```

**Real Examples**:

```http
GET /bookings?paid=true
→ 400 Bad Request
{
  "error": "invalid_request",
  "error_description": "There is no 'paid' property. Did you mean 'payment_status'?"
}
```

```http
GET /orders/123
→ 401 Unauthorized
{
  "error": "no_credentials",
  "error_description": "You must be authenticated. Include 'Authorization: Bearer <token>' header."
}
```

```http
GET /admin/users
→ 403 Forbidden
{
  "error": "protected_resource",
  "error_description": "You need admin role to access this resource."
}
```

```http
GET /hotels/999999
→ 404 Not Found
{
  "error": "not_found",
  "error_description": "The hotel '999999' does not exist."
}
```

```http
POST /orders
Accept-Language: zh
→ 406 Not Acceptable
{
  "error": "not_acceptable",
  "error_description": "Available languages: en, fr, es"
}
```

---

## 🔍 Query Parameters

### Filtering

Use `?` with query parameters to filter collections:

```http
GET /orders?state=paid&user_id=007
GET /products?category=electronics&in_stock=true
GET /users?role=admin&active=true
```

**Alternative** (hierarchical):
```http
GET /users/007/orders?state=paid
# Multiple URIs can reference same resources
```

### Sorting

**Default**: Ascending order  
**Syntax**: 
- `?sort=attr1,attr2` (ascending)
- `?desc=attr1,attr2` (descending)
- Mix both for complex sorting

```http
# Sort by rating DESC, then reviews DESC, then name ASC
GET /restaurants?sort=rating,reviews,name&desc=rating,reviews

# Sort by price ascending
GET /products?sort=price

# Sort by created date descending
GET /orders?sort=created_at&desc=created_at
```

### Pagination (MANDATORY)

**Every collection endpoint MUST implement pagination**

#### Why Mandatory?
- Prevents performance issues
- Controls response size
- Protects server resources
- Better mobile experience

#### Implementation

**Request**:
```http
GET /orders?range=48-55 HTTP/1.1
Host: api.example.com
```

**Response**:
```http
HTTP/1.1 206 Partial Content
Content-Type: application/json
Content-Range: 48-55/971
Accept-Range: order 10
Link: <https://api.example.com/v1/orders?range=0-7>; rel="first",
      <https://api.example.com/v1/orders?range=40-47>; rel="prev",
      <https://api.example.com/v1/orders?range=56-64>; rel="next",
      <https://api.example.com/v1/orders?range=968-975>; rel="last"

[
  {"id":"1248", "state":"paid"},
  {"id":"1249", "state":"running"},
  ...
]
```

**Required Headers**:
- `Content-Range`: Shows current position (48-55 of 971 total)
- `Accept-Range`: Indicates pagination unit and default size
- `Link`: Navigation links (first, prev, next, last)

**Default Pagination**:
```http
GET /orders
# Same as: GET /orders?range=0-25
```

⚠️ **Warning**: Adding resources during pagination may cause items to appear on wrong pages

**Alternative Pagination Styles**:
```http
# Offset-based (common)
GET /orders?limit=25&offset=50

# Page-based (less efficient)
GET /orders?page=3&per_page=25

# Cursor-based (best for real-time data)
GET /orders?cursor=eyJpZCI6MTIzNH0&limit=25
```

### Partial Responses (Field Selection)

**Critical for mobile** - reduces bandwidth and parsing time:

```http
GET /users/007?fields=firstname,name,address(street) HTTP/1.1

HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "007",
  "firstname": "James",
  "name": "Bond",
  "address": {
    "street": "Horsen Ferry Road"
  }
}
```

**Benefits**:
- Reduces payload size
- Faster response times
- Lower bandwidth costs
- Better mobile performance

**Syntax Examples**:
```http
# Select specific fields
GET /users/007?fields=id,name,email

# Nested field selection
GET /users/007?fields=name,address(street,city)

# Multiple resources
GET /orders?fields=id,total,user(name,email)
```

### Search

#### Resource-Specific Search
```http
GET /restaurants/search?type=thai
GET /products/search?q=laptop&category=electronics
GET /users/search?name=john&role=admin
```

#### Global Search (Google-Style)
```http
GET /search?q=running+paid
GET /search?q=order+status:paid+user:007
```

### Reserved Keywords

| Keyword | Purpose | Example | Response |
|---------|---------|---------|----------|
| `/first` | First element | `GET /orders/first` | `{"id":"1234", "state":"paid"}` |
| `/last` | Last element | `GET /orders/last` | `{"id":"5678", "state":"running"}` |
| `/count` | Collection size | `GET /orders/count` | `{"count": 971}` |

**Use Cases**:
```http
# Get most recent order
GET /orders/last

# Check if any orders exist
GET /orders/count

# Get first user
GET /users/first
```

---

## 🌍 Content Negotiation & Formats

### Content Type Negotiation

**Use Accept header** (RESTful way), not URL extensions:

```http
Accept: application/json, text/plain

✅ GET /orders
❌ GET /orders.json
```

**Multiple formats with priority**:
```http
Accept: application/json, application/xml;q=0.9, */*;q=0.8
# Prefers JSON, accepts XML, accepts anything else
```

**Default format**: Always JSON

### Date/Time/Timestamp Format

**Use ISO 8601 standard** exclusively:

```json
{
  "created_at": "1978-05-10T06:06:06+00:00",
  "updated_at": "2025-01-15T14:30:00Z",
  "birth_date": "1978-05-10"
}
```

**Formats**:
- Full timestamp: `YYYY-MM-DDThh:mm:ss+00:00`
- UTC timezone: `YYYY-MM-DDThh:mm:ssZ`
- Date only: `YYYY-MM-DD`

❌ **Never use**:
- `05/10/1978` (ambiguous)
- `1978-10-5` (inconsistent)
- Unix timestamps alone (not human-readable)

### Internationalization (i18n)

**Use Accept-Language header**, not query params:

```http
Accept-Language: fr-CA, fr-FR;q=0.9, en;q=0.8

✅ Accept-Language: fr-CA
❌ ?language=fr
❌ ?lang=fr
```

**Response**:
```http
HTTP/1.1 200 OK
Content-Language: fr-CA
```

If language not available:
```http
HTTP/1.1 406 Not Acceptable

{
  "error": "not_acceptable",
  "error_description": "Available languages: en, fr, es"
}
```

### Cross-Origin Requests (CORS)

**Required for browser-based applications**:

```http
# Preflight request
OPTIONS /orders HTTP/1.1
Origin: https://example.com
Access-Control-Request-Method: POST

# Response
HTTP/1.1 204 No Content
Access-Control-Allow-Origin: https://example.com
Access-Control-Allow-Methods: GET, POST, PUT, DELETE
Access-Control-Allow-Headers: Content-Type, Authorization
Access-Control-Max-Age: 86400
```

**Actual request**:
```http
POST /orders HTTP/1.1
Origin: https://example.com

HTTP/1.1 201 Created
Access-Control-Allow-Origin: https://example.com
```

### JSONP (Legacy Support Only)

**For Internet Explorer 7/8/9** - avoid if possible:

```http
# Standard REST → JSONP equivalent
POST /orders        → GET /orders.jsonp?method=POST&callback=handleOrder
GET /orders         → GET /orders.jsonp?callback=handleOrders
GET /orders/1234    → GET /orders/1234.jsonp?callback=handleOrder
PUT /orders/1234    → GET /orders/1234.jsonp?method=PUT&callback=handleOrder
```

⚠️ **JSONP Limitations**:
- All requests forced to GET
- Cannot use Accept header
- Cannot send request body
- **Security risk**: Web crawlers can trigger actions

**Required Security**:
- Must require OAuth2 `access_token`
- Must require OAuth2 `client_id`
- Prevents accidental data modification

---

## 🔐 Security

### Non-Negotiable Requirements

| Requirement | Why Mandatory |
|-------------|---------------|
| **HTTPS everywhere** | Prevents man-in-the-middle attacks, encrypts tokens |
| **OAuth2 for authorization** | Industry standard, covers 99% of use cases |
| **OpenID Connect for authentication** | Built on OAuth2, proven at scale |
| **Never custom auth** | You will miss edge cases and vulnerabilities |

### OAuth2 Benefits

✅ Handles all authorization scenarios:
- User-to-service
- Service-to-service
- Mobile apps
- SPAs
- Third-party integrations

✅ Proven at scale by:
- Google
- Facebook
- GitHub
- Microsoft
- Twitter

✅ Extensive ecosystem:
- Libraries for all languages
- Well-understood by developers
- Extensive documentation

### Security Headers

**Include in every response**:
```http
Strict-Transport-Security: max-age=31536000; includeSubDomains
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Content-Security-Policy: default-src 'self'
```

### Authentication Flow Example

```http
# 1. Get access token
POST /oauth2/token HTTP/1.1
Host: oauth2.example.com
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials&
client_id=YOUR_CLIENT_ID&
client_secret=YOUR_CLIENT_SECRET

HTTP/1.1 200 OK
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600
}

# 2. Use token in API requests
GET /v1/orders HTTP/1.1
Host: api.example.com
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

HTTP/1.1 200 OK
[...]
```

---

## 🔗 HATEOAS (Hypermedia)

### Purpose & Philosophy

**HATEOAS** = Hypermedia as the Engine of Application State

**Goal**: Make API completely discoverable - clients navigate by following links

### Implementation with RFC5988

Return possible actions in **Link header**:

```http
GET /users/007 HTTP/1.1
Host: api.example.com
Authorization: Bearer xxx

HTTP/1.1 200 OK
Content-Type: application/json
Link: <https://api.example.com/v1/users/007>; rel="self"; method="GET",
      <https://api.example.com/v1/users/007>; rel="edit"; method="PUT",
      <https://api.example.com/v1/users/007>; rel="delete"; method="DELETE",
      <https://api.example.com/v1/users/007/orders>; rel="orders"; method="GET",
      <https://api.example.com/v1/users/007/addresses>; rel="addresses"; method="GET"

{
  "id": "007",
  "firstname": "James",
  "name": "Bond",
  "email": "james.bond@mi6.gov.uk"
}
```

### Link Relation Types

| Relation | Meaning |
|----------|---------|
| `self` | Current resource |
| `edit` | URL to update resource |
| `delete` | URL to delete resource |
| `next` | Next page in collection |
| `prev` | Previous page |
| `first` | First page |
| `last` | Last page |
| `related` | Related resource |

### Pragmatic Reality

**Most developers will**:
- ❌ Not use hypermedia links
- ✅ Read documentation
- ✅ Hardcode URLs
- ✅ Copy/paste examples

**But still provide links because**:
- Future-proofs API
- Enables API exploration tools
- Supports automated clients
- Makes API truly RESTful
- Costs little to implement

### State Machine via Links

Links show what actions are possible from current state:

```http
# Order in "pending" state
GET /orders/1234

Link: <.../orders/1234>; rel="self",
      <.../orders/1234>; rel="cancel"; method="DELETE",
      <.../orders/1234/pay>; rel="payment"; method="POST"

# After payment - different actions available
GET /orders/1234

Link: <.../orders/1234>; rel="self",
      <.../orders/1234/ship>; rel="shipment"; method="POST",
      <.../orders/1234/refund>; rel="refund"; method="POST"
```

---

## ⚡ Non-Resource Scenarios (Operations)

### When Resources Don't Fit

Sometimes you need **operations** instead of resources:

```http
POST /emails/42/send
POST /calculator/sum
Body: [1, 2, 3, 5, 8, 13, 21]

POST /convert?from=EUR&to=USD&amount=42
POST /orders/1234/cancel
POST /users/007/reset-password
```

### Guidelines for Operations

1. **✅ Always try RESTful approach first**
2. **✅ Use POST method** for operations
3. **✅ Put verb at end of URI**
4. **✅ Document why resource model doesn't fit**

### RESTful Alternatives (Prefer These)

```http
# Instead of: POST /send-email/42
✅ Better: POST /emails/42/deliveries

# Instead of: POST /activate-user/123
✅ Better: PATCH /users/123
Body: {"status": "active"}

# Instead of: POST /approve-order/1234
✅ Better: PATCH /orders/1234
Body: {"approval_status": "approved"}

# Instead of: POST /cancel-subscription/567
✅ Better: DELETE /subscriptions/567
```

### When Operations Are Justified

✅ **Complex calculations**:
```http
POST /calculator/sum
POST /calculator/compound-interest
```

✅ **External integrations**:
```http
POST /payments/process
POST /emails/send-batch
```

✅ **Workflow actions**:
```http
POST /orders/1234/ship
POST /documents/456/publish
```

---

## 🏗️ API Infrastructure

### Domain Structure

**Separate concerns with subdomains**:

| Subdomain | Purpose | Example | Audience |
|-----------|---------|---------|----------|
| **api.** | Production API | `https://api.company.com` | Production apps |
| **api.sandbox.** | Test/Dev API | `https://api.sandbox.company.com` | Development/testing |
| **developers.** | Developer portal | `https://developers.company.com` | API consumers |
| **oauth2.** | Auth production | `https://oauth2.company.com` | Auth requests |
| **oauth2.sandbox.** | Auth testing | `https://oauth2.sandbox.company.com` | Auth testing |

### Benefits of Separation

✅ Clear environment distinction  
✅ Separate rate limits  
✅ Different security policies  
✅ Independent scaling  
✅ Isolated failures  

---

## 📚 Documentation Best Practices

### Use cURL Examples

**Why cURL?**
- ✅ Universal tool (available everywhere)
- ✅ Copy/paste ready
- ✅ Shows all details (headers, body, auth)
- ✅ Easy to convert to any language

**Example**:
```bash
curl -X POST \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer at-80003004-19a8-46a2-908e-33d4057128e7" \
  -d '{"state":"running", "user_id":"007"}' \
  https://api.company.com/v1/orders?client_id=API_KEY_003
```

### Documentation Must Include

1. **Clear endpoint description**
   - What it does
   - When to use it
   - Authentication required?

2. **Request examples**
   - Headers
   - Body (if applicable)
   - Query parameters

3. **Response examples**
   - Success case
   - Common error cases
   - All possible status codes

4. **Rate limits**
   - Requests per minute/hour
   - Headers to check limits

5. **Pagination details**
   - Default page size
   - Maximum page size
   - How to navigate

6. **Field descriptions**
   - Type (string, number, boolean, array, object)
   - Required vs optional
   - Validation rules
   - Example values

---

## ✅ Pre-Launch Checklist

### URL & Structure
- [ ] URLs use **nouns** (plural, spinal-case)
- [ ] **Versioning** in URL path (`/v1/`, `/v2/`)
- [ ] **Hierarchy** reflects relationships (max 2 levels)
- [ ] **Consistent naming** (chose snake_case OR camelCase)
- [ ] No trailing slashes

### HTTP Standards
- [ ] **HTTP methods** follow CRUD semantics correctly
- [ ] **Status codes** used appropriately
- [ ] **Headers** follow HTTP specifications
- [ ] **Error responses** use standard format
- [ ] Content negotiation via **Accept headers**

### Security
- [ ] **HTTPS only** (no HTTP endpoints)
- [ ] **OAuth2** implemented correctly
- [ ] **Rate limiting** configured
- [ ] **CORS** configured for browser clients
- [ ] Security headers included

### Performance & Scalability
- [ ] **Pagination mandatory** for collections
- [ ] **Default pagination** configured (e.g., 25 items)
- [ ] **Field selection** supported for large resources
- [ ] **Caching headers** (ETag, Cache-Control)
- [ ] **Compression** enabled (gzip)

### Developer Experience
- [ ] **Documentation** complete with examples
-