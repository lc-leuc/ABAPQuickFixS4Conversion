package de.leuc.adt.quickfix.preferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

//import de.leuc.adt.quickfix.Activator;

public class EntryParser {

    private static final String ENTRIES_SEPARATOR = "\\|";
    private static final String INNER_SEPERATOR = "#";
    public static final String ADT_ORDERBY_ENTRIES = "de.leuc.adt.quickfixes.orderby";
    private static HashMap<String, String> map;

    public String serialize(Set<UserEntry> entries) {
        StringBuffer sb = new StringBuffer();
        boolean not_first = false;
        for (UserEntry userEntry : entries) {
            if (not_first) {
                sb.append(ENTRIES_SEPARATOR);
            }
            sb.append(userEntry.getTableMatch());
            sb.append(INNER_SEPERATOR);
            sb.append(userEntry.getOrderBy());
            not_first = true;
        }

        return sb.toString();
    }

    public static Collection<? extends UserEntry> getUserEntries() {
//        String entriesString = Activator.getDefault().getPreferenceStore().getString(ADT_ORDERBY_ENTRIES);
        IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode("de.leuc.adt.quickfixes.orderby"); //$NON-NLS-1$
        String entriesString = preferences.get(ADT_ORDERBY_ENTRIES, "");
        String[] entryStrings = entriesString.split(ENTRIES_SEPARATOR);
        ArrayList<UserEntry> list = new ArrayList<UserEntry>();
        map = new HashMap<String, String>();
        if (entryStrings.length > 0) {
            for (String entryString : entryStrings) {
                String[] entry = entryString.split(INNER_SEPERATOR);
                if (entry.length == 2) {
                    UserEntry userEntry = new UserEntry();
                    userEntry.setTableMatch(entry[0]);
                    userEntry.setOrderBy(entry[1]);
                    map.put(entry[0], entry[1]);
                    list.add(userEntry);
                }
            }
        }
        return list;
    }
    
    public static String getOrderBy(String table) {
      if ( map == null ) {
          getUserEntries();
      }
      if (map.containsKey(table)) {          
          return map.get(table);
      }
      return null;
    }

}
