package de.leuc.adt.quickfix.preferences;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.leuc.adt.quickfix.Activator;

public class OrderByPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

    public class UserEntries {
        private HashSet<OrderByPrefEntry> set = new HashSet<OrderByPrefEntry>();

        public Set<OrderByPrefEntry> getEntries() {
            return set;
        }

        public void add(OrderByPrefEntry entry) {
            set.add(entry);
        }

        public void remove(OrderByPrefEntry entry) {
            set.remove(entry);
        }

        public void clear() {
            set.clear();
        }

    }

    public class EntryDialog extends TitleAreaDialog {
        private String tableMatch = "";
        private String orderBy = "";
        private Text table;
        private Text order;

        public EntryDialog(Shell parentShell, OrderByPrefEntry entry, UserEntries entries) {
            super(parentShell);
            if (entry != null) {
                tableMatch = entry.getTableMatch();
                orderBy = entry.getOrderBy();
            }
            // TODO Auto-generated constructor stub
        }

        public String getTableMatch() {
            return tableMatch;
        }

        public void setTableMatch(String tableMatch) {
            this.tableMatch = tableMatch;
        }

        public String getOrderBy() {
            return orderBy;
        }

        public void setOrderBy(String orderBy) {
            this.orderBy = orderBy;
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            Composite container = (Composite) super.createDialogArea(parent);
            GridLayout layout = new GridLayout(1, false);
            layout.marginRight = 5;
            layout.marginLeft = 10;
            container.setLayout(layout);

            Label lblTable = new Label(container, SWT.LEFT);
            lblTable.setText("Table:");

            table = new Text(container, SWT.BORDER);
            table.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            table.setText(tableMatch);
            table.addModifyListener(e -> {
                Text textWidget = (Text) e.getSource();
                String tableText = textWidget.getText();
                tableMatch = tableText;
            });

            Label lbsOrderBy = new Label(container, SWT.LEFT);
            GridData gridDataOrderByLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false);
            gridDataOrderByLabel.horizontalIndent = 1;
            lbsOrderBy.setLayoutData(gridDataOrderByLabel);
            lbsOrderBy.setText("Order-By:");

            order = new Text(container, SWT.BORDER);
            order.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            order.setText(orderBy);
            order.addModifyListener(e -> {
                Text textWidget = (Text) e.getSource();
                String passwordText = textWidget.getText();
                orderBy = passwordText;
            });
            return container;
        }

    }

    private class TextConstants {

        public static final String Catalog_Entries = "Entries";
        public static final String Remove = "Remove";
        public static final String Edit = "Edit";
        public static final String Add = "Add";
        public static final String Init = "(Re)-Init";

    }

    private TreeViewer viewer;
    private UserEntries entries;
    private OrderByPrefEntry selectedEntry;

    public void init(IWorkbench workbench) {
    }

    @Override
    protected void performDefaults() {
        HashMap<String, String> defaults = new HashMap<String, String>();
        entries.clear();

        defaults.put("wbgt", "doc_type, vbeln, posnr, posnr_sub, gjahr");
        defaults.put("wbhf", "tkonn_from, tposn_from, tposn_sub_from, tkonn_to, tposn_to, tktyp_to");
        defaults.put("wbit", "doc_type, doc_nr, doc_year, item, sub_item");
        defaults.put("wbhd", "tkonn, tposn, tposn_sub");
        defaults.put("ekbe", "ebeln ebelp zekkn vgabe gjahr belnr buzei");
        defaults.put("vbfa", "vbelv, posnv, vbeln, posnn, vbtyp_n");
        defaults.put("eine", "infnr, ekorg, esokz, werks");
        defaults.put("konv", "knumv, kposn, stunr, zaehk ");
        defaults.put("mvke", "matnr, vkorg, vtweg");
        defaults.put("drad", "dokar, doknr, dokvr, doktl, dokob, objky, obzae");
        defaults.put("wbassoc", "tew_type, assoc_step_from, rdoc_nr, rdoc_year, rdoc_bukrs, rposnr, rposnr_sub,"
                + "assoc_step_to, adoc_nr, adoc_year, adoc_bukrs, aposnr, aposnr_sub, rec_base");

        Iterator<String> it = defaults.keySet().iterator();
        while (it.hasNext()) {
            OrderByPrefEntry ue = new OrderByPrefEntry();
            ue = new OrderByPrefEntry();
            String element = it.next();
            ue.setTableMatch(element);
            ue.setOrderBy(defaults.get(element));
            entries.add(ue);
        }

        storePreferences();
        super.performDefaults();
        viewer.refresh();
    }

    @Override
    public boolean performOk() {
        storePreferences();
        return super.performOk();
    }

    private void storePreferences() {
        IEclipsePreferences prefs = getPreferences();
        try {
            String value = new OrderByPrefParser().serialize(entries.getEntries());
            prefs.put(OrderByPrefParser.ADT_ORDERBY_ENTRIES, value);
            prefs.flush();
//            Activator.getDefault().clearCatalogCache();
//            JSONSchemaProcessor.clearCache();
        } catch (Exception e) {
            logException(e);
        }
    }

    public static IEclipsePreferences getPreferences() {
//        IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
        IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(OrderByPrefParser.ADT_ORDERBY_ENTRIES); // $NON-NLS-1$
        return preferences;
    }

    private static void logException(Exception e) {
        IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
        Activator.getDefault().getLog().log(status);
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Group entriesGroup = new Group(composite, SWT.NONE);
        entriesGroup.setText(TextConstants.Catalog_Entries);
        GridLayout gl = new GridLayout(2, false);
        entriesGroup.setLayout(gl);
        entriesGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

        viewer = new TreeViewer(entriesGroup, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        viewer.setContentProvider(new EntriesContentProvider());
        viewer.setLabelProvider(new EntriesLabelProvider());
        entries = new UserEntries();
        entries.getEntries().addAll(OrderByPrefParser.getUserEntries());
        viewer.setInput(entries);
        viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.expandAll();

        Composite buttonComposite = new Composite(entriesGroup, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(1, false));
        buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

        Button addButton = new Button(buttonComposite, SWT.PUSH);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addButton.setText(TextConstants.Add);
        addButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {

            }

            public void widgetSelected(SelectionEvent e) {
//                    EntryDialog dialog = new EntryDialog(getShell(), null, entries);
                EntryDialog dialog = new EntryDialog(getShell(), selectedEntry, entries);// , null, entries);
                int ok = dialog.open();
                if (ok == Window.OK) {
                    String tableMatch = dialog.getTableMatch();
                    if (tableMatch != null) {
                        String orderBy = dialog.getOrderBy();
                        OrderByPrefEntry entry = new OrderByPrefEntry();
                        entry.setOrderBy(orderBy);
                        entry.setTableMatch(tableMatch);
                        entries.add(entry);
                        viewer.refresh();
                    }
                }
            }
        });
        final Button editButton = new Button(buttonComposite, SWT.PUSH);
        editButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        editButton.setText(TextConstants.Edit);
        editButton.setEnabled(false);
        editButton.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                if (selectedEntry == null) {
                    return;
                }
                EntryDialog dialog = new EntryDialog(getShell(), selectedEntry, entries);// new EntryDialog(getShell(),
                                                                                         // selectedEntry, entries);
                int ok = dialog.open();
                if (ok == Window.OK) {
                    String tableMatch = dialog.getTableMatch();
                    if (tableMatch != null) {
                        String orderBy = dialog.getOrderBy();
                        OrderByPrefEntry entry = selectedEntry;
                        entry.setTableMatch(tableMatch);
                        entry.setOrderBy(orderBy);
                        viewer.refresh();
                    }
                }
            }

            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });
        final Button removeButton = new Button(buttonComposite, SWT.PUSH);
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        removeButton.setText(TextConstants.Remove);
        removeButton.setEnabled(false);

        removeButton.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                if (selectedEntry != null) {
                    entries.remove(selectedEntry);
                    viewer.refresh();
                }
            }

            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });

        final Button initButton = new Button(buttonComposite, SWT.PUSH);
        initButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        initButton.setText(TextConstants.Init);
        initButton.setEnabled(true);

        initButton.addMouseListener(new MouseListener() {

            @Override
            public void mouseUp(MouseEvent e) {
                performDefaults();

            }

            @Override
            public void mouseDown(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                // TODO Auto-generated method stub

            }
        });

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                editButton.setEnabled(false);
                removeButton.setEnabled(false);
                selectedEntry = null;
                ISelection selection = event.getSelection();
                if (selection instanceof ITreeSelection) {
                    ITreeSelection treeSelection = (ITreeSelection) selection;
                    Object object = treeSelection.getFirstElement();
                    if (object instanceof OrderByPrefEntry) {
                        selectedEntry = (OrderByPrefEntry) object;
                        editButton.setEnabled(true);
                        removeButton.setEnabled(true);
                    }
                }
            }
        });

        return composite;
    }

    class EntriesContentProvider implements ITreeContentProvider {

        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof UserEntries) {
                Object[] os = ((UserEntries) parentElement).getEntries().toArray();
                Arrays.sort(os);
                return os;
            }
            return new Object[0];
        }

        public Object getParent(Object element) {
            return null;
        }

        public boolean hasChildren(Object element) {
            return element instanceof UserEntries;
        }

        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

    }

    class EntriesLabelProvider extends LabelProvider {

        @Override
        public Image getImage(Object element) {
            return super.getImage(element);
        }

        @Override
        public String getText(Object element) {
            if (element instanceof OrderByPrefEntry) {
                OrderByPrefEntry entry = (OrderByPrefEntry) element;
                String result = entry.getTableMatch();
                return result;
            }
            return super.getText(element);
        }

    }

}
