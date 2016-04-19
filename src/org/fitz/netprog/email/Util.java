package org.fitz.netprog.email;


import org.fitz.netprog.constants.ProjectConstants;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides auxiliary functions
 * Created by FitzRoi on 3/21/16.
 */
public class Util {
    /**
     * This method checks if a string is numeric
     * @param str string to check
     * @return true if string is a number, false otherwise
     */
    public static boolean isNumeric(String str) {
        return str.matches("\\d+");
    }

    /**
     * This method pads a number with 0s to make it a certain length
     * @param num the number to pad
     * @return number preceded with 0s to make it a certain length
     */
    public static String zeroPad(int num){
        String numStr = num + "";
        String padding="";
        if(numStr.length() < ProjectConstants.EMAIL_ID_LENGTH)
            padding = new String(new char[ProjectConstants.EMAIL_ID_LENGTH - numStr.length()]).replace("\0", "0");

        return padding+numStr;
    }

    /**
     * This method pads a string with spaces to reach a certain length
     * @param str the string to pad
     * @return string preceded with spaces to make it a certain length
     */
    public static String spacePad(String str){
        String padding="";
        if(str.length() < ProjectConstants.EMAIL_ID_LENGTH)
            padding = new String(new char[ProjectConstants.EMAIL_ID_LENGTH - str.length()]).replace("\0", " ");

        return padding + str;
    }

    /**
     * This method extracts an email from a "from" field which is primarily of the form "user" <user@example.com>
     * @param str the string from which the email must be extracted
     * @return extracted email address
     */
    public static String getEmailAddress(String str) {
        if (str.contains("<")) {
            str = str.substring(str.indexOf("<")+1, str.indexOf(">"));
        }

        Pattern emailPattern = Pattern.compile(ProjectConstants.EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
        Matcher matcher = emailPattern.matcher(str);
        if (matcher.find())
            return matcher.group(0);
        return makeFileNameSafe(str);
    }

    /**
     * This method uses regular expression to check if an email address is valid
     * @param str the string or email address
     * @return true if email matches regex or false otherwise
     */
    public static boolean isEmailValid(String str){
        Pattern emailPattern = Pattern.compile(ProjectConstants.EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
        Matcher matcher = emailPattern.matcher(str);
        return matcher.find();
    }

    /**
     * This method removes non-alphanumeric characters from a string to make it filename-safe
     * @param str input string
     * @return cleaned string
     */
    public static String makeFileNameSafe(String str){
        str = str.replaceAll("[^a-zA-Z0-9_ .]", "");
        return str;
    }

    public static String removeBrackets(String str){
        return str.replaceAll("\\[","").replaceAll("\\]", "");
    }
}
