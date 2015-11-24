package org.opendaylight.aaa.cli;

import java.io.FileInputStream;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.aaa.cert.api.IAaaCertProvider;

@Command(name = "add-odl-cert", scope = "aaa", description = "Add ODL signed certificaet to ODL key store.")

public class AddCertODLKeyStore extends OsgiCommandSupport {

    protected IAaaCertProvider certProvider;

    @Option(name = "-cert",
            aliases = { "--CertFile" },
            description = "The ODL certificate file.\n-file / --should be accesable by the karaf command line",
            required = true,
            multiValued = false)
    private String certFile = "";

    @Option(name = "-storepass",
            aliases = { "--KeyStorePass" },
            description = "The ODL keystore password.\n-storepass",
            required = true,
            multiValued = false)
    private String keyStorePassword = "";

    @Option(name = "-alias",
            aliases = { "--alias" },
            description = "The alias.\n-alias / ODL alias default is controller as the configuration",
            required = false,
            multiValued = false)
    private String alias = "controller";

    public AddCertODLKeyStore(IAaaCertProvider aaaCertProvider) {
        this.certProvider = aaaCertProvider;
     }

    @Override
    protected Object doExecute() throws Exception {
        final FileInputStream fInputStream = new FileInputStream(certFile);
        byte[] certBytes = new byte[fInputStream.available()];
        fInputStream.read(certBytes);
        fInputStream.close();
        String certificate = new String(certBytes, "UTF-8");
        if (certProvider.addCertificateODLKeyStore(keyStorePassword, alias, certificate))
            return alias + " certificate successfully added to ODL keystore";
        else
            return "Failed to add " + alias + " certificate to ODL keystore";
    }

}
