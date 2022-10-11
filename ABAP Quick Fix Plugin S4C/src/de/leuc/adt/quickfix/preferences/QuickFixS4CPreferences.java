package de.leuc.adt.quickfix.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.leuc.adt.quickfix.Activator;

public class QuickFixS4CPreferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private IPreferenceStore store;

	public QuickFixS4CPreferences() {
		super(GRID);
		store = Activator.getDefault().getPreferenceStore();
		setPreferenceStore(store);
		setDescription("Settings for ABAP QuickFix Plugin S4");
	}

	@Override
	public void createFieldEditors() {
		Composite top = new Composite(getFieldEditorParent(), SWT.LEFT);

		// Sets the layout data for the top composite's
		// place in its parent's layout.
		top.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		// Sets the layout for the top composite's
		// children to populate.
		top.setLayout(new GridLayout());

		Group leuc_group = new Group(top, SWT.SHADOW_ETCHED_IN);
		leuc_group.setText("Comments and Indentations.");
		addField(new BooleanFieldEditor(PreferenceConstants.NEW_STYLE, "&Use new style (2021)", leuc_group));
		addField(new BooleanFieldEditor(PreferenceConstants.ADD_COMMENTS, "&Add change comment line", leuc_group));
		addField(new BooleanFieldEditor(PreferenceConstants.COMMENT_OUT, "&Comment out original", leuc_group));
		addField(new org.eclipse.jface.preference.IntegerFieldEditor(PreferenceConstants.INDENT,
				"&Indentations in select statements", leuc_group, 2));
		addField(new org.eclipse.jface.preference.StringFieldEditor(PreferenceConstants.COMMENT_TEXT,
				"&Comment text for changes", 70, leuc_group));

	}

	@Override
	public void init(IWorkbench workbench) {

	}

	@Override
	protected void performApply() {
		super.performApply();
	}

//Apply&Close
	@Override
	public boolean performOk() {

		Boolean ApplyClose = super.performOk();
		return ApplyClose;
	}

}