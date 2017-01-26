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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// J2SRewriter provides additional rewriter utility functions specific to the needs of J2S
public class J2SRewriter extends ParseTreeRewriter {

    public final String lineBreak;      // the standard linebreak used in the token stream
    public final String singleIndent;   // the standard single indent used in the token stream

    public J2SRewriter(ParseTree tree, TokenStream tokens)
    {
        super(tree, tokens);
        lineBreak = discoverLineBreakType(tokens);
        singleIndent = discoverSingelIndentType(tokens);
    }

    static String discoverLineBreakType(TokenStream tokens)
    {
        String s = "\n";
        for (int i = 0, sz = tokens.size(); i < sz; i++)
        {
            Token t = tokens.get(i);
            if (null == t || t.getType() != Java8Parser.LB)
                continue;
            s = t.getText();
            if (-1 != (i = s.indexOf(s.charAt(0), 1))) // remove any later repetitions
                s = s.substring(0, i);
            break;
        }
        return s;
    }

    static String discoverSingelIndentType(TokenStream tokens)
    {
        String singleIndent = "\t";
        int depth = 0;
        List<Token> recentIndentsByDepth = new ArrayList<>();
        Map<String, Integer> countOfIndentStyle = new HashMap<>();
        int countOfIndents = 0;
        for (int i = 0, sz = tokens.size(); i < sz; i++)
        {
            Token t = tokens.get(i);
            if (null == t)
                continue;
            switch (t.getType())
            {
                case Java8Parser.RBRACE:
                    break;
                case Java8Parser.LBRACE:
                    depth++;
                    for (int j = recentIndentsByDepth.size(); j <= depth; j++)
                        recentIndentsByDepth.add(j, null);
                default:
                    continue;
            }
            if (i > 2)
            {
                Token tWS = tokens.get(i-1);
                if (tWS.getType() == Java8Parser.WS && tokens.get(i-2).getType() == Java8Parser.LB)
                {
                    recentIndentsByDepth.set(depth, tWS);
                    if (depth + 1 < recentIndentsByDepth.size()
                     && null != (t = recentIndentsByDepth.get(depth + 1)))
                    {
                        String deep = tWS.getText();
                        String deeper = t.getText();
                        if (deeper.startsWith(deep))
                        {
                            singleIndent = deeper.substring(deep.length());
                            Integer count = countOfIndentStyle.get(singleIndent);
                            if (null == count)
                                countOfIndentStyle.put(singleIndent, 1);
                            else
                            {
                                float share = count.floatValue()/(float)countOfIndents;
                                if (countOfIndents >= 4 && share == 1)
                                    return singleIndent; // winner, consistent use
                                if (countOfIndents > 10 && share > .9)
                                    return singleIndent; // winner, inconsistent use
                                if (countOfIndents > 20 && share > .7)
                                    return singleIndent; // winner, variable use
                                countOfIndentStyle.put(singleIndent, count + 1);
                            }
                            countOfIndents++;
                        }
                        recentIndentsByDepth.set(depth + 1, null);
                    }
                }
            }
            depth--;
        }
        // No early winner, so pick most frequent
        int best = 0;
        for (Map.Entry<String, Integer> indentStyle : countOfIndentStyle.entrySet())
        {
            int count = indentStyle.getValue();
            countOfIndents -= count; // --> remaining
            if (best < count)
            {
                best = count;
                singleIndent = indentStyle.getKey();
                if (countOfIndents < count)
                    return singleIndent; // cant be beaten now
            }
        }
        return singleIndent;
    }

    // tokens

    public Token getTokenFollowing(Token token)
    {
        int index = null != token ? token.getTokenIndex() + 1 : tokens.size();
        if (index < tokens.size())
            return tokens.get(index);
        return null;
    }

    public Token getTokenPreceding(Token token)
    {
        int index = null != token ? token.getTokenIndex() - 1 : -1;
        if (index >= 0)
            return tokens.get(index);
        return null;
    }

    // insert comments

    enum CommentWhere {
        beforeLineBreak,        // whole line comment on line inserted before that containing token
        afterLineBreak,         // whole line comment on line inserted before line following that containing token
        beforeToken,            // insert /* comment */ before token
        afterToken,             // insert /* comment */ after token
        atEndOfLine             // append comment to line after //
    }

