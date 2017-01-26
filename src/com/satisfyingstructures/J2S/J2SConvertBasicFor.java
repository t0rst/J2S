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
import com.satisfyingstructures.J2S.antlr.Java8BaseVisitor;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;
import java.util.ArrayList;

public class J2SConvertBasicFor {

    protected final J2SRewriter rewriter;

    public J2SConvertBasicFor ( J2SRewriter rewriter ) { this.rewriter = rewriter; }

    public static void convert ( ParserRuleContext ctx, J2SRewriter rewriter )
    {
        J2SConvertBasicFor converter = new J2SConvertBasicFor(rewriter);
        String couldNotConvertBecause = converter.convertBasicForStatementToForInLoopOrSayWhyNot(ctx);
        if (null != couldNotConvertBecause)
        {
            // Insert the original as a comment
            TerminalNode tnA = ctx.getToken(Java8Parser.FOR, 0), tnB = ctx.getToken(Java8Parser.RPAREN, 0);
            if (null != tnA && null!= tnB)
            {
                CharStream cs = tnA.getSymbol().getInputStream();
                String s = cs.getText(Interval.of(tnA.getSymbol().getStartIndex(), tnB.getSymbol().getStopIndex()));
                rewriter.insertComment(s, ctx, J2SRewriter.CommentWhere.beforeLineBreak);
                rewriter.insertComment("...not converted to a for-in expression because " + couldNotConvertBecause + ".", ctx, J2SRewriter.CommentWhere.beforeLineBreak);
            }
            converter.convertBasicForStatementToWhileLoop(ctx);
        }
    }

    private void convertBasicForStatementToWhileLoop( ParserRuleContext ctx )
    {
        BasicForToWhileConverter cvt = new BasicForToWhileConverter();
        cvt.convert(ctx);
    }

