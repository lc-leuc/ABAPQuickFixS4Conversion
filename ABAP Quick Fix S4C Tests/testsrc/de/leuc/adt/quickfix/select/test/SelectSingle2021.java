/**
 * 
 */
package de.leuc.adt.quickfix.select.test;

import java.io.File;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.abapblog.adt.quickfix.assist.syntax.statements.StatementAssistRegex;

/**
 * Testing results of SelectSingle.
 * 
 * @author lc
 *
 */
class SelectSingle2021 extends AbstractTest {

    private static final String TEST_FOLDER = "resources" + File.separator + "select_single2021";

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

    // we are testing without order by clause, i.e. an additional end of statement
    // (dot) is necessary

    private static final String modernTargetSelectPatternStart = de.leuc.adt.quickfix.select.SelectSingle.modernTargetSelectPatternStart;
    private static final String modernTargetSelectPatternEnd = de.leuc.adt.quickfix.select.SelectSingle.modernTargetSelectPatternEnd;

    private static final String replaceByModern = modernTargetSelectPatternStart + modernTargetSelectPatternEnd;

    String getReplacement(String statement, StatementAssistRegex cut) {
        return statement.replaceFirst(((de.leuc.adt.quickfix.select.SelectSingle2021)cut).getMatchPattern(), replaceByModern);
    }

    String getModernReplacement(String statement, StatementAssistRegex cut) {
        return statement.replaceFirst(((de.leuc.adt.quickfix.select.SelectSingle2021)cut).getMatchPattern(), replaceByModern);
    }

    @Test
    void replace() {
        replace_internal(TEST_FOLDER, new de.leuc.adt.quickfix.select.SelectSingle2021( ));
    }

}
