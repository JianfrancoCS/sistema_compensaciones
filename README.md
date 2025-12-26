# Agropay - Sistema de Gestión de Compensaciones

Sistema integral de gestión de recursos humanos y compensaciones para el sector agrícola, diseñado para administrar empleados, asistencia, planillas de pago, contratos laborales y tareos de campo.

## Descripción del Proyecto

Agropay es una plataforma completa que permite gestionar todo el ciclo de vida de los empleados en el sector agrícola, desde la contratación hasta el cálculo de planillas, incluyendo el control de asistencia en tiempo real y la gestión de tareos en campo.

## Arquitectura del Sistema

El proyecto está compuesto por tres módulos principales:

### Core (Backend)
API REST desarrollada con Spring Boot 3.5.5 y Java 21 que proporciona todos los servicios del sistema.

### Web (Frontend)
Aplicación web desarrollada con Angular 20 que permite la gestión administrativa del sistema.

### Movil (Aplicación Móvil)
Aplicación multiplataforma desarrollada con Kotlin Multiplatform y Jetpack Compose para Android e iOS, enfocada en la gestión de tareos y asistencia en campo.

## Tecnologías Principales

### Backend
- Spring Boot 3.5.5
- Java 21
- Spring Security con JWT
- Spring Data JPA
- Spring Batch
- Spring WebSocket (STOMP)
- SQL Server
- Flyway (Migraciones de base de datos)
- MapStruct (Mapeo de objetos)
- Lombok
- iText 7 (Generación de PDFs)
- Cloudinary (Almacenamiento de imágenes)

### Frontend Web
- Angular 20
- TypeScript
- PrimeNG 20
- Tailwind CSS
- RxJS
- Chart.js
- Dynamsoft Barcode Reader
- PDFMake

### Aplicación Móvil
- Kotlin Multiplatform
- Jetpack Compose
- Ktor (Cliente HTTP)
- SQLDelight (Base de datos local)
- Coroutines y Flow

## Funcionalidades Principales

### Gestión de Personal
- Administración completa de empleados
- Gestión de personas y documentos
- Asignación de cargos y posiciones
- Gestión de subsidiarias y áreas
- Fotos y documentos de empleados

### Control de Asistencia
- Registro de marcaciones con códigos QR
- Asistencia en tiempo real mediante WebSockets
- Validación de empleados por subsidiaria
- Resúmenes y reportes de asistencia
- Motivos de entrada y salida

### Planillas de Pago
- Cálculo automático de planillas
- Múltiples conceptos de pago y descuento
- Configuración de conceptos personalizados
- Generación de boletas de pago en PDF
- Procesamiento por lotes con Spring Batch

### Contratos Laborales
- Creación y gestión de contratos
- Plantillas personalizables con variables
- Generación automática de PDFs
- Firmas digitales (empleador y empleado)
- Estados de contrato (borrador, firmado, cancelado)
- Adendas a contratos

### Tareos
- Registro de tareos en campo
- Gestión de lotes y labores
- Asignación de empleados a tareos
- Cálculo de horas trabajadas
- Motivos de entrada y salida
- Cálculo de productividad (destajo)
- Sincronización offline con base de datos local

### Autenticación y Seguridad
- Autenticación JWT (access y refresh tokens)
- Sistema de perfiles y roles
- Menú dinámico según perfil
- Validación de usuarios activos
- Cambio de contraseñas
- Soporte multiplataforma (WEB, MOBILE)

### Gestión de Organización
- Empresas y subsidiarias
- Áreas y ubicaciones
- Cargos y posiciones
- Responsables de firma por subsidiaria

## Estructura del Proyecto

```
sistemas_compensaciones/
├── core/                    # Backend Spring Boot
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/         # Código fuente Java
│   │   │   └── resources/   # Configuración y migraciones
│   │   └── test/            # Pruebas unitarias
│   └── pom.xml
├── web/                      # Frontend Angular
│   ├── src/
│   │   └── app/             # Código fuente TypeScript
│   └── package.json
├── movil/                    # Aplicación móvil Kotlin
│   └── composeApp/
│       └── src/
│           └── commonMain/  # Código compartido
└── .gitignore
```

## Requisitos Previos

### Para el Backend
- Java 21 o superior
- Maven 3.6 o superior
- SQL Server 2019 o superior
- Acceso a Cloudinary (para almacenamiento de imágenes)

