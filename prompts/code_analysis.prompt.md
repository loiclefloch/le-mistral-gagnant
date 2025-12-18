# Prompt d'Analyse API selon Guidelines Pragmatiques

## Objectif

Étudie ce projet et analyse son API en te basant sur le document de guidelines fourni.

## Instructions Générales

Crée un rapport complet dans le répertoire `mistral/analysis/{date_du_jour}` ou date du jour correspond à la date du jour au format JJ/MM/AAAA composé de **3 fichiers Markdown** :

1. `api-analysis-report.md` - Rapport d'analyse détaillé
2. `improvement-action-plan.md` - Plan d'action d'amélioration
3. `README.md` - Synthèse exécutive


---

## 1. Fichier : `api-analysis-report.md`

### Structure du Rapport

#### En-tête du Document

```markdown
# Rapport d'Analyse API

**Projet** : [Nom du projet]
**Date d'analyse** : [Date]
**Version Guidelines** : [Version du fichier de guidelines utilisé]
**Analysé par** : [IA/Nom]
```

#### Section 1 : Vue d'Ensemble du Projet

Fournis une analyse contextuelle :

- **Type de projet** : MVP / Startup / Production / Platform
- **Stack technique** : Technologies identifiées (Node.js, Express, Nest.js, etc.)
- **Nombre d'endpoints** : Estimation
- **Type de clients** : Web / Mobile / AI / Mixte
- **Architecture** : Monolithe / Microservices / Autre

Si tu n'as pas assez d'informations, pose la question à l'utilisateur avant de continuer au lieu de faire des suppositions.

#### Section 2 : Analyse par Catégorie

Pour **chaque catégorie** des guidelines, fournis :

##### Structure de Section

```markdown
### [Nom de la Catégorie] (Poids : X%)

**Type** : ✅ Obligatoire / ⚠️ Conditionnel / ❌ Optionnel

**Score** : X/10

**Justification du score** :
[Explication détaillée avec arguments concrets]

**Observations détaillées** :
[Observations faites pour cette partie]

**Points forts** ✅ :
- [Point fort 1] - [`fichier.ext`](chemin/vers/fichier.ext)
- [Point fort 2] - [`autre-fichier.ext`](chemin/vers/autre-fichier.ext)

**Points faibles** ❌ :
- [Point faible 1] - [`fichier-probleme.ext`](chemin/vers/fichier-probleme.ext)
- [Point faible 2]

**Exemples de code** :

\```typescript
// Exemple extrait du projet
[Code existant qui illustre le point]
\```

**Recommandations** :
- [Recommandation 1]
- [Recommandation 2]
```

##### Catégories à Analyser (dans l'ordre de priorité)

1. **Security (15%)** - ✅ OBLIGATOIRE
   - HTTPS configuré ?
   - Authentification (JWT, OAuth, API keys) ?
   - Validation des entrées ?
   - Gestion des secrets ?
   - Rate limiting ?
   - CORS configuré ?
   - Messages d'erreur sécurisés ?
   - **Fichiers clés** : middleware auth, config serveur, validation schemas

2. **Error Handling (10%)** - ✅ OBLIGATOIRE
   - Format d'erreur standardisé ?
   - Messages clairs et cohérents ?
   - Status codes HTTP appropriés ?
   - Logs côté serveur ?
   - Pas d'exposition de stack traces ?
   - **Fichiers clés** : error handlers, middleware, controllers

3. **API Versioning (15%)** - ⚠️ CONDITIONNEL
   - Stratégie choisie (none / query param / URL path) ?
   - Appropriée au contexte (nombre de clients, mobile app) ?
   - Correctement implémentée ?
   - Rétrocompatibilité gérée ?
   - Documentation de migration ?
   - **Fichiers clés** : routes, configuration versioning

4. **URL Structure (10%)** - ✅ OBLIGATOIRE
   - Basées sur des ressources (nouns) ?
   - Pluriel pour les collections ?
   - Convention de nommage (kebab-case/camelCase) cohérente ?
   - Profondeur raisonnable (≤ 2-3 niveaux) ?
   - Operations vs Resources bien équilibré ?
   - **Fichiers clés** : fichiers de routes, controllers

5. **HTTP Methods (10%)** - ✅ OBLIGATOIRE
   - GET, POST, PUT, PATCH, DELETE utilisés correctement ?
   - Idempotence respectée ?
   - Operations pragmatiques (POST pour actions) acceptables ?
   - **Fichiers clés** : routes, handlers

