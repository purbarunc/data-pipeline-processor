package com.purbarun.pipeline.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.purbarun.pipeline.model.Department;
import com.purbarun.pipeline.model.Employee;

/// # DataLoader — Generates deterministic test data
///
/// Seeded with a fixed value (`42`) so every run produces identical record sets.
/// This makes benchmark output counts reproducible across runs; only timing varies.
public class DataLoader {

    private static final String[] FIRST_NAMES = {
        "Alice", "Bob", "Charlie", "Diana", "Eve", "Frank", "Grace", "Henry",
        "Irene", "Jack", "Karen", "Liam", "Mia", "Noah", "Olivia", "Peter"
    };
    private static final String[] LAST_NAMES = {
        "Smith", "Jones", "Brown", "Wilson", "Taylor", "Moore", "Jackson", "Martin",
        "Lee", "Harris", "Clark", "Lewis", "Robinson", "Walker", "Hall", "Allen"
    };

    private static final Random RANDOM = new Random(42);

    /// Generates `count` pseudo-random employee records.
    ///
    /// Salary range: 30,000–150,000. ~80% of records are active. Department ids: 1–5.
    ///
    /// The `catch (IllegalArgumentException _)` block demonstrates:
    ///
    /// **JEP 456 (Unnamed Variables, finalized Java 22):** `_` replaces `ignored` or `e`
    /// when the exception object is not referenced in the catch body. The compiler enforces
    /// that `_` is never used — unlike the convention-only `ignored` name.
    ///
    /// The generated data should never violate the record's `id > 0` invariant,
    /// but the defensive catch shows the pattern: skip and continue rather than abort.
    ///
    /// @param count number of employee records to generate
    /// @return mutable list of `Employee` records
    public static List<Employee> generateEmployees(int count) {
        List<Employee> employees = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String firstName = FIRST_NAMES[RANDOM.nextInt(FIRST_NAMES.length)];
            String lastName  = LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)];
            double salary    = 30_000 + RANDOM.nextDouble() * 120_000;
            int    deptId    = RANDOM.nextInt(5) + 1;
            boolean active   = RANDOM.nextDouble() > 0.2; // ~80% active
            try {
                employees.add(new Employee(i, firstName + " " + lastName, salary, deptId, active));
            } catch (IllegalArgumentException _) {
                /// JEP 456: `_` declares the exception intentionally unused.
                /// In production this would delegate to a structured logger instead of silently skipping.
            }
        }
        return employees;
    }

    /// Returns the five reference departments used throughout the pipeline.
    ///
    /// @return unmodifiable-compatible map keyed by department id
    public static Map<Integer, Department> getDepartments() {
        Map<Integer, Department> departments = new HashMap<>();
        departments.put(1, new Department(1, "Engineering",  "Bangalore"));
        departments.put(2, new Department(2, "Marketing",    "Mumbai"));
        departments.put(3, new Department(3, "Finance",      "Delhi"));
        departments.put(4, new Department(4, "HR",           "Chennai"));
        departments.put(5, new Department(5, "Operations",   "Hyderabad"));
        return departments;
    }
}
