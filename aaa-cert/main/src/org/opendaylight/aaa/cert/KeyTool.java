package org.opendaylight.aaa.cert;

import org.apache.maven.shared.utils.cli.CommandLineException;
import org.apache.maven.shared.utils.cli.Commandline;
import org.apache.maven.shared.utils.cli.javatool.AbstractJavaTool;
import org.apache.maven.shared.utils.cli.javatool.JavaTool;
import org.apache.maven.shared.utils.cli.javatool.JavaToolException;
import org.apache.maven.shared.utils.cli.javatool.JavaToolResult;

import java.io.File;
import java.io.IOException;

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
            Commandline cmd = createCommandLine(request);
            result.setCommandline(cmd);
            final Process p = cmd.execute();
            result.setErrorStream(p.getErrorStream());
        } catch (CommandLineException | IOException e) {
            result.setErrorStream(e.getMessage());
            if (e instanceof CommandLineException)
                result.setExecutionException((CommandLineException)e);
        }
        return result;
    }
}
