import args.Args;
import args.ArgsException;

import static org.junit.jupiter.api.Assertions.*;

public class ArgsTest {
    public void testCreateWithNoSchemaOrArguments() throws Exception {
        Args args = new Args("", new String[0]);
        assertEquals(0, args.cardinality());
    }

    public void testWithNoSchemaButWithOneArgument() throws Exception, ArgsException {
        new Args("", new String[]{"-x"});
        fail();
    }

    public void testWithNoSchemaButWithMultipleArguments() throws Exception, ArgsException {
        new Args("", new String[]{"-x", "-y"});
        fail();
    }

    public void testNonLetterSchema() throws Exception, ArgsException {
        new Args("*", new String[]{});
        fail("Args constructor should have thrown exception");
    }

    public void testInvalidArgumentFormat() throws Exception, ArgsException {
        new Args("f~", new String[]{});
        fail("Args constructor should have throws exception");
    }

    public void testSimpleBooleanPresent() throws Exception {
        Args args = new Args("x", new String[]{"-x"});
        assertEquals(1, args.cardinality());
        assertEquals(true, args.getBoolean('x'));
    }

    public void testSimpleStringPresent() throws Exception {
        Args args = new Args("x*", new String[]{"-x", "param"});
        assertEquals(1, args.cardinality());
        assertTrue(args.has('x'));
        assertEquals("param", args.getString('x'));
    }

    public void testMissingStringArgument() throws Exception, ArgsException {
        new Args("x*", new String[]{"-x"});
        fail();
    }

    public void testSpacesInFormat() throws Exception {
        Args args = new Args("x, y", new String[]{"-xy"});
        assertEquals(2, args.cardinality());
        assertTrue(args.has('x'));
        assertTrue(args.has('y'));
    }

    public void testSimpleIntPresent() throws Exception {
        Args args = new Args("x#", new String[]{"-x", "42"});
        assertEquals(1, args.cardinality());
        assertTrue(args.has('x'));
        assertEquals(42, args.getInt('x'));
    }

    public void testInvalidInteger() throws Exception, ArgsException {
        new Args("x#", new String[]{"-x", "Forty two"});
        fail();
    }

    public void testMissingInteger() throws Exception, ArgsException {
        new Args("x#", new String[]{"-x"});
        fail();
    }

    public void testSimpleDoublePresent() throws Exception {
        Args args = new Args("x##", new String[]{"-x", "42.3"});
        assertEquals(1, args.cardinality());
        assertTrue(args.has('x'));
        assertEquals(42.3, args.getDouble('x'), .001);
    }

    public void testInvalidDouble() throws Exception {
        new Args("x##", new String[]{"-x", "Forty two"});
        fail();
    }

    public void testMissingDouble() throws Exception {
        new Args("x##", new String[]{"-x"});
        fail();
    }
}