    private class BasicForToWhileConverter extends Java8BaseVisitor<Object>
    {
        /*  Swift 3.0 eliminated the for(;;) statement from the language (for strong business cases such as 'It is rarely
            used', 'not very Swift-like' and 'The value of this construct is limited'). It is difficult and not always
            possible to map it to a for tuple in sequence loop, so this class implements the fallback conversion to an
            equivalent if ungainly while loop.

            We convert
                for ( forInit ; expression ; forNext )
                    statement
            to
                do {
                    forInit()
                    outer: while expression() {
                        inner: do {
                            statement
                        }
                        forNext()
                    }
                }

            We only need the inner do scope if there are nested continue statements. If this is the case,
            then we convert continue to {break inner} and break to {break outer}. If this is not the case, then we don't
            need the while loop label and we don't need to convert break statements.

            We only need the outer do scope if variables are declared in the forInit, so as to avoid
            redeclaration of variables in the enclosing scope, and then only if the for statement has siblings at the
            same level, i.e. it is within a block.

            There are also degenerate cases where scopes are not needed because forInit, expression and forNext are
            optional and statement can be empty.

            We start by visiting the for statement subtree and collecting all the break and continue contexts that apply
            to the scope of the for statement, i.e., that do not apply to sub-scopes, and do not already jump out to a
            labelled scope.

            Then we decide whether we need to create the additional do scopes, and process accordingly.
        */
        List<Java8Parser.BreakStatementContext> breakStatementsToConvert;
        List<Java8Parser.ContinueStatementContext> continueStatementsToConvert;
        int switchDepth = 0;
        //
        private void convert( ParserRuleContext ctx )
        {
            int forRule = ctx.getRuleIndex();
            if (forRule != Java8Parser.RULE_basicForStatement && forRule != Java8Parser.RULE_basicForStatementNoShortIf)
                return; // not our expected parameter type
            if (null != breakStatementsToConvert)
                return; // we're already busy

            // Init instance variables
            switchDepth = 0;
            breakStatementsToConvert = new ArrayList<>();
            continueStatementsToConvert = new ArrayList<>();

            // Get to know more about our for statement…
            // 'for' '(' forInit? ';' expression? ';' forUpdate? ')' ( statement | statementNoShortIf )
            Boolean noShortIf = forRule == Java8Parser.RULE_basicForStatementNoShortIf;
            Java8Parser.ForInitContext forInitCtx = ctx.getChild(Java8Parser.ForInitContext.class, 0);
            Java8Parser.ExpressionContext expressionCtx = ctx.getChild(Java8Parser.ExpressionContext.class, 0);
            Java8Parser.ForUpdateContext forUpdateCtx = ctx.getChild(Java8Parser.ForUpdateContext.class, 0);
            ParserRuleContext statementCtx = ctx.getChild(noShortIf ? Java8Parser.StatementNoShortIfContext.class
                    : Java8Parser.StatementContext.class, 0);
            ParserRuleContext statementSubCtx = statementCtx.getChild(ParserRuleContext.class, 0);
            ParserRuleContext statementSubSubCtx = statementSubCtx.getChild(ParserRuleContext.class, 0);
            Boolean statementisEmpty = statementSubSubCtx.getRuleIndex() == Java8Parser.RULE_emptyStatement;

            // Assess whether we need an inner scope by visiting the subtree and gathering break and continue statements
            if (null != forUpdateCtx && !statementisEmpty)
                ctx.accept(this);
            Boolean needInnerScope = continueStatementsToConvert.size() > 0;

            // Assess whether we need an outer scope by checking if we have a forInit that declares local variables
            int forInitSubrule = null!=forInitCtx ? forInitCtx.getChild(ParserRuleContext.class, 0).getRuleIndex() : 0;
            Boolean needOuterScope = forInitSubrule == Java8Parser.RULE_localVariableDeclaration;

            // Assess and set up labels
            ParserRuleContext labelForCtx = null; // pre-existing label if any
            String labelFor = null;
            if (needInnerScope || needOuterScope)
            {   // If we are adding any labels, we need to know if there is a pre-existing label.
                ParserRuleContext parentCtx = ctx.getParent();       // forStatement || forStatementNoShortIf
                ParserRuleContext gparenCtx = parentCtx.getParent(); // statement || statementNoShortIf
                ParserRuleContext enclosingCtx = gparenCtx.getParent(); // ??? labeledStatement || labeledStatementNoShortIf
                int enclosingRule = enclosingCtx.getRuleIndex();
                if (enclosingRule == Java8Parser.RULE_labeledStatement
                        || enclosingRule == Java8Parser.RULE_labeledStatementNoShortIf)
                {
                    labelForCtx = enclosingCtx;
                    labelFor = labelForCtx.getToken(Java8Parser.Identifier, 0).getText();
                    // now step out to first enclosing context that is not a label
                    while (enclosingRule == Java8Parser.RULE_labeledStatement
                            || enclosingRule == Java8Parser.RULE_labeledStatementNoShortIf)
                    {
                        enclosingCtx = enclosingCtx.getParent().getParent();
                        enclosingRule = enclosingCtx.getRuleIndex();
                    }
                }
                // If we don't have siblings at our enclosing scope level, then we don't need to restrict our local
                // // variable definition with an outer scope.
                if (enclosingRule != Java8Parser.RULE_blockStatement
                        || enclosingCtx.getParent().getChildCount() == 1)
                    needOuterScope = false;
            }
            String labelInner = null;
            String labelOuter = null;
            if (needInnerScope)
            {
                if (null != labelFor)
                {
                    labelOuter = labelFor;
                    labelInner = labelOuter+"_statement";
                }
                else // make standard label suffixed with line number of first token
                {
                    int n = ctx.start.getStartIndex() % 1000;
                    labelOuter = "loop_"+n;
                    labelInner = "statement_"+n;
                }
            }

            /*  Do the token mapping

                We have four mappings based on NI(=needInnerScope) and NO(=needOuterScope), and within that we also have
                to give the right treatment for the optionality of forInit, expression and forUpdate.

                From:
                                'for' '(' forInit? ';' expression? ';' forUpdate? ')' statement
                To:
                -   -           (forInit '; ')? 'while' expression ?: 'true' ('{' statement forUpdate '}' | statement)
                NI  -           (forInit '; ')? labelOuter ': while' expression ?: 'true'
                                    '{ ' labelInner ': do {' statement '}; ' forUpdate '}'
                -   NO          'do {' forInit '; while' expression ?: 'true'
                                    ('{' statement forUpdate '}' | statement) '}'
                NI  NO          'do {' forInit '; ' labelOuter ': while' expression ?: 'true'
                                    '{ ' labelInner ': do {' statement '}; ' forUpdate '}}'

                We also have tricky complications:
                    1) The for may already be labelled. If so, and we need to add an outer scope, then the label has to
                    still refer to the for loop and not the new scope. We either have to wrap the labelled statement in
                    the new scope or suppress the label and replicate it inside the new scope.
                    2) This method is invoked when the listener is hearing an exit message, i.e. after stepping out from
                    a traversed subtree. This means that other listener methods will have recorded changes to the
                    subtree in the rewriter. In particular,
                        a) J2SwiftListener.wrapStatementInBracesIfNecessary() may have enclosed our statement in curly
                        braces, and because we need to insert the closing of a new inner scope inside those braces, we
                        need to detect and handle the cases of pre-existing and newly added braces separately.
                        b) when we move forUpdate to be after statement, we need to getText for the forUpdate from the
                        rewriter, so that we preserve what has already been transformed.

                Conversions for each token:
                T1 'for'    : if needOuterScope 'do {' else deleted
                T2 '('      : delete
                T3 forInit? : unchanged
                T4 ';'      : set to '' if !forInit,
                            plus
                              if labelOuter, append ' '+labelOuter+':' ,
                              else
                              if labelFor, append ' '+labelFor+':'
                              and if needOuterScope && labelFor delete labelForCtx.Identifier and ':',
                            plus
                              append ' while'
                            plus
                              if !expression, append ' true'
                T5 expression? : unchanged
                T6 ';'      : delete
                T7 forUpdate: if present, getText of this token from rewriter and save for later, then delete forUpdate
                T8 ')'      : if needInnerScope replace with '{ do', otherwise delete
                T9 statement: (this should alsways start and stop with braces - if not pre-existing, then by processing)
                              if !needInnerScope we need to insert the saved forUpdate text before the closing brace,
                              else append ' ' + saved forUpdate text;
                            plus
                              if needOuterScope append '}'

                If needInnerScope, we also need to add destination labels to continue and break statements
            */

            // Add destination labels to continue and break statements. Do this now, before we operate on statementCtx
            // as a whole.
            if (needInnerScope)
            {
                for (Java8Parser.ContinueStatementContext continueCtx : continueStatementsToConvert)
                    rewriter.replace(continueCtx.start, "break "+labelInner);
                for (Java8Parser.BreakStatementContext breakCtx : breakStatementsToConvert)
                    rewriter.replace(breakCtx.start, "break "+labelOuter);
            }

            TerminalNode tn;
            Token token;
            String replacement;
            int startIdx, stopIdx;

            // T1 'for'    : if needOuterScope 'do {' else deleted
            token = (tn = ctx.getToken(Java8Parser.FOR, 0)).getSymbol();
            if (needOuterScope)
                rewriter.replace(token, "do {");
            else
                rewriter.deleteAndAdjustWhitespace(token);

            // T2 '('      : delete
        //  token = (tn = ctx.getToken(Java8Parser.LPAREN, 0)).getSymbol();
        //  rewriter.deleteAndAdjustWhitespace(token);
        // ...now deleted separately

            // T3 no change

            // T4 ';' - see comment above
            token = (tn = ctx.getToken(Java8Parser.SEMI, 0)).getSymbol();
            replacement = null != forInitCtx ? ";" : "";
            if (null != labelFor) // already labelled
            {
                if (needOuterScope) // and need to move the label inside the new outer scope
                {
                    replacement += " "+labelFor+":";
                    // delete the one outside
                    rewriter.replace(labelForCtx.getToken(Java8Parser.Identifier, 0).getSymbol(), null);
                    rewriter.replace(labelForCtx.getToken(Java8Parser.COLON, 0).getSymbol(), null);
                }
            }
            else if (null != labelOuter) // adding new label
                replacement += " "+labelOuter+":";
            replacement += " while ";
            if (null == expressionCtx)
                replacement += " true";
            rewriter.replaceAndAdjustWhitespace(token, replacement);

            // T5 expression? : unchanged

            // T6 ';'      : delete
            token = (tn = ctx.getToken(Java8Parser.SEMI, 1)).getSymbol();
            rewriter.deleteAndAdjustWhitespace(token);

            // T7 forUpdate: if present, getText of this token from rewriter and save for later, then delete forUpdate
            String forUpdateText = null;
            if (null != forUpdateCtx)
            {
                forUpdateText = rewriter.getText(forUpdateCtx);
                rewriter.delete(forUpdateCtx);
            }

            // T8 ')'      : if needInnerScope replace with '{ do', otherwise delete
            token = (tn = ctx.getToken(Java8Parser.RPAREN, 0)).getSymbol();
        //  rewriter.replace(token, needInnerScope ? "{ "+labelInner+": do" : null);
        // ...now deleted separately
            if (needInnerScope)
                rewriter.insertAfter(token, "{ "+labelInner+": do ");

            // T9 statement: insert forUpdateText before closing brace if !needInnerScope, else append "until false" + forUpdateText + close brace
            token = null;
            Interval interval;
            interval = Interval.of(statementCtx.stop.getTokenIndex(), statementCtx.stop.getTokenIndex());
            //  interval = Interval.of(statementCtx.start.getTokenIndex(), statementCtx.stop.getTokenIndex());
            replacement = rewriter.getText(interval);
            int i = replacement.lastIndexOf("}");
            String beforeBrace = i==-1 ? replacement : replacement.substring(0, i++);
            int l = i==-1 ? 0 : replacement.length() - i;
            String afterBrace = l>0 ? replacement.substring(i, l) : "";
            if (null != forUpdateText)
            {
                if (needInnerScope)
                    afterBrace = "; "+forUpdateText+"; } "+afterBrace;
                else
                    beforeBrace += " "+forUpdateText+"; ";
            }
            if (needOuterScope)
                afterBrace += " }";
            replacement = beforeBrace+"}"+afterBrace;
            rewriter.replaceAndAdjustWhitespace(interval.a, interval.b, replacement);
        }
        // labelling
        @Override public Object visitBreakStatement(Java8Parser.BreakStatementContext ctx)
        {
            if ( null == ctx.Identifier() ) // add destination if the break has not already got one
                breakStatementsToConvert.add(ctx);
            return null;
        }
        @Override public Object visitContinueStatement(Java8Parser.ContinueStatementContext ctx)
        {
            if ( null == ctx.Identifier() ) // add destination if the continue has not already got one
                continueStatementsToConvert.add(ctx);
            return null;
        }
        // skip - these subtrees start a new scope for break and continue:
        @Override public Object visitWhileStatement(Java8Parser.WhileStatementContext ctx) {return null;}
        @Override public Object visitForStatement(Java8Parser.ForStatementContext ctx) {return null;}
        // skip - per statement type
        @Override public Object visitStatementWithoutTrailingSubstatement(
                Java8Parser.StatementWithoutTrailingSubstatementContext ctx)
        {
            int statementSubRule = ctx.getChild(ParserRuleContext.class, 0).getRuleIndex();
            switch ( statementSubRule )
            {
                //  We comment out the cases that we do not want to descend into
                case Java8Parser.RULE_breakStatement:           // we want to process this
                    if (switchDepth > 0) // but only if its not inside a switch
                        break;
                case Java8Parser.RULE_continueStatement:        // we want to process this
                case Java8Parser.RULE_block:                    // can contain deeper nested break and continue
                    //  case Java8Parser.RULE_emptyStatement:           // cannot contain break and continue
                    //  case Java8Parser.RULE_expressionStatement:      // cannot contain break and continue
                    //  case Java8Parser.RULE_assertStatement:          // cannot contain break and continue
                    //  case Java8Parser.RULE_doStatement:              // starts new scope for both break and continue
                    //  case Java8Parser.RULE_returnStatement:          // cannot contain break and continue
                case Java8Parser.RULE_synchronizedStatement:    // can contain deeper nested break and continue
                    //  case Java8Parser.RULE_throwStatement:           // cannot contain break and continue
                case Java8Parser.RULE_tryStatement:             // can contain deeper nested break and continue
                    visitChildren(ctx);
                    break;
                case Java8Parser.RULE_switchStatement:          // starts new scope for break, but not continue
                    switchDepth++;
                    visitChildren(ctx);
                    switchDepth--;
                    break;
                default:
                    break;
            }
            return null;
        }
    }

