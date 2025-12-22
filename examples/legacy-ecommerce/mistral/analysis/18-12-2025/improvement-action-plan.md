# Plan d'Action d'Amélioration API

**Score actuel** : 25/100  
**Niveau actuel** : Prototype Cassé / En-dessous du seuil MVP acceptable  
**Score cible recommandé** : 60/100  
**Niveau cible** : Production Ready  
**Phase du projet** : MVP → Refactoring pour Production

---

## 🔴 ACTIONS CRITIQUES

> **Définition** : Bloquants, problèmes de sécurité, bugs majeurs, risques importants.  
> **Délai recommandé** : À traiter immédiatement (< 1 semaine)

---

### 1. Corriger les GET qui modifient l'état (Violation REST majeure)

**Catégorie impactée** : HTTP Methods, URL Structure  
**Gain estimé** : +1.5 points  
**Difficulté** : 🟢 Facile  
**Effort estimé** : 2 heures  
**Priorité** : 🔴 Critique

**Description du problème** :
Plusieurs endpoints GET modifient l'état des ressources, ce qui viole le principe REST fondamental de l'idempotence. Cela peut causer des problèmes de cache, de sécurité (CSRF), et de compréhension de l'API par les clients.

**Fichiers concernés** :
- [`OrderController.java:111-120`](src/main/java/com/ecommerce/controller/OrderController.java) - GET modifie status de PENDING → VIEWED
- [`OrderController.java:201-209`](src/main/java/com/ecommerce/controller/OrderController.java) - GET /orders/{id}/ship modifie status → SHIPPED

**Solution proposée** :
1. Supprimer la modification dans le GET `/orders/{id}` (lecture pure)
2. Remplacer GET `/orders/{id}/ship` par PUT `/orders/{id}/status` avec body

**Exemple de code** :

```java
// ❌ Code actuel (VIOLATION)
@GetMapping("/{orderId}")
public Order getOrder(@PathVariable Long orderId) {
    Order order = orderService.getOrder(orderId);
    if (order != null && order.status.equals("PENDING")) {
        order.status = "VIEWED";  // ← MODIFIE L'ÉTAT !
    }
    return order;
}

@GetMapping("/{orderId}/ship")
public String shipOrder(@PathVariable Long orderId) {
    Order order = orderService.getOrder(orderId);
    if (order != null) {
        order.status = "SHIPPED";  // ← MODIFIE L'ÉTAT !
        return "Order shipped";
    }
    return "Order not found";
}

// ✅ Code corrigé
@GetMapping("/{orderId}")
public ResponseEntity<Order> getOrder(@PathVariable Long orderId) {
    Order order = orderService.getOrder(orderId);
    if (order == null) {
        return ResponseEntity.notFound().build();
    }
    // NE MODIFIE PLUS L'ÉTAT - lecture pure
    return ResponseEntity.ok(order);
}

// ✅ SUPPRIMÉ : GET /ship remplacé par PUT /status (voir action séparée)
```

**Critères de succès** :
- [ ] GET `/orders/{id}` ne modifie plus le status
- [ ] GET `/orders/{id}/ship` supprimé
- [ ] Nouveaux endpoints PUT pour modifications documentés
- [ ] Tests ajoutés vérifiant l'idempotence

---

### 2. Implémenter validation des entrées (Security)

**Catégorie impactée** : Security, Error Handling  
**Gain estimé** : +2 points  
**Difficulté** : 🟡 Moyenne  
**Effort estimé** : 1 jour  
**Priorité** : 🔴 Critique

**Description du problème** :
Aucune validation des données entrantes n'existe. Les clients peuvent envoyer des prix négatifs, des quantités négatives, des IDs invalides, ou des données malformées sans aucun contrôle. Cela expose l'API à des abus et des crashs.

**Fichiers à modifier** :
- [`ProductController.java`](src/main/java/com/ecommerce/controller/ProductController.java) - Ajouter @Valid sur tous les @RequestBody
- [`OrderController.java`](src/main/java/com/ecommerce/controller/OrderController.java) - Ajouter @Valid et validation query params
- [`Product.java`](src/main/java/com/ecommerce/model/Product.java) - Ajouter annotations Bean Validation
- [`Order.java`](src/main/java/com/ecommerce/model/Order.java) - Ajouter annotations Bean Validation
- [`📝 À créer : GlobalExceptionHandler.java`] - Gérer MethodArgumentNotValidException

**Solution proposée** :
1. Ajouter dépendance spring-boot-starter-validation dans pom.xml
2. Annoter les classes model avec @NotNull, @Min, @Max, @Size, etc.
3. Ajouter @Valid sur les @RequestBody dans les contrôleurs
4. Créer exception handler global pour retourner 400 avec détails

**Exemple de code** :

```xml
<!-- pom.xml - Ajouter dépendance -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

```java
// ❌ Code actuel (AUCUNE VALIDATION)
public class Product {
    public Long id;
    public String name;
    public double price;  // ← Peut être négatif !
    public int stock;     // ← Peut être négatif !
    public String category;
    // ...
}

@PostMapping
public Product createProduct(@RequestBody Product product) {
    // Aucune validation ! Prix négatif ? Stock -100 ? OK !
    return repository.save(product);
}

// ✅ Code corrigé avec validation
public class Product {
    
    @NotNull(message = "Product ID cannot be null")
    private Long id;
    
    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    @DecimalMax(value = "1000000", message = "Price must not exceed 1,000,000")
    private Double price;
    
    @Min(value = 0, message = "Stock cannot be negative")
    @Max(value = 100000, message = "Stock must not exceed 100,000")
    private Integer stock;
    
    @NotBlank(message = "Category is required")
    private String category;
    
    // Getters/Setters (IMPORTANT : champs private avec getters/setters)
}

@PostMapping
public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
    // @Valid déclenche validation automatique
    // Si validation échoue → 400 Bad Request automatique
    Product created = repository.save(product);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
}

// ✅ Exception handler global
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse response = new ErrorResponse(
            "Validation failed",
            errors,
            HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.badRequest().body(response);
    }
}

// ✅ Classe ErrorResponse
public class ErrorResponse {
    private String message;
    private Map<String, String> errors;
    private int status;
    private LocalDateTime timestamp;
    
