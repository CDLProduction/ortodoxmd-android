package md.ortodox.ortodoxmd.domain.model

import md.ortodox.ortodoxmd.data.model.CalendarData
import java.util.Calendar

/**
 * Definește ierarhia de solemnitate a unei sărbători, conform Tipicului.
 */
enum class HolidayRank {
    /** Praznic Împărătesc sau sărbătoare echivalentă (†). */
    GREAT_FEAST,
    /** Sărbătoare a unui sfânt de mare cinstire (†) sau Duminică. */
    IMPORTANT_SAINT
}

/**
 * Structură de date pentru o sărbătoare cu dată fixă (stil vechi).
 */
data class RedDayHoliday(
    val month: Int, // Luna (1-12), stil vechi
    val day: Int,   // Ziua (1-31), stil vechi
    val rank: HolidayRank
)

object RedLetterDays {

    /**
     * Lista completă și canonică a sărbătorilor cu dată fixă, conform calendarului ortodox pe stil vechi.
     */
    private val fixedHolidays: Set<RedDayHoliday> = setOf(
        // IANUARIE
        RedDayHoliday(1, 1, HolidayRank.GREAT_FEAST),   // Tăierea-împrejur; Sf. Vasile
        RedDayHoliday(1, 6, HolidayRank.GREAT_FEAST),   // Botezul Domnului
        RedDayHoliday(1, 7, HolidayRank.IMPORTANT_SAINT),// Soborul Sf. Ioan Botezătorul
        RedDayHoliday(1, 30, HolidayRank.GREAT_FEAST),  // Sf. Trei Ierarhi
        // FEBRUARIE
        RedDayHoliday(2, 2, HolidayRank.GREAT_FEAST),   // Întâmpinarea Domnului
        // MARTIE
        RedDayHoliday(3, 9, HolidayRank.IMPORTANT_SAINT),// Sf. 40 Mucenici
        RedDayHoliday(3, 25, HolidayRank.GREAT_FEAST),  // Buna Vestire
        // APRILIE
        RedDayHoliday(4, 23, HolidayRank.GREAT_FEAST),  // Sf. Mc. Gheorghe
        // MAI
        RedDayHoliday(5, 21, HolidayRank.GREAT_FEAST),  // Sf. Împărați Constantin și Elena
        // IUNIE
        RedDayHoliday(6, 24, HolidayRank.GREAT_FEAST),  // Nașterea Sf. Ioan Botezătorul
        RedDayHoliday(6, 29, HolidayRank.GREAT_FEAST),  // Sf. Ap. Petru și Pavel
        // IULIE
        RedDayHoliday(7, 2, HolidayRank.GREAT_FEAST),   // Sf. Ștefan cel Mare
        RedDayHoliday(7, 20, HolidayRank.GREAT_FEAST),  // Sf. Prooroc Ilie
        // AUGUST
        RedDayHoliday(8, 3, HolidayRank.IMPORTANT_SAINT), // Sf. Iraclie Flocea
        RedDayHoliday(8, 6, HolidayRank.GREAT_FEAST),   // Schimbarea la Față
        RedDayHoliday(8, 7, HolidayRank.IMPORTANT_SAINT), // Sf. Teodora de la Sihla
        RedDayHoliday(8, 8, HolidayRank.IMPORTANT_SAINT), // Sf. Alexandru Baltaga
        RedDayHoliday(8, 15, HolidayRank.GREAT_FEAST),  // Adormirea Maicii Domnului
        RedDayHoliday(8, 29, HolidayRank.GREAT_FEAST),  // Tăierea Capului Sf. Ioan
        RedDayHoliday(8, 30, HolidayRank.IMPORTANT_SAINT),// Sf. Varlaam
        // SEPTEMBRIE
        RedDayHoliday(9, 8, HolidayRank.GREAT_FEAST),   // Nașterea Maicii Domnului
        RedDayHoliday(9, 14, HolidayRank.GREAT_FEAST),  // Înălțarea Sfintei Cruci
        RedDayHoliday(9, 16, HolidayRank.IMPORTANT_SAINT),// Sf. Sofian Boghiu
        // OCTOMBRIE
        RedDayHoliday(10, 1, HolidayRank.GREAT_FEAST),  // Acoperământul Maicii Domnului
        RedDayHoliday(10, 14, HolidayRank.GREAT_FEAST), // Sf. Cuv. Parascheva
        RedDayHoliday(10, 26, HolidayRank.GREAT_FEAST), // Sf. Mc. Dimitrie
        // NOIEMBRIE
        RedDayHoliday(11, 8, HolidayRank.GREAT_FEAST),  // Soborul Sf. Arhangheli
        RedDayHoliday(11, 30, HolidayRank.GREAT_FEAST), // Sf. Ap. Andrei
        // DECEMBRIE
        RedDayHoliday(12, 6, HolidayRank.GREAT_FEAST),  // Sf. Ier. Nicolae
        RedDayHoliday(12, 13, HolidayRank.IMPORTANT_SAINT),// Sf. Dosoftei
        RedDayHoliday(12, 25, HolidayRank.GREAT_FEAST), // Nașterea Domnului
        RedDayHoliday(12, 26, HolidayRank.GREAT_FEAST), // Soborul Maicii Domnului
        RedDayHoliday(12, 27, HolidayRank.IMPORTANT_SAINT) // Sf. Arhidiacon Ștefan
    )

