package de.leuc.adt.quickfix.select;

import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import com.abapblog.adt.quickfix.assist.syntax.codeParser.AbapStatement;

import de.leuc.adt.quickfix.util.StatementUtil;

/**
 * QuickFix: Replace with VDM using mapping
 * 
 * @author lc
 *
 */
public class SelectReplaceWithVDM extends StatementAssistRegexS4C {

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

    public static final int SELECT_MATCH_SELECT_SINGLE = 0;
    public static final int SELECT_MATCH_SELECT = 1;
    public static final int SELECT_MATCH_SELECT_SINGLE_2021 = 2;
    public static final int SELECT_MATCH_SELECT_2021 = 3;
    // dot is not part of the statement
    // allowing for various sort orders of into, from and where
    public static final String[] SELECT_PATTERN = {
            // 0 select single
            "(?i)(?<select>select)\\s+single\\s+(?<fields>.*)(?:(?:(?<from>from)(?<table>.*))|(?:(?<into>into)(?<variable>.*))|(?:(?<where>where)(?<condition>.*))){3}",
            // 1 select with endselect
            "(?i)(?<select>select)\\s+(?<fields>.*)(?:(?:(?<from>from)(?<table>.*))|(?:(?<into>into)(?<variable>.*))|(?:(?<where>where)(?<condition>.*))){3}",
            // 2 select single in 2021 style
            "(?im)(?<select>select)\\s+(?<single>single)\\s+(?<from>from )(?<table>.*)\\s+(?<fieldskey>fields)\\s+(?<fields>.*)\\s+(?<where>where)\\s+(?<condition>.*)\\s+(?<into>into)(\\s+corresponting fields of)?\\s+(?<variable>.*)",
            // 3 select in 2021 style
            "(?im)(?<select>select)\\s+(?<from>from )(?<table>.*)\\s+(?<fieldskey>fields)\\s+(?<fields>.*)\\s+(?<where>where)\\s+(?<condition>.*)\\s+(?<into>into)(\\s+corresponting fields of)?\\s+(?<variable>.*)" };

    public static final String[] TARGET_SELECT_PATTERN_START = {
            // 0 select single classic
            "${select} single ${fields} ${from} ${table} ${where} ${condition} ${into} ${variable}",
            // 1 select with endselect
            "${select} ${fields} ${from} ${table} ${where} ${condition} ${into} ${variable}",
            // 2 select single in 2021 style
            "${select} single ${from} ${table} fields ${fields} ${where} ${condition}, into ${variable}",
            // 3 select in 2021 style
            "${select} ${from} ${table} fields ${fields} ${where} ${condition}, into ${variable}" };
    public static final String[] MODERN_TARGET_SELECT_PATTERN_START = {
            // 0 select single classic
            "${select} single ${from} ${table} fields ${fields} ${where} ${condition} ${into} ${variable}",
            // 1 select with endselect
            "${select} ${from} ${table} fields ${fields} ${where} ${condition} ${into} ${variable}",
            // 2 select single in 2021 style
            "${select} single ${from} ${table} fields ${fields} ${where} ${condition} into ${variable}",
            // 3 select in 2021 style
            "${select} ${from} ${table} fields ${fields} ${where} ${condition} into ${variable}" };

    /**
     * which table needs to be replaced
     */
    private String currentTable;
    private String cache;
    /**
     * index of the replacement
     */
    private final int variant;
    /**
     * multiple replacements might be possible, index required /* only the leader
     * (smallest index for variant) determines, i f we can assist - followers follow
     * leaders decision
     */
    private final SelectReplaceWithVDM leader;
    /** remember if we can assist for followers */
    private boolean canAssistFlag;
    /**
     * index of matching select pattern
     */
    private int replacementIndex = -1;

    /**
     * contains the folder if the replacement is user-defined
     */
    private String userTag = "";

    public boolean isCanAssist() {
        return canAssistFlag;
    }

