package com.purbarun.pipeline.model;

import java.util.Objects;

/// # Department — V2 Improved (Step 2, Java 25)
///
/// **`record`** (finalized Java 16) replaces the mutable POJO.
///
/// Auto-generated: canonical constructor, `equals()`, `hashCode()`, `toString()`,
/// and typed accessors `id()`, `name()`, `location()`.
///
/// The compact constructor enforces that `name` is never null —
/// a `Department` without a name is structurally incoherent and should never reach any stage.
public record Department(int id, String name, String location) {

    /// Compact canonical constructor — rejects a null department name at construction time.
    public Department {
        Objects.requireNonNull(name, "Department name must not be null (id=" + id + ")");
    }
}
