# Pragmatic RESTful API Design Guidelines

**Version**: 3.0
**Date**: November 13, 2025  
**Based On**: OCTO Technology + Pragmatic Experience  
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

---

## 📊 API Quality Scoring System

### Scoring Categories & Weights

| Category            | Weight | Mandatory?     | Context                    |
|---------------------|--------|----------------|----------------------------|
| **Security**        | 20%    | ✅ Always       | Zero Trust, OAuth 2.0      |
| **Error Handling**  | 10%    | ✅ Always       | RFC9457, clear messages    |
| **Versioning**      | 12%    | ⚠️ Conditional | Mobile apps, ext. clients  |
| **URL Structure**   | 8%     | ✅ Always       | Resource-oriented design   |
| **HTTP Methods**    | 8%     | ✅ Always       | Idempotency matters        |
| **Status Codes**    | 7%     | ✅ Always       | Standard + 422, 429        |
| **Pagination**      | 8%     | ⚠️ Conditional | Large collections only     |
| **HATEOAS**         | 5%     | ❌ Optional     | AI/discovery scenarios     |
| **Documentation**   | 10%    | ✅ Always       | OpenAPI + examples         |
| **Observability**   | 5%     | ✅ Always       | Logs, metrics, traces      |
| **Architecture**    | 5%     | ⚠️ Conditional | Proper API types           |
| **Performance**     | 2%     | ✅ Always       | Response times             |

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
- ✅ Enable HSTS
- ❌ Never self-signed certs in production

### OAuth 2.0 Flow Selection

**Client Credentials**: Server-to-server, machine-to-machine  
**Authorization Code + PKCE**: Web/mobile apps with users (mandatory in OAuth 2.1)  
**Device Grant**: IoT devices, smart TVs

### JWT Best Practices

**Validation checklist**:
- ✅ Signature (using JWKS)
- ✅ Expiration (exp)
- ✅ Issuer (iss)
- ✅ Audience (aud)
- ✅ Not Before (nbf)

**Scopes vs Permissions**:
- **Scopes**: What application CAN do
- **Permissions**: What user IS ALLOWED to do
- Effective = User permissions ∩ Client scopes

### OWASP API Top 10

1. Broken Object Level Authorization
2. Broken Authentication
3. Broken Object Property Authorization
4. Unrestricted Resource Consumption
5. Broken Function Level Authorization
6. Unrestricted Business Flow Access
7. Server Side Request Forgery
8. Security Misconfiguration
9. Improper Inventory Management
10. Unsafe API Consumption

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

### Pragmatic Exceptions

Actions that don't fit CRUD:
```
✅ POST /orders/123/cancel
✅ POST /users/456/reset-password
```

### Resource Granularity

**Start coarse-grained**, split when needed:
```
GET /users/123  # All user data
GET /users/123?fields=id,name,email  # Selective fields
```

---

## 🔧 HTTP Methods & Status Codes

### HTTP Methods

| Method   | Use           | Idempotent | Example              |
|----------|---------------|------------|----------------------|
| GET      | Read          | ✅          | `GET /products/123`  |
| POST     | Create/Action | ❌          | `POST /orders`       |
| PUT      | Replace       | ✅          | `PUT /products/123`  |
| PATCH    | Update        | ❌          | `PATCH /users/456`   |
| DELETE   | Delete        | ✅          | `DELETE /orders/789` |

### Key Status Codes

**Success**: 200, 201, 202, 204, 206  
**Client Error**: 400, 401, 403, 404, 422, 429  
**Server Error**: 500, 502, 503, 504

### Idempotency

**For POST idempotency**, use Idempotency-Key header:
```http
POST /payments
Idempotency-Key: unique-id-12345
```

---

## 📝 Error Handling (RFC 9457)

```json
{
  "type": "https://api.example.com/errors/insufficient-funds",
  "title": "Insufficient Funds",
  "status": 422,
  "detail": "Account balance is 30 EUR, but transaction requires 100 EUR"
}
```

### Multiple Errors

```json
{
  "type": "VALIDATION_FAILED",
  "title": "Validation Failed",
  "status": 400,
  "errors": [
    {"field": "email", "message": "Invalid format"},
    {"field": "age", "message": "Must be 18 or older"}
  ]
}
```

---

## 📄 Pagination & Filtering

### Page-Based Pagination

```http
GET /products?page=2&limit=25

{
  "items": [...],
  "pagination": {
    "page": 2,
    "limit": 25,
    "total": 1234,
    "total_pages": 50
  }
}
```

