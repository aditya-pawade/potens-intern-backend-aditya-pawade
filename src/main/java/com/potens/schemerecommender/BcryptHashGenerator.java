package com.potens.schemerecommender;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * One-time utility to generate BCrypt hashes for seed users.
 * Run this main method, copy output to V2__seed_users.sql, then delete this file.
 */
public class BcryptHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("admin123 -> " + encoder.encode("admin123"));
        System.out.println("user123  -> " + encoder.encode("user123"));
    }
}