    public void setCanAssist(boolean leaderDecision) {
        this.canAssistFlag = leaderDecision;
    }

    /**
     * already contains line break
     */

    public SelectReplaceWithVDM(int index, SelectReplaceWithVDM leader) {
        super();
        this.variant = index;
        this.leader = leader;
    }

    @Override
    public String getChangedCode() {

        AbapStatement currentStatement = CodeReader.CurrentStatement;
        String statement = currentStatement.getStatement();

        String leading = currentStatement.getLeadingCharacters();
        return getChangedCodeInternal(statement, leading);
    }

    public String getChangedCodeInternal(String statement, String leading) {

        // line cleanup, if messed up with comments
        String statementInternal = statement.replaceFirst("(?im)[\\s\\S]*^(\\s*)(select)", "$1$2");

        // remove all line feed characters and leading spaces
        String statementOneLine = statementInternal.replaceAll("[\r\n]", "").replaceAll("\s+", " ").trim();
        // if leading2 is empty we need to determine indentation from leading
        String originalIndentation = "";

        // we need to remember the indentation -- remove everything until the last line

        originalIndentation = leading.replaceAll("[\\s\\S]*[\\r\\n]", "");
        indentationLength = originalIndentation.length();

        String replacement = "";

        // remember the current table, in order to determine order-by statement
        currentTable = statementOneLine.replaceFirst(getMatchPattern(replacementIndex), "${table}")
                .replaceFirst("(.*)\s+as\s+.*", "$1").trim().toUpperCase();
        // do the actual replacement
        replacement = statementOneLine.replaceFirst(getMatchPattern(replacementIndex), getReplacePattern());

        Formatter formatter = new SelectFormat(statementInternal.contains("select")); // guess
                                                                              // case
        // /* alternative formatter: */ formatter = new OpenSqlFormatter();
        // self-defined prefix - from Preferences
        StatementUtil util = StatementUtil.getInstance();
        String prefix = util.getCommentPrefix(originalIndentation);

        // if preferences are set: produce a commented version of the original text
        String commentedOut = util.getCommentedOutStatement(statementInternal, originalIndentation);

        String startsWith = "select";
        if (replacement.startsWith("select single")) {
            startsWith = "select single";
        }
        // format
        String newStatement = formatter.format(originalIndentation, replacement, startsWith);

        newStatement = VdmTransformer.getInstance().transform(newStatement, currentTable + "." + variant);
        newStatement = formatter.format(originalIndentation, newStatement, startsWith);

        // concatenate leading lines with automatic comment (if set in prefs)
        // as well as original statement (as comment, if set in prefs) and the new
        // statement
//        String returning = leading.concat(prefix.concat(commentedOut.concat(newStatement)));
        if (newStatement.startsWith(" ")) {
            newStatement = newStatement.replaceFirst("\\s+", "");
        }
        String returning = prefix.concat(commentedOut.concat(originalIndentation + newStatement));
        cache = returning;
        return returning;
    }

    @Override
    public String getAssistShortText() {
        return "VDM replace " + table + " with " + getTableMap().getProperty(table) + " " + variant + "/"
                + getReplacementIndexText() + " " + userTag;
    }

    @Override
    public String getAssistLongText() {
        System.out.println("Replace with VDM<br/><b>" + getTable() + " --&gt; " + getTableMap().getProperty(table)
                + "<p>----<p>" + toPre(cache));
        return "Replace with VDM<br/><b>" + getTable() + " --&gt; " + getTableMap().getProperty(table)
                + "<p>----<p>" + toPre(cache);
    }