6. **Status Codes (8%)** - ✅ OBLIGATOIRE
   - Codes essentiels présents (200, 201, 204, 400, 401, 403, 404, 500) ?
   - Codes avancés si appropriés (202, 409, 422, 429) ?
   - Usage cohérent à travers l'API ?
   - **Fichiers clés** : controllers, error handlers

7. **Pagination (10%)** - ⚠️ CONDITIONNEL
   - Collections paginées quand nécessaire ?
   - Mécanisme choisi (page/limit, cursor, offset) ?
   - Metadata fournie (total, pages, liens) ?
   - Headers Link optionnels ?
   - **Fichiers clés** : query handlers, collection controllers

8. **HATEOAS (5%)** - ❌ OPTIONNEL
   - Nécessaire pour ce projet ? (AI clients, discovery needed)
   - Niveau d'implémentation (none / self / standard / full) ?
   - Headers Link utilisés ?
   - État de la machine représenté ?
   - **Fichiers clés** : response formatters, serializers

9. **Documentation (5%)** - ✅ OBLIGATOIRE
   - Documentation existe ? (README, Swagger, Postman)
   - Exemples de requêtes fournis ?
   - Authentification documentée ?
   - Endpoints listés avec descriptions ?
   - Code examples ?
   - **Fichiers clés** : README.md, OpenAPI spec, docs/

10. **Query Parameters (5%)** - ⚠️ CONDITIONNEL
    - Filtrage disponible ?
    - Tri implémenté ?
    - Recherche possible ?
    - Naming cohérent ?
    - **Fichiers clés** : query parsers, filters

11. **Content Negotiation (5%)** - ❌ OPTIONNEL
    - Formats supportés (JSON, XML, autres) ?
    - Headers Accept/Content-Type gérés ?
    - Default sensible ?
    - **Fichiers clés** : middleware, parsers

12. **Infrastructure (2%)** - ✅ OBLIGATOIRE
    - Configuration environnement ?
    - Logs structurés ?
    - Health check endpoint ?
    - Monitoring basique ?
    - **Fichiers clés** : config, docker, deployment files

#### Section 3 : Score Global

```markdown
## Score Global de l'API

### Calcul Détaillé

| Catégorie              | Score | Poids | Points  |
|------------------------|-------|-------|---------|
| Security               | X/10  | 15%   | X.XX    |
| Error Handling         | X/10  | 10%   | X.XX    |
| API Versioning         | X/10  | 15%   | X.XX    |
| URL Structure          | X/10  | 10%   | X.XX    |
| HTTP Methods           | X/10  | 10%   | X.XX    |
| Status Codes           | X/10  | 8%    | X.XX    |
| Pagination             | X/10  | 10%   | X.XX    |
| HATEOAS                | X/10  | 5%    | X.XX    |
| Documentation          | X/10  | 5%    | X.XX    |
| Query Parameters       | X/10  | 5%    | X.XX    |
| Content Negotiation    | X/10  | 5%    | X.XX    |
| Infrastructure         | X/10  | 2%    | X.XX    |
| **TOTAL**              |       |       | **XX/100** |

### Niveau Atteint

**Score** : XX/100

**Niveau** : [MVP/Prototype / Startup API / Production Ready / Scale/Platform / Excellence]

### Interprétation

| Score       | Niveau              | Caractéristiques                          |
|-------------|---------------------|-------------------------------------------|
| 50-60%      | MVP/Prototype       | Fonctionnel, besoins d'améliorations      |
| 60-70%      | Startup API         | Bon pour petite équipe, à consolider      |
| 70-80%      | Production Ready    | Prêt pour usage production standard       |
| 80-90%      | Scale/Platform      | Robuste, scalable, excellentes pratiques  |
| 90%+        | Excellence          | Référence, exemplaire                     |

**Conclusion** :
[Paragraphe synthétisant l'état global de l'API]
```

---

## 2. Fichier : `improvement-action-plan.md`

### Structure du Plan d'Action

#### En-tête

```markdown
# Plan d'Action d'Amélioration API

**Score actuel** : XX/100
**Niveau actuel** : [Niveau]
**Score cible recommandé** : XX/100
**Niveau cible** : [Niveau]
**Phase du projet** : [MVP / Production / Scale / Excellence]
```

#### Section : Actions par Priorité

Pour chaque action, fournis :

