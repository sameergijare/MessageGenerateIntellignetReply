
Project Name

A multi-tenant backend application built with Spring Boot, designed to securely isolate data across different organizations or customers (tenants).It generates
customized reply to Ticket based on Knowledge Bases provided using Ollama Streaming Chat model and quantized embedded model.

🚀 Getting Started


Prerequisites

• Java 17 or higher
• Maven 


Installation & Running Locally

1. Clone the repository:
   git clone https://github.com/sameergijare/MessageGenerateIntelligentReply
   cd MessageGenerateIntelligentReply
2. Build the application
   # For Maven
   ./mvnw clean package
3. Run the application
   # For Maven
  ./mvnw spring-boot:run

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
│   │   ├── java          # Backend source code
│   │   └── resources
│   │       ├── static    # Static files (index.html, css, js)
│   │       └── templates # Dynamic views (if applicable)
└── pom.xml / build.gradle







