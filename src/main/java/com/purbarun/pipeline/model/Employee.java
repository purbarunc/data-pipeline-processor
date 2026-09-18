package com.purbarun.pipeline.model;

import java.util.Objects;

/// # Employee — V2 Improved (Step 2, Java 25)
///
/// ## Features introduced
///
/// 1. **`record`** (finalized Java 16):
///    Immutable by default — compiler generates canonical constructor, `equals()`, `hashCode()`,
///    `toString()`, and typed accessors automatically. Zero boilerplate.
///    Any stage that previously mutated this object silently via setters can no longer do so.
///
/// 2. **Compact canonical constructor** (validation block in the record header):
///    Enforces the structural invariant that `name` can never be null.
///    Runs automatically for EVERY construction path, including `withDepartmentName()`.
///    Business-rule checks (blank name, salary ≤ 0, deptId ≤ 0) stay in `ValidateStage` —
///    records enforce structural correctness; stages enforce domain rules.
///
/// 3. **JEP 513** (Flexible Constructor Bodies, finalized Java 25) — 5-arg convenience constructor:
///    In Java 24 and earlier, `this()` MUST be the very first statement in a delegating
///    constructor. Putting ANY statement before `this()` was a compile error.
///    Java 25 lifts that restriction: pre-construction statements are now legal.
///    Here we validate `id > 0` with a descriptive error message (including the name) BEFORE
///    delegating to the canonical constructor — impossible to express in Java < 25.
///
/// 4. **`withDepartmentName(String)`**:
///    Replaces `EnrichStage`'s `emp.setDepartmentName()` call.
///    Returns a new record; the original is untouched — no more shared mutable state.
///
/// ## Impact story
///
/// > "I replaced mutable POJOs with Java 25 records using Flexible Constructor Bodies (JEP 513)
/// > for pre-construction validation, which eliminated ClassCastException risks at compile time
/// > and prevented invalid `Employee` objects from ever being half-constructed."
public record Employee(
        int     id,
        String  name,
        double  salary,
        int     departmentId,
        boolean active,
        String  departmentName) {

    /// Compact canonical constructor — structural invariant.
    ///
    /// Runs for every construction path. Only enforces what is ALWAYS true:
    /// an `Employee` with a null name is structurally incoherent.
    public Employee {
        Objects.requireNonNull(name, "Employee name must not be null (id=" + id + ")");
    }

    /// Convenience constructor — builds an un-enriched `Employee` (departmentName not yet known).
    ///
    /// **JEP 513 (Flexible Constructor Bodies, finalized Java 25):**
    /// The `if (id <= 0)` block is BEFORE `this()` — only legal in Java 25+.
    /// In Java 24 the compiler would reject this with:
    /// `"call to this must be first statement in constructor"`.
    /// Now we can fail-fast with full context (id AND name) before the canonical constructor
    /// ever executes, so an invalid `Employee` object never enters a half-constructed state.
    public Employee(int id, String name, double salary, int departmentId, boolean active) {
        /// Pre-construction validation — only possible since JEP 513 (Java 25)
        if (id <= 0) {
            throw new IllegalArgumentException(
                "Employee id must be positive; got " + id + " (name=" + name + ")");
        }
        this(id, name, salary, departmentId, active, null);
    }

    /// Immutable enrichment — returns a new `Employee` record with `departmentName` set.
    ///
    /// `this` is unchanged. Replaces V1's `emp.setDepartmentName()` which mutated shared state
    /// in-place and made it unsafe for multiple stages to hold references to the same object.
    public Employee withDepartmentName(String deptName) {
        return new Employee(id, name, salary, departmentId, active, deptName);
    }
}
