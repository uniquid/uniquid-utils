package com.uniquid.settings.exception;

import com.uniquid.settings.model.Setting;

public class UnknownSettingException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Create new setting validation exception
     *
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public UnknownSettingException(Setting setting, String message,
                                   Throwable cause) {
        super(message, cause);
    }

    /**
     * Create new setting validation exception
     *
     * @param message
     *            the message
     */
    public UnknownSettingException(String message) {
        super(message);
    }

}
