/**
 * 
 */
package de.leuc.adt.quickfix.select.test;

import static org.junit.jupiter.api.Assertions.*;

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

    /**
     * Capturing Groups * 1 - leading line breaks * 2 - leading spaces * 3 - word
     * "single" * 4 - field list * 5 - table * 6 - into-data statement * 7 - where
     * statement
     */

    private String breaks = "";

//    private static final String replaceBy = "select $4 from $5 into $6 up to 1 rows where $7. endselect";
//    private static final String replaceByModern = "select from $5 fields $4 where $7 into $6 up to 1 rows. endselect";
    
    private static final String replaceBy = "$3 $5 $6 $7 $8 $9 up to 1 rows $10 $11. endselect";
    private static final String replaceByModern = "$3 $6 $7 fields $5 $10 $11 $8 $9 up to 1 rows. endselect";
    

    @Test
    void replace() {

        HashMap<String, String> originals = new HashMap<String, String>();
        HashMap<String, String> results = new HashMap<String, String>();
        HashMap<String, String> newStyles = new HashMap<String, String>();

        try {
            // test with strings from files in resources
            File dir = new File("resources");
            if (dir.isDirectory()) {
                String[] list = dir.list();
                for (String file : list) {
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
                System.out.println(path);
                System.out.println(outString);

                de.leuc.adt.quickfix.select.SelectSingle cut = new de.leuc.adt.quickfix.select.SelectSingle();
                String selectPattern = cut.getMatchPattern();

                Pattern pattern = Pattern.compile(selectPattern, Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(outString);

                
                String actString = outString.replaceFirst(selectPattern, replaceBy);

                actString = actString.replaceAll(" \\.", ".");
//                String controlString = outString.replaceFirst(selectPattern, "|$7|");
//                System.out.println(controlString);

                breaks = outString.replaceFirst(selectPattern, "$1");

                System.out.println("breaks " + breaks);
                for (int i = 1; i < 12; i++) {
                    System.out.println("" + i + "  |" + outString.replaceFirst(selectPattern, "$" + i) + "|");
                }

                System.out.println("matches " + matcher.matches());
                System.out.println("find    " + matcher.find());

                System.out.println("actString |" + actString + "|");
                System.out.println("expString |" + results.get(path) + "\n|");
//                        + "select * from wbit into @data(result) up to 1 rows where tkonn eq @tkonn and tposn = @tposn. endselect\n"
//                        + "|");
                
        		SelectFormat formatter = new SelectFormat(outString.contains("select")); // guess case

                String[] s = formatter.split(actString);
                StringBuffer sb = new StringBuffer();

                for (String string : s) {
                    sb.append(formatter.format(string, "", "select"));
                }
                System.out.println(sb.toString());

                String[] r = formatter.split(results.get(path));
                StringBuffer rb = new StringBuffer();

                for (String string : r) {
                    // System.out.println(string);
                    rb.append(formatter.format(string, "", "select"));
                }
                System.out.println(rb.toString());
                
                
                assertEquals(sb.toString(), rb.toString());

                // assertEquals(actString, results.get(path) + "\n");
                // "select * from wbit into @data(result) up to 1 rows where tkonn eq @tkonn and
                // tposn = @tposn. endselect\n");

            
                ////////////////////////////////////////////////
                
                String actString2 = outString.replaceFirst(selectPattern, replaceByModern);

            

                System.out.println("actString 2021 |" + actString2 + "|");
                System.out.println("expString 2021 |" + newStyles.get(path) + "\n|");

                String[] s2 = formatter.split(actString2);
                StringBuffer sb2 = new StringBuffer();

                for (String string : s2) {
                    sb2.append(formatter.format(string, "", "select"));
                }
                System.out.println(sb2.toString());

                String[] r2 = formatter.split(newStyles.get(path));
                StringBuffer rb2 = new StringBuffer();

                for (String string : r2) {
                    rb2.append(formatter.format(string, "", "select"));
                }
                System.out.println(rb2.toString());
                
                
                assertEquals(sb2.toString(), rb2.toString());

            
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
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
        String outString;
        Scanner in = new Scanner(new FileReader(path));
        StringBuilder sb = new StringBuilder();
        while (in.hasNext()) {
            String text = in.next();
            sb.append(text.concat(" "));
        }
        in.close();
        return sb.toString().trim();
    }

}
