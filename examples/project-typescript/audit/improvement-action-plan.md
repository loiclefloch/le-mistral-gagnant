# Plan d'Action - Correction des Violations API

**Projet**: project-typescript  
**Date**: 18 décembre 2025  
**Priorité**: CRITIQUE  

---

## 📌 Résumé

Ce document fournit un plan détaillé pour corriger les 10 violations majeures de l'API identifiées dans l'audit. Chaque correction est accompagnée de code exemple et d'étapes d'implémentation.

---

## 🔴 ACTION 1: Ajouter Versioning API (URGENT)

**Effort**: ⏱️ 30 minutes  
**Impact**: CRITIQUE  
**Dépendances**: Aucune

### Étapes

1. **Modifier server.ts** - Ajouter `/v1/` à tous les endpoints
2. **Ajouter support multi-versions** - Structure pour v2 future
3. **Tester les endpoints** - Vérifier compatibilité tests

### Implémentation

**Avant**:
```typescript
app.post('/addUser', ...);
app.get('/getUser/:id', ...);
app.get('/allUsers', ...);
app.delete('/removeUser/:id', ...);
```

**Après**:
```typescript
// Créer un routeur pour v1
const v1Router = express.Router();

v1Router.post('/users', ...);
v1Router.get('/users/:id', ...);
v1Router.get('/users', ...);
v1Router.delete('/users/:id', ...);

app.use('/v1', v1Router);

// Permet l'ajout futur de /v2
```

### Code d'Implémentation

```typescript
// Dans server.ts
import express from 'express';
import { UserService } from '../application/UserService';

export const app = express();
app.use(express.json());
const userService = new UserService();

// Router pour la version 1
const v1Router = express.Router();

// Endpoints v1
v1Router.post('/users', (req, res) => {
  const user = userService.createUser(req.body);
  res.status(201).json(user);
});

v1Router.get('/users/:id', (req, res) => {
  const user = userService.getUser(req.params.id);
  res.json(user);
});

v1Router.get('/users', (req, res) => {
  const users = userService.listUsers();
  res.json(users);
});

v1Router.delete('/users/:id', (req, res) => {
  const idx = userService['users'].findIndex((u: any) => u.id === req.params.id);
  if (idx !== -1) userService['users'].splice(idx, 1);
  res.json({ removed: idx !== -1 });
});

// Enregistrer le routeur avec la version
app.use('/v1', v1Router);
```

### Validation

```bash
curl -X POST http://localhost:3000/v1/users \
  -H "Content-Type: application/json" \
  -d '{"name":"John","email":"john@example.com"}'
```

### Mise à Jour Tests

**test/user.test.ts**:
```typescript
// Remplacer les chemins
// DE: .post('/addUser')
// À: .post('/v1/users')
```

---

## 🔐 ACTION 2: Implémenter Authentification JWT (CRITIQUE)

**Effort**: ⏱️ 2 heures  
**Impact**: CRITIQUE  
**Dépendances**: jsonwebtoken

### Installation

```bash
npm install jsonwebtoken
npm install --save-dev @types/jsonwebtoken
```

### Implémentation

Créer `src/infrastructure/auth.ts`:
```typescript
import jwt from 'jsonwebtoken';

const ACCESS_TOKEN_SECRET = process.env.ACCESS_TOKEN_SECRET || 'dev-secret-key';
const TOKEN_EXPIRY = '15m';

export interface AuthToken {
  sub: string;
  iat: number;
  exp: number;
}

export const generateToken = (userId: string): string => {
  return jwt.sign(
    { sub: userId },
    ACCESS_TOKEN_SECRET,
    { expiresIn: TOKEN_EXPIRY }
  );
};

export const verifyToken = (token: string): AuthToken => {
  try {
    return jwt.verify(token, ACCESS_TOKEN_SECRET) as AuthToken;
  } catch (error) {
    throw new Error('Token invalide ou expiré');
  }
};

export const authMiddleware = (req: any, res: any, next: any) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader?.split(' ')[1];

  if (!token) {
    return res.status(401).json({
      type: 'AUTHENTICATION_REQUIRED',
      status: 401,
      title: 'Authentification requise',
      detail: 'Token d\'authentification manquant',
      instance: req.path
    });
  }

  try {
    const decoded = verifyToken(token);
    req.user = decoded;
    next();
  } catch (error) {
    return res.status(403).json({
      type: 'INVALID_TOKEN',
      status: 403,
      title: 'Token invalide',
      detail: (error as Error).message,
      instance: req.path
    });
  }
};
```

