package com.diffmin;

import java.io.File;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;


public class App
{
    public static String sample(String sampleString) {
        return sampleString;
    }
    public static void main( String[] args ) {
        if (args.length != 2) {
            System.out.println("Usage: DiffSpoon <file_1>  <file_2>");
            return;
        }
        final Diff result;
        try {
            result = new AstComparator().compare(new File(args[0]), new File(args[1]));
            System.out.println(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }
}
