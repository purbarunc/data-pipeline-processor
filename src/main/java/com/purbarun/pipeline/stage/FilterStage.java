package com.purbarun.pipeline.stage;

import java.util.ArrayList;
import java.util.List;

import com.purbarun.pipeline.model.Employee;

// V1 NAIVE:
// - Uses a for-loop that materializes a full intermediate List in heap memory.
// - Business rules (onlyActive, salaryThreshold) are hardwired as method parameters.
//   Every caller must know what parameters to pass — no configurability.
// V2 will use lazy Stream + injected Predicate<Employee> to eliminate the intermediate list.
public class FilterStage {

    public List<Employee> process(List<Employee> employees, boolean onlyActive, double salaryThreshold) {
        List<Employee> result = new ArrayList<>(); // full intermediate list allocated upfront

        for (Employee emp : employees) {
            if (onlyActive && !emp.isActive()) {
                continue;
            }
            if (emp.getSalary() < salaryThreshold) {
                continue;
            }
            result.add(emp);
        }

        return result;
    }
}
