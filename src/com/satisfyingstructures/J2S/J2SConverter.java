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

import com.satisfyingstructures.J2S.antlr.Java8Parser;
import com.satisfyingstructures.J2S.antlr.Java8BaseListener;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.misc.Interval;

import java.util.*;

class J2SConverter extends Java8BaseListener {

    // ---------------------------------------------------------------------------------------------
    // Setup.
    private final J2SRewriter rewriter;
    private final Map<String, String> options;
    private J2SStringMapper types;
    private J2SStringMapper modifiers;
    private J2SStringMapper annotationMapper;

    J2SConverter(J2SRewriter rewriter, Map<String, String> options, Map<String, String> moreTypeMappings) {
        this.options = options;
        this.rewriter = rewriter;

        modifiers = new J2SStringMapper();
        String protectedMapsTo = option("omitprotected") ? "" : "/*protected*/";
        modifiers.addPairs(
            //  "final",        "", // final variable converted elsewhere to constant; final class or func needs to remain
            //  "static",       "/*static*/", - ok to remain, except as nested class modifier
            //  "public",       "/*public*/", - ok to remain
                "protected",    protectedMapsTo, // no equivalent in Swift; omit --> internal access
            //  "private",      "/*private*/",
                "abstract",     "/*abstract*/", // not in Swift
                "strictfp",     "/*strictfp*/", // java specific
                "native",       "/*native*/",   // native method --> ???
                "synchronized", "/*synchronized*/", // not at language level in Swift
                "default",      "/*default*/",  // default interface method --> ???
                "volatile",     "/*volatile*/", // not at language level in Swift

                "",""
        );

        annotationMapper = new J2SStringMapper();
        annotationMapper.addPairs(
                "@Override",    "override",

                "",""
        );

        types = new J2SStringMapper();
        types.addPairs(
                // primitives
                "boolean",  "Bool",
                "byte",     "Int8",
                "short",    "Int16",
                "int",      "Int",
                "long",     "Int64",
                "char",     "UInt16",
                "float",    "Float",
                "double",   "Double",
                // java.lang primitive wrappers
                "Boolean",  "Bool",
                "Byte",     "Int8",
                "Short",    "Int16",
                "Integer",  "Int",
                "Long",     "Int64",
                "Char",     "UInt16",
                "Float",    "Float",
                "Double",   "Double",
                // java.lang
                "Object",   "AnyObject",
                // some common java.util types (ought to add these when we see relevant import statements)
                "List",     "Array",
                "ArrayList","Array",
                "Map",      "Dictionary",
                "HashMap",  "Dictionary",
                "Set",      "Set",
                "HashSet",  "Set",
            //  "Vector",   "Array",
            //  to do: need to add warning where mapping vector to array to ensure sync access with vars of this type

                "",""
        );
        if (null != moreTypeMappings)
            types.addMap(moreTypeMappings);
    }

    private boolean option(String key)
    {
        String value = options.get(key);
        if (null != value)
        switch (value)
        {
            case "": // key was just defined
            case "1":
            case "Y":
            case "YES":
            case "true":
            case "TRUE":
                return true;
        }
        return false;
    }

    // ---------------------------------------------------------------------------------------------
    // Helpers.

    private void mapModifierToken(Token modifierToken)
    {
        String stringInJava = modifierToken.getText();
        String stringInSwift = modifiers.map(stringInJava);
        if (null != stringInSwift && !stringInSwift.equals(stringInJava))
        {
            if (0 == stringInSwift.length())
                rewriter.deleteAndAdjustWhitespace(modifierToken);
            else
                rewriter.replace(modifierToken, stringInSwift);
        }
    }

/*
    private void mapAnnotationContext(ParserRuleContext ctx)
    {
    }
    @SuppressWarnings("unused")
    @SuppressLint("DefaultLocale")

*/

    private void mapInitialModifiersInContext(ParserRuleContext ctx)
    {
        ParseTree pt;
        int i = 0;
        while (null != (pt = ctx.getChild(i++)) && ParserRuleContext.class.isInstance(pt) )
        {
            ParserRuleContext context = (ParserRuleContext)pt;
            int contextRuleIndex = context.getRuleIndex();
            switch (contextRuleIndex)
            {
                case Java8Parser.RULE_interfaceModifier:
                case Java8Parser.RULE_interfaceMethodModifier:
                case Java8Parser.RULE_classModifier:
                case Java8Parser.RULE_constructorModifier:
                case Java8Parser.RULE_methodModifier:
                case Java8Parser.RULE_annotationTypeElementModifier:
                case Java8Parser.RULE_variableModifier:
                case Java8Parser.RULE_constantModifier:
                case Java8Parser.RULE_fieldModifier:
                    break;
                default:
                    return;
            }
            TerminalNode tn = context.getChild(TerminalNode.class, 0);
            if (null == tn)
                continue; // annotation - dealt with separately
            Token token = tn.getSymbol();
            // Handle special token cases
            switch (token.getType())
            {
                case Java8Parser.STATIC:
                    if (contextRuleIndex == Java8Parser.RULE_classModifier)
                    {
                        rewriter.deleteAndAdjustWhitespace(tn); // static not allowed for classes in Swift
                        continue;
                    }
                    break;
                case Java8Parser.PRIVATE:
                case Java8Parser.PROTECTED:
                case Java8Parser.PUBLIC:
                    if (contextRuleIndex == Java8Parser.RULE_interfaceMethodModifier)
                    {
                        rewriter.deleteAndAdjustWhitespace(tn); // access control not allowed for protocols in Swift
                        continue;
                    }
                    break;
            }
            mapModifierToken(token);
        }
        
    }

    private void mapPrimitiveTypeInContext(ParserRuleContext ctx)
    {
        String stringInJava = ctx.getText();
        String stringInSwift = types.map(stringInJava);
        if (null != stringInSwift && !stringInSwift.equals(stringInJava))
            rewriter.replace(ctx, stringInSwift);
    }

    private void mapClassIdentifierInContext(ParserRuleContext ctx)
    {
        TerminalNode tn = ctx.getToken(Java8Parser.Identifier, 0);
        Token classIdentifierToken = null != tn ? tn.getSymbol() : null;
        if (null == classIdentifierToken)
            return;
        String stringInJava = classIdentifierToken.getText();
        String stringInSwift = types.map(stringInJava);
        if (null != stringInSwift && !stringInSwift.equals(stringInJava))
            rewriter.replace(classIdentifierToken, stringInSwift);
    }

    // ---------------------------------------------------------------------------------------------
    // Listener members. Uncomment when we need an implemetation. Leave commented out when unused so 
    // that class member function list only contains salient functions.
    //

    /*  General members from interface ParseTreeListener
	@Override public void visitErrorNode(ErrorNode node) {}
    @Override public void enterEveryRule(ParserRuleContext ctx) {}
    @Override public void exitEveryRule(ParserRuleContext ctx) {}
    */
    @Override public void visitTerminal(TerminalNode node)
    {
        // Some tokens have simple transforms wherever they appear, so are easier to deal with here.
        // (...added after many simple tokens dealt with elsewhere.)
        Token token = node.getSymbol();
        int tokenType = token.getType();
        switch (tokenType)
        {
        //  case Java8Parser.ABSTRACT:
        //  case Java8Parser.ASSERT:
        //  case Java8Parser.BOOLEAN:
        //  case Java8Parser.BREAK:
        //  case Java8Parser.BYTE:
        //  case Java8Parser.CASE:
        //  case Java8Parser.CATCH:
        //  case Java8Parser.CHAR:
        //  case Java8Parser.CLASS:
        //  case Java8Parser.CONST:
        //  case Java8Parser.CONTINUE:
        //  case Java8Parser.DEFAULT:
        //  case Java8Parser.DO:
        //  case Java8Parser.DOUBLE:
        //  case Java8Parser.ELSE:
        //  case Java8Parser.ENUM:
        //  case Java8Parser.EXTENDS:
        //  case Java8Parser.FINAL:
        //  case Java8Parser.FINALLY:
        //  case Java8Parser.FLOAT:
        //  case Java8Parser.FOR:
        //  case Java8Parser.IF:
        //  case Java8Parser.GOTO:
        //  case Java8Parser.IMPLEMENTS:
        //  case Java8Parser.IMPORT:
        //  case Java8Parser.INSTANCEOF:
        //  case Java8Parser.INT:
        //  case Java8Parser.INTERFACE:
        //  case Java8Parser.LONG:
        //  case Java8Parser.NATIVE:
        //  case Java8Parser.NEW:
        //  case Java8Parser.PACKAGE:
        //  case Java8Parser.PRIVATE:
        //  case Java8Parser.PROTECTED:
        //  case Java8Parser.PUBLIC:
        //  case Java8Parser.RETURN:
        //  case Java8Parser.SHORT:
        //  case Java8Parser.STATIC:
        //  case Java8Parser.STRICTFP:
        //  case Java8Parser.SUPER:
        //  case Java8Parser.SWITCH:
        //  case Java8Parser.SYNCHRONIZED:
        //  case Java8Parser.THIS:
        //  case Java8Parser.THROW:
        //  case Java8Parser.THROWS:
        //  case Java8Parser.TRANSIENT:
        //  case Java8Parser.TRY:
        //  case Java8Parser.VOID:
        //  case Java8Parser.VOLATILE:
        //  case Java8Parser.WHILE:
        //  case Java8Parser.IntegerLiteral:
        //  case Java8Parser.FloatingPointLiteral:
        //  case Java8Parser.BooleanLiteral:
        //  case Java8Parser.CharacterLiteral:
        //  case Java8Parser.StringLiteral:
        //  case Java8Parser.NullLiteral:
        //  case Java8Parser.LPAREN:
        //  case Java8Parser.RPAREN:
        //  case Java8Parser.LBRACE:
        //  case Java8Parser.RBRACE:
        //  case Java8Parser.LBRACK:
        //  case Java8Parser.RBRACK:
            case Java8Parser.SEMI:
                // Switch allows elimination of semicolons if they aren't serving the purpose of separating statements
                // (and is preferred Swift style). (Problem if a later reorganisation inserts a statement after semicolon)
                scan: for (Token t = rewriter.getTokenFollowing(token); null != t; t = rewriter.getTokenFollowing(t))
                {
                    switch (t.getType())
                    {
                        case Java8Parser.WS:
                        case Java8Parser.COMMENT:
                            continue; // keep scanning
                        case Java8Parser.LB: // end of line, no following statement
                        case Java8Parser.LINE_COMMENT: // comment continues to line end, implying no following statement
                        case Java8Parser.RBRACE: // end of block scope, no following statement
                            rewriter.delete(token);
                            break scan;
                        default:
                            break scan;
                    }
                }
                break;
        //  case Java8Parser.COMMA:
        //  case Java8Parser.DOT:
        //  case Java8Parser.ASSIGN:
        //  case Java8Parser.GT:
        //  case Java8Parser.LT:
        //  case Java8Parser.BANG:
        //  case Java8Parser.TILDE:
        //  case Java8Parser.QUESTION:
        //  case Java8Parser.COLON:
        //  case Java8Parser.EQUAL:
        //  case Java8Parser.LE:
        //  case Java8Parser.GE:
        //  case Java8Parser.NOTEQUAL:
        //  case Java8Parser.AND:
        //  case Java8Parser.OR:
        //  case Java8Parser.INC:
        //  case Java8Parser.DEC:
        //  case Java8Parser.ADD:
        //  case Java8Parser.SUB:
        //  case Java8Parser.MUL:
        //  case Java8Parser.DIV:
        //  case Java8Parser.BITAND:
        //  case Java8Parser.BITOR:
        //  case Java8Parser.CARET:
        //  case Java8Parser.MOD:
        //  case Java8Parser.ARROW:
        //  case Java8Parser.COLONCOLON:
        //  case Java8Parser.ADD_ASSIGN:
        //  case Java8Parser.SUB_ASSIGN:
        //  case Java8Parser.MUL_ASSIGN:
        //  case Java8Parser.DIV_ASSIGN:
        //  case Java8Parser.AND_ASSIGN:
        //  case Java8Parser.OR_ASSIGN:
        //  case Java8Parser.XOR_ASSIGN:
        //  case Java8Parser.MOD_ASSIGN:
        //  case Java8Parser.LSHIFT_ASSIGN:
        //  case Java8Parser.RSHIFT_ASSIGN:
        //  case Java8Parser.URSHIFT_ASSIGN:
            case Java8Parser.Identifier:
                String identifier = J2SGrammarUtils.replacementForIdentifier(node);
                if (null != identifier)
                    rewriter.replace(node, identifier);
                break;
        //  case Java8Parser.AT:
        //  case Java8Parser.ELLIPSIS:
        //  case Java8Parser.LB:
        //  case Java8Parser.WS:
        //  case Java8Parser.COMMENT:
        //  case Java8Parser.LINE_COMMENT:
            default:
                break;
        }
    }


    // ---------------------------------------------------------------------------------------------
    // Lexical Structure (§3)

    /*
    literal
        :   IntegerLiteral
        |   FloatingPointLiteral
        |   BooleanLiteral
        |   CharacterLiteral
        |   StringLiteral
        |   NullLiteral
        ;
    @Override public void enterLiteral( Java8Parser.LiteralContext ctx ) {}
    */
    @Override public void exitLiteral( Java8Parser.LiteralContext ctx )
    {
        TerminalNode tn = ctx.getChild(TerminalNode.class, 0);
        Token token = tn.getSymbol();
        boolean isInteger = false;
        switch (token.getType())
        {
            case Java8Parser.IntegerLiteral:
                isInteger = true;
            case Java8Parser.FloatingPointLiteral:
                String numberLiteral = token.getText();
                int len = numberLiteral.length();
                int idx;
                boolean replace = false;
                // Check for and strip trailing number width/precision indicators
                switch (numberLiteral.charAt(len-1))
                {
                    case 'D': case 'F': case 'L': case 'd': case 'f': case 'l':
                        numberLiteral = numberLiteral.substring(0, --len);
                        replace = true;
                        break;
                }
                // Check for uppercase non-decimal radix indicators — Swift wants them to be lowercase. Also ensure that
                // non-hexadecimal, non-scientific notation floating point numbers have a decimal point present and
                // not orphaned at either end.
                if (numberLiteral.length() > 1)
                switch (numberLiteral.charAt(1))
                {
                    case 'B': case 'O': case 'X':
                        numberLiteral = numberLiteral.toLowerCase();
                        replace = true;
                        break;
                    case 'b':case 'o':case 'x':
                        break;
                    default:
                        if (isInteger)
                            break;
                        idx = numberLiteral.indexOf(".");
                        if (idx <= 0 || idx >= len-1)
                        if (-1 == numberLiteral.indexOf("e") && -1 == numberLiteral.indexOf("E"))
                        {
                            replace = true;
                            if (idx == -1)
                                numberLiteral += ".0";
                            else if (idx == 0)
                                numberLiteral = "0"+numberLiteral;
                            else if (idx == len-1)
                                numberLiteral += "0";
                        }
                        break;
                }
                if (replace)
                    rewriter.replace(token, numberLiteral);
                break;
            case Java8Parser.BooleanLiteral:
                break;
            case Java8Parser.CharacterLiteral:
                // Swift has narrower set of backslash-escaped characters, and no octal; these need translating to
                // unicode form, but to obscure for effort.
                break;
            case Java8Parser.StringLiteral:
                // ditto.
                break;
            case Java8Parser.NullLiteral:
                rewriter.replace(token, "nil");
                break;

        }
    }

    // ---------------------------------------------------------------------------------------------
    // Types, Values, and Variables (§4)

    /*
    type
        :   primitiveType
        |   referenceType
        ;
    @Override public void enterType( Java8Parser.TypeContext ctx ) {}
    @Override public void exitType( Java8Parser.TypeContext ctx ) {}
    */

    /*
    primitiveType
        :   annotation* numericType
        |   annotation* 'boolean'
        ;
    @Override public void enterPrimitiveType( Java8Parser.PrimitiveTypeContext ctx ) {}
    */
    @Override public void exitPrimitiveType( Java8Parser.PrimitiveTypeContext ctx )
    {
        mapPrimitiveTypeInContext(ctx);
    }

    /*
    numericType
        :   integralType
        |   floatingPointType
        ;
    @Override public void enterNumericType( Java8Parser.NumericTypeContext ctx ) {}
    @Override public void exitNumericType( Java8Parser.NumericTypeContext ctx ) {}
    */

    /*
    integralType
        :   'byte'
        |   'short'
        |   'int'
        |   'long'
        |   'char'
        ;
    @Override public void enterIntegralType( Java8Parser.IntegralTypeContext ctx ) {}
    @Override public void exitIntegralType( Java8Parser.IntegralTypeContext ctx ) {}
    */

    /*
    floatingPointType
        :   'float'
        |   'double'
        ;
    @Override public void enterFloatingPointType( Java8Parser.FloatingPointTypeContext ctx ) {}
    @Override public void exitFloatingPointType( Java8Parser.FloatingPointTypeContext ctx ) {}
    */

    /*
    referenceType
        :   classOrInterfaceType
        |   typeVariable
        |   arrayType
        ;
    @Override public void enterReferenceType( Java8Parser.ReferenceTypeContext ctx ) {}
    @Override public void exitReferenceType( Java8Parser.ReferenceTypeContext ctx ) {}
    */

