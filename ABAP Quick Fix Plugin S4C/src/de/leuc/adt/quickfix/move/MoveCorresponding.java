package de.leuc.adt.quickfix.move;

import org.eclipse.swt.graphics.Image;

import com.abapblog.adt.quickfix.assist.syntax.codeParser.AbapStatement;

import de.leuc.adt.quickfix.Activator;
import de.leuc.adt.quickfix.select.StatementAssistRegexS4C;
import de.leuc.adt.quickfix.util.StatementUtil;

public class MoveCorresponding extends StatementAssistRegexS4C {

    private static final String SELECTPATTERN =
            // move-corresponding struct2 to struct3.
            // 1 2 3
            "(?s)(\\s*)move-corresponding\\s+(.*)\\s+to\\s+(.*)";

    private static final String REPLACEBYPATTERN = "$3 = corresponding #( $2 )";
    /**
     * already contains line break
     */


    public MoveCorresponding() {
        super();
    }

    @Override
    public String getChangedCode() {
        AbapStatement currentStatement = CodeReader.CurrentStatement;
        String originalIndentation = currentStatement.getLeadingCharacters().replaceAll("[\\s\\S]*[\\r\\n]", "");

        String temp = CodeReader.CurrentStatement.replaceAllPattern("(.*)" + System.lineSeparator() + "(.*)", "$1$2");
        temp = temp.trim().replaceAll(" +", " ").trim().replaceAll("\\R+", ""); // condense spaces, remove line breaks

        StatementUtil util = StatementUtil.getInstance();
        String prefix = util.getCommentPrefix(originalIndentation);

        // if preferences are set: produce a commented version of the original text
        String commentedOut = util.getCommentedOutStatement(CodeReader.CurrentStatement.getStatement(), originalIndentation);
        return prefix.concat(commentedOut)
                .concat(originalIndentation.concat(temp.replaceFirst(getMatchPattern(), getReplacePattern())));
    }

    @Override
    public String getAssistShortText() {
        return "Replace move-corresponding by corresponding #( ) [AOC 45].";
    }

    @Override
    public String getAssistLongText() {
        return getChangedCode().replaceAll("\n", "<br/>");
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
        if (CodeReader.CurrentStatement.matchPattern(getMatchPattern())) {
//        if (CodeReader.CurrentStatement.matchPattern(getMatchPattern()) && !(new MoveExact().canAssist())) {
            return true;
        }
        return false;
    }


    @Override
    public String getMatchPattern() {
        return SELECTPATTERN;

    }

    @Override
    public String getReplacePattern() {
        return REPLACEBYPATTERN;
    }


}
