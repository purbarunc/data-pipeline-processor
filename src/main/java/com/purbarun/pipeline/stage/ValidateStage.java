package com.purbarun.pipeline.stage;

import java.util.ArrayList;
import java.util.List;

import com.purbarun.pipeline.model.Employee;

// V1 NAIVE:
// - Catches generic Exception and swallows it with printStackTrace().
//   In production: a NullPointerException on emp.getName() prints to stderr
//   and the record is silently dropped — zero traceability.
// - The `invalid` list is collected but never returned or reported — wasted allocation.
// - No way for the caller to know which records failed or why.
// V2 will use custom typed exceptions + exception chaining so root cause is never lost.
public class ValidateStage {

    public List<Employee> process(List<Employee> employees) {
        List<Employee> valid   = new ArrayList<>();
        List<Employee> invalid = new ArrayList<>(); // collected but never used — V1 flaw

        for (Employee emp : employees) {
            try {
                if (emp.getName().trim().isEmpty()) { // NPE if name is null — swallowed below!
                    invalid.add(emp);
                    continue;
                }
                if (emp.getSalary() <= 0) {
                    invalid.add(emp);
                    continue;
                }
                if (emp.getDepartmentId() <= 0) {
                    invalid.add(emp);
                    continue;
                }
                valid.add(emp);
            } catch (Exception e) {
                e.printStackTrace(); // V1: no context, no chaining, silent drop in production
            }
        }

        System.out.println("Validation: " + valid.size() + " valid, "
                + invalid.size() + " invalid (errors silently swallowed)");
        return valid;
    }
}