    /*
    classOrInterfaceType
        :   (   classType_lfno_classOrInterfaceType
            |   interfaceType_lfno_classOrInterfaceType
            )
            (   classType_lf_classOrInterfaceType
            |   interfaceType_lf_classOrInterfaceType
            )*
        ;
    @Override public void enterClassOrInterfaceType( Java8Parser.ClassOrInterfaceTypeContext ctx ) {}
    @Override public void exitClassOrInterfaceType( Java8Parser.ClassOrInterfaceTypeContext ctx ) {}
    */

    /*
    classType
        :   annotation* Identifier typeArguments?
        |   classOrInterfaceType '.' annotation* Identifier typeArguments?
        ;
    @Override public void enterClassType( Java8Parser.ClassTypeContext ctx ) {}
    @Override public void exitClassType( Java8Parser.ClassTypeContext ctx ) {}
    */

    /*
    classType_lf_classOrInterfaceType
        :   '.' annotation* Identifier typeArguments?
        ;
    @Override public void enterClassType_lf_classOrInterfaceType( Java8Parser.ClassType_lf_classOrInterfaceTypeContext ctx ) {}
    */
    @Override public void exitClassType_lf_classOrInterfaceType( Java8Parser.ClassType_lf_classOrInterfaceTypeContext ctx )
    {
        mapClassIdentifierInContext(ctx);
    }

    /*
    classType_lfno_classOrInterfaceType
        :   annotation* Identifier typeArguments?
        ;
    @Override public void enterClassType_lfno_classOrInterfaceType( Java8Parser.ClassType_lfno_classOrInterfaceTypeContext ctx ) {}
    */
    @Override public void exitClassType_lfno_classOrInterfaceType( Java8Parser.ClassType_lfno_classOrInterfaceTypeContext ctx )
    {
        mapClassIdentifierInContext(ctx);
    }

    /*
    interfaceType
        :   classType
        ;
    @Override public void enterInterfaceType( Java8Parser.InterfaceTypeContext ctx ) {}
    @Override public void exitInterfaceType( Java8Parser.InterfaceTypeContext ctx ) {}
    */

    /*
    interfaceType_lf_classOrInterfaceType
        :   classType_lf_classOrInterfaceType
        ;
    @Override public void enterInterfaceType_lf_classOrInterfaceType( Java8Parser.InterfaceType_lf_classOrInterfaceTypeContext ctx ) {}
    @Override public void exitInterfaceType_lf_classOrInterfaceType( Java8Parser.InterfaceType_lf_classOrInterfaceTypeContext ctx ) {}
    */

    /*
    interfaceType_lfno_classOrInterfaceType
        :   classType_lfno_classOrInterfaceType
        ;
    @Override public void enterInterfaceType_lfno_classOrInterfaceType( Java8Parser.InterfaceType_lfno_classOrInterfaceTypeContext ctx ) {}
    @Override public void exitInterfaceType_lfno_classOrInterfaceType( Java8Parser.InterfaceType_lfno_classOrInterfaceTypeContext ctx ) {}
    */

    /*
    typeVariable
        :   annotation* Identifier
        ;
    @Override public void enterTypeVariable( Java8Parser.TypeVariableContext ctx ) {}
    @Override public void exitTypeVariable( Java8Parser.TypeVariableContext ctx ) {}
    */

    /*
    arrayType
        :   primitiveType dims
        |   classOrInterfaceType dims
        |   typeVariable dims
        ;
    @Override public void enterArrayType( Java8Parser.ArrayTypeContext ctx ) {}
    @Override public void exitArrayType( Java8Parser.ArrayTypeContext ctx ) {}
    */

    /*
    dims
        :   annotation* '[' ']' (annotation* '[' ']')*
        ;
    @Override public void enterDims( Java8Parser.DimsContext ctx ) {}
    @Override public void exitDims( Java8Parser.DimsContext ctx ) {}
    */
    private String wrapTypeStringWithDims( String typeString, Java8Parser.DimsContext dimsCtx )
    {
        /*
        java allows:
            int b[], c[][];
            int[] d, e[];
            int[] f() {return {};}
            int[] g()[] {return {{}};}
        equivalent to
            int[] b, d;
            int[][] c, e;
            int[][] g() {return {{}};}
        which we need to map to 
            b, d: [int]
            c, e: [[int]]
        The opening bracket of a pair can be annotated, and this needs to move with the bracket. The bracket pairs next to 
        the variable declarator are innermost and need to be moved first, followed by the brackets next to the type.
        A variableDeclaratorList can mix variables of 0..n dimensions using the postfix approach, and these would need to 
        be separated into declarations with the same dimensionality - too much work, so put a warning in instead.
        
        This function is the helper for moving the bracket pairs.
        */
        TerminalNode tn;
        int i = 0, a = dimsCtx.start.getTokenIndex(), b;
        while (null != (tn = dimsCtx.getToken(Java8Parser.RBRACK, i++)))
        {
            b = tn.getSymbol().getTokenIndex() - 1;
            String leadingText = rewriter.getText(Interval.of(a, b));
            typeString = leadingText+typeString+"]";
            a = b + 2;
        }
        return typeString;
    }

    /*
    typeParameter
        :   typeParameterModifier* Identifier typeBound?
        ;
    @Override public void enterTypeParameter( Java8Parser.TypeParameterContext ctx ) {}
    @Override public void exitTypeParameter( Java8Parser.TypeParameterContext ctx ) {}
    */

    /*
    typeParameterModifier
        :   annotation
        ;
    @Override public void enterTypeParameterModifier( Java8Parser.TypeParameterModifierContext ctx ) {}
    @Override public void exitTypeParameterModifier( Java8Parser.TypeParameterModifierContext ctx ) {}
    */

    /*
    typeBound
        :   'extends' typeVariable
        |   'extends' classOrInterfaceType additionalBound*
        ;
    @Override public void enterTypeBound( Java8Parser.TypeBoundContext ctx ) {}
    */
    @Override public void exitTypeBound( Java8Parser.TypeBoundContext ctx )
    {
        rewriter.replace(ctx.getToken(Java8Parser.EXTENDS, 0), ":");
    }

    /*
    additionalBound
        :   '&' interfaceType
        ;
    @Override public void enterAdditionalBound( Java8Parser.AdditionalBoundContext ctx ) {}
    @Override public void exitAdditionalBound( Java8Parser.AdditionalBoundContext ctx ) {}
    */

    /*
    typeArguments
        :   '<' typeArgumentList '>'
        ;
    @Override public void enterTypeArguments( Java8Parser.TypeArgumentsContext ctx ) {}
    @Override public void exitTypeArguments( Java8Parser.TypeArgumentsContext ctx ) {}
    */

    /*
    typeArgumentList
        :   typeArgument (',' typeArgument)*
        ;
    @Override public void enterTypeArgumentList( Java8Parser.TypeArgumentListContext ctx ) {}
    @Override public void exitTypeArgumentList( Java8Parser.TypeArgumentListContext ctx ) {}
    */

    /*
    typeArgument
        :   referenceType
        |   wildcard
        ;
    @Override public void enterTypeArgument( Java8Parser.TypeArgumentContext ctx ) {}
    @Override public void exitTypeArgument( Java8Parser.TypeArgumentContext ctx ) {}
    */

    /*
    wildcard
        :   annotation* '?' wildcardBounds?
        ;
    @Override public void enterWildcard( Java8Parser.WildcardContext ctx ) {}
    @Override public void exitWildcard( Java8Parser.WildcardContext ctx ) {}
    */

    /*
    wildcardBounds
        :   'extends' referenceType
        |   'super' referenceType
        ;
    @Override public void enterWildcardBounds( Java8Parser.WildcardBoundsContext ctx ) {}
    */
    @Override public void exitWildcardBounds( Java8Parser.WildcardBoundsContext ctx )
    {
        TerminalNode tn = ctx.getChild(TerminalNode.class, 0);
        if (null != tn && tn.getSymbol().getType() == Java8Parser.EXTENDS)
            rewriter.replace(tn, ":");
    }

    // ---------------------------------------------------------------------------------------------
    // Names (§6)

    /*
    packageName
        :   Identifier
        |   packageName '.' Identifier
        ;
    @Override public void enterPackageName( Java8Parser.PackageNameContext ctx ) {}
    @Override public void exitPackageName( Java8Parser.PackageNameContext ctx ) {}
    */

    /*
    typeName
        :   Identifier
        |   packageOrTypeName '.' Identifier
        ;
    @Override public void enterTypeName( Java8Parser.TypeNameContext ctx ) {}
    @Override public void exitTypeName( Java8Parser.TypeNameContext ctx ) {}
    */

    /*
    packageOrTypeName
        :   Identifier
        |   packageOrTypeName '.' Identifier
        ;
    @Override public void enterPackageOrTypeName( Java8Parser.PackageOrTypeNameContext ctx ) {}
    @Override public void exitPackageOrTypeName( Java8Parser.PackageOrTypeNameContext ctx ) {}
    */

    /*
    expressionName
        :   Identifier
        |   ambiguousName '.' Identifier
        ;
    @Override public void enterExpressionName( Java8Parser.ExpressionNameContext ctx ) {}
    @Override public void exitExpressionName( Java8Parser.ExpressionNameContext ctx ) {}
    */

    /*
    methodName
        :   Identifier
        ;
    @Override public void enterMethodName( Java8Parser.MethodNameContext ctx ) {}
    @Override public void exitMethodName( Java8Parser.MethodNameContext ctx ) {}
    */

    /*
    ambiguousName
        :   Identifier
        |   ambiguousName '.' Identifier
        ;
    @Override public void enterAmbiguousName( Java8Parser.AmbiguousNameContext ctx ) {}
    @Override public void exitAmbiguousName( Java8Parser.AmbiguousNameContext ctx ) {}
    */

    // ---------------------------------------------------------------------------------------------
    // Packages (§7)

    /*
    compilationUnit
        :   packageDeclaration? importDeclaration* typeDeclaration* EOF
        ;
    @Override public void enterCompilationUnit( Java8Parser.CompilationUnitContext ctx ) {}
    @Override public void exitCompilationUnit( Java8Parser.CompilationUnitContext ctx ) {}
    */

    /*
    packageDeclaration
        :   packageModifier* 'package' Identifier ('.' Identifier)* ';'
        ;
    @Override public void enterPackageDeclaration( Java8Parser.PackageDeclarationContext ctx ) {}
    */
    @Override public void exitPackageDeclaration( Java8Parser.PackageDeclarationContext ctx )
    {
        rewriter.insertBefore(ctx, "/* ");
        rewriter.insertAfter(ctx, " */");
    }

    /*
    packageModifier
        :   annotation
        ;
    @Override public void enterPackageModifier( Java8Parser.PackageModifierContext ctx ) {}
    @Override public void exitPackageModifier( Java8Parser.PackageModifierContext ctx ) {}
    */

    /*
    importDeclaration
        :   singleTypeImportDeclaration
        |   typeImportOnDemandDeclaration
        |   singleStaticImportDeclaration
        |   staticImportOnDemandDeclaration
        ;
    @Override public void enterImportDeclaration( Java8Parser.ImportDeclarationContext ctx ) {}
    */
    @Override public void exitImportDeclaration( Java8Parser.ImportDeclarationContext ctx )
    {
        // Future: add type and method mappings based on imported packages
        rewriter.insertBefore(ctx, "/* ");
        rewriter.insertAfter(ctx, " */");
    }

    /*
    singleTypeImportDeclaration
        :   'import' typeName ';'
        ;
    @Override public void enterSingleTypeImportDeclaration( Java8Parser.SingleTypeImportDeclarationContext ctx ) {}
    @Override public void exitSingleTypeImportDeclaration( Java8Parser.SingleTypeImportDeclarationContext ctx ) {}
    */

    /*
    typeImportOnDemandDeclaration
        :   'import' packageOrTypeName '.' '*' ';'
        ;
    @Override public void enterTypeImportOnDemandDeclaration( Java8Parser.TypeImportOnDemandDeclarationContext ctx ) {}
    @Override public void exitTypeImportOnDemandDeclaration( Java8Parser.TypeImportOnDemandDeclarationContext ctx ) {}
    */

    /*
    singleStaticImportDeclaration
        :   'import' 'static' typeName '.' Identifier ';'
        ;
    @Override public void enterSingleStaticImportDeclaration( Java8Parser.SingleStaticImportDeclarationContext ctx ) {}
    @Override public void exitSingleStaticImportDeclaration( Java8Parser.SingleStaticImportDeclarationContext ctx ) {}
    */

    /*
    staticImportOnDemandDeclaration
        :   'import' 'static' typeName '.' '*' ';'
        ;
    @Override public void enterStaticImportOnDemandDeclaration( Java8Parser.StaticImportOnDemandDeclarationContext ctx ) {}
    @Override public void exitStaticImportOnDemandDeclaration( Java8Parser.StaticImportOnDemandDeclarationContext ctx ) {}
    */

    /*
    typeDeclaration
        :   classDeclaration
        |   interfaceDeclaration
        |   ';'
        ;
    @Override public void enterTypeDeclaration( Java8Parser.TypeDeclarationContext ctx ) {}
    @Override public void exitTypeDeclaration( Java8Parser.TypeDeclarationContext ctx ) {}
    */

    // ---------------------------------------------------------------------------------------------
    // Classes (§8)

    /*
    classDeclaration
        :   normalClassDeclaration
        |   enumDeclaration
        ;
    @Override public void enterClassDeclaration( Java8Parser.ClassDeclarationContext ctx ) {}
    @Override public void exitClassDeclaration( Java8Parser.ClassDeclarationContext ctx ) {}
    */

    /*
    normalClassDeclaration
        :   classModifier* 'class' Identifier typeParameters? superclass? superinterfaces? classBody
        ;
    @Override public void enterNormalClassDeclaration( Java8Parser.NormalClassDeclarationContext ctx ) {}
    */
    @Override public void exitNormalClassDeclaration( Java8Parser.NormalClassDeclarationContext ctx )
    {
        mapInitialModifiersInContext(ctx);
    }

    /*
    classModifier
        :   annotation
        |   'public'
        |   'protected'
        |   'private'
        |   'abstract'
        |   'static'
        |   'final'
        |   'strictfp'
        ;
    @Override public void enterClassModifier( Java8Parser.ClassModifierContext ctx ) {}
    @Override public void exitClassModifier( Java8Parser.ClassModifierContext ctx ) {}
    */

    /*
    typeParameters
        :   '<' typeParameterList '>'
        ;
    @Override public void enterTypeParameters( Java8Parser.TypeParametersContext ctx ) {}
    @Override public void exitTypeParameters( Java8Parser.TypeParametersContext ctx ) {}
    */

    /*
    typeParameterList
        :   typeParameter (',' typeParameter)*
        ;
    @Override public void enterTypeParameterList( Java8Parser.TypeParameterListContext ctx ) {}
    @Override public void exitTypeParameterList( Java8Parser.TypeParameterListContext ctx ) {}
    */

    /*
    superclass
        :   'extends' classType
        ;
    @Override public void enterSuperclass( Java8Parser.SuperclassContext ctx ) {}
    */
    @Override public void exitSuperclass( Java8Parser.SuperclassContext ctx )
    {
        rewriter.replace(ctx.getToken(Java8Parser.EXTENDS, 0), ":");
    }

    /*
    superinterfaces
        :   'implements' interfaceTypeList
        ;
    @Override public void enterSuperinterfaces( Java8Parser.SuperinterfacesContext ctx ) {}
    */
    @Override public void exitSuperinterfaces( Java8Parser.SuperinterfacesContext ctx )
    {
        rewriter.replace(ctx.getToken(Java8Parser.IMPLEMENTS, 0), ":");
    }

    /*
    interfaceTypeList
        :   interfaceType (',' interfaceType)*
        ;
    @Override public void enterInterfaceTypeList( Java8Parser.InterfaceTypeListContext ctx ) {}
    @Override public void exitInterfaceTypeList( Java8Parser.InterfaceTypeListContext ctx ) {}
    */

    /*
    classBody
        :   '{' classBodyDeclaration* '}'
        ;
    @Override public void enterClassBody( Java8Parser.ClassBodyContext ctx ) {}
    @Override public void exitClassBody( Java8Parser.ClassBodyContext ctx ) {}
    */

    /*
    classBodyDeclaration
        :   classMemberDeclaration
        |   instanceInitializer
        |   staticInitializer
        |   constructorDeclaration
        ;
    @Override public void enterClassBodyDeclaration( Java8Parser.ClassBodyDeclarationContext ctx ) {}
    @Override public void exitClassBodyDeclaration( Java8Parser.ClassBodyDeclarationContext ctx ) {}
    */

    /*
    classMemberDeclaration
        :   fieldDeclaration
        |   methodDeclaration
        |   classDeclaration
        |   interfaceDeclaration
        |   ';'
        ;
    @Override public void enterClassMemberDeclaration( Java8Parser.ClassMemberDeclarationContext ctx ) {}
    @Override public void exitClassMemberDeclaration( Java8Parser.ClassMemberDeclarationContext ctx ) {}
    */

    /*
    fieldDeclaration
        :   fieldModifier* unannType variableDeclaratorList ';'
        ;
    @Override public void enterFieldDeclaration( Java8Parser.FieldDeclarationContext ctx ) {}
    */
    @Override public void exitFieldDeclaration( Java8Parser.FieldDeclarationContext ctx )
    {
        mapInitialModifiersInContext(ctx);
    }

