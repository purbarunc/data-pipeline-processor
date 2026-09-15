package com.purbarun.pipeline.model;

// V1 NAIVE: Mutable POJO — built up incrementally by AggregateStage via setters.
// V2 will replace with a record that is constructed atomically.
public class DepartmentSummary {

    private int departmentId;
    private String departmentName;
    private int employeeCount;
    private double averageSalary;
    private double minSalary;
    private double maxSalary;
    private double totalSalary;

    public DepartmentSummary() {}

    public DepartmentSummary(int departmentId, String departmentName) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
    }

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public int getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(int employeeCount) { this.employeeCount = employeeCount; }

    public double getAverageSalary() { return averageSalary; }
    public void setAverageSalary(double averageSalary) { this.averageSalary = averageSalary; }

    public double getMinSalary() { return minSalary; }
    public void setMinSalary(double minSalary) { this.minSalary = minSalary; }

    public double getMaxSalary() { return maxSalary; }
    public void setMaxSalary(double maxSalary) { this.maxSalary = maxSalary; }

    public double getTotalSalary() { return totalSalary; }
    public void setTotalSalary(double totalSalary) { this.totalSalary = totalSalary; }

    @Override
    public String toString() {
        return "DepartmentSummary{dept=" + departmentName + ", count=" + employeeCount
                + ", avgSalary=" + String.format("%.2f", averageSalary) + "}";
    }
}