Modifier `server.ts`:
```typescript
import { authMiddleware } from './auth';

// Appliquer auth à tous les endpoints sauf login
v1Router.use(authMiddleware);

// Endpoint de login (avant authMiddleware)
app.post('/v1/auth/login', (req, res) => {
  // Démo simple: générer un token
  const token = generateToken('demo-user');
  res.json({ token });
});
```

### Test

```bash
# 1. Obtenir un token
TOKEN=$(curl -s -X POST http://localhost:3000/v1/auth/login | jq -r '.token')

# 2. Utiliser le token
curl -X GET http://localhost:3000/v1/users \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🔴 ACTION 3: Corriger Gestion d'Erreurs (CRITIQUE)

**Effort**: ⏱️ 1.5 heures  
**Impact**: CRITIQUE  
**Dépendances**: Aucune

### Implémentation

Créer `src/infrastructure/errors.ts`:
```typescript
export class ApiError extends Error {
  constructor(
    public type: string,
    public status: number,
    public title: string,
    public detail: string,
    public instance?: string
  ) {
    super(detail);
    this.name = 'ApiError';
  }
}

// Erreurs courantes
export class ValidationError extends ApiError {
  constructor(
    public errors: Array<{ field: string; message: string }>,
    instance?: string
  ) {
    super(
      'VALIDATION_FAILED',
      422,
      'Validation échouée',
      'Les données fournies ne respectent pas le schéma',
      instance
    );
  }
}

export class NotFoundError extends ApiError {
  constructor(resource: string, id: string, instance?: string) {
    super(
      'RESOURCE_NOT_FOUND',
      404,
      'Ressource non trouvée',
      `${resource} avec l'ID '${id}' n'existe pas`,
      instance
    );
  }
}

export class AuthenticationError extends ApiError {
  constructor(instance?: string) {
    super(
      'AUTHENTICATION_REQUIRED',
      401,
      'Authentification requise',
      'Veuillez fournir des identifiants valides',
      instance
    );
  }
}
```

Ajouter middleware d'erreur dans `server.ts`:
```typescript
app.use((err: any, req: any, res: any, next: any) => {
  if (err instanceof ApiError) {
    return res.status(err.status).json({
      type: err.type,
      status: err.status,
      title: err.title,
      detail: err.detail,
      instance: err.instance || req.path
    });
  }

  // Erreur non gérée
  console.error(err);
  res.status(500).json({
    type: 'INTERNAL_ERROR',
    status: 500,
    title: 'Erreur interne du serveur',
    detail: 'Une erreur inattendue s\'est produite',
    instance: req.path
  });
});

// 404 handler
app.use((req: any, res: any) => {
  res.status(404).json({
    type: 'ENDPOINT_NOT_FOUND',
    status: 404,
    title: 'Endpoint non trouvé',
    detail: `L'endpoint ${req.method} ${req.path} n'existe pas`,
    instance: req.path
  });
});
```

Utiliser dans les endpoints:
```typescript
v1Router.get('/users/:id', async (req, res, next) => {
  try {
    const user = userService.getUser(req.params.id);
    
    if (!user) {
      throw new NotFoundError('User', req.params.id, req.path);
    }
    
    res.status(200).json(user);
  } catch (error) {
    next(error);
  }
});
```

---

## ✏️ ACTION 4: Corriger Nommage des Endpoints (ÉLEVÉ)

**Effort**: ⏱️ 30 minutes  
**Impact**: ÉLEVÉ  
**Dépendances**: ACTION 1 (Versioning)

### Changements

| Ancien | Nouveau |
|--------|---------|
| `POST /addUser` | `POST /v1/users` |
| `GET /getUser/:id` | `GET /v1/users/:id` |
| `GET /allUsers` | `GET /v1/users` |
| `DELETE /removeUser/:id` | `DELETE /v1/users/:id` |

Voir ACTION 1 pour l'implémentation.

---

## 📊 ACTION 5: Ajouter Pagination (ÉLEVÉ)

**Effort**: ⏱️ 1 heure  
**Impact**: ÉLEVÉ  
**Dépendances**: Aucune

### Implémentation

Modifier `UserService.ts`:
```typescript
export class UserService {
  private users: User[] = [];

  // ...existing code...

  listUsers(offset: number = 0, limit: number = 25): User[] {
    return this.users.slice(offset, offset + limit);
  }

