/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.index;

import org.junit.Test;

import com.hp.util.test.ThrowableTester;
import com.hp.util.test.ThrowableTester.Instruction;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class RangeLimitTest {

    @Test
    public void testConstruction() {
		ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
			@SuppressWarnings("unused")
			@Override
			public void execute() throws Throwable {
				new RangeLimit<String>(null, "b");
			}
		});
		ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
			@SuppressWarnings("unused")
			@Override
			public void execute() throws Throwable {
				new RangeLimit<String>("a", null);
			}
		});
		ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
			@SuppressWarnings("unused")
			@Override
			public void execute() throws Throwable {
				new RangeLimit<String>(null, null);
			}
		});
		ThrowableTester.testThrows(IllegalArgumentException.class, new Instruction() {
			@SuppressWarnings("unused")
			@Override
			public void execute() throws Throwable {
				new RangeLimit<String>("b", "a");
			}
		});
		ThrowableTester.testThrows(IllegalArgumentException.class, new Instruction() {
			@SuppressWarnings("unused")
			@Override
			public void execute() throws Throwable {
				new RangeLimit<String>("a", "a");
			}
		});
    }
}
