# BaliBackend

A robust Java Spring Boot backend for the Bali App, featuring GraphQL API, Phone Authentication (Firebase), and Social Feed management.

## 🚀 Features

- **Authentication**: 
  - Phone-based login via Firebase ID tokens.
  - Legacy username/password login & registration.
  - JWT-based session management.
- **Social Feed**:
  - PAGINATED feed retrieval.
  - Create, update, and delete posts.
  - Image support for posts.
  - Like and comment functionality.
- **User Management**:
  - Detailed user profiles with image support.
  - Village-based user grouping.
- **GraphQL API**: Flexible and efficient data fetching.
- **Database**: PostgreSQL for persistence.

## 🛠 Tech Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **API**: Spring GraphQL
- **Security**: Spring Security, JJWT, Firebase Admin SDK
- **Database**: Spring Data JPA, PostgreSQL
- **Build Tool**: Gradle (Kotlin DSL)

## 📋 Prerequisites

- Java 17 or higher
- PostgreSQL
- Firebase Project (for phone authentication)

## ⚙️ Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/MaviAvnishBali/BaliBackend.git
   cd backend
   ```

2. **Configuration**:
   Update `src/main/resources/application.yml` with your database credentials and secret keys.
   Add your `firebase-service-account.json` to `src/main/resources/`.

3. **Database Setup**:
   Ensure PostgreSQL is running and create a database as specified in `application.yml`.

4. **Run the application**:
   ```bash
   ./gradlew bootRun
   ```

## 📡 API Usage

The API is accessible via GraphQL. You can access the GraphiQL interface (if enabled) at `/graphiql`.

### Sample Queries

**Fetch Feed:**
```graphql
query {
  feed(page: 0, size: 10) {
    id
    content
    imageUrl
    author {
      username
    }
    likesCount
  }
}
```

**Login with Phone:**
```graphql
mutation {
  loginWithPhone(firebaseToken: "YOUR_FIREBASE_TOKEN") {
    token
    user {
      username
      isProfileComplete
    }
  }
}
```

## 📄 License

This project is licensed under the MIT License.
