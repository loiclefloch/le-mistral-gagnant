# Pragmatic RESTful API Design Guidelines

**Version**: 2.0 (Pragmatic Edition)  
**Date**: January 13, 2025  
**Based On**: OCTO Technology REST API Guidelines + Real-world Experience  
**Philosophy**: Balance REST principles with practical development needs

---

## 📋 What's New in This Version

### Key Differences from Pure REST Guidelines

1. **✅ Pragmatic over Pure**: Focus on what matters most for your project size and stage
2. **✅ Scoring System**: Objective methodology to measure API quality
3. **✅ Context-Aware Recommendations**: Different rules for startups vs enterprises
4. **✅ AI-Aware**: Specific guidance for APIs consumed by AI agents
5. **✅ Optional HATEOAS**: Hypermedia only when it adds value

### When to Use This Guide

- ✅ Building APIs for modern applications (web, mobile, AI)
- ✅ Want practical guidance, not theoretical purity
- ✅ Need to balance quality with delivery speed
- ✅ Working in startup or small team environments
- ✅ Evolving API over time without breaking clients

---

## 🎯 Core Philosophy

### 1. Pragmatism First

**Golden Rule**: Choose the right REST principle for your context.

| Project Stage           | REST Compliance Target | Focus Areas                               |
|-------------------------|------------------------|-------------------------------------------|
| **MVP/Prototype**       | 50-60%                 | Security, Error Handling, Basic Structure |
| **Startup/Small Team**  | 60-70%                 | + Consistency, Documentation              |
| **Growing Product**     | 70-80%                 | + Versioning, Pagination, Standards       |
| **Enterprise/Platform** | 80-90%                 | + HATEOAS, Full Compliance                |

### 2. Design Principles (Priority Order)

1. **🔒 Security First** (Non-negotiable)
   - HTTPS everywhere
   - Proper authentication
   - Input validation

2. **😊 Developer Experience** (Critical)
   - Intuitive URLs
   - Clear error messages
   - Good documentation

3. **🚀 Performance** (Important)
   - Pagination where needed
   - Efficient queries
   - Reasonable response times

4. **📐 Consistency** (Important)
   - Pick conventions and stick to them
   - Predictable patterns

5. **🎨 REST Purity** (Nice to have)
   - Follow when it improves usability
   - Skip when it adds complexity

---

## 📊 API Quality Scoring System

### How to Score Your API

Each category is scored 0-10, then weighted by importance:

```
Final Score = Σ (Category Score × Weight)
```

### Scoring Categories & Weights

| Category                | Weight | Mandatory?     | Context             |
|-------------------------|--------|----------------|---------------------|
| **Security**            | 15%    | ✅ Always       | Non-negotiable      |
| **Error Handling**      | 10%    | ✅ Always       | Critical for DX     |
| **API Versioning**      | 15%    | ⚠️ Conditional | See below           |
| **URL Structure**       | 10%    | ✅ Always       | First impression    |
| **HTTP Methods**        | 10%    | ✅ Always       | Core REST           |
| **Status Codes**        | 8%     | ✅ Always       | Clear communication |
| **Pagination**          | 10%    | ⚠️ Conditional | For collections     |
| **HATEOAS**             | 5%     | ❌ Optional     | AI/discovery only   |
| **Documentation**       | 5%     | ✅ Always       | Can start simple    |
| **Query Parameters**    | 5%     | ⚠️ Conditional | As needed           |
| **Content Negotiation** | 5%     | ❌ Optional     | Advanced            |
| **Infrastructure**      | 2%     | ✅ Always       | Basic setup         |

### Scoring Guide for Each Category

#### 0-2: Critical Issues
- Fundamental problems
- Security risks
- Breaks basic functionality

#### 3-5: Needs Improvement
- Functional but not ideal
- Missing important features
- Inconsistent implementation

#### 6-7: Good Enough
- Meets minimum standards
- Usable and functional
- Some optimization possible

