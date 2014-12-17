/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.test;

import org.junit.Test;

import com.hp.util.test.EqualityTester.Exerciser;
import com.hp.util.test.ThrowableTester.Instruction;
import com.hp.util.test.ThrowableTester.Validator;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class EqualityTesterTest {

    @Test
    public void testTestEqualsAndHashCode() {
        final RandomDataGenerator randomDataGenerator = new RandomDataGenerator();

        long partofEquals = randomDataGenerator.getLong();
        Equable obj = new Equable(partofEquals, randomDataGenerator.getLong());
        Equable equal1 = new Equable(partofEquals, randomDataGenerator.getLong());
        Equable equal2 = new Equable(partofEquals, randomDataGenerator.getLong());
        Equable unequal = new Equable(randomDataGenerator.getLong(), randomDataGenerator.getLong());

        EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequal);
    }

    @Test
    public void testTestEqualsAndHashCodeWithExerciser() {
        final RandomDataGenerator randomDataGenerator = new RandomDataGenerator();

        long partofEquals = randomDataGenerator.getLong();
        Equable obj = new Equable(partofEquals, randomDataGenerator.getLong());
        Equable equal1 = new Equable(partofEquals, randomDataGenerator.getLong());
        Equable equal2 = new Equable(partofEquals, randomDataGenerator.getLong());
        Equable unequal = new Equable(randomDataGenerator.getLong(), randomDataGenerator.getLong());

        Exerciser<Equable> exerciser = new Exerciser<Equable>() {
            @Override
            public void exercise(Equable equable) {
                equable.setNotPartOfEquals(randomDataGenerator.getLong());
            }
        };

        EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, exerciser, unequal);
    }

    @Test
    public void testNoReflexive() {
        final EquableNoReflexive obj = new EquableNoReflexive(Id.BASE);
        final EquableNoReflexive equal1 = new EquableNoReflexive(Id.EQUALS_TO_BASE_1);
        final EquableNoReflexive equal2 = new EquableNoReflexive(Id.EQUALS_TO_BASE_2);
        final EquableNoReflexive unequal = new EquableNoReflexive(Id.UNEQALS_TO_BASE);

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {
            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "Reflexive property broken";
                AssertUtil.assertStartsWith(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequal);
            }
        }, errorValidator);
    }

    @Test
    public void testNoSymmetric() {
        final EquableNoSymmetric obj = new EquableNoSymmetric(Id.BASE);
        final EquableNoSymmetric equal1 = new EquableNoSymmetric(Id.EQUALS_TO_BASE_1);
        final EquableNoSymmetric equal2 = new EquableNoSymmetric(Id.EQUALS_TO_BASE_2);
        final EquableNoSymmetric unequal = new EquableNoSymmetric(Id.UNEQALS_TO_BASE);

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {
            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "Symmetric property broken";
                AssertUtil.assertStartsWith(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequal);
            }
        }, errorValidator);
    }

    @Test
    public void testNoTransitive() {
        final EquableNoTransitive obj = new EquableNoTransitive(Id.BASE);
        final EquableNoTransitive equal1 = new EquableNoTransitive(Id.EQUALS_TO_BASE_1);
        final EquableNoTransitive equal2 = new EquableNoTransitive(Id.EQUALS_TO_BASE_2);
        final EquableNoTransitive unequal = new EquableNoTransitive(Id.UNEQALS_TO_BASE);

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {
            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "Transitive property broken";
                AssertUtil.assertStartsWith(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequal);
            }
        }, errorValidator);
    }

    @Test
    public void testNullFailure() {
        final EquableNullFailure obj = new EquableNullFailure(Id.BASE);
        final EquableNullFailure equal1 = new EquableNullFailure(Id.EQUALS_TO_BASE_1);
        final EquableNullFailure equal2 = new EquableNullFailure(Id.EQUALS_TO_BASE_2);
        final EquableNullFailure unequal = new EquableNullFailure(Id.UNEQALS_TO_BASE);

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {
            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "Null reference property broken";
                AssertUtil.assertStartsWith(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequal);
            }
        }, errorValidator);
    }

    @Test
    public void testDifferentTypeFailure() {
        final EquableDifferentTypeFailure obj = new EquableDifferentTypeFailure(Id.BASE);
        final EquableDifferentTypeFailure equal1 = new EquableDifferentTypeFailure(Id.EQUALS_TO_BASE_1);
        final EquableDifferentTypeFailure equal2 = new EquableDifferentTypeFailure(Id.EQUALS_TO_BASE_2);
        final EquableDifferentTypeFailure unequal = new EquableDifferentTypeFailure(Id.UNEQALS_TO_BASE);

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {
            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "Different type parameter consideration broken";
                AssertUtil.assertStartsWith(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequal);
            }
        }, errorValidator);
    }

    @Test
    public void testInequalityFailure() {
        final EquableInequalityFailure obj = new EquableInequalityFailure(Id.BASE);
        final EquableInequalityFailure equal1 = new EquableInequalityFailure(Id.EQUALS_TO_BASE_1);
        final EquableInequalityFailure equal2 = new EquableInequalityFailure(Id.EQUALS_TO_BASE_2);
        final EquableInequalityFailure unequal = new EquableInequalityFailure(Id.UNEQALS_TO_BASE);

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {
            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "Inequality test broken";
                AssertUtil.assertStartsWith(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequal);
            }
        }, errorValidator);
    }

    @Test
    public void testHashcodeFailure() {
        final EquableHashcodeFailure obj = new EquableHashcodeFailure(Id.BASE);
        final EquableHashcodeFailure equal1 = new EquableHashcodeFailure(Id.EQUALS_TO_BASE_1);
        final EquableHashcodeFailure equal2 = new EquableHashcodeFailure(Id.EQUALS_TO_BASE_2);
        final EquableHashcodeFailure unequal = new EquableHashcodeFailure(Id.UNEQALS_TO_BASE);

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {
            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "Hashcode broken";
                AssertUtil.assertStartsWith(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequal);
            }
        }, errorValidator);
    }

    @Test
    public void testNoConsistent() {
        final EquableNoConsistent obj = new EquableNoConsistent(Id.BASE);
        final EquableNoConsistent equal1 = new EquableNoConsistent(Id.EQUALS_TO_BASE_1);
        final EquableNoConsistent equal2 = new EquableNoConsistent(Id.EQUALS_TO_BASE_2);
        final EquableNoConsistent unequal = new EquableNoConsistent(Id.UNEQALS_TO_BASE);

        final Exerciser<EquableNoConsistent> exerciser = new Exerciser<EquableNoConsistent>() {
            @Override
            public void exercise(EquableNoConsistent equable) {
                equable.brakeConsistency();
            }
        };

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {
            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "Consistent property broken for";
                AssertUtil.assertStartsWith(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, exerciser, unequal);
            }
        }, errorValidator);
    }

    @Test
    public void testHashcodeNoConsistent() {
        final EquableHashcodeNoConsistent obj = new EquableHashcodeNoConsistent(Id.BASE);
        final EquableHashcodeNoConsistent equal1 = new EquableHashcodeNoConsistent(Id.EQUALS_TO_BASE_1);
        final EquableHashcodeNoConsistent equal2 = new EquableHashcodeNoConsistent(Id.EQUALS_TO_BASE_2);
        final EquableHashcodeNoConsistent unequal = new EquableHashcodeNoConsistent(Id.UNEQALS_TO_BASE);

        final Exerciser<EquableHashcodeNoConsistent> exerciser = new Exerciser<EquableHashcodeNoConsistent>() {
            @Override
            public void exercise(EquableHashcodeNoConsistent equable) {
                equable.brakeConsistency();
            }
        };

        Validator<AssertionError> errorValidator = new Validator<AssertionError>() {
            @Override
            public void assertThrowable(AssertionError error) {
                String expectedError = "Hashcode consistent property broken";
                AssertUtil.assertStartsWith(expectedError, error.getMessage());
            }
        };

        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, exerciser, unequal);
            }
        }, errorValidator);
    }

    private static class Equable {

        private long partOfEquals;
        @SuppressWarnings("unused")
        private long notPartOfEquals;

        public Equable(long partOfEquals, long notPartOfEquals) {
            this.partOfEquals = partOfEquals;
            this.notPartOfEquals = notPartOfEquals;
        }

        public void setNotPartOfEquals(long notPartOfEquals) {
            this.notPartOfEquals = notPartOfEquals;
        }

        @Override
        public int hashCode() {
            return Long.valueOf(this.partOfEquals).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            Equable other = (Equable) obj;

            if (this.partOfEquals != other.partOfEquals) {
                return false;
            }

            return true;
        }
    }

    private static enum Id {
        BASE, EQUALS_TO_BASE_1, EQUALS_TO_BASE_2, UNEQALS_TO_BASE;
    }

    private static abstract class BrokenEquable {

        private Id id;

        public BrokenEquable(Id id) {
            this.id = id;
        }

        protected Id getId() {
            return this.id;
        }

        @Override
        public int hashCode() {
            if (this.id == Id.UNEQALS_TO_BASE) {
                return 1;
            }
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (!(obj instanceof BrokenEquable)) {
                return false;
            }

            BrokenEquable other = (BrokenEquable) obj;

            if (this.id == Id.UNEQALS_TO_BASE) {
                return false;
            }

            if (other.id == Id.UNEQALS_TO_BASE) {
                return false;
            }

            return true;
        }
    }

    private static class EquableNoReflexive extends BrokenEquable {

        public EquableNoReflexive(Id id) {
            super(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return false; // Invalid
            }
            return super.equals(obj);
        }
    }

    private static class EquableNoSymmetric extends BrokenEquable {

        public EquableNoSymmetric(Id id) {
            super(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (!(obj instanceof BrokenEquable)) {
                return false;
            }

            BrokenEquable other = (BrokenEquable) obj;

            if (getId() == Id.BASE && other.getId() == Id.EQUALS_TO_BASE_1) {
                return true;
            }

            if (getId() == Id.EQUALS_TO_BASE_1 && other.getId() == Id.BASE) {
                return false;
            }

            return super.equals(obj);
        }
    }

    private static class EquableNoTransitive extends BrokenEquable {

        public EquableNoTransitive(Id id) {
            super(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (!(obj instanceof BrokenEquable)) {
                return false;
            }

            BrokenEquable other = (BrokenEquable) obj;

            if (getId() == Id.BASE && other.getId() == Id.EQUALS_TO_BASE_1) {
                return true;
            }

            if (getId() == Id.EQUALS_TO_BASE_1 && other.getId() == Id.EQUALS_TO_BASE_2) {
                return true;
            }

            if (getId() == Id.BASE && other.getId() == Id.EQUALS_TO_BASE_2) {
                return false;
            }

            return super.equals(obj);
        }
    }

    private static class EquableNullFailure extends BrokenEquable {

        public EquableNullFailure(Id id) {
            super(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return true;
            }

            return super.equals(obj);
        }
    }

    private static class EquableDifferentTypeFailure extends BrokenEquable {

        public EquableDifferentTypeFailure(Id id) {
            super(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (!(obj instanceof EquableDifferentTypeFailure)) {
                return true;
            }

            return super.equals(obj);
        }
    }

    private static class EquableInequalityFailure extends BrokenEquable {

        public EquableInequalityFailure(Id id) {
            super(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (!(obj instanceof EquableInequalityFailure)) {
                return false;
            }

            EquableInequalityFailure other = (EquableInequalityFailure) obj;

            if (other.getId() == Id.UNEQALS_TO_BASE) {
                return true;
            }

            return super.equals(obj);
        }
    }

    private static class EquableHashcodeFailure extends BrokenEquable {

        public EquableHashcodeFailure(Id id) {
            super(id);
        }

        @Override
        public int hashCode() {
            if (getId() == Id.BASE) {
                return 0;
            }
            else if (getId() == Id.EQUALS_TO_BASE_1) {
                return 1;
            }
            else if (getId() == Id.EQUALS_TO_BASE_2) {
                return 2;
            }

            return super.hashCode();
        }
    }

    private static class EquableNoConsistent extends BrokenEquable {

        private boolean brakeConsistency = false;

        public EquableNoConsistent(Id id) {
            super(id);
        }

        public void brakeConsistency() {
            this.brakeConsistency = true;
        }

        @Override
        public boolean equals(Object obj) {
            if (this.brakeConsistency) {
                return false;
            }

            return super.equals(obj);
        }
    }

    private static class EquableHashcodeNoConsistent extends BrokenEquable {
        private boolean brakeConsistency = false;

        public EquableHashcodeNoConsistent(Id id) {
            super(id);
        }

        public void brakeConsistency() {
            this.brakeConsistency = true;
        }

        @Override
        public int hashCode() {
            if (this.brakeConsistency) {
                return getId().hashCode();
            }
            return super.hashCode();
        }
    }
}
