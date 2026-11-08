
Project Name

MessageGenerateIntelligentReply is a high-performance, multi-tenant Spring Boot backend application designed to securely handle and isolate intelligent reply generation across different business entities. Data isolation is maintained via dedicated schemas or identifiers within a centralized PostgreSQL database, while completions are processed via a localized LLM runtime.

🚀 Getting Started



Prerequisites

• Java 17 or higher
• Maven or Gradle
• PostgreSQL instance running locally on the default port
• Ollama installed locally


Dependency Prerequisites & Infrastructure Setup

Before launching the application, ensure both your local database and the open-source inference engine are up and running with the following configurations:

1. PostgreSQL Database

The application requires a PostgreSQL server listening on port 5432.
• Ensure a database exists for the application context.
• Update your src/main/resources/application.properties (or application.yml) file with your access credentials:

spring.datasource.url=jdbc:postgresql://localhost:5432/your_database_name
spring.datasource.username=your_postgres_user
spring.datasource.password=your_postgres_password
spring.jpa.hibernate.ddl-auto=update


2. Ollama Local LLM Execution Engine

Local language model inference is managed using Ollama. The application specifically expects the qwen2.5:0.5b model variant to be present locally.
• Download and run the Ollama instance on your machine.
• Pull and run the model via your terminal prior to bootstrapping the application server

ollama run qwen2.5:0.5b
• Keep the Ollama service daemon active (typically listening locally on port 11434).

Installation & Running Locally

1. Clone the repository:
   git clone https://github.com/sameergijare/MessageGenerateIntelligentReply
   cd MessageGenerateIntelligentReply
2. Build the application
   # For Maven
   ./mvnw clean package
   # For Gradle
   ./gradlew clean build
4. Run the application
   # For Maven
  ./mvnw spring-boot:run
  # For Gradle
  ./gradlew bootRun

The server will start on http://localhost:8080.

Multi-Tenancy Architecture

This application uses a multi-tenant architecture. All requests to protected endpoints must provide an identification context so the backend can isolate data routing.

Required Headers

Every API request requires a routing header to identify which tenant context you are accessing:

Header Key	Expected Value	Description
X-Tenant-ID	tenant1, test-tenant, etc.	The unique identifier/slug of the target customer/organization.
Note: If this header is missing, the server will reject the request with an HTTP 400 Bad Request error: Missing required Multi-Tenant Context Routing Header.

Testing the Endpoints

Since standard web browsers cannot natively append custom headers to the address bar, use one of the following methods for local development:

Option 1: Using cURL
    curl -H "X-Tenant-ID: tenant1" http://localhost:8080/public/smoketest

Option 2: Using Postman / Insomnia

1. Create a new request to http://localhost:8080/public/smoketest
2. Go to the Headers tab.
3. Add a new row: Key = X-Tenant-ID, Value = your-tenant-value

 Project Structure

 
 ├── src
│   ├── main
│   │   ├── java/com/company/intelligentreply     # Core application source code
│   │   │   ├── config                           # Security & multi-tenant filters
│   │   │   ├── controller                       # REST Endpoints
│   │   │   └── service                          # Reply generation engines (Ollama integration)
│   │   └── resources
│   │       ├── static                           # Web UI assets (index.html, styles, JS)
│   │       └── application.properties           # Database connections and model settings
└── pom.xml / build.gradle








