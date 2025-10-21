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
class SelectSingle extends AbstractTest {

    private static final String TEST_FOLDER = "resources" + File.separator + "select_single";

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
    private static final String TARGETSELECTPATTERNSTART = de.leuc.adt.quickfix.select.SelectSingle.TARGETSELECTPATTERNSTART;

    private static final String TARGETSELECTPATTERNEND = de.leuc.adt.quickfix.select.SelectSingle.TARGETSELECTPATTERNEND;
    private static final String MODERNTARGETSELECTPATTERNSTART = de.leuc.adt.quickfix.select.SelectSingle.MODERNTARGETSELECTPATTERNSTART;
    private static final String MODERNTARGETSELECTPATTERNEND = de.leuc.adt.quickfix.select.SelectSingle.MODERNTARGETSELECTPATTERNEND;

    private static final String REPLACEBY = TARGETSELECTPATTERNSTART + TARGETSELECTPATTERNEND;
    private static final String REPLACEBYMODERN = MODERNTARGETSELECTPATTERNSTART + MODERNTARGETSELECTPATTERNEND;

    String getReplacement(String statement, StatementAssistRegex cut) {
        return statement.replaceFirst(cut.getMatchPattern(), REPLACEBY);
    }

    String getModernReplacement(String statement, StatementAssistRegex cut) {
        return statement.replaceFirst(cut.getMatchPattern(), REPLACEBYMODERN);
    }

    @Test
    void replace() {
        replace_internal(TEST_FOLDER, new de.leuc.adt.quickfix.select.SelectSingle( ));
    }

}