#### 8-9: Excellent
- Follows best practices
- Well-implemented
- Minor improvements possible

#### 10: Perfect
- Exemplary implementation
- Nothing to improve
- Reference quality

### Target Scores by Project Type

| Project Type       | Minimum Score | Target Score | Excellence |
|--------------------|---------------|--------------|------------|
| **MVP/Prototype**  | 50/100        | 60/100       | 70/100     |
| **Startup API**    | 60/100        | 70/100       | 80/100     |
| **Production API** | 70/100        | 80/100       | 90/100     |
| **Platform API**   | 80/100        | 90/100       | 95/100     |

---

## 🔄 API Versioning: Context Matters

### ⚠️ When Versioning is OPTIONAL

**Small Projects / Startups / MVPs**:
- ✅ Team can coordinate breaking changes
- ✅ Few external clients (< 10)
- ✅ All clients can be updated quickly
- ✅ API is still evolving rapidly

**Instead of versioning**:
- Use feature flags
- Maintain backward compatibility
- Communicate changes clearly
- Plan migration windows

### ✅ When Versioning is MANDATORY

**Growing Projects / Multiple Clients**:
- ⚠️ Mobile apps in production (can't force updates)
- ⚠️ External API consumers (> 10)
- ⚠️ Public API or API-as-a-product
- ⚠️ Cannot coordinate client updates

### Versioning Strategies by Project Size

#### Strategy 1: No Versioning (Very Small)
```
GET /orders
GET /users

# Rules:
- Never break backward compatibility
- Add new fields, don't remove
- Deprecate gracefully
```

**Pros**: Simple, fast iteration  
**Cons**: Limits evolution, tech debt builds up

#### Strategy 2: Query Parameter (Medium)
```
GET /orders?v=1
GET /orders?v=2

# Default: latest version
GET /orders  # → v2
```

**Pros**: Easy to implement, flexible  
**Cons**: Not RESTful, can be forgotten

#### Strategy 3: URL Path (Large/Enterprise)
```
GET /v1/orders
GET /v2/orders
```

**Pros**: Most RESTful, explicit, cacheable  
**Cons**: More upfront work

### Pragmatic Recommendation

| Team Size  | Client Count | Mobile App? | Recommendation          |
|------------|--------------|-------------|-------------------------|
| 1-3 devs   | < 5 clients  | No          | No versioning           |
| 3-10 devs  | 5-20 clients | Maybe       | Query param OR URL path     |
| 10+ devs   | 20+ clients  | Yes         | Query param OR URL path |
| Enterprise | Public API   | Yes         | URL path (mandatory)    |

---

## 🔗 HATEOAS: When It Actually Matters

### ❌ HATEOAS is OPTIONAL for Most APIs

**Skip HATEOAS when**:
- ✅ Clients are human-developed applications
- ✅ You have good documentation
- ✅ Clients hardcode URLs anyway
- ✅ Small team, limited resources

**Why developers don't use HATEOAS**:
- They read documentation instead
- They copy/paste examples
- They hardcode known URLs
- Link parsing adds complexity

### ✅ HATEOAS is RECOMMENDED for AI-Consumed APIs

**Use HATEOAS when**:
- ⚠️ API consumed by AI agents (LLMs, automation)
- ⚠️ Complex workflows need discovery
- ⚠️ State machines are critical
- ⚠️ Building a platform API

**Why AI needs HATEOAS**:
- AI can follow links dynamically
- Discovers available actions
- Adapts to API changes automatically
- No hardcoded URLs in prompts

### Pragmatic HATEOAS Implementation

#### Minimal (Recommended for Everyone)
```http
GET /orders/1234

Link: <https://api.example.com/v1/orders/1234>; rel="self"
```

Just provide `self` link for reference.

#### Standard (For Growing APIs)
```http
GET /orders/1234

Link: <...orders/1234>; rel="self",
      <...orders/1234>; rel="update"; method="PUT",
      <...orders/1234>; rel="delete"; method="DELETE"
```

Common actions on the resource.

#### Full (For AI-Consumed APIs)
```http
GET /orders/1234

Link: <...orders/1234>; rel="self",
      <...orders/1234>; rel="cancel"; method="POST",
      <...orders/1234/payment>; rel="payment"; method="POST",
      <...orders/1234/items>; rel="items",
      <...coaches/42>; rel="coach"
```

Complete state machine with related resources.

### Scoring HATEOAS

| Implementation     | Score | When Appropriate               |
|--------------------|-------|--------------------------------|
| None               | 0/10  | Small APIs, human clients only |
| Self link only     | 3/10  | Minimum viable                 |
| Common actions     | 7/10  | Production APIs                |
| Full state machine | 10/10 | AI-consumed, platforms         |

**Impact on Overall Score**: 5% weight (low)  
**Recommendation**: Start with self link, add more if needed

---

## 🏗️ URL Structure (Simplified)

### ✅ Essential Rules (Always Follow)

1. **Use nouns, not verbs**
   ```
   ✅ GET /orders
   ❌ GET /getOrders
   ```

2. **Use plural for collections**
   ```
   ✅ GET /orders/123
   ❌ GET /order/123
   ```

3. **Use lowercase with hyphens**
   ```
   ✅ /user-preferences
   ❌ /userPreferences
   ❌ /user_preferences
   ```

The most important is to check consistency. It can be plural camel case paths if they are all following this convention. 

### ⚠️ Pragmatic Exceptions

**Operations are OK when they're truly operations**:
```
✅ POST /orders/123/cancel
✅ POST /emails/456/send
✅ POST /reports/generate

Instead of forcing:
❌ POST /order-cancellations
❌ POST /email-sends
❌ POST /report-generation-jobs
```

**Rule**: If modeling as a resource feels awkward, use a verb.

### Depth Limit

```
✅ Good: /orders/123/items
⚠️ OK: /orders/123/items/456
❌ Too deep: /orders/123/items/456/reviews/789
```

**Maximum**: 2-3 levels. Beyond that, use top-level resources.

---

## 🔧 HTTP Methods (Pragmatic Usage)

### Standard CRUD (Always Use)

| Method     | Use              | Example              |
|------------|------------------|----------------------|
| **GET**    | Read             | `GET /orders/123`    |
| **POST**   | Create           | `POST /orders`       |
| **PUT**    | Replace fully    | `PUT /orders/123`    |
| **PATCH**  | Update partially | `PATCH /orders/123`  |
| **DELETE** | Delete           | `DELETE /orders/123` |

### Pragmatic Simplifications

**You CAN use POST for operations**:
```http
POST /orders/123/refund
POST /users/456/reset-password
POST /subscriptions/789/cancel
```

Better than trying to force everything into resources.

### Status Codes (Essential Ones)

**Must Have**:
- 200 OK, 201 Created, 204 No Content
- 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found
- 500 Internal Server Error

**Nice to Have**:
- 202 Accepted (async), 206 Partial Content (pagination)
- 409 Conflict, 422 Unprocessable Entity
- 429 Too Many Requests (rate limiting)

**Can Skip**:
- 300-level redirects (unless needed)
- Obscure 4xx/5xx codes

---

## 📄 Pagination: When It's Mandatory

### ✅ Always Paginate

- Collections a lot of items
- Collections that grow over time
- User-generated content
- External API endpoints

### ⚠️ Pagination Optional

- Small, fixed collections
- Internal microservice APIs
- Lookup tables / reference data
- Prototypes / MVPs

### Simple Pagination (Recommended)

```http
GET /orders?page=1&limit=25

Response:
{
  "items": [...],
  "page": 1,
  "limit": 25,
  "total": 1234,
  "total_pages": 50
}
```
### Recommendation by Project Type

| Project    | Use                                     | Why                |
|------------|-----------------------------------------|--------------------|
| MVP        | Simple page/limit                       | Fast to implement  |
| Startup    | Simple page/limit                       | Good enough        |
| Growing    | Simple page/limit                       | Improve compliance |
| Enterprise | Simple page/limit, can add Link headers | Complete standard  |

---

## 🔐 Security (Non-Negotiable)

### ✅ Mandatory (Everyone)

1. **HTTPS Only**
   - No exceptions
   - Redirect HTTP → HTTPS
   - Valid certificates

2. **Authentication**
   - JWT tokens (Bearer)
   - OAuth 2.0 (when possible)
   - API keys for services

3. **Input Validation**
   - Validate all inputs
   - Sanitize data
   - Use schema validation (Zod, Joi)

4. **Error Messages**
   - Don't expose internals
   - Log server-side
   - Return generic errors to client

### ⚠️ Recommended (Production)

5. **Rate Limiting**
   - Protect against abuse
   - Per-user limits
   - Graceful degradation

6. **CORS**
   - Configure properly
   - Whitelist origins
   - Handle preflight

### ❌ Advanced (Enterprise)

7. **Full OAuth 2.0**
   - Authorization server
   - Multiple flows
   - Refresh tokens

8. **OpenID Connect**
   - User authentication
   - ID tokens
   - User info endpoint

**Pragmatic Take**: Start with JWT, add OAuth later if needed.

---

## 📚 Documentation Strategy

### Minimum Viable Documentation

**Must Have** (Day 1):
1. Authentication method
2. Base URL
3. List of endpoints
4. Example requests

**Should Have** (Week 1):
5. Error codes
6. Response examples
7. Rate limits

**Nice to Have** (Month 1):
8. OpenAPI/Swagger
9. Interactive docs
10. Code examples in multiple languages

### Tools by Project Size

| Team Size  | Tool                     | Why          |
|------------|--------------------------|--------------|
| 1-3        | README + cURL            | Simple, fast |
| 3-10       | Postman/Bruno collection | Shareable    |
| 10+        | OpenAPI/Swagger          | Standard     |
| Enterprise | Developer portal         | Complete     |

---

## 🎯 Improvement Areas in Original Guidelines

### What We Added

1. **✅ Scoring Methodology**
   - Objective measurement
   - Track progress over time
   - Prioritize improvements

2. **✅ Context-Aware Guidance**
   - Different rules for different sizes
   - Pragmatic tradeoffs
   - Clear when to skip rules

3. **✅ AI-Specific Recommendations**
   - HATEOAS for AI agents
   - Discovery patterns
   - State machine guidance

4. **✅ Implementation Priority**
   - What to do first
   - What can wait
   - What to skip

### What Could Be Better in Pure REST

1. **Too Dogmatic**
   - Not all rules fit all contexts
   - Some add complexity without value
   - Need pragmatic escape hatches

2. **Missing Prioritization**
   - All rules seem equally important
   - No guidance on what to do first
   - No tradeoff analysis

3. **Assumes Large Teams**
   - Guidelines for enterprise-scale
   - Overkill for startups/small teams
   - Need scaled-down versions

4. **Ignores AI Consumption**
   - Written for human developers
   - Doesn't address AI/automation use cases
   - Missing discovery patterns

---

## ✅ Pragmatic API Checklist

### Phase 1: MVP (Score Target: 50-60%)

- [ ] HTTPS enabled
- [ ] Basic authentication (JWT)
- [ ] Resource-based URLs (nouns)
- [ ] HTTP methods correct
- [ ] Standard error format
- [ ] Basic documentation (README)

### Phase 2: Production Ready (Score Target: 70-80%)

- [ ] All Phase 1 items
- [ ] Input validation everywhere
- [ ] Consistent error handling
- [ ] Pagination on large collections
- [ ] Rate limiting basics
- [ ] OpenAPI/Swagger documentation

### Phase 3: Scale (Score Target: 80-90%)

- [ ] All Phase 2 items
- [ ] API versioning (if needed)
- [ ] HATEOAS (if AI-consumed)
- [ ] Advanced error codes (409, 422, 429)
- [ ] Full OAuth 2.0 (if needed)
- [ ] Developer portal

### Phase 4: Excellence (Score Target: 90%+)

- [ ] All Phase 3 items
- [ ] Complete HATEOAS
- [ ] Content negotiation
- [ ] Multiple formats support
- [ ] Comprehensive documentation
- [ ] SDKs in multiple languages

---

## 📊 Quick Score Your API

### Self-Assessment Questions

**Security (15%)**:
- [ ] HTTPS only? (5 points)
- [ ] Authentication implemented? (5 points)
- [ ] Input validation? (3 points)
- [ ] Rate limiting? (2 points)

**Error Handling (10%)**:
- [ ] Standardized format? (5 points)
- [ ] Clear messages? (3 points)
- [ ] Proper status codes? (2 points)

**URL Structure (10%)**:
- [ ] Resource-based (nouns)? (4 points)
- [ ] Consistent naming? (3 points)
- [ ] Reasonable depth? (3 points)

**HTTP Methods (10%)**:
- [ ] Correct method usage? (6 points)
- [ ] Idempotency respected? (4 points)

**Documentation (5%)**:
- [ ] Basic docs exist? (3 points)
- [ ] Examples provided? (2 points)

**Versioning (15%)** - Context-dependent:
- [ ] Strategy defined? (5 points)
- [ ] Implemented if needed? (10 points)
- [ ] Not needed yet? (15 points - full credit)

**Pagination (10%)**:
- [ ] Implemented on collections? (7 points)
- [ ] Headers provided? (3 points)

**HATEOAS (5%)** - Optional unless AI:
- [ ] Not needed (human clients)? (5 points - full credit)
- [ ] Self links at minimum? (3 points)
- [ ] Full implementation (AI)? (5 points)

---

## 🎓 Key Takeaways

### The 5 Non-Negotiables

1. **Security**: HTTPS + Auth + Validation
2. **Consistency**: Pick patterns and stick to them
3. **Errors**: Clear, standardized, helpful
4. **Documentation**: At least the basics
5. **Pragmatism**: Use what fits your context

### The Pragmatic Mindset

- ✅ Start simple, evolve as needed
- ✅ Follow standards when they help
- ✅ Break rules when they don't
- ✅ Measure and improve over time
- ✅ Context matters more than purity

### When to Deviate from Pure REST

**It's OK to**:
- Use operations instead of forced resources
- Skip HATEOAS for human-only APIs
- Defer versioning for small projects
- Use simple pagination over perfect REST
- Mix conventions if documented clearly

**It's NOT OK to**:
- Skip security
- Have inconsistent patterns
- Ignore errors
- Have no documentation
- Break changes without notice

---

## 📚 References

### Standards
- RFC 7231: HTTP/1.1 Semantics
- RFC 5988: Web Linking (HATEOAS)
- RFC 6749: OAuth 2.0
- OpenAPI Specification 3.0

### Inspiration
- OCTO Technology REST API Guidelines
- Microsoft REST API Guidelines
- Google API Design Guide
- Stripe API Design
- GitHub API v3

### This Document
- Based on original OCTO guidelines
- Enhanced with scoring methodology
- Adjusted for pragmatic use
- Validated against real projects

---

**Version**: 2.0 Pragmatic Edition  
**Date**: January 13, 2025  
**License**: Free to use and adapt  
**Feedback**: Welcome improvements and suggestions

---

*These guidelines represent a balance between REST principles and practical software development. Use them as a framework, not a prison. The best API is one that works for your users, your team, and your constraints.*
