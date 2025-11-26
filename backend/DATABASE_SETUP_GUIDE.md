# Spring Boot Application - Switch from Mock to Real Database

## Overview
This guide will help you switch your Spring Boot application from mock mode to using your real MySQL database.

---

## What Was Changed

### 1. **New File: DatabaseUserDetailsConfig.java**
   - Location: `src/main/java/com/example/aicourse/config/DatabaseUserDetailsConfig.java`
   - Purpose: Provides UserDetailsService that loads users from MySQL database
   - Active when: NOT in mock mode (`@Profile("!mock")`)

### 2. **Updated: application.yml**
   - Changed `spring.profiles.active` from `mock` to `dev`
   - Updated default database password to `123456` (from `12345678`)
   - Added MySQL connection parameters for better compatibility

### 3. **New File: application-dev.yml**
   - Location: `src/main/resources/application-dev.yml`
   - Purpose: Development-specific configuration (logging, debugging)
   - Automatically loaded when `spring.profiles.active=dev`

### 4. **New File: PasswordHashGenerator.java**
   - Location: `src/main/java/com/example/aicourse/utils/PasswordHashGenerator.java`
   - Purpose: Utility to generate BCrypt password hashes
   - Usage: Run this to generate hashes for new passwords

### 5. **New File: update_passwords.sql**
   - Location: `backend/update_passwords.sql`
   - Purpose: SQL script to update database passwords to BCrypt format
   - **IMPORTANT**: You MUST run this before testing login!

---

## Step-by-Step Setup Instructions

### Step 1: Verify Database Configuration

Check your MySQL database settings and update `application.yml` if needed:

```yaml
spring:
  datasource:
    # If your database is NOT named 'aicourse', change it here
    url: jdbc:mysql://localhost:3306/aicourse?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true

    # If your MySQL username is NOT 'root', change it here
    username: root

    # If your MySQL password is NOT '123456', change it here
    password: 123456
```

**Common customizations:**
- Database name: Replace `aicourse` with your database name (e.g., `my_db_name`)
- MySQL port: If using a different port, change `3306`
- Password: Update to match your MySQL root password

### Step 2: Update Database Passwords to BCrypt Format

**CRITICAL**: Spring Security requires BCrypt-hashed passwords, not plain text!

**Option A: Use the provided SQL script (Recommended)**

Run this in your MySQL client:

```bash
mysql -u root -p < backend/update_passwords.sql
```

Or open MySQL Workbench and execute the SQL manually:

```sql
UPDATE t_user
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMye7UgEqXfh0lh7HS7gzV7EO3aDKmwqJuu'
WHERE username = 'student';
```

**Option B: Generate a new hash**

1. Run `PasswordHashGenerator.java` (right-click → Run in your IDE)
2. Copy the generated UPDATE statement
3. Run it in MySQL

**Verify the update worked:**

```sql
SELECT username, LEFT(password, 20) as password_preview, LENGTH(password) as pwd_length
FROM t_user
WHERE username = 'student';
```

Expected result:
- `pwd_length` = 60
- `password_preview` starts with `$2a$10$`

### Step 3: Verify Your Database Schema

Make sure your `t_user` table has the correct structure:

```sql
-- Check table structure
DESC t_user;

-- Check existing data
SELECT id, username, password, email, role, status
FROM t_user
WHERE username = 'student';
```

**Expected columns:**
- `id` (BIGINT, Primary Key)
- `username` (VARCHAR)
- `password` (VARCHAR, length >= 60 for BCrypt)
- `email` (VARCHAR, nullable)
- `phone` (VARCHAR, nullable)
- `role` (VARCHAR) - Should be 'STUDENT', 'TEACHER', or 'ADMIN'
- `status` (INT) - 1 = ACTIVE, 0 = INACTIVE
- `gmt_create` (DATETIME)
- `gmt_modified` (DATETIME)
- `last_login_time` (DATETIME, nullable)

**IMPORTANT**: Make sure `status = 1` for active users!

```sql
UPDATE t_user SET status = 1 WHERE username = 'student';
```

### Step 4: Check Student Table Data

Verify your student record exists and is linked correctly:

```sql
SELECT s.*, u.username, u.role
FROM t_student s
LEFT JOIN t_user u ON s.user_id = u.id
WHERE s.stu_no = '2023001';
```

Make sure:
- Student "张三" exists with ID = 1
- `stu_no` = '2023001'
- `user_id` is correctly linked to the user table

### Step 5: Restart Your Application

1. **Stop** the running Spring Boot application (if it's running)
2. **Rebuild** the project (in your IDE or with Maven):
   ```bash
   mvn clean package -DskipTests
   ```
3. **Start** the application:
   ```bash
   java -jar target/your-app-name.jar
   # OR run from your IDE (AICourseApplication.java main method)
   ```

### Step 6: Test Login

**Test 1: Login with real credentials**

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "student",
    "password": "123456"
  }'
