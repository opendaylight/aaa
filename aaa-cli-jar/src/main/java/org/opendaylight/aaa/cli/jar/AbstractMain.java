/*
 * Copyright (c) 2016 - 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cli.jar;

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
 * The {@link Main} subclass has the actual logic.
 *
 * @author Michael Vorburger.ch
 */
@SuppressWarnings("checkstyle:RegexpSingleLineJava") // allow System.out / System.err here..
public abstract class AbstractMain {

    private static final String OPTION_HELP = "h";
    private static final String OPTION_DB_DIR = "dbd";
    private static final String OPTION_LIST_USERS = "l";
    private static final String OPTION_CHANGE_USER = "cu";
    private static final String OPTION_NEW_USER = "nu";
    private static final String OPTION_DEL_USER = "du";
    private static final String OPTION_VERIFY_USER = "vu";
    private static final String OPTION_ADMINS = "a";
    private static final String OPTION_PASS = "p";
    private static final String OPTION_DEBUG = "X";

    private static final int RETURN_NOT_ENOUGH_ARGS = -1;
    private static final int RETURN_ABORT_DUE_TO_EXCEPTION = -2;
    private static final int RETURN_ARGUMENTS_MISMATCHED = -3;
    protected static final int RETURN_ILLEGAL_ARGUMENTS = -4;
    private static final int RETURN_ARGUMENTS_INCOMPATIBLE = -5;
    private static final int RETURN_ARGUMENTS_MISSING = -6;
    protected static final int RETURN_PASSWORD_MISMATCH = -7;

    @SuppressWarnings({ "unchecked", "checkstyle:IllegalThrows", "checkstyle:IllegalCatch" })
    public int parseArguments(final String[] args) throws Exception {
        boolean isInDebugLogging = false;
        try {
            OptionParser optionParser = getOptionParser();
            OptionSet optionSet = optionParser.parse(args);
            if (optionSet.has(OPTION_DEBUG)) {
                isInDebugLogging = true;
            }
            if (!optionSet.nonOptionArguments().isEmpty()) {
                unrecognizedOptions(optionSet.nonOptionArguments());
            }
            if (args.length == 0 || optionSet.has(OPTION_HELP) || !optionSet.nonOptionArguments().isEmpty()) {
                printHelp(optionParser);
                return RETURN_NOT_ENOUGH_ARGS;
            }

            if (optionSet.has(OPTION_CHANGE_USER) && optionSet.has(OPTION_NEW_USER)) {
                System.err.println("Can't use these options together: -" + OPTION_CHANGE_USER
                        + ", -" + OPTION_NEW_USER);
                return RETURN_ARGUMENTS_INCOMPATIBLE;
            } else if (optionSet.has(OPTION_PASS)
                    && !optionSet.has(OPTION_CHANGE_USER) && !optionSet.has(OPTION_VERIFY_USER)
                    && !optionSet.has(OPTION_NEW_USER)) {
                System.err.println("If passwords are specificied, then must use one or the other of these options: -"
                        + OPTION_CHANGE_USER + ", -" + OPTION_NEW_USER);
                return RETURN_ARGUMENTS_MISSING;
            }

            final List<String> userNames;
            if (optionSet.has(OPTION_CHANGE_USER)) {
                userNames = (List<String>) optionSet.valuesOf(OPTION_CHANGE_USER);
            } else if (optionSet.has(OPTION_NEW_USER)) {
                userNames = (List<String>) optionSet.valuesOf(OPTION_NEW_USER);
            } else if (optionSet.has(OPTION_DEL_USER)) {
                userNames = (List<String>) optionSet.valuesOf(OPTION_DEL_USER);
            } else if (optionSet.has(OPTION_VERIFY_USER)) {
                userNames = (List<String>) optionSet.valuesOf(OPTION_VERIFY_USER);
            } else {
                userNames = List.of();
            }
            List<String> passwords = (List<String>) optionSet.valuesOf(OPTION_PASS);
            if (!optionSet.has(OPTION_DEL_USER) && passwords.size() != userNames.size()) {
                System.err.println("Must give as many user names as passwords");
                return RETURN_ARGUMENTS_MISMATCHED;
            }

            File dbDirectory = (File) optionSet.valueOf(OPTION_DB_DIR);
            setDbDirectory(dbDirectory);

            if (optionSet.has(OPTION_DEL_USER)) {
                deleteUsers(userNames);
            }

            if (optionSet.has(OPTION_LIST_USERS)) {
                listUsers();
            }

            if (optionSet.has(OPTION_CHANGE_USER)) {
                return resetPasswords(userNames, passwords);
            } else if (optionSet.has(OPTION_VERIFY_USER)) {
                return verifyUsers(userNames, passwords);
            } else if (optionSet.has(OPTION_NEW_USER)) {
                boolean areAdmins = optionSet.has(OPTION_ADMINS);
                return addNewUsers(userNames, passwords, areAdmins);
            } else {
                return 0;
            }

        } catch (Throwable t) {
            if (!isInDebugLogging) {
                System.err.println("Aborting due to " + t.getClass().getSimpleName()
                        + " (use -X to see full stack trace): " + t.getMessage());
                return RETURN_ABORT_DUE_TO_EXCEPTION;
            } else {
                // Java will print the full stack trace if we rethrow it
                throw t;
            }
        }
    }

    private static OptionParser getOptionParser() {
        final var parser = new OptionParser();

        parser.acceptsAll(List.of(OPTION_HELP, "?"), "Show help").forHelp();
        parser.accepts(OPTION_DB_DIR, "databaseDirectory").withRequiredArg().ofType(File.class)
            .defaultsTo(new File(".")).describedAs("path");
        parser.acceptsAll(List.of(OPTION_LIST_USERS, "listUsers"), "List all existing users");
        parser.acceptsAll(List.of(OPTION_NEW_USER, "newUser"), "New user to create").withRequiredArg();
        parser.acceptsAll(List.of(OPTION_CHANGE_USER, "changeUser"), "Existing user name to change password")
            .withRequiredArg();
        parser.acceptsAll(List.of(OPTION_DEL_USER, "deleteUser"), "Existing user name to delete")
            .withRequiredArg();
        parser.acceptsAll(List.of(OPTION_VERIFY_USER, "verifyUser"), "Existing user name to verify password of")
            .withRequiredArg();
        parser.acceptsAll(List.of(OPTION_PASS, "passwd"), "New password").withRequiredArg();
        parser.accepts(OPTION_ADMINS, "New User(s) added with 'admin' role");
        // TODO accepts("v", "Display version information").forHelp();
        parser.acceptsAll(List.of(OPTION_DEBUG, "debug"), "Produce execution debug output");

        parser.allowsUnrecognizedOptions();
        return parser;
    }

    protected void unrecognizedOptions(final List<?> unrecognizedOptions) {
        System.err.println("Unrecognized options: " + unrecognizedOptions);
    }

    protected void printHelp(final OptionParser optionParser) throws IOException {
        optionParser.printHelpOn(System.out);
    }

    // ----

    protected abstract void setDbDirectory(File dbDirectory) throws IOException, IDMStoreException;

    protected abstract void listUsers() throws IDMStoreException;

    protected abstract int resetPasswords(List<String> userNames, List<String> passwords) throws IDMStoreException;

    protected abstract int verifyUsers(List<String> userNames, List<String> passwords) throws IDMStoreException;

    protected abstract int addNewUsers(List<String> userNames, List<String> passwords, boolean areAdmins)
            throws IDMStoreException;

    protected abstract int deleteUsers(List<String> userNames) throws IDMStoreException;

}
