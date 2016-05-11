package org.fitz.netprog;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: Fitzroy Nembhard
 * Date: 2/29/2016
 * Professor Marius Silaghi
 * Network Programming CSE5232
 *
 * This class facilitates auxiliary functions such as date parsing and validation.
 */
public class Util {

    private static String dateRegex = "^((19|20)\\d\\d)"+           //year (group(1)
                                    "-(0?[1-9]|1[012])"+            //month (group(3)
                                    "-(0?[1-9]|[12][0-9]|3[01])"+   //day (group(4)
                                    ":([01]?\\d|2[0-3])h"+          //hh (group(5)
                                    "([0-5]?\\d)m" +                //m  (group(6)
                                    "([0-5]?\\d)s" +                //s  (group(7)
                                    "(\\d\\d\\d)Z?$";               //SSS (group(8)


    /**
     * This method checks if a date string matches a specified date regular expression.
     * @param dateStr the date string
     * @return true if date matches date regular expression (regex), false otherwise
     */
    public static boolean isDateValid(String dateStr){
        Pattern datePattern;
        Matcher regexMatcher;
        datePattern = Pattern.compile(dateRegex);
        regexMatcher = datePattern.matcher(dateStr); //eg 2016-03-12:18h30m00s001Z

        return regexMatcher.matches();

    }

    /**
     * This method formats a date so that it can be parsed by simpleDateFormat
     * and used for comparison
     * @param dateStr the date-string to parse
     * @return a reformatted date that is parsable by simpleDateFormat
     */
    public static String formatDate(String dateStr){
        Pattern datePattern;
        Matcher regexMatcher;
        String newDateStr="";

        if(isDateValid(dateStr)){
            datePattern = Pattern.compile(dateRegex);
            regexMatcher = datePattern.matcher(dateStr); //eg 2016-03-12:18h30m00s001Z
            if(regexMatcher.find()) {
                String year = regexMatcher.group(1);
                String month = regexMatcher.group(3);
                String day = regexMatcher.group(4);
                String hr = regexMatcher.group(5);
                String mm = regexMatcher.group(6);
                String ss = regexMatcher.group(7);
                String S = regexMatcher.group(8);

                newDateStr = year + "-" + month + "-" + day + " " + hr + ":" + mm + ":" + ss  +"." +S;
            }

        }
        return newDateStr;
    }

    /**
     * This method checks if an end-date is >= to start-date
     * @param startDateStr the start-date string
     * @param endDateStr the end-date string
     * @return true if end-date >= start-date
     */

    public static boolean isValidDateRange(String startDateStr, String endDateStr){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        boolean isvalid = true;

        if(isDateValid(startDateStr) && isDateValid(endDateStr)){
            try {
                Date startDate = df.parse(formatDate(startDateStr));
                Date endDate = df.parse(formatDate(endDateStr));
                Date now = new Date();
//                if(startDate.before(now) || endDate.before(now)) //date range must be after now
//                    isvalid = false;

                if( startDate.before(endDate) || startDate.equals(endDate)) //end-date must be >= start-date
                    isvalid = true;
                else
                    isvalid = false;

            } catch (ParseException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
               return false;
            }
        }
        return isvalid;

    }

    /**
     * This method checks if two date ranges are valid
     * @param startDate
     * @param endDate
     * @return
     */
    public static boolean isValidDateRange(Date startDate, Date endDate){
        boolean isvalid;
        isvalid = startDate.before(endDate) || startDate.equals(endDate);
        return isvalid;
    }

    /**
     * This method parses a date string based on the format
     * 2016-03-15:18h30m00s001Z
     * @param dateStr
     * @return
     */
    public static Date getDate(String dateStr){
        Date date = null;
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            date = df.parse(formatDate(dateStr));
        }
        catch (ParseException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return date;
    }


    /**
     * This method parses a date string so it can be saved in an SQLite database
     * @param dateStr the date string
     * @return a Date
     */
    public static Date dbFormatDateStr(String dateStr){
        Date date = null;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        try {

            date = df.parse(dateStr);
        }
        catch (ParseException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return date;
    }

    /**
     * This method formats a date into the specified format
     * @param date
     * @return
     */
    public static String dbFormatDate(Date date){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return df.format(date);
    }

    public static Date parseDateStr(String dateStr){
        Date date = null;
        try {
//            DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy"); format "Sat Mar 12 18:30:00 EST 2016"
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            date = df.parse(dateStr);
        }
        catch (ParseException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return date;
    }

    /**
     * This method retrieves the IP address from a socketAddress
     * usually in the format /192.168.2.1:2655
     * @param socketAddress
     * @return an IP string; eg: 192.168.2.1
     */
    public static  String parseIPAddress(SocketAddress socketAddress){
        InetSocketAddress inetAddress = (InetSocketAddress) socketAddress;
        InetAddress ipAddress = inetAddress.getAddress();
        return ipAddress.toString().replace("/","");
    }

    /**
     * This method retrieves the IP address from a string
     * usually in the format /192.168.2.1
     * @param ipAddress
     * @return an IP string; eg: 192.168.2.1
     */
    public static  String parseIPAddress(String ipAddress){
        return ipAddress.replace("/","");
    }

}
