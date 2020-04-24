package com.twelfthmile.kyuga.regex

private const val UCS_CHAR = "[" +
        "\u00A0-\uD7FF" +
        "\uF900-\uFDCF" +
        "\uFDF0-\uFFEF" +
        "\uD800\uDC00-\uD83F\uDFFD" +
        "\uD840\uDC00-\uD87F\uDFFD" +
        "\uD880\uDC00-\uD8BF\uDFFD" +
        "\uD8C0\uDC00-\uD8FF\uDFFD" +
        "\uD900\uDC00-\uD93F\uDFFD" +
        "\uD940\uDC00-\uD97F\uDFFD" +
        "\uD980\uDC00-\uD9BF\uDFFD" +
        "\uD9C0\uDC00-\uD9FF\uDFFD" +
        "\uDA00\uDC00-\uDA3F\uDFFD" +
        "\uDA40\uDC00-\uDA7F\uDFFD" +
        "\uDA80\uDC00-\uDABF\uDFFD" +
        "\uDAC0\uDC00-\uDAFF\uDFFD" +
        "\uDB00\uDC00-\uDB3F\uDFFD" +
        "\uDB44\uDC00-\uDB7F\uDFFD" +
        "&&[^\u00A0[\u2000-\u200A]\u2028\u2029\u202F\u3000]]"

val TLD_CHAR = "a-zA-Z$UCS_CHAR"

val PUNYCODE_TLD = "xn\\-\\-[\\w\\-]{0,58}\\w"

val TLD = "($PUNYCODE_TLD|[$TLD_CHAR]{2,63})"

val LABEL_CHAR = "a-zA-Z0-9$UCS_CHAR"

val IRI_LABEL = "[" + LABEL_CHAR.toString() + "](?:[" + LABEL_CHAR + "_\\-]{0,61}[" + LABEL_CHAR + "]){0,1}"

val HOST_NAME = "($IRI_LABEL\\.)+$TLD"

const val IP_ADDRESS_STRING = ("((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
        + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
        + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
        + "|[1-9][0-9]|[0-9]))")

val DOMAIN_NAME_STR = "($HOST_NAME|$IP_ADDRESS_STRING)"

const val USER_INFO = ("(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)"
        + "\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_"
        + "\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@")

const val PORT_NUMBER = "\\:\\d{1,5}"

val PATH_AND_QUERY = ("[/\\?](?:(?:[" + LABEL_CHAR
        + ";/\\?:@&=#~" // plus optional query params
        + "\\-\\.\\+!\\*'\\(\\),_\\$])|(?:%[a-fA-F0-9]{2}))*")

const val WORD_BOUNDARY = "(?:\\b|$|^)"

const val PROTOCOL = "(?i:http|https|rtsp)://"