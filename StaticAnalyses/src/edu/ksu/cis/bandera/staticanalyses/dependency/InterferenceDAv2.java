
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.staticanalyses.dependency;

import ca.mcgill.sable.soot.SootField;
import ca.mcgill.sable.soot.SootMethod;

import ca.mcgill.sable.soot.jimple.AssignStmt;
import ca.mcgill.sable.soot.jimple.FieldRef;
import ca.mcgill.sable.soot.jimple.InstanceFieldRef;
import ca.mcgill.sable.soot.jimple.InvokeExpr;
import ca.mcgill.sable.soot.jimple.InvokeStmt;
import ca.mcgill.sable.soot.jimple.StaticFieldRef;
import ca.mcgill.sable.soot.jimple.Stmt;
import ca.mcgill.sable.soot.jimple.StmtList;
import ca.mcgill.sable.soot.jimple.Value;
import ca.mcgill.sable.soot.jimple.ValueBox;

import edu.ksu.cis.bandera.staticanalyses.InitializationException;
import edu.ksu.cis.bandera.staticanalyses.flow.Context;
import edu.ksu.cis.bandera.staticanalyses.flow.interfaces.CallGraphInfo;
import edu.ksu.cis.bandera.staticanalyses.flow.interfaces.CallGraphInfo.CallTriple;
import edu.ksu.cis.bandera.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.bandera.staticanalyses.support.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.bandera.staticanalyses.support.Pair;
import edu.ksu.cis.bandera.staticanalyses.support.Pair.PairManager;
import edu.ksu.cis.bandera.staticanalyses.support.WorkBag;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * This class provides interference dependency information.
 * 
 * <p>
 * The dependence inforamtion is stored as follows:  For dependee information, a map from a field to a map that maps a pair
 * comprising of a statement and a method to a collection of similar pair is maintained. For dependent information, a map
 * from a field to a map that maps a method to a map that maps a statement to collection of pairs comprising of a statement
 * and a method is maintained.  The reason the information maps are not identical in structures is due to the form in which
 * the dependent information is required during analysis.  In Jimple, only one field can occur in a statement. Hence, it is
 * sufficient to capture the dependency at statement level.
 * </p>
 * 
 * <p>
 * The algorithm happens in 4 phases. In phase 1, intra-method field dependencies is calculated.  In phase 2, intra-method
 * field dependencies information is propogated from callee to callers.  The direction of propogation is reversed in phase
 * 3. In phase 4, effects of concurrency is considered.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @invariant dependentMap.oclIsKindOf(Map(SootField, Map(Pair(Stmt, SootMethod), Collection(Stmt, SootMethod))))
 * @invariant dependeeMap.oclIsKindOf(Map(SootField, Map(SootMeethod, Map(Stmt, Collection(Stmt, SootMethod))))
 */
public class InterferenceDAv2
  extends DependencyAnalysis {
	/**
	 * This stores the methods that are being processed as the analysis proceeds.
	 */
	private final Collection processing = new HashSet();

	/**
	 * This captures the reaching definitions (may-information) of a fields after the trailer of a basic block.
	 *
	 * @invariant method2DefSiteMap.oclIsKindOf(Map(SootMethod, Map(BasicBlock, Map(SootField, Set(Pair(Stmt,
	 * 			  SootMethod))))))
	 */
	private final Map method2bbDefSiteMap = new HashMap();

	/**
	 * This captures the reaching definitions (may-information) of fields on entering a method.
	 *
	 * @invariant onMethodEntryField2DefSite.oclIsKindOf(Map(SootMethod, Map(SootField, Set(Pair(Stmt, Method))))
	 */
	private final Map onMethodEntryField2DefSite = new HashMap();

	/**
	 * This captures the reaching definitions (may-information) of fields on exiting a method.
	 *
	 * @invariant onMethodExitField2DefSite.oclIsKindOf(Map(SootMethod, Map(SootField, Set(Pair(Stmt, Method))))
	 */
	private final Map onMethodExitField2DefSite = new HashMap();

	/**
	 * This captures the definitions of a field that are may be alive immediately after a call-site.
	 *
	 * @invariant postInvokeStmtField2DefSiteMap.oclIsKindOf(Pair(Stmt, SootMethod), Map(SootField, Set(Pair(Stmt,
	 * 			  SootMethod)))))
	 */
	private final Map postInvokeStmtField2DefSiteMap = new HashMap();

	/**
	 * This captures the definitions of a field that are may be alive just before a call-site.
	 *
	 * @invariant preInvokeStmtField2DefSiteMap.oclIsKindOf(Pair(Stmt, SootMethod), Map(SootField, Set(Pair(Stmt,
	 * 			  SootMethod)))))
	 */
	private final Map preInvokeStmtField2DefSiteMap = new HashMap();

	/**
	 * This provide call graph information about the analyzed system.  This is required by the analysis.
	 */
	private CallGraphInfo callgraph;

	/**
	 * This manages pairs.  This is used to implement <i>flyweight</i> pattern to conserve memory.
	 */
	private PairManager pairMgr;

	/**
	 * Returns the statements on which the given field at the given statement and method depends on.
	 *
	 * @param dependentField of interest.
	 * @param stmtMethodPair is the pair of statement and method in which <code>field</code> occurs.
	 *
	 * @return a colleciton of pairs comprising of a statement and a method.
	 *
	 * @pre dependentField.oclType = SootField
	 * @pre stmtMethodPair.oclIsKindOf(Pair(Stmt, SootMethod))
	 * @post result->forall(o | o.oclIsKindOf(Pair(Stmt, SootMethod))
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis#getDependees(java.lang.Object, java.lang.Object)
	 */
	public Collection getDependees(Object dependentField, Object stmtMethodPair) {
		Collection result = Collections.EMPTY_LIST;
		Map pair2set = getDependeeMapForField((SootField) dependentField);

		if(pair2set != null) {
			Collection set = (Set) pair2set.get(stmtMethodPair);

			if(set != null) {
				result = Collections.unmodifiableCollection(set);
			}
		}
		return result;
	}

	/**
	 * Returns the statements on which the given field at the given statement and method depends on.
	 *
	 * @param dependeeField of interest.
	 * @param stmtMethodPair is the pair of statement and method in which <code>field</code> occurs.
	 *
	 * @return a colleciton of pairs comprising of a statement and a method.
	 *
	 * @pre dependeeField.oclType = SootField
	 * @pre stmtMethodPair.oclIsKindOf(Pair(Stmt, SootMethod))
	 * @post result->forall(o | o.oclIsKindOf(Pair(Stmt, SootMethod))
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis#getDependees(java.lang.Object, java.lang.Object)
	 */
	public Collection getDependents(Object dependeeField, Object stmtMethodPair) {
		Collection result = Collections.EMPTY_LIST;
		Map method2map = getDependentMapForField((SootField) dependeeField);

		if(method2map != null) {
			Map stmt2set = (Map) method2map.get(((Pair) stmtMethodPair).getSecond());

			if(stmt2set != null) {
				Collection set = (Collection) stmt2set.get(((Pair) stmtMethodPair).getFirst());

				if(set != null) {
					result = Collections.unmodifiableCollection(set);
				}
			}
		}
		return result;
	}

	/**
	 * Calculates interference dependence information for the methods provided during initialization.
	 *
	 * @return <code>true</code> as this completes in a single run.
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis#analyze()
	 */
	public boolean analyze() {
		Context context = new Context();

		for(Iterator i = callgraph.getReachableMethods().iterator(); i.hasNext();) {
			SootMethod caller = (SootMethod) i.next();
			context.setRootMethod(caller);
			fixupIntraMethodDependencies(caller, null, false);
		}
		fixupCalleeToCallerOnCallDependencies();
		fixupCallerToCalleeOnCallDependencies();
		fixupInterThreadDependencies();
		return true;
	}

	/**
	 * Resets in the internal data structures.
	 */
	public void reset() {
		super.reset();
		preInvokeStmtField2DefSiteMap.clear();
		postInvokeStmtField2DefSiteMap.clear();
		method2bbDefSiteMap.clear();
		processing.clear();
		onMethodEntryField2DefSite.clear();
	}

	/**
	 * Returns the map for containing dependee information pertaining to the given field.
	 *
	 * @param sf of interest.
	 *
	 * @return the map for containing dependee information pertaining to <code>sf</code>.
	 */
	protected Map getDependeeMapForField(SootField sf) {
		return getDependeXXMapHelper(dependeeMap, sf);
	}

	/**
	 * Returns the map for containing dependent information pertaining to the given field.
	 *
	 * @param sf of interest.
	 *
	 * @return the map for containing dependent information pertaining to <code>sf</code>.
	 */
	protected Map getDependentMapForField(SootField sf) {
		return getDependeXXMapHelper(dependentMap, sf);
	}

	/**
	 * Extracts information provided by the environment via <code>info</code> parameter to {@link #initialize initialize}.
	 *
	 * @throws InitializationException when instances of call graph or pair managing service are not provided.
	 */
	protected void setup() {
		callgraph = (CallGraphInfo) info.get(CallGraphInfo.ID);

		if(callgraph == null) {
			throw new InitializationException(CallGraphInfo.ID + " was not provided in info.");
		}
		pairMgr = (PairManager) info.get(PairManager.NAME);

		if(pairMgr == null) {
			throw new InitializationException(PairManager.NAME + " was not provided in info.");
		}
	}

	/**
	 * Helper method for getDependeXXMap() methods.
	 *
	 * @param map from which to extract the result map.
	 * @param sf is the field of interest.
	 *
	 * @return the map corresponding to the <code>sf</code>.
	 */
	private Map getDependeXXMapHelper(Map map, SootField sf) {
		Map result = (Map) map.get(sf);

		if(result == null) {
			result = new HashMap();
			map.put(sf, result);
		}
		return result;
	}

	/**
	 * Helper method to check if the <code>src</code> is contained in <code>dest</code>.  This method assumes that both the
	 * input maps map a field to a collection.  The containment check is for the mapping as well as the mapped collections.
	 *
	 * @param src is the map to be checked if contained.
	 * @param dest is the map to be checked for containment.
	 *
	 * @return <code>true</code> if all mappings in <code>src</code> exist in <code>dest</code> and all values in each value
	 * 		   in such mapping in <code>src</code> is contained in the value of the corresponding mapping in
	 * 		   <code>dest</code>; <code>false</code>, otherwise.
	 *
	 * @invariant src.oclIsKindOf(Map(SootField, Collection))
	 * @invariant dest.oclIsKindOf(Map(SootField, Collection))
	 * @post result = src.keySet()->forall(o | dest.keySet()->exists(p | p = o)) and src.keySet()->forall(o |
	 * 		 dest.get(o).containsAll(src.get(o)))
	 */
	private boolean containsAll(Map src, Map dest) {
		boolean result = true;

		for(Iterator i = dest.entrySet().iterator(); i.hasNext() && result;) {
			Map.Entry entry = (Map.Entry) i.next();
			SootField field = (SootField) entry.getKey();
			Collection destDefSites = (Collection) entry.getValue();
			Collection srcDefSites = (Collection) src.get(field);

			if(srcDefSites == null) {
				result = false;
			} else {
				result = srcDefSites.containsAll(destDefSites);
			}
		}
		return result;
	}

	/**
	 * Fixes up dependence information across method boundaries by propogating information from callee to caller.
	 */
	private void fixupCalleeToCallerOnCallDependencies() {
		fixupOnCallDependencies(postInvokeStmtField2DefSiteMap, onMethodExitField2DefSite);
	}

	/**
	 * Fixes up dependence information across method boundaries by propogating information from caller to callee.
	 */
	private void fixupCallerToCalleeOnCallDependencies() {
		fixupOnCallDependencies(preInvokeStmtField2DefSiteMap, onMethodEntryField2DefSite);
	}

	/**
	 * Fixes up dependence information considering threads.
	 */
	private void fixupInterThreadDependencies() {
		//TODO: Precision can be improved here by using the info from thread graph, call graph, and escape analysis.
		for(Iterator i = method2stmtGraph.keySet().iterator(); i.hasNext();) {
			SootMethod method = (SootMethod) i.next();
			StmtList sl = getStmtList(method);

			for(ca.mcgill.sable.util.Iterator j = sl.iterator(); j.hasNext();) {
				Stmt stmt = (Stmt) j.next();

				for(ca.mcgill.sable.util.Iterator k = stmt.getUseBoxes().iterator(); k.hasNext();) {
					ValueBox useBox = (ValueBox) k.next();
					Value use = useBox.getValue();

					if(use instanceof StaticFieldRef) {
						SootField field = ((StaticFieldRef) use).getField();
						Map dent = (Map) getDependentMapForField(field).get(method);
						Map dee = getDependeeMapForField(field);
						Collection dentSet = new HashSet();
						Collection deeSet = new HashSet();

						for(Iterator l = dent.values().iterator(); l.hasNext();) {
							dentSet.addAll((Collection) l.next());
						}

						for(Iterator l = dee.values().iterator(); l.hasNext();) {
							deeSet.addAll((Collection) l.next());
						}

						for(Iterator l = dent.keySet().iterator(); l.hasNext();) {
							dent.put(l.next(), dentSet);
						}

						for(Iterator l = dee.keySet().iterator(); l.hasNext();) {
							dee.put(l.next(), deeSet);
						}
					} else if(use instanceof InstanceFieldRef) {
						// This needs to change to provide more precise information.
						SootField field = ((InstanceFieldRef) use).getField();
						Map dent = getDependentMapForField(field);
						Map dee = getDependeeMapForField(field);
						Collection dentSet = new HashSet();
						Collection deeSet = new HashSet();

						for(Iterator l = dent.values().iterator(); l.hasNext();) {
							dentSet.addAll((Collection) l.next());
						}

						for(Iterator l = dee.values().iterator(); l.hasNext();) {
							deeSet.addAll((Collection) l.next());
						}

						for(Iterator l = dent.keySet().iterator(); l.hasNext();) {
							dent.put(l.next(), dentSet);
						}

						for(Iterator l = dee.keySet().iterator(); l.hasNext();) {
							dee.put(l.next(), deeSet);
						}
					}
				}
			}
		}
	}

	/**
	 * Sets up the intra-method data dependency for fields.  This does not consider interference via threads.
	 *
	 * @param method to be processed for data dependency.
	 * @param defSiteMap is the reaching definitons for fields that should be considered as available on entering the method.
	 * @param mergePostInvocationInfo <code>true</code> indicates if the reaching definitions for fields resulting from a
	 * 		  method invoke expression should be merged with that maintained while processing <code>method</code>;
	 * 		  <code>false</code>, otherwise.
	 *
	 * @pre method != null
	 * @post method2bbDefSiteMap.get(method).equals(method2bbDefSiteMap$pre.get(method))  or not
	 * 		 method2bbDefSiteMap.get(method).equals(method2bbDefSiteMap$pre.get(method))
	 * @post onMethodEntryField2DefSite.get(method).equals(onMethodEntryField2DefSite$pre.get(method))  or  not
	 * 		 onMethodEntryField2DefSite.get(method).equals(onMethodEntryField2DefSite$pre.get(method))
	 */
	private void fixupIntraMethodDependencies(SootMethod method, Map defSiteMap, boolean mergePostInvocationInfo) {
		processing.add(method);

		BasicBlockGraph bbGraph = getBasicBlockGraph(method);
		WorkBag workbag = new WorkBag(WorkBag.LIFO);
		Collection processed = new HashSet();

		//This maintains the may reach def sites for fields at the current point of the method that is to be processed.  
		//It is possible that this map may change after processing the point.  
		Map field2DefSite = new HashMap();
		Map bb2DefSite;

		if(!method2bbDefSiteMap.containsKey(method)) {
			bb2DefSite = new HashMap();
			method2bbDefSiteMap.put(method, bb2DefSite);
		} else {
			bb2DefSite = (Map) method2bbDefSiteMap.get(method);
		}

		if(defSiteMap != null) {
			field2DefSite.putAll(defSiteMap);
		}

		Map temp;

		if(onMethodEntryField2DefSite.get(method) == null) {
			temp = new HashMap();
			onMethodEntryField2DefSite.put(method, temp);
		} else {
			temp = (Map) onMethodEntryField2DefSite.get(method);
		}
		merge(field2DefSite, temp);
		workbag.addAllWork(bbGraph.getHeads());

		while(!workbag.isEmpty()) {
			boolean process = false;
			BasicBlock bb = (BasicBlock) workbag.getWork();

			for(Iterator i = bb.getPredsOf().iterator(); i.hasNext();) {
				BasicBlock pred = (BasicBlock) i.next();
				temp = (Map) bb2DefSite.get(pred);

				if(temp != null) {
					merge(temp, field2DefSite);
				}
			}

			Collection stmts = bb.getStmtsOf();

			for(Iterator i = stmts.iterator(); i.hasNext();) {
				Stmt stmt = (Stmt) i.next();
				processUseSites(stmt, method, field2DefSite);

				if(stmt instanceof AssignStmt) {
					AssignStmt aStmt = (AssignStmt) stmt;

					if(aStmt.getLeftOp() instanceof FieldRef) {
						processDefSites(aStmt, method, field2DefSite);
						process |= true;
					}
				}

				if(stmt instanceof InvokeStmt
						|| (stmt instanceof AssignStmt && ((AssignStmt) stmt).getRightOp() instanceof InvokeExpr)) {
					Pair pair = pairMgr.getPair(stmt, method);
					temp = (Map) preInvokeStmtField2DefSiteMap.get(pair);

					if(temp == null) {
						temp = new HashMap();
						preInvokeStmtField2DefSiteMap.put(pair, temp);
					}
					merge(field2DefSite, temp);

					/*
					 * This is kinda hard to track. If processCallees is true the body of the if conditional happens at
					 * the end of processInvokeExprStmt.  Otherwise, it happens only if mergePostInvocationInfo is true.
					 *
					 * This behavior is required as we process the methods twice.  When processing the first time we collect
					 * information at intra- method level.  This happens via the call from analyze() with both processCallees
					 * and mergePostInvocationInfo set to false. While processing for the second time we stabilise information
					 * across method calls. This happens via the call from fixupOnCallDependencies.
					 *
					 */
					if(mergePostInvocationInfo) {
						field2DefSite.putAll((Map) postInvokeStmtField2DefSiteMap.get(pair));
					}
				}
			}

			temp = (Map) bb2DefSite.get(bb);

			if(temp != null) {
				temp.putAll(field2DefSite);
			} else {
				temp = new HashMap(field2DefSite);
				bb2DefSite.put(bb, temp);
			}

			if(process) {
				workbag.addAllWork(bb.getSuccsOf());
			}
			field2DefSite.clear();
		}

		if(onMethodExitField2DefSite.get(method) == null) {
			temp = new HashMap();
			onMethodExitField2DefSite.put(method, temp);
		} else {
			temp = (Map) onMethodEntryField2DefSite.get(method);
		}

		// Merge the information after the trailer of  basic block
		for(Iterator i = bbGraph.getTails().iterator(); i.hasNext();) {
			BasicBlock bb = (BasicBlock) i.next();
			merge((Map) bb2DefSite.get(bb), temp);
		}
		processing.remove(method);
		processed.add(method);
	}

	/**
	 * Helper method to fix on-call dependence information.  It does a fixed-point iteration.  For example in case of callee-
	 * to-caller propogation, the fixed-point terminal condition is if the information at the end of the callee method is
	 * contained in the information set after the callee's call-site in the caller.  In case of callee-to-caller
	 * propogation, it is vice versa.
	 *
	 * @param invokeExprSiteMap is map at (before or after) the invocation site.
	 * @param methodEndPointsMap is map at the ends (entry or exit) of methods.
	 *
	 * @pre invokeExprSiteMap.oclIsKindOf(Map(Pair(Stmt, SootMethod),  Map(SootField, Set(Pair(Stmt, SootMethod)))))
	 * @pre methodEndPointsMap.oclIsKindOf(Map(SootMethod, Map(SootField, Set(Pair(Stmt, SootMethod)))))
	 * @post invokeExprSiteMap.equals(invokeExprSiteMap$pre)
	 * @post methodEndPointsMap.equals(methodEndPointsMap$pre)
	 */
	private void fixupOnCallDependencies(Map invokeExprSiteMap, Map methodEndPointsMap) {
		WorkBag methodList = new WorkBag(WorkBag.FIFO);
		methodList.addAllWork(callgraph.getHeads());

		while(!methodList.isEmpty()) {
			SootMethod method = (SootMethod) methodList.getWork();

			Collection callsites = callgraph.getCallees(method);

			for(Iterator i = callsites.iterator(); i.hasNext();) {
				CallTriple triple = (CallTriple) i.next();
				SootMethod callee = triple.getMethod();
				Map field2DefSite = (Map) invokeExprSiteMap.get(pairMgr.getPair(triple.getStmt(), callee));

				if(!containsAll((Map) methodEndPointsMap.get(callee), field2DefSite)) {
					fixupIntraMethodDependencies(method, field2DefSite, true);
					methodList.addWork(callee);
				}
			}
		}
	}

	/**
	 * Helper method to merge two maps.  It assumes that the maps map a field to a collection.  On merging, the collection in
	 * <code>dest</code> for a key will contains all the objects in the collection in <code>src</code> for the same key.
	 *
	 * @param src is the source to merge from.
	 * @param dest is the destination to merge into.
	 *
	 * @invariant src.oclIsKindOf(Map(SootField, Collection))
	 * @invariant dest.oclIsKindOf(Map(SootField, Collection))
	 * @post result = src.keySet()->forall(o | dest.keySet()->exists(p | p = o)) and src.keySet()->forall(o |
	 * 		 dest.get(o).containsAll(src.get(o)))
	 */
	private void merge(Map src, Map dest) {
		for(Iterator i = src.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			SootField field = (SootField) entry.getKey();
			Collection srcDefSites = (Collection) entry.getValue();
			Collection destDefSites = (Collection) dest.get(field);

			if(destDefSites == null) {
				destDefSites = new HashSet(srcDefSites);
				dest.put(field, destDefSites);
			} else {
				destDefSites.addAll(srcDefSites);
			}
		}
	}

	/**
	 * Process the field def sites in the given statement and method.  This does not generate new dependency information.
	 * However, it will alter <code>field2DefSite</code> map by replacing the old def site with a new def site for static
	 * fields.  For,
	 *
	 * @param stmt which will be explored for field def sites.
	 * @param method in which <code>stmt</code> occurs.
	 * @param field2DefSite is the reaching definition set for fields to be used at <code>stmt</code>.
	 *
	 * @pre stmt.getLeftOp().oclIsKindOf(FieldRef)
	 * @pre field2DefSite.oclIsKindOf(Map(SootField, Collection(Pair(Stmt, Method))))
	 * @post field2DefSite.equals(fieldDefSite$pre) or not field2DefSite.equals(fieldDefSite$pre)
	 */
	private void processDefSites(AssignStmt stmt, SootMethod method, Map field2DefSite) {
		FieldRef value = (FieldRef) stmt.getLeftOp();
		SootField field = value.getField();

		if(value instanceof StaticFieldRef) {
			Collection fieldDefSiteSet = (Collection) field2DefSite.get(field);

			if(fieldDefSiteSet == null) {
				fieldDefSiteSet = new HashSet();
				field2DefSite.put(field, fieldDefSiteSet);
			} else {
				fieldDefSiteSet.clear();
			}
			fieldDefSiteSet.add(pairMgr.getPair(stmt, method));
		} else if(value instanceof InstanceFieldRef) {
			//TODO: Precision can be improved here by using info from thread graph, call graph, and OFA.
			Collection fieldDefSiteSet = (Collection) field2DefSite.get(field);

			/*
			 * This fails for instance fields.  Use symbolic method local analysis to kill information in the set.
			                           if(fieldDefSiteSet == null) {
			                               fieldDefSiteSet = new HashSet();
			                               field2DefSite.put(field, fieldDefSiteSet);
			                           } else {
			                               fieldDefSiteSet.clear();
			                           }
			 */
			fieldDefSiteSet.add(pairMgr.getPair(stmt, method));
		}
	}

	/**
	 * Process a call-site in the given statement and method.  This
	 *
	 * @param stmt containing the call-site.
	 * @param method containing <code>stmt</code>.
	 * @param field2DefSite is a map of fields and their def site at the given call-site.
	 *
	 * @pre field2DefSite.oclIsKindOf(Map(SootField, Set(Pair(Stmt, SootMethod))))
	 * @post field2DefSite.equals(field2DefSite$pre) or not field2DefSite.equals(field2DefSite$pre)
	 */
	private void processInvokeExprStmt(Stmt stmt, SootMethod method, Map field2DefSite) {
		InvokeExpr expr = null;

		if(stmt instanceof InvokeStmt) {
			expr = (InvokeExpr) ((InvokeStmt) stmt).getInvokeExpr();
		} else if(stmt instanceof AssignStmt) {
			expr = (InvokeExpr) ((AssignStmt) stmt).getRightOp();
		}

		Context context = new Context();
		context.setRootMethod(method);

		Collection callees = callgraph.getCallees(expr, context);

		Map fieldMap = new HashMap();

		for(Iterator i = callees.iterator(); i.hasNext();) {
			SootMethod callee = (SootMethod) i.next();
			Map temp = (Map) onMethodExitField2DefSite.get(callee);

			if(temp != null) {
				for(Iterator k = temp.keySet().iterator(); k.hasNext();) {
					Object key = k.next();
					Set set = (Set) temp.get(key);

					if(fieldMap.containsKey(key)) {
						((Collection) fieldMap.get(key)).addAll(set);
					} else {
						Set t = new HashSet();
						t.addAll(set);
						fieldMap.put(key, t);
					}
				}
			}
		}
		field2DefSite.putAll(fieldMap);
		postInvokeStmtField2DefSiteMap.put(pairMgr.getPair(stmt, method), new HashMap(field2DefSite));
	}

	/**
	 * Process the field use sites in the given statement and method.  This can generate new dependency information.  If a
	 * field use site occurs in the given statement then dependence information in both direction is generated.
	 *
	 * @param stmt which will be explored for field use sites.
	 * @param method in which <code>stmt</code> occurs.
	 * @param field2DefSite is the reaching definition set for fields to be used at <code>stmt</code>.
	 *
	 * @post field2DefSite.equals(field2DefSite$pre)
	 * @post field2DefSite.oclIsKindOf(Map(SootField, Collection(Pair(Stmt, Method))))
	 */
	private void processUseSites(Stmt stmt, SootMethod method, Map field2DefSite) {
		ca.mcgill.sable.util.List useBoxes = stmt.getUseBoxes();

		for(ca.mcgill.sable.util.Iterator iter = useBoxes.iterator(); iter.hasNext();) {
			ValueBox vBox = (ValueBox) iter.next();
			Value value = vBox.getValue();

			if(value instanceof StaticFieldRef) {
				//TODO: Precision can be improved here by using info from thread graph and call graph.
				Collection defs;
				SootField field = ((InstanceFieldRef) value).getField();
				Map method2Map = getDependeeMapForField(field);

				if(method2Map == null) {
					method2Map = new HashMap();
					dependeeMap.put(field, method2Map);
					defs = new HashSet();
					method2Map.put(stmt, defs);
				} else {
					defs = (Collection) method2Map.get(stmt);

					if(defs == null) {
						defs = new HashSet();
						method2Map.put(stmt, defs);
					}
				}

				Collection fieldDefSites = (Collection) field2DefSite.get(field);

				if(fieldDefSites != null) {
					defs.addAll(fieldDefSites);

					Map temp = getDependentMapForField(field);

					for(Iterator i = fieldDefSites.iterator(); i.hasNext();) {
						Collection uses = (Collection) temp.get(i.next());

						if(uses != null) {
							uses.add(pairMgr.getPair(stmt, method));
						}
					}
				}
			} else if(value instanceof InstanceFieldRef) {
				//TODO: Precision can be improved here by using info from thread graph, call graph, and OFA.
				Collection defs;
				SootField field = ((InstanceFieldRef) value).getField();
				Map method2Map = getDependeeMapForField(field);

				if(method2Map == null) {
					method2Map = new HashMap();
					dependeeMap.put(field, method2Map);
					defs = new HashSet();
					method2Map.put(stmt, defs);
				} else {
					defs = (Collection) method2Map.get(stmt);

					if(defs == null) {
						defs = new HashSet();
						method2Map.put(stmt, defs);
					}
				}

				Collection fieldDefSites = (Collection) field2DefSite.get(field);

				if(fieldDefSites != null) {
					defs.addAll(fieldDefSites);

					Map temp = getDependentMapForField(field);

					for(Iterator i = fieldDefSites.iterator(); i.hasNext();) {
						Collection uses = (Collection) temp.get(i.next());

						if(uses != null) {
							uses.add(pairMgr.getPair(stmt, method));
						}
					}
				}
			}
		}
	}
}

/*****
 ChangeLog:

$Log$

*****/
