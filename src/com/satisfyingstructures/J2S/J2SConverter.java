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

import com.satisfyingstructures.J2S.antlr.Java8BaseListener;

import org.antlr.v4.runtime.*;

public class J2SConverter extends Java8BaseListener {

    // ---------------------------------------------------------------------------------------------
    // Setup.
    protected final TokenStreamRewriter rewriter;
    
    public J2SConverter(TokenStreamRewriter rewriter) {
        this.rewriter = rewriter;
    }

    // ---------------------------------------------------------------------------------------------
    // Helpers.


    // ---------------------------------------------------------------------------------------------
    // Listener members. Uncomment when we need an implemetation. Leave commented out when unused so 
    // that class member function list only contains salient functions.
    //

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
    @Override public void enterLiteral(Java8Parser.LiteralContext ctx) {}
    @Override public void exitLiteral(Java8Parser.LiteralContext ctx) {}
    */

    // ---------------------------------------------------------------------------------------------
    // Types, Values, and Variables (§4)

    /*
    type
        :   primitiveType
        |   referenceType
        ;
    @Override public void enterType(Java8Parser.TypeContext ctx) {}
    @Override public void exitType(Java8Parser.TypeContext ctx) {}
    */

    /*
    primitiveType
        :   annotation* numericType
        |   annotation* 'boolean'
        ;
    @Override public void enterPrimitiveType(Java8Parser.PrimitiveTypeContext ctx) {}
    @Override public void exitPrimitiveType(Java8Parser.PrimitiveTypeContext ctx) {}
    */

    /*
    numericType
        :   integralType
        |   floatingPointType
        ;
    @Override public void enterNumericType(Java8Parser.NumericTypeContext ctx) {}
    @Override public void exitNumericType(Java8Parser.NumericTypeContext ctx) {}
    */

    /*
    integralType
        :   'byte'
        |   'short'
        |   'int'
        |   'long'
        |   'char'
        ;
    @Override public void enterIntegralType(Java8Parser.IntegralTypeContext ctx) {}
    @Override public void exitIntegralType(Java8Parser.IntegralTypeContext ctx) {}
    */

    /*
    floatingPointType
        :   'float'
        |   'double'
        ;
    @Override public void enterFloatingPointType(Java8Parser.FloatingPointTypeContext ctx) {}
    @Override public void exitFloatingPointType(Java8Parser.FloatingPointTypeContext ctx) {}
    */

    /*
    referenceType
        :   classOrInterfaceType
        |   typeVariable
        |   arrayType
        ;
    @Override public void enterReferenceType(Java8Parser.ReferenceTypeContext ctx) {}
    @Override public void exitReferenceType(Java8Parser.ReferenceTypeContext ctx) {}
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
    @Override public void enterClassOrInterfaceType(Java8Parser.ClassOrInterfaceTypeContext ctx) {}
    @Override public void exitClassOrInterfaceType(Java8Parser.ClassOrInterfaceTypeContext ctx) {}
    */

    /*
    classType
        :   annotation* Identifier typeArguments?
        |   classOrInterfaceType '.' annotation* Identifier typeArguments?
        ;
    @Override public void enterClassType(Java8Parser.ClassTypeContext ctx) {}
    @Override public void exitClassType(Java8Parser.ClassTypeContext ctx) {}
    */

    /*
    classType_lf_classOrInterfaceType
        :   '.' annotation* Identifier typeArguments?
        ;
    @Override public void enterClassType_lf_classOrInterfaceType(Java8Parser.ClassType_lf_classOrInterfaceTypeContext ctx) {}
    @Override public void exitClassType_lf_classOrInterfaceType(Java8Parser.ClassType_lf_classOrInterfaceTypeContext ctx) {}
    */

    /*
    classType_lfno_classOrInterfaceType
        :   annotation* Identifier typeArguments?
        ;
    @Override public void enterClassType_lfno_classOrInterfaceType(Java8Parser.ClassType_lfno_classOrInterfaceTypeContext ctx) {}
    @Override public void exitClassType_lfno_classOrInterfaceType(Java8Parser.ClassType_lfno_classOrInterfaceTypeContext ctx) {}
    */

    /*
    interfaceType
        :   classType
        ;
    @Override public void enterInterfaceType(Java8Parser.InterfaceTypeContext ctx) {}
    @Override public void exitInterfaceType(Java8Parser.InterfaceTypeContext ctx) {}
    */

    /*
    interfaceType_lf_classOrInterfaceType
        :   classType_lf_classOrInterfaceType
        ;
    @Override public void enterInterfaceType_lf_classOrInterfaceType(Java8Parser.InterfaceType_lf_classOrInterfaceTypeContext ctx) {}
    @Override public void exitInterfaceType_lf_classOrInterfaceType(Java8Parser.InterfaceType_lf_classOrInterfaceTypeContext ctx) {}
    */

    /*
    interfaceType_lfno_classOrInterfaceType
        :   classType_lfno_classOrInterfaceType
        ;
    @Override public void enterInterfaceType_lfno_classOrInterfaceType(Java8Parser.InterfaceType_lfno_classOrInterfaceTypeContext ctx) {}
    @Override public void exitInterfaceType_lfno_classOrInterfaceType(Java8Parser.InterfaceType_lfno_classOrInterfaceTypeContext ctx) {}
    */

    /*
    typeVariable
        :   annotation* Identifier
        ;
    @Override public void enterTypeVariable(Java8Parser.TypeVariableContext ctx) {}
    @Override public void exitTypeVariable(Java8Parser.TypeVariableContext ctx) {}
    */

    /*
    arrayType
        :   primitiveType dims
        |   classOrInterfaceType dims
        |   typeVariable dims
        ;
    @Override public void enterArrayType(Java8Parser.ArrayTypeContext ctx) {}
    @Override public void exitArrayType(Java8Parser.ArrayTypeContext ctx) {}
    */

    /*
    dims
        :   annotation* '[' ']' (annotation* '[' ']')*
        ;
    @Override public void enterDims(Java8Parser.DimsContext ctx) {}
    @Override public void exitDims(Java8Parser.DimsContext ctx) {}
    */

    /*
    typeParameter
        :   typeParameterModifier* Identifier typeBound?
        ;
    @Override public void enterTypeParameter(Java8Parser.TypeParameterContext ctx) {}
    @Override public void exitTypeParameter(Java8Parser.TypeParameterContext ctx) {}
    */

    /*
    typeParameterModifier
        :   annotation
        ;
    @Override public void enterTypeParameterModifier(Java8Parser.TypeParameterModifierContext ctx) {}
    @Override public void exitTypeParameterModifier(Java8Parser.TypeParameterModifierContext ctx) {}
    */

    /*
    typeBound
        :   'extends' typeVariable
        |   'extends' classOrInterfaceType additionalBound*
        ;
    @Override public void enterTypeBound(Java8Parser.TypeBoundContext ctx) {}
    @Override public void exitTypeBound(Java8Parser.TypeBoundContext ctx) {}
    */

    /*
    additionalBound
        :   '&' interfaceType
        ;
    @Override public void enterAdditionalBound(Java8Parser.AdditionalBoundContext ctx) {}
    @Override public void exitAdditionalBound(Java8Parser.AdditionalBoundContext ctx) {}
    */

    /*
    typeArguments
        :   '<' typeArgumentList '>'
        ;
    @Override public void enterTypeArguments(Java8Parser.TypeArgumentsContext ctx) {}
    @Override public void exitTypeArguments(Java8Parser.TypeArgumentsContext ctx) {}
    */

    /*
    typeArgumentList
        :   typeArgument (',' typeArgument)*
        ;
    @Override public void enterTypeArgumentList(Java8Parser.TypeArgumentListContext ctx) {}
    @Override public void exitTypeArgumentList(Java8Parser.TypeArgumentListContext ctx) {}
    */

    /*
    typeArgument
        :   referenceType
        |   wildcard
        ;
    @Override public void enterTypeArgument(Java8Parser.TypeArgumentContext ctx) {}
    @Override public void exitTypeArgument(Java8Parser.TypeArgumentContext ctx) {}
    */

    /*
    wildcard
        :   annotation* '?' wildcardBounds?
        ;
    @Override public void enterWildcard(Java8Parser.WildcardContext ctx) {}
    @Override public void exitWildcard(Java8Parser.WildcardContext ctx) {}
    */

    /*
    wildcardBounds
        :   'extends' referenceType
        |   'super' referenceType
        ;
    @Override public void enterWildcardBounds(Java8Parser.WildcardBoundsContext ctx) {}
    @Override public void exitWildcardBounds(Java8Parser.WildcardBoundsContext ctx) {}
    */

    // ---------------------------------------------------------------------------------------------
    // Names (§6)

    /*
    packageName
        :   Identifier
        |   packageName '.' Identifier
        ;
    @Override public void enterPackageName(Java8Parser.PackageNameContext ctx) {}
    @Override public void exitPackageName(Java8Parser.PackageNameContext ctx) {}
    */

    /*
    typeName
        :   Identifier
        |   packageOrTypeName '.' Identifier
        ;
    @Override public void enterTypeName(Java8Parser.TypeNameContext ctx) {}
    @Override public void exitTypeName(Java8Parser.TypeNameContext ctx) {}
    */

    /*
    packageOrTypeName
        :   Identifier
        |   packageOrTypeName '.' Identifier
        ;
    @Override public void enterPackageOrTypeName(Java8Parser.PackageOrTypeNameContext ctx) {}
    @Override public void exitPackageOrTypeName(Java8Parser.PackageOrTypeNameContext ctx) {}
    */

    /*
    expressionName
        :   Identifier
        |   ambiguousName '.' Identifier
        ;
    @Override public void enterExpressionName(Java8Parser.ExpressionNameContext ctx) {}
    @Override public void exitExpressionName(Java8Parser.ExpressionNameContext ctx) {}
    */