    /*
    fieldModifier
        :   annotation
        |   'public'
        |   'protected'
        |   'private'
        |   'static'
        |   'final'
        |   'transient'
        |   'volatile'
        ;
    @Override public void enterFieldModifier( Java8Parser.FieldModifierContext ctx ) {}
    @Override public void exitFieldModifier( Java8Parser.FieldModifierContext ctx ) {}
    */

    /*
    variableDeclaratorList
        :   variableDeclarator (',' variableDeclarator)*
        ;
    @Override public void enterVariableDeclaratorList( Java8Parser.VariableDeclaratorListContext ctx ) {}
    */
    @Override public void exitVariableDeclaratorList( Java8Parser.VariableDeclaratorListContext ctx )
    {
        // See notes in exitVariableDeclaratorId below
        ParserRuleContext declarationCtx = ctx.getParent();
        convertVariableDeclaration(declarationCtx);
    }

    /*
    variableDeclarator
        :   variableDeclaratorId ('=' variableInitializer)?
        ;
    @Override public void enterVariableDeclarator( Java8Parser.VariableDeclaratorContext ctx ) {}
    @Override public void exitVariableDeclarator( Java8Parser.VariableDeclaratorContext ctx ) {}
    */

    /*
    variableDeclaratorId
        :   Identifier dims?
        ;
    @Override public void enterVariableDeclaratorId( Java8Parser.VariableDeclaratorIdContext ctx ) {}
    */
    @Override public void exitVariableDeclaratorId( Java8Parser.VariableDeclaratorIdContext ctx )
    {/*

        variableDeclaratorId is used in two main ways:

        a)  in constant, field and local variable declarations it has zero or more siblings all under the same 
            type, and each sibling may optionally have an initialiser; in terms of the grammar, its parentage is 
            ((constant|field|localVariable)Declaration).variableDeclaratorList.variableDeclarator.variableDeclaratorId
            e.g.:
                public final float r360 = 2*M_PI, r180 = M_PI, r90 = M_PIL/2;
                                   ^              ^            ^

        b)  in a (normal|catch) formal parameter, a resource definition, and as the variable in an enhanced for 
            loop, it has no siblings and (except in resource def) no initialiser; in terms of the grammar, its 
            parentage is
            ((formal|lastFormal|catchFormal)Parameter|enhancedForStatement(NoShortIf|)|resource).variableDeclaratorId
            e.g.:
                void f(@NotNull List<Shape> shapes) { ... }
                                            ^

        The outer parents named in a) and b) each have a unannType (=type without annotations) as earlier sibling 
        of the branch containing the variableDeclaratorId(s), and before the unannType, a list of zero or more
        modifiers that are legal in the context.

        The transformations we need to do to variable definitions (without yet deciding where they need to be done) are:
        i)  swap type and name:   int a --> a: int
        ii) map the type:   a: int --> a: Int
        iii)suffix optionality indicator onto the type if known to be needed:   a: Int --> a: Int?
        iv) omit the type if there is an initialiser which is explicitly of the same type:   a: Int = 1 --> a = 1
        v)  precede the variable definition(s list) with 'var' (=variable) or 'let' (=constant), or nothing if mutability
            is implied
        vi) only in a formal parameter, precede the variable with the argument label suppressor '_'

        Handling i), ii) & iv):
        The type is given by the unannType at outer parent level. If we knew it was going to stay in the same place, we
        could map it to Swift type in exitUnannType. However, theoretically, we might need to suffix it to every
        variable, e.g.:
            int a = 1, b = f(), c = g();
        --> var a = 1, b: Int = f(), c: Int = g()
        we don't know the return types of f() and g(), and java allows a type widening when assigning (intvar = shortvar;
        objectvar = shapevar) so for guaranteed compilation we need to have explicit types. However, it would be more
        pragmatic to assume that they are of compatible type, and that no fancy conversion is going on, as we can't
        reach perfection and this assumption would cover 90% of the cases. But... what about variations of:
            List<SomeClass> list = new ArrayList<>();
        ...this is a common pattern - the declaration type is specific about conformity to an expected interface, but
        the concrete type in the initialiser doesn't hold any of that information.
        Hence, if there is an initialiser then we can drop the unannType only if we are sure that it is the same as the
        initialiser type, otherwise it moves to suffix the last of the uninitialised variable declarations and gets
        mapped at the same time. For variableDeclaratorId used in a) above (const, field or local), this is best handled
        in exitVariableDeclaratorList; for variableDeclaratorId use b) above (formal params / enhanced for), the
        unannType is retained, but moved and mapped, and this could be handled here, as a common handling bottleneck.

        Handling iii): nullability
        Java variables are assumed to be nullable if they lack a @NotNull annotation. However, there is a lot of code
        which has not had appropriate annotations added. Some compilers (IntelliJ, ...) show annotations inferred by
        analysing code paths, but these inferences need to be added as explicit annotations. For better porting, a
        pass over the java source to add annotations might be well advised.
        The annotations are in the modifiers held by the parent rules, however, their effect needs to be applied to
        the relocated mapped unannType. Hence, this is maybe best handled where the unannType is handled, i.e. along
        with i), ii) & iv) above.

        Handling v): var or let
        A 'final' keyword in the modifiers before the unannType indicates const value and that 'let' should user, otherwise
        use var. The enhanced for is translated elsewhere to Swift's for-in loop, which has complications: the default is
        that a loop variable is considered implicitly const through each pass of the loop's statement and the 'let' is
        omitted; however, if the loop variable is to be modified in the statement, then it has to be preceded with 'var'
        at declaration. A formal parameter in Swift is always implicitly constant (hidden let).
        As 'let' or 'var' have to occupy the original position of the unannType it is reasonable to handle this and the
        special cases when unannType is moved and transformed.

        Handling iv): argument label
        In Swift, the variableDeclaratorId in a formal parameter doubles up: the first is the label to precede the
        argument when calling, and the second is the name to use for the parameter inside the function; java doesn't
        use argument labelling, so suppress with '_'. Hence the variableDeclaratorId has to be preceded by '_ ' when
        found in a formalParameter. Handle here.

    */

        ParserRuleContext parentCtx = ctx.getParent();
        if (parentCtx.getRuleIndex() == Java8Parser.RULE_variableDeclarator)
        {
            // Call convertVariableDeclaration() from exitDeclaratorList() instead, so that sibling
            // declarators can be dealt with together.
        //  ParserRuleContext declaratorListCtx = parentCtx.getParent();
        //  ParserRuleContext declarationCtx = declaratorListCtx.getParent();
        //  convertVariableDeclaration(declarationCtx);
            ;
        }
        else
        {
            // Parent context is (resource|(formal|lastFormal|catchFormal)Parameter|enhancedForStatement(NoShortIf|))
            convertVariableDeclaration(parentCtx);
        }
    }

    private enum Constness {
        unknown,
        variable, // use var
        implicit, // omit let
        explicit  // use let
    };

    private void convertVariableDeclaration( ParserRuleContext declarationCtx )
    {
        // Read comments in exitVariableDeclaratorId just above!

        int declarationRuleIndex = declarationCtx.getRuleIndex();
        Class<? extends ParserRuleContext> modifierContextClass = Java8Parser.VariableModifierContext.class;
        Constness constness = Constness.unknown;
        boolean isOptional = false;
        boolean hasDeclaratorList = false;
        boolean enhancedFor = false;

        switch (declarationRuleIndex)
        {
            case Java8Parser.RULE_constantDeclaration:
                modifierContextClass = Java8Parser.ConstantModifierContext.class;
                hasDeclaratorList = true;
                constness = Constness.explicit;
                break;
            case Java8Parser.RULE_fieldDeclaration:
                modifierContextClass = Java8Parser.FieldModifierContext.class;
                hasDeclaratorList = true;
                break;
            case Java8Parser.RULE_localVariableDeclaration:
                hasDeclaratorList = true;
                break;
            case Java8Parser.RULE_resource:
                constness = Constness.explicit;
                break;
            case Java8Parser.RULE_formalParameter:
            case Java8Parser.RULE_lastFormalParameter:
                constness = Constness.implicit;
                break;
            case Java8Parser.RULE_catchFormalParameter:
                constness = Constness.explicit;
                break;
            case Java8Parser.RULE_enhancedForStatement:
            case Java8Parser.RULE_enhancedForStatementNoShortIf:
                enhancedFor = true;
                constness = Constness.implicit;
                break;
            default:
                return; // not our expected parameter type
        }


        // Look for and remove 'final', '@NonNull' and '@Nullable' in modifiers
        for (ParserRuleContext modifierCtx : declarationCtx.getRuleContexts(modifierContextClass))
        {
            TerminalNode tn = modifierCtx.getChild(TerminalNode.class, 0);
            if (null == tn)
            {
                String annotationText = modifierCtx.getText();
                switch (annotationText)
                {
                    // some spelling variations here...
                    case "@Nonnull": // javax.annotation.NotNull
                    case "@NonNull": // android.support.annotation.NonNull
                                     // edu.umd.cs.findbugs.annotations.NonNull
                    case "@NotNull": // org.jetbrains.annotations.NotNull
                        isOptional = false;
                        break;
                    case "@Nullable":   isOptional = true;      break;
                    default:                                    continue;
                }
            }
            else
            {
                Token token = tn.getSymbol();
                mapModifierToken(token);
                switch (token.getType())
                {
                    case Java8Parser.FINAL:
                        if (constness == Constness.unknown)
                            constness = Constness.explicit;
                        break;
                    default:                                    continue;
                }
            }
            rewriter.deleteAndAdjustWhitespace(modifierCtx);
        }
        
        // Move trailing dimensions to wrap the type. First any dimensions binding to the declarator id and
        // then any dimensions binding to the right of the type.
        // a) start by finding the type context that will be wrapped.
        Java8Parser.UnannTypeContext unannTypeCtx = null;
        Java8Parser.UnannReferenceTypeContext unannReferenceTypeCtx = null;
        Java8Parser.UnannArrayTypeContext unannArrayTypeCtx = null;
        Java8Parser.DimsContext outerDimsCtx = null;
        ParserRuleContext typeCtx = null;
        if (declarationRuleIndex == Java8Parser.RULE_catchFormalParameter)
            typeCtx = declarationCtx.getChild(Java8Parser.CatchTypeContext.class, 0);
        else
            typeCtx = unannTypeCtx = declarationCtx.getChild(Java8Parser.UnannTypeContext.class, 0);
        if (null != unannTypeCtx)
        if (null != (unannReferenceTypeCtx = unannTypeCtx.unannReferenceType())
         && null != (unannArrayTypeCtx = unannReferenceTypeCtx.unannArrayType()))
        {
            typeCtx = unannArrayTypeCtx.getChild(ParserRuleContext.class, 0);
            outerDimsCtx = unannArrayTypeCtx.dims();
        }
        // b) process dimensions attached to declarator ids
            // ...process inside blocks below
        // c) process dimensions attached to type
            // ...process inside blocks below

        // Now insert unannTypeText at end of each variableDeclaratorId if necessary:
        ParserRuleContext ctx, varInitCtx;
        Java8Parser.VariableDeclaratorIdContext varIdCtx;
        Java8Parser.DimsContext innerDimsCtx = null;
        String unannTypeText;
        if (hasDeclaratorList)
        {
            // Iterate over the list of declarator-initialiser pairs backwards so that variable lists without
            // intialisers and with explicit enough types, just pick up the type from the end of the list, i.e.
            // so that we generate var a, b, c: Int, and not var a: Int, b: Int, c: Int.
            ListIterator<Java8Parser.VariableDeclaratorContext> iter;
            List<Java8Parser.VariableDeclaratorContext> list;
            String followingUnannTypeText = null;
            boolean followingVarHasExplicitType = false;
            boolean hasInitialiser;

            ctx = declarationCtx.getChild(Java8Parser.VariableDeclaratorListContext.class, 0);
            list = ctx.getRuleContexts(Java8Parser.VariableDeclaratorContext.class);
            iter = list.listIterator(list.size());
            unannTypeText = null;

            while (iter.hasPrevious())
            {
                ctx = iter.previous();
                varIdCtx = ctx.getRuleContext(Java8Parser.VariableDeclaratorIdContext.class, 0);

                // Wrap the inner type string with array dimensions if we have them. Have to do this for each variable,
                // because they can have different dimensionality.
                followingUnannTypeText = unannTypeText;
                unannTypeText = rewriter.getText(typeCtx);
                if (null != (innerDimsCtx = varIdCtx.dims()))
                {
                    unannTypeText = wrapTypeStringWithDims(unannTypeText, innerDimsCtx);
                    rewriter.delete(innerDimsCtx);
                }
                if (null != outerDimsCtx)
                    unannTypeText = wrapTypeStringWithDims(unannTypeText, outerDimsCtx);

                varInitCtx = ctx.getRuleContext(Java8Parser.VariableInitializerContext.class, 0);
                if (null != varInitCtx)
                    varInitCtx = varInitCtx.getChild(ParserRuleContext.class, 0); // expression or arrayInitializer
                hasInitialiser = null != varInitCtx;

                // In the basic case, we have to qualify the variable with its type, but we can omit this if it has an
                // initialiser that completely implies the type, or it has no initialiser and has the same type as the
                // a contiguously following variable with explicit type.
                if (hasInitialiser
                  ? !isVariableTypeCompletelyImpliedByInitializer( unannTypeCtx, varInitCtx, /*inEnhancedFor:*/false )
                  : !followingVarHasExplicitType || null == followingUnannTypeText || !unannTypeText.equals(followingUnannTypeText))
                {
                    rewriter.insertAfter(varIdCtx, ": "+unannTypeText+(isOptional?"?":""));
                    followingVarHasExplicitType = !hasInitialiser;
                }
            }
        }
        else
        {
            varIdCtx = declarationCtx.getRuleContext(Java8Parser.VariableDeclaratorIdContext.class, 0);
            unannTypeText = rewriter.getText(typeCtx);
            if (null != (innerDimsCtx = varIdCtx.dims()))
            {
                unannTypeText = wrapTypeStringWithDims(unannTypeText, innerDimsCtx);
                rewriter.delete(innerDimsCtx);
            }
            if (null != outerDimsCtx)
                unannTypeText = wrapTypeStringWithDims(unannTypeText, outerDimsCtx);

            varInitCtx = null;
            if (declarationRuleIndex == Java8Parser.RULE_resource
             || declarationRuleIndex == Java8Parser.RULE_enhancedForStatement
             || declarationRuleIndex == Java8Parser.RULE_enhancedForStatementNoShortIf)
                varInitCtx = declarationCtx.getRuleContext(Java8Parser.ExpressionContext.class, 0);

            if (declarationRuleIndex == Java8Parser.RULE_catchFormalParameter)
                rewriter.insertAfter(varIdCtx, " as "+unannTypeText);
            else
            if (!isVariableTypeCompletelyImpliedByInitializer( unannTypeCtx, varInitCtx, enhancedFor ))
                rewriter.insertAfter(varIdCtx, ": "+unannTypeText+(isOptional?"?":""));

            // In parameter lists, add an anonymizing argument label, as argument labels not used in java method/function calls
            if (declarationRuleIndex == Java8Parser.RULE_formalParameter
             || declarationRuleIndex == Java8Parser.RULE_lastFormalParameter)
                rewriter.insertBefore(varIdCtx, "_ ");
        }

        // Finally replace the complete type context with let/var/-
        // in an enhancedForStatement, the loop var is implicitly const, but can be made variable with var if it is
        // to be modified inside the loop; we could check for this, but its a rare scenario, and a lot of work, so no.
        if (null != unannTypeCtx)
            typeCtx = unannTypeCtx;
        switch (constness)
        {
            case explicit:  rewriter.replace(typeCtx, "let");      break;
            case implicit:  rewriter.deleteAndAdjustWhitespace(typeCtx);  break;
            case variable:  rewriter.replace(typeCtx, "var");      break;
            // if still unknown, then assume variable...
            default:        rewriter.replace(typeCtx, "var");      break;
        }
    }

    private boolean isVariableTypeCompletelyImpliedByInitializer(
            Java8Parser.UnannTypeContext typeCtx, ParserRuleContext initCtx, boolean inEnhancedFor )
    {
        // Task: examine the variable initializer initCtx and see if its result type is the same as the type for the
        // variable receiving the result. If inEnhancedFor, the initialiser result is expected to be a collection
        // of some form, and we look for a match between the collection's component type and the unannType - .

        if (null == initCtx || null == typeCtx)
            return false;

        Java8Parser.ExpressionContext initializerExpressionCtx = null;
        Java8Parser.ArrayInitializerContext initializerArrayCtx = null;
        // We expect initCtx to be an expression or an array initializer
        switch (initCtx.getRuleIndex())
        {
            case Java8Parser.RULE_expression:
                initializerExpressionCtx = (Java8Parser.ExpressionContext)initCtx;
                break;
            case Java8Parser.RULE_arrayInitializer:
                initializerArrayCtx = (Java8Parser.ArrayInitializerContext)initCtx;
                break;
            default:
                return false; // not a parameter we expect
        }
        Java8Parser.UnannReferenceTypeContext refTypeCtx = typeCtx.unannReferenceType();
        Java8Parser.UnannArrayTypeContext arrayTypeCtx = null != refTypeCtx ? refTypeCtx.unannArrayType() : null;

        if ( !inEnhancedFor && null != initializerArrayCtx && null != arrayTypeCtx )
            return isArrayVariableTypeCompletelyImpliedByInitializer( arrayTypeCtx, initializerArrayCtx );

        if ( inEnhancedFor && null != initializerArrayCtx && null == arrayTypeCtx )
        {
            Java8Parser.VariableInitializerListContext list = initializerArrayCtx.variableInitializerList();
            // ...all members same type as typeCtx ?
        }

        if ( !inEnhancedFor && null == initializerArrayCtx && null == arrayTypeCtx)
            return isNonArrayVariableTypeCompletelyImpliedByInitializer( typeCtx, initializerExpressionCtx );

        return false;
    }