### Cursor-Based Pagination

```http
GET /messages?cursor=eyJ...&limit=50

{
  "items": [...],
  "pagination": {
    "next_cursor": "eyJ...",
    "has_next": true
  }
}
```

### Filtering & Sorting

```http
GET /products?category=electronics&price_max=100&sort=price:asc
```

---

## 🔄 API Versioning

### When Mandatory

- Mobile apps in production
- External API consumers (>10)
- Public API
- Breaking changes needed

### Strategies

**URL Path** (recommended): `GET /v1/products`  
**Query Param**: `GET /products?v=1`  
**Header**: `Accept: application/vnd.api+json; version=1`

### Version Lifecycle

1. Announce new version 6 months ahead
2. Maintain max 2 versions simultaneously
3. Monitor usage before deprecation
4. Provide migration guide

---

## 🏛️ API Architecture Patterns

### API Types

1. **Core APIs**: Expose business capabilities, master of data
2. **Facade APIs**: Wrapper over legacy systems (transitional)
3. **Process APIs**: Orchestrate multiple core APIs
4. **Experience APIs (BFF)**: Tailored for specific client needs

### When to Use Each

**Core**: New services, domain boundaries  
**Facade**: Legacy integration (temporary)  
**Process**: Complex workflows spanning domains  
**BFF**: Mobile apps, specific frontend needs

### Microservices vs Monolith

**Start with Modular Monolith**, extract to microservices only when:
- Team size > 10 developers
- Clear bounded contexts exist
- Independent scaling needed
- DevOps maturity achieved

---

## 🔄 Sync vs Async Integration

### Use REST (Sync) When

- Real-time response needed (< 5 seconds)
- Simple request-response pattern
- Frontend to backend communication
- External partner integrations

### Use Messaging (Async) When

- Long-running processes (> 10 seconds)
- High volume data transfer
- Event-driven architecture
- Decoupled microservices

### REST vs Other Protocols

