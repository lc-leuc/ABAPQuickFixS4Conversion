package de.leuc.adt.quickfix.select;

import com.abapblog.adt.quickfix.assist.syntax.codeParser.AbapStatement;

/**
 * Formatter for select statements.
 * <ul>
 * <li>includes arobase (@)</li>
 * <li>includes commas in lists</li>
 * <li>reformats the statement in lines with indentation</li>
 * </ul>
 * 
 * @author lc
 *
 */
public class SelectFormat {

    private static final String secondLevelOr = "     ";
    private static final String secondLevelAnd = "    ";
    private static final String firstLevel = "  ";
    private boolean lowercase = true;

    /**
     * Determine the preferred case (upper or lower) of letters. This is important
     * for introduced elements, such as <code>up to 1 rows</code>.
     * 
     * Guessing by case of select statement:
     * <code>new SelectFormat(statement.contains("select"))</code>
     * 
     * @param lowercase - true if lower case
     */
    public SelectFormat(boolean lowercase) {
        this.lowercase = lowercase;
    }

    /**
     * Prefered case.
     * 
     * @return - true if lower case is set
     */
    public boolean getLowerCase() {
        return lowercase;
    }

    /**
     * Splits a select statement into an array of the components.
     * 
     * Also concatenates <code>into corresponding fields of</code> to one word.
     * 
     * @param in - select statement
     * @return - list of components
     */
    public String[] split(String in) {
        String[] r = new String[] {};
//        String from = "(?i)(?=from )";

        in = in.replaceFirst("(?i)(into) (corresponding) (fields) (of) (table)", "$1$2$3$4$5");
        in = in.replaceFirst("(?i)(into) (corresponding) (fields) (of)", "$1$2$3$4");

        r = in.split("(?i)(?=\sfrom\s)" // + "|(?= as )"
                + "|(?=intocorrespondingfieldsoftable\s)|(?=intocorrespondingfieldsof\s)"
                + "|(?=\sinto\s)|(?=\sup\sto\s)|(?=\swhere\s)|(?=\sand\s)"
                + "|(?=\sor\s)|(?=\sendselect)|(?=\sorder\sby\s)|(?=\sgroup\sby\s)|(?=\sfields\s)|(?=\sjoin\s)");
        return r;
    }

    /**
     * Formats a select statement with a given indentation by splitting into lines
     * and formatting each line.
     * 
     * @param originalIndentation - indentation
     * @param replacement         - select statement to be formatted
     * @param startWith           - <code>select</code> or
     *                            <code>select single</code>
     * @return - formatted statement
     */
    public String format(String originalIndentation, String replacement, String startWith) {
        // fallback
        if (!("select".equals(startWith) || "select single".equals(startWith))) {
            startWith = "select";
        }

        String[] s = split(replacement.replaceAll("\s\s*", " ")); // remove multiple spaces
        String newStatement = "";
        for (String line : s) {
            newStatement += formatLine(line, originalIndentation, startWith);
        }
        return newStatement.replaceAll("\s+\\.", ".");
    }

