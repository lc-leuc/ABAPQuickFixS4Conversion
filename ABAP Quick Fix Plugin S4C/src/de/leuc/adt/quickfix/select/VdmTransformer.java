package de.leuc.adt.quickfix.select;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import com.abapblog.adt.quickfix.assist.syntax.codeParser.AbapStatement;

import de.leuc.adt.quickfix.util.StatementUtil;

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
public final class VdmTransformer {

    public static final Set<String> OPERATORS = new HashSet<String>(Set.of(
            // Comparison
            "EQ", "=", "NE", "<>", "><", "LT", "<", "GT", ">", "LE", "<=", "GE", ">=",

            // Range / List
            "IN", // "NOT IN",
            "BETWEEN", // "NOT BETWEEN",
            "NOT",
            // Pattern Matching
            "LIKE",
            // "NOT LIKE",

            // Null / Initial
            "IS"
    // "IS NULL",
    // "IS NOT NULL",
    // "IS INITIAL",
    // "IS NOT INITIAL",
    ));

    public static final Set<String> WHERE_OPERATORS = new HashSet<String>(Set.of("WHERE", "AND", "OR", "NOT"));

    Properties replacements = new Properties();
    Map<String, Properties> map = new HashMap<String, Properties>();
    Map<String, String> userMaps = new HashMap<String, String>();

    /**
     * Determine the preferred case (upper or lower) of letters. This is important
     * for introduced elements, such as <code>up to 1 rows</code>.
     * 
     * Guessing by case of select statement:
     * <code>new SelectFormat(statement.contains("select"))</code>
     * 
     * @param lowercase - true if lower case
     */
    private VdmTransformer() {

        InputStream str;
        try {
            URL url = VdmTransformer.class.getResource("/resources/replacements.properties");
            InputStream stream = url.openStream();
            // System.out.println(url.toString());
            if (stream != null) {

                replacements.load(stream);

                Enumeration<Object> keys = replacements.keys();
                while (keys.hasMoreElements()) {
                    String fileName = (String) keys.nextElement();
                    String tableName = replacements.getProperty(fileName).trim();
                    String number = fileName.substring(fileName.indexOf('.'), fileName.lastIndexOf('.'));
                    URL resourcesUrl = VdmTransformer.class.getResource("/resources/" + fileName);
                    if (resourcesUrl != null) {
                        str = resourcesUrl.openStream();
                        Properties tableMapProperties = new Properties();
                        tableMapProperties.load(str);
                        map.put(tableName + number, tableMapProperties);
                    }
                }
                // load user directory / default directory = ~/.aqfs4c
                String directory = System.getProperty("user.home") + File.separator
                        + StatementUtil.getInstance().getUserReplacementsDir();
                File repfile = new File(directory + File.separator + "replacements.properties");
                if (repfile.exists()) {
                    BufferedReader reader = Files
                            .newBufferedReader(Paths.get(directory + File.separator + "replacements.properties"));
                    Properties repl = new Properties();
                    repl.load(reader);

                    keys = repl.keys();
                    while (keys.hasMoreElements()) {
                        String fileName = (String) keys.nextElement();
                        String tableName = repl.getProperty(fileName).trim();
                        String number = fileName.substring(fileName.indexOf('.'), fileName.lastIndexOf('.'));
                        userMaps.put(tableName + number, fileName);
                        if (new File(directory + File.separator + fileName).exists()) {
                            reader = Files.newBufferedReader(Paths.get(directory + File.separator + fileName));

                            Properties tableMapProperties = new Properties();
                            tableMapProperties.load(reader);
                            map.put(tableName + number, tableMapProperties);
                        }
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isUserMap(String fileName) {
        if (userMaps == null) {
            return false;
        }
        return userMaps.containsKey(fileName);
    }

    /**
     * @param table
     * @return
     */
    public boolean inMapping(String table) {
        if (map == null) {
            return false;
        }
        boolean ret = map.containsKey(table);
        return ret;
    }

    public boolean inReplacements(String table) {
        if (map == null) {
            return false;
        }
        return map.containsKey(table);
    }

    public Properties get(String table) {
        Properties props = new Properties();
        if (map != null) {
            if (map.containsKey(table)) {
                props = map.get(table);
            } else if (map.containsKey(table.toUpperCase())) {
                props = map.get(table.toUpperCase());
            }
        }
        return props;
    }
    private static class LazyTransfomer {
        static final VdmTransformer INSTANCE = new VdmTransformer();
    }
    public static VdmTransformer getInstance() {
        return LazyTransfomer.INSTANCE;
        //return new VdmTransformer();
    }

    String transform(String in, String table) {
        if (map == null) {
            return "";
        }
        String previousToken = "";
        String comma = "";
        StringBuffer sb = new StringBuffer();
        boolean lowercase = false;

        Properties properties = map.get(table);
        if (properties != null) {
            StringTokenizer st = new StringTokenizer(in);

            while (st.hasMoreElements()) {

                String token = st.nextToken();

                lowercase = token.toLowerCase().equals(token);

                if (token.endsWith(",")) {
                    token = token.substring(0, token.length() - 1);
                    // comma = ",";
                }
                if (previousToken.equals("AS")) {
                    sb.append(token); // alias - do not change
                } else {
                    // map database field
                    if (token.contains("~")) {
                        String[] tokenParts = token.split("~");
                        if (properties.containsKey(tokenParts[1].toUpperCase())) {
                            // alias part / qualifier / mapped field
                            sb.append(tokenParts[0] + "~" + properties.get(tokenParts[1].toUpperCase()));
                        } else {
                            sb.append(tokenParts[0] + "~" + tokenParts[1]); // fallback - no match
                        }
                    } else {
                        // host variable
                        if (token.startsWith("@")) {
                            sb.append(token.replaceAll("@", ""));
                        } else if (isOperator(previousToken)) {
                            sb.append(token);
//                        sb.append("@").append(token);
                        } else {
                            if (properties.containsKey(token.toUpperCase())) {
                                if (lowercase) {
                                    sb.append(((String) properties.get(token.toUpperCase())).toLowerCase());
                                } else {
                                    sb.append(properties.get(token));
                                }
                            } else {
                                // no mapping
                                sb.append(token);
                            }
                        }
                    }
                }

                sb.append(comma + " ");
                comma = "";
                previousToken = token.toUpperCase();
            }
        }
        return sb.toString().replaceAll("@", "").replaceAll(",", "");
    }

    public String removeAllLineComments(AbapStatement currentStatement) {
        return currentStatement.replaceAllPattern("([\r\n])\\*.*([\r\n])", "$1$2");
    }

    private boolean isOperator(String token) {
        return OPERATORS.contains(token);
    }
}
