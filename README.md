### **Protocol (Interim submission) - Monster Trading Card Game (MTCG)**

#### **1. Project Overview**
- **Project Name:** Monster Trading Card Game
- **Description:** Implementation of a server for a Trading Card Game in Java with a REST API, without using an HTTP framework.

---

#### **2. Design Decisions**
**a) Technology Stack**
- **Language:** Java
- **Server:** Custom-built HTTP server without frameworks
- **Build Tool:** `Maven`
- **Testing Methodology:** Integration tests using CURL

**b) Security Mechanisms**
- Token-based authentication: Each user receives a token upon successful login, used for accessing protected endpoints.

**c) REST API Endpoints**
- Endpoints adhere to specifications:
  - Registration: `POST /users`
  - Login: `POST /sessions`

---

#### **3. Class Structure**
**a) Model Classes:**
- **`User`**
  - Fields: `username`, `password`, `token`
- **`Card`** (planned for futur features)

**b) Controller Classes:**
- **`UserController`**
  - Handles registration and login.

**c) Service Classes:**
- **`UserService`**
  - Contains logic for registration and login.
- **`TokenService`**
  - Generates tokens.

**d) Repository Classes:**
- **`UserRepository`**
  - Stores users in an in-memory map.

**e) Server:**
- **`CustomHttpServer`**
  - Parses and routes HTTP requests.
  - Generates HTTP responses.

---

#### **4. CURL Tests Results**
The CURL tests show successful execution of the requirements:

**Test 1: Create Users (Registration)**  
- Successfully registered users.
- Conflicts identified when registering existing users.

**Test 2: Login Users**  
- Successful login with token generation.
- Failed login for invalid credentials.

---

#### **5. Challenges**
- **HTTP Parsing:** Parsing HTTP requests and responses manually was time-consuming.
- **Token Management:** Implementing a simple yet secure token logic.
- **REST API Compliance:** Ensuring strict adhherence to the specifications.

---

#### **6. GitHub Repository**
The project is on GitHub: [Click here](<https://github.com/SofiaHKG/MonsterTradingCardGame>)

---