    private boolean isNonArrayVariableTypeCompletelyImpliedByInitializer(
            Java8Parser.UnannTypeContext typeCtx, Java8Parser.ExpressionContext expressionCtx )
    {
        // Hard work still to do
        return false;
    }

    private boolean isArrayVariableTypeCompletelyImpliedByInitializer(
            Java8Parser.UnannArrayTypeContext arrayTypeCtx, Java8Parser.ArrayInitializerContext arrayInitializerCtx)
    {
        // Hard work still to do
        return false;
    }

    /*
    variableInitializer
        :   expression
        |   arrayInitializer
        ;
    @Override public void enterVariableInitializer( Java8Parser.VariableInitializerContext ctx ) {}
    @Override public void exitVariableInitializer( Java8Parser.VariableInitializerContext ctx ) {}
    */

    /*
    unannType
        :   unannPrimitiveType
        |   unannReferenceType
        ;
    @Override public void enterUnannType( Java8Parser.UnannTypeContext ctx ) {}
    @Override public void exitUnannType( Java8Parser.UnannTypeContext ctx ) {}
    */

    /*
    unannPrimitiveType
        :   numericType
        |   'boolean'
        ;
    @Override public void enterUnannPrimitiveType( Java8Parser.UnannPrimitiveTypeContext ctx ) {}
    */
    @Override public void exitUnannPrimitiveType( Java8Parser.UnannPrimitiveTypeContext ctx )
    {
        mapPrimitiveTypeInContext(ctx);
    }

    /*
    unannReferenceType
        :   unannClassOrInterfaceType
        |   unannTypeVariable
        |   unannArrayType
        ;
    @Override public void enterUnannReferenceType( Java8Parser.UnannReferenceTypeContext ctx ) {}
    @Override public void exitUnannReferenceType( Java8Parser.UnannReferenceTypeContext ctx ) {}
    */

    /*
    unannClassOrInterfaceType
        :   (   unannClassType_lfno_unannClassOrInterfaceType
            |   unannInterfaceType_lfno_unannClassOrInterfaceType
            )
            (   unannClassType_lf_unannClassOrInterfaceType
            |   unannInterfaceType_lf_unannClassOrInterfaceType
            )*
        ;
    @Override public void enterUnannClassOrInterfaceType( Java8Parser.UnannClassOrInterfaceTypeContext ctx ) {}
    @Override public void exitUnannClassOrInterfaceType( Java8Parser.UnannClassOrInterfaceTypeContext ctx ) {}
    */

    /*
    unannClassType
        :   Identifier typeArguments?
        |   unannClassOrInterfaceType '.' annotation* Identifier typeArguments?
        ;
    @Override public void enterUnannClassType( Java8Parser.UnannClassTypeContext ctx ) {}
    @Override public void exitUnannClassType( Java8Parser.UnannClassTypeContext ctx ) {}
    */

    /*
    unannClassType_lf_unannClassOrInterfaceType
        :   '.' annotation* Identifier typeArguments?
        ;
    @Override public void enterUnannClassType_lf_unannClassOrInterfaceType( Java8Parser.UnannClassType_lf_unannClassOrInterfaceTypeContext ctx ) {}
    */
    @Override public void exitUnannClassType_lf_unannClassOrInterfaceType( Java8Parser.UnannClassType_lf_unannClassOrInterfaceTypeContext ctx )
    {
        mapClassIdentifierInContext(ctx);
    }

    /*
    unannClassType_lfno_unannClassOrInterfaceType
        :   Identifier typeArguments?
        ;
    @Override public void enterUnannClassType_lfno_unannClassOrInterfaceType( Java8Parser.UnannClassType_lfno_unannClassOrInterfaceTypeContext ctx ) {}
    */
    @Override public void exitUnannClassType_lfno_unannClassOrInterfaceType( Java8Parser.UnannClassType_lfno_unannClassOrInterfaceTypeContext ctx )
    {
        mapClassIdentifierInContext(ctx);
    }

    /*
    unannInterfaceType
        :   unannClassType
        ;
    @Override public void enterUnannInterfaceType( Java8Parser.UnannInterfaceTypeContext ctx ) {}
    @Override public void exitUnannInterfaceType( Java8Parser.UnannInterfaceTypeContext ctx ) {}
    */

    /*
    unannInterfaceType_lf_unannClassOrInterfaceType
        :   unannClassType_lf_unannClassOrInterfaceType
        ;
    @Override public void enterUnannInterfaceType_lf_unannClassOrInterfaceType( Java8Parser.UnannInterfaceType_lf_unannClassOrInterfaceTypeContext ctx ) {}
    @Override public void exitUnannInterfaceType_lf_unannClassOrInterfaceType( Java8Parser.UnannInterfaceType_lf_unannClassOrInterfaceTypeContext ctx ) {}
    */

    /*
    unannInterfaceType_lfno_unannClassOrInterfaceType
        :   unannClassType_lfno_unannClassOrInterfaceType
        ;
    @Override public void enterUnannInterfaceType_lfno_unannClassOrInterfaceType( Java8Parser.UnannInterfaceType_lfno_unannClassOrInterfaceTypeContext ctx ) {}
    @Override public void exitUnannInterfaceType_lfno_unannClassOrInterfaceType( Java8Parser.UnannInterfaceType_lfno_unannClassOrInterfaceTypeContext ctx ) {}
    */

    /*
    unannTypeVariable
        :   Identifier
        ;
    @Override public void enterUnannTypeVariable( Java8Parser.UnannTypeVariableContext ctx ) {}
    @Override public void exitUnannTypeVariable( Java8Parser.UnannTypeVariableContext ctx ) {}
    */

    /*
    unannArrayType
        :   unannPrimitiveType dims
        |   unannClassOrInterfaceType dims
        |   unannTypeVariable dims
        ;
    @Override public void enterUnannArrayType( Java8Parser.UnannArrayTypeContext ctx ) {}
    @Override public void exitUnannArrayType( Java8Parser.UnannArrayTypeContext ctx ) {}
    */

    /*
    methodDeclaration
        :   methodModifier* methodHeader methodBody
        ;
    @Override public void enterMethodDeclaration( Java8Parser.MethodDeclarationContext ctx ) {}
    */
    @Override public void exitMethodDeclaration( Java8Parser.MethodDeclarationContext ctx )
    {
        mapInitialModifiersInContext(ctx);
    }

    /*
    methodModifier
        :   annotation
        |   'public'
        |   'protected'
        |   'private'
        |   'abstract'
        |   'static'
        |   'final'
        |   'synchronized'
        |   'native'
        |   'strictfp'
        ;
    @Override public void enterMethodModifier( Java8Parser.MethodModifierContext ctx ) {}
    @Override public void exitMethodModifier( Java8Parser.MethodModifierContext ctx ) {}
    */

    /*
    methodHeader
        :   result methodDeclarator throws_?
        |   typeParameters annotation* result methodDeclarator throws_?
        ;
    @Override public void enterMethodHeader( Java8Parser.MethodHeaderContext ctx ) {}
    */
    @Override public void exitMethodHeader( Java8Parser.MethodHeaderContext ctx )
    {
        Java8Parser.ResultContext resultCtx = ctx.result();
        Java8Parser.UnannTypeContext unannTypeCtx = resultCtx.unannType();
        if (null != unannTypeCtx)
        {
            ParserRuleContext typeCtx = resultCtx;

            Java8Parser.UnannReferenceTypeContext unannReferenceTypeCtx = null;
            Java8Parser.UnannArrayTypeContext unannArrayTypeCtx = null;
            Java8Parser.DimsContext outerDimsCtx = null;
            if (null != (unannReferenceTypeCtx = unannTypeCtx.unannReferenceType())
             && null != (unannArrayTypeCtx = unannReferenceTypeCtx.unannArrayType()))
            {
                typeCtx = unannArrayTypeCtx.getChild(ParserRuleContext.class, 0);
                outerDimsCtx = unannArrayTypeCtx.dims();
            }

            Java8Parser.DimsContext innerDimsCtx = ctx.methodDeclarator().dims();

            String unannTypeText = rewriter.getText(typeCtx);
            if (null != innerDimsCtx)
            {
                unannTypeText = wrapTypeStringWithDims(unannTypeText, innerDimsCtx);
                rewriter.delete(innerDimsCtx);
            }
            if (null != outerDimsCtx)
                unannTypeText = wrapTypeStringWithDims(unannTypeText, outerDimsCtx);

            rewriter.insertAfter(ctx.stop, " -> "+unannTypeText);
        }
        rewriter.replace(resultCtx, "func");
    }

    /*
    result
        :   unannType
        |   'void'
        ;
    @Override public void enterResult( Java8Parser.ResultContext ctx ) {}
    @Override public void exitResult( Java8Parser.ResultContext ctx ) {}
    */

    /*
    methodDeclarator
        :   Identifier '(' formalParameterList? ')' dims?
        ;
    @Override public void enterMethodDeclarator( Java8Parser.MethodDeclaratorContext ctx ) {}
    @Override public void exitMethodDeclarator( Java8Parser.MethodDeclaratorContext ctx ) {}
    */

    /*
    formalParameterList
        :   formalParameters ',' lastFormalParameter
        |   lastFormalParameter
        ;
    @Override public void enterFormalParameterList( Java8Parser.FormalParameterListContext ctx ) {}
    @Override public void exitFormalParameterList( Java8Parser.FormalParameterListContext ctx ) {}
    */

    /*
    formalParameters
        :   formalParameter (',' formalParameter)*
        |   receiverParameter (',' formalParameter)*
        ;
    @Override public void enterFormalParameters( Java8Parser.FormalParametersContext ctx ) {}
    @Override public void exitFormalParameters( Java8Parser.FormalParametersContext ctx ) {}
    */

    /*
    formalParameter
        :   variableModifier* unannType variableDeclaratorId
        ;
    @Override public void enterFormalParameter( Java8Parser.FormalParameterContext ctx ) {}
    @Override public void exitFormalParameter( Java8Parser.FormalParameterContext ctx ) {}
    */

    /*
    variableModifier
        :   annotation
        |   'final'
        ;
    @Override public void enterVariableModifier( Java8Parser.VariableModifierContext ctx ) {}
    @Override public void exitVariableModifier( Java8Parser.VariableModifierContext ctx ) {}
    */

    /*
    lastFormalParameter
        :   variableModifier* unannType annotation* '...' variableDeclaratorId
        |   formalParameter
        ;
    @Override public void enterLastFormalParameter( Java8Parser.LastFormalParameterContext ctx ) {}
    @Override public void exitLastFormalParameter( Java8Parser.LastFormalParameterContext ctx ) {}
    */

    /*
    receiverParameter
        :   annotation* unannType (Identifier '.')? 'this'
        ;
    @Override public void enterReceiverParameter( Java8Parser.ReceiverParameterContext ctx ) {}
    @Override public void exitReceiverParameter( Java8Parser.ReceiverParameterContext ctx ) {}
    */

    /*
    throws_
        :   'throws' exceptionTypeList
        ;
    @Override public void enterThrows_( Java8Parser.Throws_Context ctx ) {}
    */
    @Override public void exitThrows_( Java8Parser.Throws_Context ctx )
    {
        // Swift doesn't enumerate the thrown exception types in method/constructor headers
        rewriter.deleteAndAdjustWhitespace(ctx.exceptionTypeList());
    }

    /*
    exceptionTypeList
        :   exceptionType (',' exceptionType)*
        ;
    @Override public void enterExceptionTypeList( Java8Parser.ExceptionTypeListContext ctx ) {}
    @Override public void exitExceptionTypeList( Java8Parser.ExceptionTypeListContext ctx ) {}
    */

    /*
    exceptionType
        :   classType
        |   typeVariable
        ;
    @Override public void enterExceptionType( Java8Parser.ExceptionTypeContext ctx ) {}
    @Override public void exitExceptionType( Java8Parser.ExceptionTypeContext ctx ) {}
    */

    /*
    methodBody
        :   block
        |   ';'
        ;
    @Override public void enterMethodBody( Java8Parser.MethodBodyContext ctx ) {}
    @Override public void exitMethodBody( Java8Parser.MethodBodyContext ctx ) {}
    */

    /*
    instanceInitializer
        :   block
        ;
    @Override public void enterInstanceInitializer( Java8Parser.InstanceInitializerContext ctx ) {}
    @Override public void exitInstanceInitializer( Java8Parser.InstanceInitializerContext ctx ) {}
    */

    /*
    staticInitializer
        :   'static' block
        ;
    @Override public void enterStaticInitializer( Java8Parser.StaticInitializerContext ctx ) {}
    @Override public void exitStaticInitializer( Java8Parser.StaticInitializerContext ctx ) {}
    */

    /*
    constructorDeclaration
        :   constructorModifier* constructorDeclarator throws_? constructorBody
        ;
    @Override public void enterConstructorDeclaration( Java8Parser.ConstructorDeclarationContext ctx ) {}
    */
    @Override public void exitConstructorDeclaration( Java8Parser.ConstructorDeclarationContext ctx )
    {
        mapInitialModifiersInContext(ctx);
    }

    /*
    constructorModifier
        :   annotation
        |   'public'
        |   'protected'
        |   'private'
        ;
    @Override public void enterConstructorModifier( Java8Parser.ConstructorModifierContext ctx ) {}
    @Override public void exitConstructorModifier( Java8Parser.ConstructorModifierContext ctx ) {}
    */

    /*
    constructorDeclarator
        :   typeParameters? simpleTypeName '(' formalParameterList? ')'
        ;
    @Override public void enterConstructorDeclarator( Java8Parser.ConstructorDeclaratorContext ctx ) {}
    */
    @Override public void exitConstructorDeclarator( Java8Parser.ConstructorDeclaratorContext ctx )
    {
        rewriter.replace( ctx.simpleTypeName(), "init" );
    }

    /*
    simpleTypeName
        :   Identifier
        ;
    @Override public void enterSimpleTypeName( Java8Parser.SimpleTypeNameContext ctx ) {}
    @Override public void exitSimpleTypeName( Java8Parser.SimpleTypeNameContext ctx ) {}
    */

    /*
    constructorBody
        :   '{' explicitConstructorInvocation? blockStatements? '}'
        ;
    @Override public void enterConstructorBody( Java8Parser.ConstructorBodyContext ctx ) {}
    @Override public void exitConstructorBody( Java8Parser.ConstructorBodyContext ctx ) {}
    */

    /*
    explicitConstructorInvocation
        :   typeArguments? 'this' '(' argumentList? ')' ';'
        |   typeArguments? 'super' '(' argumentList? ')' ';'
        |   expressionName '.' typeArguments? 'super' '(' argumentList? ')' ';'
        |   primary '.' typeArguments? 'super' '(' argumentList? ')' ';'
        ;
    @Override public void enterExplicitConstructorInvocation( Java8Parser.ExplicitConstructorInvocationContext ctx ) {}
    */
    @Override public void exitExplicitConstructorInvocation( Java8Parser.ExplicitConstructorInvocationContext ctx )
    {
        TerminalNode tn;
        if (null != (tn = ctx.getToken(Java8Parser.THIS, 0)))
        {
            //  'this' within explicit constructor invocation is calling sideways to another constructor, which implies we are a convenience constructor
            rewriter.replace(tn, "self.init");
            // put 'convenience' before type name in our constructor declarator
            ParserRuleContext context;
            if (null != (context = ctx.getParent())) // constructorBody
            if (null != (context = context.getParent())) // constructorDeclaration
            if (null != (context = context.getChild(Java8Parser.ConstructorDeclaratorContext.class, 0)))
            if (null != (context = context.getChild(Java8Parser.SimpleTypeNameContext.class, 0)))
                rewriter.insertBefore( context, "convenience " );
        }
        else if (null != (tn = ctx.getToken(Java8Parser.SUPER, 0)))
        {
            rewriter.replace(tn, "super.init");
        }
    }

    /*
    enumDeclaration
        :   classModifier* 'enum' Identifier superinterfaces? enumBody
        ;
    @Override public void enterEnumDeclaration( Java8Parser.EnumDeclarationContext ctx ) {}
    @Override public void exitEnumDeclaration( Java8Parser.EnumDeclarationContext ctx ) {}
    */

    /*
    enumBody
        :   '{' enumConstantList? ','? enumBodyDeclarations? '}'
        ;
    @Override public void enterEnumBody( Java8Parser.EnumBodyContext ctx ) {}
    */
    @Override public void exitEnumBody( Java8Parser.EnumBodyContext ctx )
    {
        // A case keyword introduces one or more enum values. We add just a single case at the beginning of the enum 
        // body rather than for each value
        if (null != ctx.enumConstantList())
            rewriter.insertAfter(ctx.start, " case ");
        // Swift doesn't tolerate a trailing comma, but (strangely) java can have one between the enum constant last
        // and the enum body declarations - which are preceded by a semicolon — weird (or a problem in the grammar
        // description). Check and eliminate.
        TerminalNode comma = ctx.getToken(Java8Parser.COMMA, 0);
        if (null != comma)
            rewriter.delete(comma.getSymbol().getTokenIndex());
    }

