# System Access Management API (SAM)

## Overview

SAM (System Access Management) is a Java Spring Boot API project that facilitates the management of system user profiles, roles, and permissions. The application is built using Hibernate as the Object-Relational Mapping (ORM) framework and PostgreSQL as the backend database.

The primary goal of SAM is to provide a robust and flexible System Access Management solution. It allows users to create and manage system user profiles, assign roles to users, and define permissions for these roles. Additionally, SAM introduces the concept of Permission Sets, which bundles multiple permissions together, offering a convenient way to manage and assign sets of permissions to roles.

## Features

1. **User Profile Management:**
   - Create, update, and delete user profiles.
   - View details of existing user profiles.

2. **Role Assignment:**
   - Assign roles to user profiles.
   - Manage roles by creating, updating, and deleting them.

3. **Permission Management:**
   - Define individual permissions for fine-grained control.
   - Group permissions into Permission Sets for easier management.
   - Lock Permission Sets to restrict their usage to a specific role.

## Technologies Used

- **Java Spring Boot:** The foundation of the API, providing a robust and scalable framework for building enterprise-level applications.
- **Hibernate:** Serving as the ORM tool to map Java objects to database tables, simplifying database operations.
- **PostgreSQL:** The chosen relational database for storing user profiles, roles, permissions, and related data.

## Database Configuration:
Set up a PostgreSQL database and update the application.properties file with the appropriate database connection details.
properties

# application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/your_database_name
spring.datasource.username=your_username
spring.datasource.password=your_password

## Build and Run:
./mvnw clean install
./mvnw spring-boot:run

## API Endpoints:
Access the API documentation at http://localhost:8080/swagger-ui.html for details on available endpoints and request/response formats.
Usage

## User Profile Management:
Create a new user profile by making a POST request to /api/users.
Update user profiles using PUT requests to /api/users/{userId}.
Delete a user profile via a DELETE request to /api/users/{userId}.

## Role Assignment:
Create roles with a POST request to /api/roles.
Assign roles to user profiles using PUT requests to /api/users/{userId}/roles.

## Permission Management:
Define individual permissions with POST requests to /api/permissions.
Create Permission Sets via POST requests to /api/permission-sets.
Lock/Unlock Permission Sets by updating the IsLocked property with PUT requests to /api/permission-sets/{setId}.

## Contributors
Nkambwe J Mark

## License
This project is licensed under the MIT License.

## Getting Started

1. **Clone the Repository:
   git clone https://github.com/your-username/SAM.git
   cd SAM
