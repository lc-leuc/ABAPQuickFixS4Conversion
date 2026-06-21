package de.leuc.adt.quickfix.select;

import java.util.regex.Pattern;

import com.abapblog.adt.quickfix.assist.syntax.codeParser.AbapStatement;

import de.leuc.adt.quickfix.preferences.OrderByPrefParser;
import de.leuc.adt.quickfix.util.StatementUtil;

/**
 * QuickFix: Replaces a select single statement with a select statement with
 * <code>up to 1 rows</code> and <code>order-by</code> statement.
 * 
 * If a oder-by sequence is provided for a given table in the preferences, then
 * the sequence is used, otherwise <code>primary key</code>.
 * 
 * @author lc
 *
 */
public class SelectSingle extends StatementAssistRegexS4C {

    private static final String ORDER_BY_PRIMARY_KEY = " order by primary key";

    private static final String STATEMENT_START = "select";

    /**
     * Capturing Groups
     * <ul>
     * <li>leading line breaks
     * <li>leading spaces
     * <li>select
     * <li>single (word)
     * <li>field list
     * <li>from (word)
     * <li>table
     * <li>into (word)
     * <li>variable
     * <li>where (word)
     * <li>condition
     * </ul>
     */

    // dot is not part of the statement
    // allowing for different sort orders of into, from and where
    public static final String SELECTPATTERN = 
            // select single
            "(?i)"
            + "(?<select>select)\s+(?<single>single)\s+(?<fields>.*)(?:(?:(?<from> from )(?<table>.*))"
            + "|(?:(?<into> into )(?<variable>.*))|(?:(?<where>where)(?<condition>.*))){3}";
            
            

    public static final String TARGETSELECTPATTERNSTART = "${select} ${fields} ${from} ${table} ${where} ${condition}";
    public static final String TARGETSELECTPATTERNEND = " ${into} ${variable} up to 1 rows. endselect";
    public static final String MODERNTARGETSELECTPATTERNSTART = "${select} ${from} ${table} fields ${fields} ${where} ${condition}";
    public static final String MODERNTARGETSELECTPATTERNEND = " ${into} ${variable} up to 1 rows. endselect";

    protected String currentTable;

    /**
     * already contains line break
     */
//    private int indent_number = 2;// currently not in use

    public SelectSingle() {
        super();

//        // todo: include formating rules
//        IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode("org.eclipse.ui.editors");
//        Boolean bool = preferences.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS,
//                true);
//        int tabsno = preferences.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH, 4);
    }

    @Override
    public String getChangedCode() {

        AbapStatement currentStatement = CodeReader.CurrentStatement;
        
        assert currentStatement != null;
        
        String statement = currentStatement.getStatement();

        Formatter formatter = new SelectFormat(statement.contains(SELECT_LOWER_CASE)); // guess case

        // line cleanup, if messed up with comments
        statement = statement.replaceFirst("(?im)[\\s\\S]*^(\\s*)(select)", "$1$2");
    
        // remove all line feed characters and leading spaces
        String statementOneLine = statement.replaceAll("[\r\n]", "").replaceAll("\s+", " ").trim();

        
        
        
        
        
        String originalIndentation = currentStatement.getLeadingCharacters().replaceAll("[\\s\\S]*[\\r\\n]", "");
        indentationLength = originalIndentation.length();

        // remember the current table, in order to determine order-by statement
        currentTable = statementOneLine.replaceFirst(getMatchPattern(), "${table}").replaceFirst("(.*)\s+as\s+.*", "$1")
                .trim();
        
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
        return "Replace select single with select up to one rows";
    }

    @Override
    public String getAssistLongText() {
        return "Replace select single with select 'up to 1 rows'. <p>"
                + "In cases that select single might not be unique an ordering of "
                + "results can be enforced by providing the 'order by' statement. </br>"
                + "For tables that feature a new ruuid-style key field after S4/Hana transition, "
                + "a configurable 'order by' sequence can be adjusted in preferences."
                + "<p>----<p>" + toPre(getChangedCode().replaceAll("\n", "<br/>"));
    }

    @Override
    public boolean canAssist() {
        String currentStatement = CodeReader.CurrentStatement.getStatement().replaceAll("[\r\n]", "")
                .replaceAll("\s+", " ").trim();
        if (currentStatement.contains("join")) {
            return false;
        } else {
            if (Pattern.compile(getMatchPattern()).matcher(currentStatement).find()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getMatchPattern() {
        return SELECTPATTERN;
    }

    @Override
    public String getReplacePattern() {
        StringBuffer temp = new StringBuffer();
        String endPattern = "";

        if (StatementUtil.getInstance().isNewStyleSet()) {
            temp.append(MODERNTARGETSELECTPATTERNSTART);
            endPattern = MODERNTARGETSELECTPATTERNEND;
        } else {
            temp.append(TARGETSELECTPATTERNSTART);
            endPattern = TARGETSELECTPATTERNEND;
        }
        // order by depends on table
        // several tables feature uuids as keys - use old key fields to order lines
        // old keys are defined individually in the S4C preferences page
        // order by primary key otherwise
        String orderBy = OrderByPrefParser.getOrderBy(currentTable);

        if (orderBy != null) {
            temp.append(" order by " + orderBy);
        } else {
            temp.append(ORDER_BY_PRIMARY_KEY);
        }
        
        temp.append(" " + endPattern);
        return temp.toString();

    }
    
}