    /*
    enumConstantList
        :   enumConstant (',' enumConstant)*
        ;
    @Override public void enterEnumConstantList( Java8Parser.EnumConstantListContext ctx ) {}
    @Override public void exitEnumConstantList( Java8Parser.EnumConstantListContext ctx ) {}
    */

    /*
    enumConstant
        :   enumConstantModifier* Identifier ('(' argumentList? ')')? classBody?
        ;
    @Override public void enterEnumConstant( Java8Parser.EnumConstantContext ctx ) {}
    @Override public void exitEnumConstant( Java8Parser.EnumConstantContext ctx ) {}
    */

    /*
    enumConstantModifier
        :   annotation
        ;
    @Override public void enterEnumConstantModifier( Java8Parser.EnumConstantModifierContext ctx ) {}
    @Override public void exitEnumConstantModifier( Java8Parser.EnumConstantModifierContext ctx ) {}
    */

    /*
    enumBodyDeclarations
        :   ';' classBodyDeclaration*
        ;
    @Override public void enterEnumBodyDeclarations( Java8Parser.EnumBodyDeclarationsContext ctx ) {}
    @Override public void exitEnumBodyDeclarations( Java8Parser.EnumBodyDeclarationsContext ctx ) {}
    */

    // ---------------------------------------------------------------------------------------------
    // Interfaces (§9)

    /*
    interfaceDeclaration
        :   normalInterfaceDeclaration
        |   annotationTypeDeclaration
        ;
    @Override public void enterInterfaceDeclaration( Java8Parser.InterfaceDeclarationContext ctx ) {}
    @Override public void exitInterfaceDeclaration( Java8Parser.InterfaceDeclarationContext ctx ) {}
    */

    /*
    normalInterfaceDeclaration
        :   interfaceModifier* 'interface' Identifier typeParameters? extendsInterfaces? interfaceBody
        ;
    @Override public void enterNormalInterfaceDeclaration( Java8Parser.NormalInterfaceDeclarationContext ctx ) {}
    */
    @Override public void exitNormalInterfaceDeclaration( Java8Parser.NormalInterfaceDeclarationContext ctx )
    {
        mapInitialModifiersInContext(ctx);
        rewriter.replace(ctx.getToken(Java8Parser.INTERFACE, 0), "protocol");
    }

    /*
    interfaceModifier
        :   annotation
        |   'public'
        |   'protected'
        |   'private'
        |   'abstract'
        |   'static'
        |   'strictfp'
        ;
    @Override public void enterInterfaceModifier( Java8Parser.InterfaceModifierContext ctx ) {}
    @Override public void exitInterfaceModifier( Java8Parser.InterfaceModifierContext ctx ) {}
    */

    /*
    extendsInterfaces
        :   'extends' interfaceTypeList
        ;
    @Override public void enterExtendsInterfaces( Java8Parser.ExtendsInterfacesContext ctx ) {}
    */
    @Override public void exitExtendsInterfaces( Java8Parser.ExtendsInterfacesContext ctx )
    {
        rewriter.replace(ctx.getToken(Java8Parser.EXTENDS, 0), ":");
    }

    /*
    interfaceBody
        :   '{' interfaceMemberDeclaration* '}'
        ;
    @Override public void enterInterfaceBody( Java8Parser.InterfaceBodyContext ctx ) {}
    @Override public void exitInterfaceBody( Java8Parser.InterfaceBodyContext ctx ) {}
    */

    /*
    interfaceMemberDeclaration
        :   constantDeclaration
        |   interfaceMethodDeclaration
        |   classDeclaration
        |   interfaceDeclaration
        |   ';'
        ;
    @Override public void enterInterfaceMemberDeclaration( Java8Parser.InterfaceMemberDeclarationContext ctx ) {}
    @Override public void exitInterfaceMemberDeclaration( Java8Parser.InterfaceMemberDeclarationContext ctx ) {}
    */

    /*
    constantDeclaration
        :   constantModifier* unannType variableDeclaratorList ';'
        ;
    @Override public void enterConstantDeclaration( Java8Parser.ConstantDeclarationContext ctx ) {}
    @Override public void exitConstantDeclaration( Java8Parser.ConstantDeclarationContext ctx ) {}
    */

    /*
    constantModifier
        :   annotation
        |   'public'
        |   'static'
        |   'final'
        ;
    @Override public void enterConstantModifier( Java8Parser.ConstantModifierContext ctx ) {}
    @Override public void exitConstantModifier( Java8Parser.ConstantModifierContext ctx ) {}
    */

    /*
    interfaceMethodDeclaration
        :   interfaceMethodModifier* methodHeader methodBody
        ;
    @Override public void enterInterfaceMethodDeclaration( Java8Parser.InterfaceMethodDeclarationContext ctx ) {}
    */
    @Override public void exitInterfaceMethodDeclaration( Java8Parser.InterfaceMethodDeclarationContext ctx )
    {
        mapInitialModifiersInContext(ctx);
    }

    /*
    interfaceMethodModifier
        :   annotation
        |   'public'
        |   'abstract'
        |   'default'
        |   'static'
        |   'strictfp'
        ;
    @Override public void enterInterfaceMethodModifier( Java8Parser.InterfaceMethodModifierContext ctx ) {}
    @Override public void exitInterfaceMethodModifier( Java8Parser.InterfaceMethodModifierContext ctx ) {}
    */

    /*
    annotationTypeDeclaration
        :   interfaceModifier* '@' 'interface' Identifier annotationTypeBody
        ;
    @Override public void enterAnnotationTypeDeclaration( Java8Parser.AnnotationTypeDeclarationContext ctx ) {}
    @Override public void exitAnnotationTypeDeclaration( Java8Parser.AnnotationTypeDeclarationContext ctx ) {}
    */

    /*
    annotationTypeBody
        :   '{' annotationTypeMemberDeclaration* '}'
        ;
    @Override public void enterAnnotationTypeBody( Java8Parser.AnnotationTypeBodyContext ctx ) {}
    @Override public void exitAnnotationTypeBody( Java8Parser.AnnotationTypeBodyContext ctx ) {}
    */

    /*
    annotationTypeMemberDeclaration
        :   annotationTypeElementDeclaration
        |   constantDeclaration
        |   classDeclaration
        |   interfaceDeclaration
        |   ';'
        ;
    @Override public void enterAnnotationTypeMemberDeclaration( Java8Parser.AnnotationTypeMemberDeclarationContext ctx ) {}
    @Override public void exitAnnotationTypeMemberDeclaration( Java8Parser.AnnotationTypeMemberDeclarationContext ctx ) {}
    */

    /*
    annotationTypeElementDeclaration
        :   annotationTypeElementModifier* unannType Identifier '(' ')' dims? defaultValue? ';'
        ;
    @Override public void enterAnnotationTypeElementDeclaration( Java8Parser.AnnotationTypeElementDeclarationContext ctx ) {}
    @Override public void exitAnnotationTypeElementDeclaration( Java8Parser.AnnotationTypeElementDeclarationContext ctx ) {}
    */

    /*
    annotationTypeElementModifier
        :   annotation
        |   'public'
        |   'abstract'
        ;
    @Override public void enterAnnotationTypeElementModifier( Java8Parser.AnnotationTypeElementModifierContext ctx ) {}
    @Override public void exitAnnotationTypeElementModifier( Java8Parser.AnnotationTypeElementModifierContext ctx ) {}
    */

    /*
    defaultValue
        :   'default' elementValue
        ;
    @Override public void enterDefaultValue( Java8Parser.DefaultValueContext ctx ) {}
    @Override public void exitDefaultValue( Java8Parser.DefaultValueContext ctx ) {}
    */

    /*
    annotation
        :   normalAnnotation
        |   markerAnnotation
        |   singleElementAnnotation
        ;
    @Override public void enterAnnotation( Java8Parser.AnnotationContext ctx ) {}
    */
    @Override public void exitAnnotation( Java8Parser.AnnotationContext ctx )
    {
        String annotationText = ctx.getText();
        String replacementText = annotationMapper.map(annotationText);
        if (null != replacementText && 0 < replacementText.length())
            rewriter.replace(ctx, replacementText);
        else
            rewriter.replace(ctx, "/*"+replacementText+"*/");
    }

    /*
    normalAnnotation
        :   '@' typeName '(' elementValuePairList? ')'
        ;
    @Override public void enterNormalAnnotation( Java8Parser.NormalAnnotationContext ctx ) {}
    @Override public void exitNormalAnnotation( Java8Parser.NormalAnnotationContext ctx ) {}
    */

    /*
    elementValuePairList
        :   elementValuePair (',' elementValuePair)*
        ;
    @Override public void enterElementValuePairList( Java8Parser.ElementValuePairListContext ctx ) {}
    @Override public void exitElementValuePairList( Java8Parser.ElementValuePairListContext ctx ) {}
    */

    /*
    elementValuePair
        :   Identifier '=' elementValue
        ;
    @Override public void enterElementValuePair( Java8Parser.ElementValuePairContext ctx ) {}
    @Override public void exitElementValuePair( Java8Parser.ElementValuePairContext ctx ) {}
    */

    /*
    elementValue
        :   conditionalExpression
        |   elementValueArrayInitializer
        |   annotation
        ;
    @Override public void enterElementValue( Java8Parser.ElementValueContext ctx ) {}
    @Override public void exitElementValue( Java8Parser.ElementValueContext ctx ) {}
    */

    /*
    elementValueArrayInitializer
        :   '{' elementValueList? ','? '}'
        ;
    @Override public void enterElementValueArrayInitializer( Java8Parser.ElementValueArrayInitializerContext ctx ) {}
    @Override public void exitElementValueArrayInitializer( Java8Parser.ElementValueArrayInitializerContext ctx ) {}
    */

    /*
    elementValueList
        :   elementValue (',' elementValue)*
        ;
    @Override public void enterElementValueList( Java8Parser.ElementValueListContext ctx ) {}
    @Override public void exitElementValueList( Java8Parser.ElementValueListContext ctx ) {}
    */

    /*
    markerAnnotation
        :   '@' typeName
        ;
    @Override public void enterMarkerAnnotation( Java8Parser.MarkerAnnotationContext ctx ) {}
    @Override public void exitMarkerAnnotation( Java8Parser.MarkerAnnotationContext ctx ) {}
    */

    /*
    singleElementAnnotation
        :   '@' typeName '(' elementValue ')'
        ;
    @Override public void enterSingleElementAnnotation( Java8Parser.SingleElementAnnotationContext ctx ) {}
    @Override public void exitSingleElementAnnotation( Java8Parser.SingleElementAnnotationContext ctx ) {}
    */

    // ---------------------------------------------------------------------------------------------
    // Arrays (§10)

    /*
    arrayInitializer
        :   '{' variableInitializerList? ','? '}'
        ;
    @Override public void enterArrayInitializer( Java8Parser.ArrayInitializerContext ctx ) {}
    */
    @Override public void exitArrayInitializer( Java8Parser.ArrayInitializerContext ctx )
    {
        rewriter.replace(ctx.getToken(Java8Parser.LBRACE, 0), "[");
        rewriter.replace(ctx.getToken(Java8Parser.RBRACE, 0), "]");
        TerminalNode tn = ctx.getToken(Java8Parser.COMMA, 0);
        if (null != tn)
            rewriter.delete(tn);
    }

    /*
    variableInitializerList
        :   variableInitializer (',' variableInitializer)*
        ;
    @Override public void enterVariableInitializerList( Java8Parser.VariableInitializerListContext ctx ) {}
    @Override public void exitVariableInitializerList( Java8Parser.VariableInitializerListContext ctx ) {}
    */

    // ---------------------------------------------------------------------------------------------
    // Blocks and Statements (§14)

    /*
    block
        :   '{' blockStatements? '}'
        ;
    @Override public void enterBlock( Java8Parser.BlockContext ctx ) {}
    @Override public void exitBlock( Java8Parser.BlockContext ctx ) {}
    */

    /*
    blockStatements
        :   blockStatement blockStatement*
        ;
    @Override public void enterBlockStatements( Java8Parser.BlockStatementsContext ctx ) {}
    @Override public void exitBlockStatements( Java8Parser.BlockStatementsContext ctx ) {}
    */

    /*
    blockStatement
        :   localVariableDeclarationStatement
        |   classDeclaration
        |   statement
        ;
    @Override public void enterBlockStatement( Java8Parser.BlockStatementContext ctx ) {}
    @Override public void exitBlockStatement( Java8Parser.BlockStatementContext ctx ) {}
    */

    /*
    localVariableDeclarationStatement
        :   localVariableDeclaration ';'
        ;
    @Override public void enterLocalVariableDeclarationStatement( Java8Parser.LocalVariableDeclarationStatementContext ctx ) {}
    @Override public void exitLocalVariableDeclarationStatement( Java8Parser.LocalVariableDeclarationStatementContext ctx ) {}
    */

    /*
    localVariableDeclaration
        :   variableModifier* unannType variableDeclaratorList
        ;
    @Override public void enterLocalVariableDeclaration( Java8Parser.LocalVariableDeclarationContext ctx ) {}
    @Override public void exitLocalVariableDeclaration( Java8Parser.LocalVariableDeclarationContext ctx ) {}
    */

    private void addBracesAroundStatementIfNecessary(ParserRuleContext ctx)
    {
        // Ensure the statement(s) with if(else), for, while and do is always wrapped in braces.
        // At the same time, remove the parentheses around the test/control part for the statement
        int statementRule = ctx.getRuleIndex();
        if ( statementRule != Java8Parser.RULE_statement
          && statementRule != Java8Parser.RULE_statementNoShortIf )
             return; // not our expected parameter type
        ParserRuleContext parent = ctx.getParent();
        int parentRule = parent.getRuleIndex();
        switch ( parentRule ) {
            case Java8Parser.RULE_ifThenElseStatement:
            case Java8Parser.RULE_ifThenElseStatementNoShortIf:
            {
                // if this statement is an ifThen or an ifThenElse sitting within an ifThenElse, then 
                // check if it follows the else, because we don't wrap the trailing 'if' part of 'else if'
                int statementSubRule = ctx.getChild(ParserRuleContext.class, 0).getRuleIndex();
                if ( statementSubRule == Java8Parser.RULE_ifThenStatement
                  || statementSubRule == Java8Parser.RULE_ifThenElseStatement
                  || statementSubRule == Java8Parser.RULE_ifThenElseStatementNoShortIf )
                {
                    // the statement after else is the last child
                    if (parent.getChild(parent.getChildCount()-1) == ctx)
                        break;
                }
            }
            // fallthru
            case Java8Parser.RULE_ifThenStatement:
            case Java8Parser.RULE_basicForStatement:
            case Java8Parser.RULE_basicForStatementNoShortIf:
            case Java8Parser.RULE_enhancedForStatement:
            case Java8Parser.RULE_enhancedForStatementNoShortIf:
            case Java8Parser.RULE_whileStatement:
            case Java8Parser.RULE_whileStatementNoShortIf:
            case Java8Parser.RULE_doStatement:
                if ( ctx.start.getType() != Java8Parser.LBRACE ) {
                    rewriter.insertBefore( ctx.start, "{ " );
                    // rewriter.insertAfter( ctx.stop, " }" );
                    // ...we don't insert, because it binds to the following token and we may need to change/modify it
                    // higher up the stack. Instead, we replace the current content of the stop token. This is necessary
                    // because the stop can be the end of more than one statement, e.g. last semicolon in...
                    //  for(;;i++)
                    //      if (i%7)
                    //          break;
                    // ...gets wrapped twice to give
                    //  for(;;i++)
                    //      { if (i%7)
                    //          { break; } }
                    String current = rewriter.getText(ctx.stop);
                    rewriter.replace(ctx.stop, current+" }");
                }
                break;
            default:
                return;
        }

        // Remove the parentheses around the test/control part for the statement
        removeParenthesesAroundExpression(parent);
    }

    private void removeParenthesesAroundExpression( ParserRuleContext ctx )
    {
        TerminalNode leftParenTN = ctx.getToken(Java8Parser.LPAREN, 0);
        TerminalNode rightParenTN = ctx.getToken(Java8Parser.RPAREN, 0);
        if (null == leftParenTN || null == rightParenTN)
            return;
        rewriter.deleteAndAdjustWhitespace(leftParenTN);
        rewriter.deleteAndAdjustWhitespace(rightParenTN);
    }

    /*
    statement
        :   statementWithoutTrailingSubstatement
        |   labeledStatement
        |   ifThenStatement
        |   ifThenElseStatement
        |   whileStatement
        |   forStatement
        ;
    @Override public void enterStatement( Java8Parser.StatementContext ctx ) {}
    */
    @Override public void exitStatement( Java8Parser.StatementContext ctx )
    {
        addBracesAroundStatementIfNecessary(ctx);
    }

    /*
    statementNoShortIf
        :   statementWithoutTrailingSubstatement
        |   labeledStatementNoShortIf
        |   ifThenElseStatementNoShortIf
        |   whileStatementNoShortIf
        |   forStatementNoShortIf
        ;
    @Override public void enterStatementNoShortIf( Java8Parser.StatementNoShortIfContext ctx ) {}
    */
    @Override public void exitStatementNoShortIf( Java8Parser.StatementNoShortIfContext ctx )
    {
        addBracesAroundStatementIfNecessary(ctx);
    }

