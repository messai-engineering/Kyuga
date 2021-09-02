package com.twelfthmile.kyuga.utils

/**
 * Created by johnjoseph on 19/03/17.
 */


const val DT_D = "d"
const val DT_DD = "dd"
const val DT_MM = "MM"
const val DT_MMM = "MMM"
const val DT_YY = "yy"
const val DT_YYYY = "yyyy"
const val DT_HH = "HH"
const val DT_mm = "mm"
const val DT_ss = "ss"

const val TY_NUM = "NUM"
const val TY_AMT = "AMT"
const val TY_PCT = "PCT"
const val TY_DST = "DST"
const val TY_WGT = "WGT"
const val TY_ACC = "INSTRNO"
const val TY_TYP = "TYP"
const val TY_DTE = "DATE"
const val TY_TME = "TIME"
const val TY_STR = "STR"
const val TY_PHN = "PHN"
const val TY_TMS = "TIMES"
const val TY_OTP = "OTP"
const val TY_VPD = "VPD" //VPA-ID
//public static final String TY_DCT = "DCT"; //date context like sunday,today,tomorrow

val FSA_TYPES = listOf(
    Pair("FSA_MONTHS", "jan;uary,feb;ruary,mar;ch,apr;il,may,jun;e,jul;y,aug;ust,sep;t;ember,oct;ober,nov;ember,dec;ember"),
    Pair("FSA_DAYS", "sun;day,mon;day,tue;sday,wed;nesday,thu;rsday,thur;sday,fri;day,sat;urday"),
    Pair("FSA_TIMEPRFX", "at,on,before,by"),
    Pair("FSA_AMT", "lac,lakh,k"),
    Pair("FSA_TIMES", "hours,hrs,hr,mins,minutes"),
    Pair("FSA_TZ", "gmt,ist"),
    Pair("FSA_DAYSFFX", "st,nd,rd,th"),
    Pair("FSA_UPI", "UPI,MMT,NEFT")
)

const val CH_SPACE = 32
const val CH_PCT = 37
const val CH_SQOT = 39
const val CH_COMA = 44
const val CH_HYPH = 45
const val CH_FSTP = 46
const val CH_SLSH = 47
const val CH_COLN = 58
const val CH_SCLN = 59
const val CH_PLUS = 43
const val CH_ATRT = 64
const val CH_RBKT = 41
const val CH_STAR = 42
const val CH_UNSC = 95
const val CH_LSBT = 91

const val INDEX = "INDEX"

const val YUGA_CONF_DATE = "YUGA_CONF_DATE"
const val YUGA_SOURCE_CONTEXT = "YUGA_SOURCE_CONTEXT"
const val YUGA_SC_CURR = "YUGA_SC_CURR"
private const val DATE_TIME_FORMAT_STR = "yyyy-MM-dd HH:mm:ss"
private const val DATE_FORMAT_STR = "yyyy-MM-dd"