    /*
    methodName
        :   Identifier
        ;
    @Override public void enterMethodName(Java8Parser.MethodNameContext ctx) {}
    @Override public void exitMethodName(Java8Parser.MethodNameContext ctx) {}
    */

    /*
    ambiguousName
        :   Identifier
        |   ambiguousName '.' Identifier
        ;
    @Override public void enterAmbiguousName(Java8Parser.AmbiguousNameContext ctx) {}
    @Override public void exitAmbiguousName(Java8Parser.AmbiguousNameContext ctx) {}
    */

    // ---------------------------------------------------------------------------------------------
    // Packages (§7)

    /*
    compilationUnit
        :   packageDeclaration? importDeclaration* typeDeclaration* EOF
        ;
    @Override public void enterCompilationUnit(Java8Parser.CompilationUnitContext ctx) {}
    @Override public void exitCompilationUnit(Java8Parser.CompilationUnitContext ctx) {}
    */

    /*
    packageDeclaration
        :   packageModifier* 'package' Identifier ('.' Identifier)* ';'
        ;
    @Override public void enterPackageDeclaration(Java8Parser.PackageDeclarationContext ctx) {}
    @Override public void exitPackageDeclaration(Java8Parser.PackageDeclarationContext ctx) {}
    */

    /*
    packageModifier
        :   annotation
        ;
    @Override public void enterPackageModifier(Java8Parser.PackageModifierContext ctx) {}
    @Override public void exitPackageModifier(Java8Parser.PackageModifierContext ctx) {}
    */

    /*
    importDeclaration
        :   singleTypeImportDeclaration
        |   typeImportOnDemandDeclaration
        |   singleStaticImportDeclaration
        |   staticImportOnDemandDeclaration
        ;
    @Override public void enterImportDeclaration(Java8Parser.ImportDeclarationContext ctx) {}
    @Override public void exitImportDeclaration(Java8Parser.ImportDeclarationContext ctx) {}
    */

    /*
    singleTypeImportDeclaration
        :   'import' typeName ';'
        ;
    @Override public void enterSingleTypeImportDeclaration(Java8Parser.SingleTypeImportDeclarationContext ctx) {}
    @Override public void exitSingleTypeImportDeclaration(Java8Parser.SingleTypeImportDeclarationContext ctx) {}
    */

    /*
    typeImportOnDemandDeclaration
        :   'import' packageOrTypeName '.' '*' ';'
        ;
    @Override public void enterTypeImportOnDemandDeclaration(Java8Parser.TypeImportOnDemandDeclarationContext ctx) {}
    @Override public void exitTypeImportOnDemandDeclaration(Java8Parser.TypeImportOnDemandDeclarationContext ctx) {}
    */

    /*
    singleStaticImportDeclaration
        :   'import' 'static' typeName '.' Identifier ';'
        ;
    @Override public void enterSingleStaticImportDeclaration(Java8Parser.SingleStaticImportDeclarationContext ctx) {}
    @Override public void exitSingleStaticImportDeclaration(Java8Parser.SingleStaticImportDeclarationContext ctx) {}
    */

    /*
    staticImportOnDemandDeclaration
        :   'import' 'static' typeName '.' '*' ';'
        ;
    @Override public void enterStaticImportOnDemandDeclaration(Java8Parser.StaticImportOnDemandDeclarationContext ctx) {}
    @Override public void exitStaticImportOnDemandDeclaration(Java8Parser.StaticImportOnDemandDeclarationContext ctx) {}
    */

    /*
    typeDeclaration
        :   classDeclaration
        |   interfaceDeclaration
        |   ';'
        ;
    @Override public void enterTypeDeclaration(Java8Parser.TypeDeclarationContext ctx) {}
    @Override public void exitTypeDeclaration(Java8Parser.TypeDeclarationContext ctx) {}
    */

    // ---------------------------------------------------------------------------------------------
    // Classes (§8)

    /*
    classDeclaration
        :   normalClassDeclaration
        |   enumDeclaration
        ;
    @Override public void enterClassDeclaration(Java8Parser.ClassDeclarationContext ctx) {}
    @Override public void exitClassDeclaration(Java8Parser.ClassDeclarationContext ctx) {}
    */

    /*
    normalClassDeclaration
        :   classModifier* 'class' Identifier typeParameters? superclass? superinterfaces? classBody
        ;
    @Override public void enterNormalClassDeclaration(Java8Parser.NormalClassDeclarationContext ctx) {}
    @Override public void exitNormalClassDeclaration(Java8Parser.NormalClassDeclarationContext ctx) {}
    */

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
    @Override public void enterClassModifier(Java8Parser.ClassModifierContext ctx) {}
    @Override public void exitClassModifier(Java8Parser.ClassModifierContext ctx) {}
    */

    /*
    typeParameters
        :   '<' typeParameterList '>'
        ;
    @Override public void enterTypeParameters(Java8Parser.TypeParametersContext ctx) {}
    @Override public void exitTypeParameters(Java8Parser.TypeParametersContext ctx) {}
    */

    /*
    typeParameterList
        :   typeParameter (',' typeParameter)*
        ;
    @Override public void enterTypeParameterList(Java8Parser.TypeParameterListContext ctx) {}
    @Override public void exitTypeParameterList(Java8Parser.TypeParameterListContext ctx) {}
    */

    /*
    superclass
        :   'extends' classType
        ;
    @Override public void enterSuperclass(Java8Parser.SuperclassContext ctx) {}
    @Override public void exitSuperclass(Java8Parser.SuperclassContext ctx) {}
    */

    /*
    superinterfaces
        :   'implements' interfaceTypeList
        ;
    @Override public void enterSuperinterfaces(Java8Parser.SuperinterfacesContext ctx) {}
    @Override public void exitSuperinterfaces(Java8Parser.SuperinterfacesContext ctx) {}
    */

    /*
    interfaceTypeList
        :   interfaceType (',' interfaceType)*
        ;
    @Override public void enterInterfaceTypeList(Java8Parser.InterfaceTypeListContext ctx) {}
    @Override public void exitInterfaceTypeList(Java8Parser.InterfaceTypeListContext ctx) {}
    */

    /*
    classBody
        :   '{' classBodyDeclaration* '}'
        ;
    @Override public void enterClassBody(Java8Parser.ClassBodyContext ctx) {}
    @Override public void exitClassBody(Java8Parser.ClassBodyContext ctx) {}
    */

    /*
    classBodyDeclaration
        :   classMemberDeclaration
        |   instanceInitializer
        |   staticInitializer
        |   constructorDeclaration
        ;
    @Override public void enterClassBodyDeclaration(Java8Parser.ClassBodyDeclarationContext ctx) {}
    @Override public void exitClassBodyDeclaration(Java8Parser.ClassBodyDeclarationContext ctx) {}
    */

    /*
    classMemberDeclaration
        :   fieldDeclaration
        |   methodDeclaration
        |   classDeclaration
        |   interfaceDeclaration
        |   ';'
        ;
    @Override public void enterClassMemberDeclaration(Java8Parser.ClassMemberDeclarationContext ctx) {}
    @Override public void exitClassMemberDeclaration(Java8Parser.ClassMemberDeclarationContext ctx) {}
    */

    /*
    fieldDeclaration
        :   fieldModifier* unannType variableDeclaratorList ';'
        ;
    @Override public void enterFieldDeclaration(Java8Parser.FieldDeclarationContext ctx) {}
    @Override public void exitFieldDeclaration(Java8Parser.FieldDeclarationContext ctx) {}
    */

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
    @Override public void enterFieldModifier(Java8Parser.FieldModifierContext ctx) {}
    @Override public void exitFieldModifier(Java8Parser.FieldModifierContext ctx) {}
    */

    /*
    variableDeclaratorList
        :   variableDeclarator (',' variableDeclarator)*
        ;
    @Override public void enterVariableDeclaratorList(Java8Parser.VariableDeclaratorListContext ctx) {}
    @Override public void exitVariableDeclaratorList(Java8Parser.VariableDeclaratorListContext ctx) {}
    */

    /*
    variableDeclarator
        :   variableDeclaratorId ('=' variableInitializer)?
        ;
    @Override public void enterVariableDeclarator(Java8Parser.VariableDeclaratorContext ctx) {}
    @Override public void exitVariableDeclarator(Java8Parser.VariableDeclaratorContext ctx) {}
    */

    /*
    variableDeclaratorId
        :   Identifier dims?
        ;
    @Override public void enterVariableDeclaratorId(Java8Parser.VariableDeclaratorIdContext ctx) {}
    @Override public void exitVariableDeclaratorId(Java8Parser.VariableDeclaratorIdContext ctx) {}
    */

    /*
    variableInitializer
        :   expression
        |   arrayInitializer
        ;
    @Override public void enterVariableInitializer(Java8Parser.VariableInitializerContext ctx) {}
    @Override public void exitVariableInitializer(Java8Parser.VariableInitializerContext ctx) {}
    */

    /*
    unannType
        :   unannPrimitiveType
        |   unannReferenceType
        ;
    @Override public void enterUnannType(Java8Parser.UnannTypeContext ctx) {}
    @Override public void exitUnannType(Java8Parser.UnannTypeContext ctx) {}
    */

    /*
    unannPrimitiveType
        :   numericType
        |   'boolean'
        ;
    @Override public void enterUnannPrimitiveType(Java8Parser.UnannPrimitiveTypeContext ctx) {}
    @Override public void exitUnannPrimitiveType(Java8Parser.UnannPrimitiveTypeContext ctx) {}
    */

