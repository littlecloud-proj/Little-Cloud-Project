package com.littlecloud.app.data

import com.littlecloud.app.time.*

/**
 * Feasts of the Americas, kept on top of the universal Roman calendar (which does not have them).
 *
 * - Our Lady of Guadalupe, Patroness of the Americas, on December 12, kept everywhere. Its rank in the
 *   Ordo is not yet confirmed: until it is, the feast takes the place of an Advent weekday (which is
 *   commemorated) and gives way to an Advent Sunday (where it is commemorated).
 * - With the American rules: St. Peter Claver (September 9) and Sts. Isaac Jogues, John de Brébeuf and
 *   Companions, the North American Martyrs (September 26), as kept in the United States.
 *
 * Collects and lessons are from the Divinum Officium Project (Sancti/12-12n, 09-09n, 09-26n); the English
 * of the readings is the Douay-Rheims, taken by the citation. A feast whose texts are not on file in a
 * published source is not added.
 */
object Americas {
    const val GUADALUPE = "Our Lady of Guadalupe, Patroness of the Americas"

    class Feast(
        val month: Int,
        val day: Int,
        val title: String,
        /** The class it takes when kept (2 = II class, 3 = double). */
        val cls: Int,
        val color: LColor,
        val rank: String,
        /** Kept only with the American rules of fast and abstinence. */
        val usOnly: Boolean,
        val collect: Collects.Collect,
        private val epistle: String,
        private val gospel: String,
    ) {
        fun readings(): List<Readings.Reading> = listOfNotNull(
            Readings.byCitation(epistle, gospel = false),
            Readings.byCitation(gospel, gospel = true),
        )
    }

    private const val PER_DOMINUM_LA = "Per Dóminum nostrum Jesum Christum, Fílium tuum: qui tecum vivit et regnat in unitáte Spíritus Sancti, Deus, per ómnia sǽcula sæculórum."
    private const val PER_DOMINUM_EN = "Through Jesus Christ, thy Son our Lord, who liveth and reigneth with thee, in the unity of the Holy Ghost, God, world without end."
    private const val PER_EUNDEM_LA = "Per eúndem Dóminum nostrum Jesum Christum Fílium tuum, qui tecum vivit et regnat in unitáte Spíritus Sancti, Deus, per ómnia sǽcula sæculórum."
    private const val PER_EUNDEM_EN = "Through the same Jesus Christ, thy Son, our Lord, who liveth and reigneth with thee in the unity of the Holy Ghost, God, world without end."

    val feasts = listOf(
        Feast(
            12, 12, GUADALUPE, 2, LColor.WHITE, "Feast of the Americas · rank to be confirmed by the Ordo", usOnly = false,
            collect = Collects.Collect(
                "Deus, qui sub beatíssimæ Vírginis Maríæ singulári patrocínio constitútos perpétuis benefíciis nos cumulári " +
                    "voluísti: præsta supplícibus tuis; ut, cujus hódie commemoratióne lætámur in terris, ejus conspéctu perfruámur in cælis.\n" +
                    "Per Dóminum nostrum Jesum Christum, Fílium tuum: qui tecum vivit et regnat in unitáte Spíritus Sancti, Deus, per ómnia sǽcula sæculórum.",
                "O God, who hast placed us under the special patronage of the most Blessed Virgin Mary, and hast been pleased to " +
                    "shower perpetual blessings upon us: grant to thy suppliants, that as we rejoice on earth in her commemoration today, " +
                    "we may enjoy the sight of her in heaven.\n" +
                    "Through Jesus Christ, thy Son our Lord, who liveth and reigneth with thee in the unity of the Holy Ghost, God, world without end.",
            ),
            epistle = "Eccli 24:23-31", gospel = "Luc 1:39-47",
        ),
        Feast(
            9, 9, "St. Peter Claver, Confessor", 3, LColor.WHITE, "Double · kept in the United States", usOnly = true,
            collect = Collects.Collect(
                "Deus, qui, abréptos in servitútem Nigrítas ad agnitiónem tui nóminis vocatúrus, beátum Petrum mira in eis iuvándis " +
                    "caritáte et patiéntia roborásti: eius nobis intercessióne concéde; ut, quæ Iesu Christi sunt quæréntes, próximos " +
                    "ópere et veritáte diligámus.\n$PER_EUNDEM_LA",
                "O God, who to call enslaved Negroes to know your name, strengthened blessed Peter with wondrous love and patience in " +
                    "helping them; grant by his intercession, that, seeking the things of Jesus Christ, we may love our neighbors in " +
                    "deed and in truth.\n$PER_EUNDEM_EN",
            ),
            epistle = "Isa 58:6-10", gospel = "Luc 10:29-37",
        ),
        Feast(
            9, 26, "Sts. Isaac Jogues, John de Brébeuf and Companions, Martyrs", 3, LColor.RED, "Double · kept in the United States", usOnly = true,
            collect = Collects.Collect(
                "Deus, qui primítias fídei in amplíssimis Boreális Americæ regiónibus sanctórum Mártyrum tuórum Joánnis, Isaáci " +
                    "eorúmque Sociórum prædicatióne et sánguine consecrásti: concéde propítius: ut, eórum intercessióne, flórida " +
                    "christianórum seges ubíque in dies augeátur.\n$PER_DOMINUM_LA",
                "O God, Who, by the preaching and the blood of Your holy Martyrs, John, Isaac and their companions, consecrated the " +
                    "first fruits of the faith in the vast areas of North America, mercifully grant that, through their intercession, " +
                    "the fruitful harvest of Christians may everywhere increase day by day.\n$PER_DOMINUM_EN",
            ),
            epistle = "2 Cor 12:11-15", gospel = "Luc 6:17-23",
        ),
    )

    private fun feastOn(d: LocalDate, discipline: Discipline): Feast? =
        feasts.firstOrNull { it.month == d.monthValue && it.day == d.dayOfMonth && (!it.usOnly || discipline == Discipline.US_1951) }

    /** The feast kept as the office of the day, if any: not on a Sunday, nor over a day of higher rank. */
    fun kept(base: LiturgicalDay, discipline: Discipline): Feast? =
        feastOn(base.date, discipline)?.takeIf { base.date.dayOfWeek != DayOfWeek.SUNDAY && base.cls >= it.cls }

    /** A plain weekday or Saturday of Our Lady gives way without being commemorated. */
    private fun plain(title: String) = title == "Feria" || title.startsWith("Feria ·") || title == "Saturday of Our Lady"

    fun apply(day: LiturgicalDay, discipline: Discipline, rubrics: String): LiturgicalDay {
        val f = feastOn(day.date, discipline) ?: return day
        return if (kept(day, discipline) != null) day.copy(
            title = f.title,
            cls = if (rubrics == "1962") 3 else f.cls,
            color = f.color,
            commemorations = (if (plain(day.title)) emptyList() else listOf(day.title)) + day.commemorations,
            rankText = f.rank,
        ) else day.copy(commemorations = day.commemorations + f.title)
    }
}
