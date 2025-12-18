# Rapport d'Analyse API

**Projet** : Bad API Demo Project
**Date d'analyse** : 18/12/2025
**Version Guidelines** : v3.1 (Pragmatic)
**Analysé par** : IA

---

## 1) Vue d'Ensemble du Projet

- Type de projet : MVP / Démonstration
- Stack technique : Node.js, Express, TypeScript
- Nombre d'endpoints (estimé) : 4
- Type de clients : Web/API
- Architecture : Hexagonale simplifiée (Domain / Application / Infrastructure)

Description rapide : ce projet démontre volontairement des violations des bonnes pratiques de conception d’API pour servir d’exemple à un linter et à une analyse guidée.

---

## 2) Analyse par Catégorie

### 2.1 Security (Poids : 15%)
**Type** : ✅ Obligatoire  
**Score** : 2/10

**Justification du score** :
- Absence d’authentification/autorisation sur les endpoints.
- Pas de validation d’entrées au niveau des contrôleurs ni du service.
- Pas de rate limiting ni de configuration HTTPS (en prod).
- Accès direct et non encapsulé à des structures internes.

**Points forts** ✅ :
- Séparation de couches claire (facilite l’ajout de sécurité).

**Points faibles** ❌ :
- Aucune authentification ni contrôle d’accès.
- Aucune validation d’entrées côté serveur.
- Pas de limitation de débit (rate limiting) ni CORS configuré explicitement.

**Recommandations** :
- Introduire JWT/OAuth/API keys selon le contexte.
- Ajouter une validation stricte des payloads (Zod/Joi/class-validator).
- Activer CORS et rate limiting, forcer HTTPS en production.

---

### 2.2 Error Handling (Poids : 10%)
**Type** : ✅ Obligatoire  
**Score** : 1/10

**Justification du score** :
- Les erreurs ne sont pas uniformisées (pas de middleware d’erreur global).
- Les cas « ressource introuvable » ne renvoient pas 404 de manière fiable.
- Pas de logs d’erreurs structurés.

**Points forts** ✅ :
- Express permet d’ajouter facilement un middleware d’erreur.

**Points faibles** ❌ :
- Réponses implicites (res.send) sans statut adapté.
- Pas de format d’erreur standard { code, message, details }.

**Recommandations** :
- Créer une classe ApiError et un middleware d’erreur global.
- Retourner des statuts et messages cohérents (400, 404, 500, etc.).

---

### 2.3 API Versioning (Poids : 15%)
**Type** : ⚠️ Conditionnel  
**Score** : 0/10

**Justification du score** :
- Aucune stratégie de versionnage (URL path /v1, header, etc.).

**Points forts** ✅ :
- N/A

**Points faibles** ❌ :
- Évolution future difficile sans casse potentielle.

**Recommandations** :
- Introduire une stratégie de versionnage (ex: /v1) si l’API est destinée à évoluer ou à être consommée par des clients variés.

---

### 2.4 URL Structure (Poids : 10%)
**Type** : ✅ Obligatoire  
**Score** : 2/10

**Justification du score** :
- Noms d’URL actionnels au lieu de ressources (ex: addUser, getUser).
- Non-respect du style REST (collection au pluriel, ressources, sous-ressources).

**Points forts** ✅ :
- Facile à corriger en renommant les routes.

**Points faibles** ❌ :
- Verbes dans les chemins.
- Nomenclature incohérente avec les conventions REST.

**Exemple (avant → après)** :
- POST /addUser → POST /users
- GET /getUser/:id → GET /users/:id
- GET /allUsers → GET /users
- DELETE /removeUser/:id → DELETE /users/:id

**Recommandations** :
- Basculer toutes les routes vers des ressources (noms) au pluriel et utiliser les verbes HTTP pour l’action.

---

### 2.5 HTTP Methods (Poids : 10%)
**Type** : ✅ Obligatoire  
**Score** : 8/10

**Justification du score** :
- L’usage de POST, GET, DELETE est cohérent avec l’intention.
- Manque PUT/PATCH pour la mise à jour.

**Points forts** ✅ :
- Verbes HTTP globalement corrects pour les actions présentes.

**Points faibles** ❌ :
- Pas de PUT/PATCH pour les updates.

**Recommandations** :
- Ajouter PUT/PATCH pour compléter le CRUD.

---

### 2.6 Status Codes (Poids : 8%)
**Type** : ✅ Obligatoire  
**Score** : 1/10

**Justification du score** :
- Statuts implicites (200) même en cas d’erreur ou d’absence de ressource.
- Pas de 201 à la création, pas de 204 en suppression, pas de 404 si introuvable.

