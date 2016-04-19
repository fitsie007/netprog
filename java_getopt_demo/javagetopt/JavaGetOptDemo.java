package javagetopt;
import gnu.getopt.LongOpt;
import gnu.getopt.Getopt;

/*
Author: Fitzroy Nembhard
This is a demo of using the GNU Getopt for Java
*/

public class JavaGetOptDemo
{

    public static void main(String[] argv)
    {
        int c;
        String arg;
        LongOpt[] longopts = new LongOpt[3];
        //
        StringBuffer sb = new StringBuffer();
        longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
        //
        Getopt g = new Getopt("testprog", argv, "-:b:d:h;", longopts);
        g.setOpterr(false); //For custom error handling
        //
        while ((c = g.getopt()) != -1)
            switch (c)
            {
                case 0:
                    arg = g.getOptarg();
                    System.out.println("option with value '" +
                            (char)(new Integer(sb.toString())).intValue()
                            + "' with argument " +
                            ((arg != null) ? arg : "null"));
                    break;

                case 1:
                    System.out.println("Non-option argv element " +
                            "with value '" + g.getOptarg() + "'");
                    break;

                case 2:
                    arg = g.getOptarg();
                    System.out.println("Option: " +
                            longopts[g.getLongind()].getName() +
                            " with value " +
                            ((arg != null) ? arg : "null"));
                    break;

                case 'b':
                    System.out.println("You picked option " + (char)c);
                    break;

                case 'd':
                    arg = g.getOptarg();
                    System.out.println("You picked option '" + (char)c +
                            "' with argument " +
                            ((arg != null) ? arg : "null"));
                    break;

                case 'h':
                    System.out.println("This is the help option");
                    break;

                default:
                    System.out.println("getopt() returned " + c);
                    break;
            }

        for (int i = g.getOptind(); i < argv.length ; i++)
            System.out.println("Non option argv element: " + argv[i] + "\n");
    }

}


