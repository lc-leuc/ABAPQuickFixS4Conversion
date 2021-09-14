package de.leuc.adt.quickfix.preferences;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.leuc.adt.quickfix.Activator;

public class QuickFixPreferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    private IPreferenceStore store;

    public QuickFixPreferences() {
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
        addField(new BooleanFieldEditor(PreferenceConstants.ADD_COMMENTS, "&Add change comment line", leuc_group));
        addField(new BooleanFieldEditor(PreferenceConstants.COMMENT_OUT, "&Comment out original", leuc_group));
        addField(new org.eclipse.jface.preference.IntegerFieldEditor(PreferenceConstants.INDENT,
                "&Indentations in select statements", leuc_group, 2));
        addField(new org.eclipse.jface.preference.StringFieldEditor(PreferenceConstants.COMMENT_TEXT,
                "&Comment text for changes", 70, leuc_group));

        addField(new org.eclipse.jface.preference.ListEditor("deleucList", "List of Order-By", leuc_group) {

            @Override
            protected String[] parseString(String stringList) {
                return stringList.split(" ");
            }

            @Override
            protected String getNewInputObject() {
                MyTitleDialog dialog = new MyTitleDialog(getShell()) {
                };
                dialog.create();
                dialog.createTxt(getShell());
                if (dialog.open() == Window.OK) {
                    return dialog.getText();
                }
                return "";
            }

            @Override
            protected String createList(String[] items) {
                return String.join(" ", new ArrayList<String>(Arrays.asList(items)));
            }
        });
    }

    private class MyTitleDialog extends TitleAreaDialog {
        public MyTitleDialog(Shell shell) {
            super(shell);
        };

        private Text tableNameWidget;
        private String tableName;
        private Text orderByWidget;
        private String orderBy;

        public String getText() {
            return tableName;
        };

        public void createTxt(Composite container) {
            Label lbtFirstName = new Label(container, SWT.NONE);
            lbtFirstName.setText("Order By sequence");

            GridData dataFirstName = new GridData();
            dataFirstName.grabExcessHorizontalSpace = true;
            dataFirstName.horizontalAlignment = GridData.FILL;

            tableNameWidget = new Text(container, SWT.BORDER);
            tableNameWidget.setLayoutData(tableName);
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            Composite container = (Composite) super.createDialogArea(parent);
            GridLayout layout = new GridLayout(2, false);
            layout.marginRight = 5;
            layout.marginLeft = 10;
            container.setLayout(layout);

            Label lblUser = new Label(container, SWT.NONE);
            lblUser.setText("User:");

            tableNameWidget = new Text(container, SWT.BORDER);
            tableNameWidget.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            tableNameWidget.setText(tableName);
            tableNameWidget.addModifyListener(e -> {
                Text textWidget = (Text) e.getSource();
                String widgetText = textWidget.getText();
                tableName = widgetText;
            });
            return container;
        }
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