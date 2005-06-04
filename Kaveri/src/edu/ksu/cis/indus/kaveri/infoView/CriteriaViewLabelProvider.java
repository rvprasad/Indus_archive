/*
 * Created on Jun 3, 2005
 *
 *
 */
package edu.ksu.cis.indus.kaveri.infoView;

import edu.ksu.cis.indus.kaveri.preferencedata.Criteria;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author Ganeshan
 *
 * Presents the label for the criteria tab.
 */
public class CriteriaViewLabelProvider extends LabelProvider implements ITableLabelProvider {
    public String getColumnText(Object obj, int index) {
        String _retString = "";
        if (obj instanceof Criteria) {
            final Criteria _c = (Criteria) obj;
            switch (index) {
            case 0:
                _retString = _c.getStrMethodName();
                break;
            case 1:
                _retString = _c.getNLineNo() + "";
                break;
            case 2:
                _retString = _c.getNJimpleIndex() + "";
                break;
            case 3:
                _retString = _c.isBConsiderValue() + "";                
                break;
            }
        }
        return _retString;
    }

    public Image getColumnImage(Object obj, int index) {
        return null;
    }

    public Image getImage(Object obj) {
        return null;
    }
}
