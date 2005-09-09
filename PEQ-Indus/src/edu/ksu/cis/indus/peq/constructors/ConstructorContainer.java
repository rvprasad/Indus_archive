/*
 *
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
 *
 * This software is licensed under the KSU Open Academic License.
 * You should have received a copy of the license with the distribution.
 * A copy can be found at
 *     http://www.cis.ksu.edu/santos/license.html
 * or you can contact the lab at:
 *     SAnToS Laboratory
 *     234 Nichols Hall
 *     Manhattan, KS 66506, USA
 */
 
package edu.ksu.cis.indus.peq.constructors;

/**
 * @author ganeshan
 *
 * Holds the repository of constructors.
 * The constructors are reused across the graph labels as they
 * don't hold state information.
 */
public class ConstructorContainer {
    
    /**
     * ConstructorAST instances.
     */
    private ControlDepD cDepD = new ControlDepD();
    private ControlDepT cDepT = new ControlDepT();
    private IUse use = new IUse();
    private IDef def = new IDef();
    // Divergence
    private DvgDepD dvdd = new DvgDepD();
    private DvgDepT dvdt = new DvgDepT();
    // Ready
    private ReadyDepD rdd = new ReadyDepD();
    private ReadyDepT rdt = new ReadyDepT();
    // Synchronization
    private SyncDepD sdd = new SyncDepD();
    private SyncDepT sdt = new SyncDepT();
    // Inteference
    private IntfDepD idd = new IntfDepD();
    private IntfDepT idt = new IntfDepT();
    // Reference 
    private RDef rdef = new RDef();
    private RUse ruse = new RUse();
    
    
    private static ConstructorContainer container;
    
    /**
     * Return the singeleton instance.
     * @return ConstructorContainer The object instance.
     */
    public static ConstructorContainer getInstance() {
        if (container == null) {
            container = new ConstructorContainer();
        } 
        return container;
    }
    /**
     * @return Returns the cDepD.
     */
    public ControlDepD getCDepD() {
        return cDepD;
    }
    /**
     * @return Returns the cDepT.
     */
    public ControlDepT getCDepT() {
        return cDepT;
    }
    /**
     * @return Returns the def.
     */
    public IDef getIDef() {
        return def;
    }
    /**
     * @return Returns the use.
     */
    public IUse getIUse() {
        return use;
    }
    /**
     * @return Returns the dvdd.
     */
    public DvgDepD getDvdd() {
        return dvdd;
    }
    /**
     * @return Returns the dvdt.
     */
    public DvgDepT getDvdt() {
        return dvdt;
    }
    /**
     * @return Returns the idd.
     */
    public IntfDepD getIdd() {
        return idd;
    }
    /**
     * @return Returns the idt.
     */
    public IntfDepT getIdt() {
        return idt;
    }
    /**
     * @return Returns the rdd.
     */
    public ReadyDepD getRdd() {
        return rdd;
    }
    /**
     * @return Returns the rdef.
     */
    public RDef getRdef() {
        return rdef;
    }
    /**
     * @return Returns the rdt.
     */
    public ReadyDepT getRdt() {
        return rdt;
    }
    /**
     * @return Returns the ruse.
     */
    public RUse getRuse() {
        return ruse;
    }
    /**
     * @return Returns the sdd.
     */
    public SyncDepD getSdd() {
        return sdd;
    }
    /**
     * @return Returns the sdt.
     */
    public SyncDepT getSdt() {
        return sdt;
    }
}
