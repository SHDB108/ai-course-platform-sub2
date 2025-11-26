package com.example.aicourse.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility to generate BCrypt password hashes.
 * Run this class to generate hashed passwords for your database.
 *
 * Usage: Run the main method and it will print the BCrypt hash
 *        that you can use in SQL UPDATE statements.
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Generate hash for password "123456"
        String plainPassword = "123456";
        String hashedPassword = encoder.encode(plainPassword);

        System.out.println("=================================================");
        System.out.println("Password Encoding for Database");
        System.out.println("=================================================");
        System.out.println("Plain text password: " + plainPassword);
        System.out.println("BCrypt hashed password: " + hashedPassword);
        System.out.println("\nCopy the SQL below and run it in your MySQL database:");
        System.out.println("=================================================");
        System.out.println("UPDATE t_user SET password = '" + hashedPassword + "' WHERE username = 'student';");
        System.out.println("=================================================");

        // Verify the hash works
        boolean matches = encoder.matches(plainPassword, hashedPassword);
        System.out.println("\nVerification: " + (matches ? "✓ Hash is correct" : "✗ Hash failed"));

        // You can add more passwords here
        System.out.println("\n\nOther common passwords:");
        String[] passwords = {"admin123", "teacher123"};
        for (String pwd : passwords) {
            String hash = encoder.encode(pwd);
            System.out.println("Password: " + pwd);
            System.out.println("Hash: " + hash);
            System.out.println();
        }
    }
}
