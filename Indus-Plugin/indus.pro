-injars /home/ganeshan/Desktop/what/plugins/edu.ksu.cis.indus_0.2.0/lib/indus-20040717.jar
-injars /home/ganeshan/Desktop/what/plugins/edu.ksu.cis.indus_0.2.0/lib/slicer-20040717.jar
-injars /home/ganeshan/Desktop/what/plugins/edu.ksu.cis.indus_0.2.0/lib/staticanalyses-20040717.jar
-libraryjars /usr/java/j2sdk1.4.2_04/jre/lib/rt.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/soot-2.1.0-modified.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/commons-cli-1.0.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/commons-collections-3.0.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/commons-io-1.0.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/commons-lang-2.0.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/commons-logging-api.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/commons-logging.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/commons-pool-1.1.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/jibx-run.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/xmlenc-0.44.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/xmlunit1.0.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/log4j-1.2.8.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.swt.gtk_3.0.0/ws/gtk/swt.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.junit_3.8.1/junit.jar
-outjars /home/ganeshan/Desktop/what/plugins/edu.ksu.cis.indus_0.2.0/lib/Indus-0.2.jar

-printseeds /home/ganeshan/Desktop/maps/indus-0.2-seeds
-printusage /home/ganeshan/Desktop/maps/indus-0.2-usage-indus
-printmapping /home/ganeshan/Desktop/maps/indus-0.2-out.map
-verbose
-ignorewarnings
-defaultpackage obfuscated
-keepattributes InnerClasses,SourceFile,LineNumberTable,Deprecated,Signature
-renamesourcefileattribute SourceFile
-dontskipnonpubliclibraryclasses

-keep public class **.*CLI {
}

# Basic - Library. Keep all externally accessible classes, fields, and methods.
-keep public class **.I* {
    public protected <fields>;
    public protected <methods>;
}

-keepclasseswithmembers class * {
    final <methods>; 
}

# Additional - Native method names. Keep all native class/method names.
-keepclasseswithmembernames class * {
    native <methods>;
}



-keep public class edu.ksu.cis.indus.tools.slicer.SlicerTool

-keep public class edu.ksu.cis.indus.tools.CompositeConfiguration

-keep public class **JiBX*

-keep interface edu.ksu.cis.indus.common.soot.IStmtGraphFactory


-keep public class edu.ksu.cis.indus.common.soot.ExceptionFlowSensitiveStmtGraphFactory

-keep public class edu.ksu.cis.indus.staticanalyses.tokens.TokenUtil

-keep public class edu.ksu.cis.indus.common.soot.NamedTag

-keep public class edu.ksu.cis.indus.common.soot.SootBasedDriver

-keep public class edu.ksu.cis.indus.slicer.SliceCriteriaFactory

-keep public class edu.ksu.cis.indus.tools.Phase

-keep public class edu.ksu.cis.indus.common.soot.Util

-keep public class edu.ksu.cis.indus.slicer.ISliceCriterion

-keep public class edu.ksu.cis.indus.tools.slicer.SlicerConfiguration
