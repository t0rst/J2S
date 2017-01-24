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
import org.antlr.v4.runtime.tree.TerminalNode;
import java.util.*;


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

    public enum KeywordRestriction {
        not_restricted, not_in_function, not_in_type, not_in_variable;
        static int count() {return 4;}
    }

    private static HashMap<String, HashSet<String>> swiftKeywordSetsByRestrictionString = new HashMap<>();
    private static HashSet<String> swiftKeywords = new HashSet<>();
    private static ArrayList<HashSet<String>> swiftKeywordSetsByRestrictionOrdinal = new ArrayList<>(KeywordRestriction.count());
    static
    {
        String[] swiftKeywordRestrictions =
        {
            // f ==> cannot appear as function name
            // t ==> cannot appear as type name
            // v ==> cannot appear as variable name
            "Any",                      "ftv",
            "AnyObject",                "",
            "OSX",                      "",
            "OSXApplicationExtension",  "",
            "Protocol",                 "tv",
            "Type",                     "tv",
            "__COLUMN__",               "ftv",
            "__FILE__",                 "ftv",
            "__FUNCTION__",             "ftv",
            "__LINE__",                 "ftv",
            "arch",                     "",
            "arm",                      "",
            "arm64",                    "",
            "as",                       "ftv",
            "associatedtype",           "ftv",
            "associativity",            "",
            "break",                    "ftv",
            "case",                     "ftv",
            "catch",                    "ftv",
            "class",                    "ftv",
            "continue",                 "ftv",
            "convenience",              "",
            "default",                  "ftv",
            "defer",                    "ftv",
            "deinit",                   "ftv",
            "didSet",                   "",
            "do",                       "ftv",
            "dynamic",                  "",
            "dynamicType",              "",
            "else",                     "ftv",
            "enum",                     "ftv",
            "extension",                "ftv",
            "fallthrough",              "ftv",
            "false",                    "ftv",
            "fileprivate",              "ftv",
            "final",                    "",
            "for",                      "ftv",
            "func",                     "ftv",
            "get",                      "",
            "guard",                    "ftv",
            "i386",                     "",
            "iOS",                      "",
            "iOSApplicationExtension",  "",
            "if",                       "ftv",
            "import",                   "ftv",
            "in",                       "ftv",
            "indirect",                 "",
            "infix",                    "",
            "init",                     "ftv",
            "inout",                    "ftv",
            "internal",                 "ftv",
            "is",                       "ftv",
            "lazy",                     "",
            "left",                     "",
            "let",                      "ftv",
            "macOS",                    "",
            "mutating",                 "",
            "nil",                      "ftv",
            "none",                     "",
            "nonmutating",              "",
            "open",                     "",
            "operator",                 "ftv",
            "optional",                 "",
            "os",                       "",
            "override",                 "",
            "postfix",                  "",
            "precedence",               "",
            "prefix",                   "",
            "private",                  "ftv",
            "protocol",                 "ftv",
            "public",                   "ftv",
            "repeat",                   "ftv",
            "required",                 "",
            "rethrows",                 "ftv",
            "return",                   "ftv",
            "right",                    "",
            "safe",                     "",
            "self",                     "ftv",
            "set",                      "",
            "static",                   "ftv",
            "struct",                   "ftv",
            "subscript",                "ftv",
            "super",                    "ftv",
            "swift",                    "",
            "switch",                   "ftv",
            "throw",                    "ftv",
            "throws",                   "ftv",
            "true",                     "ftv",
            "try",                      "ftv",
            "tvOS",                     "",
            "typealias",                "ftv",
            "unowned",                  "",
            "unsafe",                   "t",
            "var",                      "ftv",
            "watchOS",                  "",
            "weak",                     "",
            "where",                    "ftv",
            "while",                    "ftv",
            "willSet",                  "",
            "x86_64",                   "",
        /*  Java keywords, for comparison:
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
            "default", "do", "double", "else", "enum", "extends", "false", "final", "finally", "float", "for", "goto",
            "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "null", "package",
            "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized",
            "this", "throw", "throws", "transient", "true", "try", "void", "volatile", "while"
        */
        };
        HashSet<String> keywords;
        swiftKeywordSetsByRestrictionString.put("all", swiftKeywords);
        for (int i = 0, n = swiftKeywordRestrictions.length & ~1; i < n; i += 2)
        {
            String keyword = swiftKeywordRestrictions[i];
            swiftKeywords.add(keyword);
            String restrictions = swiftKeywordRestrictions[i+1];
            int j = restrictions.length();
            do // once for "" (=unrestricted) if restrictions empty, else once for each single char
            {
                String restriction = j > 0 ? restrictions.substring(j-1, j) : "";
                if (null == (keywords = swiftKeywordSetsByRestrictionString.get(restriction)))
                    swiftKeywordSetsByRestrictionString.put(restriction, keywords = new HashSet<>());
                keywords.add(keyword);
            }
            while (--j > 0);
        }
        for (int i = 0; i < KeywordRestriction.count(); i++)
            swiftKeywordSetsByRestrictionOrdinal.add(null);
        swiftKeywordSetsByRestrictionOrdinal.set(KeywordRestriction.not_restricted.ordinal(), swiftKeywordSetsByRestrictionString.get(""));
        swiftKeywordSetsByRestrictionOrdinal.set(KeywordRestriction.not_in_function.ordinal(), swiftKeywordSetsByRestrictionString.get("f"));
        swiftKeywordSetsByRestrictionOrdinal.set(KeywordRestriction.not_in_type.ordinal(), swiftKeywordSetsByRestrictionString.get("t"));
        swiftKeywordSetsByRestrictionOrdinal.set(KeywordRestriction.not_in_variable.ordinal(), swiftKeywordSetsByRestrictionString.get("v"));
    }

    public enum IdentifierContext {
        other,
        function_name,
        type_name, // class, enum, interface
        variable_name;
    }

    static public String replacementForIdentifier(TerminalNode node)
    {
        String identifier = node.getText();
        if (!swiftKeywords.contains(identifier))
            return null;
        IdentifierContext usedFor = IdentifierContext.other;
        outer: for (RuleContext rc = (RuleContext)node.getParent(); null != rc; rc = rc.getParent())
        {
            switch (rc.getRuleIndex())
            {
                case Java8Parser.RULE_typeVariable:
                case Java8Parser.RULE_unannTypeVariable:
                case Java8Parser.RULE_typeParameter:
                case Java8Parser.RULE_expressionName:
                case Java8Parser.RULE_variableDeclaratorId:
                case Java8Parser.RULE_receiverParameter:
                case Java8Parser.RULE_enumConstant:
                case Java8Parser.RULE_enumConstantName:
                case Java8Parser.RULE_labeledStatement:
                case Java8Parser.RULE_labeledStatementNoShortIf:
                case Java8Parser.RULE_breakStatement:
                case Java8Parser.RULE_continueStatement:
                case Java8Parser.RULE_fieldAccess:
                case Java8Parser.RULE_fieldAccess_lf_primary:
                case Java8Parser.RULE_fieldAccess_lfno_primary:
                case Java8Parser.RULE_lambdaParameters:
                case Java8Parser.RULE_inferredFormalParameterList:
                    usedFor = IdentifierContext.variable_name;
                    break outer;

                case Java8Parser.RULE_packageOrTypeName:
                case Java8Parser.RULE_typeName:
                case Java8Parser.RULE_simpleTypeName:
                case Java8Parser.RULE_ambiguousName:
                case Java8Parser.RULE_enumDeclaration:
                case Java8Parser.RULE_normalClassDeclaration:
                case Java8Parser.RULE_normalInterfaceDeclaration:
                case Java8Parser.RULE_classType:
                case Java8Parser.RULE_classType_lf_classOrInterfaceType:
                case Java8Parser.RULE_classType_lfno_classOrInterfaceType:
                case Java8Parser.RULE_classInstanceCreationExpression:
                case Java8Parser.RULE_classInstanceCreationExpression_lf_primary:
                case Java8Parser.RULE_classInstanceCreationExpression_lfno_primary:
                case Java8Parser.RULE_unannClassType:
                case Java8Parser.RULE_unannClassType_lf_unannClassOrInterfaceType:
                case Java8Parser.RULE_unannClassType_lfno_unannClassOrInterfaceType:
                    usedFor = IdentifierContext.type_name;
                    break outer;

                case Java8Parser.RULE_methodName:
                case Java8Parser.RULE_methodDeclarator:
                case Java8Parser.RULE_methodInvocation:
                case Java8Parser.RULE_methodInvocation_lf_primary:
                case Java8Parser.RULE_methodInvocation_lfno_primary:
                case Java8Parser.RULE_methodReference:
                case Java8Parser.RULE_methodReference_lf_primary:
                case Java8Parser.RULE_methodReference_lfno_primary:
                    usedFor = IdentifierContext.function_name;
                    break outer;

                case Java8Parser.RULE_typeImportOnDemandDeclaration:
                case Java8Parser.RULE_singleStaticImportDeclaration:
                case Java8Parser.RULE_packageDeclaration:
                case Java8Parser.RULE_packageName:
                case Java8Parser.RULE_annotationTypeDeclaration:
                case Java8Parser.RULE_annotationTypeElementDeclaration:
                case Java8Parser.RULE_elementValuePair:
                    return null; // package names need manual attention - leave untouched

                case Java8Parser.RULE_block:
                case Java8Parser.RULE_switchBlock:
                case Java8Parser.RULE_classBody:
                case Java8Parser.RULE_constructorBody:
                case Java8Parser.RULE_enumBody:
                case Java8Parser.RULE_interfaceBody:
                case Java8Parser.RULE_annotationTypeBody:
                case Java8Parser.RULE_elementValueArrayInitializer:
                case Java8Parser.RULE_arrayInitializer:
                    return null; // stepping out of a scope without knowing what type of identifier -> ignore
            }
        }
        identifier = replacementForIdentifierInContext(identifier, usedFor);
        return identifier;
    }

    static public String replacementForIdentifierInContext(String identifier, IdentifierContext usedFor)
    {
        if (null == identifier)
            return null;
        KeywordRestriction kr = KeywordRestriction.not_restricted;
        switch (usedFor)
        {
            case function_name: kr = KeywordRestriction.not_in_function;    break;
            case type_name:     kr = KeywordRestriction.not_in_type;        break;
            case variable_name: kr = KeywordRestriction.not_in_variable;    break;
        }
        HashSet<String> keywords = swiftKeywordSetsByRestrictionOrdinal.get(kr.ordinal());
        if (null != keywords && keywords.contains(identifier))
        {
            identifier = "`" + identifier + "`";
            return identifier;
        }
        return null;
    }
}
