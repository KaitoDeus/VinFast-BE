package com.oem.evwarranty;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the OEM EV Warranty Management System.
 * This system manages warranty claims between Service Centers and EV
 * Manufacturers.
 */
@SpringBootApplication
public class EvWarrantyApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvWarrantyApplication.class, args);
    }
}


