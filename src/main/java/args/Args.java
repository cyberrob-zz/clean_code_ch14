package args;

import marshaler.DoubleArgumentMarshaler;

import java.text.ParseException;
import java.util.*;

public class Args {
    private String schema;
    //    private String[] args;
    private List<String> argsList;
    private boolean valid = true;
    private Set<Character> unexpectedArguments = new TreeSet<Character>();
    //    private Map<Character, ArgumentMarshaler> booleanArgs = new HashMap<Character, ArgumentMarshaler>();
//    private Map<Character, ArgumentMarshaler> stringArgs = new HashMap<Character, ArgumentMarshaler>();
//    private Map<Character, ArgumentMarshaler> intArgs = new HashMap<Character, ArgumentMarshaler>();
    private Map<Character, ArgumentMarshaler> marshalers = new HashMap<Character, ArgumentMarshaler>();
    private Set<Character> argsFound = new HashSet<Character>();
    private Iterator<String> currentArgument;
    private char errorArgument = '\0';
    private String errorParameter = "TILT";

    private ArgsException.ErrorCode errorCode = ArgsException.ErrorCode.OK;

    public Args(String schema, String[] args) throws ParseException {
        this.schema = schema;
        this.argsList = Arrays.asList(args);
        valid = parse();
    }

    private boolean parse() throws ParseException {
        if (schema.length() == 0 && argsList.size() == 0)
            return true;
        parseSchema();
        try {
            parseArguments();
        } catch (ArgsException e) {
            e.printStackTrace();
        }
        return valid;
    }

    private boolean parseSchema() throws ParseException {
        for (String element : schema.split(",")) {
            if (element.length() > 0) {
                String trimmedElement = element.trim();
                parseSchemaElement(trimmedElement);
            }
        }
        return true;
    }

    private void parseSchemaElement(String element) throws ParseException {
        char elementId = element.charAt(0);
        String elementTail = element.substring(1);
        validateSchemaElementId(elementId);
        if (elementTail.length() == 0) {
//            parseBooleanSchemaElement(elementId);
            marshalers.put(elementId, new BooleanArgumentMarshaler());
        } else if (elementTail.equals("*")) {
//            parseStringSchemaElement(elementId);
            marshalers.put(elementId, new StringArgumentMarshaler());
        } else if (elementTail.equals("#")) {
//            parseIntegerSchemaElement(elementId);
            marshalers.put(elementId, new IntegerArgumentMarshaler());
        } else if (elementTail.equals("##")) {
            marshalers.put(elementId, new DoubleArgumentMarshaler());
        }
    }

    private void validateSchemaElementId(char elementId) throws ParseException {
        if (!Character.isLetter(elementId)) {
            throw new ParseException(
                    "Bad character:" + elementId + "in Args format: " + schema, 0);
        }
    }

//    private void parseStringSchemaElement(char elementId) {
//        ArgumentMarshaler m = new StringArgumentMarshaler();
//        stringArgs.put(elementId, m);
//        marshalers.put(elementId, new StringArgumentMarshaler());
//    }
//
//    private boolean isStringSchemaElement(String elementTail) {
//        return elementTail.equals("*");
//    }
//
//    private boolean isBooleanSchemaElement(String elementTail) {
//        return elementTail.length() == 0;
//    }
//
//    private void parseBooleanSchemaElement(char elementId) {
//        ArgumentMarshaler m = new BooleanArgumentMarshaler();
//        booleanArgs.put(elementId, m);
//        marshalers.put(elementId, new BooleanArgumentMarshaler());
//    }

//    private boolean isIntSchemaElement(String elementTail) {
//        return elementTail.equals("#");
//    }
//
//    private void parseIntegerSchemaElement(char elementId) {
//        ArgumentMarshaler m = new IntegerArgumentMarshaler();
//        intArgs.put(elementId, m);
//        marshalers.put(elementId, new IntegerArgumentMarshaler());
//    }

    private boolean parseArguments() throws ArgsException {
        for (currentArgument = argsList.iterator(); currentArgument.hasNext(); ) {
            String arg = currentArgument.next();
            parseArgument(arg);
        }
        return true;
    }

    private void parseArgument(String arg) throws ArgsException {
        if (arg.startsWith("-"))
            parseElements(arg);
    }

    private void parseElements(String arg) throws ArgsException {
        for (int i = 1; i < arg.length(); i++)
            parseElement(arg.charAt(i));
    }

    private void parseElement(char argChar) throws ArgsException {
        if (setArgument(argChar))
            argsFound.add(argChar);
        else {
            unexpectedArguments.add(argChar);
            valid = false;
        }
    }

