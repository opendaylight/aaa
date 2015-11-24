package org.opendaylight.aaa.cert;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.shared.utils.cli.javatool.JavaToolRequest;

public abstract class abstractKeyToolCommand implements JavaToolRequest{

    protected Map<String, String> arguments = new LinkedHashMap<String, String>();

    public void validateArguments () throws Exception {
        for (String str : arguments.keySet()) {
            if (arguments.get(str).isEmpty())
                throw new Exception("Argument " + str + " is empty");
        }
    }

    public String[] getArguments() {
        int i = -1;
        String[] args = new String[arguments.size() * 2];
        for (String str : arguments.keySet()) {
            args[++i] = str;
            args[++i] = arguments.get(str);
        }
        return args;
    }
}
