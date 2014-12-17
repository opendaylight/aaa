/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common;

/**
 * A marker interface to formally keep track of special considerations that are worth recalling. An
 * alternative method would be to use TODO or FIXME which are usually tracked by IDE's and they
 * don't add overhead to production code; however comments are not good practice since they get out
 * of date very easily. Using this class would require an instance of {@code SpecialConsideration}
 * to be created, however the code would be easier to look for since the special consideration would
 * be encapsulated (In case several lines of code are needed).
 * <p>
 * This class would make refactoring or rewriting code easier since it would be evident all special
 * considerations that had to be considered in the current implementation, so the new implementation
 * doesn't have to go over the same issues again.
 * <p>
 * It is recommended to use this class in unit test, so no overhead is created in production code.
 * <p>
 * Example:
 * 
 * <pre>
 * public static void main(String[] args) {
 * 
 *     new SpecialConsideration() {
 *         {
 *             / * Description * /
 *             System.out.println("Doing something special worth recalling");
 *         }
 *     };
 * }
 * </pre>
 * 
 * @author Fabiel Zuniga
 */
public interface SpecialConsideration {

}