    /*
    unannReferenceType
        :   unannClassOrInterfaceType
        |   unannTypeVariable
        |   unannArrayType
        ;
    @Override public void enterUnannReferenceType(Java8Parser.UnannReferenceTypeContext ctx) {}
    @Override public void exitUnannReferenceType(Java8Parser.UnannReferenceTypeContext ctx) {}
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
    @Override public void enterUnannClassOrInterfaceType(Java8Parser.UnannClassOrInterfaceTypeContext ctx) {}
    @Override public void exitUnannClassOrInterfaceType(Java8Parser.UnannClassOrInterfaceTypeContext ctx) {}
    */

    /*
    unannClassType
        :   Identifier typeArguments?
        |   unannClassOrInterfaceType '.' annotation* Identifier typeArguments?
        ;
    @Override public void enterUnannClassType(Java8Parser.UnannClassTypeContext ctx) {}
    @Override public void exitUnannClassType(Java8Parser.UnannClassTypeContext ctx) {}
    */

    /*
    unannClassType_lf_unannClassOrInterfaceType
        :   '.' annotation* Identifier typeArguments?
        ;
    @Override public void enterUnannClassType_lf_unannClassOrInterfaceType(Java8Parser.UnannClassType_lf_unannClassOrInterfaceTypeContext ctx) {}
    @Override public void exitUnannClassType_lf_unannClassOrInterfaceType(Java8Parser.UnannClassType_lf_unannClassOrInterfaceTypeContext ctx) {}
    */

    /*
    unannClassType_lfno_unannClassOrInterfaceType
        :   Identifier typeArguments?
        ;
    @Override public void enterUnannClassType_lfno_unannClassOrInterfaceType(Java8Parser.UnannClassType_lfno_unannClassOrInterfaceTypeContext ctx) {}
    @Override public void exitUnannClassType_lfno_unannClassOrInterfaceType(Java8Parser.UnannClassType_lfno_unannClassOrInterfaceTypeContext ctx) {}
    */

    /*
    unannInterfaceType
        :   unannClassType
        ;
    @Override public void enterUnannInterfaceType(Java8Parser.UnannInterfaceTypeContext ctx) {}
    @Override public void exitUnannInterfaceType(Java8Parser.UnannInterfaceTypeContext ctx) {}
    */

    /*
    unannInterfaceType_lf_unannClassOrInterfaceType
        :   unannClassType_lf_unannClassOrInterfaceType
        ;
    @Override public void enterUnannInterfaceType_lf_unannClassOrInterfaceType(Java8Parser.UnannInterfaceType_lf_unannClassOrInterfaceTypeContext ctx) {}
    @Override public void exitUnannInterfaceType_lf_unannClassOrInterfaceType(Java8Parser.UnannInterfaceType_lf_unannClassOrInterfaceTypeContext ctx) {}
    */

    /*
    unannInterfaceType_lfno_unannClassOrInterfaceType
        :   unannClassType_lfno_unannClassOrInterfaceType
        ;
    @Override public void enterUnannInterfaceType_lfno_unannClassOrInterfaceType(Java8Parser.UnannInterfaceType_lfno_unannClassOrInterfaceTypeContext ctx) {}
    @Override public void exitUnannInterfaceType_lfno_unannClassOrInterfaceType(Java8Parser.UnannInterfaceType_lfno_unannClassOrInterfaceTypeContext ctx) {}
    */

    /*
    unannTypeVariable
        :   Identifier
        ;
    @Override public void enterUnannTypeVariable(Java8Parser.UnannTypeVariableContext ctx) {}
    @Override public void exitUnannTypeVariable(Java8Parser.UnannTypeVariableContext ctx) {}
    */

    /*
    unannArrayType
        :   unannPrimitiveType dims
        |   unannClassOrInterfaceType dims
        |   unannTypeVariable dims
        ;
    @Override public void enterUnannArrayType(Java8Parser.UnannArrayTypeContext ctx) {}
    @Override public void exitUnannArrayType(Java8Parser.UnannArrayTypeContext ctx) {}
    */

    /*
    methodDeclaration
        :   methodModifier* methodHeader methodBody
        ;
    @Override public void enterMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {}
    @Override public void exitMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {}
    */

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
    @Override public void enterMethodModifier(Java8Parser.MethodModifierContext ctx) {}
    @Override public void exitMethodModifier(Java8Parser.MethodModifierContext ctx) {}
    */

    /*
    methodHeader
        :   result methodDeclarator throws_?
        |   typeParameters annotation* result methodDeclarator throws_?
        ;
    @Override public void enterMethodHeader(Java8Parser.MethodHeaderContext ctx) {}
    @Override public void exitMethodHeader(Java8Parser.MethodHeaderContext ctx) {}
    */

    /*
    result
        :   unannType
        |   'void'
        ;
    @Override public void enterResult(Java8Parser.ResultContext ctx) {}
    @Override public void exitResult(Java8Parser.ResultContext ctx) {}
    */

    /*
    methodDeclarator
        :   Identifier '(' formalParameterList? ')' dims?
        ;
    @Override public void enterMethodDeclarator(Java8Parser.MethodDeclaratorContext ctx) {}
    @Override public void exitMethodDeclarator(Java8Parser.MethodDeclaratorContext ctx) {}
    */

    /*
    formalParameterList
        :   formalParameters ',' lastFormalParameter
        |   lastFormalParameter
        ;
    @Override public void enterFormalParameterList(Java8Parser.FormalParameterListContext ctx) {}
    @Override public void exitFormalParameterList(Java8Parser.FormalParameterListContext ctx) {}
    */

    /*
    formalParameters
        :   formalParameter (',' formalParameter)*
        |   receiverParameter (',' formalParameter)*
        ;
    @Override public void enterFormalParameters(Java8Parser.FormalParametersContext ctx) {}
    @Override public void exitFormalParameters(Java8Parser.FormalParametersContext ctx) {}
    */

    /*
    formalParameter
        :   variableModifier* unannType variableDeclaratorId
        ;
    @Override public void enterFormalParameter(Java8Parser.FormalParameterContext ctx) {}
    @Override public void exitFormalParameter(Java8Parser.FormalParameterContext ctx) {}
    */

    /*
    variableModifier
        :   annotation
        |   'final'
        ;
    @Override public void enterVariableModifier(Java8Parser.VariableModifierContext ctx) {}
    @Override public void exitVariableModifier(Java8Parser.VariableModifierContext ctx) {}
    */

    /*
    lastFormalParameter
        :   variableModifier* unannType annotation* '...' variableDeclaratorId
        |   formalParameter
        ;
    @Override public void enterLastFormalParameter(Java8Parser.LastFormalParameterContext ctx) {}
    @Override public void exitLastFormalParameter(Java8Parser.LastFormalParameterContext ctx) {}
    */

    /*
    receiverParameter
        :   annotation* unannType (Identifier '.')? 'this'
        ;
    @Override public void enterReceiverParameter(Java8Parser.ReceiverParameterContext ctx) {}
    @Override public void exitReceiverParameter(Java8Parser.ReceiverParameterContext ctx) {}
    */

    /*
    throws_
        :   'throws' exceptionTypeList
        ;
    @Override public void enterThrows_(Java8Parser.Throws_Context ctx) {}
    @Override public void exitThrows_(Java8Parser.Throws_Context ctx) {}
    */

    /*
    exceptionTypeList
        :   exceptionType (',' exceptionType)*
        ;
    @Override public void enterExceptionTypeList(Java8Parser.ExceptionTypeListContext ctx) {}
    @Override public void exitExceptionTypeList(Java8Parser.ExceptionTypeListContext ctx) {}
    */

    /*
    exceptionType
        :   classType
        |   typeVariable
        ;
    @Override public void enterExceptionType(Java8Parser.ExceptionTypeContext ctx) {}
    @Override public void exitExceptionType(Java8Parser.ExceptionTypeContext ctx) {}
    */

    /*
    methodBody
        :   block
        |   ';'
        ;
    @Override public void enterMethodBody(Java8Parser.MethodBodyContext ctx) {}
    @Override public void exitMethodBody(Java8Parser.MethodBodyContext ctx) {}
    */

    /*
    instanceInitializer
        :   block
        ;
    @Override public void enterInstanceInitializer(Java8Parser.InstanceInitializerContext ctx) {}
    @Override public void exitInstanceInitializer(Java8Parser.InstanceInitializerContext ctx) {}
    */

    /*
    staticInitializer
        :   'static' block
        ;
    @Override public void enterStaticInitializer(Java8Parser.StaticInitializerContext ctx) {}
    @Override public void exitStaticInitializer(Java8Parser.StaticInitializerContext ctx) {}
    */

    /*
    constructorDeclaration
        :   constructorModifier* constructorDeclarator throws_? constructorBody
        ;
    @Override public void enterConstructorDeclaration(Java8Parser.ConstructorDeclarationContext ctx) {}
    @Override public void exitConstructorDeclaration(Java8Parser.ConstructorDeclarationContext ctx) {}
    */

    /*
    constructorModifier
        :   annotation
        |   'public'
        |   'protected'
        |   'private'
        ;
    @Override public void enterConstructorModifier(Java8Parser.ConstructorModifierContext ctx) {}
    @Override public void exitConstructorModifier(Java8Parser.ConstructorModifierContext ctx) {}
    */

    /*
    constructorDeclarator
        :   typeParameters? simpleTypeName '(' formalParameterList? ')'
        ;
    @Override public void enterConstructorDeclarator(Java8Parser.ConstructorDeclaratorContext ctx) {}
    @Override public void exitConstructorDeclarator(Java8Parser.ConstructorDeclaratorContext ctx) {}
    */

    /*
    simpleTypeName
        :   Identifier
        ;
    @Override public void enterSimpleTypeName(Java8Parser.SimpleTypeNameContext ctx) {}
    @Override public void exitSimpleTypeName(Java8Parser.SimpleTypeNameContext ctx) {}
    */

