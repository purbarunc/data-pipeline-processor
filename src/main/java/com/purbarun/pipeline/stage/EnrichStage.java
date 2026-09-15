package com.purbarun.pipeline.stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.purbarun.pipeline.model.Department;
import com.purbarun.pipeline.model.Employee;

// V1 NAIVE:
// - 5 nested null checks just to set one field — "Pyramid of Doom" anti-pattern.
// - departmentMap passed as a method parameter, forcing every caller to supply it.
//   In a deeper call chain this becomes an unbearable threading problem.
// - Mutates the Employee object in place (relies on setters) — no immutability.
// V2 will use Optional<T> chaining + Scoped Values (JEP 506) for the map.
public class EnrichStage {

    public List<Employee> process(List<Employee> employees, Map<Integer, Department> departmentMap) {
        List<Employee> result = new ArrayList<>();

        for (Employee emp : employees) {
            if (emp != null) {
                if (departmentMap != null) {
                    Department dept = departmentMap.get(emp.getDepartmentId());
                    if (dept != null) {
                        if (dept.getName() != null) {
                            emp.setDepartmentName(dept.getName());
                        } else {
                            emp.setDepartmentName("Unknown");
                        }
                    } else {
                        emp.setDepartmentName("Unknown");
                    }
                } else {
                    emp.setDepartmentName("Unknown");
                }
                result.add(emp);
            }
        }

        return result;
    }
}
