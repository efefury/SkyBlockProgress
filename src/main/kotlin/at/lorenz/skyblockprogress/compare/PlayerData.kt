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
    var totalPetExpGained = 0L

    val bestiarityKills = mutableMapOf<String, Long>()
    val bestiarityDeaths = mutableMapOf<String, Long>()
    val collectionCount = mutableMapOf<String, Long>()
    val pexExperience = mutableMapOf<String, Long>()

    val mythologyData = mutableMapOf<String, Long>()
    var mythologyKills = 0L

    val slayers = mutableMapOf<String, SlayerData>()

    class SlayerData(val experience: Long, val bossKills: Map<String, Long>)

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
                        if (stats.has("total_pet_exp_gained")) {
                            data.totalPetExpGained = stats["total_pet_exp_gained"].asLong
                        }
                        if (stats.has("mythos_kills")) {
                            data.mythologyKills = stats["mythos_kills"].asLong
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
                            if (key.startsWith("mythos_burrows_")) {
                                val label = key.substring(15)
                                val value = stats[key].asLong
                                data.mythologyData[label] = value
                            }
                        }
                    }
                    for (key in member.keySet()) {
                        if (key.startsWith("experience_skill_")) {
                            val label = key.substring(17)
                            val exp = member[key].asLong
                            data.skillExperience[label] = exp
                        }
                    }
                    if (member.has("bestiary")) {
                        val bestiary = member["bestiary"].asJsonObject
                        for (key in bestiary.keySet()) {
                            if (key.startsWith("kills_")) {
                                val label = key.substring(6)
                                val kills = bestiary[key].asLong
                                data.bestiarityKills[label] = kills
                            }
                            if (key.startsWith("deaths_")) {
                                val label = key.substring(7)
                                val deaths = bestiary[key].asLong
                                data.bestiarityDeaths[label] = deaths
                            }
                        }
                    }
                    if (member.has("collection")) {
                        val collection = member["collection"].asJsonObject
                        for (key in collection.keySet()) {
                            val count = collection[key].asLong
                            data.collectionCount[key.lowercase()] = count

                        }
                    }
                    if (member.has("pets")) {
                        val pets = member["pets"].asJsonArray
                        for (entry in pets) {
                            val pet = entry.asJsonObject
                            val type = pet["type"].asString.lowercase()
                            val exp = pet["exp"].asLong
                            data.pexExperience[type] = exp
                        }
                    }

                    if (member.has("slayer_bosses")) {
                        val slayerBosses = member["slayer_bosses"].asJsonObject
                        for (key in slayerBosses.keySet()) {
//                            println("key: $key")
                            val slayer = slayerBosses[key].asJsonObject
                            if (slayer.has("xp")) {
                                val experience = slayer["xp"].asLong
//                                println("experience: $experience")
                                val bossKills = mutableMapOf<String, Long>()
                                data.slayers[key] = SlayerData(experience, bossKills)
                                for (bossName in slayer.keySet()) {
                                    if (bossName == "xp") continue
                                    val kills = slayer[bossName].asLong
//                                    println("$bossName = $kills")
                                    bossKills[bossName] = kills
                                }
                            }
                        }
                    }
                }
            }

            return data
        }
    }
}