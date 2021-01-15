package com.diffmin;

import com.diffmin.App;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        String sampleString = "Hello World!";
        String test = App.sample(sampleString);
        assert test == sampleString;
    }
}
