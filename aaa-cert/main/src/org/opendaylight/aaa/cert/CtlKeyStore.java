package org.opendaylight.aaa.cert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.maven.shared.utils.cli.CommandLineException;
import org.apache.maven.shared.utils.cli.Commandline;
import org.apache.maven.shared.utils.cli.javatool.JavaToolException;
import org.apache.maven.shared.utils.cli.javatool.JavaToolResult;
import org.opendaylight.aaa.cert.command.CreateSelfSignCert;

public class CtlKeyStore {

    private KeyTool keytool;
    public CtlKeyStore() {
        keytool = new KeyTool(KeyStoreUtilis.keyStorePath);
    }

    public void createCtlKeyStore() {
        try {
            CreateSelfSignCert cSSc = new CreateSelfSignCert();
            /*Commandline cmd = keytool.createCommandLine(cSSc, "");//
            cmd.createArg(true);
            final Process p = cmd.execute();
            String[] arg = cmd.getArguments();
            for (String str : arg) {
                System.out.println(str + " \n");
            }
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            System.out.println("start 1\n");
            String line = null; 
            //while ((line = input.readLine()) != null)
                //System.out.println(line);
            
            input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            System.out.println("start \n");
            line = null; 
            while ((line = input.readLine()) != null)
                System.out.println(line);*/
            JavaToolResult result = keytool.execute(cSSc);
        } catch (JavaToolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
    	CtlKeyStore ctl = new CtlKeyStore();
    	ctl.createCtlKeyStore();
    }
}
