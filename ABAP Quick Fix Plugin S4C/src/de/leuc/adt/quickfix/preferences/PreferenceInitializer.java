package de.leuc.adt.quickfix.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.leuc.adt.quickfix.Activator;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault(PreferenceConstants.ADD_COMMENTS, false);
        store.setDefault(PreferenceConstants.INDENT, 2);
        store.setDefault(PreferenceConstants.COMMENT_OUT, false);
        store.setDefault(PreferenceConstants.COMMENT_TEXT, "\"\">>> Begin of edit ${DATE}");
    }

}
