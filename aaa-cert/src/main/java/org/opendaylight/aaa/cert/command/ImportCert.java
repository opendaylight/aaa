package org.opendaylight.aaa.cert.command;

import org.apache.maven.shared.utils.cli.StreamConsumer;
import org.opendaylight.aaa.cert.AbstractKeyToolCommand;
import org.opendaylight.aaa.cert.KeyStoreUtilis;

public class ImportCert extends AbstractKeyToolCommand {

    public ImportCert() {
        basicArgumnet = KeyStoreUtilis.BasicKeyToolArguments.importCert;
        arguments.put(KeyStoreUtilis.KeyToolArguments.keyStore, "");
        arguments.put(KeyStoreUtilis.KeyToolArguments.storePass, "");
        arguments.put(KeyStoreUtilis.KeyToolArguments.alias, "");
        arguments.put(KeyStoreUtilis.KeyToolArguments.keyPass, "");
    }

    @Override
    public StreamConsumer getSystemOutStreamConsumer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StreamConsumer getSystemErrorStreamConsumer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setSystemOutStreamConsumer(StreamConsumer systemOutStreamConsumer) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setSystemErrorStreamConsumer(StreamConsumer systemErrorStreamConsumer) {
        // TODO Auto-generated method stub
        
    }

}
