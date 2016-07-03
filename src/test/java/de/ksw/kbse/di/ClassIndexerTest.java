package de.ksw.kbse.di;

import de.ksw.kbse.di.mocks.Bar;
import de.ksw.kbse.di.mocks.Baz;
import de.ksw.kbse.di.mocks.Foo;
import de.ksw.kbse.di.mocks.MyQualifier;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClassIndexerTest {

    private static ClassIndexer classIndexer;

    @BeforeClass
    public static void setUpClass() {
        classIndexer = new ClassIndexer(Foo.class);
    }

    @Test
    public void testInterface() {
        System.out.println("Test if interface type was indexed");
        assertTrue(classIndexer.getInterfaceFile(Bar.class.getName()) != null);
    }

    @Test
    public void testIfNotInterface() {
        System.out.println("Test is class is not indexed as interface");
        assertTrue(classIndexer.getInterfaceFile(Baz.class.getName()) == null);
    }

    @Test
    public void testNamedQualifier() {
        System.out.println("Test is @Named qualified class was indexed");
        assertTrue(classIndexer.getNamedFile("namedClass") != null);
    }

    @Test
    public void testIfNotNamed() {
        System.out.println("Test if unknown named qualifier was not indexed");
        assertTrue(classIndexer.getNamedFile("doesNotExist") == null);
    }

    @Test
    public void testQualifier() {
        System.out.println("Test if qualifier was indexed");
        assertTrue(classIndexer.getQualifierFile(MyQualifier.class.getName()) != null);
    }
    
    @Test
    public void testIfNotQualifier() {
        System.out.println("Test if unknown qualifier was not indexed");
        assertTrue(classIndexer.getQualifierFile(Foo.class.getName()) == null);
    }
}