  countUsers(): number {
    return this.users.length;
  }
}
```

Modifier `server.ts`:
```typescript
v1Router.get('/users', (req, res, next) => {
  try {
    const page = Math.max(1, parseInt(req.query.page as string) || 1);
    const limit = Math.min(
      Math.max(1, parseInt(req.query.limit as string) || 25),
      100 // Max 100
    );

    const offset = (page - 1) * limit;
    const users = userService.listUsers(offset, limit);
    const total = userService.countUsers();
    const pages = Math.ceil(total / limit);

    res.status(200).json({
      data: users,
      pagination: {
        page,
        limit,
        total,
        pages,
        hasMore: offset + limit < total,
        nextPage: page < pages ? page + 1 : null,
        prevPage: page > 1 ? page - 1 : null
      }
    });
  } catch (error) {
    next(error);
  }
});
```

### Test

```bash
curl "http://localhost:3000/v1/users?page=1&limit=10" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🛡️ ACTION 6: Ajouter Rate Limiting (ÉLEVÉ)

**Effort**: ⏱️ 45 minutes  
**Impact**: ÉLEVÉ  
**Dépendances**: express-rate-limit

### Installation

```bash
npm install express-rate-limit
npm install --save-dev @types/express-rate-limit
```

### Implémentation

Créer `src/infrastructure/rateLimiter.ts`:
```typescript
import rateLimit from 'express-rate-limit';

export const createRateLimiter = (
  windowMs: number = 15 * 60 * 1000, // 15 minutes
  max: number = 100
) => {
  return rateLimit({
    windowMs,
    max,
    message: {
      type: 'RATE_LIMIT_EXCEEDED',
      status: 429,
      title: 'Limite de taux dépassée',
      detail: 'Trop de requêtes. Réessayez plus tard.'
    },
    standardHeaders: true,
    legacyHeaders: false
  });
};

export const apiLimiter = createRateLimiter();
export const authLimiter = createRateLimiter(15 * 60 * 1000, 5); // 5 par 15 min
```

Modifier `server.ts`:
```typescript
import { apiLimiter, authLimiter } from './rateLimiter';

// Rate limiting global
app.use('/v1/', apiLimiter);

// Rate limiting plus strict sur login
app.post('/v1/auth/login', authLimiter, (req, res) => {
  const token = generateToken('demo-user');
  res.json({ token });
});
```

---

## 🆔 ACTION 7: Utiliser UUID au lieu de Math.random() (ÉLEVÉ)

**Effort**: ⏱️ 15 minutes  
**Impact**: ÉLEVÉ  
**Dépendances**: uuid

### Installation

```bash
npm install uuid
npm install --save-dev @types/uuid
```

### Implémentation

Modifier `UserService.ts`:
```typescript
import { v4 as uuidv4 } from 'uuid';

export class UserService {
  private users: User[] = [];

  createUser(data: any): User {
    const user: User = {
      id: uuidv4(), // ✅ UUID au lieu de Math.random()
      name: data.name,
      email: data.email
    };
    this.users.push(user);
    return user;
  }

  // ...existing code...
}
```

---

## 📚 ACTION 8: Ajouter Validation d'Entrée (CRITIQUE)

**Effort**: ⏱️ 1.5 heures  
**Impact**: CRITIQUE  
**Dépendances**: joi ou zod

### Approche avec Zod (recommandée)

Installation:
```bash
npm install zod
```

Créer `src/infrastructure/validators.ts`:
```typescript
import { z } from 'zod';

export const UserSchema = z.object({
  name: z.string()
    .min(2, 'Le nom doit contenir au moins 2 caractères')
    .max(50, 'Le nom ne peut pas dépasser 50 caractères'),
  email: z.string()
    .email('Email invalide')
    .max(100, 'Email trop long')
});

export const validateUser = (data: any) => {
  return UserSchema.parse(data);
};

export const validateUserSafe = (data: any) => {
  return UserSchema.safeParse(data);
};
```

Utiliser dans `server.ts`:
```typescript
import { validateUserSafe, ValidationError } from '../infrastructure/validators';

v1Router.post('/users', (req, res, next) => {
  try {
    const validation = validateUserSafe(req.body);
    
    if (!validation.success) {
      throw new ValidationError(
        validation.error.errors.map(e => ({
          field: (e.path[0] as string) || 'unknown',
          message: e.message
        })),
        req.path
      );
    }

    const user = userService.createUser(validation.data);
    res.status(201).json(user);
  } catch (error) {
    next(error);
  }
});
```

---

## 📖 ACTION 9: Ajouter Documentation OpenAPI (SOUHAITABLE)

**Effort**: ⏱️ 2 heures  
**Impact**: MODÉRÉ  
**Dépendances**: swagger-ui-express, swagger-jsdoc

### Installation

```bash
npm install swagger-ui-express swagger-jsdoc
npm install --save-dev @types/swagger-ui-express @types/swagger-jsdoc
```