    private boolean setArgument(char argChar) throws ArgsException {
        ArgumentMarshaler m = marshalers.get(argChar);
        if (m == null) {
            return false;
        }
        try {
//            if (m instanceof BooleanArgumentMarshaler)
//                m.set(currentArgument);
//            else if (m instanceof StringArgumentMarshaler)
//                m.set(currentArgument);
//            else if (m instanceof IntegerArgumentMarshaler)
//                m.set(currentArgument);
            m.set(currentArgument);
            return true;
        } catch (ArgsException e) {
            valid = false;
//            errorArgumentId = argChar;
            throw e;
        }
    }

//    private void setStringArg(ArgumentMarshaler m) throws ArgsException {
//        try {
//            m.set(currentArgument.next());
//        } catch (ArrayIndexOutOfBoundsException e) {
//            errorCode = ErrorCode.MISSING_STRING;
//            throw new ArgsException();
//        }
//    }

//    private void setBooleanArg(ArgumentMarshaler m, Iterator<String> currentArgument) {
//        try {
//            m.set("true"); // was: booleanArgs.get(argChar).set("true");
//        } catch (ArgsException e) {
//        }
//    }

//    private void setIntArg(ArgumentMarshaler m) throws ArgsException {
//        String parameter = null;
//        try {
//            parameter = currentArgument.next();
//            m.set(parameter);
//        } catch (ArrayIndexOutOfBoundsException e) {
//            errorCode = ErrorCode.MISSING_INTEGER;
//            throw new ArgsException();
//        } catch (ArgsException e) {
//            errorParameter = parameter;
//            errorCode = ErrorCode.INVALID_INTEGER;
//            throw e;
//        }
//    }

    public int cardinality() {
        return argsFound.size();
    }

    public String usage() {
        if (schema.length() > 0)
            return "-[" + schema + "]";
        else
            return "";
    }

    public String errorMessage() throws Exception {
        if (unexpectedArguments.size() > 0) {
            return unexpectedArgumentMessage();
        } else
            switch (errorCode) {
                case MISSING_STRING:
                    return String.format("Could not find string parameter for -%c.",
                            errorArgument);
                case OK:
                    throw new Exception("TILT: Should not get here.");
            }
        return "";
    }

    private String unexpectedArgumentMessage() {
        StringBuffer message = new StringBuffer("Argument(s) -");
        for (char c : unexpectedArguments) {
            message.append(c);
        }
        message.append(" unexpected.");
        return message.toString();
    }

    public boolean getBoolean(char arg) {
        Args.ArgumentMarshaler am = marshalers.get(arg);
        boolean b = false;
        try {
            b = am != null && (Boolean) am.get();
        } catch (ClassCastException e) {
            b = false;
        }
        return b;
    }

    private boolean falseIfNull(Boolean b) {
        return b == null ? false : b;
    }

    public String getString(char args) {
        ArgumentMarshaler am = marshalers.get(args);
        try {
            return am == null ? "" : (String) am.get();
        } catch (ClassCastException e) {
            return "";
        }
    }

    private String blankIfNull(String s) {
        return s == null ? "" : s;
    }

    public int getInt(char arg) {
        Args.ArgumentMarshaler am = marshalers.get(arg);
        try {
            return am == null ? 0 : (Integer) am.get();
        } catch (Exception e) {
            return 0;
        }
    }

    public double getDouble(char arg) {
        Args.ArgumentMarshaler am = marshalers.get(arg);
        try {
            return am == null ? 0 : (Double) am.get();
        } catch (Exception e) {
            return 0.0;
        }
    }

    public boolean has(char arg) {
        return argsFound.contains(arg);
    }

    public boolean isValid() {
        return valid;
    }

    private interface ArgumentMarshaler {

        public abstract void set(Iterator<String> currentArgument) throws ArgsException;

        public abstract Object get();
    }

    private class BooleanArgumentMarshaler implements ArgumentMarshaler {
        private boolean booleanValue = false;

        @Override
        public void set(Iterator<String> currentArgument) throws ArgsException {
            booleanValue = true;
        }

        @Override
        public Object get() {
            return booleanValue;
        }
    }

    private class StringArgumentMarshaler implements ArgumentMarshaler {
        private String stringValue;

        @Override
        public void set(Iterator<String> currentArgument) throws ArgsException {
            try {
                stringValue = currentArgument.next();
            } catch (NoSuchElementException e) {
                errorCode = ArgsException.ErrorCode.MISSING_STRING;
                throw new ArgsException();
            }
        }

        @Override
        public Object get() {
            return stringValue;
        }
    }

    private class IntegerArgumentMarshaler implements ArgumentMarshaler {
        private int integerValue;

        @Override
        public void set(Iterator<String> currentArgument) throws ArgsException {
            String parameter = null;
            try {
                parameter = currentArgument.next();
                integerValue = Integer.parseInt(parameter);
            } catch (NoSuchElementException e) {
                errorCode = ArgsException.ErrorCode.MISSING_INTEGER;
                throw new ArgsException();
            } catch (NumberFormatException e) {
                errorParameter = parameter;
                errorCode = ArgsException.ErrorCode.INVALID_INTEGER;
                throw new ArgsException();
            }
        }

        @Override
        public Object get() {
            return integerValue;
        }
    }

    private class DoubleArgumentMarshaler implements ArgumentMarshaler {
        private double doubleValue = 0;

        public void set(Iterator<String> currentArgument) throws ArgsException {
            String parameter = null;
            try {
                parameter = currentArgument.next();
                doubleValue = Double.parseDouble(parameter);
            } catch (NoSuchElementException e) {
                errorCode = ArgsException.ErrorCode.MISSING_DOUBLE;
                throw new ArgsException();
            } catch (NumberFormatException e) {
                errorParameter = parameter;
                errorCode = ArgsException.ErrorCode.INVALID_DOUBLE;
                throw new ArgsException();
            }
        }

        public Object get() {
            return doubleValue;
        }
    }

}