**REST**: Default choice, universal compatibility  
**GraphQL**: BFF scenarios, flexible queries  
**gRPC**: Internal microservices, performance-critical  
**SOAP**: Legacy only (don't start new projects)

---

## ⚡ Resilience Patterns

### Timeouts

Set timeouts for all external calls:
- API calls: 2-3 seconds
- Database: 1 second
- Third-party APIs: 5-10 seconds

### Retries with Exponential Backoff

100ms, 200ms, 400ms

### Circuit Breaker

Prevents cascade failures:
- **Closed**: Normal operation
- **Open**: Fast fail without calling service
- **Half-Open**: Test if service recovered

### Bulkhead Pattern

Isolate resources to prevent total failure:
- Separate thread pools per service
- Dedicated connection pools
- Container-level isolation

---

## 📊 Observability & Monitoring

### Essential Metrics

**Technical**:
- Uptime (target: > 99.9%)
- Response time (p95 < 200ms)
- Error rate (< 1%)
- Requests per second

**Product**:
- TTFAC (< 10 minutes)
- API consumers count
- Support tickets created

### Logging Strategy

**Structured logs** with correlation IDs:
```json
{
  "timestamp": "2025-02-13T10:00:00Z",
  "level": "INFO",
  "correlation_id": "abc-123",
  "method": "GET",
  "path": "/orders/123",
  "status": 200,
  "duration_ms": 45
}
```

### Health Endpoints

```
GET /health  → {"status": "healthy"}
GET /version → {"version": "1.2.3", "commit": "abc123"}
```

### Distributed Tracing

Use correlation IDs across services:
```http
X-Correlation-ID: unique-request-id
X-Request-ID: unique-request-id
```

---

## 📚 Documentation Strategy

### Spec-First vs Code-First

**Spec-First** (recommended for):
- Public APIs
- External partners
- Large teams
- Stable contracts

**Code-First** (acceptable for):
- Internal APIs
- Rapid prototyping
- Small teams

### OpenAPI Documentation

**Minimum viable**:
- Authentication method
- Base URL
- All endpoints with examples
- Error codes

**Production-ready**:
- Interactive documentation (Swagger UI)
- Code examples (multiple languages)
- Postman/Bruno collections
- Migration guides

### Developer Portal

For public/partner APIs:
- API catalog
- Interactive sandbox
- Self-service enrollment
- API key management
- Usage dashboards
- Status page

---

## 🔗 HATEOAS: When It Matters

### Skip for Most APIs

Human developers prefer documentation over link discovery.

### Use for AI Agents

AI can follow links dynamically:

```json
{
  "id": "123",
  "status": "pending",
  "_links": {
    "self": "/orders/123",
    "cancel": {"href": "/orders/123/cancel", "method": "POST"},
    "payment": {"href": "/orders/123/payment", "method": "POST"}
  }
}
```

---

## 🤖 APIs in the AI Era

### Design for LLMs

1. **Clear descriptions** in OpenAPI spec
2. **Include examples** for each endpoint
3. **Structured errors** for auto-correction
4. **Discovery endpoints** for dynamic exploration

### MCP (Model Context Protocol)

Standard for LLM-to-tool communication:
- Unified interface for AI agents
- Built on JSON-RPC 2.0
- Supports OAuth 2.0 security
- Similar architecture to REST APIs

### AI-Specific Considerations

- **Reduce token cost**: Minimize response size
- **Latency tolerance**: AI calls can be slower
- **Non-deterministic**: Plan for retry logic
- **Documentation as code**: AI reads specs at runtime

### Dynamic Context Loading

```http
GET /describe
→ Lists all available endpoints

GET /describe?endpoint=/orders
→ Details for specific endpoint
```

---

## ✅ Implementation Checklist

### Phase 1: MVP (50-60%)

- [ ] HTTPS only
- [ ] Basic authentication
- [ ] Resource-based URLs
- [ ] Standard error format
- [ ] README documentation

### Phase 2: Production (70-80%)

- [ ] OAuth 2.0 / JWT
- [ ] Input validation
- [ ] Pagination
- [ ] Rate limiting
- [ ] OpenAPI spec

### Phase 3: Scale (80-90%)

- [ ] API versioning
- [ ] Circuit breakers
- [ ] Distributed tracing
- [ ] Developer portal
- [ ] SLA monitoring

### Phase 4: Excellence (90%+)

- [ ] HATEOAS (for AI)
- [ ] Multiple SDKs
- [ ] Advanced security (mTLS)
- [ ] Full observability stack
- [ ] API governance

---

## 🎓 Key Takeaways

### The 5 Non-Negotiables

1. **Security**: Zero Trust + OAuth 2.0
2. **Consistency**: One convention, applied everywhere
3. **Errors**: RFC 9457 format with actionable messages
4. **Documentation**: OpenAPI + examples
5. **Pragmatism**: Context over dogma

### Design Philosophy

- **API as Product**: Developers are your users
- **Abstract Design**: Business concepts, not database tables
- **Zero Trust**: Verify everything, trust nothing
- **Fail Fast**: Timeouts, circuit breakers, clear errors

### When to Break Rules

✅ **OK to deviate**:
- Action endpoints for non-CRUD operations
- Skip HATEOAS for human-only APIs
- Delay versioning for internal APIs
- Simple pagination over cursor-based

❌ **Never compromise**:
- Security (HTTPS, auth, validation)
- Consistency (pick patterns, stick to them)
- Error clarity (proper status codes, messages)
- Documentation (at least OpenAPI basics)

---

## 📚 References & Resources

### Standards

- [RFC 9110](https://www.rfc-editor.org/rfc/rfc9110.html): HTTP Semantics
- [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457.html): Problem Details
- [RFC 6749](https://www.rfc-editor.org/rfc/rfc6749.html): OAuth 2.0
- [OpenAPI 3.1](https://spec.openapis.org/oas/v3.1.0): API Specification

### Inspirational APIs

- **Stripe**: Excellent documentation, DX-first
- **Twilio**: Clear examples, great onboarding
- **GitHub**: Consistent, well-versioned
- **Discord**: Good balance of simplicity/power

### Further Reading

- [OCTO Technology](https://blog.octo.com/)
- [Microsoft REST API Guidelines](https://github.com/microsoft/api-guidelines)
- [Zalando RESTful API Guidelines](https://opensource.zalando.com/restful-api-guidelines/)
- [API Security Top 10](https://owasp.org/www-project-api-security/)

---

**License**: Free to use and adapt  
**Feedback**: Contributions welcome  
**Philosophy**: *"Perfect is the enemy of good. Ship secure, consistent, documented APIs. Iterate based on real usage."*

---

*These guidelines balance REST principles with practical software development. Use them as a framework for decision-making, not as rigid rules. The best API is one that is secure, works reliably for your users, and can evolve with your needs.*
