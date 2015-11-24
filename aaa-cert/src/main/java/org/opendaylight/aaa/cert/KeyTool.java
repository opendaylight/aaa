/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert;

import java.io.IOException;
import org.apache.maven.shared.utils.cli.CommandLineException;
import org.apache.maven.shared.utils.cli.Commandline;
import org.apache.maven.shared.utils.cli.javatool.AbstractJavaTool;
import org.apache.maven.shared.utils.cli.javatool.JavaToolException;

public class KeyTool extends AbstractJavaTool<abstractKeyToolCommand> {

    private String workingDirectory = "";

    protected KeyTool() {
        super(KeyStoreUtilis.keyToolCmd);
    }

    public KeyTool(String workingDirectory) {
        super(KeyStoreUtilis.keyToolCmd);
        this.workingDirectory = workingDirectory;
    }

    @Override
    protected Commandline createCommandLine(abstractKeyToolCommand keyToolcmd, String workingDir) throws JavaToolException {
        Commandline cmd = new Commandline();
        cmd.addArguments(keyToolcmd.getArguments());
        cmd.setExecutable(this.getJavaToolName());
        cmd.setWorkingDirectory(workingDir);
        return cmd;
    }

    protected Commandline createCommandLine(abstractKeyToolCommand keyToolcmd) throws JavaToolException {
        return createCommandLine(keyToolcmd, workingDirectory);
    }

    @Override
    public KeyToolResult execute(abstractKeyToolCommand request) throws JavaToolException {
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
        } catch (CommandLineException | IOException e) {
            result.setErrorMessage(e.getMessage());
            if (e instanceof CommandLineException)
                result.setExecutionException((CommandLineException)e);
        }
        return result;
    }
}
