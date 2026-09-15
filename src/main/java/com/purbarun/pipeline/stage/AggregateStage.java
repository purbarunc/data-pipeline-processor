package com.purbarun.pipeline.stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.purbarun.pipeline.model.DepartmentSummary;
import com.purbarun.pipeline.model.Employee;

// V1 NAIVE:
// - Three separate for-loops, each materializing intermediate data structures in heap.
//   Loop 1: groups employees into a Map<Integer, List<Employee>> — full copy in memory.
//   Loop 2: iterates the map entries.
//   Loop 3: iterates each department's employee list to compute stats.
// - DepartmentSummary is built incrementally via setters — partially constructed object exists.
// V2 will use Stream.collect(groupingBy + summarizingDouble) to do this in one lazy pass.
public class AggregateStage {

    public List<DepartmentSummary> process(List<Employee> employees) {

        // Loop 1: group by department — intermediate Map allocated in heap
        Map<Integer, List<Employee>> byDepartment = new HashMap<>();
        for (Employee emp : employees) {
            int deptId = emp.getDepartmentId();
            if (byDepartment.get(deptId) == null) {
                byDepartment.put(deptId, new ArrayList<>());
            }
            byDepartment.get(deptId).add(emp);
        }

        // Loop 2 + Loop 3: compute stats per department
        List<DepartmentSummary> summaries = new ArrayList<>();

        for (Map.Entry<Integer, List<Employee>> entry : byDepartment.entrySet()) {
            int deptId = entry.getKey();
            List<Employee> deptEmployees = entry.getValue();

            DepartmentSummary summary = new DepartmentSummary(
                    deptId,
                    deptEmployees.get(0).getDepartmentName() // V1 flaw: assumes enrichment ran
            );

            double totalSalary = 0;
            double minSalary   = Double.MAX_VALUE;
            double maxSalary   = Double.MIN_VALUE;

            for (Employee emp : deptEmployees) { // Loop 3: another full iteration
                totalSalary += emp.getSalary();
                if (emp.getSalary() < minSalary) minSalary = emp.getSalary();
                if (emp.getSalary() > maxSalary) maxSalary = emp.getSalary();
            }

            summary.setEmployeeCount(deptEmployees.size());
            summary.setTotalSalary(totalSalary);
            summary.setAverageSalary(totalSalary / deptEmployees.size());
            summary.setMinSalary(minSalary);
            summary.setMaxSalary(maxSalary);

            summaries.add(summary);
        }

        return summaries;
    }
}
