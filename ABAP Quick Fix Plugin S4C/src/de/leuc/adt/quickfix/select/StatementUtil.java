package de.leuc.adt.quickfix.select;

import de.leuc.adt.quickfix.Activator;
import de.leuc.adt.quickfix.preferences.PreferenceConstants;

/**
 * Common functions used by quickfixes.
 * 
 * @author lc
 *
 */
public class StatementUtil {
    
    /**
     * Provides the original statement as comment, if preferences are set.
     * 
     * @param in - original statement
     * @param indentation - original indentation of select
     * @return the original statement as comment
     */
    public static String getCommentedOutStatement(String in, String indentation) {
        if (Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.COMMENT_OUT)) {
            in = in.replaceFirst("(?i)(?s)([\n\r]*)(\s*select.*)", "*" + indentation + "$2");
            return in.replaceAll("(\r\n|\n)", "$1" + "*").concat(".\n");
        }
        return "";
    }

    /** 
     * Provide pre-defined change statement if set in preferences.
     * If comment starts with quotation mark then keep indentation.
     *  
     * @param indentation - original indentation of select
     * @return the change statement 
     */
    public static String getCommentPrefix(String indentation) {
        if (Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.ADD_COMMENTS)) {
            String comment = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.COMMENT_TEXT);
            if (comment.startsWith("\"")) {comment = indentation + comment;}
            return comment.replace("${DATE}", java.time.LocalDate.now().toString()).concat("\n");
        }
        return "";
    }
    
    /** 
     * For debugging purposes, only.
     * Display the statement by components.
     * 
     * @param statement - given statement 
     * @param matchPattern - given match pattern
     */
    public void display_statement_all(String statement, String matchPattern) {
        display_statement_single(statement, matchPattern, "select");
        display_statement_single(statement, matchPattern, "single");
        display_statement_single(statement, matchPattern, "fields");
        display_statement_single(statement, matchPattern, "from");
        display_statement_single(statement, matchPattern, "table");
        display_statement_single(statement, matchPattern, "into");
        display_statement_single(statement, matchPattern, "variable");
        display_statement_single(statement, matchPattern, "where");
        display_statement_single(statement, matchPattern, "condition");

    }

    private void display_statement_single(String s, String m, String n) {
        System.out.println(n + ": " + s.replaceFirst(m, "${" + n + "}"));

    }
}
