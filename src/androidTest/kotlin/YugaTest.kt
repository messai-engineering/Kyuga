import com.twelfthmile.kyuga.Kyuga
import com.twelfthmile.kyuga.utils.KYugaConstants
import org.json.JSONArray
import kotlin.test.Test
import kotlin.test.assertEquals

class YugaTest {

    @Test
    fun `on Yuga parse - yuga_tests json - all match expected`() {
        val jsons = getTestResources()
        (0 until jsons.length()).forEach {
            val response = Kyuga.parse(jsons.getJSONObject(it).getString("input"),
                mapOf(
                    KYugaConstants.YUGA_CONF_DATE to "2018-01-01 00:00:00"
                ))
            assertEquals(jsons.getJSONObject(it).getJSONObject("response").getString("str"), response?.dateStr)
        }
    }

    @Test
    fun `on parse - simple date - should pass`() {
        val response = Kyuga.parse("09:40 PM May 21, 2017")
        println(response?.dateStr)
    }

    private fun getTestResources(): JSONArray {
        val inputs = YugaTest::class.java.getResource("[\n  {\n    \"input\": \"29Nov17\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2017-11-29 00:00:00\",\n      \"index\": 7\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"08 May\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2018-05-08 00:00:00\",\n      \"index\": 6\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"August 15\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2018-08-15 00:00:00\",\n      \"index\": 9\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"Sep 2017\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2017-09-01 00:00:00\",\n      \"index\": 8\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"09:40 PM May 21, 2017\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2017-05-21 21:40:00\",\n      \"index\": 21\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"18:10 (Thur, 12 Oct)\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2018-10-12 18:10:00\",\n      \"index\": 19\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"07:10 on Wed 14 Jun 17\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2017-06-14 07:10:00\",\n      \"index\": 22\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"Wed Dec 20 17:26:25 GMT+05:30 2017\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2017-12-20 17:26:25\",\n      \"index\": 34\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"2017-09-03:02:15:44\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2017-09-03 02:15:44\",\n      \"index\": 19\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"Mon Sep 04 13:47:13 IST 2017\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2017-09-04 13:47:13\",\n      \"index\": 28\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"07 Jan 18, 06:05\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2018-01-07 06:05:00\",\n      \"index\": 16\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"18/09/2017\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2017-09-18 00:00:00\",\n      \"index\": 10\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"12/05\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2018-05-12 00:00:00\",\n      \"index\": 5\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"12/05/16\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2016-05-12 00:00:00\",\n      \"index\": 8\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"2016/05/12\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2016-05-12 00:00:00\",\n      \"index\": 10\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"12Aug2017\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2017-08-12 00:00:00\",\n      \"index\": 9\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"04-Jul-17\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2017-07-04 00:00:00\",\n      \"index\": 9\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"16-Feb-2017 15:26:58\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2017-02-16 15:26:58\",\n      \"index\": 20\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"07 Sep 2017\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2017-09-07 00:00:00\",\n      \"index\": 11\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"21-Aug-17 19:21 Hrs\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2017-08-21 19:21:00\",\n      \"index\": 19\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"27Jan\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2018-01-27 00:00:00\",\n      \"index\": 5\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"Sun, 3 Sep, 2017 02:00pm\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2017-09-03 14:00:00\",\n      \"index\": 24\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"23:35 hrs\",\n    \"response\": {\n      \"type\": \"TIME\",\n      \"valMap\": {\n        \"time\": \"23:35\"\n      },\n      \"str\": \"23:35 hrs\",\n      \"index\": 9\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"24-10-2017 15:05:57\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2017-10-24 15:05:57\",\n      \"index\": 19\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"22-Oct-2017 21:10\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2017-10-22 21:10:00\",\n      \"index\": 17\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"15:59:24\",\n    \"response\": {\n      \"type\": \"TIME\",\n      \"valMap\": {\n        \"time\": \"15:59\"\n      },\n      \"str\": \"15:59:24\",\n      \"index\": 8\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"30-Nov 14:54\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2018-11-30 14:54:00\",\n      \"index\": 12\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"06/JAN/17\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2017-01-06 00:00:00\",\n      \"index\": 9\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"Dec 18th\",\n    \"response\": {\n      \"type\": \"DATE\",\n      \"valMap\": {},\n      \"str\": \"2018-12-18 00:00:00\",\n      \"index\": 6\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"100abc\",\n    \"response\": {\n      \"type\": \"STR\",\n      \"valMap\": {},\n      \"str\": \"100\",\n      \"index\": 6\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"+917032641284\",\n    \"response\": {\n      \"type\": \"NUM\",\n      \"valMap\": {\n        \"num_class\": \"PHN\"\n      },\n      \"str\": \"917032641284\",\n      \"index\": 13\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"7032641284\",\n    \"response\": {\n      \"type\": \"NUM\",\n      \"valMap\": {\n        \"num_class\": \"PHN\"\n      },\n      \"str\": \"7032641284\",\n      \"index\": 10\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"1\",\n    \"response\": {\n      \"type\": \"NUM\",\n      \"valMap\": {\n        \"num_class\": \"NUM\"\n      },\n      \"str\": \"1\",\n      \"index\": 1\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"12\",\n    \"response\": {\n      \"type\": \"NUM\",\n      \"valMap\": {\n        \"num_class\": \"NUM\"\n      },\n      \"str\": \"12\",\n      \"index\": 2\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"123\",\n    \"response\": {\n      \"type\": \"NUM\",\n      \"valMap\": {\n        \"num_class\": \"NUM\"\n      },\n      \"str\": \"123\",\n      \"index\": 3\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"123.00\",\n    \"response\": {\n      \"type\": \"AMT\",\n      \"valMap\": {},\n      \"str\": \"123.00\",\n      \"index\": 6\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"1,23,000.00\",\n    \"response\": {\n      \"type\": \"AMT\",\n      \"valMap\": {},\n      \"str\": \"123000.00\",\n      \"index\": 11\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"2000\",\n    \"response\": {\n      \"type\": \"NUM\",\n      \"valMap\": {\n        \"num_class\": \"NUM\"\n      },\n      \"str\": \"2000\",\n      \"index\": 4\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"2000.00\",\n    \"response\": {\n      \"type\": \"AMT\",\n      \"valMap\": {},\n      \"str\": \"2000.00\",\n      \"index\": 7\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"100%\",\n    \"response\": {\n      \"type\": \"PCT\",\n      \"valMap\": {},\n      \"str\": \"100\",\n      \"index\": 4\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"1,00,00,000.00\",\n    \"response\": {\n      \"type\": \"AMT\",\n      \"valMap\": {},\n      \"str\": \"10000000.00\",\n      \"index\": 14\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"1.23\",\n    \"response\": {\n      \"type\": \"AMT\",\n      \"valMap\": {},\n      \"str\": \"1.23\",\n      \"index\": 4\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"**2657\",\n    \"response\": {\n      \"type\": \"INSTRNO\",\n      \"valMap\": {},\n      \"str\": \"XX2657\",\n      \"index\": 6\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"123**2657\",\n    \"response\": {\n      \"type\": \"INSTRNO\",\n      \"valMap\": {},\n      \"str\": \"123XX2657\",\n      \"index\": 9\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"-351.00\",\n    \"response\": {\n      \"type\": \"AMT\",\n      \"valMap\": {},\n      \"str\": \"-351.00\",\n      \"index\": 7\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"-351\",\n    \"response\": {\n      \"type\": \"AMT\",\n      \"valMap\": {},\n      \"str\": \"-351\",\n      \"index\": 4\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"1-800-2703311\",\n    \"response\": {\n      \"type\": \"NUM\",\n      \"valMap\": {\n        \"num_class\": \"PHN\"\n      },\n      \"str\": \"18002703311\",\n      \"index\": 13\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"1800-2703311\",\n    \"response\": {\n      \"type\": \"NUM\",\n      \"valMap\": {\n        \"num_class\": \"PHN\"\n      },\n      \"str\": \"18002703311\",\n      \"index\": 12\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"18002703311\",\n    \"response\": {\n      \"type\": \"NUM\",\n      \"valMap\": {\n        \"num_class\": \"PHN\"\n      },\n      \"str\": \"18002703311\",\n      \"index\": 11\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"91-7032641284\",\n    \"response\": {\n      \"type\": \"NUM\",\n      \"valMap\": {\n        \"num_class\": \"PHN\"\n      },\n      \"str\": \"917032641284\",\n      \"index\": 13\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"0484-2340606\",\n    \"response\": {\n      \"type\": \"NUM\",\n      \"valMap\": {\n        \"num_class\": \"PHN\"\n      },\n      \"str\": \"04842340606\",\n      \"index\": 12\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"040-30425500\",\n    \"response\": {\n      \"type\": \"NUM\",\n      \"valMap\": {\n        \"num_class\": \"PHN\"\n      },\n      \"str\": \"04030425500\",\n      \"index\": 12\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"080 26703300\",\n    \"response\": {\n      \"type\": \"NUM\",\n      \"valMap\": {\n        \"num_class\": \"PHN\"\n      },\n      \"str\": \"08026703300\",\n      \"index\": 12\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"26700156\",\n    \"response\": {\n      \"type\": \"NUM\",\n      \"valMap\": {\n        \"num_class\": \"NUM\"\n      },\n      \"str\": \"26700156\",\n      \"index\": 8\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"35K\",\n    \"response\": {\n      \"type\": \"AMT\",\n      \"valMap\": {},\n      \"str\": \"35000\",\n      \"index\": 3\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"1Lac\",\n    \"response\": {\n      \"type\": \"AMT\",\n      \"valMap\": {},\n      \"str\": \"100000\",\n      \"index\": 3\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"10hrs\",\n    \"response\": {\n      \"type\": \"TIME\",\n      \"valMap\": {\n        \"time\": \"10:00\"\n      },\n      \"str\": \"10hrs\",\n      \"index\": 5\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"2045 HRS\",\n    \"response\": {\n      \"type\": \"TIME\",\n      \"valMap\": {\n        \"time\": \"20:45\"\n      },\n      \"str\": \"2045 HRS\",\n      \"index\": 8\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"0316-03624669-190001\",\n    \"response\": {\n      \"type\": \"NUM\",\n      \"valMap\": {\n        \"num_class\": \"NUM\"\n      },\n      \"str\": \"031603624669190001\",\n      \"index\": 20\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"461721XXXXXX7514\",\n    \"response\": {\n      \"type\": \"INSTRNO\",\n      \"valMap\": {},\n      \"str\": \"461721XXXXXX7514\",\n      \"index\": 16\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"4617-21XX-XXXX-7514\",\n    \"response\": {\n      \"type\": \"INSTRNO\",\n      \"valMap\": {},\n      \"str\": \"461721XXXXXX7514\",\n      \"index\": 19\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"0820/0950\",\n    \"response\": {\n      \"type\": \"TIMES\",\n      \"valMap\": {\n        \"arrv_time\": \"09:50\",\n        \"dept_time\": \"08:20\"\n      },\n      \"str\": \"08200950\",\n      \"index\": 9\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"[727301]\",\n    \"response\": {\n      \"type\":\"NUM\",\n      \"valMap\":{\n        \"num_class\":\"NUM\"\n      },\n      \"str\":\"727301\",\n      \"index\":7\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"727-301\",\n    \"response\": {\n      \"type\":\"NUM\",\n      \"valMap\":{\n        \"num_class\":\"NUM\"\n      },\n      \"str\":\"727301\",\n      \"index\":7\n    },\n    \"accepted\": true\n  },\n  {\n    \"input\": \"May 1 12:07 AM\",\n    \"response\": {\n      \"type\":\"DATE\",\n      \"valMap\":{},\n      \"str\":\"2018-05-01 00:07:00\",\n      \"index\":14\n    },\n    \"accepted\": true\n  }\n]\n")
            .readText()

        return JSONArray(inputs)
    }


}