### Para el Frontend Web
- Node.js 18 o superior
- npm o yarn

### Para la Aplicación Móvil
- Android Studio (para Android)
- Xcode (para iOS)
- Gradle 8.0 o superior

## Configuración e Instalación

### Backend (Core)

1. Navegar al directorio core:
```bash
cd core
```

2. Configurar variables de entorno:
   - Crear archivo `.env` o configurar variables de sistema:
     - `SPRING_PROFILES_ACTIVE`: Perfil activo (dev, test, prod)
     - `JWT_SECRET`: Clave secreta para JWT (mínimo 32 caracteres)
     - `JWT_EXPIRATION`: Tiempo de expiración del access token (milisegundos)
     - `JWT_REFRESH_EXPIRATION`: Tiempo de expiración del refresh token (milisegundos)
   - Configurar conexión a base de datos en `application-dev.yml` o `application-prod.yml`

3. Ejecutar migraciones de base de datos:
```bash
./mvnw flyway:migrate
```

4. Compilar el proyecto:
```bash
./mvnw clean compile
```

5. Ejecutar la aplicación:
```bash
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8080`
Documentación Swagger disponible en `http://localhost:8080/swagger-ui.html`

### Frontend Web

1. Navegar al directorio web:
```bash
cd web
```

2. Instalar dependencias:
```bash
npm install
```

3. Configurar variables de entorno en `src/environments/environment.ts`:
   - `apiUrl`: URL del backend (por defecto: http://localhost:8080)

4. Ejecutar en modo desarrollo:
```bash
npm start
```

La aplicación estará disponible en `http://localhost:4200`

5. Compilar para producción:
```bash
npm run build
```

### Aplicación Móvil

1. Navegar al directorio movil:
```bash
cd movil
```

2. Configurar la URL del backend en el código:
   - Editar `composeApp/src/commonMain/kotlin/com/agropay/data/remote/AuthService.kt`
   - Actualizar `baseUrl` con la URL del backend

3. Para Android:
   - Abrir el proyecto en Android Studio
   - Sincronizar Gradle
   - Ejecutar en dispositivo o emulador

4. Para iOS:
   - Abrir `iosApp/iosApp.xcodeproj` en Xcode
   - Configurar el equipo de desarrollo
   - Ejecutar en simulador o dispositivo

## Base de Datos

El sistema utiliza SQL Server como base de datos principal. Las migraciones se gestionan mediante Flyway y se encuentran en `core/src/main/resources/db/migration/`.

### Esquema Principal
- `app`: Esquema principal de la aplicación
- Tablas principales:
  - Usuarios y autenticación
  - Empleados y personas
  - Contratos y adendas
  - Asistencia y marcaciones
  - Planillas y conceptos
  - Tareos y empleados de tareo
  - Organización (empresas, subsidiarias, áreas, posiciones)

## API REST

La API REST sigue el patrón RESTful y está documentada con Swagger/OpenAPI. Los endpoints principales incluyen:

- `/v1/auth/*` - Autenticación y autorización
- `/v1/employees/*` - Gestión de empleados
- `/v1/contracts/*` - Gestión de contratos
- `/v1/attendance/*` - Control de asistencia
- `/v1/payroll/*` - Gestión de planillas
- `/v1/tareos/*` - Gestión de tareos
- `/v1/organization/*` - Gestión organizacional

## WebSockets

El sistema utiliza WebSockets (STOMP) para la comunicación en tiempo real del control de asistencia. Los endpoints WebSocket están documentados con Springwolf y disponibles en `/springwolf/docs.html`.

## Seguridad

- Autenticación basada en JWT
- Tokens de acceso y renovación
- Validación de usuarios activos
- Sistema de perfiles y permisos
- Interceptores HTTP para agregar tokens automáticamente
- Renovación automática de tokens expirados

## Desarrollo

### Compilación

Backend:
```bash
cd core
./mvnw clean compile
```

Frontend:
```bash
cd web
npm run build
```

### Pruebas

Backend:
```bash
cd core
./mvnw test
```

Frontend:
```bash
cd web
npm test
```

## Licencia

Este proyecto es privado y de uso interno.

## Contacto

Para más información sobre el proyecto, contactar al equipo de desarrollo.

