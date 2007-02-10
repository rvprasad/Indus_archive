<?xml version="1.0" encoding="utf-8"?>
<!--
Indus, a program analysis and transformation toolkit for Java.
Copyright (c) ${date} Venkatesh Prasad Ranganath

All rights reserved.  This program and the accompanying materials are made 
available under the terms of the Eclipse Public License v1.0 which accompanies 
the distribution containing this program, and is available at 
http://www.opensource.org/licenses/eclipse-1.0.php.

For questions about the license, copyright, and software, contact 
	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
                                
This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
at Kansas State University.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:output method="text"/>

  <xsl:param name="irFile"/>

  <xsl:variable name="ir" select="document($irFile)"/>

  <xsl:template match="/">
    <xsl:apply-templates select="./callgraph"/>
  </xsl:template>

  <xsl:template match="method">
    <xsl:variable name="methodId" select="@id"/>
    <xsl:variable name="methodNode" select="$ir//method[string(@id)=$methodId]"/>
    <xsl:variable name="methodName" select="concat($methodNode/../@package, '.', $methodNode/../@name, ' [', 
                                             $methodNode/@name, ']')"/> 
    <xsl:apply-templates select="callee">
      <xsl:with-param name="callerName" select="$methodName"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="caller">
      <xsl:with-param name="calleeName" select="$methodName"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="callee">
    <xsl:param name="callerName"/>
    <xsl:variable name="calleeId" select="@id"/>
    <xsl:variable name="calleeNode" select="$ir//method[string(@id)=$calleeId]"/>
    <xsl:variable name="calleeName" select="concat($calleeNode/../@package, '.', $calleeNode/../@name, ' [', 
                                             $calleeNode/@name, ']')"/> 
    <xsl:value-of select="$callerName"/> --> <xsl:value-of select="$calleeName"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="caller">
    <xsl:param name="calleeName"/>
    <xsl:variable name="callerId" select="@id"/>
    <xsl:variable name="callerNode" select="$ir//method[string(@id)=$callerId]"/>
    <xsl:variable name="callerName" select="concat($callerNode/../@package, '.', $callerNode/../@name, ' [', 
                                             $callerNode/@name, ']')"/> 
    <xsl:value-of select="$calleeName"/> &lt;-- <xsl:value-of select="$callerName"/>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

</xsl:stylesheet>
