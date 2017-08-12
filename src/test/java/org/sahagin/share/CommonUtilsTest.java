package org.sahagin.share;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;

public class CommonUtilsTest {

    @Test
    public void encodedStringTest() {
        String encodedStr1 = CommonUtils.encodeToSafeAsciiFileNameString("abcあde", StandardCharsets.UTF_8);
        String encodedStr2 = CommonUtils.encodeToSafeAsciiFileNameString("abc-", StandardCharsets.UTF_8);
        String encodedStr3 = CommonUtils.encodeToSafeAsciiFileNameString("%", StandardCharsets.UTF_8);
        String originalStr4 = "12345aaaaabbbbbccccc12345aaaaabbbbbccccc";
        String encodedStr4 = CommonUtils.encodeToSafeAsciiFileNameString(originalStr4, StandardCharsets.UTF_8);

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
        assertThat(CommonUtils.encodeToSafeAsciiFileNameString(str, StandardCharsets.UTF_8), is(str));
    }
}
