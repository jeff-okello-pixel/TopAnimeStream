package com.topanimestream.models.subs;

/**
 * This class represents problems that may arise during the parsing of a subtitle file.
 */
public class FatalParsingException extends Exception {

    private static final long serialVersionUID = 6798827566637277804L;

    private String parsingErrror;

    public FatalParsingException(String parsingError) {
        super(parsingError);
        this.parsingErrror = parsingError;
    }

    @Override
    public String getLocalizedMessage() {
        return parsingErrror;
    }

}
