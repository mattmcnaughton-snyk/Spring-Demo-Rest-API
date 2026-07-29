# Spring Boot Security Demo API

A Spring Boot REST API that **intentionally contains security vulnerabilities** for educational and security testing purposes.

> **Warning**: This application contains intentional security vulnerabilities. 

## Tech Stack

- Java 21
- Spring Boot 3.5.9
- Spring Data JPA
- H2 Database (in-memory)
- Swagger/OpenAPI (springdoc-openapi)
- Lombok
- Log4j 2.14.0 (vulnerable version)

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | Redirects to Swagger UI |
| `/hello` | GET | Simple hello world endpoint |
| `/file?filename=` | GET | File read endpoint (vulnerable to path traversal) |
| `/ping?host=` | GET | Ping endpoint (vulnerable to command injection) |
| `/ping_secure?host=` | GET | Secure ping endpoint |
| `/swagger-ui/index.html` | GET | Swagger API documentation |

## Known Security Vulnerabilities

This project intentionally includes the following vulnerabilities for security testing:

### 1. Path Traversal (CWE-22)
The `/file` endpoint is vulnerable to path traversal attacks, allowing attackers to read arbitrary files from the system.

### 2. Command Injection (CWE-78)
The `/ping` endpoint is vulnerable to OS command injection, allowing attackers to execute arbitrary system commands.

### 3. Vulnerable Dependencies
- **Log4j 2.14.0** - Affected by Log4Shell (CVE-2021-44228)

## Prerequisites

- Java 21+
- Maven 3.6+

## How to Run

### Option 1: Maven (Local)

```bash
# Build the application
mvn install

# Run the application
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

Or use the convenience script:

```bash
./build_and_run.sh
```


The API will be accessible at: http://localhost:8080

## Testing the Vulnerabilities

### Path Traversal Test

```bash
./path_traversal_vulnerability_test.sh
```

### Remote Exploit Tests

```bash
./remote_exploit_write_file.sh
./remote_exploit_stop_server.sh
```

## Security Scanning

This project is configured for security scanning with Snyk. Run scans to identify vulnerabilities:

```bash
# Scan for code vulnerabilities
snyk code test

# Scan for dependency vulnerabilities
snyk test
```

## Purpose

This application is designed for:
- Security training and education
- Demonstrating common web application vulnerabilities
- Testing security scanning tools (SAST/SCA)
- Learning secure coding practices by comparing vulnerable vs. secure implementations

## License

For educational use only.

