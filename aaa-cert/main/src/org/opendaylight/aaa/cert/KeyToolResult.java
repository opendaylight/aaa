package org.opendaylight.aaa.cert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.maven.shared.utils.cli.javatool.JavaToolResult;

public class KeyToolResult extends JavaToolResult{

    private String errorStream = "";

    public KeyToolResult() {
        
    }

    public void setErrorStream(InputStream inputStream) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line = "";
        while ((line = input.readLine()) != null)
            sb.append(line);
        errorStream = sb.toString();
    }

    public void setErrorStream(String error) {
        errorStream = error;
    }

    public String getErrors() {
        return errorStream;
    }
}
