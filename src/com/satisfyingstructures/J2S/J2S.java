/*
The MIT License (MIT)

Copyright (c) 2016 Torsten Louland

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package com.satisfyingstructures.J2S;

import com.satisfyingstructures.J2S.antlr.Java8Lexer;
import com.satisfyingstructures.J2S.antlr.Java8Parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.lang.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;


public class J2S
{
    private static class ArgsEnumerator
    {
        private String[] args;
        private int index;
        ArgsEnumerator(String[] args) { this.args = args; index = 0; }
        String next() { return index < args.length ? args[index++] : null; }
    }

    private static File wd = null;
    private static File inputFile = null;
    private static File outputFile = null;
    private static Boolean forceOverwrite = false;
    private static Map<String, String> env = new HashMap<>();
    private static Map<String, String> typeMappings = new HashMap<>();
    private static List<ArgsEnumerator> argStack = new ArrayList<>();

    private static void pushArgs( String[] args )
    {
        argStack.add(new ArgsEnumerator(args));
    }

    private static String nextArg()
    {
        int sz = argStack.size();
        ArgsEnumerator ae = sz > 0 ? argStack.get(sz-1) : null;
        String next = null;
        while (null != ae && null == (next = ae.next()))
        {
            argStack.remove(--sz);
            ae = sz > 0 ? argStack.get(sz-1) : null;
        }
        return next;
    }

    private static int oops( String whoops )
    {
        String s = "J2S:\n";
        if (null != whoops)
            s = "Oops: "+whoops+"\n";
        s += "J2S: a tool to do ~80% of the work in converting Java 8 to Swift 3\n"
            +"Usage:\n"
            +"J2S [-i path] [-o path] [-w path] [-a path] [-f] [-Dkey=value | -Dkey]* [path]\n\n"
            +"    -i input-file-path\n"
            +"        if this option not used, then take input from stdin\n\n"
            +"    -o output-file-path\n"
            +"        enclosing directory must exist; if this option not used, then send output to stdout\n\n"
            +"    -f\n"
            +"        force overwrite of existing file\n\n"
            +"    -w working-directory\n"
            +"        input and output paths subsequent to this argument, can be specified relative to this\n"
            +"        existing directory\n\n"
            +"    -a arguments-file-path\n"
            +"        insert arguments from this file into the argument list as if inserted at this position on\n"
            +"        the commandline; one argument per line; lines are whitespace trimmed; empty or hash-prefixed\n"
            +"        lines are ignored;\n\n"
            +"    -Dkey[=value]\n"
            +"        define a value for a key that the converter will use; absent value ==> value=1\n"
            +"    -Mtype=replacement\n"
            +"        map 'type' to 'replacement' in generated Swift output\n"
            +"    -h | -help\n"
            +"        show this usage help\n\n";
        System.err.println(s);
        return 1;
    }

    private static int parseArg( String arg )
    {
        String path = null;
        File f = null;
        if (arg.startsWith("-"))
        {
            arg = arg.substring(1);
            switch (arg)
            {
                case "h":
                case "help":
                case "-help":
                    return oops(null);
                case "f":
                    forceOverwrite = true;
                    break;
                case "i-":
                    inputFile = null; // use stdin
                    break;
                case "i":
                    if (null == (path = nextArg()) || 0 == path.length())
                        return oops("expected argument -i to be followed by a file path.");
                    if (  !(f = new File(path)).isFile()
                       &&  (null == wd || !(f = new File(wd, path)).isFile()))
                            return oops("expected argument -i to have existing file path: "+path);
                    inputFile = f;
                    break;
                case "o-":
                    outputFile = null; // use stdin
                    break;
                case "o":
                    if (null == (path = nextArg()) || 0 == path.length())
                        return oops("expected argument -o to be followed by a file path.");
                    // check: file or its enclosing directory should exist at absolute path or wd relative path
                    if ( !( (f = new File(path)).isFile() || f.getParentFile().isDirectory() )
                      && (  null == wd
                         || !( (f = new File(wd, path)).isFile() || f.getParentFile().isDirectory() )))
                            return oops("expected argument -o to be for file in existing directory: "+path);
                    outputFile = f;
                    break;
                case "w":
                    if (null == (path = nextArg()) || 0 == path.length())
                        return oops("expected argument -w to be followed by a directory path.");
                    if ( !(f = new File(path)).isDirectory() )
                        return oops("expected argument -w to have existing directory path: "+path);
                    wd = f;
                    break;
                case "a":
                    if (null == (path = nextArg()) || 0 == path.length())
                        return oops("expected argument -a to be followed by a file path.");
                    if ( !(f = new File(path)).isFile() )
                        return oops("expected argument -a to have existing file path: "+path);
                    try
                    {
                        InputStream is = new FileInputStream(f);
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader reader = new BufferedReader(isr);
                        List<String> params = new ArrayList<>();
                        while (null != (arg = reader.readLine()))
                        {
                            if (-1 != arg.indexOf('#'))
                                arg = arg.substring(0, arg.indexOf('#'));
                            arg = arg.trim();
                            if (0 == arg.length())
                                continue;
                            params.add(arg);
                        }
                        String[] strings = new String[params.size()];
                        pushArgs( params.toArray(strings) );
                    }
                    catch (IOException ex)
                    {
                        return oops("problem reading parameter file: "+path);
                    }
                    break;
                default:
                    if (arg.length() > 1 && (arg.startsWith("D") || arg.startsWith("M")))
                    {
                        int splitAt = arg.indexOf("=");
                        String key = splitAt > 0 ? arg.substring(1, splitAt) : arg.substring(1);
                        String value = splitAt > 0 ? arg.substring(splitAt+1, arg.length()) : "1";
                        if (arg.startsWith("D"))
                            env.put(key, value);
                        else if (splitAt > 0 && splitAt < arg.length()-1)
                            typeMappings.put(key, value);
                    }
                    else
                        return oops("did not recognise this argument: -"+arg);
                    break;
            }
        }
        else
        {
            if (null == inputFile)
            {
                if (arg.length() > 0 && (f = new File(arg)).isFile())
                    inputFile = f;
                else
                    return oops("expected this argument to be a file: "+arg);
            }
            else
                return oops("did not recognise this argument: "+arg);
        }
        return 0;
    }

    private static void convert(InputStream is, PrintStream ps) throws Exception
    {
        ANTLRInputStream input = new ANTLRInputStream( is );

        Java8Lexer lexer = new Java8Lexer( input );
        CommonTokenStream tokens = new CommonTokenStream( lexer );
        Java8Parser parser = new Java8Parser( tokens );
        ParseTree tree = parser.compilationUnit();
    /*
        // Fix for stubborn *.java that crash the parser
        // From http://stackoverflow.com/a/32918434/618653
        // But requires antlr 4.5.3-opt - we don't have it yet
        try
        {
            // First Attempt: High-speed parsing for correct documents

            parser.setErrorHandler(new BailErrorStrategy());
            parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
            parser.getInterpreter().tail_call_preserves_sll = false;
            tree = parser.compilationUnit();
        }
        catch (ParseCancellationException e)
        {
            // Second Attempt: High-accuracy fallback parsing for complex and/or erroneous documents

            // T O D O : reset your input stream
            parser.setErrorHandler(new DefaultErrorStrategy());
            parser.getInterpreter().setPredictionMode(PredictionMode.LL);
            parser.getInterpreter().tail_call_preserves_sll = false;
            parser.getInterpreter().enable_global_context_dfa = true;
            tree = parser.compilationUnit();
        }
    */
        J2SRewriter rewriter = new J2SRewriter( tree, tokens );
        J2SConverter listener = new J2SConverter( rewriter, env, typeMappings );
        ParseTreeWalker.DEFAULT.walk( listener, tree );
        
        ps.println( rewriter.getText() );
    }

    public static void main( String[] args ) throws Exception
    {
        pushArgs(args);
        for ( String arg = nextArg() ; null != arg ; arg = nextArg() )
        {
            int status = parseArg(arg);
            if ( 0 != status )
                System.exit(status);
        }

        if ( !forceOverwrite && null != outputFile && outputFile.isFile() )
        {
            oops("use option -f to force overwrite of existing file at: "+outputFile.getAbsolutePath());
            System.exit(1);
        }
        
        // FIXME: Generates CRLF line endings even if system property line.endings is LF
        InputStream is = null == inputFile ? System.in : new FileInputStream( inputFile );
        PrintStream ps = null == outputFile ? System.out : new PrintStream( outputFile );

        convert(is, ps);
    }
}
