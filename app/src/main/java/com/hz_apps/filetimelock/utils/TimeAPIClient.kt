import android.app.Activity
import org.json.JSONObject
import java.net.URL
import java.time.LocalDateTime

class TimeApiClient (
                private val activity: Activity,
                private val onTimeAPIListener: OnTimeAPIListener
){
    private val apiUrl = "https://timeapi.io/api/Time/current/zone?timeZone=UTC"

    fun getCurrentTime() {

        try {
            val response = URL(apiUrl).readText()
            val jsonData = JSONObject(response)
            val year = jsonData.getInt("year")
            val month = jsonData.getInt("month")
            val day = jsonData.getInt("day")
            val hour = jsonData.getInt("hour")
            val minute = jsonData.getInt("minute")
            val seconds = jsonData.getInt("seconds")
            val dateTime = LocalDateTime.of(year, month, day, hour, minute, seconds)
            activity.runOnUiThread {
                onTimeAPIListener.onGetTime(dateTime)
            }
        } catch (e : Exception) {
            activity.runOnUiThread{
                onTimeAPIListener.onFailToGetTime(e.toString())
            }
        }
    }


}

interface OnTimeAPIListener {
    fun onGetTime(dateTime: LocalDateTime)
    fun onFailToGetTime(error : String)
}