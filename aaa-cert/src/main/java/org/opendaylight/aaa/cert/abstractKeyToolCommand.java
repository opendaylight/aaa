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

public abstract class abstractKeyToolCommand implements JavaToolRequest {

    protected String basicArgumnet = "";
    protected Map<String, String> arguments = new LinkedHashMap<String, String>();

    public void validateArguments () throws Exception {
        for (String str : arguments.keySet()) {
            if(str != KeyStoreUtilis.KeyToolArguments.rfc)
                if (arguments.get(str).isEmpty())
                    throw new Exception("Argument " + str + " is empty");
        }
    }

    public String[] getArguments() {
        List<String> args = new ArrayList<String>();
        args.add(basicArgumnet);
        for (String str : arguments.keySet()) {
            args.add(str);
            args.add(arguments.get(str));
        }
        return (String[]) args.toArray();
    }
}
