package com.tencent.iot.explorer.link.core.link.exception;

public class TCLinkException extends Exception {
    private String mErrorCode = "unknown";
    private String mErrorMessage = "unknown";

    public TCLinkException(String errorMessage) {
        super(errorMessage);
        mErrorMessage = errorMessage;
    }

    public TCLinkException(String errorCode, String errorMessage) {
        this(errorMessage);
        mErrorCode = errorCode;
    }

    public TCLinkException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        mErrorMessage = errorMessage;
    }

    public TCLinkException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        mErrorCode = errorCode;
        mErrorMessage = errorMessage;
    }

    public String getErrorCode() {
        return mErrorCode;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }
}

