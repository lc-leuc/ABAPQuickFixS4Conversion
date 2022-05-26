package de.leuc.adt.quickfix.select.test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class ReplaceTest {
   
    /**
     * Capturing Groups
     * * 1 - leading line breaks
     * * 2 - leading spaces
     * * 3 - word "single"
     * * 4 - field list
     * * 5 - table
     * * 6 - into-data statement
     * * 7 - where statement 
     */
    private static final String selectPattern = 
            //      select     single      *      from     wbhk  into  @data(result) where tkonn = ''.
            //   1          2               3           4               5               6                7                        
            "(?i)([\n\r]*)(\\s*)select\\s+(single)\\s+(.*)\\s+from\\s+(.*)\\s+into\\s+(.*)\\s+where\\s+(.*)\\.";
      //    "(?i)(\\s*)select\\s+(single)\\s+(.*)\\s+from\\s+(.*)\\s+into\\s+(.*)\\s+where\\s+(.*)(order by .*)?\\.";  // optional order by

    private String breaks = "";
    
    private static final String r1 = "select $4 from $5";
    private static final String r2 = "into $6";
    private static final String r3 = "up to 1 rows";
    private static final String r4 = "where";
    private static final String r5 = "  $7";
    private static final String r6 = "endselect.";
//    private static final String r7 = "";
//    private static final String r8 = "endselect";
//  private static final String replaceBy = r1 + "\n" + r2 + "\n" + r3 + "\n" + r4 + "\n" + r5 + "\n" + r6;
  private static final String replaceBy = r1 + " " + r2 + " " + r3 + " "  + r4 + " " + r5 + " " + r6;
//    private static final String replaceBy = r1 + r2 + r3 +  r4 + r5 + r6;
    
    @Test
    void test() {
        
        try {
            Scanner in = new Scanner(new FileReader("test2.txt"));
            
//            while (in.hasNext()) {
//                String text = in.next() ;
//                System.out.println(text);
//            }
            
            StringBuilder sb = new StringBuilder();
            while(in.hasNext()) {
                String text = in.next();
                if( text.substring(text.length()-1, text.length()).equals(".") ) {
                    sb.append(text.concat("\n"));                    
                }else{
                    
                    sb.append(text.concat(" "));
                }
            }
            in.close();
            String outString = sb.toString();
            
//            outString =  "\r\n"//"data tkonn type tkonn. "
//                        +  "\r\n"//+ "tkonn = '1100000000'. "
//                     + "    select single *\n      FROM wbit \"\"test\n      iNto @data(result)\r\n      where tkonn = @tkonn and tposn = @tposn.";
            System.out.println(outString);
            
            
            Pattern pattern = Pattern.compile(selectPattern, Pattern.MULTILINE );
            Matcher matcher = pattern.matcher(outString);
            
            
            String newString = outString.replaceFirst(selectPattern, replaceBy);
            
            breaks = outString.replaceFirst(selectPattern,"$1");
            String second = outString.replaceFirst(selectPattern,"$2");
            String third = outString.replaceFirst(selectPattern,"$3");
            String fourth = outString.replaceFirst(selectPattern,"$4");
            
            System.out.println(matcher.matches());
            System.out.println("breaks " + breaks);
            System.out.println("second _" + second + "_");
            System.out.println("third  " + third);
            System.out.println("fourth " + fourth);
            
            System.out.println(matcher.find());

            System.out.println(newString);
            String[] s = split(newString.replaceAll("\\s\\s*", " "));
            
            for (String string : s) {                
                System.out.println(format(string)+string);
            }
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       
    }
    private String[] split(String in) {
        String[] r = new String[]{};
        String from = "(?i)(?=from )";
        r = in.split( from  + "|(?=into )" + "|(?=up\\sto )" + "|(?=where )" + "|(?=and )" + "|(?=or )"  + "|(?=endselect)" + "|(?=order )" + "|(?=group )" );
        return r;
    }
    private String originalIndentation = "  ";
    private String format(String in) {
        if (in.startsWith("select ")) {
            return breaks + originalIndentation;
        }else if(in.startsWith("and ")) {
            return originalIndentation + "    ";
        }else if(in.startsWith("or ")) {
            return originalIndentation + "     ";
        }else if(in.startsWith("endselect")) {
            return originalIndentation;
        }else{
            return originalIndentation + "  ";
        }
        
        
    }
/*
    *
    *    select single * from wbit into @data(result)
    *    where tkonn eq @tkonn
    *    and tposn = @tposn
        and tposn = @tposn
        and tposn = @tposnselect * from wbit
        and tposn = @tposn  into @data(result)   
        and tposn = @tposn  up to 1 rows
        and tposn = @tposn  where
        and tposn = @tposn    tkonn eq @tkonn    and tposn = @tposn
        and tposn = @tposn  order by primary key.
        and tposn = @tposnendselect..
*/
}
