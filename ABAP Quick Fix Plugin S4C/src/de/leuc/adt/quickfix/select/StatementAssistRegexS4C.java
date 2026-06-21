package de.leuc.adt.quickfix.select;

import org.eclipse.swt.graphics.Image;

import com.abapblog.adt.quickfix.assist.syntax.statements.StatementAssistRegex;

import de.leuc.adt.quickfix.Activator;

public abstract class StatementAssistRegexS4C extends StatementAssistRegex {
    protected static final String SELECT_LOWER_CASE = "select";
    protected int indentationLength = 0;
    private static Image icon;


    @Override
    public int getStartOfReplace() {
         int beginOfStatementReplacement = CodeReader.CurrentStatement.getBeginOfStatementReplacement();//CodeReader.CurrentStatement.getBeginOfStatement();
         return beginOfStatementReplacement - indentationLength;
    }

    @Override
    public int getReplaceLength() {
        return CodeReader.CurrentStatement.getStatementLength() + indentationLength;
    }
    
    @Override
    public Image getAssistIcon() {
        if (icon == null) {
            icon = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/qfs4c16.png").createImage();
        }
        return icon;
    }

    protected String toPre(String in) {
        return "<pre>".concat(in).concat("</pre>");
    }

}