    public ErrorResponse(String message, Map<String, String> errors, int status) {
        this.message = message;
        this.errors = errors;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
    // Getters/Setters
}
```

**Ressources** :
- [Bean Validation Documentation](https://jakarta.ee/specifications/bean-validation/3.0/)
- [Spring Validation Guide](https://spring.io/guides/gs/validating-form-input/)

**Critères de succès** :
- [ ] Toutes les classes model ont annotations de validation
- [ ] Tous les @RequestBody ont @Valid
- [ ] GlobalExceptionHandler retourne 400 avec détails
- [ ] Tests de validation ajoutés
- [ ] Documentation mise à jour avec contraintes

---

### 3. Utiliser ResponseEntity pour tous les retours (Status Codes)

**Catégorie impactée** : Status Codes, Error Handling  
**Gain estimé** : +1.5 points  
**Difficulté** : 🟢 Facile  
**Effort estimé** : 4 heures  
**Priorité** : 🔴 Critique

**Description du problème** :
Actuellement, tous les endpoints retournent des objets directs ou void, ce qui fait que Spring Boot retourne toujours 200 OK par défaut. Impossible de retourner 201 Created, 204 No Content, 404 Not Found, etc.

**Fichiers à modifier** :
- [`ProductController.java`](src/main/java/com/ecommerce/controller/ProductController.java) - Tous les endpoints
- [`OrderController.java`](src/main/java/com/ecommerce/controller/OrderController.java) - Tous les endpoints

**Solution proposée** :
Remplacer tous les types de retour directs par `ResponseEntity<T>` pour contrôler explicitement les status codes.

**Exemple de code** :

```java
// ❌ Code actuel (retourne toujours 200)
@PostMapping
public Product createProduct(@RequestBody Product product) {
    return repository.save(product);  // ← 200 OK au lieu de 201 Created
}

@DeleteMapping("/{id}")
public void deleteProduct(@PathVariable Long id) {
    repository.delete(id);  // ← 200 OK au lieu de 204 No Content
}

@GetMapping("/{id}")
public Product getProduct(@PathVariable Long id) {
    return repository.findById(id);  // ← 200 avec null au lieu de 404
}

// ✅ Code corrigé avec ResponseEntity
@PostMapping
public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
    Product created = repository.save(product);
    
    // 201 Created avec header Location
    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(created.getId())
        .toUri();
    
    return ResponseEntity.created(location).body(created);
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
    Product product = repository.findById(id);
    
    if (product == null) {
        return ResponseEntity.notFound().build();  // 404
    }
    
    repository.delete(id);
    return ResponseEntity.noContent().build();  // 204 No Content
}

@GetMapping("/{id}")
public ResponseEntity<Product> getProduct(@PathVariable Long id) {
    Product product = repository.findById(id);
    
    if (product == null) {
        return ResponseEntity.notFound().build();  // 404 Not Found
    }
    
    return ResponseEntity.ok(product);  // 200 OK
}

@PutMapping("/{id}")
public ResponseEntity<Product> updateProduct(
        @PathVariable Long id,
        @Valid @RequestBody Product product) {
    
    Product existing = repository.findById(id);
    if (existing == null) {
        return ResponseEntity.notFound().build();  // 404
    }
    
    product.setId(id);
    Product updated = repository.save(product);
    return ResponseEntity.ok(updated);  // 200 OK
}
```

**Résumé des changements** :

| Endpoint | Avant | Après | Status Code |
|----------|-------|-------|-------------|
| POST create | `Product` | `ResponseEntity<Product>` | 201 Created + Location |
| GET by id | `Product` | `ResponseEntity<Product>` | 200 OK ou 404 |
| PUT update | `Product` | `ResponseEntity<Product>` | 200 OK ou 404 |
| DELETE | `void` | `ResponseEntity<Void>` | 204 No Content ou 404 |

**Critères de succès** :
- [ ] Tous les endpoints utilisent ResponseEntity
- [ ] 201 Created pour créations avec header Location
- [ ] 204 No Content pour suppressions
- [ ] 404 Not Found quand ressource inexistante
- [ ] Tests vérifient les status codes corrects

---

### 4. Supprimer tous les doublons d'endpoints

**Catégorie impactée** : URL Structure, Maintenance  
**Gain estimé** : +1 point  
**Difficulté** : 🟢 Facile  
**Effort estimé** : 1 heure  
**Priorité** : 🔴 Critique

**Description du problème** :
L'API contient de nombreux doublons d'endpoints faisant exactement la même chose avec des URLs différentes. Cela crée confusion, maintenance difficile, et augmentation inutile de la surface d'attaque.

**Fichiers à modifier** :
- [`ProductController.java`](src/main/java/com/ecommerce/controller/ProductController.java)
- [`OrderController.java`](src/main/java/com/ecommerce/controller/OrderController.java)

**Solution proposée** :
Supprimer les endpoints dupliqués et ne garder que la version RESTful standard.

**Exemple de code** :

```java
// ❌ DOUBLONS À SUPPRIMER

// Doublon 1 : GET product by ID
@GetMapping("/{id}")
public Product getProduct(@PathVariable Long id) { ... }

@GetMapping("/get/{id}")  // ← SUPPRIMER CE DOUBLON
public Product getProductById(@PathVariable Long id) { ... }

// Doublon 2 : DELETE product
@DeleteMapping("/{id}")
public void deleteProduct(@PathVariable Long id) { ... }

@DeleteMapping("/remove/{id}")  // ← SUPPRIMER CE DOUBLON
public boolean removeProduct(@PathVariable Long id) { ... }

// Doublon 3 : UPDATE product
@PutMapping("/{id}")
public Product updateProduct(@PathVariable Long id, @RequestBody Product product) { ... }

@PostMapping("/{id}/update")  // ← SUPPRIMER CE DOUBLON
public Product modifyProduct(@PathVariable Long id, @RequestBody Product product) { ... }

// Doublon 4 : Add to cart
@PostMapping("/cart/{cartId}/items")
public void addToCart(...) { ... }

@PostMapping("/cart/{cartId}/add")  // ← SUPPRIMER CE DOUBLON
public String addItemToCart(...) { ... }

// Doublon 5 : Update order status
@PutMapping("/{orderId}/status")
public void updateOrderStatus(...) { ... }

@PostMapping("/{orderId}/status")  // ← SUPPRIMER CE DOUBLON
public String changeStatus(...) { ... }

// Doublon 6 : Get cart
@GetMapping("/cart/{cartId}")
public Cart getCart(@PathVariable Long cartId) { ... }

@GetMapping("/carts/{cartId}")  // ← SUPPRIMER CE DOUBLON (inconsistance cart vs carts)
public Object getCartById(@PathVariable Long cartId) { ... }

// ✅ Garder UNIQUEMENT les versions RESTful standard
@GetMapping("/{id}")
public ResponseEntity<Product> getProduct(@PathVariable Long id) { ... }

@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteProduct(@PathVariable Long id) { ... }

@PutMapping("/{id}")
public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) { ... }

@PostMapping("/carts/{cartId}/items")
public ResponseEntity<Cart> addToCart(...) { ... }

@PutMapping("/{orderId}/status")
public ResponseEntity<Order> updateOrderStatus(...) { ... }

