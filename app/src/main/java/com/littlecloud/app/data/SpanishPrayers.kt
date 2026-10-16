package com.littlecloud.app.data

/**
 * Traditional Spanish texts of the prayers, as found in the pre-conciliar catechisms
 * (Astete, Ripalda) and devotional manuals. Prayers missing here have no approved
 * Spanish text on file yet; the app shows the Latin (or English) instead.
 */
object SpanishPrayers {
    private val pater = "Padre nuestro, que estás en los cielos, santificado sea tu nombre; venga a nosotros tu reino; hágase tu voluntad así en la tierra como en el cielo. El pan nuestro de cada día dánosle hoy; y perdónanos nuestras deudas, así como nosotros perdonamos a nuestros deudores; y no nos dejes caer en la tentación, mas líbranos del mal. Amén."
    private val ave = "Dios te salve, María, llena eres de gracia, el Señor es contigo; bendita tú eres entre todas las mujeres, y bendito es el fruto de tu vientre, Jesús. Santa María, Madre de Dios, ruega por nosotros pecadores, ahora y en la hora de nuestra muerte. Amén."
    private val gloria = "Gloria al Padre, y al Hijo, y al Espíritu Santo. Como era en el principio, ahora y siempre, y por los siglos de los siglos. Amén."

    val texts: Map<String, String> = mapOf(
        "sign_cross" to "En el nombre del Padre, y del Hijo, y del Espíritu Santo. Amén.",
        "pater" to pater,
        "ave" to ave,
        "gloria" to gloria,
        "pag" to "$pater\n\n$ave\n\n$gloria",
        "creed" to "Creo en Dios Padre todopoderoso, Creador del cielo y de la tierra; y en Jesucristo, su único Hijo, Nuestro Señor, que fue concebido por obra y gracia del Espíritu Santo, nació de Santa María Virgen; padeció bajo el poder de Poncio Pilato, fue crucificado, muerto y sepultado; descendió a los infiernos; al tercer día resucitó de entre los muertos; subió a los cielos y está sentado a la diestra de Dios Padre todopoderoso; desde allí ha de venir a juzgar a los vivos y a los muertos. Creo en el Espíritu Santo, la santa Iglesia católica, la comunión de los santos, el perdón de los pecados, la resurrección de la carne y la vida perdurable. Amén.",
        "contrition" to "Señor mío Jesucristo, Dios y Hombre verdadero, Creador, Padre y Redentor mío; por ser Vos quien sois, bondad infinita, y porque os amo sobre todas las cosas, me pesa de todo corazón de haberos ofendido; también me pesa porque podéis castigarme con las penas del infierno. Ayudado de vuestra divina gracia, propongo firmemente nunca más pecar, confesarme y cumplir la penitencia que me fuere impuesta. Amén.",
        "angele_dei" to "Ángel de Dios, que eres mi custodio, pues la bondad divina me ha encomendado a ti, ilumíname, guárdame, rígeme y gobiérname. Amén.",
        "angelus" to "℣. El Ángel del Señor anunció a María.\n℟. Y concibió por obra del Espíritu Santo.\nDios te salve, María…\n\n" +
            "℣. He aquí la esclava del Señor.\n℟. Hágase en mí según tu palabra.\nDios te salve, María…\n\n" +
            "℣. Y el Verbo se hizo carne.\n℟. Y habitó entre nosotros.\nDios te salve, María…\n\n" +
            "℣. Ruega por nosotros, santa Madre de Dios.\n℟. Para que seamos dignos de alcanzar las promesas de Nuestro Señor Jesucristo.\n\n" +
            "Oremos. Infunde, Señor, tu gracia en nuestras almas, para que, los que hemos conocido por el anuncio del Ángel la Encarnación de tu Hijo Jesucristo, por su Pasión y su Cruz seamos llevados a la gloria de su Resurrección. Por el mismo Cristo Nuestro Señor. Amén.",
        "regina_caeli" to "Reina del cielo, alégrate, aleluya.\nPorque el que mereciste llevar en tu seno, aleluya.\nResucitó según dijo, aleluya.\nRuega por nosotros a Dios, aleluya.\n\n℣. Gózate y alégrate, Virgen María, aleluya.\n℟. Porque verdaderamente ha resucitado el Señor, aleluya.\n\nOremos. Oh Dios, que por la resurrección de tu Hijo, Nuestro Señor Jesucristo, te dignaste alegrar al mundo, concédenos que por su Madre, la Virgen María, alcancemos los gozos de la vida eterna. Por el mismo Cristo Nuestro Señor. Amén.",
        "salve_regina" to "Dios te salve, Reina y Madre de misericordia, vida, dulzura y esperanza nuestra; Dios te salve. A ti llamamos los desterrados hijos de Eva; a ti suspiramos, gimiendo y llorando en este valle de lágrimas. Ea, pues, Señora, abogada nuestra, vuelve a nosotros esos tus ojos misericordiosos; y después de este destierro muéstranos a Jesús, fruto bendito de tu vientre. ¡Oh clementísima, oh piadosa, oh dulce siempre Virgen María!",
        "sub_tuum" to "Bajo tu amparo nos acogemos, santa Madre de Dios; no desprecies las súplicas que te dirigimos en nuestras necesidades, antes bien líbranos siempre de todos los peligros, oh Virgen gloriosa y bendita.",
        "memorare" to "Acordaos, oh piadosísima Virgen María, que jamás se ha oído decir que ninguno de los que han acudido a vuestra protección, implorado vuestra asistencia y reclamado vuestro socorro, haya sido abandonado de Vos. Animado con esta confianza, a Vos también acudo, oh Madre, Virgen de las vírgenes, y aunque gimiendo bajo el peso de mis pecados, me atrevo a comparecer ante vuestra presencia soberana. No desechéis, oh Madre de Dios, mis humildes súplicas, antes bien escuchadlas y acogedlas benignamente. Amén.",
        "anima_christi" to "Alma de Cristo, santifícame. Cuerpo de Cristo, sálvame. Sangre de Cristo, embriágame. Agua del costado de Cristo, lávame. Pasión de Cristo, confórtame. ¡Oh buen Jesús!, óyeme. Dentro de tus llagas, escóndeme. No permitas que me aparte de Ti. Del maligno enemigo, defiéndeme. En la hora de mi muerte, llámame. Y mándame ir a Ti, para que con tus santos te alabe, por los siglos de los siglos. Amén.",
        "st_michael" to "San Miguel Arcángel, defiéndenos en la batalla; sé nuestro amparo contra la perversidad y asechanzas del demonio. Reprímale Dios, pedimos suplicantes; y tú, Príncipe de la milicia celestial, arroja al infierno con el divino poder a Satanás y a los otros espíritus malignos que andan dispersos por el mundo para la perdición de las almas. Amén.",
        "spiritual_communion" to "Creo, Jesús mío, que estáis realmente presente en el Santísimo Sacramento del Altar. Os amo sobre todas las cosas y deseo recibiros en mi alma. Pero ya que ahora no puedo recibiros sacramentado, venid a lo menos espiritualmente a mi corazón. Y como si ya os hubiese recibido, os abrazo y me uno del todo a Vos. No permitáis, Señor, que jamás me aparte de Vos. Amén.",
        "rosary" to "Misterios gozosos (lunes y jueves): la Encarnación del Hijo de Dios; la Visitación de Nuestra Señora a su prima Santa Isabel; el Nacimiento del Hijo de Dios; la Presentación de Jesús en el templo; el Niño Jesús perdido y hallado en el templo.\n\n" +
            "Misterios dolorosos (martes y viernes): la Oración en el Huerto; la Flagelación del Señor; la Coronación de espinas; Jesús con la Cruz a cuestas; la Crucifixión y muerte del Señor.\n\n" +
            "Misterios gloriosos (miércoles, sábado y domingo): la Resurrección del Señor; la Ascensión del Señor; la Venida del Espíritu Santo; la Asunción de Nuestra Señora; la Coronación de la Santísima Virgen.\n\nCómo rezarlo\n1. Haz la señal de la cruz y reza el Credo.\n2. Reza un Padre nuestro, tres Avemarías y un Gloria.\n3. En cada misterio, anúncialo y medítalo mientras rezas un Padre nuestro, diez Avemarías y un Gloria.\n4. Al terminar, reza la Salve.\n\n℣. Ruega por nosotros, santa Madre de Dios.\n℟. Para que seamos dignos de alcanzar las promesas de Nuestro Señor Jesucristo.",
        "examen" to "1. Ponte en la presencia de Dios.\n2. Da gracias por los beneficios de este día.\n3. Pide luz para conocer tus faltas.\n4. Repasa el día: pensamientos, palabras, obras y omisiones.\n5. Pide perdón y haz un acto de contrición.\n6. Propón la enmienda, con la ayuda de la gracia de Dios.",
    )

    fun get(id: String?): String? = id?.let { texts[it] ?: MorePrayers.spanish[it] }
}
