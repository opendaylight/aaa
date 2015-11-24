package org.opendaylight.aaa.cert;

import org.apache.maven.shared.utils.cli.CommandLineException;
import org.apache.maven.shared.utils.cli.Commandline;
import org.apache.maven.shared.utils.cli.javatool.JavaToolException;

public class CtlKeyStore {

    private KeyTool keytool;
    public CtlKeyStore() {
        keytool = new KeyTool();
    }

    public void createCtlKeyStore() {
        try {
            CreateSelfSignCert cSSc = new CreateSelfSignCert();
            Commandline cmdLine = keytool.createCommandLine(cSSc, "");
            cmdLine.execute();
        } catch (JavaToolException | CommandLineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
