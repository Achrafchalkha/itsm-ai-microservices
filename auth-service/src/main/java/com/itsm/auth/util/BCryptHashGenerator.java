package com.itsm.auth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Utility class to generate and verify BCrypt hashes
 * Run this as a main method to generate hash for admin123
 */
public class BCryptHashGenerator {
    
    public static void main(String[] args) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        String plainPassword = "admin123";
        
        // Generate new hash
        String hash1 = passwordEncoder.encode(plainPassword);
        String hash2 = passwordEncoder.encode(plainPassword);
        String hash3 = passwordEncoder.encode(plainPassword);
        
        System.out.println("=== BCrypt Hash Generator for admin123 ===");
        System.out.println("Plain password: " + plainPassword);
        System.out.println();
        
        System.out.println("Generated Hash 1: " + hash1);
        System.out.println("Generated Hash 2: " + hash2);
        System.out.println("Generated Hash 3: " + hash3);
        System.out.println();
        
        // Test the old hash
        String oldHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTUDnDdYiDUiT4h7DcoFhfePOjKjPOIm";
        System.out.println("=== Verification Tests ===");
        System.out.println("Old hash: " + oldHash);
        System.out.println("Old hash matches 'admin123': " + passwordEncoder.matches(plainPassword, oldHash));
        System.out.println();
        
        // Test new hashes
        System.out.println("Hash 1 matches 'admin123': " + passwordEncoder.matches(plainPassword, hash1));
        System.out.println("Hash 2 matches 'admin123': " + passwordEncoder.matches(plainPassword, hash2));
        System.out.println("Hash 3 matches 'admin123': " + passwordEncoder.matches(plainPassword, hash3));
        System.out.println();
        
        // Test wrong password
        System.out.println("Hash 1 matches 'wrongpassword': " + passwordEncoder.matches("wrongpassword", hash1));
        System.out.println();
        
        System.out.println("=== SQL UPDATE COMMAND ===");
        System.out.println("Use this hash in your SQL:");
        System.out.println("UPDATE utilisateurs SET mot_de_passe_hashe = '" + hash1 + "' WHERE email = 'admin@itsm.com';");
        System.out.println();
        
        System.out.println("=== OR INSERT COMMAND ===");
        System.out.println("INSERT INTO utilisateurs (id, nom, prenom, email, mot_de_passe_hashe, role, actif, date_creation, date_modification)");
        System.out.println("VALUES (gen_random_uuid(), 'Admin', 'System', 'admin@itsm.com', '" + hash1 + "', 'ADMIN', true, NOW(), NOW());");
    }
}
