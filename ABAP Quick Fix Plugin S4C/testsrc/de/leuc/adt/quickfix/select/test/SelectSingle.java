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
class SelectSingle {

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
  
    // we are testing without order by clause, i.e. an additional end of statement (dot) is necessary
    private static final String targetSelectPatternStart = de.leuc.adt.quickfix.select.SelectSingle.targetSelectPatternStart + ".";
    //"${select} ${fields} ${from} ${table} ${into} ${variable} up to 1 rows ${where} ${condition}.";
    private static final String targetSelectPatternEnd = de.leuc.adt.quickfix.select.SelectSingle.targetSelectPatternEnd;
    //"endselect";
    private static final String modernTargetSelectPatternStart = de.leuc.adt.quickfix.select.SelectSingle.modernTargetSelectPatternStart;
            //"${select} ${from} ${table} fields ${fields} ${where} ${condition}";
    private static final String modernTargetSelectPatternEnd = de.leuc.adt.quickfix.select.SelectSingle.modernTargetSelectPatternEnd;
            //" ${into} ${variable} up to 1 rows. endselect";

    private static final String replaceBy = targetSelectPatternStart + targetSelectPatternEnd;
    private static final String replaceByModern = modernTargetSelectPatternStart + modernTargetSelectPatternEnd;

    @Test
    void replace() {

        HashMap<String, String> originals = new HashMap<String, String>();
        HashMap<String, String> results = new HashMap<String, String>();
        HashMap<String, String> newStyles = new HashMap<String, String>();

        try {
            // test with strings from files in resources
            File dir = new File("resources" + File.separator + "select_single");
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

                String outString = "";
                outString = originals.get(path);// reatTextFile("resources/select_with_pre_line.txt");
                System.out.println("====================================================================");
                System.out.println("Testing: "+ path);
                System.out.println("====================================================================");
                System.out.println("Current Select Statement:");
                System.out.println(outString);
                System.out.println("--------------------------------------------------------------------");

                de.leuc.adt.quickfix.select.SelectSingle cut = new de.leuc.adt.quickfix.select.SelectSingle();
                String selectPattern = cut.getMatchPattern();

                Pattern pattern = Pattern.compile(selectPattern, Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(outString);

                
                String actString = outString.replaceFirst(selectPattern, replaceBy);

                actString = actString.replaceAll(" \\.", ".");
//                String controlString = outString.replaceFirst(selectPattern, "|$7|");
//                System.out.println(controlString);

                String breaks = outString.replaceFirst(selectPattern, "$1");

                System.out.println();                
                System.out.println("Parts of Select Statement:" );
                for (int i = 1; i < 12; i++) {
                    System.out.println("" + i + "  |" + outString.replaceFirst(selectPattern, "$" + i) + "|");
                }
                System.out.println("--------------------------------------------------------------------");
                System.out.println();                

                System.out.println("Matcher matches: " + matcher.matches());
                System.out.println("Matcher find:    " + matcher.find());

                System.out.println("--------------------------------------------------------------------");
                System.out.println();                

                System.out.println("Current Statement:  |" + actString + "|");
                System.out.println("Expected Statement: |" + results.get(path) + "|");

                System.out.println("--------------------------------------------------------------------");
                System.out.println("Current Statement (formatted):");                

                
        		SelectFormat formatter = new SelectFormat(outString.contains("select")); // guess case
                String sb = formatter.format("", actString, "select");
                System.out.println(sb.toString());

                System.out.println("--------------------------------------------------------------------");
                System.out.println("Expected Statement (indentation and line breaks):");                                
//                String rb = formatter.format("",results.get(path), "select");
                String rb = results.get(path);
                System.out.println(rb.toString());

///////////////////////////////////////// Differences
//                System.out.println("--------------------------------------------------------------------");
//                System.out.println("Diff statements: ");                                
//                System.out.println();
//                List<Diff> diffs = new DiffMatchPatch().diff_main(sb, rb);
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
                
                assertEquals(sb.toString(), rb.toString());

                // assertEquals(actString, results.get(path) + "\n");
                // "select * from wbit into @data(result) up to 1 rows where tkonn eq @tkonn and
                // tposn = @tposn. endselect\n");

            
                ////////////////////////////////////////////////
                
                String actString2 = outString.replaceFirst(selectPattern, replaceByModern);

            
                System.out.println();
                System.out.println("--------------------------------------------------------------------");
                System.out.println("Testing newest style (2021): " + path);
                System.out.println("--------------------------------------------------------------------");
                System.out.println("actString 2021 |" + actString2 + "|");
                System.out.println("expString 2021 |" + newStyles.get(path) + "\n|");

                
                String sb2 = formatter.format("", actString2, "select");
                System.out.println("Current Statement (2021)");
                System.out.println(sb2.toString());

               // String rb2 = formatter.format("", newStyles.get(path), "select");
                String rb2 = newStyles.get(path);
                System.out.println("Expected Statement (2021)");
                System.out.println(rb2.toString());
                
                assertEquals(sb2.toString(), rb2.toString());

            
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
            } else if (s.startsWith("and") || s.startsWith("or")) {
                sb.append("    ");
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
