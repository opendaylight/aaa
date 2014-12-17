/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common;

/**
 * A marker interface to formally keep track of workarounds. An alternative method would be to use
 * TODO or FIXME which are usually tracked by IDE's and they don't add overhead to production code.
 * Using this class would require an instance of {@code Workaround} to be created, however the code
 * would be easier to maintain since the workaround would be encapsulated (In case several lines of
 * code are needed).
 * <p>
 * A workaround is a bypass of a recognized problem in a system. A workaround is typically a
 * temporary fix that implies that a genuine solution to the problem is needed.
 * http://en.wikipedia.org/wiki/Workaround
 * <p>
 * Typically they are considered brittle in that they will not respond well to further pressure from
 * a system beyond the original design. In implementing a workaround it is important to flag the
 * change so as to later implement a proper solution.
 * <p>
 * Example:
 * 
 * <pre>
 * public static void main(String[] args) {
 *     final String subject = &quot;World&quot;;
 * 
 *     new Workaround() {
 *         {
 *             System.out.println(&quot;Hello &quot; + subject);
 *         }
 *     };
 * 
 *     new PreprocessWorkaround() {
 *         {
 *             System.out.println(&quot;Hello &quot; + subject);
 *         }
 *     };
 * }
 * 
 * class PreprocessWorkaround implements Workaround {
 *     public PreprocessWorkaround() {
 *         System.out.println(&quot;Preprocessing...&quot;);
 *     }
 * }
 * 
 * </pre>
 * 
 * @author Fabiel Zuniga
 */
public interface Workaround {

}
