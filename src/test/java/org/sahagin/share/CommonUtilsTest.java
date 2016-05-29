package org.sahagin.share;

import org.apache.commons.io.Charsets;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class CommonUtilsTest {

    @Test
    public void encodedStringTest() {
        String encodedStr1 = CommonUtils.encodeToSafeAsciiFileNameString("abcあde", Charsets.UTF_8);
        String encodedStr2 = CommonUtils.encodeToSafeAsciiFileNameString("abc-", Charsets.UTF_8);
        String encodedStr3 = CommonUtils.encodeToSafeAsciiFileNameString("%", Charsets.UTF_8);
        String originalStr4 = "12345aaaaabbbbbccccc12345aaaaabbbbbccccc";
        String encodedStr4 = CommonUtils.encodeToSafeAsciiFileNameString(originalStr4, Charsets.UTF_8);

        // check if encodedStr is SHA1 encoded
        assertTrue(encodedStr1.matches("[a-z0-9]+"));
        assertTrue(encodedStr2.matches("[a-z0-9]+"));
        assertTrue(encodedStr3.matches("[a-z0-9]+"));
        assertTrue(encodedStr4.matches("[a-z0-9]+"));
        // SHA1 digest string must be encoded again
        assertThat(encodedStr4, not(is(originalStr4)));
    }

    @Test
    public void notEncodedStringTest() {
        String str = "aAzZ012_.";
        assertThat(CommonUtils.encodeToSafeAsciiFileNameString(str, Charsets.UTF_8), is(str));
    }
}
