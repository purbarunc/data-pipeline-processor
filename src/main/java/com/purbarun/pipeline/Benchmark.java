package com.purbarun.pipeline;

import java.util.List;
import java.util.Map;

import com.purbarun.pipeline.model.Department;
import com.purbarun.pipeline.model.DepartmentSummary;
import com.purbarun.pipeline.model.Employee;
import com.purbarun.pipeline.pipeline.DataPipeline;
import com.purbarun.pipeline.report.ReportGenerator;
import com.purbarun.pipeline.util.DataLoader;

/// # Benchmark — Data Pipeline Processor
///
/// Measures wall-clock time for the two most impactful improvements in the V1 →
/// V2 journey. Run this after each step to track delta.
///
/// ## Step coverage
///
/// | Step | Description | Status |
/// |------|-------------------------------------|-------------------------------------------------|
/// | 1 | Project skeleton | ✓ DONE (Maven / Java 25) | | 2 | Model layer V2 | ✓
/// DONE (records + JEP 513 + generics + JEP 456) | | 3 | Exception handling V2 |
/// pending | | 4 | Stream API + Gatherers V2 | pending — will affect
/// **Pipeline** benchmark | | 5 | Functional interfaces + Scoped Values |
/// pending | | 6 | Optional | pending | | 7 | StringBuilder | pending — will
/// affect **String** benchmark |
///
/// ## Benchmarks tracked
///
/// - **Pipeline** — full filter → enrich → validate → aggregate on 100K records
/// - **String `+=`** — O(n²) report generation on 10K records (Step 7 target)
///
/// ## V1 baseline (GraalVM 25.0.1, Windows, 2026-09-14)
///
/// - Pipeline: **39 ms** - String `+=`: **205 ms**
///
/// See `BENCHMARKS.md` for full analysis and the cumulative V1 → V2 delta table.
///
/// > **Note:** Step 2 (records + immutability) does not change wall-clock
/// numbers — > its wins are compile-time safety and allocation pattern, not
/// throughput. > Measurable wall-clock improvements come in Steps 4
/// (Stream/Gatherers) and 7 (StringBuilder).
///
/// ## Java feature showcased in this file
///
/// - **JEP 467** (Markdown Documentation Comments, finalized Java 23): `///`
/// replaces `/** */` for all documentation. Content is CommonMark Markdown —
/// tables, bold, code spans, and blockquotes are all rendered natively
/// by `javadoc`.
public class Benchmark {

	private static final int PIPELINE_RECORD_COUNT = 100_000;
	private static final int STRING_RECORD_COUNT = 10_000;

	/// Entry point. Runs JVM warm-up, then the pipeline and string benchmarks in
	/// sequence.
	///
	/// Core algorithms are still V1-style (for-loops, `String +=`) until Steps 4 and
	/// 7. Update the printed heading when each subsequent step is completed.
	public static void main(String[] args) {
		System.out.println("=== Benchmark — Step 2 V2 (records + immutability; algorithms unchanged) ===");
		System.out.println("Pipeline records : " + PIPELINE_RECORD_COUNT);
		System.out.println("String   records : " + STRING_RECORD_COUNT);
		System.out.println();

		/// JVM warm-up — one round through each benchmark so the JIT compiles hot paths
		/// before the timed measurement. Output is suppressed via the `silent` flag.
		System.out.println("[warm-up] Running...");
		runPipelineBenchmark(1_000, true);
		runStringBenchmark(200, true);
		System.out.println("[warm-up] Done.\n");

		/// Pipeline benchmark — Steps 1–2 complete; for-loop algorithm still V1-style.
		/// Expected delta vs V1 baseline: negligible — record accessors are inlined by
		/// JIT. Will drop when Step 4 replaces for-loops with lazy Streams + Gatherers.
		System.out.println("--- Pipeline Benchmark (for-loop, V1 algorithm) ---");
		long pipelineMs = runPipelineBenchmark(PIPELINE_RECORD_COUNT, false);
		System.out.println("Pipeline time : " + pipelineMs + " ms  (baseline: ~39 ms)");
		System.out.println();

		/// String benchmark — Step 7 target: replace String += with StringBuilder. V2
		/// change: pre-enrichment uses `emp.withDepartmentName()` (immutable record
		/// copy) instead of `emp.setDepartmentName()` (mutable POJO setter). Same
		/// O(n²) algorithm.
		System.out.println("--- String Concatenation Benchmark (String +=, V1 algorithm) ---");
		long stringMs = runStringBenchmark(STRING_RECORD_COUNT, false);
		System.out.println("String += time : " + stringMs + " ms  (baseline: ~205 ms)");
		System.out.println();

		System.out.println("=== Compare against BENCHMARKS.md to track V1 → V2 delta ===");
	}

