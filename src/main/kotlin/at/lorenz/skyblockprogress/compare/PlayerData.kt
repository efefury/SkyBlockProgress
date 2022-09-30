package at.lorenz.skyblockprogress.compare

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File

class PlayerData {

    var fetchTime = 0L
    var deathCount = 0L
    var statsDeaths = 0L
    var kills = 0L
    val statsKillsReason = mutableMapOf<String, Long>()
    val statsDeathsReason = mutableMapOf<String, Long>()
    val skillExperience = mutableMapOf<String, Long>()

    companion object {
        fun grab(uuid: String, file: File): PlayerData {
            val list = file.readLines()
            val fullText = list.joinToString("").replace("  ", "")
            val profile = JsonParser.parseString(fullText) as JsonObject

            return fillData(profile, uuid)
        }

        private fun fillData(profile: JsonObject, uuid: String): PlayerData {
            val data = PlayerData()
            if (profile.has("fetch_time")) {
                data.fetchTime = profile["fetch_time"].asLong
            }

            if (profile.has("members")) {
                val members = profile["members"].asJsonObject
                if (members.has(uuid)) {
                    val member = members[uuid].asJsonObject

                    if (member.has("death_count")) {
                        data.deathCount = member["death_count"].asLong
                    }
                    if (member.has("stats")) {
                        val stats = member["stats"].asJsonObject
                        if (stats.has("deaths")) {
                            data.statsDeaths = stats["deaths"].asLong
                        }
                        if (stats.has("kills")) {
                            data.kills = stats["kills"].asLong
                        }
                        for (key in stats.keySet()) {
                            if (key.startsWith("kills_")) {
                                val label = key.substring(6)
                                val kills = stats[key].asLong
                                data.statsKillsReason[label] = kills
                            }
                            if (key.startsWith("deaths_")) {
                                val label = key.substring(7)
                                val deaths = stats[key].asLong
                                data.statsDeathsReason[label] = deaths
                            }
                        }
                    }
                    for (key in member.keySet()) {
                        if (key.startsWith("experience_skill_")) {
                            val label = key.substring(17)
                            val exp = member[key].asLong
                            data.skillExperience[label] = exp
                            println("found skill: $label $exp")
                        }
                    }

                }
            }

            return data
        }
    }
}