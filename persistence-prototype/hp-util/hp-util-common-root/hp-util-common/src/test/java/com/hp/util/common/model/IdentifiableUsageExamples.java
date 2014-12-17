/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.model;

import java.io.Serializable;

import com.hp.util.common.type.Id;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "unused" })
public class IdentifiableUsageExamples {

    private static class Example1 {

        /*
         * No inheritance
         */

        private static void usageExample() {
            Id<Person, Long> id = Id.valueOf(Long.valueOf(1));
            Person person = new Person(id);

            Id<Person, Long> personId = person.getId();
        }

        private static final class Person extends AbstractIdentifiable<Person, Long> {
            private static final long serialVersionUID = 1L;

            protected Person(Id<? extends Person, Long> id) {
                super(id);
            }
        }
    }

    private static class Example2 {

        /*
         * Compatible branches in the hierarchy
         */

        private static void usageExample() {
            Id<Employee, Long> id = Id.valueOf(Long.valueOf(1));
            Employee employee = new Employee(id);

            Id<Employee, Long> employeeId = employee.getId();
            Id<Person, Long> personId = employee.getId();
            Id<Contractor, Long> contractorId = employee.getId();

        }

        private static abstract class Person extends AbstractIdentifiable<Person, Long> {
            private static final long serialVersionUID = 1L;

            protected Person(Id<? extends Person, Long> id) {
                super(id);
            }
        }

        private static final class Employee extends Person {
            private static final long serialVersionUID = 1L;

            protected Employee(Id<Employee, Long> id) {
                super(id);
            }
        }

        private static final class Contractor extends Person {
            private static final long serialVersionUID = 1L;

            protected Contractor(Id<Contractor, Long> id) {
                super(id);
            }
        }
    }

    private static class Example3 {

        /*
         * Incompatible branches in the hierarchy
         */

        private static void usageExample() {
            Id<Employee, Long> id = Id.valueOf(Long.valueOf(1));
            Employee employee = new Employee(id);

            Id<Employee, Long> employeeId = employee.getId();
            // Bound mismatch error:
            // Id<Person<Employee, Long>, Long> personId = employee.getId();
            // Bound mismatch error:
            // Id<Person<Person, Long>, Long> personId = employee.getId();
            // Bound mismatch error:
            // Id<Costumer, Long> costumerId = employee.getId();

            Id<Costumer, Long> costumerId = Id.valueOf(Long.valueOf(1));
            Costumer costumer = new Costumer(costumerId);
            costumerId = costumer.getId();
            // Bound mismatch error:
            // Id<Employee, Long> employeeId = costumer.getId();
        }

        private static abstract class Person<T extends Person<T>> extends AbstractIdentifiable<T, Long> {
            private static final long serialVersionUID = 1L;

            protected Person(Id<? extends T, Long> id) {
                super(id);
            }
        }

        private static final class Employee extends Person<Employee> {
            private static final long serialVersionUID = 1L;

            protected Employee(Id<Employee, Long> id) {
                super(id);
            }
        }

        private static final class Costumer extends Person<Costumer> {
            private static final long serialVersionUID = 1L;

            protected Costumer(Id<Costumer, Long> id) {
                super(id);
            }
        }
    }

    private static class Example4 {

        /*
         * Incompatible branches in the hierarchy (General abstract class)
         */

        private static void usageExample() {
            Id<Employee, Long> id = Id.valueOf(Long.valueOf(1));
            Employee employee = new Employee(id);

            Id<Employee, Long> employeeId = employee.getId();
            // Bound mismatch error:
            // Id<Person<Employee, Long>, Long> personId = employee.getId();
            // Bound mismatch error:
            // Id<Person<Person, Long>, Long> personId = employee.getId();
            // Bound mismatch error:
            // Id<Costumer, Long> costumerId = employee.getId();

            Id<Costumer, Long> costumerId = Id.valueOf(Long.valueOf(1));
            Costumer costumer = new Costumer(costumerId);
            costumerId = costumer.getId();
            // Bound mismatch error:
            // Id<Employee, Long> employeeId = costumer.getId();
        }

        private static abstract class Person<T extends Person<T, I>, I extends Serializable> extends
                AbstractIdentifiable<T, I> {
            private static final long serialVersionUID = 1L;

            protected Person(Id<? extends T, I> id) {
                super(id);
            }
        }

        private static final class Employee extends Person<Employee, Long> {
            private static final long serialVersionUID = 1L;

            protected Employee(Id<Employee, Long> id) {
                super(id);
            }
        }

        private static final class Costumer extends Person<Costumer, Long> {
            private static final long serialVersionUID = 1L;

            protected Costumer(Id<Costumer, Long> id) {
                super(id);
            }
        }
    }
}
