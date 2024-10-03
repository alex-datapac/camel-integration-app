package com.camel.integration.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
@NamedQuery(name = "findAllEmployees", query = "SELECT e FROM Employee e")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String position;
    private BigDecimal salary;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    // Constructors
    public Employee() {}

    public Employee(String name, String position, BigDecimal salary, LocalDate hireDate) {
        this.name = name;
        this.position = position;
        this.salary = salary;
        this.hireDate = hireDate;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    // Add this setter
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

	public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

	public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }
}
