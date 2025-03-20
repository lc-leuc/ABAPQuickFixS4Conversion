package de.leuc.adt.quickfix.select;

import java.util.regex.Pattern;

import org.eclipse.swt.graphics.Image;

import com.abapblog.adt.quickfix.assist.syntax.codeParser.AbapCodeReader;
import com.abapblog.adt.quickfix.assist.syntax.codeParser.AbapStatement;
import com.abapblog.adt.quickfix.assist.syntax.statements.IAssistRegex;
import com.abapblog.adt.quickfix.assist.syntax.statements.StatementAssistRegex;

import de.leuc.adt.quickfix.Activator;
import de.leuc.adt.quickfix.preferences.OrderByPrefParser;
import de.leuc.adt.quickfix.preferences.PreferenceConstants;

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
public class SelectSingle extends StatementAssistRegex implements IAssistRegex {

    private static final String ORDER_BY_PRIMARY_KEY = " order by primary key";

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
    public static final String selectPattern = "(?i)"
            // + "(?<breaks>[\n\r]*)(?<spaces>\s*)"
            + "(?<select>select)\s+(?<single>single)\s+(?<fields>.*)(?:(?:(?<from> from )(?<table>.*))"
            + "|(?:(?<into> into )(?<variable>.*))|(?:(?<where>where)(?<condition>.*))){3}"; // only one of each

    public static final String targetSelectPatternStart = "${select} ${fields} ${from} ${table} ${where} ${condition}";
    public static final String targetSelectPatternEnd = " ${into} ${variable} up to 1 rows. endselect";
    public static final String modernTargetSelectPatternStart = "${select} ${from} ${table} fields ${fields} ${where} ${condition}";
    public static final String modernTargetSelectPatternEnd = " ${into} ${variable} up to 1 rows. endselect";

    private String currentTable;

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

    @SuppressWarnings("restriction")
    @Override
    public String getChangedCode() {

        AbapStatement currentStatement = CodeReader.CurrentStatement;
        String statement = currentStatement.getStatement();

        // line cleanup, if messed up with comments
        statement = statement.replaceFirst("(?im)[\\s\\S]*^(\\s*)(select)", "$1$2");
//        int beginOfStatement = currentStatement.getBeginOfStatement();
//        int beginOfStatementReplacement = AbapCodeReader.scannerServices
//                .getStatementTokens(AbapCodeReader.document, beginOfStatement).get(0).offset;

        SelectFormat formatter = new SelectFormat(statement.contains("select")); // guess case

        // remove all line feed characters and leading spaces
        String statementOneLine = statement.replaceAll("[\r\n]", "").replaceAll("\s+", " ").trim();
        // if leading2 is empty we neet to determine indentation from leading

        String originalIndentation = currentStatement.getLeadingCharacters().replaceAll("[\\s\\S]*[\\r\\n]", "");

        // remember the current table, in order to determine order-by statement
        currentTable = statementOneLine.replaceFirst(getMatchPattern(), "${table}").replaceFirst("(.*)\s+as\s+.*", "$1")
                .trim();
        // do the actual replacement
        String replacement = statementOneLine.replaceFirst(getMatchPattern(), getReplacePattern());

        // self-defined prefix - from Preferences
        String prefix = StatementUtil.getCommentPrefix(originalIndentation);

        // if preferences are set: produce a commented version of the original text
        String comentedOut = StatementUtil.getCommentedOutStatement(statement, originalIndentation);

        // format
        String newStatement = formatter.format(originalIndentation, replacement, "select");

        // concatenate leading lines with automatic comment (if set in prefs)
        // as well as original statement (as comment, if set in prefs) and the new
        // statement
        String returning = prefix.concat(comentedOut.concat(newStatement));

//        //    Debugging
//        System.out.println("---------------------------------------------------------------------------------------");
//        System.out.println("statement");
//        System.out.println(currentStatement.getStatement());
//        System.out.println("---------------------------------------------------------------------------------------");
//        System.out.println("begin st     " + beginOfStatement);
//        System.out.println("---------------------------------------------------------------------------------------");
//        System.out.println("begin rep    " + beginOfStatementReplacement);
//        System.out.println("---------------------------------------------------------------------------------------");
//        System.out.println("begin start  " + getStartOfReplace());
//        System.out.println("---------------------------------------------------------------------------------------");
//        System.out.println("begin length " + getReplaceLength());
//        System.out.println("---------------------------------------------------------------------------------------");
//        System.out.println("leadingCommentsWorkaround");
//        System.out.println(leadingCommentsWorkaround);
//        System.out.println("---------------------------------------------------------------------------------------");
//        System.out.println("leading w/ workaround");
//        System.out.println(leading);
//        System.out.println("---------------------------------------------------------------------------------------");
//        System.out.println("originalIndentation");
//        System.out.println("|"+originalIndentation+"|");
//        System.out.println("---------------------------------------------------------------------------------------");
//        System.out.println("newStatement");
//        System.out.println(newStatement);
//        System.out.println("---------------------------------------------------------------------------------------");
//        System.out.println("returning");
//        System.out.println(returning);
//        System.out.println("---------------------------------------------------------------------------------------");

        return returning.substring(originalIndentation.length()); // make sure the first line is not indented twice
    }

    @Override
    public String getAssistShortText() {
        return "Replace select single with select up to one rows";
    }

    @Override
    public String getAssistLongText() {
        return "Replace select single with select 'up to 1 rows'. "
                + "In cases that select single might not be unique an ordering of "
                + "results can be enforced by providing the 'order by' statement. "
                + "For tables that feature a new ruuid-style key field after S4/Hana transition, "
                + "a configurable 'order by' sequence is provided in preferences." + "<br/>"
                + getChangedCode().replaceAll("\n", "<br/>");
    }

    private static Image icon;

    @Override
    public Image getAssistIcon() {
        if (icon == null) {
            icon = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/qfs4c16.png").createImage();
        }
        return icon;
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
    public int getStartOfReplace() {
        return CodeReader.CurrentStatement.getBeginOfStatementReplacement();
    }

    @Override
    public int getReplaceLength() {
        return CodeReader.CurrentStatement.getStatementLength();
    }

    @Override
    public String getMatchPattern() {
        return selectPattern;
    }

    @Override
    public String getReplacePattern() {
        StringBuffer temp = new StringBuffer();
        String endPattern = "";
        boolean newStyle = Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.NEW_STYLE);

        if (newStyle) {
            temp.append(modernTargetSelectPatternStart);
            endPattern = modernTargetSelectPatternEnd;
        } else {
            temp.append(targetSelectPatternStart);
            endPattern = targetSelectPatternEnd;
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
        ;
        temp.append(" " + endPattern);
        return temp.toString();

    }

    private String statementBuffer = "";
    private boolean isModernBuffer = false;
//    private boolean isInModernStyle(String statement) {
//        if (statementBuffer.equals(statement)) {
//            return isModernBuffer;
//        } else {
//            
//            statementBuffer = statement;
//        isModernBuffer = statement.replaceFirst("(?im)(.*)into corresponding fields of.*", "$1")
//                .matches("(?im).*(?:(?:(?<from>\\sfrom\\s)(?<table>.*))(?:(?<fields>\\sfields\\s)(?<tle>.*)))");
//        }
//        return isModernBuffer;
//    }
}
