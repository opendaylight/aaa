/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert;

import org.apache.maven.shared.utils.cli.CommandLineException;
import org.apache.maven.shared.utils.cli.Commandline;
import org.apache.maven.shared.utils.cli.javatool.AbstractJavaTool;
import org.apache.maven.shared.utils.cli.javatool.JavaToolException;
import org.codehaus.plexus.logging.Logger;

public class KeyTool extends AbstractJavaTool<AbstractKeyToolCommand> {

    private String workingDirectory = "";
    private Logger log;

    protected KeyTool() {
        super(KeyStoreUtilis.keyToolCmd);
        this.enableLogging(log);
        this.workingDirectory = KeyStoreUtilis.createDir(KeyStoreUtilis.keyStorePath);
    }

    public KeyTool(String workingDirectory) {
        super(KeyStoreUtilis.keyToolCmd);
        this.enableLogging(log);
        this.workingDirectory = KeyStoreUtilis.createDir(workingDirectory);
    }

    @Override
    protected Commandline createCommandLine(AbstractKeyToolCommand keyToolcmd, String workingDir) throws JavaToolException {
        Commandline cmd = new Commandline();
        cmd.addArguments(keyToolcmd.getArguments());
        cmd.setExecutable(this.getJavaToolName());
        cmd.setWorkingDirectory(workingDir);
        return cmd;
    }

    protected Commandline createCommandLine(AbstractKeyToolCommand keyToolcmd) throws JavaToolException {
        return createCommandLine(keyToolcmd, workingDirectory);
    }

    @Override
    public KeyToolResult execute(AbstractKeyToolCommand request) {
        KeyToolResult result = new KeyToolResult();
        try {
            request.validateArguments();
        } catch (Exception e1) {
            result.setErrorMessage(e1.getMessage());
            return result;
        }

        try {
            Commandline cmd = createCommandLine(request);
            result.setCommandline(cmd);
            final Process p = cmd.execute();
            result.setErrorMessage(p.getErrorStream());
            result.setMessage(p.getInputStream());
        } catch (CommandLineException | JavaToolException e) {
            result.setErrorMessage(e.getMessage());
            if (e instanceof CommandLineException)
                result.setExecutionException((CommandLineException)e);
        }
        return result;
    }
}