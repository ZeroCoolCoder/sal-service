package com.sal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Storage Abstraction Layer (SAL) Service.
 * 
 * Provides a unified API for storing, retrieving, versioning, searching,
 * auditing, and governing binary content independently of the underlying
 * storage technology.
 */
@SpringBootApplication
public class SalApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalApplication.class, args);
    }
}