    /*
    statementWithoutTrailingSubstatement
        :   block
        |   emptyStatement
        |   expressionStatement
        |   assertStatement
        |   switchStatement
        |   doStatement
        |   breakStatement
        |   continueStatement
        |   returnStatement
        |   synchronizedStatement
        |   throwStatement
        |   tryStatement
        ;
    @Override public void enterStatementWithoutTrailingSubstatement( Java8Parser.StatementWithoutTrailingSubstatementContext ctx ) {}
    @Override public void exitStatementWithoutTrailingSubstatement( Java8Parser.StatementWithoutTrailingSubstatementContext ctx ) {}
    */

    /*
    emptyStatement
        :   ';'
        ;
    @Override public void enterEmptyStatement( Java8Parser.EmptyStatementContext ctx ) {}
    @Override public void exitEmptyStatement( Java8Parser.EmptyStatementContext ctx ) {}
    */

    /*
    labeledStatement
        :   Identifier ':' statement
        ;
    @Override public void enterLabeledStatement( Java8Parser.LabeledStatementContext ctx ) {}
    @Override public void exitLabeledStatement( Java8Parser.LabeledStatementContext ctx ) {}
    */

    /*
    labeledStatementNoShortIf
        :   Identifier ':' statementNoShortIf
        ;
    @Override public void enterLabeledStatementNoShortIf( Java8Parser.LabeledStatementNoShortIfContext ctx ) {}
    @Override public void exitLabeledStatementNoShortIf( Java8Parser.LabeledStatementNoShortIfContext ctx ) {}
    */

    /*
    expressionStatement
        :   statementExpression ';'
        ;
    @Override public void enterExpressionStatement( Java8Parser.ExpressionStatementContext ctx ) {}
    @Override public void exitExpressionStatement( Java8Parser.ExpressionStatementContext ctx ) {}
    */

    /*
    statementExpression
        :   assignment
        |   preIncrementExpression
        |   preDecrementExpression
        |   postIncrementExpression
        |   postDecrementExpression
        |   methodInvocation
        |   classInstanceCreationExpression
        ;
    @Override public void enterStatementExpression( Java8Parser.StatementExpressionContext ctx ) {}
    @Override public void exitStatementExpression( Java8Parser.StatementExpressionContext ctx ) {}
    */

    /*
    ifThenStatement
        :   'if' '(' expression ')' statement
        ;
    @Override public void enterIfThenStatement( Java8Parser.IfThenStatementContext ctx ) {}
    @Override public void exitIfThenStatement( Java8Parser.IfThenStatementContext ctx ) {}
    */

    /*
    ifThenElseStatement
        :   'if' '(' expression ')' statementNoShortIf 'else' statement
        ;
    @Override public void enterIfThenElseStatement( Java8Parser.IfThenElseStatementContext ctx ) {}
    @Override public void exitIfThenElseStatement( Java8Parser.IfThenElseStatementContext ctx ) {}
    */

    /*
    ifThenElseStatementNoShortIf
        :   'if' '(' expression ')' statementNoShortIf 'else' statementNoShortIf
        ;
    @Override public void enterIfThenElseStatementNoShortIf( Java8Parser.IfThenElseStatementNoShortIfContext ctx ) {}
    @Override public void exitIfThenElseStatementNoShortIf( Java8Parser.IfThenElseStatementNoShortIfContext ctx ) {}
    */

    /*
    assertStatement
        :   'assert' expression ';'
        |   'assert' expression ':' expression ';'
        ;
    @Override public void enterAssertStatement( Java8Parser.AssertStatementContext ctx ) {}
    @Override public void exitAssertStatement( Java8Parser.AssertStatementContext ctx ) {}
    */

    /*
    switchStatement
        :   'switch' '(' expression ')' switchBlock
        ;
    @Override public void enterSwitchStatement( Java8Parser.SwitchStatementContext ctx ) {}
    @Override public void exitSwitchStatement( Java8Parser.SwitchStatementContext ctx ) {}
    */

    /*
    switchBlock
        :   '{' switchBlockStatementGroup* switchLabel* '}'
        ;
    @Override public void enterSwitchBlock( Java8Parser.SwitchBlockContext ctx ) {}
    */
    @Override public void exitSwitchBlock( Java8Parser.SwitchBlockContext ctx )
    {
        if (ctx.getChildCount() <= 2) return; // (...assure the compiler)

        // Ensure any trailing switch labels are grouped and end with a break, and we add a default clause if missing
        List<Java8Parser.SwitchLabelContext> switchLabels = ctx.switchLabel();
        Java8Parser.SwitchLabelContext switchLabelCtx;
        Token appendToToken = null;
        String append = "";

        // First need newline + indent for our insertions
        String wrap = rewriter.lineBreak;
        Token token = rewriter.getTokenPreceding(ctx.getChild(ParserRuleContext.class, 0).start);
        if (null != token && token.getType() == Java8Parser.WS)
            wrap += token.getText();

        // Get the last switch label
        if (!switchLabels.isEmpty())
        {
            groupConsecutiveSwitchLabels(switchLabels);
            // Check if the last label is a default
            switchLabelCtx = switchLabels.get(switchLabels.size()-1);
            if (switchLabelCtx.getChildCount() != 2) // two tokens for default + :, three for case + value + :
            {
                if (switchLabels.size() > 1) // can't merge previous case labels with default, so have to fallthrough
                    append = wrap + rewriter.singleIndent + "fallthrough";
                append += wrap + "default:";
            }
            append += wrap + rewriter.singleIndent + "break";
            appendToToken = switchLabelCtx.stop;
        }
        else
        {
            // No orphan switch labels, so child count - 2 is count of statement groups. Get the last one.
            Java8Parser.SwitchBlockStatementGroupContext grp = ctx.switchBlockStatementGroup(ctx.getChildCount() - 3);
            switchLabels = grp.switchLabels().switchLabel();
            // Check if the last label is a default
            switchLabelCtx = switchLabels.get(switchLabels.size()-1);
            if (switchLabelCtx.getChildCount() != 2) // two tokens for default + :, three for case + value + :
            {
                append += wrap + "default:" + wrap + rewriter.singleIndent + "break";
                appendToToken = grp.stop;
            }
        }

        if (null != appendToToken)
            rewriter.insertAfter(appendToToken, append);
    }

    /*
    switchBlockStatementGroup
        :   switchLabels blockStatements
        ;
    @Override public void enterSwitchBlockStatementGroup( Java8Parser.SwitchBlockStatementGroupContext ctx ) {}
    */
    @Override public void exitSwitchBlockStatementGroup( Java8Parser.SwitchBlockStatementGroupContext ctx )
    {
        // Append explicit 'fallthrough' if there is a path without a break (only simplest cases)
        if (!statementEndsWithSwitchExit(ctx.blockStatements()))
        {
            String append = rewriter.lineBreak
                          + rewriter.getTokenPreceding(ctx.start).getText() // indent
                          + rewriter.singleIndent
                          + "fallthrough";
            rewriter.insertAfter(ctx.stop, append);
        }
    }

    private boolean statementEndsWithSwitchExit( ParserRuleContext ctx )
    {
        ParserRuleContext subCtx = ctx;
        for (ctx = subCtx; ctx != null; ctx = subCtx)
        {
            switch (ctx.getRuleIndex())
            {
                case Java8Parser.RULE_blockStatements:
                    subCtx = ctx.getChild(Java8Parser.BlockStatementContext.class, ctx.getChildCount()-1);
                    continue;
                case Java8Parser.RULE_blockStatement:
                    subCtx = ctx.getChild(ParserRuleContext.class, 0); // class or local var decl, or other statement
                    continue;
                case Java8Parser.RULE_localVariableDeclarationStatement:
                    return false;
                case Java8Parser.RULE_classDeclaration:
                    return false;
                case Java8Parser.RULE_statement:
                case Java8Parser.RULE_statementNoShortIf:
                    subCtx = ctx.getChild(ParserRuleContext.class, 0);
                    continue;
                case Java8Parser.RULE_statementWithoutTrailingSubstatement:
                    subCtx = ctx.getChild(ParserRuleContext.class, 0);
                    continue;
                case Java8Parser.RULE_labeledStatement:
                case Java8Parser.RULE_labeledStatementNoShortIf:
                    // Identifier ':' (statement|statementNoShortIf)
                    // nodes 1 & 2 are terminal nodes; node 3 is first rule node
                    subCtx = ctx.getChild(ParserRuleContext.class, 0);
                    continue;
                case Java8Parser.RULE_breakStatement:
                case Java8Parser.RULE_continueStatement:
                case Java8Parser.RULE_returnStatement:
                case Java8Parser.RULE_throwStatement:
                    return true;
                case Java8Parser.RULE_ifThenStatement:
                    return false;
                case Java8Parser.RULE_ifThenElseStatement:
                case Java8Parser.RULE_ifThenElseStatementNoShortIf:
                    // 'if' '(' expression ')' statementNoShortIf 'else' statement
                    // if-statement is second rule node; else-statement is third rule node; others are terminal nodes
                    return statementEndsWithSwitchExit(ctx.getChild(ParserRuleContext.class, 1))
                        && statementEndsWithSwitchExit(ctx.getChild(ParserRuleContext.class, 2));
                case Java8Parser.RULE_whileStatement:
                case Java8Parser.RULE_whileStatementNoShortIf:
                case Java8Parser.RULE_forStatement:
                case Java8Parser.RULE_forStatementNoShortIf:
                case Java8Parser.RULE_doStatement:
                    // indeterminate: whether a nested exit is hit depends on data
                    return false;
                case Java8Parser.RULE_block:
                    // '{' blockStatements? '}'
                    // ctx.getChildCount() counts both rule and terminal nodes; nbr of rule nodes is two less here;
                    subCtx = ctx.getChild(ParserRuleContext.class, ctx.getChildCount()-3);
                    continue;
                case Java8Parser.RULE_emptyStatement:
                case Java8Parser.RULE_expressionStatement:
                case Java8Parser.RULE_assertStatement:
                    return false;
                case Java8Parser.RULE_switchStatement:
                    // too much work
                    return false;
                case Java8Parser.RULE_synchronizedStatement:
                    // 'synchronized' '(' expression ')' block
                case Java8Parser.RULE_tryStatement:
                    // 'try' block catches | 'try' block catches? finally_ | tryWithResourcesStatement
                    subCtx = ctx.getChild(Java8Parser.BlockContext.class, 0);
                    continue;
                default:
                    return false;
            }
        }
        return false;
    }

    /*
    switchLabels
        :   switchLabel switchLabel*
        ;
    @Override public void enterSwitchLabels( Java8Parser.SwitchLabelsContext ctx ) {}
    */
    @Override public void exitSwitchLabels( Java8Parser.SwitchLabelsContext ctx )
    {
        groupConsecutiveSwitchLabels(ctx.switchLabel());
    }

    private void groupConsecutiveSwitchLabels( List<Java8Parser.SwitchLabelContext> switchLabels )
    {
        if (switchLabels.size() < 2)
            return;
        // Ensure consecutive switch labels are joined by comma instead of :\n\s+case
        int index = 0, indexLast = switchLabels.size();
        TerminalNode colonNode = null;
        for (Java8Parser.SwitchLabelContext switchLabel : switchLabels)
        {
            if (1 == ++index)
            {
                colonNode = switchLabel.getToken(Java8Parser.COLON, 0);
                continue;
            }
            if (switchLabel.getChildCount() == 2) // ==> default
            {
                Token token = rewriter.getTokenPreceding(switchLabel.start);
                String indent = null != token && token.getType() == Java8Parser.WS ? token.getText() : "";
                rewriter.insertAfter(colonNode, rewriter.lineBreak + indent + rewriter.singleIndent + "fallthrough");
            }
            else
            {
                rewriter.replace(colonNode, ",");
                colonNode = switchLabel.getToken(Java8Parser.COLON, 0);
                rewriter.replace(switchLabel.getToken(Java8Parser.CASE, 0), "    ");
            }
        }
    }

    /*
    switchLabel
        :   'case' constantExpression ':'
        |   'case' enumConstantName ':'
        |   'default' ':'
        ;
    @Override public void enterSwitchLabel( Java8Parser.SwitchLabelContext ctx ) {}
    @Override public void exitSwitchLabel( Java8Parser.SwitchLabelContext ctx ) {}
    */

    /*
    enumConstantName
        :   Identifier
        ;
    @Override public void enterEnumConstantName( Java8Parser.EnumConstantNameContext ctx ) {}
    @Override public void exitEnumConstantName( Java8Parser.EnumConstantNameContext ctx ) {}
    */

    /*
    whileStatement
        :   'while' '(' expression ')' statement
        ;
    @Override public void enterWhileStatement( Java8Parser.WhileStatementContext ctx ) {}
    @Override public void exitWhileStatement( Java8Parser.WhileStatementContext ctx ) {}
    */

    /*
    whileStatementNoShortIf
        :   'while' '(' expression ')' statementNoShortIf
        ;
    @Override public void enterWhileStatementNoShortIf( Java8Parser.WhileStatementNoShortIfContext ctx ) {}
    @Override public void exitWhileStatementNoShortIf( Java8Parser.WhileStatementNoShortIfContext ctx ) {}
    */

    /*
    doStatement
        :   'do' statement 'while' '(' expression ')' ';'
        ;
    @Override public void enterDoStatement( Java8Parser.DoStatementContext ctx ) {}
    */
    @Override public void exitDoStatement( Java8Parser.DoStatementContext ctx )
    {
        TerminalNode tn = ctx.getToken(Java8Parser.DO, 0);
        if (null != tn)
            rewriter.replace(tn, "repeat");
    }

    /*
    forStatement
        :   basicForStatement
        |   enhancedForStatement
        ;
    @Override public void enterForStatement( Java8Parser.ForStatementContext ctx ) {}
    @Override public void exitForStatement( Java8Parser.ForStatementContext ctx ) {}
    */

    /*
    forStatementNoShortIf
        :   basicForStatementNoShortIf
        |   enhancedForStatementNoShortIf
        ;
    @Override public void enterForStatementNoShortIf( Java8Parser.ForStatementNoShortIfContext ctx ) {}
    @Override public void exitForStatementNoShortIf( Java8Parser.ForStatementNoShortIfContext ctx ) {}
    */

    /*
    basicForStatement
        :   'for' '(' forInit? ';' expression? ';' forUpdate? ')' statement
        ;
    @Override public void enterBasicForStatement( Java8Parser.BasicForStatementContext ctx ) {}
    */
    @Override public void exitBasicForStatement( Java8Parser.BasicForStatementContext ctx )
    {
        J2SConvertBasicFor.convert(ctx, rewriter);
    }

    /*
    basicForStatementNoShortIf
        :   'for' '(' forInit? ';' expression? ';' forUpdate? ')' statementNoShortIf
        ;
    @Override public void enterBasicForStatementNoShortIf( Java8Parser.BasicForStatementNoShortIfContext ctx ) {}
    */
    @Override public void exitBasicForStatementNoShortIf( Java8Parser.BasicForStatementNoShortIfContext ctx )
    {
        J2SConvertBasicFor.convert(ctx, rewriter);
    }

    /*
    forInit
        :   statementExpressionList
        |   localVariableDeclaration
        ;
    @Override public void enterForInit( Java8Parser.ForInitContext ctx ) {}
    @Override public void exitForInit( Java8Parser.ForInitContext ctx ) {}
    */

    /*
    forUpdate
        :   statementExpressionList
        ;
    @Override public void enterForUpdate( Java8Parser.ForUpdateContext ctx ) {}
    @Override public void exitForUpdate( Java8Parser.ForUpdateContext ctx ) {}
    */

    /*
    statementExpressionList
        :   statementExpression (',' statementExpression)*
        ;
    @Override public void enterStatementExpressionList( Java8Parser.StatementExpressionListContext ctx ) {}
    @Override public void exitStatementExpressionList( Java8Parser.StatementExpressionListContext ctx ) {}
    */

    /*
    enhancedForStatement
        :   'for' '(' variableModifier* unannType variableDeclaratorId ':' expression ')' statement
        ;
    @Override public void enterEnhancedForStatement( Java8Parser.EnhancedForStatementContext ctx ) {}
    */
    @Override public void exitEnhancedForStatement( Java8Parser.EnhancedForStatementContext ctx )
    {
        TerminalNode tn = ctx.getToken(Java8Parser.COLON, 0);
        if (null != tn)
            rewriter.replace(tn, "in");
    }

    /*
    enhancedForStatementNoShortIf
        :   'for' '(' variableModifier* unannType variableDeclaratorId ':' expression ')' statementNoShortIf
        ;
    @Override public void enterEnhancedForStatementNoShortIf( Java8Parser.EnhancedForStatementNoShortIfContext ctx ) {}
    */
    @Override public void exitEnhancedForStatementNoShortIf( Java8Parser.EnhancedForStatementNoShortIfContext ctx )
    {
        TerminalNode tn = ctx.getToken(Java8Parser.COLON, 0);
        if (null != tn)
            rewriter.replace(tn, "in");
    }

    /*
    breakStatement
        :   'break' Identifier? ';'
        ;
    @Override public void enterBreakStatement( Java8Parser.BreakStatementContext ctx ) {}
    @Override public void exitBreakStatement( Java8Parser.BreakStatementContext ctx ) {}
    */

