package com.camel.integration.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import com.camel.integration.model.Employee;

@Component
public class EmployeeRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // REST Configuration
        restConfiguration()
            .component("netty-http")
            .host("0.0.0.0")
            .port(8080)
            .bindingMode(org.apache.camel.model.rest.RestBindingMode.json)
            .dataFormatProperty("prettyPrint", "true");

        // REST Endpoints
        rest("/employees")
            .consumes("application/json")
            .produces("application/json")

            // Create a new Employee
            .post()
                .type(Employee.class)
                .to("direct:createEmployee")

            // Get all Employees
            .get()
                .to("direct:getEmployees")

            // Get Employee by ID
            .get("/{id}")
                .to("direct:getEmployeeById")

            // Update Employee
            .put("/{id}")
                .type(Employee.class)
                .to("direct:updateEmployee")

            // Delete Employee
            .delete("/{id}")
                .to("direct:deleteEmployee");

        // Route implementations
        from("direct:createEmployee")
            .to("jpa://" + Employee.class.getName() + "?usePersist=true")
            .setHeader("CamelHttpResponseCode", constant(201));

        from("direct:getEmployees")
            .to("jpa://" + Employee.class.getName() + "?namedQuery=findAllEmployees")
            .marshal().json();

        from("direct:getEmployeeById")
            .toD("jpa://" + Employee.class.getName() + "?query=select e from Employee e where e.id = ${header.id}")
            .choice()
                .when(body().isNull())
                    .setHeader("CamelHttpResponseCode", constant(404))
                .otherwise()
                    .marshal().json()
            .endChoice();

        from("direct:updateEmployee")
            .process(exchange -> {
                Long id = Long.parseLong(exchange.getIn().getHeader("id", String.class));
                Employee newEmployeeData = exchange.getIn().getBody(Employee.class);

                // Retrieve existing employee from database
                Employee existingEmployee = exchange.getContext().createProducerTemplate()
                    .requestBodyAndHeader(
                        "jpa://" + Employee.class.getName() + "?query=select e from Employee e where e.id = :?id",
                        null,
                        "id",
                        id,
                        Employee.class
                    );

                if (existingEmployee != null) {
                    // Update fields
                    existingEmployee.setName(newEmployeeData.getName());
                    existingEmployee.setPosition(newEmployeeData.getPosition());
                    existingEmployee.setSalary(newEmployeeData.getSalary());
                    existingEmployee.setHireDate(newEmployeeData.getHireDate());

                    exchange.getIn().setBody(existingEmployee);
                } else {
                    // Handle not found
                    exchange.getIn().setHeader("CamelHttpResponseCode", 404);
                    exchange.getIn().setBody(null);
                }
            })
            .choice()
                .when(body().isNotNull())
                    .to("jpa://" + Employee.class.getName() + "?useMerge=true")
                    .setHeader("CamelHttpResponseCode", constant(200))
            .end();

        from("direct:deleteEmployee")
            .toD("jpa://" + Employee.class.getName() + "?nativeQuery=DELETE FROM employees WHERE id = ${header.id}")
            .setHeader("CamelHttpResponseCode", constant(204));
    }
}
