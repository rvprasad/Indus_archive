/*
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

/*
 * Created on May 27, 2004
 *
 *
 */
package edu.ksu.cis.indus.kaveri.preferencedata;

/**
 * This class encapsulates the view.
 * 
 * @author Ganeshan
 */
public class ViewData {
    /**
     * Control dependence.
     */
    private boolean control;

    /**
     * Data dependence.
     */
    private boolean data;

    /**
     * Interference dependence.
     */
    private boolean interference;

    /**
     * Ready dependence.
     */
    private boolean ready;

    /**
     * Synchronization dependence.
     */
    private boolean synchronization;

    /**
     * Set control dependence.
     * 
     * @param setcontrol
     *            The control to set.
     */
    public void setControl(final boolean setcontrol) {
        control = setcontrol;
    }

    /**
     * Control dependence status.
     * 
     * @return Returns the control.
     */
    public boolean isControl() {
        return control;
    }

    /**
     * Set data dependence.
     * 
     * @param setdata
     *            The data to set.
     */
    public void setData(final boolean setdata) {
        data = setdata;
    }

    /**
     * Data dependence status.
     * 
     * @return Returns the data.
     */
    public boolean isData() {
        return data;
    }

    /**
     * Sets Interference dependence.
     * 
     * @param setinterference
     *            The interference to set.
     */
    public void setInterference(final boolean setinterference) {
        interference = setinterference;
    }

    /**
     * Interference dependence status.
     * 
     * @return Returns the interference.
     */
    public boolean isInterference() {
        return interference;
    }

    /**
     * Set ready dependence.
     * 
     * @param setready
     *            The ready to set.
     */
    public void setReady(final boolean setready) {
        ready = setready;
    }

    /**
     * Ready dependence status.
     * 
     * @return Returns the ready.
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * Sets synchronization dependence.
     * 
     * @param setsynchronization
     *            The synchronization to set.
     */
    public void setSynchronization(final boolean setsynchronization) {
        synchronization = setsynchronization;
    }

    /**
     * Synchronization dependence.
     * 
     * @return Returns the synchronization.
     */
    public boolean isSynchronization() {
        return synchronization;
    }

    /**
     * Returns true if the objects are equal.
     * 
     * @param arg0
     *            The object to be compared
     * 
     * @return boolean True if equal
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(final Object arg0) {
        if (arg0 instanceof ViewData) {
            final ViewData _cdata = (ViewData) arg0;
            boolean _result = false;
            _result = (control == _cdata.isControl())
                    & (data == _cdata.isData());
            _result = _result & (ready == _cdata.isReady());
            _result = _result & (interference == _cdata.isInterference());
            _result = _result & (synchronization == _cdata.isSynchronization());
            return _result;
        }
        return super.equals(arg0);
    }

    /**
     * The hash code.
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return super.hashCode();
    }
}