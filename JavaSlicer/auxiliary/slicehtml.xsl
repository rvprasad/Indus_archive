<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:output method="html" indent="no"/>

  <xsl:strip-space elements="*"/>

  <xsl:param name="sliceFile" select="'file:slicer.xml'"/>

  <xsl:variable name="slice" select="document($sliceFile)"/>

  <xsl:template match="/">
    <html>
      <head>
        <style type="text/css">
          span.slice {color: blue}
          span.normal {color: black}
          span.green {color: green}
        </style>
      </head>
      <body bgcolor="#FFFFFF">
        <pre>
          <xsl:apply-templates/>
        </pre>
      </body>
    </html>
  </xsl:template>
  
  <xsl:template match="jimple">
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template name="modifiers">
    <xsl:if test="string-length(@accessSpec) > 0"> 
      <xsl:value-of select="@accessSpec"/>
      <xsl:text> </xsl:text>
    </xsl:if>
    <xsl:if test="@abstract='true'">
      <xsl:text>abstract </xsl:text>
    </xsl:if>
    <xsl:if test="@static='true'">
      <xsl:text>static </xsl:text>
    </xsl:if>
    <xsl:if test="@final='true'">
      <xsl:text>final </xsl:text>
    </xsl:if>
    <xsl:if test="@native='true'">
      <xsl:text>native </xsl:text>
    </xsl:if>
    <xsl:if test="@synchronized='true'">
      <xsl:text>synchronized </xsl:text>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="l1modifiers">
    <xsl:text>&#x9;</xsl:text><xsl:call-template name="modifiers"/>
  </xsl:template>
  
  <xsl:template name="l2">
    <xsl:text>&#xA;&#x9;&#x9;</xsl:text>
  </xsl:template>

  <xsl:template name="getTypeName">
    <xsl:param name="typeid"/>
    <xsl:param name="strType" select="string($typeid)"/>
    <xsl:variable name="j" select="id($strType)"/>
    <xsl:choose>
      <xsl:when test="count($j) &gt; 0 and string-length($j/@package) != 0">
        <xsl:value-of select="$j/@package"/>.<xsl:value-of select="$j/@name"/>
      </xsl:when>
      <xsl:when test="count($j) &gt; 0 and string-length($j/@package) = 0">
        <xsl:value-of select="$j/@name"/>
      </xsl:when>
      <xsl:when test="count($j) = 0 and contains($strType, '..')">
        <xsl:call-template name="getTypeName">
          <xsl:with-param name="typeid" select="substring-before($strType, '..')"/>
        </xsl:call-template>
        <xsl:call-template name="repeater">
          <xsl:with-param name="num" select="number(substring-after($strType, '..'))"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="count($j) = 0 and not(contains($strType, '..'))">
        <xsl:value-of select="$typeid"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="repeater">
    <xsl:param name="num" select="1"/>
    <xsl:if test="$num > 0">
      <xsl:text>[]</xsl:text>
      <xsl:call-template name="repeater">
        <xsl:with-param name="num" select="$num - 1"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="labelInserter">
    <xsl:param name="node"/>
    <xsl:if test="string($node/@label) = 'true'">
      <xsl:text>&#xA;&#xA;&#x9;</xsl:text>
      <xsl:value-of select="$node/@id"/>
      <xsl:text>:</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template name="sliceSpan">
    <xsl:param name="node"/>
    <xsl:choose>
      <xsl:when test="count($slice//*[string(@id)=string($node/@id)]) > 0">
        <span class="slice">
          <xsl:apply-templates select="$node">
            <xsl:with-param name="sliceFlag" select="0"/>
          </xsl:apply-templates>
        </span>
      </xsl:when>
      <xsl:otherwise>
        <span class="normal">
          <xsl:apply-templates select="$node">
            <xsl:with-param name="sliceFlag" select="0"/>
          </xsl:apply-templates>
        </span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template name="sliceSpanDiv">
    <xsl:param name="node"/>
    <xsl:choose>
      <xsl:when test="count($slice//*[string(@id)=string($node/@id)]) > 0">
        <div>
          <span class="slice">
            <xsl:apply-templates select="$node">
              <xsl:with-param name="sliceFlag" select="0"/>
            </xsl:apply-templates>
          </span>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <div>
          <span class="normal">
            <xsl:apply-templates select="$node">
              <xsl:with-param name="sliceFlag" select="0"/>
            </xsl:apply-templates>
          </span>
        </div>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template match="class|interface">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpanDiv">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>&#xA;&#xA;&#xA;&#xD;</xsl:text>
        
        <xsl:if test="string-length(@package)!=0">
          <xsl:text>package </xsl:text>
          <xsl:value-of select="@package"/>;
        </xsl:if>
        <xsl:text>&#xA;&#xD;</xsl:text>
        
        <xsl:call-template name="modifiers"/>
        <xsl:value-of select="local-name()"/>
        <xsl:text> </xsl:text>      
        <xsl:value-of select="@name"/> 
        <xsl:text>&#xD;</xsl:text>
        
        <xsl:apply-templates select="superclass"/>
        <xsl:apply-templates select="interfaceList"/>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>{&#xA;&#xD;</xsl:text>
        <xsl:apply-templates select="field"/>
        <xsl:if test="count(method) > 0">
          <br />
        </xsl:if>
        <xsl:apply-templates select="method"/>
        <xsl:text>}</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
    
  <xsl:template match="superclass">
    extends<xsl:text> </xsl:text>
    <xsl:call-template name="getTypeName">
      <xsl:with-param name="typeid" select="@typeId"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="interfaceList">
    implements<xsl:text> </xsl:text>
    <xsl:for-each select="superinterface">
      <xsl:call-template name="getTypeName">
        <xsl:with-param name="typeid" select="@typeId"/>
      </xsl:call-template>
      <xsl:if test="position() != last()">, </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="field">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="l1modifiers"/>
        <xsl:call-template name="getTypeName">
          <xsl:with-param name="typeid" select="@typeId"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <xsl:value-of select="@name"/><xsl:text>;&#xA;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="method">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpanDiv">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="l1modifiers"/>
        <xsl:apply-templates select="signature"/>
        <xsl:text>&#xA;&#xD;&#x9;{</xsl:text>
        <xsl:apply-templates select="local"/>
        <xsl:if test="count(local) > 0">
          <br />
        </xsl:if>
        <xsl:apply-templates select="child::*[not(self::signature|self::local|self::traplist)]"/>
        <xsl:if test="count(traplist/trap) > 0">
          <br />
        </xsl:if>
        <xsl:apply-templates select="traplist/trap"/>
        <xsl:text>&#xA;&#xD;&#x9;}</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    <br />
  </xsl:template>
  
  <xsl:template match="signature">
    <xsl:value-of select="returnType/@typeId"/><xsl:text> </xsl:text>
    <xsl:value-of select="ancestor::method/@name"/>
    <xsl:text>(</xsl:text>
    <xsl:for-each select="paramType">
      <xsl:call-template name="getTypeName">
        <xsl:with-param name="typeid" select="@typeId"/>
      </xsl:call-template>
      <xsl:text> $p</xsl:text><xsl:value-of select="@position"/>
      <xsl:if test="position() != last()">, </xsl:if>
    </xsl:for-each>
    <xsl:text>)</xsl:text>
    <xsl:if test="count(exception) > 0">
      <xsl:text> throws </xsl:text>
      <xsl:for-each select="exception">
        <xsl:call-template name="getTypeName">
          <xsl:with-param name="typeid" select="@typeId"/>
        </xsl:call-template>
        <xsl:if test="position() != last()">, </xsl:if>
      </xsl:for-each>
    </xsl:if> 
  </xsl:template>
  
  <xsl:template match="local">
    <xsl:call-template name="l2"/>
    <xsl:call-template name="getTypeName">
      <xsl:with-param name="typeid" select="@typeId"/>
    </xsl:call-template>
    <xsl:text> </xsl:text>
    <xsl:value-of select="@name"/>
    <xsl:text>;</xsl:text>
  </xsl:template>

  <xsl:template match="trap">
    <span class="green">
      <xsl:call-template name="l2"/>
      <xsl:text>catch </xsl:text>
      <xsl:call-template name="getTypeName">
        <xsl:with-param name="typeid" select="@typeId"/>
      </xsl:call-template>
      <xsl:text> from </xsl:text>
      <xsl:value-of select="@beginId"/>
      <xsl:text>&#xA;&#x9;&#x9;&#x9;&#x9;&#x9;to </xsl:text>
      <xsl:value-of select="@endId"/>
      <xsl:text>&#xA;&#x9;&#x9;&#x9;&#x9;&#x9;with </xsl:text>
      <xsl:value-of select="@handlerId"/>
    </span>
  </xsl:template>

  <xsl:template match="assign_stmt">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="labelInserter">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
        <xsl:call-template name="l2"/>
        <xsl:apply-templates select="lhs/*"/> = <xsl:apply-templates select="rhs/*"/>
        <xsl:text>;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="breakpoing_stmt">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="labelInserter">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
        breakpoint TODO
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>  

  <xsl:template match="entermonitor_stmt">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="labelInserter">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
        <xsl:call-template name="l2"/>
        <xsl:text>entermonitor </xsl:text><xsl:value-of select="id(string(local_ref/@localId))/@name"/>
        <xsl:text>;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="exitmonitor_stmt">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="labelInserter">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
        <xsl:call-template name="l2"/>
        <xsl:text>exitmonitor </xsl:text><xsl:value-of select="id(string(local_ref/@localId))/@name"/>
        <xsl:text>;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="goto_stmt">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="labelInserter">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
        <xsl:call-template name="l2"/><xsl:text>goto </xsl:text><xsl:value-of select="@target"/>
        <xsl:text>;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="identity_stmt">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="labelInserter">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
        <xsl:call-template name="l2"/>
        <xsl:apply-templates select="lhs/*"/> := <xsl:apply-templates select="rhs/*"/>
        <xsl:text>;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="if_stmt">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="labelInserter">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
        <xsl:call-template name="l2"/>
        <xsl:text>if </xsl:text><xsl:apply-templates select="condition"/>
        <xsl:text> goto </xsl:text><xsl:value-of select="@trueTargetId"/><xsl:text>;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="invoke_stmt">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="labelInserter">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
        <xsl:call-template name="l2"/>
        <xsl:apply-templates/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="lookupswitch_stmt">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="labelInserter">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
        <xsl:call-template name="l2"/>
        <xsl:text>lookupswitch(</xsl:text>
        <xsl:apply-templates select="key/*"/>
        <xsl:text>)&#xA;&#xD;</xsl:text>
        <xsl:call-template name="l2"/><xsl:text>{</xsl:text>
        <xsl:for-each select="case">
          <xsl:text>&#xA;&#xD;&#x9;&#x9;&#x9;case </xsl:text>
          <xsl:value-of select="@value"/>
          <xsl:text> goto </xsl:text>
          <xsl:value-of select="@targetId"/>
          <xsl:text>;</xsl:text>
        </xsl:for-each>
        <xsl:text>&#xA;&#xD;&#x9;&#x9;&#x9;default: </xsl:text>
        <xsl:text> goto </xsl:text>
        <xsl:value-of select="@defaultTargetId"/>
        <xsl:text>;</xsl:text>
        <xsl:call-template name="l2"/><xsl:text>}</xsl:text>    
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="tableswitch_stmt">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="labelInserter">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
        table TODO
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="nop_stmt">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="labelInserter">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
        <xsl:call-template name="l2"/><xsl:text>nop;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="ret_stmt">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="labelInserter">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
        <xsl:call-template name="l2"/><xsl:text>ret </xsl:text><xsl:apply-templates/>
        <xsl:text>;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="return_stmt">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="labelInserter">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
        <xsl:call-template name="l2"/><xsl:text>return </xsl:text><xsl:apply-templates/>
        <xsl:text>;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="returnvoid_stmt">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="labelInserter">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
        <xsl:call-template name="l2"/><xsl:text>return;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="throw_stmt">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="labelInserter">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
        <xsl:call-template name="l2"/><xsl:text>throw </xsl:text><xsl:apply-templates/>
        <xsl:text>;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="binary_expr">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="left_op/*"/>
        <xsl:variable name="op" select="@op"/>
        <xsl:choose>
          <xsl:when test="$op = 'add'"><xsl:text> + </xsl:text></xsl:when>
          <xsl:when test="$op = 'binary and'"><xsl:text> &amp; </xsl:text></xsl:when>
          <xsl:when test="$op = 'compare'"><xsl:text> cmp </xsl:text></xsl:when>
          <xsl:when test="$op = 'compare greater'"><xsl:text> &gt; </xsl:text></xsl:when>
          <xsl:when test="$op = 'compare lesser'"><xsl:text> &lt; </xsl:text></xsl:when>
          <xsl:when test="$op = 'divide'"><xsl:text> / </xsl:text></xsl:when>
          <xsl:when test="$op = 'equal'"><xsl:text> == </xsl:text></xsl:when>
          <xsl:when test="$op = 'greater than or equal'"><xsl:text> &gt;= </xsl:text></xsl:when>
          <xsl:when test="$op = 'greater than'"><xsl:text> &gt; </xsl:text></xsl:when>
          <xsl:when test="$op = 'less than or equal'"><xsl:text> &lt;= </xsl:text></xsl:when>
          <xsl:when test="$op = 'less than'"><xsl:text> &lt; </xsl:text></xsl:when>
          <xsl:when test="$op = 'multiply'"><xsl:text> * </xsl:text></xsl:when>
          <xsl:when test="$op = 'not equal'"><xsl:text> != </xsl:text></xsl:when>
          <xsl:when test="$op = 'binary or'"><xsl:text> | </xsl:text></xsl:when>
          <xsl:when test="$op = 'shift left'"><xsl:text> &lt;&lt; </xsl:text></xsl:when>
          <xsl:when test="$op = 'shift right'"><xsl:text> &gt;&gt; </xsl:text></xsl:when>
          <xsl:when test="$op = 'reminder'"><xsl:text> % </xsl:text></xsl:when>
          <xsl:when test="$op = 'subtract'"><xsl:text> - </xsl:text></xsl:when>
          <xsl:when test="$op = 'unsigned shift right'"><xsl:text> &gt;&gt;&gt; </xsl:text></xsl:when>
          <xsl:when test="$op = 'binary xor'"><xsl:text> ^ </xsl:text></xsl:when>
        </xsl:choose>
        <xsl:apply-templates select="right_op/*"/>
      </xsl:otherwise>
    </xsl:choose>
   </xsl:template>

  <xsl:template match="unary_expr">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="op" select="@op"/>
        <xsl:choose>
          <xsl:when test="$op = 'negation'"><xsl:text> !(</xsl:text></xsl:when>
          <xsl:when test="$op = 'length'"><xsl:text> length(</xsl:text></xsl:when>      
        </xsl:choose><xsl:text/>
        <xsl:text>)</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="invoke_expr">
   <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text/><xsl:value-of select="@name"/><xsl:text>invoke </xsl:text>
        <xsl:variable name="method" select="id(string(method_ref/@methodId))"/>
        <xsl:variable name="class" select="$method/ancestor::node()"/>
        <xsl:if test="@name != 'static'">
          <xsl:apply-templates select="base/local_ref"/><xsl:text>.</xsl:text>
        </xsl:if>
        <xsl:text>&lt;</xsl:text>
        <xsl:call-template name="getTypeName">
          <xsl:with-param name="typeid" select="$class/@id"/>
        </xsl:call-template>
        <xsl:text>: </xsl:text>
        <xsl:call-template name="getTypeName">
          <xsl:with-param name="typeid" select="$method/signature/returnType/@typeId"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <xsl:value-of select="$method/@name"/>
        <xsl:text>()&gt;(</xsl:text>
        <xsl:for-each select="arguments/*">
          <xsl:apply-templates select="self::node()"/>
          <xsl:if test="position() != last()">, </xsl:if>
        </xsl:for-each>
        <xsl:text>)</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
 </xsl:template>  
  
  <xsl:template match="array_ref">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="base/local_ref"/>
        <xsl:text>[</xsl:text>
        <xsl:apply-templates select="index/*"/>
        <xsl:text>]</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="float|double|integer|long">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@value"/><xsl:text/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="string">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <q><xsl:value-of select="@value"/></q><xsl:text/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  

  <xsl:template match="cast">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>(</xsl:text>
        <xsl:call-template name="getTypeName">
          <xsl:with-param name="typeid" select="@typeId"/>
        </xsl:call-template>
        <xsl:text>) </xsl:text>
        <xsl:apply-templates/><xsl:text/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="caught_exception_ref">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>@caughtexception</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="static_field_ref|instance_field_ref">
    <xsl:choose>
      <xsl:when test="name() = 'static_field_ref'">
        <xsl:variable name="ances" select="id(string(field_ref/@fieldId))"/>
        <xsl:call-template name="getTypeName">
          <xsl:with-param name="typeid" select="$ances/ancestor::node()/@id"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="name() = 'instance_field_ref'">
        <xsl:apply-templates select="base/*"/>
      </xsl:when>
    </xsl:choose>
    <xsl:text>.</xsl:text>
    <xsl:value-of select="id(string(field_ref/@fieldId))/@name"/>
  </xsl:template>

  <xsl:template match="instanceof">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>instanceof </xsl:text>
        <xsl:call-template name="getTypeName">
          <xsl:with-param name="typeid" select="@typeId"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="new|new_array|new_multi_array">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="name()"/>
        <xsl:text> </xsl:text>
        <xsl:call-template name="getTypeName">
          <xsl:with-param name="typeid" select="@typeId"/>
        </xsl:call-template>
        <xsl:for-each select="size">
          <xsl:sort order="ascending" select="dimension"/>
          <xsl:text>[</xsl:text>
          <xsl:apply-templates/>
          <xsl:text>]</xsl:text>
        </xsl:for-each>
        <xsl:if test="name() = 'new_multi_array'">
          <xsl:variable name="j" select="@dimension - count(size)"/>
          <xsl:if test="$j > 0">
            <xsl:call-template name="repeater">
              <xsl:with-param name="num" select="$j"/>
            </xsl:call-template>
          </xsl:if>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="null">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>null</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="parameter_ref">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>$p</xsl:text><xsl:value-of select="@position"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="local_ref">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="id(string(@localId))/@name"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="this">
    <xsl:param name="sliceFlag" select="1"/>
    <xsl:choose>
      <xsl:when test="$sliceFlag > 0">
        <xsl:call-template name="sliceSpan">
          <xsl:with-param name="node" select="self::node()"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>@this</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
