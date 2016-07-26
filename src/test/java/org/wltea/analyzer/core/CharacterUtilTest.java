package org.wltea.analyzer.core;

import junit.framework.TestCase;

/**
 * Created by two8g on 16-7-26.
 */
public class CharacterUtilTest extends TestCase {
    public void testIdentifyCharType() throws Exception {

    }

    public void testRegularize() throws Exception {
        char c = 12288;
        assert CharacterUtil.regularize(c) == 32;
        for (c = 65280 + 1; c < 65375; c++) {
            char c1 = CharacterUtil.regularize(c);
            System.out.println(c + "=>" + c1);
            assert c1 == (c - 65248);
        }
        for (c = 'A'; c <= 'Z'; c++) {
            char c1 = CharacterUtil.regularize(c);
            System.out.println(c + "=>" + c1);
            assert c1 == c;
        }
    }

    public void testAcceptChar() throws Exception {

    }

}