	/// Runs the full pipeline benchmark on `count` employee records.
	///
	/// **Pipeline stages:** `DataLoader` → `FilterStage` → `EnrichStage` →
	/// `ValidateStage` → `AggregateStage`
	///
	/// ### Step 2 V2 impact
	///
	/// - `Employee`, `Department`, and `DepartmentSummary` are now immutable
	///   records.
	/// - `EnrichStage` calls `emp.withDepartmentName()` (new record copy) instead of
	///   `setDepartmentName()`.
	/// - `AggregateStage` constructs `DepartmentSummary` atomically — all stats
	///   computed before `new`.
	/// - Record accessor calls (`emp.salary()`, `emp.active()`, ...) are inlined by
	///   JIT — zero overhead.
	///
	/// @param count  number of employee records to generate and process
	/// @param silent when `true`, suppresses stage log output (used during warm-up)
	/// @return elapsed time in milliseconds
	private static long runPipelineBenchmark(int count, boolean silent) {
		List<Employee> employees = DataLoader.generateEmployees(count);
		Map<Integer, Department> departments = DataLoader.getDepartments();

		if (silent) {
			/// Suppress stage log output during warm-up by redirecting stdout to a
			/// null sink.
			java.io.PrintStream original = System.out;
			System.setOut(new java.io.PrintStream(java.io.OutputStream.nullOutputStream()));
			DataPipeline pipeline = new DataPipeline();
			long start = System.nanoTime();
			pipeline.execute(employees, true, 50_000.0, departments, "bench-tenant");
			long elapsed = System.nanoTime() - start;
			System.setOut(original);
			return elapsed / 1_000_000;
		}

		DataPipeline pipeline = new DataPipeline();
		long start = System.nanoTime();
		List<DepartmentSummary> result = pipeline.execute(employees, true, 50_000.0, departments, "bench-tenant");
		long elapsed = System.nanoTime() - start;
		System.out.println("  Summaries produced: " + result.size());
		return elapsed / 1_000_000;
	}

	/// Runs the string concatenation benchmark on `count` employee records.
	///
	/// **Covers:** `generateDetailedReport()` in `ReportGenerator` — the `String +=`
	/// loop.
	///
	/// ### Step 2 V2 impact
	///
	/// Pre-enrichment now uses `emp.withDepartmentName()` (returns a new immutable
	/// record) instead of `emp.setDepartmentName()` (mutable POJO setter). The timed
	/// section — `generateDetailedReport()` — is intentionally unchanged: still
	/// `String +=` at O(n²).
	///
	/// ### Step 7 target
	///
	/// Replace `String +=` with `StringBuilder` → expected **~10–15× speedup**
	/// (~15–30 ms).
	///
	/// @param count  number of employee records to generate and report on
	/// @param silent when `true`, suppresses the report-length output line
	/// @return elapsed time in milliseconds
	private static long runStringBenchmark(int count, boolean silent) {
		/// V2 (Step 2): Employee is now immutable — `withDepartmentName()` returns a
		/// new record copy. V1 equivalent: `for (Employee emp : employees) {
		/// emp.setDepartmentName("Dept-" + emp.getDepartmentId()); }` This setup runs
		/// outside the timed block and has no effect on the measured time.
		List<Employee> employees = DataLoader.generateEmployees(count).stream()
			.map(emp -> emp.withDepartmentName("Dept-" + emp.departmentId())).toList();

		ReportGenerator generator = new ReportGenerator();
		long start = System.nanoTime();
		/// O(n²) String += — intentionally unchanged until Step 7
		String report = generator.generateDetailedReport(employees);
		long elapsed = System.nanoTime() - start;

		if (!silent) {
			System.out.println("  Report length: " + report.length() + " chars");
		}
		return elapsed / 1_000_000;
	}
}
