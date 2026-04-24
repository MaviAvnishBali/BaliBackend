# Postman cURL Commands for Bali Backend GraphQL API

Base URL: `http://192.168.68.130:8080/graphql`

---

## 🔐 Authentication APIs

### 1. Register User
```bash
curl --location 'http://192.168.68.130:8080/graphql' \
--header 'Content-Type: application/json' \
--data '{
  "query": "mutation { register(username: \"testuser\", email: \"test@example.com\", password: \"password123\") { id username email createdAt } }"
}'
```

### 2. Login (Username/Password)
```bash
curl --location 'http://192.168.68.130:8080/graphql' \
--header 'Content-Type: application/json' \
--data '{
  "query": "mutation { login(username: \"testuser\", password: \"password123\") { token user { id username email phoneNumber } isProfileComplete } }"
}'
```

**Save the `token` from response for authenticated requests!**

### 3. Login with Phone (Firebase)
```bash
curl --location 'http://192.168.68.130:8080/graphql' \
--header 'Content-Type: application/json' \
--data '{
  "query": "mutation { loginWithPhone(firebaseToken: \"YOUR_FIREBASE_ID_TOKEN\") { token user { id phoneNumber } isProfileComplete } }"
}'
```

---

## 👤 User Profile APIs

### 4. Get Current User (Me) - Requires Auth
```bash
curl --location 'http://192.168.68.130:8080/graphql' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_JWT_TOKEN_HERE' \
--data '{
  "query": "query { me { id username email phoneNumber address profileImageUrl role isProfileComplete createdAt lastLogin villageGroup { id name } } }"
}'
```

### 5. Get User by ID
```bash
curl --location 'http://192.168.68.130:8080/graphql' \
--header 'Content-Type: application/json' \
--data '{
  "query": "query { user(id: 1) { id username email phoneNumber address profileImageUrl role createdAt villageGroup { id name } } }"
}'
```

### 6. Save/Update Profile - Requires Auth
```bash
curl --location 'http://192.168.68.130:8080/graphql' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_JWT_TOKEN_HERE' \
--data '{
  "query": "mutation { saveProfile(username: \"john_doe\", email: \"john@example.com\", address: \"123 Main Street\", villageId: 1, profileImageUrl: \"https://example.com/image.jpg\") { id username email address profileImageUrl villageGroup { id name } isProfileComplete } }"
}'
```

**Partial Update Example (only email):**
```bash
curl --location 'http://192.168.68.130:8080/graphql' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer YOUR_JWT_TOKEN_HERE' \
--data '{
  "query": "mutation { saveProfile(email: \"newemail@example.com\") { id email } }"
}'
```

---

## 📰 Post APIs

### 7. Get Feed (Paginated)
```bash
curl --location 'http://192.168.68.130:8080/graphql' \
--header 'Content-Type: application/json' \
--data '{
  "query": "query { feed(page: 0, size: 10) { id content imageUrl likesCount createdAt author { id username } villageGroup { id name } comments { id content createdAt author { id username } } } }"
}'
```

### 8. Get Post by ID
```bash
curl --location 'http://192.168.68.130:8080/graphql' \
--header 'Content-Type: application/json' \
--data '{
  "query": "query { post(id: 1) { id content imageUrl likesCount createdAt author { id username email } villageGroup { id name description } comments { id content createdAt author { id username } } } }"
}'
```

---

## 🏘️ Village Group APIs

### 9. Get All Village Groups
```bash
curl --location 'http://192.168.68.130:8080/graphql' \
--header 'Content-Type: application/json' \
--data '{
  "query": "query { villageGroups { id name description members { id username } } }"
}'
```

### 10. Get Village Group by ID
```bash
curl --location 'http://192.168.68.130:8080/graphql' \
--header 'Content-Type: application/json' \
--data '{
  "query": "query { villageGroup(id: 1) { id name description members { id username email phoneNumber } } }"
}'
```

---

## 📝 Using Variables (Recommended for Postman)

### Example with Variables:
```bash
curl --location 'http://192.168.68.130:8080/graphql' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {{jwt_token}}' \
--data '{
  "query": "mutation SaveProfile($username: String, $email: String, $address: String, $villageId: ID, $profileImageUrl: String) { saveProfile(username: $username, email: $email, address: $address, villageId: $villageId, profileImageUrl: $profileImageUrl) { id username email address isProfileComplete } }",
  "variables": {
    "username": "john_doe",
    "email": "john@example.com",
    "address": "123 Main St",
    "villageId": 1,
    "profileImageUrl": "https://example.com/image.jpg"
  }
}'
```

---

## 🔧 Postman Collection Setup

### Environment Variables (Create in Postman):
- `base_url`: `http://192.168.68.130:8080`
- `graphql_endpoint`: `{{base_url}}/graphql`
- `jwt_token`: (set after login)

### Pre-request Script (for Login):
After login mutation, save token:
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    if (jsonData.data && jsonData.data.login && jsonData.data.login.token) {
        pm.environment.set("jwt_token", jsonData.data.login.token);
    }
}
```

---

## 📋 Quick Test Sequence

1. **Register** → Get user ID
2. **Login** → Get JWT token (save it!)
3. **Get Me** → Verify authentication works
4. **Save Profile** → Complete user profile
5. **Get Me again** → Verify profile is saved

---

## ⚠️ Important Notes

- Replace `YOUR_JWT_TOKEN_HERE` with actual token from login response
- Replace `YOUR_FIREBASE_ID_TOKEN` with actual Firebase token from Android app
- All authenticated requests need `Authorization: Bearer <token>` header
- For local testing, replace `192.168.68.130` with `localhost` or `10.0.2.2` (Android emulator)
