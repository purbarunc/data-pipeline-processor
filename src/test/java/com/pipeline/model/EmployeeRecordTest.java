package com.pipeline.model;

import org.junit.jupiter.api.Test;

import com.purbarun.pipeline.model.Employee;

import static org.junit.jupiter.api.Assertions.*;

/// # EmployeeRecordTest — V2 (Step 2, Java 25)
///
/// Covers: record immutability, compact constructor validation,
/// JEP 513 pre-construction check, and `withDepartmentName()` copy semantics.
class EmployeeRecordTest {

    /// --- Compact canonical constructor ---

    @Test
    void shouldThrowNullPointerExceptionForNullName() {
        /// The compact canonical constructor calls `Objects.requireNonNull(name)`.
        /// Null is rejected before the object is ever placed in heap.
        /// V1 flaw: null name silently propagated and caused NPE buried in ValidateStage's catch block.
        assertThrows(NullPointerException.class,
                () -> new Employee(1, null, 80_000, 1, true));
    }

    /// --- JEP 513 (Flexible Constructor Bodies, finalized Java 25) ---

    @Test
    void shouldThrowForNonPositiveId_JEP513() {
        /// The 5-arg constructor validates `id > 0` BEFORE calling `this()` — only legal in Java 25+.
        /// In Java 24, `if (id <= 0) throw ...` before `this()` was a compile error.
        assertThrows(IllegalArgumentException.class,
                () -> new Employee(0, "Alice", 80_000, 1, true));
        assertThrows(IllegalArgumentException.class,
                () -> new Employee(-1, "Alice", 80_000, 1, true));
    }

    @Test
    void shouldIncludeEmployeeNameInJEP513ErrorMessage() {
        /// Pre-construction validation can reference ALL constructor parameters for richer messages —
        /// impossible before JEP 513 because `this()` had to be first (no local variables allowed).
        var ex = assertThrows(IllegalArgumentException.class,
                () -> new Employee(0, "Alice", 80_000, 1, true));
        assertTrue(ex.getMessage().contains("Alice"),
                "Error message should include name for easier debugging");
    }

    /// --- Immutability ---

    @Test
    void shouldBeImmutable_withDepartmentNameReturnsNewInstance() {
        Employee original = new Employee(1, "Alice", 80_000, 1, true);
        Employee enriched = original.withDepartmentName("Engineering");

        /// Original is unchanged
        assertNull(original.departmentName(), "withDepartmentName must not mutate the original");
        /// Enriched copy has the new value
        assertEquals("Engineering", enriched.departmentName());
        /// All other fields are identical
        assertEquals(original.id(),           enriched.id());
        assertEquals(original.name(),         enriched.name());
        assertEquals(original.salary(),       enriched.salary(), 0.01);
        assertEquals(original.departmentId(), enriched.departmentId());
        assertEquals(original.active(),       enriched.active());
    }

    /// --- Record value-based equality ---

    @Test
    void shouldHaveValueBasedEquality() {
        /// records generate `equals()` that compares all components — no need to override.
        var e1 = new Employee(1, "Alice", 80_000, 1, true);
        var e2 = new Employee(1, "Alice", 80_000, 1, true);
        assertEquals(e1, e2, "Two records with identical components must be equal");
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenAnyComponentDiffers() {
        var base    = new Employee(1, "Alice", 80_000, 1, true);
        var diffId  = new Employee(2, "Alice", 80_000, 1, true);
        var diffSal = new Employee(1, "Alice", 90_000, 1, true);
        assertNotEquals(base, diffId);
        assertNotEquals(base, diffSal);
    }

    /// --- Record accessors (no `get` prefix) ---

    @Test
    void shouldExposeComponentsViaAccessors() {
        var emp = new Employee(7, "Bob", 65_000, 3, false, "Finance");
        assertEquals(7,         emp.id());
        assertEquals("Bob",     emp.name());
        assertEquals(65_000,    emp.salary(), 0.01);
        assertEquals(3,         emp.departmentId());
        assertFalse(             emp.active());
        assertEquals("Finance", emp.departmentName());
    }
}
