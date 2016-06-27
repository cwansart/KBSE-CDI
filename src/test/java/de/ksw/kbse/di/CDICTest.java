package de.ksw.kbse.di;

import de.ksw.kbse.di.mocks.BarImpl;
import de.ksw.kbse.di.mocks.Foo;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CDICTest {

    private static CDIC cdic;
    private Foo foo;

    @BeforeClass
    public static void setUpClass() {
        cdic = new CDIC();
    }
    
    @Before
    public void setUp() {
        foo = cdic.init(Foo.class);
    }

    @Test
    public void testInitsReturnNotNull() {
        System.out.println("Test if init()'s return value is not null");
        assertTrue(foo != null);
    }

    @Test
    public void testFoosFieldNotNull() {
        System.out.println("Test if Foo's field baz is not null");
        assertTrue(foo.baz != null);
    }

    @Test
    public void testIfBooNotNull() {
        System.out.println("Test if Baz' field boo is not null (recursion test)");
        assertTrue(foo.baz.boo != null);
    }
    
    @Test
    public void testIfIFaceBarNotNull() {
        System.out.println("Test if Baz' interface field bar is not null (recursion test)");
        assertTrue(foo.bar != null);
    }
    
    @Test
    public void testIfIFaceBarIsTypeOfBarImpl() {
        System.out.println("Test if Baz' interface field bar is of type BarImpl");
        assertTrue(foo.bar instanceof BarImpl);
    }

    @Test
    public void testClassIndexer() {
        /*
        Das ist ein tempoäer Test. Wir sollten das später noch
        anpassen und den Test um entsprechende asserts erweitern.
        */
        ClassIndexer classIndexer = new ClassIndexer(Foo.class);
        System.out.println(classIndexer);
    }
}
