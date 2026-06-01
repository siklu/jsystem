/*
 * Copyright 2005-2010 Ignis Software Tools Ltd. All rights reserved.
 */
package jsystem.treeui.actionItems;
import java.util.logging.Logger;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import jsystem.framework.FrameworkOptions;
import jsystem.framework.TestRunnerFrame;
import jsystem.guiMapping.JsystemMapping;
import jsystem.treeui.TestRunner;
import jsystem.treeui.dialog.DialogWithCheckBox;


public class ExitAction extends IgnisAction {
	private static final Logger log = Logger.getLogger(ExitAction.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	private static ExitAction action;
	


	private ExitAction(){
		super();
		putValue(Action.NAME, "Exit");
		putValue(Action.SHORT_DESCRIPTION, JsystemMapping.getInstance().getExitButton());
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
		putValue(Action.ACTION_COMMAND_KEY, "exit");
	}
	
	public static ExitAction getInstance(){
		if (action == null){
			action =  new ExitAction();
		}
		return action;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
			exit();
	}

	public void exit() {
		int save_Ans = 0;
		int ans =0;
		
		ans = DialogWithCheckBox.showConfirmDialogWithCheckBox("Exit Confirmation","Are you sure that you want to exit ?","Auto Save on exit",FrameworkOptions.AUTO_SAVE_NO_CONFIRMATION);
				 
		if (ans == JOptionPane.YES_OPTION) {
			try {
				//TODO: check if the scenario have been change before exit
				save_Ans = SaveScenarioAction.getInstance().saveCurrentScenarioWithConfirmation();
			} catch (Exception e1) {
				log.warning(e1.getMessage());
			}
			if(save_Ans != JOptionPane.CANCEL_OPTION){
				TestRunner.treeView.dispose();
				TestRunner.treeView.getRunner().exit();
			}
		}
	}
}
