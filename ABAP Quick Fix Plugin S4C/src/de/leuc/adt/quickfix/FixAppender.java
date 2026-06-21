package de.leuc.adt.quickfix;

import java.util.ArrayList;

import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;

import com.abapblog.adt.quickfix.IFixAppender;
import com.abapblog.adt.quickfix.assist.syntax.statements.StatementAssist;

import de.leuc.adt.quickfix.move.MoveCorresponding;
import de.leuc.adt.quickfix.select.SelectNewStyle;
import de.leuc.adt.quickfix.select.SelectReplaceWithVDM;
import de.leuc.adt.quickfix.select.SelectSingle;
import de.leuc.adt.quickfix.select.SelectSingle2021;
import de.leuc.adt.quickfix.select.SelectSingleNewStyle;

public class FixAppender implements IFixAppender {

    @Override
    public ArrayList<StatementAssist> additional_fixes(IQuickAssistInvocationContext context) {
        ArrayList<StatementAssist> list = new ArrayList<StatementAssist>();
        list.add(new SelectSingle());
        list.add(new SelectSingle2021());
        list.add(new SelectSingleNewStyle());
        list.add(new SelectNewStyle());
        list.add(new MoveCorresponding());
        SelectReplaceWithVDM leader = new SelectReplaceWithVDM(0,null);
        list.add(leader);
        list.add(new SelectReplaceWithVDM(1, leader));
        list.add(new SelectReplaceWithVDM(2, leader));
        list.add(new SelectReplaceWithVDM(3, leader));
        list.add(new SelectReplaceWithVDM(4, leader));
        list.add(new SelectReplaceWithVDM(5, leader));
        list.add(new SelectReplaceWithVDM(6, leader));
        list.add(new SelectReplaceWithVDM(7, leader));
        list.add(new SelectReplaceWithVDM(8, leader));
        return list;
    }
}
