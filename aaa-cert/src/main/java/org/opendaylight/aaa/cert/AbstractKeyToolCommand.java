/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.shared.utils.cli.javatool.JavaToolRequest;

public abstract class AbstractKeyToolCommand implements JavaToolRequest {

    protected String basicArgumnet = "";
    protected Map<String, String> arguments = new LinkedHashMap<String, String>();

    public void validateArguments () throws Exception {
        if (basicArgumnet.isEmpty())
            throw new Exception("Basic Argument is empty");

        for (String str : arguments.keySet()) {
            if(str != KeyStoreUtilis.BooleanKeyToolArguments.rfc && str != KeyStoreUtilis.BooleanKeyToolArguments.noPrompt
                    && str != KeyStoreUtilis.BooleanKeyToolArguments.verbose) {
                if (arguments.get(str).isEmpty())
                    throw new Exception("Argument " + str + " is empty");
            }
        }
    }

    public String[] getArguments() {
        String[] args = new String[arguments.size()*2+1];
        int i = 0;
        args[i] = basicArgumnet;
        for(String str : arguments.keySet()) {
            args[++i] = str;
            args[++i] = arguments.get(str);
        }
        return args;
    }
}