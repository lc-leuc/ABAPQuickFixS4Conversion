package de.leuc.adt.quickfix.Methods;

import org.eclipse.swt.graphics.Image;

import com.abapblog.adt.quickfix.assist.syntax.statements.IAssistRegex;
import com.abapblog.adt.quickfix.assist.syntax.statements.StatementAssistRegex;
import com.abapblog.adt.quickfix.assist.syntax.statements.move.MoveExact;

public class From2Method extends StatementAssistRegex implements IAssistRegex {

    
    private static final String formPattern =
            // form  'SOME_FORM'. <other text lines> endform.
            "form '(.*)'.(.*)endform.";

    public From2Method() {
        super();
       
    }

    @Override
    public String getChangedCode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAssistShortText() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAssistLongText() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Image getAssistIcon() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean canAssist() {
        if (CodeReader.CurrentStatement.matchPattern(getMatchPattern()) && !(new MoveExact().canAssist())) {
            // table name to decide on order by clause
                 return true;
        }
        return false;
    }

    @Override
    public int getStartOfReplace() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getReplaceLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getMatchPattern() {
        return formPattern;
    }

    @Override
    public String getReplacePattern() {
        // TODO Auto-generated method stub
        return null;
    }

    
    
}
