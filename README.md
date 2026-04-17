# Backend - Spring Boot

## Arquitectura Hexagonal

```
backend/
├── src/main/java/com/familyfood/
│   ├── domain/           # Capa Domain (núcleo de negocio)
│   │   ├── model/        # Modelos de dominio (inmutables)
│   │   ├── entity/       # Entidades JPA
│   │   ├── repository/   # Interfaces de repositorio
│   │   └── service/      # Servicios de dominio
│   │
│   ├── application/      # Capa Application (casos de uso)
│   │   ├── dto/          # Data Transfer Objects
│   │   ├── usecase/      # Casos de uso
│   │   ├── mapper/       # Mapeadores DTO ↔ Entity
│   │   └── config/       # Configuración de aplicación
│   │
│   └── infrastructure/   # Capa Infrastructure (adaptadores)
│       ├── controller/   # Controladores REST
│       ├── security/     # Seguridad JWT
│       ├── persistence/  # Implementación repositorios
│       ├── config/       # Configuración infrastructure
│       └── exception/    # Manejo de excepciones
│
├── src/main/resources/
│   ├── db/migration/     # Flyway migrations
│   └── messages/         # Mensajes internacionalización
│
└── src/test/
    ├── java/             # Tests unitarios e integración
    └── resources/        # Configuración tests
```

## Agente Asignado

**BACKEND** → Trabaja en este directorio

## Comandos Útiles

```bash
# Compilar
./mvnw clean compile

# Ejecutar tests
./mvnw test

# Ejecutar aplicación
./mvnw spring-boot:run

# Build producción
./mvnw clean package -DskipTests
```
