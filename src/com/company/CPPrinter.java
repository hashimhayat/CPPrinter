package com.company; /**
    Created by Hashim Hayat on 3/22/17.

    This class is responsible for the Phase 3 of the Translator

    Phase 3: Write C++ header with inheritance hierarchy
    Input: Set of output ASTs from phase 2
    Output: No output.

    Steps:

      - Traverse set of output ASTs from phase 2
      - For each AST, generate concrete C++ syntax and print it into a C++ header file output.h that contains all the type definitions.

     *Side effect: the generated header file output.h is on disk and complete at this point.
 **/


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CPPrinter {

    /*
       TODO:
       * Read the AST File and Store it locally
       * Parse the AST file
       * Generate the C++ Code
       * Create utilities for C++ code generator
    */

    // Local Buffer to hold AST data
    private String[] AST;

    // Local Buffer to hold the header/namespace etc information
    private StringBuilder CPP_HEAD = new StringBuilder();

    // Local Buffer to hold the class declarations
    private StringBuilder CPP_CLASSES = new StringBuilder();

    // Local Buffer to hold the v tables
    private StringBuilder CPP_VTS = new StringBuilder();

    // Local Buffer to hold generated C++ code
    private StringBuilder CPP_CODE = new StringBuilder();

    // The level of indent in the code so far
    private String indent_ = "";

    // Custom Constructor that Reads the AST file and stores it locally.
    public CPPrinter(String filePath){

        //readFile(filePath);
    }

    // AST file Reader.
    private void readFile(String filePath) {

        String fileContent = "";

        try (BufferedReader buffer = new BufferedReader(new FileReader(filePath))) {

            String currentLine;

            while ((currentLine = buffer.readLine()) != null) {
                fileContent += currentLine;
                fileContent += '\n';
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        AST = fileContent.replaceAll("\\s+", " ").trim().split(" ");
    }

    // Writes the C++ code to the local buffer
    private void writer(String text, int NewLines, Boolean indent, int section){

        for (int i = 0; i < NewLines; ++i)
            text += '\n';

        if (indent)
            indent();
            text = indent_ + text;

        // 1 -- HEAD. 2 -- CLASSES. 3 -- VTS

        if (section == 1)
            CPP_HEAD.append(text);
        else if (section == 2)
            CPP_CLASSES.append(text);
        else if (section == 3)
            CPP_VTS.append(text);

    }

    // Print Debug info on the console.
    public void printConsole(){

        CPP_CODE.append(CPP_HEAD).append(CPP_CLASSES).append(CPP_VTS);
        System.out.println(CPP_CODE.toString());
    }

    /* -------------------------------------------------------------- */
    /*              AST to C++ Conversion Utilities */
    /* -------------------------------------------------------------- */

    /*
        Init the local CPP_CODE by including:
         - #pragma once
         - #include "java_lang.h"
    */
    private void cppInit(){
        this.writer("#pragma once", 2, false,1);
        this.writer("#include \"java_lang.h\"", 2, false,1);
        this.writer("using namespace java::lang;", 2, false,1);
    }

    // Performs an indent
    private void indent(){
        indent_ += "    ";
    }

    private void freshLine(int section){
        this.writer("", 1, false,section);
    }

    // Generates the namespaces
    private void resolve_namespace(String identifier){
        this.writer("namespace " + identifier + " { "  ,1,true,1);
    }

    /*
        Init a Class Declaration
         - #struct __className;
         - struct __className_VT;
         - typedef __A* A;
    */

    private void initClassDeclaration(String className){
        freshLine(2);
        this.writer("struct __" + className,1,true,2);
        this.writer("struct __" + className + "_VT",1,false,2);
        this.writer("typedef __" + className + "* " + className ,1,false,2);
    }


    /*
        Method responsible for generating the C++ code from the AST data
        using the utilities
    */

    public void cppGenerator(){
        this.cppInit();
        this.resolve_namespace("inputs");
        this.resolve_namespace("javalang");
        this.initClassDeclaration("A");
    }


}