    /*
    constructorBody
        :   '{' explicitConstructorInvocation? blockStatements? '}'
        ;
    @Override public void enterConstructorBody(Java8Parser.ConstructorBodyContext ctx) {}
    @Override public void exitConstructorBody(Java8Parser.ConstructorBodyContext ctx) {}
    */

    /*
    explicitConstructorInvocation
        :   typeArguments? 'this' '(' argumentList? ')' ';'
        |   typeArguments? 'super' '(' argumentList? ')' ';'
        |   expressionName '.' typeArguments? 'super' '(' argumentList? ')' ';'
        |   primary '.' typeArguments? 'super' '(' argumentList? ')' ';'
        ;
    @Override public void enterExplicitConstructorInvocation(Java8Parser.ExplicitConstructorInvocationContext ctx) {}
    @Override public void exitExplicitConstructorInvocation(Java8Parser.ExplicitConstructorInvocationContext ctx) {}
    */

    /*
    enumDeclaration
        :   classModifier* 'enum' Identifier superinterfaces? enumBody
        ;
    @Override public void enterEnumDeclaration(Java8Parser.EnumDeclarationContext ctx) {}
    @Override public void exitEnumDeclaration(Java8Parser.EnumDeclarationContext ctx) {}
    */

    /*
    enumBody
        :   '{' enumConstantList? ','? enumBodyDeclarations? '}'
        ;
    @Override public void enterEnumBody(Java8Parser.EnumBodyContext ctx) {}
    @Override public void exitEnumBody(Java8Parser.EnumBodyContext ctx) {}
    */

    /*
    enumConstantList
        :   enumConstant (',' enumConstant)*
        ;
    @Override public void enterEnumConstantList(Java8Parser.EnumConstantListContext ctx) {}
    @Override public void exitEnumConstantList(Java8Parser.EnumConstantListContext ctx) {}
    */

    /*
    enumConstant
        :   enumConstantModifier* Identifier ('(' argumentList? ')')? classBody?
        ;
    @Override public void enterEnumConstant(Java8Parser.EnumConstantContext ctx) {}
    @Override public void exitEnumConstant(Java8Parser.EnumConstantContext ctx) {}
    */

    /*
    enumConstantModifier
        :   annotation
        ;
    @Override public void enterEnumConstantModifier(Java8Parser.EnumConstantModifierContext ctx) {}
    @Override public void exitEnumConstantModifier(Java8Parser.EnumConstantModifierContext ctx) {}
    */

    /*
    enumBodyDeclarations
        :   ';' classBodyDeclaration*
        ;
    @Override public void enterEnumBodyDeclarations(Java8Parser.EnumBodyDeclarationsContext ctx) {}
    @Override public void exitEnumBodyDeclarations(Java8Parser.EnumBodyDeclarationsContext ctx) {}
    */

    // ---------------------------------------------------------------------------------------------
    // Interfaces (§9)

    /*
    interfaceDeclaration
        :   normalInterfaceDeclaration
        |   annotationTypeDeclaration
        ;
    @Override public void enterInterfaceDeclaration(Java8Parser.InterfaceDeclarationContext ctx) {}
    @Override public void exitInterfaceDeclaration(Java8Parser.InterfaceDeclarationContext ctx) {}
    */

    /*
    normalInterfaceDeclaration
        :   interfaceModifier* 'interface' Identifier typeParameters? extendsInterfaces? interfaceBody
        ;
    @Override public void enterNormalInterfaceDeclaration(Java8Parser.NormalInterfaceDeclarationContext ctx) {}
    @Override public void exitNormalInterfaceDeclaration(Java8Parser.NormalInterfaceDeclarationContext ctx) {}
    */

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
    @Override public void enterInterfaceModifier(Java8Parser.InterfaceModifierContext ctx) {}
    @Override public void exitInterfaceModifier(Java8Parser.InterfaceModifierContext ctx) {}
    */

    /*
    extendsInterfaces
        :   'extends' interfaceTypeList
        ;
    @Override public void enterExtendsInterfaces(Java8Parser.ExtendsInterfacesContext ctx) {}
    @Override public void exitExtendsInterfaces(Java8Parser.ExtendsInterfacesContext ctx) {}
    */

    /*
    interfaceBody
        :   '{' interfaceMemberDeclaration* '}'
        ;
    @Override public void enterInterfaceBody(Java8Parser.InterfaceBodyContext ctx) {}
    @Override public void exitInterfaceBody(Java8Parser.InterfaceBodyContext ctx) {}
    */

    /*
    interfaceMemberDeclaration
        :   constantDeclaration
        |   interfaceMethodDeclaration
        |   classDeclaration
        |   interfaceDeclaration
        |   ';'
        ;
    @Override public void enterInterfaceMemberDeclaration(Java8Parser.InterfaceMemberDeclarationContext ctx) {}
    @Override public void exitInterfaceMemberDeclaration(Java8Parser.InterfaceMemberDeclarationContext ctx) {}
    */

    /*
    constantDeclaration
        :   constantModifier* unannType variableDeclaratorList ';'
        ;
    @Override public void enterConstantDeclaration(Java8Parser.ConstantDeclarationContext ctx) {}
    @Override public void exitConstantDeclaration(Java8Parser.ConstantDeclarationContext ctx) {}
    */

    /*
    constantModifier
        :   annotation
        |   'public'
        |   'static'
        |   'final'
        ;
    @Override public void enterConstantModifier(Java8Parser.ConstantModifierContext ctx) {}
    @Override public void exitConstantModifier(Java8Parser.ConstantModifierContext ctx) {}
    */

    /*
    interfaceMethodDeclaration
        :   interfaceMethodModifier* methodHeader methodBody
        ;
    @Override public void enterInterfaceMethodDeclaration(Java8Parser.InterfaceMethodDeclarationContext ctx) {}
    @Override public void exitInterfaceMethodDeclaration(Java8Parser.InterfaceMethodDeclarationContext ctx) {}
    */

    /*
    interfaceMethodModifier
        :   annotation
        |   'public'
        |   'abstract'
        |   'default'
        |   'static'
        |   'strictfp'
        ;
    @Override public void enterInterfaceMethodModifier(Java8Parser.InterfaceMethodModifierContext ctx) {}
    @Override public void exitInterfaceMethodModifier(Java8Parser.InterfaceMethodModifierContext ctx) {}
    */

    /*
    annotationTypeDeclaration
        :   interfaceModifier* '@' 'interface' Identifier annotationTypeBody
        ;
    @Override public void enterAnnotationTypeDeclaration(Java8Parser.AnnotationTypeDeclarationContext ctx) {}
    @Override public void exitAnnotationTypeDeclaration(Java8Parser.AnnotationTypeDeclarationContext ctx) {}
    */

    /*
    annotationTypeBody
        :   '{' annotationTypeMemberDeclaration* '}'
        ;
    @Override public void enterAnnotationTypeBody(Java8Parser.AnnotationTypeBodyContext ctx) {}
    @Override public void exitAnnotationTypeBody(Java8Parser.AnnotationTypeBodyContext ctx) {}
    */

    /*
    annotationTypeMemberDeclaration
        :   annotationTypeElementDeclaration
        |   constantDeclaration
        |   classDeclaration
        |   interfaceDeclaration
        |   ';'
        ;
    @Override public void enterAnnotationTypeMemberDeclaration(Java8Parser.AnnotationTypeMemberDeclarationContext ctx) {}
    @Override public void exitAnnotationTypeMemberDeclaration(Java8Parser.AnnotationTypeMemberDeclarationContext ctx) {}
    */

    /*
    annotationTypeElementDeclaration
        :   annotationTypeElementModifier* unannType Identifier '(' ')' dims? defaultValue? ';'
        ;
    @Override public void enterAnnotationTypeElementDeclaration(Java8Parser.AnnotationTypeElementDeclarationContext ctx) {}
    @Override public void exitAnnotationTypeElementDeclaration(Java8Parser.AnnotationTypeElementDeclarationContext ctx) {}
    */

    /*
    annotationTypeElementModifier
        :   annotation
        |   'public'
        |   'abstract'
        ;
    @Override public void enterAnnotationTypeElementModifier(Java8Parser.AnnotationTypeElementModifierContext ctx) {}
    @Override public void exitAnnotationTypeElementModifier(Java8Parser.AnnotationTypeElementModifierContext ctx) {}
    */

    /*
    defaultValue
        :   'default' elementValue
        ;
    @Override public void enterDefaultValue(Java8Parser.DefaultValueContext ctx) {}
    @Override public void exitDefaultValue(Java8Parser.DefaultValueContext ctx) {}
    */

    /*
    annotation
        :   normalAnnotation
        |   markerAnnotation
        |   singleElementAnnotation
        ;
    @Override public void enterAnnotation(Java8Parser.AnnotationContext ctx) {}
    @Override public void exitAnnotation(Java8Parser.AnnotationContext ctx) {}
    */

    /*
    normalAnnotation
        :   '@' typeName '(' elementValuePairList? ')'
        ;
    @Override public void enterNormalAnnotation(Java8Parser.NormalAnnotationContext ctx) {}
    @Override public void exitNormalAnnotation(Java8Parser.NormalAnnotationContext ctx) {}
    */

    /*
    elementValuePairList
        :   elementValuePair (',' elementValuePair)*
        ;
    @Override public void enterElementValuePairList(Java8Parser.ElementValuePairListContext ctx) {}
    @Override public void exitElementValuePairList(Java8Parser.ElementValuePairListContext ctx) {}
    */