    @Override
    public boolean canAssist() {
        VdmTransformer transformer = VdmTransformer.getInstance();
        if (transformer.inReplacements(getTable() + "." + variant)) {
            if (leader != null) {
                setTable(leader.getTable());
//                if (!transformer.inMapping(getTable() + "." + variant)) {
//                    transformer.retreiveMapping(getTable() + "." + variant);
//                }
                setTableMap(transformer.get(getTable() + "." + variant));
//                targetCDS = getTableMap().getProperty(getTable());
            }
            replacementIndex = leader.getReplacementIndex();
//            System.out.println("Rplacement Index A "+replacementIndex);
            if (replacementIndex < 0) {
                setCanAssist(false);
            } else {
                setCanAssist(transformer.inMapping(getTable() + "." + variant));
            }
        } else {

            String currentStatement = CodeReader.CurrentStatement.getStatement().replaceAll("[\r\n]", "").trim();
            if (Pattern.compile(getMatchPattern(SELECT_MATCH_SELECT_2021)).matcher(currentStatement).find()) {
                replacementIndex = SELECT_MATCH_SELECT_2021;
            } else if (Pattern.compile(getMatchPattern(SELECT_MATCH_SELECT_SINGLE_2021)).matcher(currentStatement)
                    .find()) {
                replacementIndex = SELECT_MATCH_SELECT_SINGLE_2021;
            } else if (Pattern.compile(getMatchPattern(SELECT_MATCH_SELECT_SINGLE)).matcher(currentStatement).find()) {
                replacementIndex = SELECT_MATCH_SELECT_SINGLE;
            } else if (Pattern.compile(getMatchPattern(SELECT_MATCH_SELECT)).matcher(currentStatement).find()) {
                replacementIndex = SELECT_MATCH_SELECT;
            } else {
                return false;
            }

//            System.out.println("Replacement Index B "+replacementIndex);

            if (Pattern.compile(getMatchPattern(replacementIndex)).matcher(currentStatement).find()) {
                if (currentStatement.contains("join")) {
                    setCanAssist(false);
                } else {

                    AbapStatement curStatement = CodeReader.CurrentStatement;
                    String statement = curStatement.getStatement();

                    // line cleanup, if messed up with comments
                    statement = statement.replaceFirst("(?im)[\\s\\S]*^(\\s*)(select)", "$1$2");

                    // remove all line feed characters and leading spaces
                    String statementOneLine = statement.replaceAll("[\r\n]", "").trim();

                    // remember the current table, in order to determine order-by statement
                    String table = "";
                    table = statementOneLine.replaceFirst(getMatchPattern(replacementIndex), "${table}")
                            .replaceFirst("(.*)\s+as\s+.*", "$1").trim().toUpperCase();
                    setTable(table);
                    if (transformer.inReplacements(table + "." + variant)) {
                        setTableMap(transformer.get(table + "." + variant));
                    }
                    setCanAssist(transformer.inMapping(table + "." + variant));

                }
            } else {
                setCanAssist(false);
            }
        }
        if (isCanAssist() && transformer.isUserMap(getTable() + "." + variant)) {
            userTag = "(" + StatementUtil.getInstance().getUserReplacementsDir() + ")";
        }
        return isCanAssist() && 
                ( StatementUtil.getInstance().isAlwaysAllow() 
                  || areAllSelectedElementsInMapping(CodeReader.CurrentStatement.getStatement()));
    }

