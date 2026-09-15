package com.purbarun.pipeline.model;

// V1 NAIVE: Mutable POJO with getters/setters — no validation, no immutability.
// Any stage can mutate this object at any point, creating subtle data corruption bugs.
// V2 will replace this with a Java record + Flexible Constructor Bodies (JEP 513).
public class Employee {

    private int id;
    private String name;
    private double salary;
    private int departmentId;
    private boolean active;
    private String departmentName; // mutated during EnrichStage

    public Employee() {}

    public Employee(int id, String name, double salary, int departmentId, boolean active) {
        this.id = id;
        this.name = name;
        this.salary = salary;
        this.departmentId = departmentId;
        this.active = active;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    @Override
    public String toString() {
        return "Employee{id=" + id + ", name=" + name + ", salary=" + salary
                + ", departmentId=" + departmentId + ", active=" + active
                + ", departmentName=" + departmentName + "}";
    }
}
