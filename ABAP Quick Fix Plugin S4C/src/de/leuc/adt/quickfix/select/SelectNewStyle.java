package de.leuc.adt.quickfix.select;

import java.util.regex.Pattern;

import com.abapblog.adt.quickfix.assist.syntax.codeParser.AbapStatement;

import de.leuc.adt.quickfix.util.StatementUtil;

/**
 * QuickFix: Applies new formatting rules to select statement.
 * 
 * @author lc
 *
 */
public class SelectNewStyle extends StatementAssistRegexS4C {

    private static final String STATEMENT_START = "select";
    /**
     * Capturing Groups
     * <ul>
     * <li>leading line breaks
     * <li>leading spaces
     * <li>word "single"
     * <li>field list
     * <li>table
     * <li>into-data statement
     * <li>where statement
     * </ul>
     */

    // pattern allows different orders of into, from, where
    public static final String SELECTPATTERN = "(?i)(?<breaks>[\n\r]*)(?<spaces>\\s*)(?<select>select)\\s+(?<fields>.*)\\s+"
            + "(?:(?:(?<from>from)\\s+(?<table>.*))" + "|(?:(?<into>into)(?<variable>.*))"
            + "|(?:(?<where>where)\\s+(?<condition>.*)?)){3}";

    public static final String TARGETSELECTPATTERN = "${select} ${fields} ${from} ${table} ${where} ${condition}"
            + " ${into} ${variable}";
    public static final String MODERNTARGETSELECTPATTERN = "${select} ${from} ${table} fields ${fields} ${where} ${condition}"
            + " ${into} ${variable}";


    public SelectNewStyle() {
        super();

//        // todo: include formating rules
//        IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode("org.eclipse.ui.editors");
//        Boolean bool = preferences.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS,
//                true);
//        int tabsno = preferences.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH, 4);
    }

    public SelectNewStyle(boolean debug) {
        super();
    }

    @Override
    public String getChangedCode() {

        AbapStatement currentStatement = CodeReader.CurrentStatement;
        String statement = currentStatement.getStatement();

        Formatter formatter = new SelectFormat(statement.contains(SELECT_LOWER_CASE)); // guess case

        // remove all line feed characters and leading spaces
        String statementOneLine = statement.replaceAll("[\r\n]", "").trim();

        // replacement only if we want to convert to the new style
        if (StatementUtil.getInstance().isNewStyleSet()) {
            // do the actual replacement
            statementOneLine = statementOneLine.replaceFirst(getMatchPattern(), getReplacePattern());
        }
        String originalIndentation = currentStatement.getLeadingCharacters().replaceAll("[\\s\\S]*[\\r\\n]", "");
        indentationLength = originalIndentation.length();

        // remember the current table, in order to determine order-by statement
//        currentTable = statementOneLine.replaceFirst(getMatchPattern(), "${table}").replaceFirst("(.*)\s+as\s+.*", "$1")
//                .trim();
        
        // do the actual replacement
        String replacement = statementOneLine.replaceFirst(getMatchPattern(), getReplacePattern());

        // self-defined prefix - from Preferences
        StatementUtil util = StatementUtil.getInstance();
        String prefix = util.getCommentPrefix(originalIndentation);

        // if preferences are set: produce a commented version of the original text
        String commentedOut = util.getCommentedOutStatement(statement, originalIndentation);

        // format
        String newStatement = formatter.format(originalIndentation, replacement, STATEMENT_START );

        // concatenate leading lines with automatic comment (if set in prefs)
        // as well as original statement (as comment, if set in prefs) and the new
        // statement
        return prefix.concat(commentedOut).concat(newStatement);
    }

    @Override
    public String getAssistShortText() {
        return "Select w/ endselect or w/ into table: replace old style SQL with new style.";
    }

    @Override
    public String getAssistLongText() {

        return "Replace old style SQL with new style for select/endselect.\n New style includes @ for variables and commas for lists.";
    }


    @Override
    public boolean canAssist() {
        String currentStatement = CodeReader.CurrentStatement.getStatement();
        // do not propose if select single or already in new SQL style
        if (currentStatement.toLowerCase().contains("\ssingle\s") || currentStatement.contains("@")) {
            return false;
        }
        return Pattern.compile(getMatchPattern()).matcher(currentStatement.replaceAll("[\r\n]", "").trim()).find();
    }

    @Override
    public String getMatchPattern() {
        return SELECTPATTERN;
    }

    @Override
    public String getReplacePattern() {
        if (StatementUtil.getInstance().isNewStyleSet()) {
            return MODERNTARGETSELECTPATTERN;
        }
        return TARGETSELECTPATTERN;
    }

}