    private String convertBasicForStatementToForInLoopOrSayWhyNot(ParserRuleContext ctx )
    {
        int forRule = ctx.getRuleIndex();
        if (forRule != Java8Parser.RULE_basicForStatement && forRule != Java8Parser.RULE_basicForStatementNoShortIf)
            return "statement kind is not as expected"; // not our expected parameter type
        // Get to know more about our for statement…
        // 'for' '(' forInit? ';' expression? ';' forUpdate? ')' ( statement | statementNoShortIf )
        Boolean noShortIf = forRule == Java8Parser.RULE_basicForStatementNoShortIf;
        Java8Parser.ForInitContext forInitCtx = ctx.getChild(Java8Parser.ForInitContext.class, 0);
        Java8Parser.ExpressionContext expressionCtx = ctx.getChild(Java8Parser.ExpressionContext.class, 0);
        Java8Parser.ForUpdateContext forUpdateCtx = ctx.getChild(Java8Parser.ForUpdateContext.class, 0);
        ParserRuleContext statementCtx = ctx.getChild(noShortIf ? Java8Parser.StatementNoShortIfContext.class
                : Java8Parser.StatementContext.class, 0);
        ParserRuleContext statementSubCtx = statementCtx.getChild(ParserRuleContext.class, 0);
        ParserRuleContext statementSubSubCtx = statementSubCtx.getChild(ParserRuleContext.class, 0);
        Boolean statementisEmpty = statementSubSubCtx.getRuleIndex() == Java8Parser.RULE_emptyStatement;
        /*
            'for' '(' forInit? ';' expression? ';' forUpdate? ')' ( statement | statementNoShortIf )

            Swift 3.0 has got rid of for(;;) statements for stong business cases such as...
                'It is rarely used'
                'not very Swift-like'
                'The value of this construct is limited'
            ...and other total crap.

            We can convert simple equivalents of
                for ( i = startvalue ; i < endvalue ; i += step)
            to
                for i in start..<end
            or
                for i in start.stride(to: end by: step)

            To identify this we look for
            1) have a forUpdate, which...
                a) operates on a single loop variable
                    forUpdate().statementExpressionList().statementExpression().count()==1
                b) incorporates increment or decrement by a constant step (++i,i++,i+=step,--i,i--,i-=step,)
                    statementExpression rule is RULE_(assignment|preinc|postinc|predec|postdec)
                c) operates on the same variable tested in expression (compare - 2b)
            2) have an expression, which...
                a) should be a simple comparison (<,<=,!=,>,>=, implicit non-zero)
                b) one side should be same as the loop var (compare - 1c)
                c) other side should not mutate within the loop - we can't tell this, too difficult
            3) forInit
                a) must be
                    i) empty(start with loop var existing value), or
                    ii) simple init of a single loop var, or
                    iii) simple declaration of a loop var
        */
        // 1) Update statement. We need one...
        if (null == forUpdateCtx)
            return "it lacks an update statement";
        // 1a) and it must operate on a single variable
        if (forUpdateCtx.statementExpressionList().getChildCount() != 1)
            return "there is more than one expression in the update statement";
        // 1b) and it must be a simple increment or decrement
        Java8Parser.StatementExpressionContext updateStatementExpressionCtx =
            forUpdateCtx.statementExpressionList().statementExpression(0);
        //  statementExpression : assignment | preIncrementExpression | preDecrementExpression
        //                                   | postIncrementExpression | postDecrementExpression
        //                                   | methodInvocation | classInstanceCreationExpression
        ParserRuleContext updateExpressionCtx = updateStatementExpressionCtx.getChild(ParserRuleContext.class, 0);
        int updateExpressionRule = updateExpressionCtx.getRuleIndex();
        boolean ascending_sequence;
        boolean open_interval;
        ParserRuleContext stepExpressionCtx = null;
        switch (updateExpressionRule) {

            // unaryExpression : preIncrementExpression | preDecrementExpression
            //                 | '+' unaryExpression | '-' unaryExpression
            //                 | unaryExpressionNotPlusMinus
            // preDecrementExpression : '--' unaryExpression
            // preIncrementExpression : '++' unaryExpression
            case Java8Parser.RULE_preDecrementExpression:   ascending_sequence = false;     break;
            case Java8Parser.RULE_preIncrementExpression:   ascending_sequence = true;      break;

            // postfixExpression : ( primary | expressionName ) ( '++' | '--')*
            // postIncrementExpression : postfixExpression '++'
            // postDecrementExpression : postfixExpression '--'
            case Java8Parser.RULE_postDecrementExpression:  ascending_sequence = false;     break;
            case Java8Parser.RULE_postIncrementExpression:  ascending_sequence = true;      break;

            // assignment : leftHandSide assignmentOperator expression
            // leftHandSide : expressionName | fieldAccess | arrayAccess
            case Java8Parser.RULE_assignment:
                if (null != updateStatementExpressionCtx.assignment().leftHandSide().arrayAccess())
                    return "cant convert a loop variable that is an array element";
                TerminalNode node = updateStatementExpressionCtx.assignment().assignmentOperator().getChild(TerminalNode.class, 0);
                switch (node.getSymbol().getType())
                {
                    case Java8Parser.ADD_ASSIGN:    ascending_sequence = true;      break;
                    case Java8Parser.SUB_ASSIGN:    ascending_sequence = false;     break;
                    case Java8Parser.ASSIGN:        // possibilities too complex to warrant extracting simple a=a+1 cases
                    default:                        return "potentially too complex to create a sequence from this update operation";
                }
                stepExpressionCtx = updateStatementExpressionCtx.assignment().expression();
                break;
            default: // methodInvocation | classInstanceCreationExpression
                return "the expression in the update statement is too complex";
        }
        // In each of the cases that we have not rejected, the loop variable is in the first child rule context of the
        // update statement. Get the text of the variable, rather than analysing the graph any further, as the
        // possibilities are endless; all that we require is that the loop variable text matches that in the text
        // expression and the init expression.
        ParserRuleContext loopVariable_updated_Ctx = updateExpressionCtx.getChild(ParserRuleContext.class, 0);
        String loopVariableTxt = loopVariable_updated_Ctx.getText(); // we want original text

        // 2) Expression
        if (null == expressionCtx)
            return "it lacks a test expression";
        // expression : lambdaExpression | assignmentExpression
        if (null != expressionCtx.lambdaExpression())
            return "cannot convert a lambda expression";
        // assignmentExpression : conditionalExpression | assignment
        if (null != expressionCtx.assignmentExpression().assignment())
            return "cannot convert an assignment within the test expression";
        // 2a) must be a simple relation:
        // Descend the chain of expression rule pass-through branches until we find the one that is significant, then
        // test to see if expression contains a terminal that is one of !=, <, <=, >, >=.
        ParserRuleContext testExpressionCtx = J2SGrammarUtils.descendToSignificantExpression(expressionCtx);
        int testExpressionRule = testExpressionCtx.getRuleIndex();
        TerminalNode node = testExpressionCtx.getChild(TerminalNode.class, 0);
        int testOperatorType = null != node ? node.getSymbol().getType() : 0;
        switch (testOperatorType)
        {
            case Java8Parser.NOTEQUAL:  open_interval = true;   break;
            case Java8Parser.LE:        open_interval = false;  break;
            case Java8Parser.GE:        open_interval = false;  break;

            case Java8Parser.LT: // can occur in relational and shift expressions
            case Java8Parser.GT: // can occur in relational and shift expressions
                if (testExpressionRule == Java8Parser.RULE_relationalExpression)
                {
                    open_interval = true;
                    break;
                }
            default:
                return "can only convert test expressions that use !=, <, <=, > or >=";
        }
        // 2b) relation must be testing same var as changed in update expression
        // The loop variable could be on the left or the right of the comparison operator
        int i;
        ParserRuleContext loopVariable_tested_Ctx = null;
        for (i = 0; i < 2; i++)
        {
            loopVariable_tested_Ctx = testExpressionCtx.getChild(ParserRuleContext.class, i);
            if (null != loopVariable_tested_Ctx
             && loopVariableTxt.equals(loopVariable_tested_Ctx.getText()))
                break; // found matching loop variable
            loopVariable_tested_Ctx = null;
        }
        if (null == loopVariable_tested_Ctx || (i == 1 && testExpressionCtx.getChildCount() > 3))
            return "the test expression must be testing the same variable as changed in update expression";
        ParserRuleContext terminalValueCtx = testExpressionCtx.getChild(ParserRuleContext.class, i^1);
        // 2c) the terminal value side should not mutate within the loop
        // - way too difficult for us to determine this

        // 3) Loop init expression. Must be either...
        ParserRuleContext initialValueCtx;
        if (null == forInitCtx) // a) empty
        {
            // Using the loop variable's existing value from outside the scope
            initialValueCtx = loopVariable_tested_Ctx;
        }
        else
        if (null != forInitCtx.statementExpressionList()) // b) a simple init of a single loop var
        {
/*
        // Could not convert...
        // for (i = 0; i<10; i++)
        // ...to a for..in statement because can only work with an assignment expression for loop variable initialisation.
        i = 0; while i<10  {j += 1 i += 1; }
*/
            if (forInitCtx.statementExpressionList().getChildCount() != 1)
                return "can only work with initialisation of a single loop variable";
            Java8Parser.StatementExpressionContext initExpressionCtx =
                forInitCtx.statementExpressionList().statementExpression(0);
            if (null == initExpressionCtx.assignment())
                return "can only work with an assignment expression for loop variable initialisation";
            if (!loopVariableTxt.equals(initExpressionCtx.assignment().leftHandSide().getText()))
                return "the initialised variable is different from the updated variable"; // different to the loop variable
            initialValueCtx = initExpressionCtx.assignment().expression();
        }
        else
        if (null != forInitCtx.localVariableDeclaration()) // c) a simple decl of a single loop var
        {
            // localVariableDeclaration : variableModifier* unannType variableDeclaratorList
            Java8Parser.VariableDeclaratorListContext vdlc = forInitCtx.localVariableDeclaration().variableDeclaratorList();
            // variableDeclaratorList : variableDeclarator (',' variableDeclarator)*
            if (vdlc.getChildCount() != 1)
                return "can only work with declaration of a single loop variable";
            Java8Parser.VariableDeclaratorContext vdc = vdlc.variableDeclarator(0);
            // variableDeclarator : variableDeclaratorId ('=' variableInitializer)?
            if (!loopVariableTxt.equals(vdc.getChild(0).getText()))
                return "the declared loop variable is be different from the updated variable";
            initialValueCtx = vdc.variableInitializer();
            if (null == initialValueCtx)
                return "there is no initialiser for the loop variable";
        }
        else
            return "loop initialisation is in unexpected form";

        // Now we have all the components we need
        String forInLoopText;
        // Use actual text with replacements
        String initialValueTxt = rewriter.getText(initialValueCtx);
        String terminalValueTxt = rewriter.getText(terminalValueCtx);
        // !!!: watch out...
        // if we use the actual text from the update expression, we can find that the pre/post-inc/dec has been
        // converted to the add/sub-assign form and because structure is lost when rewriting, the new form can
        // stick to the variable when we retrieve it. There's no easy solution for this (and any similar occurrences),
        // but we side step it by getting the text of loop variable from the test expression:
        loopVariableTxt = rewriter.getText(loopVariable_tested_Ctx);
        if (null != stepExpressionCtx || !ascending_sequence)
        {
            String stepExpressionText = stepExpressionCtx == null ? "-1" : ascending_sequence ? rewriter.getText(stepExpressionCtx) : "-(" + rewriter.getText(stepExpressionCtx) + ")";
            forInLoopText = "for " + loopVariableTxt + " in " + loopVariableTxt + ".stride(from: " + initialValueTxt + (open_interval ? ", to: " : ", through: ") + terminalValueTxt + ", by: " + stepExpressionText + ")";
        }
        else
        {
            forInLoopText = "for " + loopVariableTxt + " in " + initialValueTxt + (open_interval ? " ..< " : " ... ") + terminalValueTxt;
        }

        Token startToken = ctx.getToken(Java8Parser.FOR, 0).getSymbol();
        Token endToken = ctx.getToken(Java8Parser.RPAREN, 0).getSymbol();

        CharStream cs = startToken.getInputStream();
        String originalExpressionTxt = cs.getText(Interval.of(startToken.getStartIndex(), endToken.getStopIndex()));
        rewriter.insertComment(originalExpressionTxt + " …converted to…", ctx, J2SRewriter.CommentWhere.beforeLineBreak);

        int startIndex = startToken.getTokenIndex();
        int endIndex = endToken.getTokenIndex();

        // Problem: (see notes in J2SRewriter.replaceAndAdjustWhitespace) Before converting to for-in, the loop will
        // also have had parentheses removed (and other transforms); rewriter may have coallesced some of the changes
        // so that the old end boundary no longer exists. (- a shortcoming of TokenStreamRewriter)
        // Workaround: test if endIndex is straddled by changed interval, and if so, extend our interval to the end of
        // the change. (Pretty horrendous to have to work around this here, but I don't yet see an easy way of fixing
        // the underlying problem or a generalised way of working around it.)
        Interval interval = rewriter.getChangedIntervalContaining(endIndex, endIndex);
        if (null != interval && interval.a <= endIndex && interval.b > endIndex)
            endIndex = interval.b;

        rewriter.replaceAndAdjustWhitespace(startIndex, endIndex, forInLoopText);

        return null;
    }
}
