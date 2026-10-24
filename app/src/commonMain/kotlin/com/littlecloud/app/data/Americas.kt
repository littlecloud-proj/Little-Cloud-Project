package com.littlecloud.app.data

import com.littlecloud.app.time.*

/**
 * Feasts of the Americas, kept on top of the universal Roman calendar (which does not have them).
 * For now: Our Lady of Guadalupe, Patroness of the Americas, on December 12. Its Mass and collect are
 * from the Divinum Officium Project (Sancti/12-12n): the lesson of Our Lady of Mount Carmel
 * (Ecclus. 24:23-31) and the Gospel of the Visitation (Luke 1:39-47).
 *
 * Its rank in the CMRI's Ordo is not yet confirmed. Until it is, the feast takes the place of an
 * Advent weekday (which is commemorated) and gives way to an Advent Sunday (where it is commemorated).
 */
object Americas {
    const val GUADALUPE = "Our Lady of Guadalupe, Patroness of the Americas"
    private const val RANK = "Feast of the Americas · rank to be confirmed by the Ordo"

    private fun isGuadalupe(d: LocalDate) = d.monthValue == 12 && d.dayOfMonth == 12

    /** Whether the feast is kept as the office of the day (not on a Sunday). */
    fun kept(d: LocalDate) = isGuadalupe(d) && d.dayOfWeek != DayOfWeek.SUNDAY

    fun apply(day: LiturgicalDay, rubrics: String): LiturgicalDay = when {
        !isGuadalupe(day.date) -> day
        kept(day.date) -> day.copy(
            title = GUADALUPE,
            cls = if (rubrics == "1962") 3 else 2,
            color = LColor.WHITE,
            commemorations = listOf(day.title) + day.commemorations,
            rankText = RANK,
        )
        else -> day.copy(commemorations = day.commemorations + GUADALUPE)
    }

    val collect = Collects.Collect(
        "Deus, qui sub beatíssimæ Vírginis Maríæ singulári patrocínio constitútos perpétuis benefíciis nos cumulári " +
            "voluísti: præsta supplícibus tuis; ut, cujus hódie commemoratióne lætámur in terris, ejus conspéctu perfruámur in cælis.\n" +
            "Per Dóminum nostrum Jesum Christum, Fílium tuum: qui tecum vivit et regnat in unitáte Spíritus Sancti, Deus, per ómnia sǽcula sæculórum.",
        "O God, who hast placed us under the special patronage of the most Blessed Virgin Mary, and hast been pleased to " +
            "shower perpetual blessings upon us: grant to thy suppliants, that as we rejoice on earth in her commemoration today, " +
            "we may enjoy the sight of her in heaven.\n" +
            "Through Jesus Christ, thy Son our Lord, who liveth and reigneth with thee in the unity of the Holy Ghost, God, world without end.",
    )

    fun readings(): List<Readings.Reading> = listOfNotNull(
        Readings.byCitation("Eccli 24:23-31", gospel = false),
        Readings.byCitation("Luc 1:39-47", gospel = true),
    )
}
