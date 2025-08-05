package md.ortodox.ortodoxmd.ui.bible

object BibleBookMapper {

    // Maparea de la diverse abrevieri/nume la numele canonic din baza de date
    private val bookNameMap = mapOf(
        // Pentateuh
        "facerea" to "FACEREA (GENEZA)",
        "geneza" to "FACEREA (GENEZA)",
        "gen" to "FACEREA (GENEZA)",
        "iesirea" to "IEȘIREA (EXODUL)",
        "exodul" to "IEȘIREA (EXODUL)",
        "ex" to "IEȘIREA (EXODUL)",
        "leviticul" to "LEVITICUL",
        "lev" to "LEVITICUL",
        "numerii" to "NUMERII",
        "num" to "NUMERII",
        "deuteronomul" to "DEUTERONOMUL",
        "deut" to "DEUTERONOMUL",

        // Cărți Istorice
        "iosua" to "IOSUA NAVI",
        "judecatori" to "JUDECĂTORI",
        "jud" to "JUDECĂTORI",
        "rut" to "RUT",
        "1 regi" to "CARTEA ÎNTÂI A REGILOR",
        "1 samuel" to "CARTEA ÎNTÂI A REGILOR",
        "2 regi" to "CARTEA A DOUA A REGILOR",
        "2 samuel" to "CARTEA A DOUA A REGILOR",
        "3 regi" to "CARTEA A TREIA A REGILOR",
        "1 imparati" to "CARTEA A TREIA A REGILOR",
        "4 regi" to "CARTEA A PATRA A REGILOR",
        "2 imparati" to "CARTEA A PATRA A REGILOR",
        "1 cronici" to "I PARALIPOMENA (I CRONICI)",
        "1 paralipomena" to "I PARALIPOMENA (I CRONICI)",
        "2 cronici" to "II PARALIPOMENA (II CRONICI)",
        "2 paralipomena" to "II PARALIPOMENA (II CRONICI)",
        "ezdra" to "EZDRA",
        "neemia" to "NEEMIA (A DOUA EZDRA)",
        "estera" to "ESTERA",

        // Cărți Poetice și de Înțelepciune
        "iov" to "IOV",
        "psalmi" to "PSALMII",
        "ps" to "PSALMII",
        "pilde" to "PILDELE LUI SOLOMON",
        "proverbe" to "PILDELE LUI SOLOMON",
        "eclesiastul" to "ECCLESIASTUL",
        "cantarea cantarilor" to "CÂNTAREA CÂNTĂRILOR",

        // Profeți Mari
        "isaia" to "ISAIA",
        "is" to "ISAIA",
        "ieremia" to "IEREMIA",
        "plangerile" to "PLÂNGERILE LUI IEREMIA",
        "iezechiel" to "IEZECHIEL",
        "daniel" to "DANIEL",

        // Profeți Mici
        "osea" to "OSEA",
        "ioil" to "IOIL",
        "amos" to "AMOS",
        "avdie" to "AVDIE",
        "iona" to "IONA",
        "miheia" to "MIHEIA",
        "naum" to "NAUM",
        "avacum" to "AVACUM",
        "sofonie" to "SOFONIE",
        "agheu" to "AGHEU",
        "zaharia" to "ZAHARIA",
        "maleahi" to "MALEAHI",

        // Noul Testament - Evanghelii
        "matei" to "EVANGHELIA DUPĂ MATEI",
        "mt" to "EVANGHELIA DUPĂ MATEI",
        "marcu" to "EVANGHELIA DUPĂ MARCU",
        "mc" to "EVANGHELIA DUPĂ MARCU",
        "luca" to "EVANGHELIA DUPĂ LUCA",
        "lc" to "EVANGHELIA DUPĂ LUCA",
        "ioan" to "EVANGHELIA DUPĂ IOAN",
        "in" to "EVANGHELIA DUPĂ IOAN",

        // Fapte și Epistole Pauline
        "fapte" to "FAPTELE APOSTOLILOR",
        "fa" to "FAPTELE APOSTOLILOR",
        "romani" to "EPISTOLA CĂTRE ROMANI A SFÂNTULUI APOSTOL PAVEL",
        "rom" to "EPISTOLA CĂTRE ROMANI A SFÂNTULUI APOSTOL PAVEL",
        "1 corinteni" to "EPISTOLA ÎNTÂI CĂTRE CORINTENI A SFÂNTULUI APOSTOL PAVEL",
        "1 cor" to "EPISTOLA ÎNTÂI CĂTRE CORINTENI A SFÂNTULUI APOSTOL PAVEL",
        "2 corinteni" to "EPISTOLA A DOUA CĂTRE CORINTENI A SFÂNTULUI APOSTOL PAVEL",
        "2 cor" to "EPISTOLA A DOUA CĂTRE CORINTENI A SFÂNTULUI APOSTOL PAVEL",
        "galateni" to "EPISTOLA CĂTRE GALATENI A SFÂNTULUI APOSTOL PAVEL",
        "gal" to "EPISTOLA CĂTRE GALATENI A SFÂNTULUI APOSTOL PAVEL",
        "efeseni" to "EPISTOLA CĂTRE EFESENI A SFÂNTULUI APOSTOL PAVEL",
        "efes" to "EPISTOLA CĂTRE EFESENI A SFÂNTULUI APOSTOL PAVEL",
        "filipeni" to "EPISTOLA CĂTRE FILIPENI A SFÂNTULUI APOSTOL PAVEL",
        "fil" to "EPISTOLA CĂTRE FILIPENI A SFÂNTULUI APOSTOL PAVEL",
        "coloseni" to "EPISTOLA CĂTRE COLOSENI A SFÂNTULUI APOSTOL PAVEL",
        "col" to "EPISTOLA CĂTRE COLOSENI A SFÂNTULUI APOSTOL PAVEL",
        "1 tesaloniceni" to "EPISTOLA ÎNTÂI CĂTRE TESALONICENI A SFÂNTULUI APOSTOL PAVEL",
        "1 tes" to "EPISTOLA ÎNTÂI CĂTRE TESALONICENI A SFÂNTULUI APOSTOL PAVEL",
        "2 tesaloniceni" to "EPISTOLA A DOUA CĂTRE TESALONICENI A SFÂNTULUI APOSTOL PAVEL",
        "2 tes" to "EPISTOLA A DOUA CĂTRE TESALONICENI A SFÂNTULUI APOSTOL PAVEL",
        "1 timotei" to "EPISTOLA ÎNTÂI CĂTRE TIMOTEI A SFÂNTULUI APOSTOL PAVEL",
        "1 tim" to "EPISTOLA ÎNTÂI CĂTRE TIMOTEI A SFÂNTULUI APOSTOL PAVEL",
        "2 timotei" to "EPISTOLA A DOUA CĂTRE TIMOTEI A SFÂNTULUI APOSTOL PAVEL",
        "2 tim" to "EPISTOLA A DOUA CĂTRE TIMOTEI A SFÂNTULUI APOSTOL PAVEL",
        "tit" to "EPISTOLA CĂTRE TIT A SFÂNTULUI APOSTOL PAVEL",
        "filimon" to "EPISTOLA CĂTRE FILIMON A SFÂNTULUI APOSTOL PAVEL",
        "evrei" to "EPISTOLA CĂTRE EVREI A SFÂNTULUI APOSTOL PAVEL",
        "evr" to "EPISTOLA CĂTRE EVREI A SFÂNTULUI APOSTOL PAVEL",

        // Epistole Sobornicești și Apocalipsa
        "iacov" to "EPISTOLA SOBORNICEASCĂ A SFÂNTULUI APOSTOL IACOV",
        "1 petru" to "EPISTOLA ÎNTÂI SOBORNICEASCĂ A SFÂNTULUI APOSTOL PETRU",
        "1 pt" to "EPISTOLA ÎNTÂI SOBORNICEASCĂ A SFÂNTULUI APOSTOL PETRU",
        "2 petru" to "EPISTOLA A DOUA SOBORNICEASCĂ A SFÂNTULUI APOSTOL PETRU",
        "2 pt" to "EPISTOLA A DOUA SOBORNICEASCĂ A SFÂNTULUI APOSTOL PETRU",
        "1 ioan" to "EPISTOLA ÎNTÂI SOBORNICEASCĂ A SFÂNTULUI APOSTOL IOAN",
        "1 in" to "EPISTOLA ÎNTÂI SOBORNICEASCĂ A SFÂNTULUI APOSTOL IOAN",
        "2 ioan" to "EPISTOLA A DOUA SOBORNICEASCĂ A SFÂNTULUI APOSTOL IOAN",
        "2 in" to "EPISTOLA A DOUA SOBORNICEASCĂ A SFÂNTULUI APOSTOL IOAN",
        "3 ioan" to "EPISTOLA A TREIA SOBORNICEASCĂ A SFÂNTULUI APOSTOL IOAN",
        "3 in" to "EPISTOLA A TREIA SOBORNICEASCĂ A SFÂNTULUI APOSTOL IOAN",
        "iuda" to "EPISTOLA SOBORNICEASCĂ A SFÂNTULUI APOSTOL IUDA",
        "apocalipsa" to "APOCALIPSA SFÂNTULUI IOAN TEOLOGUL",
        "apoc" to "APOCALIPSA SFÂNTULUI IOAN TEOLOGUL"
    )

    // Lista de chei sortate după lungime, descrescător, pentru a potrivi corect "2 Tesaloniceni" înaintea lui "Tesaloniceni"
    private val sortedKeys = bookNameMap.keys.sortedByDescending { it.length }

    /**
     * Găsește numele canonic al cărții pe baza unei posibile abrevieri sau nume popular.
     * @param input Textul introdus de utilizator care ar putea conține numele cărții.
     * @return O pereche conținând numele canonic și restul textului de după numele cărții.
     */
    fun findBook(input: String): Pair<String, String>? {
        val normalizedInput = input.trim().lowercase()
        for (key in sortedKeys) {
            if (normalizedInput.startsWith(key)) {
                val canonicalName = bookNameMap[key] ?: continue
                val restOfString = input.substring(key.length).trim()
                return Pair(canonicalName, restOfString)
            }
        }
        return null
    }
}