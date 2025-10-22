# Authentication and Authorization System

A Spring Boot application demonstrating role-based access control (RBAC) with HTTP Basic Authentication.

## Overview

This system implements a secure authentication and authorization prototype with:
- **Role-based access control** (ADMIN, WRITE, READ roles)
- **HTTP Basic Authentication** for secure access
- **Dynamic user management** (create, delete, update roles)
- **URL-based service identification** for different access levels
- **BCrypt password hashing** for secure password storage

## Security Features

### Authentication
- **HTTP Basic Authentication** - Industry standard for API authentication
- **BCrypt Password Hashing** - Adaptive hashing function resistant to brute force attacks
- **In-memory user store** - Fast access with secure password verification

### Authorization
- **Role-based access control** with three distinct roles:
  - `ADMIN` - Full system access including user management
  - `WRITE` - Can create and modify resources
  - `READ` - Can view resources and data
- **Method-level security** using `@PreAuthorize` annotations
- **URL-based service identification** for different access levels

### Security Best Practices
- **CSRF protection disabled** for API-only usage (appropriate for REST APIs)
- **Password encoding** with BCrypt (adaptive, secure hashing)
- **Input validation** for user creation and role management
- **Authorization checks** on all protected endpoints


## Sequrity Requirement

### 1. **Authentication Security**
- **Strong Password Hashing**: BCrypt provides secure password storage
- **HTTP Basic Auth**: Industry standard for API authentication
- **Credential Validation**: Secure credential verification process

### 2. **Authorization Security**
- **Role-based Access Control**: Clear separation of privileges
- **Method-level Security**: Fine-grained access control
- **Principle of Least Privilege**: Users only get necessary permissions

### 3. **Input Security**
- **Request Validation**: All user inputs are validated
- **Role Validation**: Only valid roles are accepted
- **Error Handling**: Secure error responses without information leakage

### 4. **Operational Security**
- **Audit Logging**: All user operations are logged
- **Real-time Monitoring**: Console output shows security events
- **Secure Defaults**: Secure configuration by default

This system demonstrates enterprise-grade security practices suitable for production environments while maintaining simplicity for educational purposes.


## Quick Start

### Prerequisites
- Java 21+
- Maven 3.6+

### Running the Application
```bash
# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run
```

The application will start on **http://localhost:8082**

### Predefined Users
The system comes with three predefined users for testing:

| Username | Password | Roles | Access Level |
|----------|----------|-------|--------------|
| `system_admin` | `system_admin_pass` | ADMIN, READ, WRITE | Full access |
| `system_reader` | `system_reader_pass` | READ | Read-only access |
| `system_writer` | `system_writer_pass` | WRITE | Write access |

## API Endpoints

### Service Endpoints (Role-based Access)

#### Admin Endpoints (Requires ADMIN role)
- `GET /service/admin/ping` - Admin health check
- `GET /service/admin/system-info` - System information

#### Read Endpoints (Requires READ role)
- `GET /service/read/ping` - Read access health check
- `GET /service/read/public-data` - Public data access

#### Write Endpoints (Requires WRITE role)
- `GET /service/write/ping` - Write access health check
- `POST /service/write/create` - Create new resources

### User Management Endpoints (Requires ADMIN role)

#### Create User
```bash
curl -u system_admin:system_admin_pass -X POST http://localhost:8082/auth/users \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","password":"newpass","roles":["READ"]}'
```

#### Update User Roles
```bash
curl -u system_admin:system_admin_pass -X PUT http://localhost:8082/auth/users/newuser/roles \
  -H "Content-Type: application/json" \
  -d '{"roles":["WRITE","READ"]}'
```

#### Delete User
```bash
curl -u system_admin:system_admin_pass -X DELETE http://localhost:8082/auth/users/newuser
```

## Testing the System

### Automated Test Suite
Run the comprehensive test script to verify all functionality:

```bash
chmod +x test_auth_system.sh
./test_auth_system.sh
```

This script will:
- Test all predefined users
- Verify authorization works correctly
- Create new users dynamically
- Test role-based access control
- Demonstrate user management operations
- Clean up test users

### Manual Testing Examples

#### Test Admin Access
```bash
curl -u system_admin:system_admin_pass http://localhost:8082/service/admin/ping
```

#### Test Authorization (Should Fail)
```bash
curl -u system_reader:system_reader_pass http://localhost:8082/service/admin/ping
```

#### Create a New User
```bash
curl -u system_admin:system_admin_pass -X POST http://localhost:8082/auth/users \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass","roles":["WRITE"]}'
```

## Security Architecture

### Authentication Flow
1. Client sends HTTP Basic Auth credentials
2. Spring Security validates credentials against user store
3. BCrypt verifies password hash
4. User details and authorities are loaded
5. Authentication token is created

### Authorization Flow
1. `@PreAuthorize` annotations check user roles
2. Method-level security enforces access control
3. Unauthorized requests are rejected with 403 Forbidden
4. Console logging tracks access attempts

### Security Measures
- **Password Security**: BCrypt with adaptive hashing
- **Access Control**: Role-based permissions
- **Input Validation**: Request validation for user operations
- **Audit Logging**: Console output for security events
- **Method Security**: Annotation-based authorization

## Project Structure

```
src/main/java/org/ngafid/
├── AuthPrototypeApplication.java    # Main application with REST endpoints
├── SecurityConfig.java             # Spring Security configuration
└── UserDetailsManagerConfig.java   # User management configuration

src/main/resources/
└── application.properties          # Application configuration

test_auth_system.sh                 # Comprehensive test script
```

