package org.fitz.netprog.constants;

/**
 * This class contains various constants used in the program
 * Created by FitzRoi on 3/21/16.
 */
public class ProjectConstants {
    public static String NO_SELECT="Noselect";
    public static String HAS_CHILDREN ="HasChildren";
    public static String TAG_PREFIX ="A00";
    public static String EXISTS_COMMAND ="EXISTS";
    public static String AUTHENTICATE_COMMAND = " AUTHENTICATE ";
    public static String SKEY_AUTHENTICATION = "SKEY";
    public static String LOGIN_COMMAND =" LOGIN ";
    public static String LOGOUT_COMMAND =" LOGOUT";
    public static String SUCCESS ="success";
    public static String COMPLETED = "completed";
    public static String ERROR ="error";
    public static String LIST_COMMAND =" LIST ";
    public static String FETCH_COMMAND =" FETCH ";
    public static String SELECT_COMMAND =" SELECT ";
    public static String RFC_HEADER =" rfc822.header";
    public static String FROM_SUBJECT_TAG =" (body[header.fields (\"from\" \"subject\")])";
    public static String EMAIL_BODY_TAG =" body[text]";
    public static String FROM ="From:";
    public static String SUBJECT ="Subject:";
    public static String COLON =":";
    public static String AT_SIGN ="@";
    public static int PORT = 993;
    public static String DEFAULT_SERVER="imap.gmail.com";
    public static int EMAIL_ID_LENGTH = 5;
    public static final String ANSI_CLS = "\u001b[2J"; //ansi to clear command screen

    public static String LIST_REGEX ="^(\\* LIST \\(([ ?\\x5Ca-zA-Z]+)\\) \"/\" \"([\\[?\\w+\\]?/?]+)\")$"; //x5C is backslash
    public static final String EMAIL_REGEX ="^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";

}
