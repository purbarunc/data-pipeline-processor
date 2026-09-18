# Benchmarks — Data Pipeline Processor

Environment: Java 25.0.1 (Oracle GraalVM 25.0.1+8, LTS), Windows, Maven 3.9.9.

## V1 Baseline (naive) — captured 2026-09-14

| Benchmark | Input | Time | Notes |
|---|---|---|---|
| Pipeline | 100,000 records | **39 ms** | 66,763 passed filter (active && salary >= 50K) → 5 department summaries |
| String `+=` report | 10,000 records | **205 ms** | 333,614-char report |

Raw output:

```
=== V1 Naive — Benchmark ===
Pipeline records : 100000
String   records : 10000

--- Pipeline Benchmark ---
[bench-tenant] After filter:    66763 records
[bench-tenant] After enrich:    66763 records
Validation: 66763 valid, 0 invalid (errors silently swallowed)
[bench-tenant] After validate:  66763 records
[bench-tenant] After aggregate: 5 department summaries
  Summaries produced: 5
V1 Pipeline time : 39 ms

--- String Concatenation Benchmark ---
  Report length: 333614 chars
V1 String += time: 205 ms
```

## Analysis

- **Pipeline (39 ms):** for-loops over `ArrayList` are not slow in wall-clock time — the V1 cost is allocation churn (5 intermediate lists of ~67K elements) and code quality. V2 comparison should also measure GC pressure (`-Xlog:gc`), not just `nanoTime`. Wall-clock gains for the pipeline are expected from Virtual Threads parallel stages, not streams alone.
- **String `+=` (205 ms):** O(n²) copying — ~1.65 billion char copies (~3.3 GB byte traffic) for 10K lines. Report generation on 1/10th the data took ~5x longer than the entire 100K-record pipeline.
- **Reproducibility:** data outputs are deterministic across runs (identical record counts and report length); timings vary within JIT/GC noise (observed 39–41 ms pipeline, 205–249 ms string).
- **V2 expectation:** `StringBuilder` removes the O(n²) copy, but `String.format("%.2f", ...)` per line remains (~1–2 µs each). Realistic V2 estimate: ~15–30 ms (~10–15x speedup).

## Step 2 V2 — records + immutability (captured 2026-09-18)

Model layer replaced with immutable records (JEP 513, JEP 456, bounded generics).
Core algorithms unchanged — for-loops and String `+=` are still V1-style until Steps 4 and 7.

| Benchmark | Input | V1 baseline | Step 2 V2 | Delta | Notes |
|---|---|---|---|---|---|
| Pipeline | 100,000 records | 39 ms | **32 ms** | −7 ms (−18%) | Within JIT/GC noise; record accessors inlined by JVM |
| String `+=` report | 10,000 records | 205 ms | **234 ms** | +29 ms (+14%) | Within JIT/GC noise; O(n²) algorithm unchanged |

Raw output:

```
=== Benchmark — Step 2 V2 (records + immutability; algorithms unchanged) ===
Pipeline records : 100000
String   records : 10000

--- Pipeline Benchmark (for-loop, V1 algorithm) ---
[bench-tenant] After filter:    66763 records
[bench-tenant] After enrich:    66763 records
Validation: 66763 valid, 0 invalid (errors silently swallowed)
[bench-tenant] After validate:  66763 records
[bench-tenant] After aggregate: 5 department summaries
  Summaries produced: 5
Pipeline time : 32 ms  (baseline: ~39 ms)

--- String Concatenation Benchmark (String +=, V1 algorithm) ---
  Report length: 333614 chars
String += time : 234 ms  (baseline: ~205 ms)
```

### Interpretation

- **Pipeline (32 ms vs 39 ms):** The −18% is normal JIT/GC run-to-run variance, not a genuine Step 2 speedup. Record accessor calls (`emp.salary()`, `emp.active()` etc.) are inlined by the JVM and carry zero overhead vs POJO getters. The `withDepartmentName()` immutable copy in EnrichStage allocates one extra `Employee` object per enriched record (~67K extra short-lived objects on 100K input), but GraalVM's GC reclaims these cheaply. No meaningful wall-clock change is expected from Step 2 — that is by design.
- **String `+=` (234 ms vs 205 ms):** The +14% is also within noise (observed range 205–249 ms across all runs). The O(n²) algorithm and the `String.format()` call per line are identical to V1. The only V2 change here is `withDepartmentName()` used during setup — that is outside the timed block and has no effect on the measured time.
- **Key Step 2 wins are not wall-clock:** compile-time type safety via `PipelineStage<T>`, construction-time rejection of null names and non-positive ids (JEP 513), and elimination of shared mutable state in EnrichStage. These appear in code quality and test coverage, not in milliseconds.
- **Next measurable deltas:** Step 4 (Stream Gatherers) will reduce pipeline allocation churn; Step 7 (StringBuilder) will reduce String time by ~10–15×.

## Cumulative V1 → V2 delta table (updated per step)

| Step | What changed | Pipeline delta | String delta |
|---|---|---|---|
| V1 baseline | — | 39 ms | 205 ms |
| Step 2 — records + immutability | model POJOs → records, JEP 513, generics | ±noise (32–41 ms) | ±noise (205–249 ms) |
| Step 3 — exception handling | pending | — | — |
| Step 4 — Stream + Gatherers | pending | expected ↓ | — |
| Step 5 — Functional + Scoped Values | pending | — | — |
| Step 6 — Optional | pending | — | — |
| Step 7 — StringBuilder | pending | — | expected ↓↓ |
