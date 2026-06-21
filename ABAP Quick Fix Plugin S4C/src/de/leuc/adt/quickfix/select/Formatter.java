package de.leuc.adt.quickfix.select;

@FunctionalInterface
public interface Formatter {

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
    String format(String originalIndentation, String replacement, String startWith);

}