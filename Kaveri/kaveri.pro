-injars /home/ganeshan/Desktop/what/plugins/edu.ksu.cis.indus.kaveri_0.1.0/Kaveri.jar
-libraryjars /usr/java/j2sdk1.4.2_04/jre/lib/rt.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/commons-cli-1.0.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/commons-collections-3.0.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/commons-io-1.0.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/commons-lang-2.0.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/commons-logging-api.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/commons-logging.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/commons-pool-1.1.jar
#-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/indus-20040717.jar
-libraryjars /home/ganeshan/Desktop/what/plugins/edu.ksu.cis.indus_0.2.0/lib/Indus-0.2.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/jasminclasses-sable-1.2.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/jibx-run.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/log4j-1.2.8.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/log4jconfig.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/polyglot-1.1.0.jar
#-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/slicer-20040717.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/soot-2.1.0-modified.jar
#-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/staticanalyses-20040717.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/xmlenc-0.44.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/xmlunit1.0.jar
-libraryjars /home/ganeshan/Desktop/eclipse/workspace/Indus-Plugin/lib/xpp3.jar
-libraryjars /home/ganeshan/Desktop/what/plugins/edu.ksu.cis.indus.kaveri_0.1.0/lib/xstream.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.osgi_3.0.0/console.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.osgi_3.0.0/core.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.osgi_3.0.0/defaultAdaptor.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.debug.core_3.0.0/dtcore.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.debug.ui_3.0.0/dtui.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.osgi_3.0.0/eclipseAdaptor.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.ui.editors_3.0.0/editors.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.core.filebuffers_3.0.0/filebuffers.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.ui.ide_3.0.0/ide.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.jdt.ui_3.0.0/jdt.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.jdt.core_3.0.0/jdtcore.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.jface_3.0.0/jface.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.jface.text_3.0.0/jfacetext.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.jdt.launching_3.0.0/launching.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.osgi_3.0.0/osgi.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.osgi_3.0.0/resolver.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.core.resources_3.0.0/resources.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.core.runtime_3.0.0/runtime.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.search_3.0.0/search.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.swt.gtk_3.0.0/ws/gtk/swt.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.text_3.0.0/text.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.ui.workbench.texteditor_3.0.0/texteditor.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.ui_3.0.0/ui.jar
-libraryjars /home/ganeshan/Desktop/eclipse/plugins/org.eclipse.ui.workbench_3.0.0/workbench.jar
-outjars /home/ganeshan/Desktop/what/plugins/edu.ksu.cis.indus.kaveri_0.1.0/Kaveri-Obfuscate.jar

-printseeds /home/ganeshan/Desktop/maps/kaveri-seeds-0.1
-printusage /home/ganeshan/Desktop/maps/kaveri-usage-0.1
-printmapping /home/ganeshan/Desktop/maps/kaveri-out-0.1.map
-verbose
-defaultpackage whatever
-keepattributes SourceFile,LineNumberTable,Deprecated,Signature
-renamesourcefileattribute SourceFile
-dontskipnonpubliclibraryclasses


# Additional - Native method names. Keep all native class/method names.
-keepclasseswithmembernames class * {
    native <methods>;
}

# Explicitly preserve all serialization members. The Serializable interface
# is only a marker interface, so it wouldn't save them.
# You can comment this out if your library doesn't use serialization.
# If your code contains serializable classes that have to be backward
# compatible, please refer to the manual.
#-keepclassmembers class * implements java.io.Serializable {
#    static final long serialVersionUID;
#    private void writeObject(java.io.ObjectOutputStream);
#    private void readObject(java.io.ObjectInputStream);
#    Object writeReplace();
#    Object readResolve();
#}
# Your library may contain more items that need to be preserved; 
# typically classes that are dynamically created using Class.forName:
-keep public class edu.ksu.cis.indus.kaveri.KaveriPlugin

-keep public class edu.ksu.cis.indus.kaveri.decorator.IndusDecorator

-keep public class * extends org.eclipse.ui.IEditorActionDelegate

-keep public class * extends org.eclipse.ui.IObjectActionDelegate

-keep public class edu.ksu.cis.indus.kaveri.preferencedata.AnnotationData

-keep public class edu.ksu.cis.indus.kaveri.preferencedata.Criteria

-keep public class edu.ksu.cis.indus.kaveri.preferencedata.CriteriaData

-keep public interface  edu.ksu.cis.indus.kaveri.preferencedata.IDeltaListener

-keep public class edu.ksu.cis.indus.kaveri.preferencedata.SliceConfigurationHolder

-keep public class edu.ksu.cis.indus.kaveri.preferencedata.ViewConfiguration

-keep public class edu.ksu.cis.indus.kaveri.preferencedata.ViewData

-keep public class edu.ksu.cis.indus.kaveri.preferences.PluginPreference

-keep public class edu.ksu.cis.indus.kaveri.views.PartialSliceView
