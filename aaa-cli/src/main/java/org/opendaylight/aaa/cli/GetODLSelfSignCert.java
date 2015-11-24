package org.opendaylight.aaa.cli;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.aaa.cert.api.IAaaCertProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(name = "get-odl-cert", scope = "aaa", description = "get self sign certificate for the opendaylight controller.")

public class GetODLSelfSignCert extends OsgiCommandSupport{

    private static final Logger LOG = LoggerFactory.getLogger(GetODLSelfSignCert.class);
    protected IAaaCertProvider certProvider;

    @Option(name = "-storepass",
            aliases = { "--KeyStorePass" },
            description = "The keystore password.\n-storepass",
            required = true,
            multiValued = false)
    private String keyStorePassword = "";

    @Option(name = "-alias",
            aliases = { "--alias" },
            description = "The alias.\n-alias / --default is controller",
            required = false,
            multiValued = false)
    private String alias = "controller";

    public GetODLSelfSignCert(IAaaCertProvider aaaCertProvider) {
       this.certProvider = aaaCertProvider;
    }

    @Override
    protected Object doExecute() throws Exception {
        return certProvider.getODLKeyStorCertificate(keyStorePassword, alias);
    }

}
