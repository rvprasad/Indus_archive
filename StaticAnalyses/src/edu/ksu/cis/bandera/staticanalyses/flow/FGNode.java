package edu.ksu.cis.bandera.bfa;

import java.util.Collection;


/**
 * FGNode.java
 *
 *
 * Created: Sun Feb 24 08:36:51 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public interface FGNode extends Prototype {

	public void addSucc(FGNode node);

	public void addSuccs(Collection succs);

	public void addValue(Object value);

	public void addValues(Collection values);

	public boolean containsValue(Object o);

	public Collection diffValues(FGNode src);

	public Collection getValues();

	public void onNewSucc(FGNode succ);

	public void onNewSuccs(Collection succs);

	public void onNewValue(Object value);

	public void onNewValues(Collection values);

}// FGNode
