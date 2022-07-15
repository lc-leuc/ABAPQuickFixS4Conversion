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

public class SelectNewStyle extends StatementAssistRegex implements IAssistRegex {

	/**
	 * Capturing Groups * 1 - leading line breaks * 2 - leading spaces * 3 - word
	 * "single" * 4 - field list * 5 - table * 6 - into-data statement * 7 - where
	 * statement
	 */
	private static final String selectPattern =
			// dot is not part of the statement
			// select single * from wbhk into @data(result) where tkonn = ''
			// 1 2 3 4 5 6 7
			"(?i)([\n\r]*)(\\s*)select\\s+(.*)\\s+from\\s+(.*)\\s+into([ corresponding fields of]?)\\s+(.*)\\s+where\\s+(.*)";

	private String currentTable;
	/**
	 * already contains line break
	 */
	private String leadingBreaks = "";
	private boolean comments = false;
	private int indent_number = 2;

	public SelectNewStyle() {
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
		currentTable = temp.replaceFirst(getMatchPattern(), "$5");

		String[] s = formatter.split(temp.trim().replaceAll("\\s\\s*", " ")); // remove multiple spaces
		String statement = "";
		for (String line : s) {
			statement += formatter.format(line, originalIndentation, "select");
		}
		statement = statement.replaceAll("[\\r\\n]$", ""); // remove last line break

		String concat = leadingBreaks.concat(getCommentPrefix().concat(comentedOut.concat(statement)));
		return concat;
	}

	private String getCommentedOutStatement(String in) {
		if (Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.COMMENT_OUT)) {
			// String ls = in.replaceFirst("(?i)(?s)([\\n\\r]*)(\\s*select.*)","$1");
			in = in.replaceFirst("(?i)(?s)([\\n\\r]*)(\\s*select.*)", "*$2");
			return in.replaceAll("(\r\n|\n)", "$1" + "*").concat("\n");
		}
		return "";
	}

	@Override
	public String getAssistShortText() {
		return "Select / endselect: replace old style SQL with new style.";
	}

	@Override
	public String getAssistLongText() {

		return "Replace old style SQL with new style for select/endselect.\n New style includes @ for variables and commas for lists.";
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
		// do not propose if select single or already in new SQL style
		if (currentStatement.toLowerCase().contains(" single ") || currentStatement.contains(("@"))) {
			return false;
		}
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
		return "";

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
