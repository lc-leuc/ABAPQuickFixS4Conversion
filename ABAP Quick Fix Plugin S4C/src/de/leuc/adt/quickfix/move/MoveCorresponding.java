package de.leuc.adt.quickfix.move;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import com.abapblog.adt.quickfix.assist.syntax.statements.IAssistRegex;
import com.abapblog.adt.quickfix.assist.syntax.statements.StatementAssistRegex;
import com.abapblog.adt.quickfix.assist.syntax.statements.move.MoveExact;

import de.leuc.adt.quickfix.Activator;
import de.leuc.adt.quickfix.QuickFixIcon;
import de.leuc.adt.quickfix.preferences.PreferenceConstants;

public class MoveCorresponding extends StatementAssistRegex implements IAssistRegex {


    private static final String selectPattern =
            //         move-corresponding  struct2   to   struct3.
            //      1                         2              3
            "(?s)(\\s*)move-corresponding\\s+(.*)\\s+to\\s+(.*)";

    private static final String replaceByPattern = "$3 = corresponding #( $2 )";
    /**
     * already contains line break
     */
    private String currentIndent;
    private boolean comments = false;
    private int indent_number = 2;

    public MoveCorresponding() {
        super();

        IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode("org.eclipse.ui.editors");
        Boolean bool = preferences.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS,
                true);
        int tabsno = preferences.getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH, 4);
        System.out.println("preferences are: " + bool + tabsno);
    }

    @Override
    public String getChangedCode() {
        String temp2 = CodeReader.CurrentStatement.getStatement().replaceAll(" ", "");
        String leadingBreaks = temp2.replaceFirst("(?i)(?s)([\\n\\r]*)(\\s*)(move-corresponding)(.*)", "$1");

        String temp3 = CodeReader.CurrentStatement.replaceAllPattern("\r\n\\s*[\r\n]", ""); // remove first line feed
        // characters
        String originalIndentation = temp3.replaceFirst("(?i)(?s)(\\s*)(move-corresponding)(.*)", "$1");
        
        String temp = CodeReader.CurrentStatement.replaceAllPattern("(.*)"+System.lineSeparator()+"(.*)", "$1$2");
        temp = temp.trim().replaceAll(" +", " ").trim().replaceAll("\\R+", ""); // condense spaces, remove line breaks
        
        String comentedOut = getCommentedOutStatement(CodeReader.CurrentStatement.getStatement());
        return leadingBreaks.concat( 
                getCommentPrefix().concat(
                        comentedOut).concat(
                                originalIndentation.concat( 
                                    temp.replaceFirst(getMatchPattern(), getReplacePattern()) )) );
    }

    private String getCommentedOutStatement(String in) {
        if (Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.COMMENT_OUT)) {
            // String ls = in.replaceFirst("(?i)(?s)([\\n\\r]*)(\\s*move-corresponding.*)","$1");
            in = in.replaceFirst("(?i)(?s)([\\n\\r]*)(\\s*move-corresponding.*)", "*$2");
            return in.replaceAll("(\r\n|\n)", "$1" + "*").concat("\n");
        }
        return "";
    }

    @Override
    public String getAssistShortText() {
        return "Replace move-corresponding by corresponding #( ) [AOC 45].";
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
            icon = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/qfs4c16.png")
                    .createImage();
        }
        return icon;
    }

    @Override
    public boolean canAssist() {
        if (CodeReader.CurrentStatement.matchPattern(getMatchPattern()) && !(new MoveExact().canAssist())) {

            // get current indentation for statement, prefix with line break
            String temp = CodeReader.CurrentStatement.replacePattern(getMatchPattern(), "$1");
            //  currentIndent = System.lineSeparator().concat(temp.replaceAll("\\s*\\R", ""));// remove spaces
           

            IPreferenceStore store = Activator.getDefault().getPreferenceStore();
            comments = store.getBoolean(PreferenceConstants.ADD_COMMENTS);
            indent_number = store.getInt(PreferenceConstants.INDENT);

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
        return replaceByPattern;
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

}
