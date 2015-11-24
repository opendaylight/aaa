package org.opendaylight.aaa.cli;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.aaa.cert.api.AaaCertProvider;
import org.opendaylight.aaa.cert.api.IAaaCertProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(name = "add-ctl-ks", scope = "aaa", description = "Create the default keystore for the opendaylight controller.")

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
            aliases = { "--KeyStorePass" },
            description = "The keystore password.\n-storepass",
            required = true,
            multiValued = false)
    private String keyStorePassword = "";

    @Option(name = "-storepass",
            aliases = { "--KeyPass" },
            description = "The key password.\n-keypass default will be same as keystore password",
            required = false,
            multiValued = false)
    private String keyPassword = "";

    @Option(name = "-alias",
            aliases = { "--alias" },
            description = "The alias.\n-alias / --default is controller",
            required = false,
            multiValued = false)
    private String alias = "controller";

    @Option(name = "-dName",
            aliases = { "--dName" },
            description = "The dName.\n-dName / --should be in the following formate 'CN=, OU=, O=, ST= C='",
            required = false,
            multiValued = false)
    private String dName = "'CN=ODL, OU=Dev, O=LinuxFoundation, ST=QC. Montreal, C=CA'";

    public CreateODLKeyStore(IAaaCertProvider aaaCertProvider) {
       this.certProvider = aaaCertProvider;
    }

    @Override
    protected Object doExecute() throws Exception {
        if (keyPassword.isEmpty())
            keyPassword = keyStorePassword;

        return certProvider.CreateODLKeyStore(keyStoreName, keyStorePassword, keyPassword, alias, dName);
    }

}