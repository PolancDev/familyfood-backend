# FamilyFood - Backend

Backend de la aplicación de planificación de menús familiares. Proporciona una API REST para la gestión de usuarios, familias, recetas, planificación semanal y lista de la compra.

## Stack Tecnológico

- **Java 21**
- **Spring Boot 3.2.5**
- **Spring Security + JWT** (jjwt 0.12.5)
- **Spring Data JPA** (Hibernate)
- **PostgreSQL 16**
- **Flyway** (migraciones de base de datos)
- **MapStruct** (mapeo DTO/Entidad)
- **Lombok** (reducción de boilerplate)
- **OpenAPI 3.0 / Swagger** (documentación API)
- **Bucket4j** (rate limiting)
- **Micrometer Tracing** (trazabilidad)
- **JUnit 5 + Mockito + Testcontainers** (tests)
- **Maven** (gestor de dependencias)

## Arquitectura

Arquitectura Hexagonal (Ports & Adapters):

```
src/main/java/com/familyfood/
├── domain/                    → Lógica de negocio pura
│   ├── enums/                 → Role, FamilyRole, JoinRequestStatus
│   ├── model/                 → User, FamilyGroup, FamilyMember, JoinRequest
│   └── exception/             → DomainException, NotFoundException, etc.
│
├── application/               → Casos de uso y puertos
│   ├── dto/                   → auth/, family/ (LoginRequest, FamilyResponse...)
│   ├── mapper/                → AuthMapper, FamilyMapper, UserMapper
│   ├── port/repository/       → UserRepository, FamilyGroupRepository...
│   └── service/               → AuthService, JwtService, FamilyService
│
└── infrastructure/            → Adaptadores técnicos
    ├── adapter/
    │   ├── persistence/       → entities/, repository/, adapters/
    │   ├── security/          → JwtAuthenticationFilter, CustomUserDetails
    │   └── web/               → AuthController, FamilyController
    ├── config/                → SecurityConfig, GlobalExceptionHandler
    └── init/                  → DataInitializer (usuarios demo)
```

## API Endpoints

### Autenticación
| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| POST | `/api/v1/auth/register` | Registrar nuevo usuario | ❌ No |
| POST | `/api/v1/auth/login` | Iniciar sesión (devuelve JWT) | ❌ No |

### Familias/Grupos
| Método | Ruta | Descripción | Rol |
|--------|------|-------------|-----|
| POST | `/api/v1/families` | Crear nueva familia | Cualquiera |
| POST | `/api/v1/families/{id}/join` | Solicitar unirse a una familia | Cualquiera |
| GET | `/api/v1/families/mine` | Listar mis familias | Cualquiera |
| GET | `/api/v1/families/{id}/requests` | Ver solicitudes pendientes | ADMIN |
| PUT | `/api/v1/families/requests/{requestId}/approve` | Aprobar solicitud | ADMIN |
| PUT | `/api/v1/families/requests/{requestId}/reject` | Rechazar solicitud | ADMIN |

### Usuarios
| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| GET | `/api/v1/usuarios/me` | Perfil del usuario actual | ✅ Sí |

### Recetas
| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| GET | `/api/v1/recetas` | Listar recetas del usuario | ✅ Sí |
| POST | `/api/v1/recetas` | Crear nueva receta | ✅ Sí |
| GET | `/api/v1/recetas/{id}` | Obtener receta por ID | ✅ Sí |
| PUT | `/api/v1/recetas/{id}` | Actualizar receta | ✅ Sí |
| DELETE | `/api/v1/recetas/{id}` | Eliminar receta | ✅ Sí |

**Características:** CRUD completo con ingredientes, etiquetas (RAPIDA, ECONOMICA, NINOS), niveles de cocina (BASICO, MEDIO, AVANZADO), fotos y soporte para optimistic locking.

## Base de Datos

### Migraciones Flyway
| Archivo | Descripción |
|---------|-------------|
| `V1__create_users_table.sql` | Tabla `users` con id, email, password, nombre, role, fechas |
| `V4__create_family_tables.sql` | Tablas `family_group`, `family_member`, `join_request` |

### Modelo de Datos
```
User (1) ──── (N) FamilyMember (N) ──── (1) FamilyGroup
  │
  └── (N) JoinRequest (N) ──── (1) FamilyGroup
```

## Cómo ejecutar

### Requisitos
- Java 21+
- Maven 3.9+
- PostgreSQL 16+

### Pasos
```bash
# 1. Clonar el repositorio
git clone https://github.com/PolancDev/familyfood-backend.git
cd familyfood-backend

# 2. Crear base de datos en PostgreSQL
psql -U postgres -c "CREATE DATABASE familyfood;"

# 3. Configurar application.yml
#    Editar src/main/resources/application.yml:
#    spring.datasource.url: jdbc:postgresql://localhost:5432/familyfood
#    spring.datasource.username: postgres
#    spring.datasource.password: tu_password
#    jwt.secret: clave_secreta_para_firmar_tokens

# 4. Ejecutar la aplicación
mvn spring-boot:run

# 5. La API estará disponible en:
#    - API: http://localhost:8080
#    - Swagger UI: http://localhost:8080/swagger-ui.html
#    - OpenAPI JSON: http://localhost:8080/v3/api-docs
```

## Tests
```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests con cobertura
mvn verify

# Ejecutar un test específico
mvn test -Dtest=AuthServiceTest
```

## Usuarios Demo
Al iniciar la aplicación, se crean automáticamente estos usuarios de prueba:

| Email | Contraseña | Rol |
|-------|-----------|-----|
| admin@familyfood.com | admin123 | ADMIN |
| consumer@familyfood.com | consumer123 | CONSUMER |

## Seguridad
- Todos los endpoints excepto `/api/v1/auth/*` requieren JWT
- El token JWT se envía en el header: `Authorization: Bearer {token}`
- Roles: `INVITADO` (sin familia), `CONSUMER` (miembro de familia), `ADMIN` (administrador de familia)
- Rate limiting con Bucket4j en endpoints críticos
- Passwords hasheadas con BCrypt

## Documentación
- **OpenAPI/Swagger:** http://localhost:8080/swagger-ui.html
- **Contrato API:** `devops/openapi.yml`
