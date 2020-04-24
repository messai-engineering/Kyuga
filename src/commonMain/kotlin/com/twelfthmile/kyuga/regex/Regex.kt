package com.twelfthmile.kyuga.regex

val PHONE = ( // sdd = space, dot, or dash
        "(\\+[0-9]+[\\- \\.]*)?" // +<digits><sdd>*
                + "(\\([0-9]+\\)[\\- \\.]*)?" // (<digits>)<sdd>*
                + "([0-9][0-9\\- \\.]+[0-9])").toRegex()

val EMAIL_ADDRESS = (
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
        ).toRegex()


val WEB_URL = (
        "("
                + "("
                + "(?:" + PROTOCOL + "(?:" + USER_INFO + ")?" + ")?"
                + "(?:" + DOMAIN_NAME_STR + ")"
                + "(?:" + PORT_NUMBER + ")?"
                + ")"
                + "(" + PATH_AND_QUERY + ")?"
                + WORD_BOUNDARY
                + ")"
        ).toRegex()