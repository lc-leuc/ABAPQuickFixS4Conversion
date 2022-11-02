/**
 * 
 */
package de.leuc.adt.quickfix.select.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.leuc.adt.quickfix.select.SelectFormat;

/**
 * @author lc
 *
 */
class SelectNewStyle {

	/**
     * @throws java.lang.Exception
     */
    @BeforeAll
    static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterAll
    static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterEach
    void tearDown() throws Exception {
    }
  
    private static final String modernTargetSelectPattern = de.leuc.adt.quickfix.select.SelectNewStyle.modernTargetSelectPattern;
  
    
    
    private static final String replaceByModern = modernTargetSelectPattern;
  
    @Test
    void replace() {

        HashMap<String, String> originals = new HashMap<String, String>();
        HashMap<String, String> results = new HashMap<String, String>();
        HashMap<String, String> newStyles = new HashMap<String, String>();

        try {
            // test with strings from files in resources
            File dir = new File("resources" + File.separator + "select_new_style");
            if (dir.isDirectory()) {
                String[] list = dir.list();
                for (String file : list) {
                    if (file.equals( "attic" ) ) {
                        continue;
                    }
                    if ( ! file.startsWith("result-") && ! file.startsWith("2021-")) {
                        String textFilePath = dir.getPath() + File.separator + file;
                        System.out.println(textFilePath);
                        originals.put(textFilePath, readTextFile(textFilePath));
                        results.put(textFilePath, readResultFile(dir.getPath() + File.separator + "result-" + file));
                        newStyles.put(textFilePath, readResultFile(dir.getPath() + File.separator + "2021-result-" + file));
                    }
                }
            }

            Iterator<String> it = originals.keySet().iterator();
            while (it.hasNext()) {
                String path = (String) it.next();

                String statement = "";
                statement = originals.get(path);// reatTextFile("resources/select_with_pre_line.txt");
                System.out.println("====================================================================");
                System.out.println("Testing: "+ path);
                System.out.println("====================================================================");
                System.out.println("Current Select Statement:");
                System.out.println(statement);
                System.out.println("--------------------------------------------------------------------");

                de.leuc.adt.quickfix.select.SelectNewStyle cut = new de.leuc.adt.quickfix.select.SelectNewStyle();
                String selectPattern = cut.getMatchPattern();

                Pattern pattern = Pattern.compile(selectPattern, Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(statement);


                SelectFormat formatter = new SelectFormat(statement.contains("select")); // guess case
                
                // remove all line feed characters and leading spaces
                statement = statement.replaceAll("[\r\n]", "").trim();
//                // remember the current table, in order to determine order-by statement
//                currentTable = statement.replaceFirst(getMatchPattern(), "${table}").replaceFirst( "(.*)\\s+as\\s+.*", "$1" ).trim();
                // do the actual replacement
                String replacement = statement;//.replaceFirst(selectPattern, replaceBy);
                // format 
                String newStatement = formatter.format("", replacement, "select");
                 
                
                replacement = replacement.replaceAll(" \\.", ".");
//                String controlString = outString.replaceFirst(selectPattern, "|$7|");
//                System.out.println(controlString);

                String breaks = replacement.replaceFirst(selectPattern, "$1");

                System.out.println();                
                System.out.println("Parts of Select Statement:" );
                for (int i = 1; i < 12; i++) {
                    System.out.println("" + i + "  |" + statement.replaceFirst(selectPattern, "$" + i) + "|");
                }
                System.out.println("--------------------------------------------------------------------");
                System.out.println();                

                System.out.println("Matcher matches: " + matcher.matches());
                System.out.println("Matcher find:    " + matcher.find());

                System.out.println("--------------------------------------------------------------------");
                System.out.println();                

                System.out.println("Current Statement:  |" + replacement + "|");
                System.out.println("Expected Statement: |" + results.get(path) + "|");

                System.out.println("--------------------------------------------------------------------");
                System.out.println("Current Statement (formatted):");                

                
                System.out.println(newStatement.toString());

                System.out.println("--------------------------------------------------------------------");
                System.out.println("Expected Statement (indentation and line breaks):");                                
                String rb = results.get(path);
                System.out.println(rb.toString());

///////////////////////////////////////// Differences
//                System.out.println("--------------------------------------------------------------------");
//                System.out.println("Diff statements: ");                                
//                System.out.println();
//                List<Diff> diffs = new DiffMatchPatch().diff_main(newStatement, rb);
//                //System.out.println(diffs);
//                diffs.forEach( new Consumer<Diff>() { 
//                    public void accept(Diff t) {
//                        System.out.println( t.operation  +"\n" + t.text  + "\n " + t.operation ); 
//                        } 
//                    } );
//                System.out.println("\n--------------------------------------------------------------------\n"
//                        + "Differences:");
//                for (Diff diff : diffs) {
//                    if (diff.operation == Operation.INSERT) {
//                      System.out.println(diff.text); 
//                    }
//                  }
//                System.out.println("--------------------------------------------------------------------");
////////////////////////////////////////
                
                assertEquals(newStatement.toString(), rb.toString());

                // assertEquals(actString, results.get(path) + "\n");
                // "select * from wbit into @data(result) up to 1 rows where tkonn eq @tkonn and
                // tposn = @tposn. endselect\n");

            
                ////////////////////////////////////////////////
                
                String replacement2021 = statement.replaceFirst(selectPattern, replaceByModern);

            
                System.out.println();
                System.out.println("--------------------------------------------------------------------");
                System.out.println("Testing newest style (2021): " + path);
                System.out.println("--------------------------------------------------------------------");
                System.out.println("actString 2021 |" + replacement2021 + "|");
                System.out.println("expString 2021 |" + newStyles.get(path) + "\n|");

                
                String sb2 = formatter.format("", replacement2021, "select");
                System.out.println("--------------------------------------------------------------------");
                System.out.println("Current Statement (2021)");
                System.out.println(sb2.toString());

               // String rb2 = formatter.format("", newStyles.get(path), "select");
                String rb2 = newStyles.get(path);
                System.out.println("--------------------------------------------------------------------");
                System.out.println("Expected Statement (2021)");
                System.out.println(rb2.toString());
                
///////////////////////////////////////// Differences
//                System.out.println("--------------------------------------------------------------------");
//                System.out.println("Diff statements: ");                                
//                System.out.println();
//                List<Diff> diffs2 = new DiffMatchPatch().diff_main(sb2, rb2);
//                diffs2.forEach( new Consumer<Diff>() { 
//                public void accept(Diff t) {
//                  System.out.println( t.operation  +"\n" + t.text  + "\n " + t.operation ); 
//                  } 
//                } );
//                System.out.println("\n--------------------------------------------------------------------\n"
//                  + "Differences:");
//                for (Diff diff : diffs2) {
//                if (diff.operation == Operation.INSERT) {
//                System.out.println(diff.text); 
//                }
//                }
//                System.out.println("--------------------------------------------------------------------");
////////////////////////////////////////

                assertEquals(sb2.toString(), rb2.toString());

            
            }

            for (String file : dir.list()) {
                if (file.equals( "attic" ) ) {
                    continue;
                }
                if ( ! file.startsWith("result-") && ! file.startsWith("2021-")) {
                    String textFilePath = dir.getPath() + File.separator + file;
                    System.out.println(textFilePath);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

	private String readTextFile(String path) throws FileNotFoundException {
        String outString;
        Scanner in = new Scanner(new FileReader(path));
        StringBuilder sb = new StringBuilder();
        while (in.hasNext()) {
            String text = in.next();
            if (text.substring(text.length() - 1, text.length()).equals(".")) {
                sb.append(text.concat("\n"));
            } else {

                sb.append(text.concat(" "));
            }
        }
        in.close();
        return sb.toString();
    }

    private String readResultFile(String path) throws FileNotFoundException {
        Scanner in = new Scanner(new FileReader(path));
        StringBuilder sb = new StringBuilder();
        while (in.hasNext()) {
            String text = in.next();
            sb.append(text.concat(" "));
        }
        in.close();
        SelectFormat sf = new SelectFormat(true);
        String[] strings = sf.split(sb.toString().trim());
        sb = new StringBuilder();
        for (String str : strings) {
            String s = str.toLowerCase();
            if (s.startsWith("from") || s.startsWith("into") || s.startsWith("up") || s.startsWith("where")  || s.startsWith("fields")  ) {
              sb.append("  ");
            } else if ( s.startsWith("and") ) {
                sb.append("    ");
            } else if ( s.startsWith("or") ) {
                sb.append("     ");
            }
            sb.append(str
                    .replaceFirst("(?i)(into)(corresponding)(fields)(of)(table) ", "$1 $2 $3 $4 $5 " ) 
                    .replaceFirst("(?i)(into)(corresponding)(fields)(of) ", "$1 $2 $3 $4 " )
                    .trim()
                    );
            sb.append("\n");
        }
        return sb.toString().trim();
    }

}