    private val mobileHolidayKeywords: Map<String, HolidayRank> = mapOf(
        "Intrarea Domnului în Ierusalim" to HolidayRank.GREAT_FEAST,
        "Floriile" to HolidayRank.GREAT_FEAST,
        "Învierea Domnului" to HolidayRank.GREAT_FEAST,
        "Sfintele Paști" to HolidayRank.GREAT_FEAST,
        "A doua zi de Paști" to HolidayRank.GREAT_FEAST,
        "A treia zi de Paști" to HolidayRank.GREAT_FEAST,
        "Izvorul Tămăduirii" to HolidayRank.GREAT_FEAST,
        "Înălțarea Domnului" to HolidayRank.GREAT_FEAST,
        "Pogorârea Sfântului Duh" to HolidayRank.GREAT_FEAST,
        "Rusaliile" to HolidayRank.GREAT_FEAST,
        "Sfânta Treime" to HolidayRank.GREAT_FEAST
    )

    /**
     * Verifică statutul unei zile, returnând rangul sărbătorii.
     * @param gregorianCalendar Calendarul pe Stil Nou (civil) folosit de UI.
     */
    fun getHolidayInfo(data: CalendarData?, gregorianCalendar: Calendar): HolidayRank? {
        if (data == null) return null

        // **AICI ESTE CORECȚIA ESENȚIALĂ**
        // Convertim data curentă (Stil Nou) la Stilul Vechi (Iulian) scăzând 13 zile.
        val julianCalendar = gregorianCalendar.clone() as Calendar
        julianCalendar.add(Calendar.DAY_OF_YEAR, -13)

        // Verificăm sărbătorile în ordinea corectă a importanței

        // Pasul 1: Verificarea sărbătorilor cu dată variabilă (au prioritate maximă)
        val summaryTitle = data.summaryTitleRo.trim()
        for ((keyword, rank) in mobileHolidayKeywords) {
            if (summaryTitle.equals(keyword, ignoreCase = true)) {
                return rank
            }
        }

        // Pasul 2: Verificarea sărbătorilor cu dată fixă (folosind data convertită pe stil vechi)
        val month = julianCalendar.get(Calendar.MONTH) + 1
        val day = julianCalendar.get(Calendar.DAY_OF_MONTH)
        val fixedHoliday = fixedHolidays.find { it.month == month && it.day == day }
        if (fixedHoliday != null) {
            return fixedHoliday.rank
        }

        // Pasul 3: Verificarea Duminicilor
        if (gregorianCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            return HolidayRank.IMPORTANT_SAINT
        }

        // Pasul 4: Nu este o sărbătoare marcată cu roșu
        return null
    }
}