    /*
    elementValuePair
        :   Identifier '=' elementValue
        ;
    @Override public void enterElementValuePair(Java8Parser.ElementValuePairContext ctx) {}
    @Override public void exitElementValuePair(Java8Parser.ElementValuePairContext ctx) {}
    */

    /*
    elementValue
        :   conditionalExpression
        |   elementValueArrayInitializer
        |   annotation
        ;
    @Override public void enterElementValue(Java8Parser.ElementValueContext ctx) {}
    @Override public void exitElementValue(Java8Parser.ElementValueContext ctx) {}
    */

    /*
    elementValueArrayInitializer
        :   '{' elementValueList? ','? '}'
        ;
    @Override public void enterElementValueArrayInitializer(Java8Parser.ElementValueArrayInitializerContext ctx) {}
    @Override public void exitElementValueArrayInitializer(Java8Parser.ElementValueArrayInitializerContext ctx) {}
    */

    /*
    elementValueList
        :   elementValue (',' elementValue)*
        ;
    @Override public void enterElementValueList(Java8Parser.ElementValueListContext ctx) {}
    @Override public void exitElementValueList(Java8Parser.ElementValueListContext ctx) {}
    */

    /*
    markerAnnotation
        :   '@' typeName
        ;
    @Override public void enterMarkerAnnotation(Java8Parser.MarkerAnnotationContext ctx) {}
    @Override public void exitMarkerAnnotation(Java8Parser.MarkerAnnotationContext ctx) {}
    */

    /*
    singleElementAnnotation
        :   '@' typeName '(' elementValue ')'
        ;
    @Override public void enterSingleElementAnnotation(Java8Parser.SingleElementAnnotationContext ctx) {}
    @Override public void exitSingleElementAnnotation(Java8Parser.SingleElementAnnotationContext ctx) {}
    */

    // ---------------------------------------------------------------------------------------------
    // Arrays (§10)

    /*
    arrayInitializer
        :   '{' variableInitializerList? ','? '}'
        ;
    @Override public void enterArrayInitializer(Java8Parser.ArrayInitializerContext ctx) {}
    @Override public void exitArrayInitializer(Java8Parser.ArrayInitializerContext ctx) {}
    */

    /*
    variableInitializerList
        :   variableInitializer (',' variableInitializer)*
        ;
    @Override public void enterVariableInitializerList(Java8Parser.VariableInitializerListContext ctx) {}
    @Override public void exitVariableInitializerList(Java8Parser.VariableInitializerListContext ctx) {}
    */

    // ---------------------------------------------------------------------------------------------
    // Blocks and Statements (§14)

    /*
    block
        :   '{' blockStatements? '}'
        ;
    @Override public void enterBlock(Java8Parser.BlockContext ctx) {}
    @Override public void exitBlock(Java8Parser.BlockContext ctx) {}
    */

    /*
    blockStatements
        :   blockStatement blockStatement*
        ;
    @Override public void enterBlockStatements(Java8Parser.BlockStatementsContext ctx) {}
    @Override public void exitBlockStatements(Java8Parser.BlockStatementsContext ctx) {}
    */

    /*
    blockStatement
        :   localVariableDeclarationStatement
        |   classDeclaration
        |   statement
        ;
    @Override public void enterBlockStatement(Java8Parser.BlockStatementContext ctx) {}
    @Override public void exitBlockStatement(Java8Parser.BlockStatementContext ctx) {}
    */

    /*
    localVariableDeclarationStatement
        :   localVariableDeclaration ';'
        ;
    @Override public void enterLocalVariableDeclarationStatement(Java8Parser.LocalVariableDeclarationStatementContext ctx) {}
    @Override public void exitLocalVariableDeclarationStatement(Java8Parser.LocalVariableDeclarationStatementContext ctx) {}
    */

    /*
    localVariableDeclaration
        :   variableModifier* unannType variableDeclaratorList
        ;
    @Override public void enterLocalVariableDeclaration(Java8Parser.LocalVariableDeclarationContext ctx) {}
    @Override public void exitLocalVariableDeclaration(Java8Parser.LocalVariableDeclarationContext ctx) {}
    */

    /*
    statement
        :   statementWithoutTrailingSubstatement
        |   labeledStatement
        |   ifThenStatement
        |   ifThenElseStatement
        |   whileStatement
        |   forStatement
        ;
    @Override public void enterStatement(Java8Parser.StatementContext ctx) {}
    @Override public void exitStatement(Java8Parser.StatementContext ctx) {}
    */

    /*
    statementNoShortIf
        :   statementWithoutTrailingSubstatement
        |   labeledStatementNoShortIf
        |   ifThenElseStatementNoShortIf
        |   whileStatementNoShortIf
        |   forStatementNoShortIf
        ;
    @Override public void enterStatementNoShortIf(Java8Parser.StatementNoShortIfContext ctx) {}
    @Override public void exitStatementNoShortIf(Java8Parser.StatementNoShortIfContext ctx) {}
    */

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
    @Override public void enterStatementWithoutTrailingSubstatement(Java8Parser.StatementWithoutTrailingSubstatementContext ctx) {}
    @Override public void exitStatementWithoutTrailingSubstatement(Java8Parser.StatementWithoutTrailingSubstatementContext ctx) {}
    */

    /*
    emptyStatement
        :   ';'
        ;
    @Override public void enterEmptyStatement(Java8Parser.EmptyStatementContext ctx) {}
    @Override public void exitEmptyStatement(Java8Parser.EmptyStatementContext ctx) {}
    */

    /*
    labeledStatement
        :   Identifier ':' statement
        ;
    @Override public void enterLabeledStatement(Java8Parser.LabeledStatementContext ctx) {}
    @Override public void exitLabeledStatement(Java8Parser.LabeledStatementContext ctx) {}
    */

    /*
    labeledStatementNoShortIf
        :   Identifier ':' statementNoShortIf
        ;
    @Override public void enterLabeledStatementNoShortIf(Java8Parser.LabeledStatementNoShortIfContext ctx) {}
    @Override public void exitLabeledStatementNoShortIf(Java8Parser.LabeledStatementNoShortIfContext ctx) {}
    */

    /*
    expressionStatement
        :   statementExpression ';'
        ;
    @Override public void enterExpressionStatement(Java8Parser.ExpressionStatementContext ctx) {}
    @Override public void exitExpressionStatement(Java8Parser.ExpressionStatementContext ctx) {}
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
    @Override public void enterStatementExpression(Java8Parser.StatementExpressionContext ctx) {}
    @Override public void exitStatementExpression(Java8Parser.StatementExpressionContext ctx) {}
    */

    /*
    ifThenStatement
        :   'if' '(' expression ')' statement
        ;
    @Override public void enterIfThenStatement(Java8Parser.IfThenStatementContext ctx) {}
    @Override public void exitIfThenStatement(Java8Parser.IfThenStatementContext ctx) {}
    */

    /*
    ifThenElseStatement
        :   'if' '(' expression ')' statementNoShortIf 'else' statement
        ;
    @Override public void enterIfThenElseStatement(Java8Parser.IfThenElseStatementContext ctx) {}
    @Override public void exitIfThenElseStatement(Java8Parser.IfThenElseStatementContext ctx) {}
    */

    /*
    ifThenElseStatementNoShortIf
        :   'if' '(' expression ')' statementNoShortIf 'else' statementNoShortIf
        ;
    @Override public void enterIfThenElseStatementNoShortIf(Java8Parser.IfThenElseStatementNoShortIfContext ctx) {}
    @Override public void exitIfThenElseStatementNoShortIf(Java8Parser.IfThenElseStatementNoShortIfContext ctx) {}
    */

    /*
    assertStatement
        :   'assert' expression ';'
        |   'assert' expression ':' expression ';'
        ;
    @Override public void enterAssertStatement(Java8Parser.AssertStatementContext ctx) {}
    @Override public void exitAssertStatement(Java8Parser.AssertStatementContext ctx) {}
    */

    /*
    switchStatement
        :   'switch' '(' expression ')' switchBlock
        ;
    @Override public void enterSwitchStatement(Java8Parser.SwitchStatementContext ctx) {}
    @Override public void exitSwitchStatement(Java8Parser.SwitchStatementContext ctx) {}
    */

    /*
    switchBlock
        :   '{' switchBlockStatementGroup* switchLabel* '}'
        ;
    @Override public void enterSwitchBlock(Java8Parser.SwitchBlockContext ctx) {}
    @Override public void exitSwitchBlock(Java8Parser.SwitchBlockContext ctx) {}
    */

    /*
    switchBlockStatementGroup
        :   switchLabels blockStatements
        ;
    @Override public void enterSwitchBlockStatementGroup(Java8Parser.SwitchBlockStatementGroupContext ctx) {}
    @Override public void exitSwitchBlockStatementGroup(Java8Parser.SwitchBlockStatementGroupContext ctx) {}
    */

    /*
    switchLabels
        :   switchLabel switchLabel*
        ;
    @Override public void enterSwitchLabels(Java8Parser.SwitchLabelsContext ctx) {}
    @Override public void exitSwitchLabels(Java8Parser.SwitchLabelsContext ctx) {}
    */

    /*
    switchLabel
        :   'case' constantExpression ':'
        |   'case' enumConstantName ':'
        |   'default' ':'
        ;
    @Override public void enterSwitchLabel(Java8Parser.SwitchLabelContext ctx) {}
    @Override public void exitSwitchLabel(Java8Parser.SwitchLabelContext ctx) {}
    */

    /*
    enumConstantName
        :   Identifier
        ;
    @Override public void enterEnumConstantName(Java8Parser.EnumConstantNameContext ctx) {}
    @Override public void exitEnumConstantName(Java8Parser.EnumConstantNameContext ctx) {}
    */

