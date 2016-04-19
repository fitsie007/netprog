package joptsimple.examples;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import static org.junit.Assert.assertTrue;

/**
 * Created by FitzRoi on 2/9/16.
 */
public class TestRunnerDemo {

    public static void main(String[] args){
        OptionParser parser = new OptionParser( "aB?*." );

        OptionSet options = parser.parse( args );

        if(options.hasOptions()) {
            System.out.println((options.has("a")));
            System.out.println((options.has("B")));
            System.out.println(options.valuesOf("B"));
            assertTrue(options.has("?"));
        }
    }
}