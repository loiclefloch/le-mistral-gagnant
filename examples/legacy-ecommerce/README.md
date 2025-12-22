# Legacy E-commerce Application

> Last updated: March 2018

## Overview

This is a modern e-commerce platform built with Spring Boot 1.5.9 and Java 8. The application provides a robust REST API for managing products, shopping carts, and orders with full authentication and database persistence.

## Technology Stack

- **Java 8** (JDK 1.8.0_151 or higher)
- **Spring Boot 1.5.9** with Spring Security
- **MySQL 5.7** for data persistence
- **Redis** for caching and session management
- **RabbitMQ** for asynchronous order processing
- **Elasticsearch** for product search
- **Docker** for containerization

## Prerequisites

Make sure you have the following installed:
- Java 8 (JDK 1.8)
- Maven 3.3+
- MySQL 5.7
- Redis Server 3.2+
- RabbitMQ 3.6+
- Elasticsearch 5.6

## Database Setup

1. Install MySQL 5.7
2. Create database:
```sql
CREATE DATABASE ecommerce_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'ecommerce_user'@'localhost' IDENTIFIED BY 'ecommerce_pass';
GRANT ALL PRIVILEGES ON ecommerce_db.* TO 'ecommerce_user'@'localhost';
FLUSH PRIVILEGES;
```

3. The application will automatically create tables on startup using Hibernate DDL

## Configuration

Edit `application.properties` to configure your environment:

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db
spring.datasource.username=ecommerce_user
spring.datasource.password=ecommerce_pass

# Redis Configuration
spring.redis.host=localhost
spring.redis.port=6379

# RabbitMQ Configuration
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# Elasticsearch Configuration
spring.data.elasticsearch.cluster-name=ecommerce-cluster
spring.data.elasticsearch.cluster-nodes=localhost:9300

# Security
jwt.secret=mySecretKey123456789
jwt.expiration=86400000
```

## Installation & Running

### Using Maven

```bash
# Clone the repository
git clone https://github.com/company/ecommerce-api.git
cd ecommerce-api

# Install dependencies
mvn clean install

# Run the application
mvn spring-boot:run
```

### Using Docker

```bash
# Build Docker image
docker build -t ecommerce-api:latest .

# Run with docker-compose
docker-compose up -d
```

The application will start on `http://localhost:8080`

## API Documentation

### Authentication

All endpoints require JWT authentication. First, obtain a token:

```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400
}
```

Include the token in subsequent requests:
```
Authorization: Bearer <token>
```

### Product Endpoints

#### Get All Products
```bash
GET /api/v1/products
```

#### Get Product by ID
```bash
GET /api/v1/products/{id}
```

#### Search Products
```bash
GET /api/v1/products/search?q={query}&category={category}&minPrice={min}&maxPrice={max}
```

#### Create Product (Admin only)
```bash
POST /api/v1/products
Content-Type: application/json

{
  "name": "Product Name",
  "description": "Product Description",
  "price": 99.99,
  "category": "Electronics",
  "stock": 100,
  "imageUrl": "https://example.com/image.jpg",
  "tags": ["tag1", "tag2"]
}
```

#### Update Product (Admin only)
```bash
PUT /api/v1/products/{id}
```

#### Delete Product (Admin only)
```bash
DELETE /api/v1/products/{id}
```

### Cart Endpoints

#### Create Cart
```bash
POST /api/v1/carts
Content-Type: application/json

{
  "userId": 123
}
```

#### Get Cart
```bash
GET /api/v1/carts/{cartId}
```

#### Add Item to Cart
```bash
POST /api/v1/carts/{cartId}/items
Content-Type: application/json

{
  "productId": 456,
  "quantity": 2
}
```

#### Update Cart Item Quantity
```bash
PUT /api/v1/carts/{cartId}/items/{productId}
Content-Type: application/json

{
  "quantity": 3
}
```

#### Remove Item from Cart
```bash
DELETE /api/v1/carts/{cartId}/items/{productId}
```

#### Clear Cart
```bash
DELETE /api/v1/carts/{cartId}/clear
```

### Order Endpoints

#### Create Order
```bash
POST /api/v1/orders
Content-Type: application/json

{
  "userId": 123,
  "cartId": 789,
  "shippingAddress": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  },
  "billingAddress": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  },
  "paymentMethod": "CREDIT_CARD",
  "paymentDetails": {
    "cardNumber": "4111111111111111",
    "expiryDate": "12/25",
    "cvv": "123"
  }
}
```

