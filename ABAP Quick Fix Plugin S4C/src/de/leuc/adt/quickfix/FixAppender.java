package de.leuc.adt.quickfix;

import java.util.ArrayList;

import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;

import com.abapblog.adt.quickfix.IFixAppender;
import com.abapblog.adt.quickfix.assist.syntax.statements.StatementAssist;

import de.leuc.adt.quickfix.select.SelectSingle;

public class FixAppender implements IFixAppender {

    @Override
    public ArrayList<StatementAssist> additional_fixes(IQuickAssistInvocationContext context) {
//        System.out.println("Hello World");
       ArrayList<StatementAssist> list = new ArrayList<StatementAssist>();
       list.add(new SelectSingle(context));
       return list;
    }

}
