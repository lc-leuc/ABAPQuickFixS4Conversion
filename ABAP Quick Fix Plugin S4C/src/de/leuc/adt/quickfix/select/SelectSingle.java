package de.leuc.adt.quickfix.select;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import com.abapblog.adt.quickfix.assist.syntax.codeParser.AbapCodeReader;
import com.abapblog.adt.quickfix.assist.syntax.codeParser.AbapStatement;
import com.abapblog.adt.quickfix.assist.syntax.statements.IAssistRegex;
import com.abapblog.adt.quickfix.assist.syntax.statements.StatementAssistRegex;
import com.sap.adt.tools.abapsource.ui.sources.IAbapSourceScannerServices.Token;

import de.leuc.adt.quickfix.Activator;
import de.leuc.adt.quickfix.preferences.OrderByPrefParser;
import de.leuc.adt.quickfix.preferences.PreferenceConstants;

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
//    public static final String selectPattern =
//            // select single * from wbhk into @data(result) where tkonn = ''
//                    "(?i)(?<breaks>[\n\r]*)(?<spaces>\\s*)(?<select>select)" 
//                    + "\\s+(?<single>single)\\s+(?<fields>.*)"
//                    + "\\s+(?<from>from)\\s+(?<table>.*)"
//                    + "\\s+(?<into>into)\\s+(?<variable>.*)"
//                    + "\\s+(?<where>where)\\s+(?<condition>.*)";
    // allowing for different sort orders of into, from and where
    public static final String selectPattern = "(?i)"
            // + "(?<breaks>[\n\r]*)(?<spaces>\s*)"
            + "(?<select>select)\s+(?<single>single)\s+(?<fields>.*)" + "(?:(?:(?<from>from)(?<table>.*))"
            + "|(?:(?<into>into)(?<variable>.*))" + "|(?:(?<where>where)(?<condition>.*))){3}"; // only one of each

    public static final String targetSelectPatternStart = "${select} ${fields} ${from} ${table} ${into} ${variable} up to 1 rows ${where} ${condition}";
    public static final String targetSelectPatternEnd = "endselect";
    public static final String modernTargetSelectPatternStart = "${select} ${from} ${table} fields ${fields} ${where} ${condition}";
    public static final String modernTargetSelectPatternEnd = " ${into} ${variable} up to 1 rows. endselect";

    private String currentTable;
    /**
     * already contains line break
     */
    private boolean comments = false;
    private int indent_number = 2;

    public SelectSingle() {
        super();

        // todo: include formating rules
        IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode("org.eclipse.ui.editors");
        Boolean bool = preferences.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS,
                true);
        int tabsno = preferences.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH, 4);

        // System.out.println("preferences are: " + bool + tabsno);
    }

    @Override
    public String getChangedCode() {
//    	 String code = CodeReader.getCode();

        AbapStatement currentStatement = CodeReader.CurrentStatement;
        String statement = currentStatement.getStatement();
        String origin = statement;
        int beginOfStatement = currentStatement.getBeginOfStatement();
        List<Token> statementTokens = AbapCodeReader.scannerServices.getStatementTokens(AbapCodeReader.document,
                beginOfStatement);
        int beginOfStatementReplacement = statementTokens.get(0).offset;

//        // determine comments preceding the statement
//        String initialComment = "";//statement.replaceFirst("(?i)(?s)((?:\r|\n|\s*\\\"|^\\*).*\n)(\s*select\s.*)", "$1");
//        // determine statement without preceding comments
//        statement = statement.replaceFirst("(?i)(?s)((?:\r|\n|\s*\\\"|^\\*).*\n)(\s*select\s.*)", "$2");

        // statement = statement.replaceAll("\r\n\\s*[\r\n]", "");
        // wee need to remember the indentation
        String originalIndentation = "";// statement.replaceFirst("(?i)(?s)(\s*)(select)(.*)", "$1");

        SelectFormat formatter = new SelectFormat(statement.contains("select")); // guess case

        // if preferences are set: produce a commented version of the original text

        // remove all line feed characters and leading spaces
        statement = statement.replaceAll("[\r\n]", "").trim();

        System.out.println("-----------------------------");
        System.out.println("begin st     " + beginOfStatement);
        System.out.println("begin rep    " + beginOfStatementReplacement);
        System.out.println("begin start  " + getStartOfReplace());
        System.out.println("begin length " + getReplaceLength());
        display_statement_all(statement);
        // remember the current table, in order to determine order-by statement
        currentTable = statement.replaceFirst(getMatchPattern(), "${table}").replaceFirst("(.*)\s+as\s+.*", "$1")
                .trim();

        // do the actual replacement
        String replacement = statement.replaceFirst(getMatchPattern(), getReplacePattern());

        // concatenate leading breaks with automatic comment (if set in prefs)
        // as well as original statement (as comment if set in prefs) and new statement
        String leading = AbapCodeReader.getCode().substring(beginOfStatement, beginOfStatementReplacement);
        originalIndentation = leading.replaceAll(".*[\r\n]", "");
        String comentedOut = getCommentedOutStatement(origin, originalIndentation);

        // format
        String newStatement = formatter.format(originalIndentation, replacement, "select");
        return leading + getCommentPrefix().concat(comentedOut).concat(newStatement);
    }

    private void display_statement_all(String statement) {
        display_statement_single(statement, "select");
        display_statement_single(statement, "single");
        display_statement_single(statement, "fields");
        display_statement_single(statement, "from");
        display_statement_single(statement, "table");
        display_statement_single(statement, "into");
        display_statement_single(statement, "variable");
        display_statement_single(statement, "where");
        display_statement_single(statement, "condition");

    }

    private void display_statement_single(String s, String n) {
        System.out.println(n + ": " + s.replaceFirst(getMatchPattern(), "${" + n + "}"));

    }

    private String getCommentedOutStatement(String in, String indent) {
        if (Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.COMMENT_OUT)) {
            in = in.replaceFirst("(?i)(?s)([\n\r]*)(\s*select.*)", "*" + indent + "$2");
            return in.replaceAll("(\r\n|\n)", "$1" + "*").concat(".\n".concat(indent));
        }
        return "";
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
                + "a configurable 'order by' sequence is provided in preferences.";
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
        String currentStatement = CodeReader.CurrentStatement.getStatement();
        // table name to decide on order by clause
        if (Pattern.compile(getMatchPattern()).matcher(currentStatement.replaceAll("[\r\n]", "").trim()).find()) {
            // table name to decide on order by clause

            IPreferenceStore store = Activator.getDefault().getPreferenceStore();
            comments = store.getBoolean(PreferenceConstants.ADD_COMMENTS);
            indent_number = store.getInt(PreferenceConstants.INDENT);
            System.out.println("local preferences are: " + comments + " " + indent_number);

            return true;
        }
        return false;
    }

    @Override
    public int getStartOfReplace() {
        return CodeReader.CurrentStatement.getBeginOfStatement();
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
        if (!newStyle) {
            temp.append(".");
        }
        ;
        temp.append(" " + endPattern);
        return temp.toString();

    }

    private String getCommentPrefix() {
        if (comments) {
            String comment = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.COMMENT_TEXT);
            return comment.replace("${DATE}", java.time.LocalDateTime.now().toString()).concat("\n");
        }
        return "";
    }

}