```markdown
### 🔴 ACTIONS CRITIQUES

> **Définition** : Bloquants, problèmes de sécurité, bugs majeurs, risques importants.
> **Délai recommandé** : À traiter immédiatement (< 1 semaine)

---

#### 1. [Titre Action Critique #1]

**Catégorie impactée** : [Security / Error Handling / ...]
**Gain estimé** : +X points
**Difficulté** : 🟢 Facile / 🟡 Moyenne / 🔴 Difficile
**Priorité** : 🔴 Critique

**Description du problème** :
[Explication claire du problème actuel avec impact]

**Solution proposée** :
[Description détaillée de ce qu'il faut faire]

**Fichiers à modifier** :
- [`src/auth/middleware.ts`](src/auth/middleware.ts) - Ajouter validation JWT
- [`config/security.js`](config/security.js) - Configurer HTTPS
- [`📝 À créer : src/validation/schemas.ts`] - Schémas de validation

**Exemple de code** :

\```typescript
// ❌ Code actuel (problématique)
app.get('/users/:id', (req, res) => {
  const user = db.getUser(req.params.id); // Pas de validation
  res.json(user);
});

// ✅ Code amélioré
import { z } from 'zod';

const userIdSchema = z.string().uuid();

app.get('/users/:id', async (req, res) => {
  try {
    const userId = userIdSchema.parse(req.params.id);
    const user = await db.getUser(userId);
    res.json(user);
  } catch (error) {
    res.status(400).json({ error: 'Invalid user ID format' });
  }
});
\```

**Ressources** :
- [Zod Documentation](https://zod.dev)
- [Express Validation Best Practices](https://...)

**Critères de succès** :
- [ ] Validation implémentée sur tous les endpoints
- [ ] Tests unitaires ajoutés
- [ ] Documentation mise à jour

---

#### 2. [Titre Action Critique #2]

[Même structure...]

---

### 🟠 ACTIONS HAUTE PRIORITÉ

> **Définition** : Important pour la production, améliore significativement la qualité.
> **Délai recommandé** : 1-2 semaines

[Même structure que Critique, avec actions haute priorité...]

---

### 🟡 ACTIONS PRIORITÉ MOYENNE

> **Définition** : Améliore l'expérience développeur et la maintenabilité.
> **Délai recommandé** : 1 mois

[Même structure...]

---

### 🟢 ACTIONS PRIORITÉ BASSE

> **Définition** : Nice to have, optimisations, perfections.
> **Délai recommandé** : Quand temps disponible

[Même structure...]
```

#### Section : Quick Wins

```markdown
## 🚀 Quick Wins (Ratio Gain/Effort Optimal)

Actions à impact maximum avec effort minimum :

| Action                          | Gain | Difficulté | Fichiers                    |
|---------------------------------|------|------------|-----------------------------|
| [Action 1]                      | +X   | 🟢         | [`file.ts`](path/file.ts)   |
| [Action 2]                      | +X   | 🟢         | [`file.ts`](path/file.ts)   |
| [Action 3]                      | +X   | 🟡         | [`file.ts`](path/file.ts)   |

**Recommandation** : Commencer par ces actions pour un boost rapide du score.
```

#### Section : Roadmap Suggérée

```markdown
## 📅 Roadmap d'Amélioration Suggérée

### Phase 1 : Fondations (X semaines)

**Objectif** : Sécuriser et stabiliser l'API
**Score cible** : XX/100

**Actions** :
- [ ] [Action Critique 1] - [`fichier1.ts`](path/to/fichier1.ts)
- [ ] [Action Critique 2] - [`fichier2.ts`](path/to/fichier2.ts)
- [ ] [Quick Win 1] - [`fichier3.ts`](path/to/fichier3.ts)

**Effort total** : XX heures/jours

---

### Phase 2 : Production Ready (X semaines)

**Objectif** : Rendre l'API robuste et maintenable
**Score cible** : XX/100

**Actions** :
- [ ] [Action Haute 1]
- [ ] [Action Haute 2]
- [ ] [Action Moyenne 1]

**Effort total** : XX heures/jours

---

### Phase 3 : Excellence (X semaines)

**Objectif** : Atteindre les meilleures pratiques
**Score cible** : XX/100

**Actions** :
- [ ] [Action Moyenne 2]
- [ ] [Action Basse 1]
- [ ] [Optimisation avancée]

**Effort total** : XX heures/jours

---

## 📊 Évolution du Score Projetée

| Phase           | Score Actuel | Score Cible | Gain   | Effort     |
|-----------------|--------------|-------------|--------|------------|
| **Maintenant**  | XX/100       | -           | -      | -          |
| **Phase 1**     | -            | XX/100      | +XX    | X semaines |
| **Phase 2**     | -            | XX/100      | +XX    | X semaines |
| **Phase 3**     | -            | XX/100      | +XX    | X semaines |
```

---

## 3. Fichier : `README.md`

### Structure de la Synthèse

