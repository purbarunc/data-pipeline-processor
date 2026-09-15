package com.purbarun.pipeline;

import java.util.List;
import java.util.Map;

import com.purbarun.pipeline.model.Department;
import com.purbarun.pipeline.model.DepartmentSummary;
import com.purbarun.pipeline.model.Employee;
import com.purbarun.pipeline.pipeline.DataPipeline;
import com.purbarun.pipeline.report.ReportGenerator;
import com.purbarun.pipeline.util.DataLoader;

// Run this class to capture V1 baseline numbers.
// Record the output — these are your "before" numbers for the Impact Story.
// V2 (improved) will run the same benchmark and show the delta.
//
// Expected V1 results (approximate):
//   Pipeline (100K records) : ~150–300 ms  (for-loops + intermediate lists)
//   String += (10K records) : ~2000–5000 ms (O(n²) heap churn)
public class Benchmark {

    private static final int PIPELINE_RECORD_COUNT = 100_000;
    private static final int STRING_RECORD_COUNT   =  10_000;

    public static void main(String[] args) {
        System.out.println("=== V1 Naive — Benchmark ===");
        System.out.println("Pipeline records : " + PIPELINE_RECORD_COUNT);
        System.out.println("String   records : " + STRING_RECORD_COUNT);
        System.out.println();

        // JVM warm-up (JIT compilation)
        System.out.println("[warm-up] Running...");
        runPipelineBenchmark(1_000,  true);
        runStringBenchmark(200, true);
        System.out.println("[warm-up] Done.\n");

        // --- Pipeline benchmark ---
        System.out.println("--- Pipeline Benchmark ---");
        long pipelineMs = runPipelineBenchmark(PIPELINE_RECORD_COUNT, false);
        System.out.println("V1 Pipeline time : " + pipelineMs + " ms");
        System.out.println();

        // --- String += benchmark ---
        System.out.println("--- String Concatenation Benchmark ---");
        long stringMs = runStringBenchmark(STRING_RECORD_COUNT, false);
        System.out.println("V1 String += time: " + stringMs + " ms");
        System.out.println();

        System.out.println("=== Record these numbers as your V1 baseline ===");
    }

    private static long runPipelineBenchmark(int count, boolean silent) {
        List<Employee>           employees   = DataLoader.generateEmployees(count);
        Map<Integer, Department> departments = DataLoader.getDepartments();

        if (silent) {
            // redirect stdout to suppress stage logs during warm-up
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
        List<DepartmentSummary> result = pipeline.execute(
                employees, true, 50_000.0, departments, "bench-tenant");
        long elapsed = System.nanoTime() - start;
        System.out.println("  Summaries produced: " + result.size());
        return elapsed / 1_000_000;
    }

    private static long runStringBenchmark(int count, boolean silent) {
        List<Employee> employees = DataLoader.generateEmployees(count);
        for (Employee emp : employees) {
            emp.setDepartmentName("Dept-" + emp.getDepartmentId()); // pre-fill dept name
        }

        ReportGenerator generator = new ReportGenerator();
        long start   = System.nanoTime();
        String report = generator.generateDetailedReport(employees);
        long elapsed  = System.nanoTime() - start;

        if (!silent) {
            System.out.println("  Report length: " + report.length() + " chars");
        }
        return elapsed / 1_000_000;
    }
}
