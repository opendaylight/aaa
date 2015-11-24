package org.opendaylight.aaa.cert;

import org.apache.maven.shared.utils.cli.javatool.JavaToolRequest;

public interface abstractKeyToolCommand extends JavaToolRequest{

    String[] getArguments();

    void setArguments(String[] arguments);
}