#### Get Order by ID
```bash
GET /api/v1/orders/{orderId}
```

#### Get User Orders
```bash
GET /api/v1/orders/user/{userId}?page=0&size=20&sort=createdAt,desc
```

#### Update Order Status (Admin only)
```bash
PUT /api/v1/orders/{orderId}/status
Content-Type: application/json

{
  "status": "SHIPPED",
  "trackingNumber": "1Z999AA10123456784"
}
```

#### Cancel Order
```bash
POST /api/v1/orders/{orderId}/cancel
```

### User Endpoints

#### Register User
```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePassword123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Get User Profile
```bash
GET /api/v1/users/profile
```

#### Update User Profile
```bash
PUT /api/v1/users/profile
```

## Architecture

### Layers

```
┌─────────────────────────────────────────┐
│         Controllers (REST API)          │
├─────────────────────────────────────────┤
│         Services (Business Logic)       │
├─────────────────────────────────────────┤
│      Repositories (Data Access)         │
├─────────────────────────────────────────┤
│      MySQL Database + Redis Cache       │
└─────────────────────────────────────────┘
```

### Key Components

- **Controllers**: Handle HTTP requests and responses
- **Services**: Implement business logic and orchestrate operations
- **Repositories**: Interact with MySQL using Spring Data JPA
- **DTOs**: Data Transfer Objects for API contracts
- **Entities**: JPA entities mapped to database tables
- **Security**: JWT-based authentication with Spring Security
- **Cache**: Redis for caching frequently accessed data
- **Message Queue**: RabbitMQ for asynchronous order processing

## Features

- ✅ User authentication and authorization with JWT
- ✅ Product catalog management with categories and tags
- ✅ Advanced product search with Elasticsearch
- ✅ Shopping cart functionality with Redis persistence
- ✅ Order processing and management
- ✅ Payment gateway integration (Stripe)
- ✅ Email notifications for order updates
- ✅ Real-time inventory management
- ✅ Admin dashboard for analytics
- ✅ RESTful API with HATEOAS support
- ✅ API rate limiting
- ✅ Comprehensive logging and monitoring
- ✅ Unit and integration tests with 90% code coverage

## Testing

Run all tests:
```bash
mvn test
```

Run integration tests only:
```bash
mvn verify -P integration-tests
```

Generate coverage report:
```bash
mvn jacoco:report
```

Coverage report will be available at `target/site/jacoco/index.html`

## Performance Considerations

- Product catalog is cached in Redis with 1-hour TTL
- Database queries are optimized with proper indexes
- Connection pooling configured with HikariCP (min: 5, max: 20)
- Async processing for email notifications and order processing
- Elasticsearch for fast product search (< 50ms average)

## Monitoring

Application metrics are exposed at:
- Health check: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Info: `http://localhost:8080/actuator/info`

Prometheus metrics available at: `http://localhost:8080/actuator/prometheus`

## Deployment

### Production Deployment

1. Build production JAR:
```bash
mvn clean package -P production
```

2. Set environment variables:
```bash
export SPRING_PROFILES_ACTIVE=production
export DB_HOST=production-db.example.com
export DB_PORT=3306
export DB_NAME=ecommerce_prod
export DB_USER=prod_user
export DB_PASSWORD=prod_secure_password
export REDIS_HOST=production-redis.example.com
export JWT_SECRET=productionSecretKey
```

3. Run the application:
```bash
java -jar target/ecommerce-api-1.0.0.jar
```

### Docker Deployment

Use the provided `docker-compose.yml` for production deployment with all dependencies.

## Troubleshooting

### Common Issues

1. **Port 8080 already in use**: Change `server.port` in `application.properties`
2. **MySQL connection refused**: Ensure MySQL is running and credentials are correct
3. **Redis connection timeout**: Check Redis server status with `redis-cli ping`
4. **Elasticsearch cluster not available**: Verify Elasticsearch is running on port 9300

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For questions or issues, please contact:
- Email: support@ecommerce-api.com
- Slack: #ecommerce-api channel
- Internal Wiki: http://wiki.company.com/ecommerce-api

## Changelog

### Version 1.0.0 (Current)
- Initial release with full e-commerce functionality
- JWT authentication
- Product catalog with Elasticsearch
- Shopping cart with Redis
- Order management
- Payment gateway integration

### Version 0.9.0 (Beta)
- Basic product and order management
- User authentication
- Initial API implementation

---

**Note**: This application is actively maintained and regularly updated. Please refer to the internal documentation for the latest API changes and deployment procedures.
