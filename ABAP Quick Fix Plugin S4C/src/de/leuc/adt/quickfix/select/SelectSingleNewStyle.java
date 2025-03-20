package de.leuc.adt.quickfix.select;

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

import de.leuc.adt.quickfix.Activator;
import de.leuc.adt.quickfix.preferences.PreferenceConstants;

/**
 * QuickFix: Applies new formatting rules to select statement.
 * 
 * @author lc
 *
 */
public class SelectSingleNewStyle extends StatementAssistRegex implements IAssistRegex {

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
    // pattern allows various orders of into, from, where
    private static final String selectPattern = // vvvvvvvvvvvvvvvvvvvvv
            "(?i)(?<breaks>[\n\r]*)(?<spaces>\s*)(?<select>select)\s*(?<single>single)\s+(?<fields>.*)\s+"
                    + "(?:(?:(?<from>from)\s+(?<table>.*))" + "|(?:(?<into>into)(?<variable>.*))"
                    + "|(?:(?<where>where)\s+(?<condition>.*)?)){3}";
    private static final String modernTargetSelectPattern = "${select} ${single} ${from} ${table} fields ${fields} ${where} ${condition}"
            + " ${into} ${variable}";

    /**
     * already contains line break
     */
    private String leadingBreaks = "";
    private boolean comments = false;
    private int indent_number = 2;

    public SelectSingleNewStyle() {
        super();

        // todo: include formating rules
        IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode("org.eclipse.ui.editors");
        Boolean bool = preferences.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS,
                true);
        int tabsno = preferences.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH, 4);

    }

    @Override
    public String getChangedCode() {

        AbapStatement currentStatement = CodeReader.CurrentStatement;
        String statement = currentStatement.getStatement();

        int beginOfStatement = currentStatement.getBeginOfStatement();
        int beginOfStatementReplacement = AbapCodeReader.scannerServices
                .getStatementTokens(AbapCodeReader.document, beginOfStatement).get(0).offset;

        SelectFormat formatter = new SelectFormat(statement.contains("select")); // guess case

        // remove all line feed characters and leading spaces
        String statementOneLine = statement.replaceAll("[\r\n]", "").trim();

        // replacement only if we want to convert to the new style
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        if (store.getBoolean(PreferenceConstants.NEW_STYLE)) {
            // do the actual replacement
            statementOneLine = statementOneLine.replaceFirst(getMatchPattern(), getReplacePattern());
        }

//        // we need to remember the indentation -- remove everything until the last line
//        String leading = AbapCodeReader.getCode().substring(beginOfStatement, beginOfStatementReplacement);
//        String originalIndentation = leading.replaceAll(".*[\r\n]", "");
//        leading = leading.substring(0, leading.length() - originalIndentation.length());
        String originalIndentation = currentStatement.getLeadingCharacters().replaceAll("[\\s\\S]*[\\r\\n]", "");
        ;

        // if preferences are set: produce a commented version of the original text
        String comentedOut = StatementUtil.getCommentedOutStatement(statement, originalIndentation);

        // format
        String newStatement = formatter.format(originalIndentation, statementOneLine, "select single");

        // concatenate leading lines with automatic comment (if set in prefs)
        // as well as original statement (as comment, if set in prefs) and the new
        // statement
        String prefix = StatementUtil.getCommentPrefix(originalIndentation);

        return prefix.concat(comentedOut).concat(newStatement).substring(originalIndentation.length());
    }

    @Override
    public String getAssistShortText() {
        return "Select single: replace old style SQL with new style.";
    }

    @Override
    public String getAssistLongText() {

        return "Replace old style SQL with new style for select single.\n New style includes @ for variables and commas for lists.";
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
        // do not propose if already in new SQL style
        if (currentStatement.contains("@")) {
            // return false;
        }
        if (Pattern.compile(getMatchPattern()).matcher(currentStatement.replaceAll("[\r\n]", "").trim()).find()) {// &&
                                                                                                                  // !(new
                                                                                                                  // MoveExact().canAssist()))
            // {
            // table name to decide on order by clause

            IPreferenceStore store = Activator.getDefault().getPreferenceStore();
            comments = store.getBoolean(PreferenceConstants.ADD_COMMENTS);
            indent_number = store.getInt(PreferenceConstants.INDENT);
            // System.out.println("preferences are: " + comments + " " + indent_number);

            return true;
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
        return modernTargetSelectPattern;

    }

}
