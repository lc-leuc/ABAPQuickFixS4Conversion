package de.leuc.adt.quickfix.select;

import com.abapblog.adt.quickfix.assist.syntax.codeParser.AbapStatement;

public class SelectFormat {

    private static final String secondLevelOr = "     ";
    private static final String secondLevelAnd = "    ";
    private static final String firstLevel = "  ";
    private boolean lowercase = true;

    public SelectFormat(boolean lowercase) {
        this.lowercase = lowercase;
    }

    public boolean getLowerCase() {
        return lowercase;
    }

    public String[] split(String in) {
        String[] r = new String[] {};
//        String from = "(?i)(?=from )";

        in = in.replaceFirst("(?i)(into) (corresponding) (fields) (of) (table)", "$1$2$3$4$5");
        in = in.replaceFirst("(?i)(into) (corresponding) (fields) (of)", "$1$2$3$4");

        r = in.split("(?i)(?=from )" + "|(?=as )" + "|(?=intocorrespondingfieldsoftable )"
                + "|(?=intocorrespondingfieldsof )" + "|(?=into )" + "|(?=up\\sto )" + "|(?=where )" + "|(?=and )"
                + "|(?=or )" + "|(?=endselect)" + "|(?=order )" + "|(?=group )" + "|(?=fields )" + "|(?=join )");
        return r;
    }

    public String format(String originalIndentation, String replacement, String startWith) {
        String[] s = split(replacement.replaceAll("\\s\\s*", " ")); // remove multiple spaces
        String newStatement = "";
        for (String line : s) {
            newStatement += formatLine(line, originalIndentation, startWith);
        }
        return newStatement.replaceAll("\\s+\\.", ".");
    }

    public String formatLine(String input, String originalIndentation, String start) {

        // String in = input.toLowerCase();
        String indentation = originalIndentation;
        originalIndentation = "\n" + originalIndentation;
        String in = input;
        String in2 = input.toLowerCase();
        if (in2.startsWith(start)) {
            String selection = in.replaceFirst("(?i)" + start + "\\s+(.*)", "$1").trim();
            if (selection.contains(" ") && !selection.contains(",")) {
                in = transformCase(start).concat(" ".concat(selection.replaceAll("\\s+", ", ")));
            }
            return indentation + in.trim();
        } else if (in2.startsWith("from ")) {
            String table = in.replaceFirst("(?i)from\\s+(.*)", "$1").trim();
            in = handleInLineComment(transformCase("from ").concat(table.replaceAll("\\s+", ", ")));
            return originalIndentation + firstLevel + in.trim();
        } else if (in2.startsWith("as ")) {
            return " " + in.trim();
        } else if (in2.startsWith("fields ")) {
            String fields = in.replaceFirst("(?i)fields\\s+(.*)", "$1").trim();
            // if (fields.contains(" ") && !fields.contains(",")) {
            in = transformCase("fields ").concat(fields.replaceAll("(?<!,)\\s+", ", "));// ("\\s+", ", "));
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
            return originalIndentation + firstLevel + adaptInto(in).trim();
        } else if (in2.startsWith("order by primary key")) {
            return originalIndentation + firstLevel + transformCase(in).trim();
        } else if (in2.startsWith("order by ")) {
            String orderByList = in.replaceFirst("(?i)order by\\s+(.*)", "$1").trim();
            return originalIndentation + firstLevel + transformCase("order by ").concat(orderByList).trim();
        } else if (in2.startsWith("endselect")) {
            // no additional line break
            return originalIndentation + transformCase(in).trim();
        } else {
            return originalIndentation + firstLevel + transformCase(in).trim();
        }

    }

    private String handleInLineComment(String fromLine) {
        return fromLine.replaceAll(",(\\s*\\\")", "$1");
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
            keyWord = "into\\s+table";
        } else if (in.toLowerCase().startsWith("into ")) {
            keyWord = "into";
        } else if (in.toLowerCase().startsWith("intocorrespondingfieldsof ")) {
            keyWord = "intocorrespondingfieldsof";
        } else if (in.toLowerCase().startsWith("intocorrespondingfieldsoftable ")) {
            keyWord = "intocorrespondingfieldsoftable";
        }

        String prefix = in.replaceFirst("(?i)(" + keyWord + ")\\s+(.*)", "$1").concat(" ");
        String temp = in.replaceFirst("(?i)(" + keyWord + ")\\s+(.*)", "$2");

        // replace into (tkonn, tposn) with into (@tkonn, @tposn)
        String temp2 = prefix.concat(temp.replaceAll("(^\\(|^|[ ,])([<a-zA-Z0-9_])", "$1@$2"));
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
        return currentStatement.replaceAllPattern("([\r\n])\\*.*([\\r\\n])", "$1$2");
    }

}
