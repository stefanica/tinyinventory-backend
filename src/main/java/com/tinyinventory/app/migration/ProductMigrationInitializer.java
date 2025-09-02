package com.tinyinventory.app.migration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/***IMPORTANT: Used the example below to run scripts and modify the DB, when needed **/

@Component
public class ProductMigrationInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public ProductMigrationInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        // Drop old columns price, quantity, IF they exist
        try {
            jdbcTemplate.execute("ALTER TABLE products DROP COLUMN IF EXISTS price");
            jdbcTemplate.execute("ALTER TABLE products DROP COLUMN IF EXISTS quantity");
            //System.out.println("[DB MIGRATION] Dropped old columns: price, quantity");
        } catch (Exception e) {
            System.err.println("[DB MIGRATION] Error dropping old columns: " + e.getMessage());
        }

        // Set default value for price_out where null
        try {
            jdbcTemplate.update("UPDATE products SET price_out = 0 WHERE price_out IS NULL");
        } catch (Exception e) {
            System.err.println("[DB MIGRATION] Error updating price_out: " + e.getMessage());
        }

        // Set default value for old rows where it's NULL
        try {
            jdbcTemplate.update("UPDATE products SET quantity_threshold = 0 WHERE quantity_threshold IS NULL");
        } catch (Exception e) {
            System.err.println("[DB MIGRATION] Error updating quantity_threshold: " + e.getMessage());
        }

        //Remove updated_at Column from ProductHistory Entity
        try {
            jdbcTemplate.execute("ALTER TABLE product_history DROP COLUMN IF EXISTS updated_at");
        } catch (Exception e) {
            System.err.println("[DB MIGRATION] Error dropping old columns: " + e.getMessage());
        }
    }
}
