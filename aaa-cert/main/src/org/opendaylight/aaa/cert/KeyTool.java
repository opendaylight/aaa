package org.opendaylight.aaa.cert;

import org.apache.maven.shared.utils.cli.Commandline;
import org.apache.maven.shared.utils.cli.javatool.AbstractJavaTool;
import org.apache.maven.shared.utils.cli.javatool.JavaTool;
import org.apache.maven.shared.utils.cli.javatool.JavaToolException;

import java.io.File;

public class KeyTool extends AbstractJavaTool<abstractKeyToolCommand> {

    protected KeyTool() {
        super(KeyStoreUtilis.keyToolCmd);
    }

    @Override
    protected Commandline createCommandLine(abstractKeyToolCommand arg0, String arg1) throws JavaToolException {
        // TODO Auto-generated method stub
        return null;
    }

}
