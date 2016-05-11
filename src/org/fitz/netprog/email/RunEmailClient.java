package org.fitz.netprog.email;

import org.fitz.netprog.constants.EmailClientConstants;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This is the main class that allows run the email retrieval program
 * Created by FitzRoi on 3/19/16.
 */
public class RunEmailClient {


    public static void main(String args[]) {
        try {
            String opensslCommand = "openssl s_client -crlf -connect ";
            String server = EmailClientConstants.DEFAULT_SERVER;
            String downloadFolder = "";
            int exitVal;

            if (args.length > 0) {
                server = args[0];
            }

            if (args.length > 1) {
                downloadFolder = args[1];
            }

            opensslCommand += server + EmailClientConstants.COLON +EmailClientConstants.PORT;
            String email = "", pwd, loginMsg;
            Scanner scanner = new Scanner(System.in);

            //make sure email address is valid
            while (!Util.isEmailValid(email)) {
                System.out.print("Email: ");
                email = scanner.nextLine();
            }

            System.out.print("Password: ");
            pwd = scanner.nextLine();

            Process openssl = Runtime.getRuntime().exec(opensslCommand);
            InputStream is = openssl.getInputStream();
            BufferedReader processOutput = new BufferedReader(new InputStreamReader(is));
            PrintWriter output = new PrintWriter(new OutputStreamWriter(openssl.getOutputStream()));

            EmailClient emailClient = new EmailClient(processOutput, output, downloadFolder);
            loginMsg = emailClient.login(email, pwd);

            //If login successful, fetch and save emails
            if (loginMsg.equals(EmailClientConstants.SUCCESS)) {
                ArrayList<String> list = emailClient.getList();
                emailClient.traverseList(list);
                emailClient.logout();
            }

            processOutput.close();
//            exitVal = openssl.waitFor();
//            System.out.println("Exited with error code " + exitVal);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
