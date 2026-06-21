package de.leuc.adt.quickfix.util;

import org.eclipse.jface.preference.IPreferenceStore;
//import org.eclipse.jface.preference.PreferenceStore;

import de.leuc.adt.quickfix.Activator;
import de.leuc.adt.quickfix.preferences.PreferenceConstants;

/**
 * Common functions used by quickfixes.
 * 
 * @author lc
 *
 */
public class StatementUtil {
    
    private static class LazyUtil {
        static final StatementUtil INSTANCE = new StatementUtil();
    }
    
    /**
     * Plugin preferences.
     */
    private final IPreferenceStore preferences;

    /**
     * @return instance of class
     */
    public static StatementUtil getInstance() {
        return LazyUtil.INSTANCE;
    }

    public StatementUtil() {
        preferences = Activator.getDefault().getPreferenceStore();
//        preferences = new PreferenceStore();
//        preferences.setValue(PreferenceConstants.COMMENT_OUT, false);
//        preferences.setValue(PreferenceConstants.ADD_COMMENTS, false);
//        preferences.setValue(PreferenceConstants.NEW_STYLE, false);
//        preferences.setValue(PreferenceConstants.VDM_REPLACEMENTS_DIR, ".aqs4c");
//        preferences.setValue(PreferenceConstants.VDM_ALWAYS_ALLOW, true);
    }

    /**
     * Provides the original statement as comment, if preferences are set.
     * 
     * @param in          - original statement
     * @param indentation - original indentation of select
     * @return the original statement as comment
     */
    public String getCommentedOutStatement(final String in, final String indentation) {
        String out = "";
        if (preferences.getBoolean(PreferenceConstants.COMMENT_OUT)) {
            out = in.replaceFirst("(?i)(?s)([\n\r]*)(\s*select.*)", "*" + indentation + "$2");
            out = out.replaceAll("(\r\n|\n)", "$1" + "*").concat(".\n");
        }
        return out;
    }


    /**
     * Provide pre-defined change statement if set in preferences. If comment starts
     * with quotation mark then keep indentation.
     * 
     * @param indentation - original indentation of select
     * @return the change statement
     */
    public String getCommentPrefix(final String indentation) {
        String prefix = "";
        if (preferences.getBoolean(PreferenceConstants.ADD_COMMENTS)) {
            String comment = preferences.getString(PreferenceConstants.COMMENT_TEXT);
            if (comment.startsWith("\"")) {
                comment = indentation.concat(comment).concat("\n");
            } else {
                comment = comment.concat("\n");
            }

            prefix = comment.replace("${DATE}", java.time.LocalDate.now().toString());
        }
        return prefix;
    }

    /**
     * @return true if new style is set in preferences
     */
    public boolean isNewStyleSet() {
        return preferences.getBoolean(PreferenceConstants.NEW_STYLE);
    }
    
    /**
     * Provides preferences settings for always allow VDM replacement.
     * 
     * @return preferences setting
     */
    public boolean isAlwaysAllow() {
        return preferences.getBoolean(PreferenceConstants.VDM_ALWAYS_ALLOW);
    }
    

    /**
     * @return directory of additional replacements
     */
    public String getUserReplacementsDir() {
        return preferences.getString(PreferenceConstants.VDM_REPLACEMENTS_DIR);
    }

    /**
     * For debugging purposes, only. Display the statement by components.
     * 
     * @param statement    - given statement
     * @param matchPattern - given match pattern
     */
    public void displayStatementAll(final String statement, final String matchPattern) {
        displayStatementSingle(statement, matchPattern, "select");
        displayStatementSingle(statement, matchPattern, "single");
        displayStatementSingle(statement, matchPattern, "fields");
        displayStatementSingle(statement, matchPattern, "from");
        displayStatementSingle(statement, matchPattern, "table");
        displayStatementSingle(statement, matchPattern, "into");
        displayStatementSingle(statement, matchPattern, "variable");
        displayStatementSingle(statement, matchPattern, "where");
        displayStatementSingle(statement, matchPattern, "condition");

    }

    private void displayStatementSingle(String s, String m, String n) {
        // System.out.println(n + ": " + s.replaceFirst(m, "${" + n + "}"));
    }

}
