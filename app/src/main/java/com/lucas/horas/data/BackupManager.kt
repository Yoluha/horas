package com.lucas.horas.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

object BackupManager {

    fun buildJson(punches: List<PunchEntity>, context: android.content.Context): String {
        val root = JSONObject()
        root.put("versao", 1)

        val arrayPunches = JSONArray()
        for (p in punches) {
            val obj = JSONObject()
            obj.put("timestamp", p.timestamp)
            obj.put("type", p.type.name)
            obj.put("note", p.note ?: JSONObject.NULL)
            arrayPunches.put(obj)
        }
        root.put("punches", arrayPunches)

        val horario = JSONObject()
        val dias = listOf(
            Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY
        )
        for (dia in dias) {
            horario.put(dia.toString(), ScheduleStore.getHoursFor(context, dia))
        }
        root.put("horario", horario)

        return root.toString(2)
    }

    /** Devolve a lista de PunchEntity a inserir (sem id, para o Room gerar novos). */
    fun parsePunches(json: String): List<PunchEntity> {
        val root = JSONObject(json)
        val arrayPunches = root.optJSONArray("punches") ?: JSONArray()
        val resultado = mutableListOf<PunchEntity>()
        for (i in 0 until arrayPunches.length()) {
            val obj = arrayPunches.getJSONObject(i)
            val tipo = PunchType.valueOf(obj.getString("type"))
            val nota = if (obj.isNull("note")) null else obj.getString("note")
            resultado.add(PunchEntity(timestamp = obj.getLong("timestamp"), type = tipo, note = nota))
        }
        return resultado
    }

    fun aplicarHorario(json: String, context: android.content.Context) {
        val root = JSONObject(json)
        val horario = root.optJSONObject("horario") ?: return
        val chaves = horario.keys()
        for (chave in chaves) {
            val diaSemana = chave.toIntOrNull() ?: continue
            ScheduleStore.setHoursFor(context, diaSemana, horario.getDouble(chave))
        }
    }
}
