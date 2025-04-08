# Post Platform Backend

This is the backend for the **Post Platform**, developed using **Spring Boot**, **JPA**, **Redis**, and **JWT**. This platform allows users to create posts, moderate comments, manage join requests to communities, and more. It implements robust authentication mechanisms, user management, post creation, and caching to ensure optimal performance.

## Features

### 1. **User Authentication & Authorization**
- **JWT Authentication**: Secure user authentication via **JSON Web Tokens (JWT)**. The backend uses JWT tokens to authenticate and authorize users.
- **User Management**: The platform allows user registration, login, and profile management.

### 2. **Post Management**
- Users can create posts that are associated with a specific community.
- Posts are temporarily cached in **Redis** for fast access and then stored in the **MySQL** database.
- **Post Moderation**: Community administrators or moderators can approve or reject posts.
  
### 3. **Commenting System**
- Users can comment on posts.
- Comments are associated with the post and the user, ensuring that each comment has an owner.
- Only the post owner and the comment owner can reply to the comment, ensuring privacy and moderation.
  
### 4. **Community Management**
- Users can request to join a community.
- Once a user sends a request to join a community, it’s temporarily cached in **Redis**.
- **Moderators** can approve or reject requests to join the community.
  
### 5. **Join Request Management**
- **Redis Caching**: Join requests are stored temporarily in **Redis** to allow fast access. Once validated, they are permanently stored in **MySQL**.
- **Redis Cache**: Reduces database load by caching the join requests and post data, allowing for more efficient processing.

### 6. **Redis Caching Layer**
- **Redis** is used to cache temporary data, such as join requests and posts, before they are moved to the permanent database (MySQL).
- The caching layer improves performance by reducing the need to repeatedly query the database for frequently accessed data.

### 7. **Post Moderation and Commenting**
- **Comment Moderation**: Comments are stored in **Redis** initially, just like posts and join requests, and later moved to **MySQL** for permanent storage. 
- The backend ensures that only the comment author and the post owner can reply, thus maintaining the privacy of conversations.

## Technologies Used

- **Spring Boot**: Framework for building the backend application and RESTful APIs.
- **JWT (JSON Web Tokens)**: Authentication is secured using JWT tokens, allowing stateless authentication for the users.
- **Spring Data JPA**: For interacting with **MySQL**, handling entities, and performing CRUD operations.
- **MySQL**: The database for persistent storage of data such as users, posts, comments, and join requests.
- **Redis**: In-memory data store used for caching temporary data like posts and join requests before they are persisted in the database.
- **Spring Security**: Provides security configurations for handling authentication and authorization.

## API Endpoints

### **Authentication**

- **POST /api/auth/login**
    - Logs in a user using their username and password.
    - Returns a **JWT** token upon successful login.

- **POST /api/auth/register**
    - Registers a new user with a username and password.

- **POST /api/auth/refresh**
    - Refreshes the **JWT** token for an authenticated user.

### **User Operations**

- **GET /api/users/{userId}**
    - Retrieves details of a specific user by their user ID.
  
- **PUT /api/users/{userId}**
    - Updates the details of a specific user by their user ID (e.g., username, email).

- **DELETE /api/users/{userId}**
    - Deletes the user from the platform by their user ID.

### **Post Operations**

- **GET /api/posts**
    - Retrieves a list of all posts.
  
- **POST /api/posts**
    - Allows a user to create a new post.
    - Requires user authentication.

- **GET /api/posts/{postId}**
    - Retrieves the details of a specific post by post ID.

- **PUT /api/posts/{postId}**
    - Allows a user to update a specific post by post ID.

- **DELETE /api/posts/{postId}**
    - Allows a user to delete a specific post by post ID.

### **Comment Operations**

- **GET /api/posts/{postId}/comments**
    - Retrieves all comments for a specific post.

- **POST /api/posts/{postId}/comments**
    - Allows a user to create a new comment on a post.
  
- **PUT /api/comments/{commentId}**
    - Allows a user to update a specific comment by comment ID.

- **DELETE /api/comments/{commentId}**
    - Allows a user to delete a specific comment by comment ID.

### **Community Operations**

- **GET /api/communities**
    - Lists all available communities.

- **POST /api/communities**
    - Allows the creation of a new community by an authorized user (admin).

- **GET /api/communities/{communityId}**
    - Retrieves the details of a specific community by community ID.

- **PUT /api/communities/{communityId}**
    - Updates the details of a specific community (e.g., name, description).

- **DELETE /api/communities/{communityId}**
    - Deletes a specific community by community ID (admin-only).

### **Join Request Operations**

- **POST /api/communities/{communityId}/join**
    - Allows a user to request to join a community.
  
- **GET /api/communities/{communityId}/join-requests**
    - Retrieves all pending join requests for a community.

- **PUT /api/communities/{communityId}/join-requests/{requestId}/approve**
    - Approves a join request for a community.
  
- **PUT /api/communities/{communityId}/join-requests/{requestId}/reject**
    - Rejects a join request for a community.

### **Moderation Operations**

- **GET /api/moderation/posts/pending**
    - Retrieves all posts pending approval.

- **PUT /api/moderation/posts/{postId}/approve**
    - Approves a post for public viewing.

- **PUT /api/moderation/posts/{postId}/reject**
    - Rejects a post, removing it from the platform.

- **GET /api/moderation/comments/pending**
    - Retrieves all comments pending approval.

- **PUT /api/moderation/comments/{commentId}/approve**
    - Approves a comment.

- **PUT /api/moderation/comments/{commentId}/reject**
    - Rejects a comment, removing it from the platform.

## Data Flow

1. **Post Creation**:
   - Posts are created and stored temporarily in Redis before being moved to the MySQL database for persistence.
   - Posts undergo moderation by community moderators before being made public.

2. **Comment Creation**:
   - Comments are also temporarily stored in Redis.
   - Only the post owner and the comment owner can reply to the comment, preventing spam.

3. **Join Request**:
   - Join requests are cached in Redis to reduce response time.
   - Once validated by a community moderator, the join request is moved to MySQL.

4. **User Authentication**:
   - Users authenticate with their username and password.
   - Upon successful authentication, a JWT token is issued for further requests.

## Conclusion

The **Post Platform Backend** provides a robust, scalable, and secure foundation for the **Post Platform**. By leveraging **Spring Boot**, **JWT**, **Redis**, and **MySQL**, this platform enables efficient user management, post creation, comment moderation, and community join requests, with the added benefit of caching to improve performance.

---

**Developer:** Halit