    public void insertComment(String comment, ParseTree pt, CommentWhere where)
    {
        int idx = where == CommentWhere.beforeToken || where == CommentWhere.beforeLineBreak
                ? pt.getSourceInterval().a : pt.getSourceInterval().b;
        insertComment(comment, idx, where);
    }

    public void insertComment(String comment, Token token, CommentWhere where)
    {
        insertComment(comment, token.getTokenIndex(), where);
    }

    public void insertComment(String comment, int tokenIndex, CommentWhere where)
    {
        Token token, tokenWS;
        String s;
        switch (where)
        {
            case beforeLineBreak:
                for (token = null; tokenIndex >= 0; tokenIndex--)
                    if (Java8Parser.LB == (token = tokens.get(tokenIndex)).getType())
                        break;
                s = getText(token); // have to get text, because it could be multiline
                if (Java8Parser.WS == (tokenWS = getTokenFollowing(token)).getType())
                    s += getText(tokenWS); // Add indent
                s += "// " + comment + lineBreak;
                replace(token, s);
                break;
            case beforeToken:
                insertBefore(tokenIndex, "/* " + comment + " */ ");
                break;
            case afterToken:
                insertAfter(tokenIndex, " /* " + comment + " */");
                break;
            case afterLineBreak:
            case atEndOfLine:
                for (token = null; tokenIndex < tokens.size(); tokenIndex++)
                    if (Java8Parser.LB == (token = tokens.get(tokenIndex)).getType())
                        break;
                if (where == CommentWhere.afterLineBreak)
                {
                    s = "";
                    if (Java8Parser.WS == (tokenWS = getTokenFollowing(token)).getType())
                        s = getText(tokenWS); // Add indent
                    insertBefore(token, lineBreak + s + "// " + comment);
                }
                else
                    insertBefore(token, " // " + comment);
                break;
        }
    }

    // delete

    public void deleteAndAdjustWhitespace(Token token)
    {
        int i = token.getTokenIndex();
        deleteAndAdjustWhitespace(i, i);
    }

    public void deleteAndAdjustWhitespace(ParseTree pt)
    {
        Interval interval = pt.getSourceInterval();
        deleteAndAdjustWhitespace(interval.a, interval.b);
    }

    public void deleteAndAdjustWhitespace(int i, int j)
    {
        replaceAndAdjustWhitespace(i, j, "");
    }

    public void replaceAndAdjustWhitespace(Token token, String text)
    {
        int i = token.getTokenIndex();
        replaceAndAdjustWhitespace(i, i, text);
    }

    // replace

    public void replaceAndAdjustWhitespace(ParseTree pt, String text)
    {
        Interval interval = pt.getSourceInterval();
        replaceAndAdjustWhitespace(interval.a, interval.b, text);
    }

    boolean recommendKeepSeparate(int codePointL, int codePointR)
    {
        boolean idCharL = codePointL == '`' || Character.isJavaIdentifierPart(codePointL);
        boolean idCharR = codePointR == '`' || Character.isJavaIdentifierPart(codePointR);
        // ...characters may be from an identifier that has already been wrapped in back ticks to resolve a clash with
        // a swift keyword, hence we also consider back tick to be an identifier character.
        boolean keepSeparate = false;
        if (idCharL && idCharR)
            keepSeparate = true;
        else if (idCharR)
        {
            // crude and not comprehensive, but good enough for now...
            switch (codePointL)
            {
                case '=': case ',': case '?': case '}':
                case '*': case '/': case '+': case '-':
                keepSeparate = true;
                break;
            }
        }
        else if (idCharL)
        {
            switch (codePointR)
            {
                case '=': case ',': case '?': case '{': case '!':
                case '*': case '/': case '+': case '-':
                keepSeparate = true;
                break;
            }
        }
        return keepSeparate;
    }

