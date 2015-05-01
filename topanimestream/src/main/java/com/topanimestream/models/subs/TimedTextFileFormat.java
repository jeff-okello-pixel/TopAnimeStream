/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

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
