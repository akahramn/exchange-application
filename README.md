# 💱 Currency Exchange Application

A simple REST API built with **Spring Boot (Java 21)** that allows you to:
- Convert currency values
- Fetch current exchange rates
- View historical conversion records

Includes Docker support with Redis caching and H2 in-memory database.

---

## 🚀 Features

- 🔁 Real-time currency conversion
- 📊 Get exchange rates between currencies
- 🧾 Access conversion history (by ID or date)
- ⚡ Redis caching for improved performance
- 🐳 Dockerized deployment with `docker-compose`
- 📄 Swagger UI for API documentation

---

## 🛠️ Technologies Used

- Java 21 (Eclipse Temurin)
- Spring Boot 3.x
- Spring Web & Data JPA
- H2 In-Memory Database
- Redis (via Spring Cache)
- Swagger (Springdoc OpenAPI)
- Docker & Docker Compose

---

## 📦 Installation & Running

### 🚨 Prerequisites
- Java 21
- Docker & Docker Compose
- Maven (or use `./mvnw`)

### ▶️ Run with Docker

```bash
# Build the JAR
./mvnw clean package -DskipTests

# Start the application with Redis using Docker
docker-compose up --build