    public void replaceAndAdjustWhitespace(int from, int to, String text)
    {
        // The aim here is to prevent text merging with adjacent tokens (and to separate as looks reasonable).

        // It would be much better if TokenStreamRewriter recorded alterations as token insertions, deletions, moves and
        // alterations, so that lexical and structural information is retained as much as possible, then we would just
        // need a final pass before the end of processing to insert essential whitespace tokens. However, it doesn't.

        // The problem is that because the insertions and replacements are expressed as strings, lexical information is
        // lost, and we can no longer be certain what the type of adjacent tokens is - it may well have already been
        // rewritten. Hence we have to fallback to testing the adjacent token text - what a pain. Which leads to this
        // fragile, messy code.

        // Furthermore, when we scan backwards and forwards for adjacent non-empty tokens, we can find 'residue': where
        // a whole rule context has been replaced with an empty string, getText for the first token returns empty, but
        // getText for the following token (i.e. inside the subtree of the context) can return the original token text
        // - rewriter doesn't expect us to be asking for this token's text, and does not apply correct transform - so
        // we see something that will not actually be output in the final rewritten stream. We can't trust what we see.
        // (Root cause is TokenStreamRewriter.reduceToSingleOperationPerIndex - operation per index only returns the
        // right operation for the first index of the range it changes.)
        // Hard one to workaround.

        // OK, worked around it - here's how: in ParseTreeRewriter, we maintain a changed interval map. When we scan
        // through adjacent text, if a token is in a changed interval, get the whole changed text, otherwise get the
        // token text.

        // Gotcha: adding separation whitespace is ok, but removing excess separation can be problematic: on a later
        // call to getText(), the rewriter can consolidate accumulated rewrite operations, such that a reduction of the
        // trailing WS token gets merged with whatever transformed text there is for the preceding token, i.e. combined
        // replacement text for a widened range of tokens, extending beyond the original range of significant (i.e.
        // non-white) tokens; if there is a further transformation on the significant range of tokens, a
        // replacement will be requested up to the end of the last significant token, but this now falls in the
        // middle of the consolidated replacement op, hence TokenStreamRewriter considers it an overlapping replacement
        // request and throws an exception. !!! What a pain. The ideal time for adjusting whitespace would be in an
        // extra pass at the end — if we still had lexical information about our replacements; but we do not. :-(

        String textL, textR;
        Interval interval;
        int i, tokenIdxL, tokenIdxR, lenL, lenR, wsL, wsR, wsWanted;
        int tokenIdxL_WSFrom, tokenIdxL_WSTo, tokenIdxR_WSFrom, tokenIdxR_WSTo;
        int codePointL, codePointR;
        boolean leftIsIndent;
        boolean keepSeparate;

        text = text.trim();

        // Assess left: count whitespace and stop when first non-white character reached
        tokenIdxL = from - 1; textL = null;
        tokenIdxL_WSFrom = tokenIdxL_WSTo = -1;
        codePointL = 0; wsL = 0; i = -1;
        assessL: while ( 0 <= tokenIdxL )
        {
            if (null != (interval = getChangedIntervalContaining(tokenIdxL, tokenIdxL)))
                textL = getText(interval);
            else
                textL = getText(tokenIdxL, tokenIdxL);
            for (lenL = textL.length(), i = lenL-1; 0 <= i; i--)
            {
                codePointL = textL.codePointAt(i);
            //  if (Character.SPACE_SEPARATOR != Character.getType(codePointL))
            //  ...doesn't work as expected - getType('\t') primary category is not SPACE_SEPARATOR, so test explicitly
                if (' ' != codePointL && '\t' != codePointL)
                    break assessL;
                wsL++;
                tokenIdxL_WSFrom = null != interval ? interval.a : tokenIdxL;
                if (tokenIdxL_WSTo == -1)
                    tokenIdxL_WSTo = null != interval ? interval.b : tokenIdxL;
            }
            tokenIdxL = null != interval ? interval.a - 1 : tokenIdxL - 1;
            textL = null;
        }
        // leftIsIndent = Character.LINE_SEPARATOR == Character.getType(codePointL)
        // ...doesn't work as expected - getType('\n') primary category is CONTROL not LINE_SEPARATOR, so test explicitly
        leftIsIndent = codePointL == '\n' || codePointL == '\r' || (0 > tokenIdxL && 0 > i);

        // Assess right: count whitespace and stop when first non-white character reached
        tokenIdxR = to + 1; textR = null;
        tokenIdxR_WSFrom = tokenIdxR_WSTo = -1;
        codePointR = 0; wsR = 0;
        assessR: while ( tokenIdxR < tokens.size() )
        {
            if (null != (interval = getChangedIntervalContaining(tokenIdxR, tokenIdxR)))
                textR = getText(interval);
            else
                textR = getText(tokenIdxR, tokenIdxR);
            for (i = 0, lenR = textR.length(); i < lenR; i++)
            {
                codePointR = textR.codePointAt(i);
            //  if (Character.SPACE_SEPARATOR != Character.getType(codePointR))
            //  ...doesn't work as expected - getType('\t') primary category is not SPACE_SEPARATOR, so test explicitly
                if (' ' != codePointR && '\t' != codePointR)
                    break assessR;
                wsR++;
                if (tokenIdxR_WSFrom == -1)
                    tokenIdxR_WSFrom = null != interval ? interval.a : tokenIdxR;
                tokenIdxR_WSTo = null != interval ? interval.b : tokenIdxR;
            }
            tokenIdxR = null != interval ? interval.b + 1 : tokenIdxR + 1;
            textR = null;
        }

        if (0 < text.length())
        {
            keepSeparate = recommendKeepSeparate(codePointL, text.codePointAt(0));
            wsWanted = keepSeparate ? 1 : 0;
            if (!leftIsIndent && wsL != wsWanted)
            {
                if (wsL == 0)
                {
                    // with no white space on the left, we can use the replacement text
                    text = " " + text;
                }
                else
                {
                    // We have an excess of whitespace
                    String s = tokenIdxL == tokenIdxL_WSTo
                             ? textL
                             : getText(tokenIdxL_WSFrom, tokenIdxL_WSTo);
                    s = s.substring(0, s.length() - (wsL - wsWanted)); // trim excess from end
                    replace(tokenIdxL_WSFrom, tokenIdxL_WSTo, s);
                }
            }

            // Same again for RHS
            keepSeparate = recommendKeepSeparate(text.codePointAt(text.length()-1), codePointR);
            wsWanted = keepSeparate ? 1 : 0;
            if (wsR != wsWanted)
            {
                if (wsR == 0)
                {
                    // with no white space on the right, we can use the replacement text
                    text += " ";
                }
                else
                {
                    // We have an excess of whitespace
                    String s = tokenIdxR == tokenIdxR_WSFrom
                             ? textR
                             : getText(tokenIdxR_WSFrom, tokenIdxR_WSTo);
                    s = s.substring(wsR - wsWanted); // trim excess from start
                    replace(tokenIdxR_WSFrom, tokenIdxR_WSTo, s);
                }
            }
        }
        else
        if (0 < from && to < tokens.size()-1)
        {
            keepSeparate = recommendKeepSeparate(codePointL, codePointR);
            wsWanted = keepSeparate && !leftIsIndent ? 1 : 0;
            if (leftIsIndent)
                ; // do nothing to left
            else if (wsL == wsWanted)
                wsWanted -= wsL;
            else if (wsL > wsWanted)
            {
                // We have an excess of whitespace
                String s = tokenIdxL == tokenIdxL_WSTo
                         ? textL
                         : getText(tokenIdxL_WSFrom, tokenIdxL_WSTo);
                s = s.substring(0, s.length() - (wsL - wsWanted)); // trim excess from end
                replace(tokenIdxL_WSFrom, tokenIdxL_WSTo, s);
                wsWanted = 0;
            }

            if (wsR > wsWanted)
            {
                String s = tokenIdxR == tokenIdxR_WSFrom
                         ? textR
                         : getText(tokenIdxR_WSFrom, tokenIdxR_WSTo);
                s = s.substring(wsR - wsWanted); // trim excess from start
                replace(tokenIdxR_WSFrom, tokenIdxR_WSTo, s);
            }
            else if (wsR < wsWanted)
            {
                text = " ";
            }
        }

        // Finally replace the requested interval
        replace(from, to, text);
    }
}
