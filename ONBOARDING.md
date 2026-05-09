# Order-Scene Onboarding Guide

## Project Overview

**Name:** order-scene  
**Description:** 电商运单场景-高并发、高可用、微服务、分布式、消息队列  
**Languages:** Java, XML, YAML, Markdown, Batch, JavaScript

**Frameworks:**
- Spring Boot 2.1.16
- MyBatis Plus
- Kafka
- Redis / Redisson
- Caffeine (local cache)

This is an e-commerce order logistics system designed for high concurrency and high availability, using a microservices architecture with distributed message queues.

---

## Architecture Layers

| Layer | Description | Key Files |
|-------|-------------|----------|
| **Entry Point** | Spring Boot bootstrap class | `LogisticsApplication.java` |
| **API Layer** | REST controllers | `OrderController.java` |
| **Business Layer** | Service interface + implementation | `OrderService.java`, `OrderServiceImpl.java` |
| **Data Access** | Entity models + MyBatis mappers | `Order.java`, `OrderTrack.java`, `Idempotent.java`, `OrderMapper.java` |
| **Messaging** | Kafka producer/consumer | `KafkaProducer.java`, `TrackConsumer.java` |
| **Cache** | Multi-layer caching (Redis + Redisson + Caffeine) | `CacheSyncListener.java`, `RedisConfig.java`, `RedissonConfig.java`, `CaffeineConfig.java` |
| **Configuration** | Spring bean configs | `KafkaConsumerConfig.java`, `KafkaProducerConfig.java`, `MyBatisConfig.java` |
| **Utilities** | Helper classes | `IdempotentUtil.java`, `LogUtil.java`, `BusinessException.java` |

---

## Key Concepts

### Idempotency
The system implements idempotency protection for order operations using the `IdempotentUtil.java` utility class and `Idempotent.java` entity. This ensures duplicate order requests are handled correctly.

### Multi-Layer Caching
Three caching layers work together:
1. **Caffeine** - Local in-memory cache (fastest)
2. **Redisson** - Distributed locks and cache
3. **Redis** - Distributed cache with pub/sub for cache synchronization

### Message Queue (Kafka)
Asynchronous order tracking through Kafka:
- `TrackConsumer.java` consumes order track messages
- `KafkaProducer.java` sends messages
- Supports idempotent consumption patterns

---

## Guided Tour

Follow this learning path:

### Step 1: Project Overview
Start with `README.md` and `pom.xml` to understand the build configuration.

### Step 2: Application Entry Point
`LogisticsApplication.java` is the Spring Boot entry point - this is where the application starts.

### Step 3: REST API Layer
`OrderController.java` exposes the order REST endpoints.

### Step 4: Business Logic
`OrderService.java` (interface) + `OrderServiceImpl.java` (implementation) contain core business logic.

### Step 5: Data Persistence
Entity models (`Order`, `OrderTrack`, `Idempotent`) and `OrderMapper.java` for database operations.

### Step 6: Message Queue
`KafkaProducer.java` and `TrackConsumer.java` handle async processing.

### Step 7: Caching Strategy
`RedisConfig.java`, `RedissonConfig.java`, `CaffeineConfig.java` for multi-layer caching.

### Step 8: Application Configuration
`application.yml` contains all Spring Boot configuration settings.

---

## File Map

### Controllers
- **OrderController.java** - REST API for order operations

### Services
- **OrderService.java** - Business service interface
- **OrderServiceImpl.java** - Business service implementation

### Entities
- **Order.java** - Order entity model
- **OrderTrack.java** - Order tracking entity
- **Idempotent.java** - Idempotency entity

### Data Access
- **OrderMapper.java** - MyBatis mapper interface
- **OrderMapper.xml** - MyBatis SQL mappings

### Message Queue
- **KafkaProducer.java** - Kafka message producer
- **TrackConsumer.java** - Kafka message consumer

### Cache
- **CacheSyncListener.java** - Redis pub/sub listener
- **LocalCache.java** - Local Caffeine cache
- **RedisConfig.java** - Redis configuration
- **RedissonConfig.java** - Redisson (distributed lock) configuration
- **CaffeineConfig.java** - Caffeine local cache configuration

### Utilities
- **IdempotentUtil.java** - Idempotency check utility
- **LogUtil.java** - Logging utility
- **RedisPubSubUtil.java** - Redis pub/sub utility
- **BusinessException.java** - Custom exception class

### Configuration
- **KafkaConsumerConfig.java** - Kafka consumer beans
- **KafkaProducerConfig.java** - Kafka producer beans
- **MyBatisConfig.java** - MyBatis configuration
- **application.yml** - Spring Boot application config
- **pom.xml** - Maven build config

---

## Complexity Hotspots

Approach these files with extra care:

1. **TrackConsumer.java** (197 lines) - Complex Kafka consumer logic with transaction management
2. **OrderServiceImpl.java** (90 lines) - Core business logic with multiple dependencies
3. **application.yml** (51 lines) - Many configuration settings

---

## Getting Started

1. **Build:** `mvn clean package`
2. **Run:** `mvn spring-boot:run`
3. **API:** The application exposes REST endpoints via Spring Web

---

*Generated from knowledge graph - commit: cf5cb2c*