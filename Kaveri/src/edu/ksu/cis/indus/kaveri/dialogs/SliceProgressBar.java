/*
 * Created on Jan 16, 2005
 *
 *
 */
package edu.ksu.cis.indus.kaveri.dialogs;

import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import edu.ksu.cis.indus.kaveri.KaveriPlugin;

/**
 * @author ganeshan
 * 
 */
public class SliceProgressBar extends ProgressMonitorDialog {
	/**
	 * Name to use for task when normal task name is empty string.
	 */
	private static String DEFAULT_TASKNAME = JFaceResources
			.getString("ProgressMonitorDialog.message"); //$NON-NLS-1$
	/**
	 * Constants for label and monitor size
	 */
	private static int LABEL_DLUS = 21;
	private static int BAR_DLUS = 9;
	private List progressList;
	private Button pausecancelButton;
	final int pausecancelButtonId = 1337;
	
	public SliceProgressBar(Shell shell) {
		super(shell);
	}
	
	/** Create the dialog area.
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		setMessage(DEFAULT_TASKNAME);
		createMessageArea(parent);
		//Only set for backwards compatibility
		taskLabel = messageLabel;
		// progress indicator
		progressIndicator = new ProgressIndicator(parent);
		GridData gd = new GridData();
		gd.heightHint = convertVerticalDLUsToPixels(BAR_DLUS);
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;		
		progressIndicator.setLayoutData(gd);
		// label showing current task
		subTaskLabel = new Label(parent, SWT.LEFT | SWT.WRAP);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = convertVerticalDLUsToPixels(LABEL_DLUS);
		gd.horizontalSpan = 2;
		subTaskLabel.setLayoutData(gd);
		subTaskLabel.setFont(parent.getFont());
		
		progressList = new List(parent, SWT.BORDER |  SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = convertVerticalDLUsToPixels(3 * LABEL_DLUS);
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalSpan = 2;
			
		progressList.setLayoutData(gd);			
		progressList.setFont(parent.getFont());
		
		return parent;			
	}
	
	/**
	 * Set the message in the message label.
	 * @param messageString The string for the new message.
	 */
	private void setMessage(String messageString) {
		//must not set null text in a label
		message = messageString == null ? "" : messageString; //$NON-NLS-1$
		if (messageLabel == null || messageLabel.isDisposed())
			return;
		messageLabel.setText(message);
	}
	
	public void addSliceMessage(String message) {
		if (!progressList.isDisposed()) {
			progressList.add(message);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		pausecancelButton =  createButton(parent, pausecancelButtonId,
				"Pause", false);		
		super.createButtonsForButtonBar(parent);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {		
		super.buttonPressed(buttonId);
		if (buttonId == pausecancelButtonId) {
			pausecancelPressed();
		}
	}
	
	private void pausecancelPressed() {
		final String _currString = pausecancelButton.getText();
		if (_currString.equals("Pause")) {
			pausecancelButton.setText("Continue");
			Display.getCurrent().asyncExec( 
				new Runnable() {
					public void run() {
						KaveriPlugin.getDefault().getIndusConfiguration().
						getEclipseIndusDriver().getSlicer().pause();
					}
				}
			);
			
		} else {
			pausecancelButton.setText("Pause");
			Display.getCurrent().asyncExec( 
					new Runnable() {
						public void run() {
							KaveriPlugin.getDefault().getIndusConfiguration().
							getEclipseIndusDriver().getSlicer().resume();
						}
					}
				);
		}
	}
}
