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
 * Created by Torsten Louland on 22/10/2016.
 */
package com.satisfyingstructures.J2S;

import com.satisfyingstructures.J2S.antlr.Java8Parser;

import org.antlr.v4.runtime.*;


/// J2SGrammarUtils provides helpers that encapsulate some limited knowledge of the Java8 grammar
public class J2SGrammarUtils
{
    public static ParserRuleContext descendToSignificantExpression(ParserRuleContext ctx)
    {
        // These expressions are chained by precedence in the grammar. We descend into the subrulecontexts until we
        // find a rule context that is significant, i.e. its not just a context with one child that is another
        // expression.
        // First validate parameter
        switch (ctx.getRuleIndex()) {
            case Java8Parser.RULE_constantExpression:
            case Java8Parser.RULE_expression:
            case Java8Parser.RULE_assignmentExpression:
            case Java8Parser.RULE_conditionalExpression:
            case Java8Parser.RULE_conditionalOrExpression:
            case Java8Parser.RULE_conditionalAndExpression:
            case Java8Parser.RULE_inclusiveOrExpression:
            case Java8Parser.RULE_exclusiveOrExpression:
            case Java8Parser.RULE_andExpression:
            case Java8Parser.RULE_equalityExpression:
            case Java8Parser.RULE_relationalExpression:
            case Java8Parser.RULE_shiftExpression:
            case Java8Parser.RULE_additiveExpression:
            case Java8Parser.RULE_multiplicativeExpression:
            case Java8Parser.RULE_unaryExpression:
            case Java8Parser.RULE_unaryExpressionNotPlusMinus:
            case Java8Parser.RULE_postfixExpression:
            case Java8Parser.RULE_primary:
            case Java8Parser.RULE_primaryNoNewArray_lfno_primary:
                break;
            default:
                return ctx; // not an acceptable parameter type
        }
        descent: while (ctx.getChildCount() == 1)
        {
            ParserRuleContext childCtx = ctx.getChild(ParserRuleContext.class, 0);
            if (null == childCtx)
                break;
            switch (ctx.getRuleIndex())
            {
                case Java8Parser.RULE_unaryExpression:
                    if (childCtx.getRuleIndex() != Java8Parser.RULE_unaryExpressionNotPlusMinus)
                        break descent;
                case Java8Parser.RULE_unaryExpressionNotPlusMinus:
                    if (childCtx.getRuleIndex() != Java8Parser.RULE_postfixExpression)
                        break descent;
                case Java8Parser.RULE_postfixExpression:
                    if (childCtx.getRuleIndex() != Java8Parser.RULE_primary)
                        break descent;
            }
            ctx = childCtx;
        }
        return ctx;
    }

}
