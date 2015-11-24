package org.opendaylight.aaa.cli;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.aaa.cert.api.IAaaCertProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(name = "create-odl-keystore", scope = "aaa", description = "Create the default keystore for the opendaylight controller.")

public class CreateODLKeyStore extends OsgiCommandSupport{

    private static final Logger LOG = LoggerFactory.getLogger(CreateODLKeyStore.class);
    protected IAaaCertProvider certProvider;

    @Option(name = "-keystore",
            aliases = { "--KeyStore" },
            description = "The keystore name.\n-keystore / --default is ctl.jks",
            required = false,
            multiValued = false)
    private String keyStoreName = "ctl.jks";

    @Option(name = "-storepass",
            aliases = { "--KeyPass" },
            description = "The keystore password.\n-storepass",
            required = true,
            multiValued = false)
    private String keyStorePassword = "";

    public CreateODLKeyStore(IAaaCertProvider aaaCertProvider) {
       this.certProvider = aaaCertProvider;
    }

    @Override
    protected Object doExecute() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}