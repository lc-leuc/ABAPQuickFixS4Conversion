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

import com.abapblog.adt.quickfix.assist.syntax.statements.StatementAssistRegex;

import de.leuc.adt.quickfix.select.SelectFormat;

/**
 * Testing results of SelectNewStyle.
 * 
 * @author lc
 *
 */
class SelectNewStyle extends AbstractTest{

    private static final String TEST_FOLDER = "resources" + File.separator + "select_new_style";

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

   // private static final String replaceBy = de.leuc.adt.quickfix.select.SelectNewStyle.selectPattern;
    private static final String replaceByModern = modernTargetSelectPattern;

    public String getReplacement(String statement, StatementAssistRegex cut) {
        return statement;//.replaceFirst(selectPattern, selectPattern);;
    }
    public String getModernReplacement(String statement, StatementAssistRegex cut) {
        return statement.replaceFirst(cut.getMatchPattern(), replaceByModern);
    }

    @Test
    void replace() {
        replace_internal(TEST_FOLDER, new de.leuc.adt.quickfix.select.SelectNewStyle());
    }
    

}
