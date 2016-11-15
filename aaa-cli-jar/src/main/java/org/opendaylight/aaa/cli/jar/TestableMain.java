/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cli.jar;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.util.List;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.opendaylight.aaa.api.IDMStoreException;

/**
 * Class with main() method and argument parsing etc.
 * This class ONLY deals with argument parsing etc. and doesn't "do" anything,
 * yet; this is intentional, and best for true unit test-ability of this class.
 *
 * @author Michael Vorburger
 */
@SuppressWarnings("checkstyle:RegexpSingleLineJava") // allow System.out / System.err here..
public class TestableMain {

    private static final String OPTION_HELP = "h";
    private static final String OPTION_DB_DIR = "dbd";
    private static final String OPTION_LIST_USERS = "l";
    private static final String OPTION_CHANGE_USER = "cu";
    private static final String OPTION_NEW_USER = "nu";
    private static final String OPTION_PASS = "p";
    private static final String OPTION_DEBUG = "X";

    @SuppressWarnings("checkstyle:IllegalThrows")
    public static void main(String[] args) throws Exception {
        System.exit(new TestableMain().parseArguments(args));
    }

    private final OptionParser optionParser = getOptionParser();

    @SuppressWarnings({ "unchecked", "checkstyle:IllegalThrows", "checkstyle:IllegalCatch" })
    public int parseArguments(String[] args) throws Exception {
        boolean isInDebugLogging = false;
        try {
            OptionSet optionSet = optionParser.parse(args);
            if (optionSet.has(OPTION_DEBUG)) {
                isInDebugLogging = true;
                // TODO configure slf4j-simple to debug?
            }
            if (!optionSet.nonOptionArguments().isEmpty()) {
                unrecognizedOptions(optionSet.nonOptionArguments());
            }
            if (args.length == 0 || optionSet.has(OPTION_HELP) || !optionSet.nonOptionArguments().isEmpty()) {
                printHelp();
                return -1;
            }

            File dbDirectory = (File) optionSet.valueOf(OPTION_DB_DIR);
            setDbDirectory(dbDirectory);

            if (optionSet.has(OPTION_LIST_USERS)) {
                listUsers();
            }

            if (optionSet.has(OPTION_CHANGE_USER) && optionSet.has(OPTION_NEW_USER)) {
                System.err.println("Can't use these options together: -" + OPTION_CHANGE_USER
                        + ", -" + OPTION_NEW_USER);
                return -5;
            } else if (optionSet.has(OPTION_PASS) && !optionSet.has(OPTION_CHANGE_USER)
                    && !optionSet.has(OPTION_NEW_USER)) {
                System.err.println("If passwords are specificied, then must use one or the other of these options: -"
                        + OPTION_CHANGE_USER + ", -" + OPTION_NEW_USER);
                return -6;
            }

            List<String> userNames;
            if (optionSet.has(OPTION_CHANGE_USER)) {
                userNames = (List<String>) optionSet.valuesOf(OPTION_CHANGE_USER);
            } else { // optionSet.has(OPTION_NEW_USER))
                userNames = (List<String>) optionSet.valuesOf(OPTION_NEW_USER);
            }
            List<String> passwords = (List<String>) optionSet.valuesOf(OPTION_PASS);
            if (passwords.size() != userNames.size()) {
                System.err.println("Must give as many user names as passwords");
                return -3;
            }

            if (optionSet.has(OPTION_CHANGE_USER)) {
                return resetPasswords(userNames, passwords);
            } else { // optionSet.has(OPTION_NEW_USER))
                return addNewUsers(userNames, passwords);
            }

        } catch (Throwable t) {
            if (!isInDebugLogging) {
                System.err.println("Aborting due to " + t.getClass().getSimpleName()
                        + " (use -X to see full stack trace): " + t.getMessage());
                return -2;
            } else {
                // Java will print the full stack trace if we rethrow it
                throw t;
            }
        }
    }

    private OptionParser getOptionParser() {
        return new OptionParser() { {
                acceptsAll(asList(OPTION_HELP, "?" ), "Show help").forHelp();
                accepts(OPTION_DB_DIR, "databaseDirectory").withRequiredArg().ofType(File.class)
                        .defaultsTo(new File(".")).describedAs("path");
                acceptsAll(asList(OPTION_LIST_USERS, "listUsers"), "User Name");
                acceptsAll(asList(OPTION_NEW_USER, "newUser"), "New User Name").withRequiredArg();
                acceptsAll(asList(OPTION_CHANGE_USER, "changeUser"), "Existing User Name to change password")
                        .withRequiredArg();
                acceptsAll(asList(OPTION_PASS, "passwd"), "New Password").withRequiredArg();
                // TODO accepts("v", "Display version information").forHelp();
                acceptsAll(asList(OPTION_DEBUG, "debug"), "Produce execution debug output");

                allowsUnrecognizedOptions();
            }
        };
    }

    protected void unrecognizedOptions(List<?> unrecognizedOptions) {
        System.err.println("Unrecognized options: " + unrecognizedOptions);
    }

    protected void printHelp() throws IOException {
        optionParser.printHelpOn(System.out);
    }

    // ----

    protected void setDbDirectory(File dbDirectory) throws IOException {
    }

    protected void listUsers() throws IDMStoreException {
    }

    protected int resetPasswords(List<String> userNames, List<String> passwords) throws IDMStoreException {
        return 0;
    }

    protected int addNewUsers(List<String> userNames, List<String> passwords) {
        return 0;
    }


}