### Implémentation

Créer `src/infrastructure/swagger.ts`:
```typescript
import swaggerJsdoc from 'swagger-jsdoc';
import swaggerUi from 'swagger-ui-express';

const options = {
  definition: {
    openapi: '3.0.0',
    info: {
      title: 'User Management API',
      version: '1.0.0',
      description: 'API pour gérer les utilisateurs',
      contact: {
        name: 'Support',
        email: 'support@example.com'
      }
    },
    servers: [
      {
        url: 'http://localhost:3000',
        description: 'Développement'
      },
      {
        url: 'https://api.example.com',
        description: 'Production'
      }
    ],
    components: {
      securitySchemes: {
        bearerAuth: {
          type: 'http',
          scheme: 'bearer',
          bearerFormat: 'JWT'
        }
      },
      schemas: {
        User: {
          type: 'object',
          properties: {
            id: { type: 'string', format: 'uuid' },
            name: { type: 'string' },
            email: { type: 'string', format: 'email' }
          }
        },
        Error: {
          type: 'object',
          properties: {
            type: { type: 'string' },
            status: { type: 'integer' },
            title: { type: 'string' },
            detail: { type: 'string' }
          }
        }
      }
    },
    security: [{ bearerAuth: [] }]
  },
  apis: ['./src/infrastructure/server.ts']
};

export const specs = swaggerJsdoc(options);
export const swaggerUI = swaggerUi;
```

Modifier `server.ts`:
```typescript
import { specs, swaggerUI } from './swagger';

app.use('/docs', swaggerUI.serve, swaggerUI.setup(specs));
```

### Ajouter JSDoc aux endpoints

```typescript
/**
 * @swagger
 * /v1/users:
 *   post:
 *     summary: Créer un nouvel utilisateur
 *     tags: [Users]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/User'
 *     responses:
 *       201:
 *         description: Utilisateur créé
 *       422:
 *         description: Erreur de validation
 */
v1Router.post('/users', (req, res, next) => {
  // ...
});
```

### Accès

```
http://localhost:3000/docs
```

---

## 💾 ACTION 10: Ajouter Caching (SOUHAITABLE)

**Effort**: ⏱️ 1 heure  
**Impact**: MODÉRÉ  
**Dépendances**: Aucune (utiliser les headers HTTP)

### Implémentation

Créer `src/infrastructure/caching.ts`:
```typescript
import crypto from 'crypto';

export const generateETag = (data: any): string => {
  return crypto
    .createHash('md5')
    .update(JSON.stringify(data))
    .digest('hex');
};

export const cacheMiddleware = (req: any, res: any, next: any) => {
  const originalJson = res.json;

  res.json = function (data: any) {
    // Ajouter ETag
    const etag = generateETag(data);
    res.set('ETag', `"${etag}"`);

    // Ajouter Cache-Control
    if (req.method === 'GET') {
      res.set('Cache-Control', 'public, max-age=300'); // 5 minutes
    } else {
      res.set('Cache-Control', 'no-cache');
    }

    return originalJson.call(this, data);
  };

  next();
};
```

Utiliser dans `server.ts`:
```typescript
app.use(cacheMiddleware);
```

---

## 📈 Timeline de Correction Recommandée

```
Semaine 1:
  Jour 1-2: ACTION 1 (Versioning)
  Jour 2-3: ACTION 2 (Authentification)
  Jour 3-4: ACTION 3 (Gestion erreurs)
  Jour 4-5: ACTION 4 (Nommage) + ACTION 8 (Validation)

Semaine 2:
  Jour 1-2: ACTION 5 (Pagination)
  Jour 2-3: ACTION 6 (Rate limiting)
  Jour 3-4: ACTION 7 (UUID)
  Jour 4-5: Tests et débogage

Semaine 3+:
  ACTION 9 (Documentation)
  ACTION 10 (Caching)
  Tests d'intégration
  Code review et déploiement
```

---

## ✅ Critères d'Acceptation

Pour chaque action, valider:
- ✅ Code implémenté
- ✅ Tests passent (tests existants + nouveaux)
- ✅ Pas de régression
- ✅ Documentation mise à jour
- ✅ Code review approuvé

---

## 🔗 Ressources Supplémentaires

- [Express.js Documentation](https://expressjs.com/)
- [JWT Best Practices](https://tools.ietf.org/html/rfc7519)
- [HTTP Status Codes](https://httpwg.org/specs/rfc7231.html)
- [OpenAPI 3.0](https://spec.openapis.org/)
- [OWASP API Security](https://owasp.org/www-project-api-security/)