**Points forts** ✅ :
- Facile à corriger avec un middleware d’erreur et un contrat de réponse clair.

**Points faibles** ❌ :
- Ambiguïté côté clients (impossible de distinguer succès/erreur sans conventions).

**Recommandations** :
- Utiliser 201 Created pour POST réussi, 204 No Content pour DELETE, 404 Not Found si introuvable, 400 pour validation, 500 pour erreurs internes.

---

### 2.7 Pagination (Poids : 10%)
**Type** : ⚠️ Conditionnel  
**Score** : 0/10

**Justification du score** :
- Les listes ne sont pas paginées (retour complet).

**Points forts** ✅ :
- Facile à ajouter sur l’endpoint de collection.

**Points faibles** ❌ :
- Pas de limit/offset ni de métadonnées.

**Recommandations** :
- Introduire limit/offset (ou cursor), bornes par défaut et max, renvoyer meta { total, limit, offset, pages }.

---

### 2.8 HATEOAS (Poids : 5%)
**Type** : ❌ Optionnel  
**Score** : 0/10 (Non applicable au MVP)

**Justification du score** :
- Non requis pour un MVP de démonstration.

**Recommandations** :
- Optionnel : ajouter des liens self ou HAL/JSON-LD si besoin de découverte.

---

### 2.9 Documentation (Poids : 5%)
**Type** : ✅ Obligatoire  
**Score** : 3/10

**Justification du score** :
- README présent mais succinct pour l’API.
- Pas de spécification OpenAPI/Swagger ni d’exemples cURL détaillés.

**Points forts** ✅ :
- README de projet existant.

**Points faibles** ❌ :
- Manque d’OpenAPI, pas de codes d’erreur documentés, pas de collection Postman/Insomnia.

**Recommandations** :
- Créer une spec OpenAPI, ajouter exemples cURL, documenter les codes d’erreur et les schémas de réponse.

---

### 2.10 Query Parameters (Poids : 5%)
**Type** : ⚠️ Conditionnel  
**Score** : 0/10

**Justification du score** :
- Aucun filtrage/tri/recherche via query params.

**Recommandations** :
- Ajouter ?limit, ?offset, ?sort, ?search selon le besoin et valider ces paramètres.

---

### 2.11 Content Negotiation (Poids : 5%)
**Type** : ❌ Optionnel  
**Score** : 5/10

**Justification du score** :
- JSON par défaut via express.json(), pas de gestion avancée d’Accept.
- Suffisant pour un MVP, améliorable pour des besoins multi-formats.

**Recommandations** :
- Gzip/compression, éventuellement autres formats si requis, vérifier les en-têtes Accept/Content-Type.

---

### 2.12 Infrastructure (Poids : 2%)
**Type** : ✅ Obligatoire  
**Score** : 4/10

**Justification du score** :
- Setup minimal fonctionnel (TS, structure claire).
- Manque logging structuré, health check, variables d’environnement, monitoring.

**Recommandations** :
- Ajouter logger (Winston/Pino), endpoint /health, config via .env + dotenv, métriques basiques et Dockerisation si pertinent.

---

## 3) Score Global de l’API

### Calcul détaillé (pondération)

| Catégorie              | Score | Poids | Points |
|------------------------|-------|-------|--------|
| Security               | 2/10  | 15%   | 0.30   |
| Error Handling         | 1/10  | 10%   | 0.10   |
| API Versioning         | 0/10  | 15%   | 0.00   |
| URL Structure          | 2/10  | 10%   | 0.20   |
| HTTP Methods           | 8/10  | 10%   | 0.80   |
| Status Codes           | 1/10  | 8%    | 0.08   |
| Pagination             | 0/10  | 10%   | 0.00   |
| HATEOAS                | 0/10  | 5%    | 0.00   |
| Documentation          | 3/10  | 5%    | 0.15   |
| Query Parameters       | 0/10  | 5%    | 0.00   |
| Content Negotiation    | 5/10  | 5%    | 0.25   |
| Infrastructure         | 4/10  | 2%    | 0.08   |
| **TOTAL**              |       | 100%  | **1.96 / 100** |

### Niveau atteint
- Score global : ~2 / 100
- Niveau : MVP/Prototype (démonstration volontaire de mauvaises pratiques)

### Conclusion
Le projet remplit son objectif de démonstration. Pour converger vers une API « production ready », les priorités sont :
1) refonte des URLs en suivant REST,
2) gestion d’erreurs et statuts HTTP,
3) validation d’entrées et authentification,
4) pagination et query params,
5) documentation (OpenAPI) et logging/health-check.

Ces actions suffisent à faire progresser rapidement le score (> 60/100) et améliorer l’expérience des clients de l’API.
