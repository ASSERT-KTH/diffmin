package com.diffmin;

import java.io.File;
import java.util.List;

import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.operations.Operation;

public class App
{
    public static String sample(String sampleString) {
        return sampleString;
    }

    public static List<Operation> getOperations(File f1, File f2) throws Exception {
        final List<Operation> operations;
        try {
            operations = new AstComparator().compare(f1, f2).getRootOperations();
        } catch (Exception e) {
            throw e;
        }
        return operations;
    }

    public static void main( String[] args ) {
        if (args.length != 2) {
            System.out.println("Usage: DiffSpoon <file_1>  <file_2>");
            return;
        }
        List<Operation> operations;
        try {
            operations = App.getOperations(new File(args[0]), new File(args[1]));
            for (int op=0; op<operations.size(); ++op) {

                System.out.println(operations.get(op).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