@GetMapping("/carts/{cartId}")
public ResponseEntity<Cart> getCart(@PathVariable Long cartId) { ... }
```

**Endpoints à supprimer** :

| À Supprimer | Garder |
|-------------|--------|
| `GET /products/get/{id}` | `GET /products/{id}` |
| `DELETE /products/remove/{id}` | `DELETE /products/{id}` |
| `POST /products/{id}/update` | `PUT /products/{id}` |
| `POST /orders/cart/{cartId}/add` | `POST /orders/carts/{cartId}/items` |
| `POST /orders/{id}/status` | `PUT /orders/{id}/status` |
| `GET /orders/cart/{cartId}` | `GET /orders/carts/{cartId}` |

**Critères de succès** :
- [ ] 6 endpoints doublons supprimés
- [ ] Documentation mise à jour
- [ ] Tests pointent vers les bons endpoints
- [ ] Plus d'ambiguïté dans l'API

---

### 5. Créer un GlobalExceptionHandler pour gestion centralisée des erreurs

**Catégorie impactée** : Error Handling, Security  
**Gain estimé** : +2 points  
**Difficulté** : 🟡 Moyenne  
**Effort estimé** : 4 heures  
**Priorité** : 🔴 Critique

**Description du problème** :
Actuellement, chaque endpoint gère (ou ne gère pas) ses propres erreurs. Les exceptions non catchées exposent des stack traces. Pas de format d'erreur standardisé. Codes HTTP incorrects pour les erreurs.

**Fichiers à créer** :
- [`📝 À créer : GlobalExceptionHandler.java`] - Gestion centralisée
- [`📝 À créer : ErrorResponse.java`] - Format d'erreur standardisé
- [`📝 À créer : exceptions/ProductNotFoundException.java`] - Exception custom
- [`📝 À créer : exceptions/OrderNotFoundException.java`] - Exception custom
- [`📝 À créer : exceptions/InsufficientStockException.java`] - Exception métier

**Solution proposée** :
Créer un @RestControllerAdvice qui intercepte toutes les exceptions et retourne des réponses JSON standardisées avec les codes HTTP appropriés.

**Exemple de code** :

```java
// ✅ ErrorResponse.java - Format d'erreur standardisé
package com.ecommerce.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponse {
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private String path;
    private Map<String, String> errors;  // Pour erreurs de validation
    
    public ErrorResponse(String message, int status, String path) {
        this.message = message;
        this.status = status;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
    
    // Constructor pour erreurs de validation
    public ErrorResponse(String message, int status, String path, Map<String, String> errors) {
        this(message, status, path);
        this.errors = errors;
    }
    
    // Getters/Setters
}

// ✅ ProductNotFoundException.java - Exception custom
package com.ecommerce.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long id) {
        super("Product not found with id: " + id);
    }
}

// ✅ InsufficientStockException.java - Exception métier
package com.ecommerce.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(Long productId, int requested, int available) {
        super(String.format("Insufficient stock for product %d. Requested: %d, Available: %d",
            productId, requested, available));
    }
}

// ✅ GlobalExceptionHandler.java - Gestion centralisée
package com.ecommerce.exception;

