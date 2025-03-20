package de.leuc.adt.quickfix.select;

import java.util.regex.Pattern;

import org.eclipse.swt.graphics.Image;

import com.abapblog.adt.quickfix.assist.syntax.codeParser.AbapCodeReader;
import com.abapblog.adt.quickfix.assist.syntax.codeParser.AbapStatement;
import com.abapblog.adt.quickfix.assist.syntax.statements.IAssistRegex;
import com.abapblog.adt.quickfix.assist.syntax.statements.StatementAssistRegex;

import de.leuc.adt.quickfix.Activator;
import de.leuc.adt.quickfix.preferences.OrderByPrefParser;
import de.leuc.adt.quickfix.preferences.PreferenceConstants;

/**
 * QuickFix: Replaces a select single statement in modern style with a select
 * statement with <code>up to 1 rows</code> and <code>order-by</code> statement.
 * 
 * If a oder-by sequence is provided for a given table in the preferences, then
 * the sequence is used, otherwise <code>primary key</code>.
 * 
 * @author lc
 *
 */
public class SelectSingle2021 extends SelectSingle implements IAssistRegex {

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
    // allowing for different sort orders of into, from and where
    public static final String selectPattern = "(?im)(?<select>select)\\s+(?<single>single)\\s*(?:(?:(?:(?<fieldskey> fields )(?<fields>.*))|(?:(?<from> from )(?<table>.*))){2}|(?:(?:(?<into> into )(?<variable>.*))|(?:(?<where> where )(?<condition>.*))){2}){2}";

    public static final String modernTargetSelectPatternStart = "${select} ${from} ${table} fields ${fields} ${where} ${condition}";
    public static final String modernTargetSelectPatternEnd = " ${into} ${variable} up to 1 rows. endselect";

    private String currentTable;

    public SelectSingle2021() {
        super();
    }

    @Override
    public String getAssistShortText() {
        return "Replace select single (2021 version) with select up to one rows";
    }

    @Override
    public String getAssistLongText() {
        return "Replace select single (2021) with select 'up to 1 rows'. "
                + "In cases that select single might not be unique an ordering of "
                + "results can be enforced by providing the 'order by' statement. "
                + "For tables that feature a new ruuid-style key field after S4/Hana transition, "
                + "a configurable 'order by' sequence is provided in preferences." + "<br/>"
                + getChangedCode().replaceAll("\n", "<br/>");
    }

    @Override
    public String getMatchPattern() {
        return selectPattern;
    }

    @Override
    public String getReplacePattern() {
        StringBuffer temp = new StringBuffer();
        String endPattern = "";

        temp.append(modernTargetSelectPatternStart);
        endPattern = modernTargetSelectPatternEnd;

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
        ;
        temp.append(" " + endPattern);
        return temp.toString();

    }
}