    /**
     * Every line is formatted and augmented with indentation according to its
     * function.
     * 
     * @param input               - input string, single line
     * @param originalIndentation - indentation (prefix)
     * @param start               - <code>select</code> or
     *                            <code>select single</code>
     * @return - formatted line
     */
    public String formatLine(String input, String originalIndentation, String start) {

        // String in = input.toLowerCase();
        String indentation = originalIndentation;
        originalIndentation = "\n" + originalIndentation;
        String in = input;
        String in2 = input.toLowerCase().trim();
        if (in2.startsWith(start)) {
            if (!in2.equals(start)) {
                String selection = in.replaceFirst("(?i)" + start + "\s+(.*)", "$1").trim();
                if (selection.contains(" ") && !selection.contains(",")) {
                    // prevent commas around 'as', 'distinct' and functions/'(' - negative look
                    // behind and negative look ahead
                    in = transformCase(start)
                            .concat(" ".concat(selection.replaceAll("(?<!,| as|\\(| distinct)\\s+(?!as |\\))", ", ")));
                }
            }
            return indentation + in.trim();
        } else if (in2.startsWith("from ")) {
            String table = in.replaceFirst("(?i)from\s+(.*)", "$1").trim();
            in = handleInLineComment(transformCase("from ").concat(table));
            return originalIndentation + firstLevel + in.trim();
        } else if (in2.startsWith("as ")) {
            return " " + in.trim();
        } else if (in2.startsWith("fields ")) {
            String fields = in.replaceFirst("(?i)fields\s+(.*)", "$1").trim();
            // prevent commas around 'as', 'distinct' and functions/'(' - negative look
            // behind and negative look ahead
            in = transformCase("fields ").concat(fields.replaceAll("(?<!,| as|\\(| distinct)\\s+(?!as |\\))", ", "));
            // }
            return originalIndentation + firstLevel + in.trim();
        } else if (in2.startsWith("where ")) {
            return originalIndentation + firstLevel + adaptNewStyle(in).trim();
        } else if (in2.startsWith("and ")) {
            return originalIndentation + secondLevelAnd + adaptNewStyle(in).trim();
        } else if (in2.startsWith("or ")) {
            return originalIndentation + secondLevelOr + adaptNewStyle(in).trim();
        } else if (in2.toLowerCase().startsWith("intocorrespondingfieldsoftable ")) {
            return originalIndentation + firstLevel + adaptInto(in)
                    .replaceFirst("(?i)(into)(corresponding)(fields)(of)(table) ", "$1 $2 $3 $4 $5 ").trim();
        } else if (in2.toLowerCase().startsWith("intocorrespondingfieldsof ")) {
            String temp3 = adaptInto(in);
            temp3 = temp3.replaceFirst("(?i)(into)(corresponding)(fields)(of) ", "$1 $2 $3 $4 ").trim();
            return originalIndentation + firstLevel + temp3;
        } else if (in2.startsWith("into ")) {
            return originalIndentation + firstLevel + adaptInto(in2).trim();
        } else if (in2.startsWith("group by ")) {
            return originalIndentation + firstLevel + in2.trim();
        } else if (in2.startsWith("order by primary key")) {
            return originalIndentation + firstLevel + transformCase(in).trim();
        } else if (in2.startsWith("order by ")) {
            String orderByList = in.replaceFirst("(?i)order by\s+(.*)", "$1").trim();
            return originalIndentation + firstLevel + transformCase("order by ").concat(orderByList).trim();
        } else if (in2.startsWith("endselect")) {
            // no additional line break
            return originalIndentation + transformCase(in2).trim();
        } else {
            return originalIndentation + firstLevel + transformCase(in).trim();
        }

    }

    private String handleInLineComment(String fromLine) {
        return fromLine.replaceAll(",(\s*\")", "$1");
    }

    private String transformCase(String string) {
        if (getLowerCase()) {
            return string;
        }
        return string.toUpperCase();
    }

    private String adaptInto(String in) {
        String keyWord = "";
        if (in.toLowerCase().startsWith("into table ")) {
            keyWord = "into\s+table";
        } else if (in.toLowerCase().startsWith("into ")) {
            keyWord = "into";
        } else if (in.toLowerCase().startsWith("intocorrespondingfieldsof ")) {
            keyWord = "intocorrespondingfieldsof";
        } else if (in.toLowerCase().startsWith("intocorrespondingfieldsoftable ")) {
            keyWord = "intocorrespondingfieldsoftable";
        }

        String prefix = in.replaceFirst("(?i)(" + keyWord + ")\s+(.*)", "$1").concat(" ");
        String temp = in.replaceFirst("(?i)(" + keyWord + ")\s+(.*)", "$2");

        // replace into (tkonn, tposn) with into (@tkonn, @tposn)
        String temp2 = transformCase(prefix).concat(temp.replaceAll("(^\\(|^|[ ,])([<a-zA-Z0-9_])", "$1@$2"));
        return temp2;
    }

    private String adaptIntoCorrespondingFieldsOf(String in) {
        // replace into (tkonn, tposn) with into (@tkonn, @tposn)
        return in.replaceAll("( \\(|[ ,])([<a-zA-Z0-9_])", "$1@$2");
    }

    private String adaptNewStyle(String in) {
        // replace where tkonn = tkonn with where tkonn = @tkonn
        // (no duplicate spaces) where tkonn eq tkonn
        String replaceFirst = in.replaceFirst("([a-zA-Z0-9_]*) (.*) (..?) ([<a-zA-Z0-9_])", "$1 $2 $3 @$4");
        return replaceFirst;
    }

    public String removeAllLineComments(AbapStatement currentStatement) {
        return currentStatement.replaceAllPattern("([\r\n])\\*.*([\r\n])", "$1$2");
    }

}