    /*
    continueStatement
        :   'continue' Identifier? ';'
        ;
    @Override public void enterContinueStatement( Java8Parser.ContinueStatementContext ctx ) {}
    @Override public void exitContinueStatement( Java8Parser.ContinueStatementContext ctx ) {}
    */

    /*
    returnStatement
        :   'return' expression? ';'
        ;
    @Override public void enterReturnStatement( Java8Parser.ReturnStatementContext ctx ) {}
    @Override public void exitReturnStatement( Java8Parser.ReturnStatementContext ctx ) {}
    */

    /*
    throwStatement
        :   'throw' expression ';'
        ;
    @Override public void enterThrowStatement( Java8Parser.ThrowStatementContext ctx ) {}
    @Override public void exitThrowStatement( Java8Parser.ThrowStatementContext ctx ) {}
    */

    /*
    synchronizedStatement
        :   'synchronized' '(' expression ')' block
        ;
    @Override public void enterSynchronizedStatement( Java8Parser.SynchronizedStatementContext ctx ) {}
    @Override public void exitSynchronizedStatement( Java8Parser.SynchronizedStatementContext ctx ) {}
    */

    /*
    tryStatement
        :   'try' block catches
        |   'try' block catches? finally_
        |   tryWithResourcesStatement
        ;
    @Override public void enterTryStatement( Java8Parser.TryStatementContext ctx ) {}
    */
    @Override public void exitTryStatement( Java8Parser.TryStatementContext ctx )
    {
        // In Swift, a 'try' block becomes a 'do' block and individual calls within the block have to be prefixed with
        // 'try' if they are capable of throwing. We can't work this out because we don't have access to the the throws
        // property of all invoked functions and methods. This is easier solved by later manual correction as the Swift
        // compiler will identify them for us.
        TerminalNode tn = ctx.getToken(Java8Parser.TRY, 0);
        if (null == tn)
            tn = ctx.tryWithResourcesStatement().getToken(Java8Parser.TRY, 0);
        if (null != tn)
            rewriter.replace(tn, "do");
    }

    /*
    catches
        :   catchClause catchClause*
        ;
    @Override public void enterCatches( Java8Parser.CatchesContext ctx ) {}
    @Override public void exitCatches( Java8Parser.CatchesContext ctx ) {}
    */

    /*
    catchClause
        :   'catch' '(' catchFormalParameter ')' block
        ;
    @Override public void enterCatchClause( Java8Parser.CatchClauseContext ctx ) {}
    */
    @Override public void exitCatchClause( Java8Parser.CatchClauseContext ctx )
    {
        removeParenthesesAroundExpression(ctx);
    }

    /*
    catchFormalParameter
        :   variableModifier* catchType variableDeclaratorId
        ;
    @Override public void enterCatchFormalParameter( Java8Parser.CatchFormalParameterContext ctx ) {}
    @Override public void exitCatchFormalParameter( Java8Parser.CatchFormalParameterContext ctx ) {}
    */

    /*
    catchType
        :   unannClassType ('|' classType)*
        ;
    @Override public void enterCatchType( Java8Parser.CatchTypeContext ctx ) {}
    @Override public void exitCatchType( Java8Parser.CatchTypeContext ctx ) {}
    */

    /*
    finally_
        :   'finally' block
        ;
    @Override public void enterFinally_( Java8Parser.Finally_Context ctx ) {}
    @Override public void exitFinally_( Java8Parser.Finally_Context ctx ) {}
    */

    /*
    tryWithResourcesStatement
        :   'try' resourceSpecification block catches? finally_?
        ;
    @Override public void enterTryWithResourcesStatement( Java8Parser.TryWithResourcesStatementContext ctx ) {}
    @Override public void exitTryWithResourcesStatement( Java8Parser.TryWithResourcesStatementContext ctx ) {}
    */

    /*
    resourceSpecification
        :   '(' resourceList ';'? ')'
        ;
    @Override public void enterResourceSpecification( Java8Parser.ResourceSpecificationContext ctx ) {}
    @Override public void exitResourceSpecification( Java8Parser.ResourceSpecificationContext ctx ) {}
    */

    /*
    resourceList
        :   resource (';' resource)*
        ;
    @Override public void enterResourceList( Java8Parser.ResourceListContext ctx ) {}
    @Override public void exitResourceList( Java8Parser.ResourceListContext ctx ) {}
    */

    /*
    resource
        :   variableModifier* unannType variableDeclaratorId '=' expression
        ;
    @Override public void enterResource( Java8Parser.ResourceContext ctx ) {}
    @Override public void exitResource( Java8Parser.ResourceContext ctx ) {}
    */

    // ---------------------------------------------------------------------------------------------
    // Expressions (§15)

    /*
    primary
        :   (   primaryNoNewArray_lfno_primary
            |   arrayCreationExpression
            )
            (   primaryNoNewArray_lf_primary
            )*
        ;
    @Override public void enterPrimary( Java8Parser.PrimaryContext ctx ) {}
    @Override public void exitPrimary( Java8Parser.PrimaryContext ctx ) {}
    */

    /*  not directly used by any other rule; narrower partial rules primaryNoNewArray_lf* used instead; so for reference:-
    primaryNoNewArray
        :   literal
        |   typeName ('[' ']')* '.' 'class'
        |   'void' '.' 'class'
        |   'this'
        |   typeName '.' 'this'
        |   '(' expression ')'
        |   classInstanceCreationExpression
        |   fieldAccess
        |   arrayAccess
        |   methodInvocation
        |   methodReference
        ;
    @Override public void enterPrimaryNoNewArray( Java8Parser.PrimaryNoNewArrayContext ctx ) {}
    @Override public void exitPrimaryNoNewArray( Java8Parser.PrimaryNoNewArrayContext ctx ) {}
    */
    private void convertPrimaryVariants( ParserRuleContext ctx )
    {
        // To be called with one of the primaryNoNewArray_lf~ rules.
        // Convert 'this' to 'self' and '.' 'class' to '.' 'Type' 
        int ctxRuleIndex = ctx.getRuleIndex();
        switch (ctxRuleIndex)
        {
            case Java8Parser.RULE_primaryNoNewArray:
            case Java8Parser.RULE_primaryNoNewArray_lfno_arrayAccess:
            case Java8Parser.RULE_primaryNoNewArray_lfno_primary:
            case Java8Parser.RULE_primaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary:
                break;
            default:
                return;
        }
        TerminalNode tn;
        if (null != (tn = ctx.getToken(Java8Parser.THIS, 0)))
            rewriter.replace(tn, "self");
        else
        if (null != (tn = ctx.getToken(Java8Parser.CLASS, 0)))
            rewriter.replace(tn, "Type");
    }

    /*
    primaryNoNewArray_lf_arrayAccess
        :
        ;
    @Override public void enterPrimaryNoNewArray_lf_arrayAccess( Java8Parser.PrimaryNoNewArray_lf_arrayAccessContext ctx ) {}
    @Override public void exitPrimaryNoNewArray_lf_arrayAccess( Java8Parser.PrimaryNoNewArray_lf_arrayAccessContext ctx ) {}
    */

    /*
    primaryNoNewArray_lfno_arrayAccess
        :   literal
        |   typeName ('[' ']')* '.' 'class'
        |   'void' '.' 'class'
        |   'this'
        |   typeName '.' 'this'
        |   '(' expression ')'
        |   classInstanceCreationExpression
        |   fieldAccess
        |   methodInvocation
        |   methodReference
        ;
    @Override public void enterPrimaryNoNewArray_lfno_arrayAccess( Java8Parser.PrimaryNoNewArray_lfno_arrayAccessContext ctx ) {}
    */
    @Override public void exitPrimaryNoNewArray_lfno_arrayAccess( Java8Parser.PrimaryNoNewArray_lfno_arrayAccessContext ctx )
    {
        convertPrimaryVariants(ctx);
    }

    /*
    primaryNoNewArray_lf_primary
        :   classInstanceCreationExpression_lf_primary
        |   fieldAccess_lf_primary
        |   arrayAccess_lf_primary
        |   methodInvocation_lf_primary
        |   methodReference_lf_primary
        ;
    @Override public void enterPrimaryNoNewArray_lf_primary( Java8Parser.PrimaryNoNewArray_lf_primaryContext ctx ) {}
    @Override public void exitPrimaryNoNewArray_lf_primary( Java8Parser.PrimaryNoNewArray_lf_primaryContext ctx ) {}
    */

    /*
    primaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary
        :
        ;
    @Override public void enterPrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary( Java8Parser.PrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primaryContext ctx ) {}
    @Override public void exitPrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary( Java8Parser.PrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primaryContext ctx ) {}
    */

    /*
    primaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary
        :   classInstanceCreationExpression_lf_primary
        |   fieldAccess_lf_primary
        |   methodInvocation_lf_primary
        |   methodReference_lf_primary
        ;
    @Override public void enterPrimaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary( Java8Parser.PrimaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primaryContext ctx ) {}
    @Override public void exitPrimaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary( Java8Parser.PrimaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primaryContext ctx ) {}
    */

    /*
    primaryNoNewArray_lfno_primary
        :   literal
        |   typeName ('[' ']')* '.' 'class'
        |   unannPrimitiveType ('[' ']')* '.' 'class'
        |   'void' '.' 'class'
        |   'this'
        |   typeName '.' 'this'
        |   '(' expression ')'
        |   classInstanceCreationExpression_lfno_primary
        |   fieldAccess_lfno_primary
        |   arrayAccess_lfno_primary
        |   methodInvocation_lfno_primary
        |   methodReference_lfno_primary
        ;
    @Override public void enterPrimaryNoNewArray_lfno_primary( Java8Parser.PrimaryNoNewArray_lfno_primaryContext ctx ) {}
    */
    @Override public void exitPrimaryNoNewArray_lfno_primary( Java8Parser.PrimaryNoNewArray_lfno_primaryContext ctx )
    {
        convertPrimaryVariants(ctx);
    }

    /*
    primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary
        :
        ;
    @Override public void enterPrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary( Java8Parser.PrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primaryContext ctx ) {}
    @Override public void exitPrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary( Java8Parser.PrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primaryContext ctx ) {}
    */

    /*
    primaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary
        :   literal
        |   typeName ('[' ']')* '.' 'class'
        |   unannPrimitiveType ('[' ']')* '.' 'class'
        |   'void' '.' 'class'
        |   'this'
        |   typeName '.' 'this'
        |   '(' expression ')'
        |   classInstanceCreationExpression_lfno_primary
        |   fieldAccess_lfno_primary
        |   methodInvocation_lfno_primary
        |   methodReference_lfno_primary
        ;
    @Override public void enterPrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary( Java8Parser.PrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primaryContext ctx ) {}
    */
    @Override public void exitPrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary( Java8Parser.PrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primaryContext ctx )
    {
        convertPrimaryVariants(ctx);
    }

    /*
    classInstanceCreationExpression
        :   'new' typeArguments? annotation* Identifier ('.' annotation* Identifier)* typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        |   expressionName '.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        |   primary '.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        ;
    @Override public void enterClassInstanceCreationExpression( Java8Parser.ClassInstanceCreationExpressionContext ctx ) {}
    */
    @Override public void exitClassInstanceCreationExpression( Java8Parser.ClassInstanceCreationExpressionContext ctx )
    {
        rewriter.deleteAndAdjustWhitespace(ctx.getToken(Java8Parser.NEW, 0));
        mapClassIdentifierInContext(ctx);
    }

    /*
    classInstanceCreationExpression_lf_primary
        :   '.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        ;
    @Override public void enterClassInstanceCreationExpression_lf_primary( Java8Parser.ClassInstanceCreationExpression_lf_primaryContext ctx ) {}
    */
    @Override public void exitClassInstanceCreationExpression_lf_primary( Java8Parser.ClassInstanceCreationExpression_lf_primaryContext ctx )
    {
        rewriter.deleteAndAdjustWhitespace(ctx.getToken(Java8Parser.NEW, 0));
        mapClassIdentifierInContext(ctx);
    }

    /*
    classInstanceCreationExpression_lfno_primary
        :   'new' typeArguments? annotation* Identifier ('.' annotation* Identifier)* typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        |   expressionName '.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        ;
    @Override public void enterClassInstanceCreationExpression_lfno_primary( Java8Parser.ClassInstanceCreationExpression_lfno_primaryContext ctx ) {}
    */
    @Override public void exitClassInstanceCreationExpression_lfno_primary( Java8Parser.ClassInstanceCreationExpression_lfno_primaryContext ctx )
    {
        rewriter.deleteAndAdjustWhitespace(ctx.getToken(Java8Parser.NEW, 0));
        mapClassIdentifierInContext(ctx);
    }

    /*
    typeArgumentsOrDiamond
        :   typeArguments
        |   '<' '>'
        ;
    @Override public void enterTypeArgumentsOrDiamond( Java8Parser.TypeArgumentsOrDiamondContext ctx ) {}
    @Override public void exitTypeArgumentsOrDiamond( Java8Parser.TypeArgumentsOrDiamondContext ctx ) {}
    */

    /*
    fieldAccess
        :   primary '.' Identifier
        |   'super' '.' Identifier
        |   typeName '.' 'super' '.' Identifier
        ;
    @Override public void enterFieldAccess( Java8Parser.FieldAccessContext ctx ) {}
    @Override public void exitFieldAccess( Java8Parser.FieldAccessContext ctx ) {}
    */

    /*
    fieldAccess_lf_primary
        :   '.' Identifier
        ;
    @Override public void enterFieldAccess_lf_primary( Java8Parser.FieldAccess_lf_primaryContext ctx ) {}
    @Override public void exitFieldAccess_lf_primary( Java8Parser.FieldAccess_lf_primaryContext ctx ) {}
    */

    /*
    fieldAccess_lfno_primary
        :   'super' '.' Identifier
        |   typeName '.' 'super' '.' Identifier
        ;
    @Override public void enterFieldAccess_lfno_primary( Java8Parser.FieldAccess_lfno_primaryContext ctx ) {}
    @Override public void exitFieldAccess_lfno_primary( Java8Parser.FieldAccess_lfno_primaryContext ctx ) {}
    */

    /*
    arrayAccess
        :   (   expressionName '[' expression ']'
            |   primaryNoNewArray_lfno_arrayAccess '[' expression ']'
            )
            (   primaryNoNewArray_lf_arrayAccess '[' expression ']'
            )*
        ;
    @Override public void enterArrayAccess( Java8Parser.ArrayAccessContext ctx ) {}
    @Override public void exitArrayAccess( Java8Parser.ArrayAccessContext ctx ) {}
    */

    /*
    arrayAccess_lf_primary
        :   (   primaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary '[' expression ']'
            )
            (   primaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary '[' expression ']'
            )*
        ;
    @Override public void enterArrayAccess_lf_primary( Java8Parser.ArrayAccess_lf_primaryContext ctx ) {}
    @Override public void exitArrayAccess_lf_primary( Java8Parser.ArrayAccess_lf_primaryContext ctx ) {}
    */

    /*
    arrayAccess_lfno_primary
        :   (   expressionName '[' expression ']'
            |   primaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary '[' expression ']'
            )
            (   primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary '[' expression ']'
            )*
        ;
    @Override public void enterArrayAccess_lfno_primary( Java8Parser.ArrayAccess_lfno_primaryContext ctx ) {}
    @Override public void exitArrayAccess_lfno_primary( Java8Parser.ArrayAccess_lfno_primaryContext ctx ) {}
    */

    /*
    methodInvocation
        :   methodName '(' argumentList? ')'
        |   typeName '.' typeArguments? Identifier '(' argumentList? ')'
        |   expressionName '.' typeArguments? Identifier '(' argumentList? ')'
        |   primary '.' typeArguments? Identifier '(' argumentList? ')'
        |   'super' '.' typeArguments? Identifier '(' argumentList? ')'
        |   typeName '.' 'super' '.' typeArguments? Identifier '(' argumentList? ')'
        ;
    @Override public void enterMethodInvocation( Java8Parser.MethodInvocationContext ctx ) {}
    @Override public void exitMethodInvocation( Java8Parser.MethodInvocationContext ctx ) {}
    */

    /*
    methodInvocation_lf_primary
        :   '.' typeArguments? Identifier '(' argumentList? ')'
        ;
    @Override public void enterMethodInvocation_lf_primary( Java8Parser.MethodInvocation_lf_primaryContext ctx ) {}
    @Override public void exitMethodInvocation_lf_primary( Java8Parser.MethodInvocation_lf_primaryContext ctx ) {}
    */

    /*
    methodInvocation_lfno_primary
        :   methodName '(' argumentList? ')'
        |   typeName '.' typeArguments? Identifier '(' argumentList? ')'
        |   expressionName '.' typeArguments? Identifier '(' argumentList? ')'
        |   'super' '.' typeArguments? Identifier '(' argumentList? ')'
        |   typeName '.' 'super' '.' typeArguments? Identifier '(' argumentList? ')'
        ;
    @Override public void enterMethodInvocation_lfno_primary( Java8Parser.MethodInvocation_lfno_primaryContext ctx ) {}
    @Override public void exitMethodInvocation_lfno_primary( Java8Parser.MethodInvocation_lfno_primaryContext ctx ) {}
    */

    /*
    argumentList
        :   expression (',' expression)*
        ;
    @Override public void enterArgumentList( Java8Parser.ArgumentListContext ctx ) {}
    @Override public void exitArgumentList( Java8Parser.ArgumentListContext ctx ) {}
    */