    /*
    whileStatement
        :   'while' '(' expression ')' statement
        ;
    @Override public void enterWhileStatement(Java8Parser.WhileStatementContext ctx) {}
    @Override public void exitWhileStatement(Java8Parser.WhileStatementContext ctx) {}
    */

    /*
    whileStatementNoShortIf
        :   'while' '(' expression ')' statementNoShortIf
        ;
    @Override public void enterWhileStatementNoShortIf(Java8Parser.WhileStatementNoShortIfContext ctx) {}
    @Override public void exitWhileStatementNoShortIf(Java8Parser.WhileStatementNoShortIfContext ctx) {}
    */

    /*
    doStatement
        :   'do' statement 'while' '(' expression ')' ';'
        ;
    @Override public void enterDoStatement(Java8Parser.DoStatementContext ctx) {}
    @Override public void exitDoStatement(Java8Parser.DoStatementContext ctx) {}
    */

    /*
    forStatement
        :   basicForStatement
        |   enhancedForStatement
        ;
    @Override public void enterForStatement(Java8Parser.ForStatementContext ctx) {}
    @Override public void exitForStatement(Java8Parser.ForStatementContext ctx) {}
    */

    /*
    forStatementNoShortIf
        :   basicForStatementNoShortIf
        |   enhancedForStatementNoShortIf
        ;
    @Override public void enterForStatementNoShortIf(Java8Parser.ForStatementNoShortIfContext ctx) {}
    @Override public void exitForStatementNoShortIf(Java8Parser.ForStatementNoShortIfContext ctx) {}
    */

    /*
    basicForStatement
        :   'for' '(' forInit? ';' expression? ';' forUpdate? ')' statement
        ;
    @Override public void enterBasicForStatement(Java8Parser.BasicForStatementContext ctx) {}
    @Override public void exitBasicForStatement(Java8Parser.BasicForStatementContext ctx) {}
    */

    /*
    basicForStatementNoShortIf
        :   'for' '(' forInit? ';' expression? ';' forUpdate? ')' statementNoShortIf
        ;
    @Override public void enterBasicForStatementNoShortIf(Java8Parser.BasicForStatementNoShortIfContext ctx) {}
    @Override public void exitBasicForStatementNoShortIf(Java8Parser.BasicForStatementNoShortIfContext ctx) {}
    */

    /*
    forInit
        :   statementExpressionList
        |   localVariableDeclaration
        ;
    @Override public void enterForInit(Java8Parser.ForInitContext ctx) {}
    @Override public void exitForInit(Java8Parser.ForInitContext ctx) {}
    */

    /*
    forUpdate
        :   statementExpressionList
        ;
    @Override public void enterForUpdate(Java8Parser.ForUpdateContext ctx) {}
    @Override public void exitForUpdate(Java8Parser.ForUpdateContext ctx) {}
    */

    /*
    statementExpressionList
        :   statementExpression (',' statementExpression)*
        ;
    @Override public void enterStatementExpressionList(Java8Parser.StatementExpressionListContext ctx) {}
    @Override public void exitStatementExpressionList(Java8Parser.StatementExpressionListContext ctx) {}
    */

    /*
    enhancedForStatement
        :   'for' '(' variableModifier* unannType variableDeclaratorId ':' expression ')' statement
        ;
    @Override public void enterEnhancedForStatement(Java8Parser.EnhancedForStatementContext ctx) {}
    @Override public void exitEnhancedForStatement(Java8Parser.EnhancedForStatementContext ctx) {}
    */

    /*
    enhancedForStatementNoShortIf
        :   'for' '(' variableModifier* unannType variableDeclaratorId ':' expression ')' statementNoShortIf
        ;
    @Override public void enterEnhancedForStatementNoShortIf(Java8Parser.EnhancedForStatementNoShortIfContext ctx) {}
    @Override public void exitEnhancedForStatementNoShortIf(Java8Parser.EnhancedForStatementNoShortIfContext ctx) {}
    */

    /*
    breakStatement
        :   'break' Identifier? ';'
        ;
    @Override public void enterBreakStatement(Java8Parser.BreakStatementContext ctx) {}
    @Override public void exitBreakStatement(Java8Parser.BreakStatementContext ctx) {}
    */

    /*
    continueStatement
        :   'continue' Identifier? ';'
        ;
    @Override public void enterContinueStatement(Java8Parser.ContinueStatementContext ctx) {}
    @Override public void exitContinueStatement(Java8Parser.ContinueStatementContext ctx) {}
    */

    /*
    returnStatement
        :   'return' expression? ';'
        ;
    @Override public void enterReturnStatement(Java8Parser.ReturnStatementContext ctx) {}
    @Override public void exitReturnStatement(Java8Parser.ReturnStatementContext ctx) {}
    */

    /*
    throwStatement
        :   'throw' expression ';'
        ;
    @Override public void enterThrowStatement(Java8Parser.ThrowStatementContext ctx) {}
    @Override public void exitThrowStatement(Java8Parser.ThrowStatementContext ctx) {}
    */

    /*
    synchronizedStatement
        :   'synchronized' '(' expression ')' block
        ;
    @Override public void enterSynchronizedStatement(Java8Parser.SynchronizedStatementContext ctx) {}
    @Override public void exitSynchronizedStatement(Java8Parser.SynchronizedStatementContext ctx) {}
    */

    /*
    tryStatement
        :   'try' block catches
        |   'try' block catches? finally_
        |   tryWithResourcesStatement
        ;
    @Override public void enterTryStatement(Java8Parser.TryStatementContext ctx) {}
    @Override public void exitTryStatement(Java8Parser.TryStatementContext ctx) {}
    */

    /*
    catches
        :   catchClause catchClause*
        ;
    @Override public void enterCatches(Java8Parser.CatchesContext ctx) {}
    @Override public void exitCatches(Java8Parser.CatchesContext ctx) {}
    */

    /*
    catchClause
        :   'catch' '(' catchFormalParameter ')' block
        ;
    @Override public void enterCatchClause(Java8Parser.CatchClauseContext ctx) {}
    @Override public void exitCatchClause(Java8Parser.CatchClauseContext ctx) {}
    */

    /*
    catchFormalParameter
        :   variableModifier* catchType variableDeclaratorId
        ;
    @Override public void enterCatchFormalParameter(Java8Parser.CatchFormalParameterContext ctx) {}
    @Override public void exitCatchFormalParameter(Java8Parser.CatchFormalParameterContext ctx) {}
    */

    /*
    catchType
        :   unannClassType ('|' classType)*
        ;
    @Override public void enterCatchType(Java8Parser.CatchTypeContext ctx) {}
    @Override public void exitCatchType(Java8Parser.CatchTypeContext ctx) {}
    */

    /*
    finally_
        :   'finally' block
        ;
    @Override public void enterFinally_(Java8Parser.Finally_Context ctx) {}
    @Override public void exitFinally_(Java8Parser.Finally_Context ctx) {}
    */

    /*
    tryWithResourcesStatement
        :   'try' resourceSpecification block catches? finally_?
        ;
    @Override public void enterTryWithResourcesStatement(Java8Parser.TryWithResourcesStatementContext ctx) {}
    @Override public void exitTryWithResourcesStatement(Java8Parser.TryWithResourcesStatementContext ctx) {}
    */

    /*
    resourceSpecification
        :   '(' resourceList ';'? ')'
        ;
    @Override public void enterResourceSpecification(Java8Parser.ResourceSpecificationContext ctx) {}
    @Override public void exitResourceSpecification(Java8Parser.ResourceSpecificationContext ctx) {}
    */

    /*
    resourceList
        :   resource (';' resource)*
        ;
    @Override public void enterResourceList(Java8Parser.ResourceListContext ctx) {}
    @Override public void exitResourceList(Java8Parser.ResourceListContext ctx) {}
    */

    /*
    resource
        :   variableModifier* unannType variableDeclaratorId '=' expression
        ;
    @Override public void enterResource(Java8Parser.ResourceContext ctx) {}
    @Override public void exitResource(Java8Parser.ResourceContext ctx) {}
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
    @Override public void enterPrimary(Java8Parser.PrimaryContext ctx) {}
    @Override public void exitPrimary(Java8Parser.PrimaryContext ctx) {}
    */

    /*
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
    @Override public void enterPrimaryNoNewArray(Java8Parser.PrimaryNoNewArrayContext ctx) {}
    @Override public void exitPrimaryNoNewArray(Java8Parser.PrimaryNoNewArrayContext ctx) {}
    */

    /*
    primaryNoNewArray_lf_arrayAccess
        :
        ;
    @Override public void enterPrimaryNoNewArray_lf_arrayAccess(Java8Parser.PrimaryNoNewArray_lf_arrayAccessContext ctx) {}
    @Override public void exitPrimaryNoNewArray_lf_arrayAccess(Java8Parser.PrimaryNoNewArray_lf_arrayAccessContext ctx) {}
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
    @Override public void enterPrimaryNoNewArray_lfno_arrayAccess(Java8Parser.PrimaryNoNewArray_lfno_arrayAccessContext ctx) {}
    @Override public void exitPrimaryNoNewArray_lfno_arrayAccess(Java8Parser.PrimaryNoNewArray_lfno_arrayAccessContext ctx) {}
    */

    /*
    primaryNoNewArray_lf_primary
        :   classInstanceCreationExpression_lf_primary
        |   fieldAccess_lf_primary
        |   arrayAccess_lf_primary
        |   methodInvocation_lf_primary
        |   methodReference_lf_primary
        ;
    @Override public void enterPrimaryNoNewArray_lf_primary(Java8Parser.PrimaryNoNewArray_lf_primaryContext ctx) {}
    @Override public void exitPrimaryNoNewArray_lf_primary(Java8Parser.PrimaryNoNewArray_lf_primaryContext ctx) {}
    */

