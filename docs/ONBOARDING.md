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

An e-commerce order logistics system designed for high concurrency and high availability, using microservices architecture with distributed message queues.

---

## Architecture Layers

| Layer | Description | Key Files |
|-------|-------------|----------|
| **Entry Point** | Spring Boot bootstrap | `LogisticsApplication.java` |
| **API Layer** | REST controllers | `OrderController.java` |
| **Business Layer** | Service interface + implementation | `OrderService.java`, `OrderServiceImpl.java` |
| **Data Access** | Entity models + MyBatis mappers | `Order.java`, `OrderTrack.java`, `Idempotent.java`, `OrderMapper.java` |
| **Messaging** | Kafka producer/consumer | `KafkaProducer.java`, `TrackConsumer.java` |
| **Cache** | Multi-layer caching | `CacheSyncListener.java`, `RedisConfig.java`, `RedissonConfig.java`, `CaffeineConfig.java` |
| **Configuration** | Spring bean configs | `KafkaConsumerConfig.java`, `KafkaProducerConfig.java`, `MyBatisConfig.java` |
| **Utilities** | Helper classes | `IdempotentUtil.java`, `LogUtil.java`, `BusinessException.java` |

---

## Key Concepts

### Idempotency
The system implements idempotency protection using `IdempotentUtil.java` and `Idempotent.java` entity. Ensures duplicate order requests are handled correctly.

### Multi-Layer Caching
Three caching layers:
1. **Caffeine** - Local in-memory cache (fastest)
2. **Redisson** - Distributed locks and cache  
3. **Redis** - Distributed cache with pub/sub

### Message Queue (Kafka)
- `TrackConsumer.java` - consumes order track messages
- `KafkaProducer.java` - sends messages
- Supports idempotent consumption patterns

---

## Guided Tour

Follow this learning path:

### Step 1: Project Overview
Start with `README.md` and `pom.xml` for build configuration.

### Step 2: Application Entry Point
`LogisticsApplication.java` - Spring Boot entry point.

### Step 3: REST API Layer
`OrderController.java` - exposes order REST endpoints.

### Step 4: Business Logic
`OrderService.java` + `OrderServiceImpl.java` - core business logic.

### Step 5: Data Persistence
`Order.java`, `OrderTrack.java`, `Idempotent.java`, `OrderMapper.java`.

### Step 6: Message Queue
`KafkaProducer.java` and `TrackConsumer.java`.

### Step 7: Caching Strategy
`RedisConfig.java`, `RedissonConfig.java`, `CaffeineConfig.java`.

### Step 8: Application Configuration
`application.yml` - Spring Boot settings.

---

## File Map

### Controllers
- `OrderController.java` - REST API for order operations

### Services
- `OrderService.java` - Business service interface
- `OrderServiceImpl.java` - Business service implementation

### Entities
- `Order.java` - Order entity model
- `OrderTrack.java` - Order tracking entity
- `Idempotent.java` - Idempotency entity

### Data Access
- `OrderMapper.java` - MyBatis mapper interface
- `OrderMapper.xml` - MyBatis SQL mappings

### Message Queue
- `KafkaProducer.java` - Kafka message producer
- `TrackConsumer.java` - Kafka message consumer (197 lines - complex)

### Cache
- `CacheSyncListener.java` - Redis pub/sub listener
- `LocalCache.java` - Local Caffeine cache
- `RedisConfig.java` - Redis configuration
- `RedissonConfig.java` - Redisson config
- `CaffeineConfig.java` - Caffeine config

### Utilities
- `IdempotentUtil.java` - Idempotency utility
- `LogUtil.java` - Logging utility
- `RedisPubSubUtil.java` - Redis pub/sub utility
- `BusinessException.java` - Custom exception

### Configuration
- `KafkaConsumerConfig.java` - Kafka consumer beans
- `KafkaProducerConfig.java` - Kafka producer beans
- `MyBatisConfig.java` - MyBatis configuration
- `application.yml` - Spring Boot config
- `pom.xml` - Maven build config

---

## Complexity Hotspots

Files to approach with extra care (complex):

1. **TrackConsumer.java** (197 lines) - Complex Kafka consumer with transactions
2. **OrderServiceImpl.java** (90 lines) - Core business logic
3. **OrderController.java** - REST endpoint logic
4. **application.yml** - Many configuration settings

---

## Getting Started

1. **Build:** `mvn clean package`
2. **Run:** `mvn spring-boot:run`
3. **API:** REST endpoints via Spring Web

---

*Generated from knowledge graph - commit: cf5cb2c*
*Saved to: docs/ONBOARDING.md*