    /*
    methodReference
        :   expressionName '::' typeArguments? Identifier
        |   referenceType '::' typeArguments? Identifier
        |   primary '::' typeArguments? Identifier
        |   'super' '::' typeArguments? Identifier
        |   typeName '.' 'super' '::' typeArguments? Identifier
        |   classType '::' typeArguments? 'new'
        |   arrayType '::' 'new'
        ;
    @Override public void enterMethodReference( Java8Parser.MethodReferenceContext ctx ) {}
    @Override public void exitMethodReference( Java8Parser.MethodReferenceContext ctx ) {}
    */

    /*
    methodReference_lf_primary
        :   '::' typeArguments? Identifier
        ;
    @Override public void enterMethodReference_lf_primary( Java8Parser.MethodReference_lf_primaryContext ctx ) {}
    @Override public void exitMethodReference_lf_primary( Java8Parser.MethodReference_lf_primaryContext ctx ) {}
    */

    /*
    methodReference_lfno_primary
        :   expressionName '::' typeArguments? Identifier
        |   referenceType '::' typeArguments? Identifier
        |   'super' '::' typeArguments? Identifier
        |   typeName '.' 'super' '::' typeArguments? Identifier
        |   classType '::' typeArguments? 'new'
        |   arrayType '::' 'new'
        ;
    @Override public void enterMethodReference_lfno_primary( Java8Parser.MethodReference_lfno_primaryContext ctx ) {}
    @Override public void exitMethodReference_lfno_primary( Java8Parser.MethodReference_lfno_primaryContext ctx ) {}
    */

    /*
    arrayCreationExpression
        :   'new' primitiveType dimExprs dims?
        |   'new' classOrInterfaceType dimExprs dims?
        |   'new' primitiveType dims arrayInitializer
        |   'new' classOrInterfaceType dims arrayInitializer
        ;
    @Override public void enterArrayCreationExpression( Java8Parser.ArrayCreationExpressionContext ctx ) {}
    */
    @Override public void exitArrayCreationExpression( Java8Parser.ArrayCreationExpressionContext ctx )
    {
        rewriter.deleteAndAdjustWhitespace(ctx.getToken(Java8Parser.NEW, 0));
    }

    /*
    dimExprs
        :   dimExpr dimExpr*
        ;
    @Override public void enterDimExprs( Java8Parser.DimExprsContext ctx ) {}
    @Override public void exitDimExprs( Java8Parser.DimExprsContext ctx ) {}
    */

    /*
    dimExpr
        :   annotation* '[' expression ']'
        ;
    @Override public void enterDimExpr( Java8Parser.DimExprContext ctx ) {}
    @Override public void exitDimExpr( Java8Parser.DimExprContext ctx ) {}
    */

    /*
    constantExpression
        :   expression
        ;
    @Override public void enterConstantExpression( Java8Parser.ConstantExpressionContext ctx ) {}
    @Override public void exitConstantExpression( Java8Parser.ConstantExpressionContext ctx ) {}
    */

    /*
    expression
        :   lambdaExpression
        |   assignmentExpression
        ;
    @Override public void enterExpression( Java8Parser.ExpressionContext ctx ) {}
    @Override public void exitExpression( Java8Parser.ExpressionContext ctx ) {}
    */

    /*
    lambdaExpression
        :   lambdaParameters '->' lambdaBody
        ;
    @Override public void enterLambdaExpression( Java8Parser.LambdaExpressionContext ctx ) {}
    @Override public void exitLambdaExpression( Java8Parser.LambdaExpressionContext ctx ) {}
    */

    /*
    lambdaParameters
        :   Identifier
        |   '(' formalParameterList? ')'
        |   '(' inferredFormalParameterList ')'
        ;
    @Override public void enterLambdaParameters( Java8Parser.LambdaParametersContext ctx ) {}
    @Override public void exitLambdaParameters( Java8Parser.LambdaParametersContext ctx ) {}
    */

    /*
    inferredFormalParameterList
        :   Identifier (',' Identifier)*
        ;
    @Override public void enterInferredFormalParameterList( Java8Parser.InferredFormalParameterListContext ctx ) {}
    @Override public void exitInferredFormalParameterList( Java8Parser.InferredFormalParameterListContext ctx ) {}
    */

    /*
    lambdaBody
        :   expression
        |   block
        ;
    @Override public void enterLambdaBody( Java8Parser.LambdaBodyContext ctx ) {}
    @Override public void exitLambdaBody( Java8Parser.LambdaBodyContext ctx ) {}
    */

    /*
    assignmentExpression
        :   conditionalExpression
        |   assignment
        ;
    @Override public void enterAssignmentExpression( Java8Parser.AssignmentExpressionContext ctx ) {}
    @Override public void exitAssignmentExpression( Java8Parser.AssignmentExpressionContext ctx ) {}
    */

    /*
    assignment
        :   leftHandSide assignmentOperator expression
        ;
    @Override public void enterAssignment( Java8Parser.AssignmentContext ctx ) {}
    @Override public void exitAssignment( Java8Parser.AssignmentContext ctx ) {}
    */

    /*
    leftHandSide
        :   expressionName
        |   fieldAccess
        |   arrayAccess
        ;
    @Override public void enterLeftHandSide( Java8Parser.LeftHandSideContext ctx ) {}
    @Override public void exitLeftHandSide( Java8Parser.LeftHandSideContext ctx ) {}
    */

    /*
    assignmentOperator
        :   '='
        |   '*='
        |   '/='
        |   '%='
        |   '+='
        |   '-='
        |   '<<='
        |   '>>='
        |   '>>>='
        |   '&='
        |   '^='
        |   '|='
        ;
    @Override public void enterAssignmentOperator( Java8Parser.AssignmentOperatorContext ctx ) {}
    @Override public void exitAssignmentOperator( Java8Parser.AssignmentOperatorContext ctx ) {}
    */

    /*
    conditionalExpression
        :   conditionalOrExpression
        |   conditionalOrExpression '?' expression ':' conditionalExpression
        ;
    @Override public void enterConditionalExpression( Java8Parser.ConditionalExpressionContext ctx ) {}
    @Override public void exitConditionalExpression( Java8Parser.ConditionalExpressionContext ctx ) {}
    */

    /*
    conditionalOrExpression
        :   conditionalAndExpression
        |   conditionalOrExpression '||' conditionalAndExpression
        ;
    @Override public void enterConditionalOrExpression( Java8Parser.ConditionalOrExpressionContext ctx ) {}
    @Override public void exitConditionalOrExpression( Java8Parser.ConditionalOrExpressionContext ctx ) {}
    */

    /*
    conditionalAndExpression
        :   inclusiveOrExpression
        |   conditionalAndExpression '&&' inclusiveOrExpression
        ;
    @Override public void enterConditionalAndExpression( Java8Parser.ConditionalAndExpressionContext ctx ) {}
    @Override public void exitConditionalAndExpression( Java8Parser.ConditionalAndExpressionContext ctx ) {}
    */

    /*
    inclusiveOrExpression
        :   exclusiveOrExpression
        |   inclusiveOrExpression '|' exclusiveOrExpression
        ;
    @Override public void enterInclusiveOrExpression( Java8Parser.InclusiveOrExpressionContext ctx ) {}
    @Override public void exitInclusiveOrExpression( Java8Parser.InclusiveOrExpressionContext ctx ) {}
    */

    /*
    exclusiveOrExpression
        :   andExpression
        |   exclusiveOrExpression '^' andExpression
        ;
    @Override public void enterExclusiveOrExpression( Java8Parser.ExclusiveOrExpressionContext ctx ) {}
    @Override public void exitExclusiveOrExpression( Java8Parser.ExclusiveOrExpressionContext ctx ) {}
    */

    /*
    andExpression
        :   equalityExpression
        |   andExpression '&' equalityExpression
        ;
    @Override public void enterAndExpression( Java8Parser.AndExpressionContext ctx ) {}
    @Override public void exitAndExpression( Java8Parser.AndExpressionContext ctx ) {}
    */

    /*
    equalityExpression
        :   relationalExpression
        |   equalityExpression '==' relationalExpression
        |   equalityExpression '!=' relationalExpression
        ;
    @Override public void enterEqualityExpression( Java8Parser.EqualityExpressionContext ctx ) {}
    @Override public void exitEqualityExpression( Java8Parser.EqualityExpressionContext ctx ) {}
    */

    /*
    relationalExpression
        :   shiftExpression
        |   relationalExpression '<' shiftExpression
        |   relationalExpression '>' shiftExpression
        |   relationalExpression '<=' shiftExpression
        |   relationalExpression '>=' shiftExpression
        |   relationalExpression 'instanceof' referenceType
        ;
    @Override public void enterRelationalExpression( Java8Parser.RelationalExpressionContext ctx ) {}
    */
    @Override public void exitRelationalExpression( Java8Parser.RelationalExpressionContext ctx )
    {
        TerminalNode tn;
        if (1 < ctx.getChildCount()
         && null != (tn = ctx.getToken(Java8Parser.INSTANCEOF, 0)))
            rewriter.replace(tn, "is");
    }

    /*
    shiftExpression
        :   additiveExpression
        |   shiftExpression '<' '<' additiveExpression
        |   shiftExpression '>' '>' additiveExpression
        |   shiftExpression '>' '>' '>' additiveExpression
        ;
    @Override public void enterShiftExpression( Java8Parser.ShiftExpressionContext ctx ) {}
    @Override public void exitShiftExpression( Java8Parser.ShiftExpressionContext ctx ) {}
    */

    /*
    additiveExpression
        :   multiplicativeExpression
        |   additiveExpression '+' multiplicativeExpression
        |   additiveExpression '-' multiplicativeExpression
        ;
    @Override public void enterAdditiveExpression( Java8Parser.AdditiveExpressionContext ctx ) {}
    @Override public void exitAdditiveExpression( Java8Parser.AdditiveExpressionContext ctx ) {}
    */

    /*
    multiplicativeExpression
        :   unaryExpression
        |   multiplicativeExpression '*' unaryExpression
        |   multiplicativeExpression '/' unaryExpression
        |   multiplicativeExpression '%' unaryExpression
        ;
    @Override public void enterMultiplicativeExpression( Java8Parser.MultiplicativeExpressionContext ctx ) {}
    @Override public void exitMultiplicativeExpression( Java8Parser.MultiplicativeExpressionContext ctx ) {}
    */

    /*
    unaryExpression
        :   preIncrementExpression
        |   preDecrementExpression
        |   '+' unaryExpression
        |   '-' unaryExpression
        |   unaryExpressionNotPlusMinus
        ;
    @Override public void enterUnaryExpression( Java8Parser.UnaryExpressionContext ctx ) {}
    @Override public void exitUnaryExpression( Java8Parser.UnaryExpressionContext ctx ) {}
    */

    /*
    preIncrementExpression
        :   '++' unaryExpression
        ;
    @Override public void enterPreIncrementExpression( Java8Parser.PreIncrementExpressionContext ctx ) {}
    */
    @Override public void exitPreIncrementExpression( Java8Parser.PreIncrementExpressionContext ctx )
    {
        // Cater for the simple case of ++a --> a+=1. Leave more complex cases for manual fix.
        rewriter.delete(ctx.start);
        rewriter.replace(ctx.stop, rewriter.getText(ctx.stop) + " += 1");
        // ...using insertAfter instead is problematic as the text pre-pends to the following token and a subsequent
        // rewriter.getText() for this context will not retrieve it.
    }

    /*
    preDecrementExpression
        :   '--' unaryExpression
        ;
    @Override public void enterPreDecrementExpression( Java8Parser.PreDecrementExpressionContext ctx ) {}
    */
    @Override public void exitPreDecrementExpression( Java8Parser.PreDecrementExpressionContext ctx )
    {
        // Cater for the simple case of --a --> a-=1. Leave more complex cases for manual fix.
        rewriter.delete(ctx.start);
        rewriter.replace(ctx.stop, rewriter.getText(ctx.stop) + " -= 1");
        // ...using insertAfter instead is problematic as the text pre-pends to the following token and a subsequent
        // rewriter.getText() for this context will not retrieve it.
    }

    /*
    unaryExpressionNotPlusMinus
        :   postfixExpression
        |   '~' unaryExpression
        |   '!' unaryExpression
        |   castExpression
        ;
    @Override public void enterUnaryExpressionNotPlusMinus( Java8Parser.UnaryExpressionNotPlusMinusContext ctx ) {}
    @Override public void exitUnaryExpressionNotPlusMinus( Java8Parser.UnaryExpressionNotPlusMinusContext ctx ) {}
    */

    /*
    postfixExpression
        :   (   primary
            |   expressionName
            )
            (   postIncrementExpression_lf_postfixExpression
            |   postDecrementExpression_lf_postfixExpression
            )*
        ;
    @Override public void enterPostfixExpression( Java8Parser.PostfixExpressionContext ctx ) {}
    */
    @Override public void exitPostfixExpression( Java8Parser.PostfixExpressionContext ctx )
    {
        int increments = ctx.getChildCount() - 1;
        int increment = 0;
        for (int i = 0; i < increments; i++)
        {
            ParserRuleContext postfixCtx = ctx.getChild(ParserRuleContext.class, i+1);
            switch (postfixCtx.getRuleIndex()) {
                case Java8Parser.RULE_postIncrementExpression_lf_postfixExpression: increment++; break;
                case Java8Parser.RULE_postDecrementExpression_lf_postfixExpression: increment--; break;
            }
            rewriter.delete(postfixCtx);
        }
        if (0 != increment)
            rewriter.replace(ctx.stop, 0 < increment ? (" += " + increment) : (" -= " + -increment));
    }

    /*
    postIncrementExpression
        :   postfixExpression '++'
        ;
    @Override public void enterPostIncrementExpression( Java8Parser.PostIncrementExpressionContext ctx ) {}
    */
    @Override public void exitPostIncrementExpression( Java8Parser.PostIncrementExpressionContext ctx )
    {
        // Cater for the simple case of a++ --> a+=1. Leave more complex cases for manual fix.
        rewriter.replace(ctx.stop, " += 1");
    }

    /*
    postIncrementExpression_lf_postfixExpression
        :   '++'
        ;
    @Override public void enterPostIncrementExpression_lf_postfixExpression( Java8Parser.PostIncrementExpression_lf_postfixExpressionContext ctx ) {}
    @Override public void exitPostIncrementExpression_lf_postfixExpression( Java8Parser.PostIncrementExpression_lf_postfixExpressionContext ctx ) {}
    */

    /*
    postDecrementExpression
        :   postfixExpression '--'
        ;
    @Override public void enterPostDecrementExpression( Java8Parser.PostDecrementExpressionContext ctx ) {}
    */
    @Override public void exitPostDecrementExpression( Java8Parser.PostDecrementExpressionContext ctx )
    {
        // Cater for the simple case of a-- --> a-=1. Leave more complex cases for manual fix.
        rewriter.replace(ctx.stop, " -= 1");
    }

    /*
    postDecrementExpression_lf_postfixExpression
        :   '--'
        ;
    @Override public void enterPostDecrementExpression_lf_postfixExpression( Java8Parser.PostDecrementExpression_lf_postfixExpressionContext ctx ) {}
    @Override public void exitPostDecrementExpression_lf_postfixExpression( Java8Parser.PostDecrementExpression_lf_postfixExpressionContext ctx ) {}
    */

    /*
    castExpression
        :   '(' primitiveType ')' unaryExpression
        |   '(' referenceType additionalBound* ')' unaryExpressionNotPlusMinus
        |   '(' referenceType additionalBound* ')' lambdaExpression
        ;
    @Override public void enterCastExpression( Java8Parser.CastExpressionContext ctx ) {}
    */
    @Override public void exitCastExpression( Java8Parser.CastExpressionContext ctx )
    {
        TerminalNode tn = ctx.getToken(Java8Parser.RPAREN, 0);
        int indexRParenTok = tn.getSymbol().getTokenIndex();
        ParserRuleContext typeStartCtx = null, typeEndCtx = null, exprCtx = null;
        if (null != (typeStartCtx = ctx.primitiveType()))
        {
            exprCtx = ctx.unaryExpression();
            // If expression is already parenthesized, then these existing parentheses will do for enclosing the
            // expression as argument to making the case type. Just use a simple test, and leave cases like
            // (TypeC)(TypeB)a
            if (exprCtx.start.getType() != Java8Parser.LPAREN || exprCtx.stop.getType() != Java8Parser.RPAREN)
            {
                rewriter.insertBefore(exprCtx, "(");
                rewriter.insertAfter(exprCtx, ")");
            }
            rewriter.deleteAndAdjustWhitespace(ctx.start);
            rewriter.deleteAndAdjustWhitespace(tn);
        }
        else
        {
            for (ParserRuleContext c : ctx.getRuleContexts(ParserRuleContext.class))
            {
                if (null == typeStartCtx)
                    typeEndCtx = typeStartCtx = c;
                else if (c.start.getTokenIndex() < indexRParenTok)
                    typeEndCtx = c;
                else
                    exprCtx = c;
            }
            if (null == typeStartCtx || null == exprCtx)
                return;;
            Interval i = Interval.of(typeStartCtx.start.getTokenIndex(), typeEndCtx.stop.getTokenIndex());
            String typeText = rewriter.getText(i);
            rewriter.deleteAndAdjustWhitespace(ctx.start.getTokenIndex(), indexRParenTok);
            rewriter.insertAfter(exprCtx, " as! "+typeText);
        }
    }

}


