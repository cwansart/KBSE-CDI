package de.ksw.kbse.di;

import de.ksw.kbse.di.mocks.Foo;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

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
    
    @Test
    public void testIfBooNotNull() {
        System.out.println("Test if Baz' field boo is not null (recursion test)");
        Foo foo = cdic.init(Foo.class);
        assertTrue(foo.baz.boo != null);
    }
}
