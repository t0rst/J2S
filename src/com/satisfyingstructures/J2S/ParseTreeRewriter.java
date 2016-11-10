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

/**
 * Created by Torsten Louland on 21/10/2016.
 */
package com.satisfyingstructures.J2S;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.misc.Interval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ParseTreeRewriter extends TokenStreamRewriter {

    final ParseTree tree;

    public ParseTreeRewriter(ParseTree tree, TokenStream tokens) {
        super(tokens);
        this.tree = tree;
    }

	// operations on an Interval

	public void insertAfter(Interval i, Object text) {
		insertAfter(DEFAULT_PROGRAM_NAME, i.b, text);
	}

	public void insertAfter(String programName, Interval i, Object text) {
		insertAfter(programName, i.b, text);
	}

	public void insertBefore(Interval i, Object text) {
		insertBefore(DEFAULT_PROGRAM_NAME, i.a, text);
	}

	public void insertBefore(String programName, Interval i, Object text) {
		insertBefore(programName, i.a, text);
	}

	public void replace(Interval i, Object text) {
		replace(DEFAULT_PROGRAM_NAME, i.a, i.b, text);
	}

	public void replace(String programName, Interval i, Object text) {
		replace(programName, i.a, i.b, text);
	}

	public void delete(Interval i) {
		delete(DEFAULT_PROGRAM_NAME, i);
	}

	public void delete(String programName, Interval i) {
		replace(programName, i, null);
	}

    // operations on a ParseTree

	public void insertAfter(ParseTree pt, Object text) {
		insertAfter(DEFAULT_PROGRAM_NAME, pt, text);
	}

	public void insertAfter(String programName, ParseTree pt, Object text) {
		insertAfter(programName, pt.getSourceInterval(), text);
	}

	public void insertBefore(ParseTree pt, Object text) {
		insertBefore(DEFAULT_PROGRAM_NAME, pt, text);
	}

	public void insertBefore(String programName, ParseTree pt, Object text) {
		insertBefore(programName, pt.getSourceInterval(), text);
	}

	public void replace(ParseTree pt, Object text) {
		replace(DEFAULT_PROGRAM_NAME, pt, text);
	}

	public void replace(String programName, ParseTree pt, Object text) {
		replace(programName, pt.getSourceInterval(), text);
	}

	public void delete(ParseTree pt) {
		delete(DEFAULT_PROGRAM_NAME, pt);
	}

	public void delete(String programName, ParseTree pt) {
		replace(programName, pt.getSourceInterval(), null);
	}

	public String getText(Token t) {
		return getText(DEFAULT_PROGRAM_NAME, t);
	}

	public String getText(String programName, Token t) {
        return getText(programName, Interval.of(t.getTokenIndex(), t.getTokenIndex()));
    }

	public String getText(ParseTree pt) {
		return getText(DEFAULT_PROGRAM_NAME, pt);
	}

	public String getText(String programName, ParseTree pt) {
        return getText(programName, pt.getSourceInterval());
    }
}