    /*
    primaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary
        :
        ;
    @Override public void enterPrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary(Java8Parser.PrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primaryContext ctx) {}
    @Override public void exitPrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary(Java8Parser.PrimaryNoNewArray_lf_primary_lf_arrayAccess_lf_primaryContext ctx) {}
    */

    /*
    primaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary
        :   classInstanceCreationExpression_lf_primary
        |   fieldAccess_lf_primary
        |   methodInvocation_lf_primary
        |   methodReference_lf_primary
        ;
    @Override public void enterPrimaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary(Java8Parser.PrimaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primaryContext ctx) {}
    @Override public void exitPrimaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary(Java8Parser.PrimaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primaryContext ctx) {}
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
    @Override public void enterPrimaryNoNewArray_lfno_primary(Java8Parser.PrimaryNoNewArray_lfno_primaryContext ctx) {}
    @Override public void exitPrimaryNoNewArray_lfno_primary(Java8Parser.PrimaryNoNewArray_lfno_primaryContext ctx) {}
    */

    /*
    primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary
        :
        ;
    @Override public void enterPrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary(Java8Parser.PrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primaryContext ctx) {}
    @Override public void exitPrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary(Java8Parser.PrimaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primaryContext ctx) {}
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
    @Override public void enterPrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary(Java8Parser.PrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primaryContext ctx) {}
    @Override public void exitPrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary(Java8Parser.PrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primaryContext ctx) {}
    */

    /*
    classInstanceCreationExpression
        :   'new' typeArguments? annotation* Identifier ('.' annotation* Identifier)* typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        |   expressionName '.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        |   primary '.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        ;
    @Override public void enterClassInstanceCreationExpression(Java8Parser.ClassInstanceCreationExpressionContext ctx) {}
    @Override public void exitClassInstanceCreationExpression(Java8Parser.ClassInstanceCreationExpressionContext ctx) {}
    */

    /*
    classInstanceCreationExpression_lf_primary
        :   '.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        ;
    @Override public void enterClassInstanceCreationExpression_lf_primary(Java8Parser.ClassInstanceCreationExpression_lf_primaryContext ctx) {}
    @Override public void exitClassInstanceCreationExpression_lf_primary(Java8Parser.ClassInstanceCreationExpression_lf_primaryContext ctx) {}
    */

    /*
    classInstanceCreationExpression_lfno_primary
        :   'new' typeArguments? annotation* Identifier ('.' annotation* Identifier)* typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        |   expressionName '.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        ;
    @Override public void enterClassInstanceCreationExpression_lfno_primary(Java8Parser.ClassInstanceCreationExpression_lfno_primaryContext ctx) {}
    @Override public void exitClassInstanceCreationExpression_lfno_primary(Java8Parser.ClassInstanceCreationExpression_lfno_primaryContext ctx) {}
    */

    /*
    typeArgumentsOrDiamond
        :   typeArguments
        |   '<' '>'
        ;
    @Override public void enterTypeArgumentsOrDiamond(Java8Parser.TypeArgumentsOrDiamondContext ctx) {}
    @Override public void exitTypeArgumentsOrDiamond(Java8Parser.TypeArgumentsOrDiamondContext ctx) {}
    */

    /*
    fieldAccess
        :   primary '.' Identifier
        |   'super' '.' Identifier
        |   typeName '.' 'super' '.' Identifier
        ;
    @Override public void enterFieldAccess(Java8Parser.FieldAccessContext ctx) {}
    @Override public void exitFieldAccess(Java8Parser.FieldAccessContext ctx) {}
    */

    /*
    fieldAccess_lf_primary
        :   '.' Identifier
        ;
    @Override public void enterFieldAccess_lf_primary(Java8Parser.FieldAccess_lf_primaryContext ctx) {}
    @Override public void exitFieldAccess_lf_primary(Java8Parser.FieldAccess_lf_primaryContext ctx) {}
    */

    /*
    fieldAccess_lfno_primary
        :   'super' '.' Identifier
        |   typeName '.' 'super' '.' Identifier
        ;
    @Override public void enterFieldAccess_lfno_primary(Java8Parser.FieldAccess_lfno_primaryContext ctx) {}
    @Override public void exitFieldAccess_lfno_primary(Java8Parser.FieldAccess_lfno_primaryContext ctx) {}
    */

    /*
    arrayAccess
        :   (   expressionName '[' expression ']'
            |   primaryNoNewArray_lfno_arrayAccess '[' expression ']'
            )
            (   primaryNoNewArray_lf_arrayAccess '[' expression ']'
            )*
        ;
    @Override public void enterArrayAccess(Java8Parser.ArrayAccessContext ctx) {}
    @Override public void exitArrayAccess(Java8Parser.ArrayAccessContext ctx) {}
    */

    /*
    arrayAccess_lf_primary
        :   (   primaryNoNewArray_lf_primary_lfno_arrayAccess_lf_primary '[' expression ']'
            )
            (   primaryNoNewArray_lf_primary_lf_arrayAccess_lf_primary '[' expression ']'
            )*
        ;
    @Override public void enterArrayAccess_lf_primary(Java8Parser.ArrayAccess_lf_primaryContext ctx) {}
    @Override public void exitArrayAccess_lf_primary(Java8Parser.ArrayAccess_lf_primaryContext ctx) {}
    */

    /*
    arrayAccess_lfno_primary
        :   (   expressionName '[' expression ']'
            |   primaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary '[' expression ']'
            )
            (   primaryNoNewArray_lfno_primary_lf_arrayAccess_lfno_primary '[' expression ']'
            )*
        ;
    @Override public void enterArrayAccess_lfno_primary(Java8Parser.ArrayAccess_lfno_primaryContext ctx) {}
    @Override public void exitArrayAccess_lfno_primary(Java8Parser.ArrayAccess_lfno_primaryContext ctx) {}
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
    @Override public void enterMethodInvocation(Java8Parser.MethodInvocationContext ctx) {}
    @Override public void exitMethodInvocation(Java8Parser.MethodInvocationContext ctx) {}
    */

    /*
    methodInvocation_lf_primary
        :   '.' typeArguments? Identifier '(' argumentList? ')'
        ;
    @Override public void enterMethodInvocation_lf_primary(Java8Parser.MethodInvocation_lf_primaryContext ctx) {}
    @Override public void exitMethodInvocation_lf_primary(Java8Parser.MethodInvocation_lf_primaryContext ctx) {}
    */

    /*
    methodInvocation_lfno_primary
        :   methodName '(' argumentList? ')'
        |   typeName '.' typeArguments? Identifier '(' argumentList? ')'
        |   expressionName '.' typeArguments? Identifier '(' argumentList? ')'
        |   'super' '.' typeArguments? Identifier '(' argumentList? ')'
        |   typeName '.' 'super' '.' typeArguments? Identifier '(' argumentList? ')'
        ;
    @Override public void enterMethodInvocation_lfno_primary(Java8Parser.MethodInvocation_lfno_primaryContext ctx) {}
    @Override public void exitMethodInvocation_lfno_primary(Java8Parser.MethodInvocation_lfno_primaryContext ctx) {}
    */

    /*
    argumentList
        :   expression (',' expression)*
        ;
    @Override public void enterArgumentList(Java8Parser.ArgumentListContext ctx) {}
    @Override public void exitArgumentList(Java8Parser.ArgumentListContext ctx) {}
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
    @Override public void enterMethodReference(Java8Parser.MethodReferenceContext ctx) {}
    @Override public void exitMethodReference(Java8Parser.MethodReferenceContext ctx) {}
    */

    /*
    methodReference_lf_primary
        :   '::' typeArguments? Identifier
        ;
    @Override public void enterMethodReference_lf_primary(Java8Parser.MethodReference_lf_primaryContext ctx) {}
    @Override public void exitMethodReference_lf_primary(Java8Parser.MethodReference_lf_primaryContext ctx) {}
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
    @Override public void enterMethodReference_lfno_primary(Java8Parser.MethodReference_lfno_primaryContext ctx) {}
    @Override public void exitMethodReference_lfno_primary(Java8Parser.MethodReference_lfno_primaryContext ctx) {}
    */

    /*
    arrayCreationExpression
        :   'new' primitiveType dimExprs dims?
        |   'new' classOrInterfaceType dimExprs dims?
        |   'new' primitiveType dims arrayInitializer
        |   'new' classOrInterfaceType dims arrayInitializer
        ;
    @Override public void enterArrayCreationExpression(Java8Parser.ArrayCreationExpressionContext ctx) {}
    @Override public void exitArrayCreationExpression(Java8Parser.ArrayCreationExpressionContext ctx) {}
    */

    /*
    dimExprs
        :   dimExpr dimExpr*
        ;
    @Override public void enterDimExprs(Java8Parser.DimExprsContext ctx) {}
    @Override public void exitDimExprs(Java8Parser.DimExprsContext ctx) {}
    */

    /*
    dimExpr
        :   annotation* '[' expression ']'
        ;
    @Override public void enterDimExpr(Java8Parser.DimExprContext ctx) {}
    @Override public void exitDimExpr(Java8Parser.DimExprContext ctx) {}
    */

    /*
    constantExpression
        :   expression
        ;
    @Override public void enterConstantExpression(Java8Parser.ConstantExpressionContext ctx) {}
    @Override public void exitConstantExpression(Java8Parser.ConstantExpressionContext ctx) {}
    */

    /*
    expression
        :   lambdaExpression
        |   assignmentExpression
        ;
    @Override public void enterExpression(Java8Parser.ExpressionContext ctx) {}
    @Override public void exitExpression(Java8Parser.ExpressionContext ctx) {}
    */