    private boolean areAllSelectedElementsInMapping(String statement) {
        String statementOneLine = statement.replaceFirst("(?im)[\\s\\S]*^(\\s*)(select)", "$1$2")
                .replaceAll("[\r\n]", "").trim();

        String fields = statementOneLine.replaceFirst(getMatchPattern(replacementIndex), "${fields}").replaceFirst("single", "").trim();
        fields = fields.replaceAll("(?i)(.*?)\\s+as\\s+[a-zA-Z_0-9]*", "$1");
        if (! fields.equals("*")) {
            fields = fields.replaceFirst("single", "").trim();
            String splitCharacter;
            if (fields.contains(",")) {
                splitCharacter = ",";
            } else {
                splitCharacter = " ";
            }
            String[] split = fields.split(splitCharacter);

            for (String string : split) {

                if (!getTableMap().containsKey(string.trim().toUpperCase())) {
                    return false;
                }
            }
        }
        String orderby = "";
        String remaining = statementOneLine.replaceFirst(getMatchPattern(replacementIndex), "${condition}");
        remaining = remaining.replaceAll(",", "").replaceAll("\\(", "").replaceAll("\\)", "");

        String condition = remaining.replaceFirst("(?i)\\s+(?<fields>.*)\\s+(?<groupby>group by)(?<group>.*)",
                "${fields}");
        if (condition.contains("order by")) {
            orderby = condition.replaceFirst("(?i)\\s+(?<fields>.*)\\s+(?<orderby>order by)(?<order>.*)", "${order}");
            condition = condition.replaceFirst("(?i)\\s+(?<fields>.*)\\s+(?<orderby>order by)(?<order>.*)",
                    "${fields}");
        }
        String groupby = "";

        if (condition.contains("group by")) {
            groupby = condition.replaceFirst("(?i)\\s+(?<fields>.*)\\s+(?<groupby>group by)(?<group>.*)", "${group}");
            if (condition.contains("oder by")) {
                orderby = groupby.replaceFirst("(?i)\\s+(?<fields>.*)\\s+(?<orderby>order by)(?<order>.*)", "${oder}");
                groupby = groupby.replaceFirst("(?i)\\s+(?<fields>.*)\\s+(?<orderby>order by)(?<order>.*)",
                        "${fields}");
            }
        }
        StringTokenizer tokenizer = new StringTokenizer(condition);
        while (tokenizer.hasMoreTokens()) {

            String token = tokenizer.nextToken();
            if (token.trim().toUpperCase().equals("AND") || token.trim().toUpperCase().equals("OR")) {
                token = tokenizer.nextToken();
            }

            token = token.replaceFirst(".*~", "");
            if (!getTableMap().containsKey(token.trim().toUpperCase())) {
                return false;

            }
            tokenizer.nextToken();
            tokenizer.nextToken();

        }
        tokenizer = new StringTokenizer(groupby);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            token = token.replaceFirst(".*~", "");
            if (!getTableMap().containsKey(token.trim().toUpperCase())) {
                return false;
            }
        }
        if (! orderby.trim().equals("primary key")) {
        tokenizer = new StringTokenizer(orderby);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            token = token.replaceFirst(".*~", "");
            if (!getTableMap().containsKey(token.trim().toUpperCase())) {
                return false;
            }
        }
        }

        return true;
    }

    @Override
    public String getMatchPattern() {
        return getMatchPattern(0);
    }

    @Override
    public String getReplacePattern() {
        return getReplacePattern(false);
    }

    //////////////////////////////////////////////////////
    // Replace VDM specific

    private String table = "";
//    private String targetCDS = "";
    private Properties tableMap = new Properties();

    public String getMatchPattern(int index) {
        return SELECT_PATTERN[index];
    }

    private String getReplacePattern(boolean forceNewStyle) {
        if (StatementUtil.getInstance().isNewStyleSet()) {
            return MODERN_TARGET_SELECT_PATTERN_START[replacementIndex];
        }
        // else {
        return TARGET_SELECT_PATTERN_START[replacementIndex];
    }

    private int getReplacementIndex() {
        return replacementIndex;
    }

    private String getReplacementIndexText() {
        String type = "";
        if (replacementIndex == SELECT_MATCH_SELECT_2021) {
            type = "2021 Style";
        } else if (replacementIndex == SELECT_MATCH_SELECT_SINGLE_2021) {
            type = "2021 Style (single)";
        } else if (replacementIndex == SELECT_MATCH_SELECT) {
            type = "select";
        } else if (replacementIndex == SELECT_MATCH_SELECT_SINGLE) {
            type = "select single";
        }
        return type;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Properties getTableMap() {
        return tableMap;
    }

    public void setTableMap(Properties tableMap) {
        this.tableMap = tableMap;
    }

    public void setReplacementIndex(int i) {
        replacementIndex = i;

    }
}
