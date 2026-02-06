# PackingOptions

A Spring Boot REST API application that calculates optimal packaging options for grocery orders. The system minimizes the number of packages needed by using available bundle sizes intelligently.

## Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Project Setup](#project-setup)
- [Running the Application](#running-the-application)
- [API Documentation & Testing](#api-documentation--testing)
- [API Endpoints](#api-endpoints)
- [Testing](#testing)
- [Database](#database)
- [Project Structure](#project-structure)

---

## Overview

The PackingOptions application provides:
- **Product Management**: CRUD operations for grocery products
- **Packaging Options**: Configure bundle sizes and prices for products
- **Order Processing**: Create orders with optimal packaging calculation to minimize package count
- **Swagger UI**: Interactive API documentation

<details>
<summary><strong>Example Use Case</strong></summary>

For a product like Cheese (CE) with bundle options of 3-pack and 5-pack:
- Ordering 13 units → 2×5-pack + 1×3-pack = **3 packages** (optimal)

</details>

---

## Prerequisites

Ensure you have the following installed:

| Requirement | Version |
|-------------|---------|
| **Java JDK** | 17 or higher |
| **Maven** | 3.6+ (or use included Maven wrapper) |

<details>
<summary><strong>Verify Installation</strong></summary>

```bash
java -version
mvn -version
```

</details>

---

## Project Setup

### 1. Clone/Download the Project

```bash
cd PackingOptions
```

### 2. Build the Project

<details>
<summary><strong>Using Maven Wrapper (Recommended)</strong></summary>

```bash
# Windows
.\mvnw.cmd clean install

# Linux/macOS
./mvnw clean install
```

</details>

<details>
<summary><strong>Using System Maven</strong></summary>

```bash
mvn clean install
```

</details>

---

## Running the Application

<details>
<summary><strong>Option 1: Using Maven</strong></summary>

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/macOS
./mvnw spring-boot:run
```

</details>

<details>
<summary><strong>Option 2: Using JAR file</strong></summary>

```bash
# Build the JAR first
.\mvnw.cmd clean package -DskipTests

# Run the JAR
java -jar target/PackingOptions-0.0.1-SNAPSHOT.jar
```

</details>

### Verify Application is Running

Once started, the application runs on **http://localhost:8080**

Check health by accessing:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:file:./data/grocerydb`
  - Username: `admin`
  - Password: `admin`

---

## API Documentation & Testing

Interactive API documentation is available via **Swagger UI**:

📖 **http://localhost:8080/swagger-ui.html**

### Option 1: Using Swagger UI

<details>
<summary><strong>Step 1: Open Swagger UI</strong></summary>

1. Start the application (see [Running the Application](#running-the-application))
2. Open your browser and navigate to: **http://localhost:8080/swagger-ui.html**
3. You'll see all available API endpoints grouped by controller

</details>

<details>
<summary><strong>Step 2: Explore API Endpoints</strong></summary>

1. Click on any API group to expand it (e.g., **Orders**, **Products**, **Packaging Options**)
2. Click on a specific endpoint to see details:
   - HTTP method and URL
   - Request parameters/body schema
   - Response format and status codes

</details>

<details>
<summary><strong>Step 3: Execute a Request ("Try it out")</strong></summary>

1. Click the **"Try it out"** button on any endpoint
2. Fill in required parameters:
   - For `GET` requests: Enter path parameters (e.g., product code, order ID)
   - For `POST`/`PUT` requests: Edit the JSON request body
3. Click **"Execute"** to send the request
4. View the response:
   - **Response body**: The actual data returned
   - **Response code**: HTTP status (200, 201, 404, etc.)
   - **Response headers**: Additional metadata

</details>

<details>
<summary><strong>Step 4: Example - Create an Order</strong></summary>

1. Expand **Orders** → Click on `POST /api/v1/orders`
2. Click **"Try it out"**
3. In the request body, enter:
   ```json
   {
     "items": [
       {"productCode": "CE", "quantity": 13},
       {"productCode": "HM", "quantity": 10}
     ]
   }
   ```
4. Click **"Execute"**
5. View the response showing optimal packaging breakdown

</details>

<details>
<summary><strong>Step 5: Example - View Products</strong></summary>

1. Expand **Products** → Click on `GET /api/v1/products`
2. Click **"Try it out"**
3. Click **"Execute"**
4. View the list of all available products with their codes and prices

</details>

### Option 2: Using Postman

A pre-configured Postman collection is available for testing all API endpoints.

<details>
<summary><strong>Step 1: Import the Collection</strong></summary>

1. Open **Postman** application
2. Click **Import** button (top-left corner)
3. Choose one of these methods:
   - **File**: Select `postman-collection/Grocery Shop API.postman_collection.json` from the project folder
   - **Drag & Drop**: Drag the JSON file directly into Postman
4. Click **Import** to confirm

</details>

<details>
<summary><strong>Step 2: Set Up Environment (Optional)</strong></summary>

1. The collection uses `{{baseUrl}}` variable pointing to `http://localhost:8080`
2. To create an environment:
   - Click **Environments** → **Create Environment**
   - Add variable: `baseUrl` = `http://localhost:8080`
   - Select the environment from the dropdown

</details>

<details>
<summary><strong>Step 3: Run Requests</strong></summary>

1. Expand the **Grocery Shop API** collection in the sidebar
2. Browse folders: **Products**, **Packaging Options**, **Orders**
3. Click on any request to open it
4. Click **Send** to execute the request
5. View the response in the lower panel

</details>

<details>
<summary><strong>Step 4: Example Requests Included</strong></summary>

| Folder | Requests |
|--------|----------|
| **Products** | Get All, Get by Code, Create, Update, Delete |
| **Packaging Options** | Get All, Get by Product, Create, Update, Delete |
| **Orders** | Create Order, Get by ID, Get All, Delete |

</details>

---

## API Endpoints
<img width="1255" height="883" alt="image" src="https://github.com/user-attachments/assets/8d2e3cdf-59d6-4f0c-bf80-7dbb5ab613d6" />

<details>
<summary><strong>Products API (`/api/v1/products`)</strong></summary>

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/products` | Get all products |
| GET | `/api/v1/products/{code}` | Get product by code |
| POST | `/api/v1/products` | Create a new product |
| PUT | `/api/v1/products/{code}` | Update a product |
| DELETE | `/api/v1/products/{code}` | Delete a product |

</details>

<details>
<summary><strong>Packaging Options API (`/api/v1/packaging-options`)</strong></summary>

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/packaging-options` | Get all packaging options |
| GET | `/api/v1/packaging-options/{id}` | Get packaging option by ID |
| GET | `/api/v1/packaging-options/product/{productCode}` | Get options for a product |
| POST | `/api/v1/packaging-options` | Create a packaging option |
| PUT | `/api/v1/packaging-options/{id}` | Update a packaging option |
| DELETE | `/api/v1/packaging-options/{id}` | Delete a packaging option |

</details>

<details>
<summary><strong>Orders API (`/api/v1/orders`)</strong></summary>

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/orders` | Get all orders |
| GET | `/api/v1/orders/{id}` | Get order by ID |
| POST | `/api/v1/orders` | Create order with optimal packaging |
| DELETE | `/api/v1/orders/{id}` | Delete an order |

</details>

---

## Testing

### Run All Tests

```bash
# Windows
.\mvnw.cmd test

# Linux/macOS
./mvnw test
```

<details>
<summary><strong>Run Tests with Coverage Report</strong></summary>

```bash
.\mvnw.cmd clean test jacoco:report
```

Coverage report will be generated at: `target/site/jacoco/index.html`

</details>

<details>
<summary><strong>Test Categories</strong></summary>

The project includes:
- **Unit Tests**: Service and repository layer tests
- **Controller Tests**: REST API endpoint tests using MockMvc
- **Integration Tests**: End-to-end order processing tests

| Test File | Description |
|-----------|-------------|
| `ProductServiceTest` | Product service unit tests |
| `OrderServiceTest` | Order service unit tests |
| `PackagingCalculatorServiceTest` | Packaging algorithm tests |
| `PackagingOptionServiceTest` | Packaging option service tests |
| `ProductControllerTest` | Product API endpoint tests |
| `OrderControllerTest` | Order API endpoint tests |
| `OrderIntegrationTest` | Full integration tests |

</details>

---

## Database

The application uses **H2 Database** with the following configuration:

| Property | Value |
|----------|-------|
| Type | H2 (file-based for dev, in-memory for tests) |
| Location | `./data/grocerydb` |
| Console URL | http://localhost:8080/h2-console |
| Username | `admin` |
| Password | `admin` |

<details>
<summary><strong>Pre-loaded Sample Data</strong></summary>

The application comes with sample data:

**Products:**
| Code | Name | Base Price |
|------|------|------------|
| CE | Cheese | $5.95 |
| HM | Ham | $7.95 |
| SS | Soy Sauce | $11.95 |

**Packaging Options:**
| Product | Bundle Size | Bundle Price |
|---------|-------------|--------------|
| Cheese (CE) | 3 | $14.95 |
| Cheese (CE) | 5 | $20.95 |
| Ham (HM) | 2 | $13.95 |
| Ham (HM) | 5 | $29.95 |
| Ham (HM) | 8 | $40.95 |

</details>

---

## Project Structure

<details>
<summary><strong>View Project Structure</strong></summary>

```
PackingOptions/
├── src/
│   ├── main/
│   │   ├── java/com/project/packingoptions/
│   │   │   ├── config/           # Application configuration
│   │   │   ├── controller/       # REST API controllers
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── exception/        # Custom exceptions & handlers
│   │   │   ├── mapper/           # Entity-DTO mappers
│   │   │   ├── model/            # JPA entities
│   │   │   ├── repository/       # Spring Data JPA repositories
│   │   │   ├── service/          # Business logic services
│   │   │   └── PackingOptionsApplication.java
│   │   └── resources/
│   │       ├── application.yml   # Main configuration
│   │       └── data.sql          # Initial data
│   └── test/
│       ├── java/                 # Test classes
│       └── resources/
│           └── application-test.yml  # Test configuration
├── data/                         # H2 database files
├── pom.xml                       # Maven dependencies
└── README.md
```

</details>

---

## Technologies Used

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **H2 Database**
- **Lombok**
- **SpringDoc OpenAPI (Swagger)**
- **JaCoCo** (Code Coverage)
- **JUnit 5** & **Mockito** (Testing)

---

## Troubleshooting

<details>
<summary><strong>Port Already in Use</strong></summary>

```bash
# Change port in application.yml or run with:
java -jar target/PackingOptions-0.0.1-SNAPSHOT.jar --server.port=8081
```

</details>

<details>
<summary><strong>Database Lock Error</strong></summary>

Delete the `data/` folder and restart the application to reset the database.

</details>

<details>
<summary><strong>Maven Wrapper Permission (Linux/macOS)</strong></summary>

```bash
chmod +x mvnw
```

</details>
