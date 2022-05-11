package de.leuc.adt.quickfix;

import java.util.ArrayList;

import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;

import com.abapblog.adt.quickfix.IFixAppender;
import com.abapblog.adt.quickfix.assist.syntax.statements.StatementAssist;

import de.leuc.adt.quickfix.move.MoveCorresponding;
import de.leuc.adt.quickfix.select.SelectSingle;

public class FixAppender implements IFixAppender {

    @Override
    public ArrayList<StatementAssist> additional_fixes(IQuickAssistInvocationContext context) {
       ArrayList<StatementAssist> list = new ArrayList<StatementAssist>();
       list.add(new SelectSingle());
       list.add(new MoveCorresponding());
       return list;
    }
}
