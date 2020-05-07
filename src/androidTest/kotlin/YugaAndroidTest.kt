import com.twelfthmile.kyuga.Kyuga
import com.twelfthmile.kyuga.utils.YUGA_CONF_DATE
import org.json.JSONArray
import kotlin.test.Test
import kotlin.test.assertEquals

class YugaAndroidTest {

    @Test
    fun `on Yuga parse - yuga_tests json - all match expected`() {
        val jsons = getTestResources()
        (0 until jsons.length()).forEach {
            val response = Kyuga.parse(jsons.getJSONObject(it).getString("input"),
                mapOf(
                    YUGA_CONF_DATE to "2018-01-01 00:00:00"
                ))
            assertEquals(jsons.getJSONObject(it).getJSONObject("response").getString("str"), response?.dateStr)
        }
    }

    private fun getTestResources(): JSONArray {
        val inputs = YugaAndroidTest::class.java.getResource("yuga_test_data.json")
            .readText()

        return JSONArray(inputs)
    }
}