import com.ecommerce.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    // Gère ProductNotFoundException → 404
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(
            ProductNotFoundException ex, WebRequest request) {
        
        log.warn("Product not found: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    // Gère OrderNotFoundException → 404
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(
            OrderNotFoundException ex, WebRequest request) {
        
        log.warn("Order not found: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    // Gère erreurs de validation → 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("Validation failed: {}", errors);
        
        ErrorResponse error = new ErrorResponse(
            "Validation failed",
            HttpStatus.BAD_REQUEST.value(),
            request.getDescription(false).replace("uri=", ""),
            errors
        );
        
        return ResponseEntity.badRequest().body(error);
    }
    
    // Gère exceptions métier → 422
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(
            InsufficientStockException ex, WebRequest request) {
        
        log.warn("Insufficient stock: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }
    
    // Gère IllegalArgumentException → 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        
        log.warn("Invalid argument: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.badRequest().body(error);
    }
    
    // Gère toutes les autres exceptions → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        
        log.error("Unexpected error occurred", ex);
        
        // NE PAS exposer le message d'exception en production
        ErrorResponse error = new ErrorResponse(
            "An internal server error occurred",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

// ✅ Utilisation dans les contrôleurs
@GetMapping("/{id}")
public ResponseEntity<Product> getProduct(@PathVariable Long id) {
    Product product = repository.findById(id);
    
    if (product == null) {
        throw new ProductNotFoundException(id);  // ← Intercepté par le handler → 404
    }
    
    return ResponseEntity.ok(product);
}

@PostMapping("/{cartId}/items")
public ResponseEntity<Cart> addToCart(
        @PathVariable Long cartId,
        @RequestParam Long productId,
        @RequestParam int quantity) {
    
    Product product = productRepository.findById(productId);
    if (product == null) {
        throw new ProductNotFoundException(productId);  // → 404
    }
    
    if (product.getStock() < quantity) {
        throw new InsufficientStockException(productId, quantity, product.getStock());  // → 422
    }
    
    Cart cart = orderService.addToCart(cartId, productId, quantity);
    return ResponseEntity.ok(cart);
}
```

**Réponses JSON générées** :

```json
// 404 Not Found
{
  "message": "Product not found with id: 123",
  "status": 404,
  "timestamp": "2025-12-18T10:30:45",
  "path": "/api/products/123"
}

// 400 Bad Request (validation)
{
  "message": "Validation failed",
  "status": 400,
  "timestamp": "2025-12-18T10:31:20",
  "path": "/api/products",
  "errors": {
    "name": "Product name is required",
    "price": "Price must be at least 0.01"
  }
}

// 422 Unprocessable Entity (logique métier)
{
  "message": "Insufficient stock for product 5. Requested: 10, Available: 3",
  "status": 422,
  "timestamp": "2025-12-18T10:32:10",
  "path": "/api/carts/1/items"
}
```

**Critères de succès** :
- [ ] GlobalExceptionHandler créé avec @RestControllerAdvice
- [ ] Tous les types d'erreur retournent JSON structuré
- [ ] Codes HTTP appropriés (404, 400, 422, 500)
- [ ] Plus de stack traces exposées
- [ ] Logs structurés pour toutes les erreurs
- [ ] Tests pour chaque type d'erreur

---

## 🟠 ACTIONS HAUTE PRIORITÉ

> **Définition** : Important pour la production, améliore significativement la qualité.  
> **Délai recommandé** : 1-2 semaines

---

### 6. Aligner la documentation README avec le code réel

**Catégorie impactée** : Documentation  
**Gain estimé** : +2 points  
**Difficulté** : 🟢 Facile  
**Effort estimé** : 3 heures  
**Priorité** : 🟠 Haute

**Description du problème** :
Le README contient de nombreuses informations incorrectes qui ne correspondent pas au code réel : versions, endpoints, technologies, features. Cela induit en erreur et rend l'API difficile à utiliser.

**Fichiers à modifier** :
- [`README.md`](README.md)

**Solution proposée** :
Réviser complètement le README pour correspondre au code actuel et supprimer toutes les mentions de features non implémentées.

**Divergences à corriger** :

| README (Faux) | Code Réel (Vrai) |
|---------------|------------------|
| Spring Boot 1.5.9 | Spring Boot 2.7.18 |
| Java 8 | Java 11 |
| `/api/v1/products` | `/api/products` (pas de v1) |
| JWT authentication | Aucune authentification |
| MySQL, Redis, RabbitMQ, Elasticsearch | Stockage en mémoire uniquement |
| HATEOAS support | Aucun HATEOAS |
| Pagination `?page=0&size=20` | Pas de pagination implémentée |
| Docker, docker-compose | Aucun fichier Docker |
| `/actuator/health` | Actuator pas dans pom.xml |
| Prometheus metrics | Aucune métrique |

**Sections à réécrire** :

```markdown
# ✅ README corrigé

## Technology Stack (RÉEL)

- **Java 11**
- **Spring Boot 2.7.18**
- **Spring Web** (REST)
- **Maven 3.x**
- **In-memory storage** (no database)

## Current Limitations (MVP Status)

⚠️ This is a **MVP/Prototype** with the following limitations:

- **No authentication** - All endpoints are public
- **No database** - Data stored in memory (lost on restart)
- **No pagination** - All lists return complete results
- **No validation** - Input data not validated
- **Basic error handling** - Needs improvement
- **No deployment config** - Local development only

## API Endpoints (RÉELS)

### Products

- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/category/{category}` - Get products by category
- `GET /api/products/search?query=...` - Search products
- `POST /api/products` - Create product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### Carts

- `POST /api/orders/cart?userId=...` - Create cart
- `GET /api/orders/carts/{cartId}` - Get cart
- `POST /api/orders/carts/{cartId}/items?productId=...&quantity=...` - Add item
- `DELETE /api/orders/carts/{cartId}/items/{productId}` - Remove item

### Orders

- `POST /api/orders` - Create order
- `GET /api/orders/{orderId}` - Get order
- `GET /api/orders/user/{userId}` - Get user orders
- `PUT /api/orders/{orderId}/status?status=...` - Update status
- `POST /api/orders/{orderId}/cancel` - Cancel order

## Installation (SIMPLIFIÉ)

```bash
# Clone repository
git clone ...

# Build
mvn clean install

# Run
mvn spring-boot:run

# App starts on http://localhost:8080
```

## Next Steps for Production

Before deploying to production, the following must be implemented:

1. 🔴 **Add authentication/authorization** (Spring Security + JWT)
2. 🔴 **Add database** (H2 for dev, PostgreSQL for prod)
3. 🔴 **Implement validation** (Bean Validation)
4. 🔴 **Fix error handling** (Global exception handler)
5. 🟠 **Add pagination** on list endpoints
6. 🟠 **Add tests** (unit + integration)
7. 🟠 **Add health checks** (Spring Actuator)
```

**Critères de succès** :
- [ ] README ne contient plus d'informations fausses
- [ ] Endpoints documentés correspondent au code
- [ ] Technologies mentionnées sont réellement utilisées
- [ ] Limitations MVP clairement documentées
- [ ] Prochaines étapes vers production listées

---

### 7. Remplacer POST par PUT/PATCH pour les updates

**Catégorie impactée** : HTTP Methods, URL Structure  
**Gain estimé** : +1 point  
**Difficulté** : 🟢 Facile  
**Effort estimé** : 2 heures  
**Priorité** : 🟠 Haute

**Description du problème** :
Plusieurs endpoints utilisent POST pour faire des updates de ressources existantes au lieu d'utiliser PUT (update complet) ou PATCH (update partiel).

**Fichiers à modifier** :
- [`ProductController.java`](src/main/java/com/ecommerce/controller/ProductController.java)
- [`OrderController.java`](src/main/java/com/ecommerce/controller/OrderController.java)

**Solution proposée** :
1. Remplacer `POST /products/{id}/stock` par `PATCH /products/{id}` avec body `{"stock": 100}`
2. Supprimer `POST /products/{id}/addStock`, utiliser PATCH à la place
3. Supprimer `POST /products/{id}/update`, déjà un PUT existe

**Exemple de code** :

```java
// ❌ Code actuel (POST pour update)
@PostMapping("/{id}/stock")
public Product updateStock(@PathVariable Long id, @RequestParam int quantity) {
    Product product = repository.findById(id);
    if (product != null) {
        product.setStock(quantity);
        return repository.save(product);
    }
    return null;
}

@PostMapping("/{id}/addStock")
public void addStock(@PathVariable Long id, @RequestParam int qty) {
    Product product = repository.findById(id);
    if (product == null) {
        throw new RuntimeException("Product not found");
    }
    product.stock = product.stock + qty;
    repository.save(product);
}

// ✅ Code corrigé avec PATCH
@PatchMapping("/{id}")
public ResponseEntity<Product> patchProduct(
        @PathVariable Long id,
        @RequestBody ProductPatchRequest patch) {
    
    Product product = repository.findById(id);
    if (product == null) {
        throw new ProductNotFoundException(id);
    }
    
    // Appliquer uniquement les champs fournis
    if (patch.getStock() != null) {
        product.setStock(patch.getStock());
    }
    
    if (patch.getPrice() != null) {
        product.setPrice(patch.getPrice());
    }
    
    if (patch.getName() != null) {
        product.setName(patch.getName());
    }
    
    Product updated = repository.save(product);
    return ResponseEntity.ok(updated);
}

// DTO pour PATCH
public class ProductPatchRequest {
    private String name;
    private Double price;
    private Integer stock;
    
    // Getters/Setters (tous optionnels)
}

// Exemples d'utilisation
// PATCH /api/products/5
// { "stock": 100 }  ← Update seulement le stock

// PATCH /api/products/5
// { "price": 299.99, "stock": 50 }  ← Update prix et stock
```

**Différence PUT vs PATCH** :

```java
// PUT - Remplace la ressource ENTIÈRE
@PutMapping("/{id}")
public ResponseEntity<Product> updateProduct(
        @PathVariable Long id,
        @Valid @RequestBody Product product) {
    
    // Tous les champs sont requis
    // Remplace complètement la ressource
    Product existing = repository.findById(id);
    if (existing == null) {
        throw new ProductNotFoundException(id);
    }
    
    product.setId(id);
    Product updated = repository.save(product);
    return ResponseEntity.ok(updated);
}

// PATCH - Update PARTIEL de la ressource
@PatchMapping("/{id}")
public ResponseEntity<Product> patchProduct(
        @PathVariable Long id,
        @RequestBody ProductPatchRequest patch) {
    
    // Seulement les champs fournis sont modifiés
    // Les autres restent inchangés
    Product product = repository.findById(id);
    if (product == null) {
        throw new ProductNotFoundException(id);
    }
    
    // Apply only provided fields
    applyPatch(product, patch);
    
    Product updated = repository.save(product);
    return ResponseEntity.ok(updated);
}
```

**Critères de succès** :
- [ ] POST /products/{id}/stock supprimé
- [ ] POST /products/{id}/addStock supprimé
- [ ] PATCH /products/{id} implémenté
- [ ] PUT vs PATCH clairement différenciés
- [ ] Documentation mise à jour
- [ ] Tests ajoutés pour PATCH

---

### 8. Implémenter pagination sur les endpoints de liste

**Catégorie impactée** : Pagination  
**Gain estimé** : +2.5 points  
**Difficulté** : 🟡 Moyenne  
**Effort estimé** : 1 jour  
**Priorité** : 🟠 Haute

**Description du problème** :
Aucune pagination n'existe. Tous les endpoints retournent la liste complète des résultats, ce qui peut poser problème avec de grandes quantités de données.

**Fichiers à modifier** :
- [`ProductController.java`](src/main/java/com/ecommerce/controller/ProductController.java)
- [`OrderController.java`](src/main/java/com/ecommerce/controller/OrderController.java)
- [`ProductRepository.java`](src/main/java/com/ecommerce/repository/ProductRepository.java)

**Solution proposée** :
Ajouter support pagination avec query params `?page=0&size=20&sort=field,direction`

**Exemple de code** :

```java
// ❌ Code actuel (pas de pagination)
@GetMapping
public List<Product> getAllProducts() {
    return repository.findAll();  // ← Retourne TOUT
}

@GetMapping("/search")
public List<Product> search(@RequestParam(required = false) String query) {
    // Retourne tous les résultats
}

// ✅ Code corrigé avec pagination
@GetMapping
public ResponseEntity<Page<Product>> getAllProducts(
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        @RequestParam(defaultValue = "id,asc") String[] sort) {
    
    // Parse sort parameter
    Sort.Order order = new Sort.Order(
        Sort.Direction.fromString(sort[1]),
        sort[0]
    );
    
    Pageable pageable = PageRequest.of(page, size, Sort.by(order));
    Page<Product> productsPage = repository.findAll(pageable);
    
    return ResponseEntity.ok(productsPage);
}

@GetMapping("/search")
public ResponseEntity<Page<Product>> search(
        @RequestParam @NotBlank String query,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<Product> results = repository.searchByQuery(query, pageable);
    
    return ResponseEntity.ok(results);
}

@GetMapping("/user/{userId}")
public ResponseEntity<Page<Order>> getUserOrders(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        @RequestParam(defaultValue = "orderDate,desc") String[] sort) {
    
    Sort.Order order = new Sort.Order(
        Sort.Direction.fromString(sort[1]),
        sort[0]
    );
    
    Pageable pageable = PageRequest.of(page, size, Sort.by(order));
    Page<Order> orders = orderService.getOrdersByUser(userId, pageable);
    
    return ResponseEntity.ok(orders);
}

// ✅ Repository avec pagination (si utilisation Spring Data)
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Page<Product> findAll(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Product> searchByQuery(@Param("query") String query, Pageable pageable);
}
```

**Réponse JSON avec pagination** :

```json
{
  "content": [
    {
      "id": 1,
      "name": "Laptop",
      "price": 999.99,
      "stock": 10
    },
    {
      "id": 2,
      "name": "Mouse",
      "price": 29.99,
      "stock": 50
    }
    // ... 18 autres produits (total 20 par page)
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "pageNumber": 0,
    "pageSize": 20,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 5,
  "totalElements": 95,
  "last": false,
  "first": true,
  "size": 20,
  "number": 0,
  "numberOfElements": 20,
  "empty": false
}
```

**Exemples de requêtes** :

```bash
# Page 1 (0-indexé), 20 items par page, trié par nom croissant
GET /api/products?page=0&size=20&sort=name,asc

# Page 2, 50 items, trié par prix décroissant
GET /api/products?page=1&size=50&sort=price,desc

# Search avec pagination
GET /api/products/search?query=laptop&page=0&size=10

# User orders avec pagination et tri
GET /api/orders/user/123?page=0&size=10&sort=orderDate,desc
```

**Critères de succès** :
- [ ] Pagination sur GET /products
- [ ] Pagination sur GET /products/search
- [ ] Pagination sur GET /orders/user/{userId}
- [ ] Query params validés (@Min, @Max)
- [ ] Limite max 100 items par page
- [ ] Tri supporté via query param
- [ ] Documentation mise à jour avec exemples

---

### 9. Remplacer System.out.println par un logger approprié

**Catégorie impactée** : Infrastructure, Maintenance  
**Gain estimé** : +0.5 point  
**Difficulté** : 🟢 Facile  
**Effort estimé** : 2 heures  
**Priorité** : 🟠 Haute

**Description du problème** :
Le code utilise `System.out.println` partout pour logger, ce qui est une mauvaise pratique. Pas de niveaux de log, pas de structure, pas de configuration possible.

**Fichiers à modifier** :
- [`ProductController.java`](src/main/java/com/ecommerce/controller/ProductController.java)
- [`OrderController.java`](src/main/java/com/ecommerce/controller/OrderController.java)
- [`OrderService.java`](src/main/java/com/ecommerce/service/OrderService.java)
- [`Product.java`](src/main/java/com/ecommerce/model/Product.java)
- [`Cart.java`](src/main/java/com/ecommerce/model/Cart.java)
- [`Order.java`](src/main/java/com/ecommerce/model/Order.java)

**Solution proposée** :
Remplacer tous les `System.out.println` par SLF4J Logger avec niveaux appropriés (INFO, DEBUG, WARN, ERROR).

**Exemple de code** :

```java
// ❌ Code actuel (System.out.println)
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @GetMapping
    public List<Product> getAllProducts() {
        requestCount++;
        System.out.println("Getting all products - count: " + requestCount);
        return repository.findAll();
    }
    
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        repository.delete(id);
        System.out.println("Deleted product: " + id);
    }
}

// ✅ Code corrigé avec Logger
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        log.info("Fetching all products. Request count: {}", requestCount);
        List<Product> products = repository.findAll();
        log.debug("Returned {} products", products.size());
        return ResponseEntity.ok(products);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("Deleting product with id: {}", id);
        
        Product product = repository.findById(id);
        if (product == null) {
            log.warn("Attempted to delete non-existent product: {}", id);
            throw new ProductNotFoundException(id);
        }
        
        repository.delete(id);
        log.info("Successfully deleted product: {}", id);
        return ResponseEntity.noContent().build();
    }
}

// OrderService avec logger
public class OrderService {
    
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    
    public Cart createCart(Long userId) {
        if (userId == null) {
            log.warn("Attempted to create cart with null userId");
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        Cart cart = new Cart(cartIdCounter++, userId);
        carts.put(cart.id, cart);
        
        log.info("Created cart {} for user {}", cart.getId(), userId);
        log.debug("Total carts in memory: {}", carts.size());
        
        return cart;
    }
    
    public Order createOrder(Long userId, Long cartId, String shippingAddress) {
        log.info("Creating order for user {} with cart {}", userId, cartId);
        
        Cart cart = carts.get(cartId);
        if (cart == null) {
            log.error("Order creation failed: Cart {} not found", cartId);
            throw new CartNotFoundException(cartId);
        }
        
        if (cart.items.isEmpty()) {
            log.warn("Order creation failed: Cart {} is empty", cartId);
            throw new EmptyCartException(cartId);
        }
        
        Order order = new Order(userId, cart, shippingAddress);
        order.id = orderIdCounter++;
        orders.put(order.id, order);
        
        log.info("Successfully created order {} with total amount {}", order.getId(), order.getTotalAmount());
        
        return order;
    }
}
```

**Configuration logging** :

```properties
# application.properties

# Niveaux de log
logging.level.root=INFO
logging.level.com.ecommerce=DEBUG
logging.level.org.springframework.web=INFO

# Pattern console
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n

# Fichier log (optionnel)
logging.file.name=logs/ecommerce-api.log
logging.file.max-size=10MB
logging.file.max-history=30
```

**Niveaux de log à utiliser** :

| Niveau | Usage | Exemple |
|--------|-------|---------|
| ERROR | Erreurs critiques | "Failed to process order payment" |
| WARN | Situations anormales mais gérables | "Product stock is low", "Attempted to access non-existent resource" |
| INFO | Événements importants | "Order created", "Product deleted", "User logged in" |
| DEBUG | Détails pour debugging | "Returned 25 products", "Cart contains 3 items" |
| TRACE | Détails très fins (rarement utilisé) | "Entering method X with params Y" |

**Critères de succès** :
- [ ] Tous les System.out.println remplacés
- [ ] Logger créé dans chaque classe qui log
- [ ] Niveaux appropriés (INFO, DEBUG, WARN, ERROR)
- [ ] Logging configuré dans application.properties
- [ ] Messages structurés avec paramètres `{}` (pas de concat)
- [ ] Aucun System.out ou System.err restant

---

## 🟡 ACTIONS PRIORITÉ MOYENNE

> **Définition** : Améliore l'expérience développeur et la maintenabilité.  
> **Délai recommandé** : 1 mois

---

### 10. Ajouter Spring Boot Actuator pour health checks

**Catégorie impactée** : Infrastructure, Monitoring  
**Gain estimé** : +0.5 point  
**Difficulté** : 🟢 Facile  
**Effort estimé** : 1 heure  
**Priorité** : 🟡 Moyenne

**Description du problème** :
Impossible de vérifier si l'application est opérationnelle. Pas de health check endpoint, pas de métriques, pas de monitoring.

**Fichiers à modifier** :
- [`pom.xml`](pom.xml)
- [`application.properties`](src/main/resources/application.properties)

**Solution proposée** :
Ajouter Spring Boot Actuator et exposer les endpoints health, info, metrics.

**Exemple de code** :

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```properties
# application.properties

# Actuator endpoints
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always

# Info
info.app.name=Legacy E-commerce API
info.app.version=1.0.0
info.app.description=E-commerce REST API MVP
info.app.encoding=@project.build.sourceEncoding@
info.app.java.version=@java.version@
```

**Endpoints disponibles** :

```bash
# Health check
GET http://localhost:8080/actuator/health

# Response:
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 250000000000,
        "threshold": 10485760
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}

# Application info
GET http://localhost:8080/actuator/info

# Response:
{
  "app": {
    "name": "Legacy E-commerce API",
    "version": "1.0.0",
    "description": "E-commerce REST API MVP",
    "encoding": "UTF-8",
    "java": {
      "version": "11.0.12"
    }
  }
}

# Metrics
GET http://localhost:8080/actuator/metrics
```

**Critères de succès** :
- [ ] Actuator ajouté dans pom.xml
- [ ] /actuator/health retourne 200 UP
- [ ] /actuator/info retourne infos application
- [ ] Documentation mise à jour

---

### 11. Implémenter filtres avancés dans /products/search

**Catégorie impactée** : Query Parameters  
**Gain estimé** : +1 point  
**Difficulté** : 🟡 Moyenne  
**Effort estimé** : 4 heures  
**Priorité** : 🟡 Moyenne

**Description du problème** :
Le search n'accepte que `?query=...` alors que le README documente des filtres avancés (category, minPrice, maxPrice).

**Fichiers à modifier** :
- [`ProductController.java`](src/main/java/com/ecommerce/controller/ProductController.java)
- [`ProductRepository.java`](src/main/java/com/ecommerce/repository/ProductRepository.java)

**Solution proposée** :
Implémenter tous les filtres documentés avec validation.

**Exemple de code** :

```java
// ❌ Code actuel (query seulement)
@GetMapping("/search")
public List<Product> search(@RequestParam(required = false) String query) {
    // ...
}

// ✅ Code corrigé avec filtres multiples
@GetMapping("/search")
public ResponseEntity<Page<Product>> search(
        @RequestParam(required = false) @Size(min = 2, max = 100) String query,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) @Min(0) Double minPrice,
        @RequestParam(required = false) @Min(0) Double maxPrice,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        @RequestParam(defaultValue = "name,asc") String[] sort) {
    
    // Validation logique métier
    if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
        throw new IllegalArgumentException("minPrice cannot be greater than maxPrice");
    }
    
    // Créer critères de recherche
    ProductSearchCriteria criteria = ProductSearchCriteria.builder()
        .query(query)
        .category(category)
        .minPrice(minPrice)
        .maxPrice(maxPrice)
        .build();
    
    Sort.Order order = new Sort.Order(
        Sort.Direction.fromString(sort[1]),
        sort[0]
    );
    Pageable pageable = PageRequest.of(page, size, Sort.by(order));
    
    Page<Product> results = repository.search(criteria, pageable);
    
    log.info("Search executed: query='{}', category='{}', priceRange=[{}, {}], results={}",
        query, category, minPrice, maxPrice, results.getTotalElements());
    
    return ResponseEntity.ok(results);
}

// DTO critères de recherche
@Builder
public class ProductSearchCriteria {
    private String query;
    private String category;
    private Double minPrice;
    private Double maxPrice;
    
    // Getters
}

// Repository avec recherche avancée
public class ProductRepository {
    
    public Page<Product> search(ProductSearchCriteria criteria, Pageable pageable) {
        List<Product> allProducts = findAll();
        
        // Filtrer selon critères
        List<Product> filtered = allProducts.stream()
            .filter(p -> matchesQuery(p, criteria.getQuery()))
            .filter(p -> matchesCategory(p, criteria.getCategory()))
            .filter(p -> matchesPriceRange(p, criteria.getMinPrice(), criteria.getMaxPrice()))
            .collect(Collectors.toList());
        
        // Appliquer tri (simplifié pour mémoire, mieux avec Spring Data)
        // Appliquer pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        
        List<Product> pageContent = filtered.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, filtered.size());
    }
    
    private boolean matchesQuery(Product p, String query) {
        if (query == null || query.isBlank()) return true;
        String q = query.toLowerCase();
        return p.getName().toLowerCase().contains(q) ||
               (p.getDescription() != null && p.getDescription().toLowerCase().contains(q));
    }
    
    private boolean matchesCategory(Product p, String category) {
        if (category == null || category.isBlank()) return true;
        return category.equalsIgnoreCase(p.getCategory());
    }
    
    private boolean matchesPriceRange(Product p, Double minPrice, Double maxPrice) {
        if (minPrice != null && p.getPrice() < minPrice) return false;
        if (maxPrice != null && p.getPrice() > maxPrice) return false;
        return true;
    }
}
```

**Exemples de requêtes** :

```bash
# Search par nom
GET /api/products/search?query=laptop

# Search par catégorie
GET /api/products/search?category=Electronics

# Search par fourchette de prix
GET /api/products/search?minPrice=100&maxPrice=500

# Search combiné
GET /api/products/search?query=laptop&category=Electronics&minPrice=500&maxPrice=1500&page=0&size=10&sort=price,asc
```

**Critères de succès** :
- [ ] Filtres query, category, minPrice, maxPrice implémentés
- [ ] Validation des paramètres
- [ ] Filtres combinables
- [ ] Pagination et tri fonctionnels
- [ ] Documentation mise à jour

---

### 12. Restructurer les carts comme ressource de premier niveau

**Catégorie impactée** : URL Structure  
**Gain estimé** : +0.5 point  
**Difficulté** : 🟡 Moyenne  
**Effort estimé** : 3 heures  
**Priorité** : 🟡 Moyenne

**Description du problème** :
Les carts sont actuellement sous `/api/orders/cart` ce qui est illogique. Les carts ne sont pas des orders.

**Fichiers à modifier** :
- [`📝 À créer : CartController.java`] - Nouveau contrôleur
- [`OrderController.java`](src/main/java/com/ecommerce/controller/OrderController.java) - Supprimer endpoints cart

**Solution proposée** :
Créer `/api/carts` comme ressource de premier niveau.

**Exemple de code** :

```java
// ✅ Nouveau CartController.java
package com.ecommerce.controller;

import com.ecommerce.model.Cart;
import com.ecommerce.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
public class CartController {
    
    private static final Logger log = LoggerFactory.getLogger(CartController.class);
    private final CartService cartService;
    
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }
    
    @PostMapping
    public ResponseEntity<Cart> createCart(@RequestParam Long userId) {
        log.info("Creating cart for user {}", userId);
        Cart cart = cartService.createCart(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }
    
    @GetMapping("/{cartId}")
    public ResponseEntity<Cart> getCart(@PathVariable Long cartId) {
        Cart cart = cartService.getCart(cartId);
        if (cart == null) {
            throw new CartNotFoundException(cartId);
        }
        return ResponseEntity.ok(cart);
    }
    
    @PostMapping("/{cartId}/items")
    public ResponseEntity<Cart> addItem(
            @PathVariable Long cartId,
            @RequestParam Long productId,
            @RequestParam @Min(1) int quantity) {
        
        log.info("Adding {} x product {} to cart {}", quantity, productId, cartId);
        Cart cart = cartService.addToCart(cartId, productId, quantity);
        return ResponseEntity.ok(cart);
    }
    
    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<Cart> removeItem(
            @PathVariable Long cartId,
            @PathVariable Long productId) {
        
        log.info("Removing product {} from cart {}", productId, cartId);
        Cart cart = cartService.removeFromCart(cartId, productId);
        return ResponseEntity.ok(cart);
    }
    
    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> deleteCart(@PathVariable Long cartId) {
        log.info("Deleting cart {}", cartId);
        cartService.deleteCart(cartId);
        return ResponseEntity.noContent().build();
    }
}
```

**Nouvelles URLs** :

| Ancien | Nouveau |
|--------|---------|
| `POST /api/orders/cart` | `POST /api/carts?userId=...` |
| `GET /api/orders/cart/{id}` | `GET /api/carts/{id}` |
| `POST /api/orders/cart/{id}/items` | `POST /api/carts/{id}/items` |
| `DELETE /api/orders/cart/{id}/items/{pid}` | `DELETE /api/carts/{id}/items/{pid}` |

**Critères de succès** :
- [ ] CartController créé
- [ ] Endpoints cart supprimés de OrderController
- [ ] URLs logiques `/api/carts`
- [ ] Documentation mise à jour
- [ ] Tests ajoutés

---

## 🟢 ACTIONS PRIORITÉ BASSE

> **Définition** : Nice to have, optimisations, perfectionnement.  
> **Délai recommandé** : Quand temps disponible

---

### 13. Ajouter Swagger/OpenAPI pour documentation interactive

**Catégorie impactée** : Documentation  
**Gain estimé** : +1 point  
**Difficulté** : 🟢 Facile  
**Effort estimé** : 2 heures  
**Priorité** : 🟢 Basse

**Fichiers à modifier** :
- [`pom.xml`](pom.xml)
- [`📝 À créer : OpenApiConfig.java`]

**Exemple de code** :

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
    <version>1.7.0</version>
</dependency>
```

```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI ecommerceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("E-commerce API")
                .description("API REST pour e-commerce MVP")
                .version("1.0.0")
                .contact(new Contact()
                    .name("E-commerce Team")
                    .email("support@ecommerce.com")));
    }
}
```

Accessible à : `http://localhost:8080/swagger-ui.html`

---

### 14. Encapsuler les champs publics des models

**Catégorie impactée** : Code Quality, Security  
**Gain estimé** : +0 point (bonne pratique)  
**Difficulté** : 🟢 Facile  
**Effort estimé** : 2 heures  
**Priorité** : 🟢 Basse

**Fichiers à modifier** :
- [`Product.java`](src/main/java/com/ecommerce/model/Product.java)
- [`Order.java`](src/main/java/com/ecommerce/model/Order.java)
- [`Cart.java`](src/main/java/com/ecommerce/model/Cart.java)

**Exemple de code** :

```java
// ❌ Champs publics
public class Product {
    public Long id;
    public String name;
    public double price;
    public int stock;
}

// ✅ Champs privés avec getters/setters
public class Product {
    private Long id;
    private String name;
    private Double price;
    private Integer stock;
    
    // Getters/Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    // ...
}
```

---

## 🚀 Quick Wins (Ratio Gain/Effort Optimal)

Actions à impact maximum avec effort minimum :

| Action | Gain | Effort | Difficulté | Fichiers |
|--------|------|--------|------------|----------|
| **#4 - Supprimer doublons endpoints** | +1.0 | 1h | 🟢 | [`ProductController.java`](src/main/java/com/ecommerce/controller/ProductController.java), [`OrderController.java`](src/main/java/com/ecommerce/controller/OrderController.java) |
| **#1 - Corriger GET qui modifient état** | +1.5 | 2h | 🟢 | [`OrderController.java`](src/main/java/com/ecommerce/controller/OrderController.java) |
| **#7 - Remplacer POST par PUT/PATCH** | +1.0 | 2h | 🟢 | [`ProductController.java`](src/main/java/com/ecommerce/controller/ProductController.java) |
| **#9 - Logger au lieu de System.out** | +0.5 | 2h | 🟢 | Tous les contrôleurs |
| **#10 - Ajouter Actuator** | +0.5 | 1h | 🟢 | [`pom.xml`](pom.xml), [`application.properties`](src/main/resources/application.properties) |
| **#13 - Ajouter Swagger** | +1.0 | 2h | 🟢 | [`pom.xml`](pom.xml), [`OpenApiConfig.java`] |

**Gain total Quick Wins** : +5.5 points en ~10 heures

**Recommandation** : Commencer par ces actions pour un boost rapide du score de 25 → 30.5 en 2 jours.

---

## 📅 Roadmap d'Amélioration Suggérée

### Phase 1 : Fondations - Corriger les Problèmes Critiques (1 semaine)

**Objectif** : Rendre l'API minimalement viable et sécurisée  
**Score cible** : 40/100

**Actions** :
- [x] **#1** - Corriger GET qui modifient état (2h) - [`OrderController.java`](src/main/java/com/ecommerce/controller/OrderController.java)
- [x] **#3** - Utiliser ResponseEntity partout (4h) - Tous contrôleurs
- [x] **#4** - Supprimer doublons endpoints (1h) - [`ProductController.java`](src/main/java/com/ecommerce/controller/ProductController.java), [`OrderController.java`](src/main/java/com/ecommerce/controller/OrderController.java)
- [x] **#2** - Implémenter validation entrées (1 jour) - Models + contrôleurs
- [x] **#5** - Créer GlobalExceptionHandler (4h) - [`GlobalExceptionHandler.java`]
- [x] **#9** - Logger au lieu System.out (2h) - Tous fichiers

**Effort total** : ~3-4 jours (1 personne)

**Gain estimé** : +7 points → Score 32/100

---

### Phase 2 : Production Ready - Améliorer Qualité et Stabilité (2 semaines)

**Objectif** : Préparer pour déploiement production  
**Score cible** : 55/100

**Actions** :
- [x] **#6** - Aligner documentation (3h) - [`README.md`](README.md)
- [x] **#7** - PUT/PATCH au lieu POST (2h) - [`ProductController.java`](src/main/java/com/ecommerce/controller/ProductController.java)
- [x] **#8** - Implémenter pagination (1 jour) - Contrôleurs + repository
- [x] **#10** - Ajouter Actuator (1h) - [`pom.xml`](pom.xml)
- [x] **#11** - Filtres avancés search (4h) - [`ProductController.java`](src/main/java/com/ecommerce/controller/ProductController.java)
- [x] **#12** - Restructurer carts (3h) - [`CartController.java`]
- [ ] **Ajouter tests unitaires** (3 jours) - Tous contrôleurs
- [ ] **Ajouter authentification basique** (2 jours) - Spring Security

**Effort total** : ~2 semaines (1 personne)

**Gain estimé** : +15 points → Score 47/100

---

### Phase 3 : Excellence - Optimisations et Perfectionnement (1 mois)

**Objectif** : Atteindre standards production élevés  
**Score cible** : 65-70/100

**Actions** :
- [x] **#13** - Ajouter Swagger (2h)
- [x] **#14** - Encapsuler champs models (2h)
- [ ] **Ajouter vraie base de données** (H2/PostgreSQL) (3 jours)
- [ ] **Implémenter CORS** (2h)
- [ ] **Ajouter rate limiting** (1 jour)
- [ ] **Tests d'intégration** (5 jours)
- [ ] **CI/CD pipeline** (2 jours)
- [ ] **Documentation OpenAPI complète** (1 jour)

**Effort total** : ~3-4 semaines (1 personne)

**Gain estimé** : +20 points → Score 67/100

---

## 📊 Évolution du Score Projetée

| Phase | Score Actuel | Score Cible | Gain | Effort | Délai |
|-------|--------------|-------------|------|--------|-------|
| **Maintenant** | **25/100** | - | - | - | - |
| **Quick Wins** | 25 | 30.5 | +5.5 | 10h | 2 jours |
| **Phase 1 (Fondations)** | 25 | 40 | +15 | 3-4j | 1 semaine |
| **Phase 2 (Production Ready)** | 40 | 55 | +15 | 10j | 2-3 semaines |
| **Phase 3 (Excellence)** | 55 | 67 | +12 | 20j | 1-2 mois |

**Score visé réaliste pour MVP production** : **55-60/100** (après Phase 2)

---

## 🎯 Résumé des Priorités

### À faire IMMÉDIATEMENT (< 1 semaine) 🔴

1. Corriger GET qui modifient état
2. Implémenter validation des entrées
3. Utiliser ResponseEntity
4. Supprimer doublons
5. GlobalExceptionHandler

**Impact** : Passe de "Cassé" à "MVP acceptable"

### À faire rapidement (1-2 semaines) 🟠

6. Aligner documentation
7. PUT/PATCH corrects
8. Pagination
9. Logger approprié

**Impact** : Passe de "MVP" à "Production Ready"

### À faire quand prêt (1-2 mois) 🟡

10-14. Optimisations, Swagger, restructuration

**Impact** : Passe de "Production Ready" à "Qualité élevée"

---

*Plan d'action généré le 18/12/2025 par OpenCode AI Assistant*
*Basé sur analyse API score 25/100*
