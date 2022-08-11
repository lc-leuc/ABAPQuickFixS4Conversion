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

	private static final String ORDER_BY_PRIMARY_KEY = " order by primary key";

	/**
	 * Capturing Groups * 1 - leading line breaks * 2 - leading spaces * 3 - word
	 * "single" * 4 - field list * 5 - table * 6 - into-data statement * 7 - where
	 * statement
	 */
//    private static final String selectPattern =
	// dot is not part of the statement
//            //                  select     single     *      from     wbhk   into     @data(result) where tkonn = ''
//            //   1        2                3          4               5               6                7
//            "(?i)([\n\r]*)(\\s*)select\\s+(single)\\s+(.*)\\s+from\\s+(.*)\\s+into\\s+(.*)\\s+where\\s+(.*)";
	// 1 breaks
	// 2 spaces
	// 3 single
	// 4 fields
	// 5 table
	// 6 into variable
	// 7 where statement
//    private static final String targetSelectPatternStart = "select $4 from $5 into $6 up to 1 rows where $7";
//    private static final String targetSelectPatternEnd = "endselect";
//    private static final String modernTargetSelectPatternStart = "select from $5 fields $4 where $7";
//    private static final String modernTargetSelectPatternEnd = " into $6 up to 1 rows. endselect";

	private static final String selectPattern =
			// select single * from wbhk into @data(result) where tkonn = ''
			// 1 2 3 4 5 6 7 8 9 10 11
			"(?i)([\n\r]*)(\\s*)(select)\\s+(single)\\s+(.*)\\s+(from)\\s+(.*)\\s+(into)\\s+(.*)(?:\\s+(where)\\s+(.*))?";

	// 01 breaks
	// 02 spaces
	// 03 select
	// 04 single
	// 05 fields
	// 06 from
	// 07 table
	// 08 into
	// 09 into variable
	// 10 where
	// 11 where statement

	private static final String targetSelectPatternStart = "$3 $5 $6 $7 $8 $9 up to 1 rows $10 $11";
	private static final String targetSelectPatternEnd = "endselect";
	private static final String modernTargetSelectPatternStart = "$3 $6 $7 fields $5 $10 $11";
	private static final String modernTargetSelectPatternEnd = " $8 $9 up to 1 rows. endselect";

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

		SelectFormat formatter = new SelectFormat(temp2.contains("select")); // guess case
		
		String temp = (formatter.removeAllLineComments(CodeReader.CurrentStatement)).replaceAll("\r\n\\s*[\r\n]", ""); // remove
																														// first
																														// line
																														// feed
																														// characters
		String originalIndentation = temp.replaceFirst("(?i)(?s)(\\s*)(select)(.*)", "$1").replaceAll("[\r\n]", "");

		// line breaks are added automatically with the indentation prefix
		String comentedOut = getCommentedOutStatement(temp);

		temp = temp.replaceAll("[\r\n]", ""); // remove all line feed characters
		currentTable = temp.replaceFirst(getMatchPattern(), "$7"); //workaround - cannot change signature of getReplacePattern

		String m = getMatchPattern();
		String r = getReplacePattern();
		String newString = temp.replaceFirst(m, r);
		String[] s = formatter.split(newString.replaceAll("\\s\\s*", " ")); // remove multiple spaces
		String statement = "";
		for (String line : s) {
			statement += formatter.format(line, originalIndentation, "select");
		}

		String concat = leadingBreaks.concat(getCommentPrefix().concat(comentedOut.concat(statement)));
		return concat;
	}

	private String getCommentedOutStatement(String in) {
		if (Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.COMMENT_OUT)) {
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
		return "Replace select single with select 'up to 1 rows'. "
				+ "In cases that select single might not be unique an ordering of "
				+ "results can be enforced by providing the 'order by' statement. "
				+ "For tables that feature a new ruuid-style key field after S4/Hana transition, "
				+ "a configurable 'order by' sequence is provided in preferences.";
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
			temp.append(" order by " + orderBy);
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

}
