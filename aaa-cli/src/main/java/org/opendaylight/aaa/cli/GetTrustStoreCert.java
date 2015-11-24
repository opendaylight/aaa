package org.opendaylight.aaa.cli;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.aaa.cert.api.IAaaCertProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(name = "get-node-cert", scope = "aaa", description = "get node certificate form the opendaylight trust keystore .")

public class GetTrustStoreCert  extends OsgiCommandSupport{

    private static final Logger LOG = LoggerFactory.getLogger(GetTrustStoreCert.class);
    protected IAaaCertProvider certProvider;

    @Option(name = "-storepass",
            aliases = { "--KeyStorePass" },
            description = "The keystore password.\n-storepass",
            required = true,
            multiValued = false)
    private String keyStorePassword = "";

    @Option(name = "-alias",
            aliases = { "--alias" },
            description = "The alias.\n-alias / --should be the node certificate alias",
            required = true,
            multiValued = false)
    private String alias = "";

    public GetTrustStoreCert(IAaaCertProvider aaaCertProvider) {
       this.certProvider = aaaCertProvider;
    }

    @Override
    protected Object doExecute() throws Exception {
        return certProvider.getCertificateTrustStore(keyStorePassword, alias);
    }

}