```

**Expected response:**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "student",
    "userId": 1,
    "role": "STUDENT"
  }
}
```

**Test 2: Verify mock password no longer works**

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "student",
    "password": "mock-password"
  }'
```

**Expected response:**
```json
{
  "code": 500,
  "msg": "Invalid username or password"
}
```

### Step 7: Check Dashboard Data

After successful login, access the student dashboard:

```bash
curl -X GET http://localhost:8080/api/v1/student/dashboard \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

You should now see:
- Real course data from the database
- Student information for "张三"
- Enrolled courses
- Learning progress
- Real data instead of empty lists

---

## Troubleshooting

### Problem: "Invalid username or password" error

**Possible causes:**
1. Password in database is not BCrypt-encoded
   - **Solution**: Run `update_passwords.sql` again

2. Wrong database password in `application.yml`
   - **Solution**: Check MySQL password and update configuration

3. User status is not active (`status != 1`)
   - **Solution**: Run `UPDATE t_user SET status = 1 WHERE username = 'student';`

### Problem: "User not found: student"

**Possible causes:**
1. User doesn't exist in database
   - **Solution**: Insert user record:
   ```sql
   INSERT INTO t_user (username, password, role, status, gmt_create, gmt_modified)
   VALUES ('student', '$2a$10$N9qo8uLOickgx2ZMRZoMye7UgEqXfh0lh7HS7gzV7EO3aDKmwqJuu', 'STUDENT', 1, NOW(), NOW());
   ```

2. Database connection failed
   - **Solution**: Check `application.yml` datasource settings
   - Verify MySQL is running: `mysql -u root -p`

### Problem: Dashboard is still empty

**Possible causes:**
1. Student record not linked to user
   - **Solution**: Check foreign key relationship
   ```sql
   SELECT * FROM t_student WHERE user_id = (SELECT id FROM t_user WHERE username = 'student');
   ```

2. No course enrollment data
   - **Solution**: Check `t_course_student` table
   ```sql
   SELECT * FROM t_course_student WHERE student_id = 1;
   ```

3. KnowledgeGraphClient still using mock
   - **Solution**: Verify profile is NOT 'mock' in logs
   - Check startup log for: "The following profiles are active: dev"

### Problem: Application won't start

**Check for:**
1. Database connection errors in logs
2. Redis connection errors (if Redis is not running, comment out Redis config temporarily)
3. Port 8080 already in use

**View logs:**
```bash
tail -f backend.log
# OR check console output when running from IDE
```

---

## Verification Checklist

✅ **Configuration:**
- [ ] `application.yml` has `spring.profiles.active: dev` (NOT `mock`)
- [ ] Database URL, username, and password are correct
- [ ] Database name matches your actual MySQL database

✅ **Database:**
- [ ] User 'student' exists in `t_user` table
- [ ] Password is BCrypt-hashed (60 characters, starts with `$2a$10$`)
- [ ] User status = 1 (ACTIVE)
- [ ] Student record exists in `t_student` table
- [ ] `user_id` foreign key correctly links student to user

✅ **Application:**
- [ ] Application starts without errors
- [ ] Startup log shows: "The following profiles are active: dev"
- [ ] No "mock" in active profiles
- [ ] Can login with username 'student' and password '123456'
- [ ] JWT token is returned
- [ ] Dashboard returns real data (not empty)

---

## Next Steps

Once everything works:

1. **Add more users:** Use `PasswordHashGenerator.java` to create BCrypt hashes for new users

2. **Create a production profile:** Create `application-prod.yml` for production settings

3. **Secure your configuration:** Move sensitive data to environment variables:
   ```bash
   export DB_URL="jdbc:mysql://localhost:3306/aicourse"
   export DB_USER="root"
   export DB_PASSWORD="your-secure-password"
   export JWT_SECRET="your-long-random-secret-key"
   ```

4. **Enable Redis caching:** Make sure Redis is running if you're using caching features

5. **Test other features:** Try course enrollment, task submission, video progress tracking, etc.

---

## Quick Reference: Configuration Files

```
backend/
├── src/main/resources/
│   ├── application.yml              # Main config (profile = dev, database settings)
│   └── application-dev.yml          # Dev-specific config (logging, debugging)
├── src/main/java/com/example/aicourse/config/
│   ├── DatabaseUserDetailsConfig.java  # Real database auth (active in dev)
│   ├── MockUserDetailsConfig.java      # Mock auth (active only in mock profile)
│   └── SecurityConfig.java             # Security rules (always active)
└── update_passwords.sql             # SQL to fix passwords
```

---

## Support

If you encounter issues:

1. Check the **Troubleshooting** section above
2. Review application logs: `backend.log` or console output
3. Verify database connectivity: `mysql -u root -p -e "USE aicourse; SHOW TABLES;"`
4. Confirm Spring profile: Look for "The following profiles are active" in startup logs

---

**Last Updated:** 2025-11-25
