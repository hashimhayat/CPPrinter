package com.company; /**
    Created by Hashim Hayat on 3/22/17.
    Modified by Hashim Hayat and Jose Reyes.

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
import java.util.ArrayList;
import java.util.HashMap;

public class CPPrinter {

    /*
       TODO:
       * Read the AST File and Store it locally
       * Parse the AST file
       * Generate the C++ Code
       * Create utilities for C++ code generator
    */

    // Local Buffer to hold AST data
    private String AST;

    /*
        The C++ Code is divided into THREE sections
        -----------------------------------------------

        HEAD (CPP_HEAD) - Contains:
        Header Files, Namespaces, etc

        -----------------------------------------------

        CLASS DECLARATION (CPP_CLASSES) - Contains:
        All the Class Declarations

        -----------------------------------------------

        VTABLES (CPP_VTS) - Contains:
        All the VTable Declarations

        -----------------------------------------------
        All of these sections together make the (CPP_CODE)
     */

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

    // Previous indent level
    private String lastIndent_ = "";

    // Boolean to keep track if this is the first class that is being printed
    // Used to maintain the indent
    private Boolean shouldIndent = true;

    /*
        Method Class
            MethodReturnType
            MethodName
            Parameter
                ParameterType
                ParameterName
     */

    class Method {
        public String returnType;
        public String methodName;
        // <Type, Name>
        public HashMap<String, String> Parameters = new HashMap<String, String>();

        Method(String returnType, String methodName, HashMap<String, String> Parameters){
            this.returnType = returnType;
            this.methodName = methodName;
            this.Parameters = Parameters;
        }
    }

    /*
        ClassVariableDeclaration
            VariableType
            VariableName
            VariableValue
     */

    class Variable {
        public String varType;
        public String varName;

        Variable(String type, String name){
            this.varType = type;
            this.varName = name;
        }
    }

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

        AST = fileContent;
    }

    // Writes the C++ code to the local buffer
    private void writer(String text, int NewLines, Boolean indent, int section){

        for (int i = 0; i < NewLines; i++){
            text += "\n";
        }

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
        lastIndent_ = indent_;
        indent_ += "    ";
    }

    // Undo an Indent
    private void undoIndent(){
        indent_ = lastIndent_;
    }

    private void freshLine(int section){
        this.writer("", 1, false,section);
    }

    // Generates the namespaces
    private void resolve_namespace(String identifier){

        Boolean first = false;

        for (String namespace : identifier.split("\\.")) {
            this.writer("namespace " + namespace + " { ", 1, first, 1);
            first = true;
        }
    }

    /*
        Init a Class Declaration
         - #struct __className;
         - struct __className_VT;
         - typedef __A* A;
    */

    private void initClassDeclaration(String className){
        freshLine(2);
        this.writer("struct __" + className + ";",1,shouldIndent,2);
        this.writer("struct __" + className + "_VT;",1,false,2);
        this.writer("typedef __" + className + "* " + className + ";" ,1,false,2);
        shouldIndent = false;
    }

    /*
        Generates a Class Declaration
         - #struct __className;
         - struct __className_VT;
         - typedef __A* A;
    */

    private void resolve_ClassDeclaration(String className, ArrayList<Method> methods, ArrayList<Variable> variables){
        freshLine(2);
        this.writer("struct __" + className + " {",2,false,3);
        this.writer("__" + className + "_VT* __vptr;",2,true,3);
        this.writer("__" + className + "();",2,false,3);

        if (variables.size() > 0) {

            for (Variable variable : variables){

                String varDec = variable.varType + " " + variable.varName + ";";
                this.writer(varDec,1,false,3);
            }
        }

        if (methods.size() > 0) {

            for (Method method : methods){

                String methodDec = "static " + method.returnType + " " + method.methodName + "(" + className;

                for (String type : method.Parameters.keySet()) {
                    methodDec += ", " + type + " " + method.Parameters.get(type);
                }

                methodDec += ");";

                this.writer(methodDec,1,false,3);
            }
        }

        freshLine(2);
        this.writer("static Class __class();",2,false,3);
        this.writer("static __" + className + "_VT __vtable;",2,false,3);

        undoIndent();
        this.writer("};",2,false,3);
    }

    /*
        struct __className_VT {
            // all v tables have this variable
            Class __is_a;

            // declare all the types to be used
            returnType (*methodName)(paramTypes);

            // declare methods distinguishing between
            // overridden and non-overridden methods

            // use paramType ? className

            
        }
    */
    private void resolve_VTable(String className, ArrayList<Method> methods) {
        this.writer("struct __" + className + "_VT {",1,false,3);
        this.writer("Class __is_a;",2,true,3);

        // for each method print the ff:
        // returnType (*methodName)(paramTypes);
        if (methods.size() > 0) {
            for (Method method : methods){
                String methodDec = method.returnType + " (*" + method.methodName + ")" + "(" + className;

                for (String type : method.Parameters.keySet()) {
                    methodDec += ", " + type + " " + method.Parameters.get(type);
                }

                methodDec += ");";

                this.writer(methodDec,1,false,3);
            }
            this.writer("",1,false,3);
        }

        this.writer("__" + className + "_VT()",1,false,3);
        this.writer(": __is_a(__" + className + "::__class()),",1,false,3);

        if (methods.size() > 0) {
            for (Method method : methods) {
                if (method.Parameters.get(0).parameterType == Object) {
                    String methodDec = method.methodName + "((" + method.returnType + " (*)" + "(" + className;

                    for (String type : method.Parameters.keySet()) {
                        methodDec += ", " + type + " " + method.Parameters.get(type);
                    }

                    // TODO: figure out how to distinguish between 
                    // overridden methods and non-overridden
                    methodDec += ") &__" + className + "::" + method.methodName + "),";
                    this.writer(methodDec,1,false,3);
                } else {
                    String methodDec = method.methodName + "(&__" + className + "::" + method.methodName + "),"
                }
            }
            undoIndent();
            this.writer("{",1,false,3);
            this.writer("}",1,false,3);
        }    

        undoIndent();
        this.writer("};",2,false,3);   
    }

    /*
        Method responsible for generating the C++ code from the AST data
        using the utilities
    */

    public void cppGenerator(){

        this.cppInit();
        this.resolve_namespace("inputs.test002");
        this.initClassDeclaration("A");

        HashMap<String, String> paramaters = new HashMap<>();
        paramaters.put("int","text");
        paramaters.put("Bool","isOk");

        HashMap<String, String> paramater = new HashMap<>();
        paramater.put("int","first");
        paramater.put("Bool","second");

        Method m = new Method("String", "toString",paramaters);
        Method m2 = new Method("int", "hash",paramater);

        Variable var = new Variable("int","count");
        ArrayList<Variable> vars = new ArrayList<>();
        vars.add(var);


        ArrayList<Method> methods = new ArrayList<>();
        methods.add(m);
        methods.add(m2);

        this.resolve_ClassDeclaration("A", methods,vars);

    }


}
