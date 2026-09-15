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

## V2 Results (improved) — TBD

| Benchmark | Input | V1 | V2 | Delta |
|---|---|---|---|---|
| Pipeline | 100,000 records | 39 ms | — | — |
| String report | 10,000 records | 205 ms | — | — |