```markdown
# Analyse API - Synthèse Exécutive

> Rapport d'analyse complet de l'API selon les guidelines pragmatiques v2.0

---

## 📊 Score Global

### Résultat

**XX/100** - Niveau : **[MVP/Startup/Production/Platform/Excellence]**

```
████████░░ 80%
```

### Répartition par Catégorie

| Catégorie           | Score | Poids | Points       | État    |
|---------------------|-------|-------|--------------|---------|
| Security            | 1/10  | 15%   | 0.15         | 🟢/🟡/🔴 |
| Error Handling      | 2/10  | 10%   | 0.20         | 🟢/🟡/🔴 |
| API Versioning      | 3/10  | 15%   | 0.45         | 🟢/🟡/🔴 |
| URL Structure       | 4/10  | 10%   | 0.40         | 🟢/🟡/🔴 |
| HTTP Methods        | 3/10  | 10%   | 0.30         | 🟢/🟡/🔴 |
| Status Codes        | 2/10  | 8%    | 0.16         | 🟢/🟡/🔴 |
| Pagination          | 1/10  | 10%   | 0.10         | 🟢/🟡/🔴 |
| HATEOAS             | 0/10  | 5%    | 0.00         | 🟢/🟡/🔴 |
| Documentation       | 4/10  | 5%    | 0.20         | 🟢/🟡/🔴 |
| Query Parameters    | 3/10  | 5%    | 0.15         | 🟢/🟡/🔴 |
| Content Negotiation | 7/10  | 5%    | 0.35         | 🟢/🟡/🔴 |
| Infrastructure      | 4/10  | 2%    | 0.08         | 🟢/🟡/🔴 |
| **TOTAL**           |       |       | **25.4/100** | [ETAT MOYEN: 🟢/🟡/🔴] |
---

## 🎯 Top 3 Points Forts

1. **[Catégorie]** : [Brève description du point fort] - [`fichier.ts`](path/fichier.ts)
2. **[Catégorie]** : [Brève description du point fort] - [`fichier.ts`](path/fichier.ts)
3. **[Catégorie]** : [Brève description du point fort] - [`fichier.ts`](path/fichier.ts)

---

## 🚀 Quick Wins (À Faire en Premier)

Actions à impact maximum avec effort minimum :

- [ ] **[Action 1]** - **+X pts** - 🟢 Facile
- [ ] **[Action 2]** - **+X pts** - 🟡 Moyenne
- [ ] **[Action 3]** - **+X pts** - 🟢 Facile

**Gain total estimé** : +XX points

---

## 📈 Évolution Possible du Score

| Phase              | Score Actuel | Score Cible |
|--------------------|--------------|-------------|
| **Maintenant**     | XX/100       | -           |
| **Phase 1 (Fondations)**  | -    | XX/100      |
| **Phase 2 (Production)**  | -    | XX/100      |
| **Phase 3 (Excellence)**  | -    | XX/100      | 

---

## 📁 Documents Détaillés

- 📊 [**Rapport d'Analyse Complet**](api-analysis-report.md) - Analyse catégorie par catégorie avec scores et observations
- 📋 [**Plan d'Action Détaillé**](improvement-action-plan.md) - Actions concrètes avec priorités et exemples de code

---

## 🔍 Méthodologie

Cette analyse a été réalisée selon les **API Guidelines**, qui évaluent :

- ✅ 12 catégories (Security, Error Handling, Versioning, etc.)
- ✅ Scoring pondéré sur 100 points
- ✅ Approche pragmatique adaptée au contexte du projet
- ✅ Recommandations actionnables avec exemples de code
- ✅ Liens directs vers les fichiers concernés

---

## 📞 Prochaines Étapes Recommandées

1. **Immédiat** :
   - Traiter les actions 🔴 **CRITIQUES**
   - Implémenter les Quick Wins

2. **Court terme** :
   - Traiter les actions 🟠 **HAUTE PRIORITÉ**
   - Commencer Phase 1 de la roadmap

3. **Moyen terme** :
   - Actions 🟡 **PRIORITÉ MOYENNE**
   - Phases 2 et 3 de la roadmap

4. **Long terme** :
   - Actions 🟢 **BASSE PRIORITÉ**
   - Optimisations et perfectionnement

---
```

---

## Consignes Importantes pour l'IA

### 1. Liens vers Fichiers (OBLIGATOIRE)

**Format à utiliser** :
```markdown
[`nom-fichier.ext`](chemin/relatif/nom-fichier.ext)
```

**Exemples** :
- Fichier existant : [`routes/users.ts`](src/routes/users.ts)
- Fichier à créer : [`📝 À créer : error-handler.ts`]
- Dossier : [`src/middleware/`](src/middleware/)

