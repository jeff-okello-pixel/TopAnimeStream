package com.topanimestream.models.subs;

import java.io.IOException;

/**
 * This class specifies the interface for any format supported by the converter, these formats must
 * create a {@link com.topanimestream.models.subs.TimedTextObject} from an {@link java.io.InputStream} (so it can process files form standard In or uploads)
 * and return a String array for text formats, or byte array for binary formats.*/

public abstract class TimedTextFileFormat {

    /**
     * This methods receives the path to a file, parses it, and returns a TimedTextObject
     *
     * @param fileName String that contains the path to the file
     * @return TimedTextObject representing the parsed file
     * @throws java.io.IOException when having trouble reading the file from the given path
     */
    public abstract TimedTextObject parseFile(String fileName, String[] inputString) throws IOException, FatalParsingException;

    public TimedTextObject parseFile(String fileName, String inputString) throws IOException, FatalParsingException {
        return parseFile(fileName, inputString.split("\n|\r\n"));
    }

    /**
     * This method transforms a given TimedTextObject into a formated subtitle file
     *
     * @param tto the object to transform into a file
     * @return NULL if the given TimedTextObject has not been built first,
     * or String[] where each String is at least a line, if size is 2, then the file has at least two lines.
     * or byte[] in case the file is a binary (as is the case of STL format)
     */
    public abstract Object toFile(TimedTextObject tto);

    protected String getLine(String[] strArray, int index) {
        if (index < strArray.length) {
            return strArray[index];
        }
        return null;
    }


}
