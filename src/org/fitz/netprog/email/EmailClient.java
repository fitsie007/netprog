package org.fitz.netprog.email;

import org.fitz.netprog.constants.EmailClientConstants;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class facilitates connecting to an email server and fetching emails using IMAP
 * Created by FitzRoi on 3/21/16.
 */
public class EmailClient {
    private int index = 0;
    BufferedReader processOutput;
    PrintWriter output;
    String downloadFolder;

    public EmailClient(BufferedReader processOutput, PrintWriter output, String downloadFolder){
        this.processOutput = processOutput;
        this.output = output;
        this.downloadFolder = downloadFolder;
    }


    /**
     * This method logs into the server using plain text authentication
     * @param emailAddress the email address of the user
     * @param password the user's password
     * @return success or failure string
     */
    public String login(String emailAddress, String password){
        String tag, line="", loginCommand;
        tag = EmailClientConstants.TAG_PREFIX + (++index);
        loginCommand = tag +EmailClientConstants.LOGIN_COMMAND +emailAddress +" " +password;
        output.println(loginCommand);
        output.flush();
        try {
            while (!(line = processOutput.readLine()).contains(tag)) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(EmailClientConstants.LOGIN_COMMAND  +line +"\n");//authenticated success
        if(line.toLowerCase().contains(EmailClientConstants.SUCCESS) || line.toLowerCase().contains(EmailClientConstants.COMPLETED) )
            return EmailClientConstants.SUCCESS;
        else
            return EmailClientConstants.ERROR;

    }

    /**
     * This method logs out of the server
     */
    public void logout(){
        String tag, line=null, logoutCommand;
        tag = EmailClientConstants.TAG_PREFIX + (++index);
        logoutCommand = tag +EmailClientConstants.LOGOUT_COMMAND;
        output.println(logoutCommand);
        output.flush();
        try {
            while (!(line = processOutput.readLine()).contains(tag)) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(EmailClientConstants.LOGOUT_COMMAND  +" "+line +"\n");//logout success
    }

    /**
     * This method fetches a list of folders on the current user system
     * @return an arraylist of folders in raw form; example: * LIST (\HasNoChildren) "/" "INBOX"
     */
    public ArrayList<String> getList(){
        String tag, line=null;
        tag = EmailClientConstants.TAG_PREFIX +(++index);
        String listCommand = tag +EmailClientConstants.LIST_COMMAND + "\"\" \"*\"";
        output.println(listCommand);
        output.flush();

        //get a list of mailboxes
        ArrayList<String> mailboxList = new ArrayList<String>();
        try {
            while (!(line = processOutput.readLine()).contains(tag)) {
                mailboxList.add(line);
                System.out.println(line +"\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(EmailClientConstants.LIST_COMMAND +line +"\n");//list retrieved successfully
        return mailboxList;
    }

    /**
     * This method traverses the folders returned from the LIST command and saves
     * the emails to their corresponding folders locally
     * @param mailboxList an arraylist of mailboxes/folders
     */
    public void traverseList(ArrayList<String> mailboxList) {
        Pattern listPattern = Pattern.compile(EmailClientConstants.LIST_REGEX);
        String tag, line = null;

        for (String item : mailboxList) {
            Matcher regexMatcher = listPattern.matcher(item);
            int numEmails = 0;
//                System.out.print("'"+item +"': " +regexMatcher.matches() +"\n");
            if (regexMatcher.find()) {
                String childrenParamStr = regexMatcher.group(2).replace("\\", "");// eg: (\Drafts \HasNoChildren)
                String mailbox = regexMatcher.group(3); //eg: "INBOX"

                String childrenParams[] = childrenParamStr.split(" ");
                String hasChildren = childrenParams[0]; //eg: HasChildren

                File mailboxDir = new File(downloadFolder + mailbox);
                if (!mailboxDir.exists()) {
                    if (mailboxDir.mkdir()) { //create a directory named mailbox
                        System.out.println(mailbox + " directory created");
                    }
                }

                //run SELECT command on this mailbox (eg: A001 SELECT INBOX)
                tag = EmailClientConstants.TAG_PREFIX + (++index);
                String selectCommand = tag + EmailClientConstants.SELECT_COMMAND + mailbox;
                output.println(selectCommand);
                output.flush();

                try {
                    while (!(line = processOutput.readLine()).contains(tag)) {
                        System.out.println(line + "\n");
                        if (line.contains(EmailClientConstants.EXISTS_COMMAND)) {
                            String existParts[] = line.split(" ");
                            for (String part : existParts) {
                                if (Util.isNumeric(part))
                                    numEmails = Integer.parseInt(part);
                            }
                        }
                    }

                    System.out.println(numEmails + " email(s) found in " + mailbox + "\n");
                    if(numEmails > 0) {
                        System.out.println("Now saving emails...\n");
                        fetchAndSaveEmails(numEmails, mailbox);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(line +"\n");//mailbox selected successfully
            }
        }

    }

    /**
     * This method fetches and save to a file the emails in a certain folder
     * @param numEmails the number of emails in the folder
     * @param mailBox the folder or mailbox from which to fecth the emails
     */
    public void fetchAndSaveEmails(int numEmails, String mailBox) {
        String tag, line, filename, message;

        if (numEmails > 0)
            for (int i = 1; i <= numEmails; i++) {
                tag = EmailClientConstants.TAG_PREFIX + (++index);
                String fetchCommand = tag + EmailClientConstants.FETCH_COMMAND + i + EmailClientConstants.RFC_HEADER;
                String from = null, subject = null;
                output.println(fetchCommand);
                output.flush();
//                System.out.println(i +" of " + numEmails);

                try {
                    while (!(line = processOutput.readLine()).contains(tag)) {
                        if (line.contains(EmailClientConstants.FROM)) {
                            from = line.substring(line.indexOf(EmailClientConstants.COLON) + 1, line.length()).trim();
                            if (!from.contains(EmailClientConstants.AT_SIGN))
                                from += processOutput.readLine(); //if emailAddress wrapped to next line
                        }
                        if (line.contains(EmailClientConstants.SUBJECT))
                            subject = line.substring(line.indexOf(EmailClientConstants.COLON) + 1, line.length()).trim();

                        System.out.println(line + "\n");
//                        System.out.println(ProjectConstants.ANSI_CLS);
                    }
                    System.out.println(line +"\n");//email header fetched successfully
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //parse the from and subject line to create a string for the filename
                //which will be of the form 00001_user@example.com_subject_of_message
                if (from != null && subject != null) {
                    String sendersEmail = Util.getEmailAddress(from);
                    subject = Util.makeFileNameSafe(subject);
                    String emailNumber = Util.zeroPad(i);
                    filename = downloadFolder + mailBox + "/" + emailNumber + "_" + sendersEmail + "_" + subject;
                    message = fetchEmail(i);
                    saveEmailToFile(filename, message);
                }
            }

    }

    /**
     * This method retrieved the message body for a certain email message
     * @param index the index of the email in the mailbox
     * @return the message body
     */
    public String fetchEmail(int index){
        String tag, line;
        tag = EmailClientConstants.TAG_PREFIX +(++index);
        String fetchCommand = tag + EmailClientConstants.FETCH_COMMAND + index +EmailClientConstants.EMAIL_BODY_TAG;
        String message = "";
        output.println(fetchCommand);
        output.flush();

        try {
            while (!(line = processOutput.readLine()).contains(tag)) {
                message += line;
            }
            System.out.println(line +"\n");//email body fetched successfully

        } catch (IOException e) {
            e.printStackTrace();
        }

        return message;
    }

    /**
     * This method saves a certain email to a file
     * @param filename the filename to use to save the email
     * @param message the email message
     */
    public void saveEmailToFile(String filename, String message){
        PrintWriter writer;
        try {
            writer = new PrintWriter(filename, "UTF-8");
            writer.println(message);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * this method tells the server the authentication type to use to authenticate users
     * @param authenticationType the authentication type
     */
    public void selectAuthentication(String authenticationType){
        String tag, line="", authenticateCommand;
        tag = EmailClientConstants.TAG_PREFIX + (++index);
        authenticateCommand = tag +EmailClientConstants.AUTHENTICATE_COMMAND + authenticationType;

        output.println(authenticateCommand);
        output.flush();
        try {
            while (!(line = processOutput.readLine()).contains(tag)) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(EmailClientConstants.AUTHENTICATE_COMMAND  +line +"\n");//success

    }


}
