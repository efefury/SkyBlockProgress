package at.lorenz.skyblockprogress.compare

import com.google.gson.GsonBuilder
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class CompareData(private val apiKey: String, players: MutableMap<String, String>) {

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")

    init {
        println(" ")
        println("Compare Data")
        for ((uuid, profileName) in players) {
            comparePlayerData(uuid, profileName)
        }
    }

    private fun comparePlayerData(uuid: String, profileName: String) {
        val parent = File("data/$uuid/$profileName")
        if (!parent.isDirectory) {
            println("No folder: $parent")
            return
        }

        val map = mutableMapOf<PlayerData, Long>()
        for (file in parent.listFiles()) {
            val name = file.name
            if (name.endsWith("-cleaned.json")) {
                val data = PlayerData.grab(uuid, file)
                map[data] = data.fetchTime
            }
        }

        if (map.size < 2) {
            println("not enough data points saved!")
            return
        }

        val sorted = map.toSortedMap { a, b -> if (a.fetchTime < b.fetchTime) 1 else -1 }

        var first: PlayerData? = null
        var second: PlayerData? = null

        var i = 0
        for (entry in sorted.entries) {
            if (i == 0) first = entry.key
            if (i == 1) second = entry.key
            if (i == 2) break
            i++
        }
        compare(second!!, first!!)
    }

    private fun compare(first: PlayerData, second: PlayerData) {
        val firstLastSave = dateFormat.format(first.fetchTime)
        val secondLastSave = dateFormat.format(second.fetchTime)

        val result = mutableListOf<String>()

        result.add(" ")
        result.add("compare $firstLastSave with $secondLastSave")
        result.add(" ")
        result.add(makeCompareText("deathCount", first.deathCount, second.deathCount))
        result.add(makeCompareText("statsDeaths", first.statsDeaths, second.statsDeaths))

        result.add(makeCompareText("kills", first.kills, second.kills))
        result.addAll(printListChange("stats-kills",first.statsKillsReason, second.statsKillsReason))
        result.addAll(printListChange("skill-exp-gain",first.skillExperience, second.skillExperience))

        for (line in result) {
            if (line.isEmpty()) continue
            println(line)
        }
    }

    private fun printListChange(
        listLabel: String,
        first: MutableMap<String, Long>,
        second: MutableMap<String, Long>,
    ): List<String> {
        val changedStats = mutableListOf<String>()
        for (entry in second) {
            val label = entry.key
            val newValue = entry.value
            val oldValue = first.getOrDefault(label, 0)
            if (newValue != oldValue) {
                changedStats.add(makeCompareText(label, oldValue, newValue))
            }
        }
        val result = mutableListOf<String>()
        if (changedStats.isNotEmpty()) {
            result.add("\n[$listLabel]")
            for (value in changedStats) {
                result.add("  $value")
            }
        }
        return result
    }


    private fun makeCompareText(label: String, a: Long, b: Long): String {
        val numberFormatter = DecimalFormat("#,##0")
        val diff = b - a
        if (diff == 0L) return ""
        val plus = if (diff > 0) "+" else ""
        val aa = numberFormatter.format(a)
        val bb = numberFormatter.format(b)
        val diffFormat = numberFormatter.format(diff)
        return "$label: $plus$diffFormat ($aa -> $bb)"
    }
}
