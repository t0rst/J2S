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

import com.satisfyingstructures.J2S.antlr.Java8Parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;


/// J2SRewriter provides additional rewriter utility functions specific to the needs of J2S
public class J2SRewriter extends ParseTreeRewriter {

    public J2SRewriter(ParseTree tree, TokenStream tokens)
    {
        super(tree, tokens);
    }

    public void deleteWithExcessWhitespace(Token token)
    {
        int i = token.getTokenIndex();
        deleteWithExcessWhitespace(i, i);
    }

    public void deleteWithExcessWhitespace(ParseTree pt)
    {
        Interval interval = pt.getSourceInterval();
        deleteWithExcessWhitespace(interval.a, interval.b);
    }

    public void deleteWithExcessWhitespace(int i, int j)
    {
        // If we have collapsible whitespace on both sides of the deletion, then we need to get rid
        // of one side. We don't touch the space before if it holds the initial indent for the line
        // i.e. \n\s+.
        // Otherwise, if one side is a non-identifier character then space on the other side can be
        // deleted to allow the neighbouring token to move up flush to the non-identifier.
        TokenStream ts = getTokenStream();
        Token tokBfr = ts.get(i-1);
        Token tokAft = ts.get(j+1);
        String textBfr = getText(tokBfr);
        String textAft = getText(tokAft);
        char charBfr = 0 < textBfr.length() ? textBfr.charAt(textBfr.length()-1) : 'a';
        char charAft = 0 < textAft.length() ? textAft.charAt(0) : 'a';
        boolean canDeleteSpaceFromAfter = !Character.isJavaIdentifierStart(charBfr);
        boolean canDeleteSpaceFromBefore = !Character.isJavaIdentifierStart(charAft)
                                        && -1 == textBfr.indexOf('\n');
        int idx, white, len;
        char c = ' ';
        boolean deletedSpaceAfter = false;
        if (canDeleteSpaceFromAfter)
        {
            for (idx = 0, white = 0, len = textAft.length(); idx < len; idx++, white++)
                if (' ' != (c = textAft.charAt(idx)) && '\t' != c)
                    break;
            if (0 < white)
            {
                replace(tokAft, textAft.substring(white));
                deletedSpaceAfter = true;
            }
        }
        if (canDeleteSpaceFromBefore)
        {
            for (idx = textBfr.length(), white = 0; 0 < idx; white++)
                if (' ' != (c = textBfr.charAt(--idx)) && '\t' != c)
                    break;
            if (0 < white && deletedSpaceAfter)
                {white--; idx++;}
            if (0 < white)
                replace(tokBfr, textBfr.substring(0, idx));
        }
        // Finally delete the range. We do this separately from collapsing whitespace because the
        // rewriter throws an exception if a later rewrite request partially overlaps an earlier
        // request, which can easily happen if we group the delete range with tokens on either side
        // and and an enclosing range is later requested that shares an edge with the original
        // deletion.
        replace(i, j, null);
    }

    public void replaceAndSurroundWithWhitespace(Token token, String text)
    {
        int i = token.getTokenIndex();
        replaceAndSurroundWithWhitespace(i, i, text);
    }

    public void replaceAndSurroundWithWhitespace(ParseTree pt, String text)
    {
        Interval interval = pt.getSourceInterval();
        replaceAndSurroundWithWhitespace(interval.a, interval.b, text);
    }

    public void replaceAndSurroundWithWhitespace(int i, int j, String text)
    {
        // Sort out the surrounding text
        int idx, separation, len;
        Token token;
        char c = ' ';
        for (idx = 0, separation = 0, len = text.length(); idx < len; idx++, separation++)
            if (' ' != (c = text.charAt(idx)) && '\t' != c)
                break;
        if (0 == separation && Character.isJavaIdentifierStart(c))
        {
            token = getTokenStream().get(i-1);
            if (token.getType() != Java8Parser.WS && token.getType() != Java8Parser.COMMENT)
                text = " "+text;
        }
        for (idx = text.length() - 1, separation = 0; 0 <= idx; idx--, separation++)
            if (' ' != (c = text.charAt(idx)) && '\t' != c)
                break;
        if (0 == separation && Character.isJavaIdentifierPart(c))
        {
            token = getTokenStream().get(j+1);
            if (token.getType() != Java8Parser.WS && token.getType() != Java8Parser.COMMENT)
                text += " ";
        }
        // Finally replace the requested interval
        replace(DEFAULT_PROGRAM_NAME, i, j, text);
    }
}
