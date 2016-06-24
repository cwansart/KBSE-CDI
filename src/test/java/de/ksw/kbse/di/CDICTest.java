/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ksw.kbse.di;

import de.ksw.kbse.di.mocks.Foo;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Christian
 */
public class CDICTest {
    
    private static CDIC cdic;
    
    @BeforeClass
    public static void setUpClass() {
        cdic = new CDIC();
    }

    @Test
    public void testInitsReturnNotNull() {
        System.out.println("Test if init()'s return value is not null");
        Foo foo = cdic.init(Foo.class);
        assertTrue(foo != null);
    }
    
    @Test
    public void testFoosFieldNotNull() {
        System.out.println("Test if Foo's field baz is not null");
        Foo foo = cdic.init(Foo.class);
        assertTrue(foo.baz != null);
    }
}