    /*
    lambdaExpression
        :   lambdaParameters '->' lambdaBody
        ;
    @Override public void enterLambdaExpression(Java8Parser.LambdaExpressionContext ctx) {}
    @Override public void exitLambdaExpression(Java8Parser.LambdaExpressionContext ctx) {}
    */

    /*
    lambdaParameters
        :   Identifier
        |   '(' formalParameterList? ')'
        |   '(' inferredFormalParameterList ')'
        ;
    @Override public void enterLambdaParameters(Java8Parser.LambdaParametersContext ctx) {}
    @Override public void exitLambdaParameters(Java8Parser.LambdaParametersContext ctx) {}
    */

    /*
    inferredFormalParameterList
        :   Identifier (',' Identifier)*
        ;
    @Override public void enterInferredFormalParameterList(Java8Parser.InferredFormalParameterListContext ctx) {}
    @Override public void exitInferredFormalParameterList(Java8Parser.InferredFormalParameterListContext ctx) {}
    */

    /*
    lambdaBody
        :   expression
        |   block
        ;
    @Override public void enterLambdaBody(Java8Parser.LambdaBodyContext ctx) {}
    @Override public void exitLambdaBody(Java8Parser.LambdaBodyContext ctx) {}
    */

    /*
    assignmentExpression
        :   conditionalExpression
        |   assignment
        ;
    @Override public void enterAssignmentExpression(Java8Parser.AssignmentExpressionContext ctx) {}
    @Override public void exitAssignmentExpression(Java8Parser.AssignmentExpressionContext ctx) {}
    */

    /*
    assignment
        :   leftHandSide assignmentOperator expression
        ;
    @Override public void enterAssignment(Java8Parser.AssignmentContext ctx) {}
    @Override public void exitAssignment(Java8Parser.AssignmentContext ctx) {}
    */

    /*
    leftHandSide
        :   expressionName
        |   fieldAccess
        |   arrayAccess
        ;
    @Override public void enterLeftHandSide(Java8Parser.LeftHandSideContext ctx) {}
    @Override public void exitLeftHandSide(Java8Parser.LeftHandSideContext ctx) {}
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
    @Override public void enterAssignmentOperator(Java8Parser.AssignmentOperatorContext ctx) {}
    @Override public void exitAssignmentOperator(Java8Parser.AssignmentOperatorContext ctx) {}
    */

    /*
    conditionalExpression
        :   conditionalOrExpression
        |   conditionalOrExpression '?' expression ':' conditionalExpression
        ;
    @Override public void enterConditionalExpression(Java8Parser.ConditionalExpressionContext ctx) {}
    @Override public void exitConditionalExpression(Java8Parser.ConditionalExpressionContext ctx) {}
    */

    /*
    conditionalOrExpression
        :   conditionalAndExpression
        |   conditionalOrExpression '||' conditionalAndExpression
        ;
    @Override public void enterConditionalOrExpression(Java8Parser.ConditionalOrExpressionContext ctx) {}
    @Override public void exitConditionalOrExpression(Java8Parser.ConditionalOrExpressionContext ctx) {}
    */

    /*
    conditionalAndExpression
        :   inclusiveOrExpression
        |   conditionalAndExpression '&&' inclusiveOrExpression
        ;
    @Override public void enterConditionalAndExpression(Java8Parser.ConditionalAndExpressionContext ctx) {}
    @Override public void exitConditionalAndExpression(Java8Parser.ConditionalAndExpressionContext ctx) {}
    */

    /*
    inclusiveOrExpression
        :   exclusiveOrExpression
        |   inclusiveOrExpression '|' exclusiveOrExpression
        ;
    @Override public void enterInclusiveOrExpression(Java8Parser.InclusiveOrExpressionContext ctx) {}
    @Override public void exitInclusiveOrExpression(Java8Parser.InclusiveOrExpressionContext ctx) {}
    */

    /*
    exclusiveOrExpression
        :   andExpression
        |   exclusiveOrExpression '^' andExpression
        ;
    @Override public void enterExclusiveOrExpression(Java8Parser.ExclusiveOrExpressionContext ctx) {}
    @Override public void exitExclusiveOrExpression(Java8Parser.ExclusiveOrExpressionContext ctx) {}
    */

    /*
    andExpression
        :   equalityExpression
        |   andExpression '&' equalityExpression
        ;
    @Override public void enterAndExpression(Java8Parser.AndExpressionContext ctx) {}
    @Override public void exitAndExpression(Java8Parser.AndExpressionContext ctx) {}
    */

    /*
    equalityExpression
        :   relationalExpression
        |   equalityExpression '==' relationalExpression
        |   equalityExpression '!=' relationalExpression
        ;
    @Override public void enterEqualityExpression(Java8Parser.EqualityExpressionContext ctx) {}
    @Override public void exitEqualityExpression(Java8Parser.EqualityExpressionContext ctx) {}
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
    @Override public void enterRelationalExpression(Java8Parser.RelationalExpressionContext ctx) {}
    @Override public void exitRelationalExpression(Java8Parser.RelationalExpressionContext ctx) {}
    */

    /*
    shiftExpression
        :   additiveExpression
        |   shiftExpression '<' '<' additiveExpression
        |   shiftExpression '>' '>' additiveExpression
        |   shiftExpression '>' '>' '>' additiveExpression
        ;
    @Override public void enterShiftExpression(Java8Parser.ShiftExpressionContext ctx) {}
    @Override public void exitShiftExpression(Java8Parser.ShiftExpressionContext ctx) {}
    */

    /*
    additiveExpression
        :   multiplicativeExpression
        |   additiveExpression '+' multiplicativeExpression
        |   additiveExpression '-' multiplicativeExpression
        ;
    @Override public void enterAdditiveExpression(Java8Parser.AdditiveExpressionContext ctx) {}
    @Override public void exitAdditiveExpression(Java8Parser.AdditiveExpressionContext ctx) {}
    */

    /*
    multiplicativeExpression
        :   unaryExpression
        |   multiplicativeExpression '*' unaryExpression
        |   multiplicativeExpression '/' unaryExpression
        |   multiplicativeExpression '%' unaryExpression
        ;
    @Override public void enterMultiplicativeExpression(Java8Parser.MultiplicativeExpressionContext ctx) {}
    @Override public void exitMultiplicativeExpression(Java8Parser.MultiplicativeExpressionContext ctx) {}
    */

    /*
    unaryExpression
        :   preIncrementExpression
        |   preDecrementExpression
        |   '+' unaryExpression
        |   '-' unaryExpression
        |   unaryExpressionNotPlusMinus
        ;
    @Override public void enterUnaryExpression(Java8Parser.UnaryExpressionContext ctx) {}
    @Override public void exitUnaryExpression(Java8Parser.UnaryExpressionContext ctx) {}
    */

    /*
    preIncrementExpression
        :   '++' unaryExpression
        ;
    @Override public void enterPreIncrementExpression(Java8Parser.PreIncrementExpressionContext ctx) {}
    @Override public void exitPreIncrementExpression(Java8Parser.PreIncrementExpressionContext ctx) {}
    */

    /*
    preDecrementExpression
        :   '--' unaryExpression
        ;
    @Override public void enterPreDecrementExpression(Java8Parser.PreDecrementExpressionContext ctx) {}
    @Override public void exitPreDecrementExpression(Java8Parser.PreDecrementExpressionContext ctx) {}
    */

    /*
    unaryExpressionNotPlusMinus
        :   postfixExpression
        |   '~' unaryExpression
        |   '!' unaryExpression
        |   castExpression
        ;
    @Override public void enterUnaryExpressionNotPlusMinus(Java8Parser.UnaryExpressionNotPlusMinusContext ctx) {}
    @Override public void exitUnaryExpressionNotPlusMinus(Java8Parser.UnaryExpressionNotPlusMinusContext ctx) {}
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
    @Override public void enterPostfixExpression(Java8Parser.PostfixExpressionContext ctx) {}
    @Override public void exitPostfixExpression(Java8Parser.PostfixExpressionContext ctx) {}
    */

    /*
    postIncrementExpression
        :   postfixExpression '++'
        ;
    @Override public void enterPostIncrementExpression(Java8Parser.PostIncrementExpressionContext ctx) {}
    @Override public void exitPostIncrementExpression(Java8Parser.PostIncrementExpressionContext ctx) {}
    */

    /*
    postIncrementExpression_lf_postfixExpression
        :   '++'
        ;
    @Override public void enterPostIncrementExpression_lf_postfixExpression(Java8Parser.PostIncrementExpression_lf_postfixExpressionContext ctx) {}
    @Override public void exitPostIncrementExpression_lf_postfixExpression(Java8Parser.PostIncrementExpression_lf_postfixExpressionContext ctx) {}
    */

    /*
    postDecrementExpression
        :   postfixExpression '--'
        ;
    @Override public void enterPostDecrementExpression(Java8Parser.PostDecrementExpressionContext ctx) {}
    @Override public void exitPostDecrementExpression(Java8Parser.PostDecrementExpressionContext ctx) {}
    */

    /*
    postDecrementExpression_lf_postfixExpression
        :   '--'
        ;
    @Override public void enterPostDecrementExpression_lf_postfixExpression(Java8Parser.PostDecrementExpression_lf_postfixExpressionContext ctx) {}
    @Override public void exitPostDecrementExpression_lf_postfixExpression(Java8Parser.PostDecrementExpression_lf_postfixExpressionContext ctx) {}
    */

    /*
    castExpression
        :   '(' primitiveType ')' unaryExpression
        |   '(' referenceType additionalBound* ')' unaryExpressionNotPlusMinus
        |   '(' referenceType additionalBound* ')' lambdaExpression
        ;
    @Override public void enterCastExpression(Java8Parser.CastExpressionContext ctx) {}
    @Override public void exitCastExpression(Java8Parser.CastExpressionContext ctx) {}
    */

}


