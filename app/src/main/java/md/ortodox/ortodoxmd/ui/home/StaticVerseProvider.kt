package md.ortodox.ortodoxmd.ui.home

data class DailyVerse(val text: String, val reference: String)

object StaticVerseProvider {
    private val verses = listOf(
        DailyVerse("Veniţi la Mine toţi cei osteniţi şi împovăraţi şi Eu vă voi odihni pe voi.", "Matei 11:28"),
        DailyVerse("Căci Dumnezeu aşa a iubit lumea, încât pe Fiul Său Cel Unul-Născut L-a dat, ca oricine crede în El să nu piară, ci să aibă viaţă veşnică.", "Ioan 3:16"),
        DailyVerse("Toate le pot întru Hristos, Cel care mă întăreşte.", "Filipeni 4:13"),
        DailyVerse("Chiar de voi şi umbla prin mijlocul umbrei morţii, nu mă voi teme de rele, că Tu cu mine eşti.", "Psalmul 22:4"),
        DailyVerse("Eu sunt Calea, Adevărul şi Viaţa. Nimeni nu vine la Tatăl Meu decât prin Mine.", "Ioan 14:6"),
        DailyVerse("Cereţi şi vi se va da; căutaţi şi veţi afla; bateţi şi vi se va deschide.", "Matei 7:7"),
        DailyVerse("Dacă Dumnezeu este pentru noi, cine este împotriva noastră?", "Romani 8:31")
    )

    fun getRandomVerse(): DailyVerse {
        return verses.random()
    }
}