**Règles** :
- ✅ TOUJOURS inclure des liens vers les fichiers concernés
- ✅ Utiliser des chemins relatifs depuis la racine du projet
- ✅ Indiquer clairement les fichiers à créer avec l'emoji 📝
- ✅ Grouper les fichiers par catégorie quand pertinent

### 2. Scoring (Objectivité)

- ✅ Sois **objectif et précis** dans chaque score
- ✅ **Justifie** chaque point attribué ou retiré avec des exemples concrets
- ✅ Compare au **contexte du projet** (MVP vs Enterprise vs AI-consumed)
- ✅ Utilise les **critères des guidelines** (0-2 : Critique, 3-5 : Needs improvement, 6-7 : Good enough, 8-9 : Excellent, 10 : Perfect)

### 3. Pragmatisme (Adaptation au Contexte)

- ✅ Identifie le **type de projet** (MVP, Startup, Production, Platform)
- ✅ Adapte les **recommandations au contexte**
- ✅ Ne pénalise **pas** l'absence de features optionnelles si non nécessaires
- ✅ Distingue clairement : **OBLIGATOIRE** / **CONDITIONNEL** / **OPTIONNEL**
- ✅ Explique **pourquoi** une pratique est nécessaire ou non dans ce contexte

### 4. Actions Concrètes

- ✅ Sois **concret et actionnable** : chaque action doit être claire et réalisable
- ✅ Fournis des **exemples de code** "avant/après" quand pertinent
- ✅ Estime **réalistement l'effort** (heures/jours, pas vague)
- ✅ Priorise selon l'**impact réel** sur la qualité et la sécurité
- ✅ Indique les **fichiers exacts** à modifier avec liens

### 5. Format et Clarté

- ✅ Utilise **Markdown** correctement avec headers, tables, lists
- ✅ Ajoute des **emojis** pour la clarté visuelle (🔴🟠🟡🟢, ✅❌, 📊📋📁)
- ✅ Structure avec des **sections claires** et hiérarchie logique
- ✅ Inclus des **code blocks** avec syntax highlighting approprié
- ✅ Utilise des **tableaux** pour les comparaisons et résumés

### 6. Exemples de Code

**Format attendu** :

```typescript
// ❌ AVANT (Problématique)
[Code actuel avec le problème]

// ✅ APRÈS (Amélioré)
[Code corrigé avec bonnes pratiques]
```

### 7. Cohérence Inter-Fichiers

- ✅ Assure la **cohérence** entre les 3 fichiers (scores, actions, synthèse)
- ✅ Les liens entre fichiers doivent **fonctionner** (anchors Markdown)
- ✅ Les **mêmes actions** doivent avoir les **mêmes titres** partout
- ✅ Les **fichiers mentionnés** doivent être **cohérents** entre rapports

### 8. Ton et Style

- ✅ Ton **professionnel mais accessible**
- ✅ Explications **claires et pédagogiques**
- ✅ **Positif et constructif** (pas seulement critique)
- ✅ **Factuel** : base sur des observations concrètes du code
- ✅ **Synthétique** : aller droit au but

---

## Exemple de Début d'Exécution

Une fois ce prompt fourni, l'IA devrait commencer ainsi :

[✓] Explorer le projet et comprendre la structure de l'API
[✓] Analyser chaque catégorie selon les guidelines pragmatiques
[✓] Calculer les scores et générer le rapport d'analyse détaillé
[✓] Créer le plan d'action d'amélioration avec actions prioritaires
[✓] Générer la synthèse exécutive README.md

```markdown
# Démarrage de l'analyse...

## Étape 1 : Exploration du projet

[L'IA liste les fichiers principaux trouvés]

## Étape 2 : Identification du contexte

- Type de projet détecté : [MVP/Startup/Production/Platform]
- Stack technique : [Technologies]
- Clients cibles : [Web/Mobile/AI]

## Étape 3 : Analyse catégorie par catégorie

[L'IA analyse chaque catégorie des guidelines]

## Étape 4: Calculer les scores et générer le rapport d'analyse détaillé

Création de :
- `mistral/analysis/api-analysis-report.md`

## Étape 5: Créer le plan d'action d'amélioration avec actions prioritaires

Création de :
- `mistral/analysis/improvement-action-plan.md`
 

## Étape 6 : Générer la synthèse exécutive README.md

Création de :
- `mistral/analysis/README.md`

[Contenu généré selon les structures ci-dessus]
```

---

**Commence l'analyse maintenant en explorant le projet.**

