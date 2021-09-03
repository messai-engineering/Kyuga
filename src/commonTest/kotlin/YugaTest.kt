import com.twelfthmile.kyuga.Kyuga
import com.twelfthmile.kyuga.utils.YUGA_CONF_DATE
import kotlin.test.Test
import kotlin.test.assertEquals

class YugaTest {

    @Test
    fun `on parse - simple date - should pass`() {
        val response = Kyuga.parse("05.06.2018", mapOf(
            YUGA_CONF_DATE to "2017-01-01 00:00:00"
        ))
        assertEquals("2018-06-05 00:00:00", response?.dateStr)
    }

    @Test
    fun `on tokenize - valid sms - should tokenize`() {
        val validSms = "INR 7,980.00 Dr to A/c No XX2471 towards SI HDFC177126215 BSES Rajdhani -02/10/17 Val 03-OCT-17. Clr Bal INR 8,822.69."
        val candidateTokens = validSms.split(" ").map { it.trim() }
        val tokens = Kyuga.tokenize(candidateTokens)

        assertEquals("[INR, AMT, Dr, to, A/c, No, INSTRNO, towards, SI, IDVAL, BSES, Rajdhani, AMT, Val, DATE, Clr, Bal, INR, AMT]", tokens.toString())
    }

    // TODO fix test to avoid crash for formats like 22:55:4 (https://github.com/messai-engineering/Kyuga/issues/1)
    fun `on tokenize - valid offer- should tokenize`() {
        val sms = "55865 is your One Time Password. This OTP is valid for 10 minutes only. OTP generated on: 09-04-2017 22:55:4"
        val candidateTokens = sms.split(" ").map { it.trim() }
        val tokens = Kyuga.tokenize(candidateTokens)
        println(tokens)
    }

    @Test
    fun `on tokenise - message with email - should tokenise email`() {
        val sms = "Sometime emails are representations of one own self loath like madness@rethtomail.com and killer@gmail.in"
        val tokenizedMessage = Kyuga.tokenize(sms)
        assertEquals("Sometime emails are representations of one own self loath like EMAILADDR and EMAILADDR",
            tokenizedMessage)
    }

    @Test
    fun `on tokenise - valid sms - should tokenize`() {
        val validSms = "INR 7,983.00 Dr to A/c No XX1234 towards SI HDFC177126215 BSES Rajdhani -02/10/17 Val 03-OCT-17. Clr Bal INR 8,822.69."
        val tokenized = Kyuga.tokenize(validSms)
        assertEquals("INR NUM Dr to A/c No INSTRNO towards SI IDVAL BSES Rajdhani AMT Val DATE Clr Bal INR NUM", tokenized)
    }

    @Test
    fun `on tokenise - valid bank sms - should tokenize`() {
        val validSms =
            "Rs 300 withdrawn at SBG ATM  S1BB005916124  KOTHAGUDA X ROADS HYDERAB, HYDERABAD from A/c xxxx 5678 on 260916.Txn#4801 .Avl bal Rs 9732.5."
        val tokenized = Kyuga.tokenize(validSms)
        assertEquals(
            "Rs NUM withdrawn at SBG ATM IDVAL KOTHAGUDA INSTRNO ROADS HYDERAB, HYDERABAD from A/c INSTRNO NUM on NUM Txn#4801 Avl bal Rs NUM",
            tokenized
        )
    }
}