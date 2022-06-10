package de.leuc.adt.quickfix.select;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import com.abapblog.adt.quickfix.assist.syntax.statements.IAssistRegex;
import com.abapblog.adt.quickfix.assist.syntax.statements.StatementAssistRegex;

import de.leuc.adt.quickfix.Activator;
import de.leuc.adt.quickfix.preferences.OrderByPrefParser;
import de.leuc.adt.quickfix.preferences.PreferenceConstants;

public class SelectSingle extends StatementAssistRegex implements IAssistRegex {

    private static final String ORDER_BY_PRIMARY_KEY = "order by primary key.";

    /**
     * Capturing Groups * 1 - leading line breaks * 2 - leading spaces * 3 - word
     * "single" * 4 - field list * 5 - table * 6 - into-data statement * 7 - where
     * statement
     */
    private static final String selectPattern =
            // dot is not part of the statement
            // select single * from wbhk into @data(result) where tkonn = ''
            // 1 2 3 4 5 6 7
            "(?i)([\n\r]*)(\\s*)select\\s+(single)\\s+(.*)\\s+from\\s+(.*)\\s+into\\s+(.*)\\s+where\\s+(.*)";
//             "^(\\s*)select\\s+(single)\\s+(.*)\\s+from\\s+(.*)\\s+into\\s+(.*)\\s+where\\s+(.*)";

    private static final String targetSelectPatternStart = "select $4 from $5 into $6 up to 1 rows where $7 ";
    private static final String targetSelectPatternEnd = "endselect";
    private static final String modernTargetSelectPatternStart = "select from $5 fields $4 where $7";
    private static final String modernTargetSelectPatternEnd = " into $6 up to 1 rows. endselect";

    private String currentTable;
    /**
     * already contains line break
     */
    private String leadingBreaks = "";
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
        String temp2 = CodeReader.CurrentStatement.getStatement().replaceAll(" ", "");

        leadingBreaks = temp2.replaceFirst("(?i)(?s)([\\n\\r]*)(\\s*)(select)(.*)", "$1");

        String temp = CodeReader.CurrentStatement.replaceAllPattern("\r\n\\s*[\r\n]", ""); // remove first line feed
                                                                                           // characters
        String originalIndentation = temp.replaceFirst("(?i)(?s)(\\s*)(select)(.*)", "$1").replaceAll("[\r\n]", "");

        // line breaks are added automatically with the indentation prefix
        String comentedOut = getCommentedOutStatement(temp);

        temp = temp.replaceAll("[\r\n]", ""); // remove all line feed characters
        currentTable = temp.replaceFirst(getMatchPattern(), "$5");

        String m = getMatchPattern();
        String r = getReplacePattern();
        String newString = temp.replaceFirst(m, r);
        String[] s = split(newString.replaceAll("\\s\\s*", " ")); // remove multiple spaces
        String statement = "";
        for (String line : s) {
            statement += format(line, originalIndentation);
        }

        String concat = leadingBreaks.concat(getCommentPrefix().concat(comentedOut.concat(statement)));
        return concat;
    }

    private String getCommentedOutStatement(String in) {
        if (Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.COMMENT_OUT)) {
            // String ls = in.replaceFirst("(?i)(?s)([\\n\\r]*)(\\s*select.*)","$1");
            in = in.replaceFirst("(?i)(?s)([\\n\\r]*)(\\s*select.*)", "*$2");
            return in.replaceAll("(\r\n|\n)", "$1" + "*").concat("\n");
        }
        return "";
    }

    @Override
    public String getAssistShortText() {
        return "Replace select single with select up to one rows";
    }

    @Override
    public String getAssistLongText() {
        // TODO Auto-generated method stub
        return null;
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
        if (Pattern.compile(getMatchPattern()).matcher(currentStatement).find()) {// && !(new MoveExact().canAssist()))
                                                                                  // {
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
            temp.append("order by " + orderBy);
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

    private String getPrefix() {
        return String.format("%" + indent_number + "s", "");
    }

    public static String[] split(String in) {
        String[] r = new String[] {};
        String from = "(?i)(?=from )";
        r = in.split(from + "|(?=into )" + "|(?=up\\sto )" + "|(?=where )" + "|(?=and )" + "|(?=or )" + "|(?=endselect)"
                + "|(?=order )" + "|(?=group )" + "|(?=fields )");
        return r;
    }

   
    public static String format(String in, String originalIndentation) {
        if (in.startsWith("select ")) {
            String selection = in.replaceFirst("select\\s+(.*)", "$1").trim();
            if (selection.contains(" ") && !selection.contains(",")) {
                in = "select ".concat(selection.replaceAll("\\s+", ", "));
            }
            return originalIndentation + in.trim() + "\n";
        } else if (in.startsWith("fields ")) {
            String selection = in.replaceFirst("fields\\s+(.*)", "$1").trim();
            if (selection.contains(" ") && !selection.contains(",")) {
                in = "fields ".concat(selection.replaceAll("\\s+", ", "));
            }
            return originalIndentation + "  " + in .trim()+ "\n";
        } else if (in.startsWith("where ")) {
            return originalIndentation + "  " + adaptNewStyle(in).trim() + "\n";
        } else if (in.startsWith("and ")) {
            return originalIndentation + "    " + adaptNewStyle(in).trim() + "\n";
        } else if (in.startsWith("or ")) {
            return originalIndentation + "     " + adaptNewStyle(in).trim() + "\n";
        } else if (in.startsWith("into ")) {

            return originalIndentation + "  " + adaptInto(in).trim() + "\n";
        } else if (in.startsWith("endselect")) {
            return originalIndentation + in.trim();
        } else {
            return originalIndentation + "  " + in.trim() + "\n";
        }

    }

    private static String adaptInto(String in) {
        // replace into (tkonn, tposn) with into (@tkonn, @tposn)
        return in.replaceAll("( \\(|[ ,])([a-zA-Z_])", "$1@$2");
    }

    private static String adaptNewStyle(String in) {
        // replace where tkonn = tkonn with where tkonn = @tkonn
        // (no duplicate spaces)                where   tkonn eq    tkonn
        String replaceFirst = in.replaceFirst("([a-z]*) (.*) (..?) ([a-zA-Z_])", "$1 $2 $3 @$4");
        return replaceFirst;
    }
}
