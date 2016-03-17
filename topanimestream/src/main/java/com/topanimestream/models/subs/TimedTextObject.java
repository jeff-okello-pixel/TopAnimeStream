package com.topanimestream.models.subs;

import java.util.Hashtable;
import java.util.TreeMap;

/**
 * These objects can (should) only be created through the implementations of parseFile() in the {@link TimedTextFileFormat} interface
 * They are an object representation of a subtitle file and contain all the captions and associated styles.
 */
public class TimedTextObject {

    /*
     * Attributes
     *
     */
    //meta info
    public String title = "";
    public String description = "";
    public String copyright = "";
    public String author = "";
    public String fileName = "";
    public String language = "";

    //list of styles (id, reference)
    public Hashtable<String, Style> styling;

    //list of captions (begin time, reference)
    //represented by a tree map to maintain order
    public TreeMap<Integer, Caption> captions;

    //to store non fatal errors produced during parsing
    public String warnings;

    //**** OPTIONS *****
    //to know whether file should be saved as .ASS or .SSA
    public boolean useASSInsteadOfSSA = true;
    //to delay or advance the subtitles, parsed into +/- milliseconds
    public int offset = 0;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    //to know if a parsing method has been applied
    public boolean built = false;


    /**
     * Protected constructor so it can't be created from outside
     */
    protected TimedTextObject() {
        styling = new Hashtable<String, Style>();
        captions = new TreeMap<Integer, Caption>();

        warnings = "List of non fatal errors produced during parsing:\n\n";
    }

	
    /*
     * Writing Methods
	 * 
	 */

    /**
     * Method to generate the .SRT file
     *
     * @return an array of strings where each String represents a line
     */
    public String[] toSRT() {
        return new FormatSRT().toFile(this);
    }

    /**
     * Method to generate the .WebVTT file
     *
     * @return an array of strings where each String represents a line
     */
    public String[] toWebVTT() {
        return new FormatWebVTT().toFile(this);
    }


    /**
     * Method to generate the .ASS file
     *
     * @return an array of strings where each String represents a line
     */
    public String[] toASS() {
        return new FormatASS().toFile(this);
    }

	/* 
     * PROTECTED METHODS
	 * 
	 */

    /**
     * This method simply checks the style list and eliminate any style not referenced by any caption
     * This might come useful when default styles get created and cover too much.
     * It require a unique iteration through all captions.
     */
    protected void cleanUnusedStyles() {
        //here all used styles will be stored
        Hashtable<String, Style> usedStyles = new Hashtable<String, Style>();
        //we iterate over the captions
        for (Caption current : captions.values()) {
            //new caption
            //if it has a style
            if (current.style != null) {
                String iD = current.style.iD;
                //if we haven't saved it yet
                if (!usedStyles.containsKey(iD))
                    usedStyles.put(iD, current.style);
            }
        }
        //we saved the used styles
        this.styling = usedStyles;
    }

}
