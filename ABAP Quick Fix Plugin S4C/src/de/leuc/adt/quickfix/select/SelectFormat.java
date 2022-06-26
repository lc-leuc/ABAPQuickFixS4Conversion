package de.leuc.adt.quickfix.select;

public abstract class SelectFormat {

    public static String[] split(String in) {
        String[] r = new String[] {};
        String from = "(?i)(?=from )";
        in = in.replace("into corresponding fields of table", "intocorrespondingfieldsoftable");
        in = in.replace("into corresponding fields of", "intocorrespondingfieldsof");
        r = in.split(from + "|(?=intocorrespondingfieldsoftable )" +  "|(?=intocorrespondingfieldsof )" + "|(?=into )" + "|(?=up\\sto )" + "|(?=where )" + "|(?=and )" + "|(?=or )" + "|(?=endselect)"
                + "|(?=order )" + "|(?=group )" + "|(?=fields )");
        return r;
    }

   
    public static String format(String in, String originalIndentation, String start) {
        if (in.startsWith(start)) {
            String selection = in.replaceFirst(start + "\\s+(.*)", "$1").trim();
            if (selection.contains(" ") && !selection.contains(",")) {
                in = start.concat( " ".concat(selection.replaceAll("\\s+", ", ")));
            }
            return originalIndentation + in.trim() + "\n";
        } else if (in.startsWith("fields ")) {
            String selection = in.replaceFirst("fields\\s+(.*)", "$1").trim();
            if (selection.contains(" ") && !selection.contains(",")) {
                in = "fields ".concat(selection.replaceAll("\\s+", ", "));
            }
            return originalIndentation + "  " + in .trim()+ "\n";
        } else if (in.startsWith("where ")) {
            return originalIndentation + "  " + adaptNewStyle(in).trim() + "\n";
        } else if (in.startsWith("and ")) {
            return originalIndentation + "    " + adaptNewStyle(in).trim() + "\n";
        } else if (in.startsWith("or ")) {
            return originalIndentation + "     " + adaptNewStyle(in).trim() + "\n";
        } else if (in.startsWith("intocorrespondingfieldsoftable ")) {
            return originalIndentation + "  " + adaptInto(in).replace("intocorrespondingfieldsoftable ", "into corresponding fields of table ").trim() + "\n";
        } else if (in.startsWith("intocorrespondingfieldsof ")) {
            return originalIndentation + "  " + adaptInto(in).replace("intocorrespondingfieldsof ", "into corresponding fields of ").trim() + "\n";
        } else if (in.startsWith("into ")) {
            return originalIndentation + "  " + adaptInto(in).trim() + "\n";
        } else if (in.startsWith("endselect")) {
            return originalIndentation + in.trim();
        } else {
            return originalIndentation + "  " + in.trim() + "\n";
        }

    }
    
    private static String adaptInto(String in) {
        // replace into (tkonn, tposn) with into (@tkonn, @tposn)
        return in.replaceAll("( \\(|[ ,])([a-zA-Z_])", "$1@$2");
    }

    private static String adaptIntoCorrespondingFieldsOf(String in) {
        // replace into (tkonn, tposn) with into (@tkonn, @tposn)
        return in.replaceAll("( \\(|[ ,])([a-zA-Z_])", "$1@$2");
    }

    private static String adaptNewStyle(String in) {
        // replace where tkonn = tkonn with where tkonn = @tkonn
        // (no duplicate spaces)                where   tkonn eq    tkonn
        String replaceFirst = in.replaceFirst("([a-z]*) (.*) (..?) ([a-zA-Z_])", "$1 $2 $3 @$4");
        return replaceFirst;
    }

}
