# Redes-XMPP_Server

This is the backend part of an XMPP (Extensible Messaging and Presence Protocol) client, responsible for handling user authentication, managing contacts, processing messages, and maintaining the connection with the XMPP server. The backend is developed using Spring Boot, providing a robust and scalable solution for the XMPP client.

## Features

* **User Authentication:** Secure login and registration using XMPP.
* **Contact Management:** Handle contacts, retrieve their statuses, and manage the user's roster.
* **Messaging:** Send and receive messages through the XMPP protocol.
* **Presence Management:** Track and update user presence and availability statuses.
* **RESTful API:** Provides endpoints to interact with the XMPP server and manage client requests.

## Technologies Used

* **Spring Boot:** The framework used for building the backend services.
* **Java:** The programming language used for development.
* **Smack API:** A Java library for communicating with XMPP servers.
* **Maven:** For dependency management and project build.
* **RESTful API:** For communication between the frontend and backend.

## Prerequisites

Before you begin, ensure you have met the following requirements:

* **Java 11 or later:** Make sure Java is installed on your machine. You can download it from the official website: https://www.java.com/en/download/
* **Maven:** Install Maven for building the project. You can download and install it from the official website: https://maven.apache.org/
* **XMPP Server:** Set up an XMPP server (e.g., Openfire, ejabberd) that the client will connect to.

## Installation

**1. Clone the Repository:**

```bash
git clone https://github.com/XaviAlvarado18/Redes-XMPP_Server.git
```

2. Navigate to the Project Directory:

```bash
cd Redes-XMPP_Server
```

3. Configure the Application:

Modify the application.properties file located in src/main/resources to configure the XMPP server connection details. You'll need to replace the placeholders with your actual values:

```bash
xmpp.domain=your-xmpp-domain
xmpp.host=your-xmpp-server-host
xmpp.port=your-xmpp-server-port
```

4. Build the Project:

Run the following command to build the project using Maven:

```bash
mvn clean install
```

5. Run the Application:

Start the Spring Boot application by running:

```bash
mvn spring-boot:run
```

The application will be available at http://localhost:8080.

## Usage

Authentication
- Login: Authenticate users with their XMPP credentials.
- Register: Allow users to create new XMPP accounts.

Contact Management
- Get Contacts: Retrieve the list of contacts (roster) for the authenticated user.
- Get Contact Status: Get the current status (available, busy, etc.) of each contact.

Messaging
- Send Messages: Send messages to contacts.
- Receive Messages: Handle incoming messages from the XMPP server.

Presence Management
- Update Status: Allow users to update their availability status (e.g., available, away, busy).
- Get Current Status: Retrieve the current status of the authenticated user.
