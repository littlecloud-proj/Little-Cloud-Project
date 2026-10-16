package com.littlecloud.app.data

/**
 * Traditional prayers. Only public-domain texts are included here.
 * latin = null means the prayer is shown in English only for now.
 */
data class Prayer(
    val id: String,
    val title: String,
    val subtitle: String,
    val latin: String?,
    val english: String,
)

object PrayerBook {

    private val pater = Prayer(
        "pater", "Our Father", "Pater Noster",
        "Pater noster, qui es in cælis, sanctificétur nomen tuum. Advéniat regnum tuum. Fiat volúntas tua, sicut in cælo et in terra. Panem nostrum quotidiánum da nobis hódie, et dimítte nobis débita nostra, sicut et nos dimíttimus debitóribus nostris. Et ne nos indúcas in tentatiónem: sed líbera nos a malo. Amen.",
        "Our Father, who art in heaven, hallowed be Thy name; Thy kingdom come; Thy will be done on earth as it is in heaven. Give us this day our daily bread; and forgive us our trespasses, as we forgive those who trespass against us; and lead us not into temptation, but deliver us from evil. Amen."
    )

    private val ave = Prayer(
        "ave", "Hail Mary", "Ave Maria",
        "Ave María, grátia plena, Dóminus tecum. Benedícta tu in muliéribus, et benedíctus fructus ventris tui, Jesus. Sancta María, Mater Dei, ora pro nobis peccatóribus, nunc et in hora mortis nostræ. Amen.",
        "Hail Mary, full of grace, the Lord is with thee; blessed art thou amongst women, and blessed is the fruit of thy womb, Jesus. Holy Mary, Mother of God, pray for us sinners, now and at the hour of our death. Amen."
    )

    private val gloria = Prayer(
        "gloria", "Glory Be", "Gloria Patri",
        "Glória Patri, et Fílio, et Spirítui Sancto. Sicut erat in princípio, et nunc, et semper, et in sǽcula sæculórum. Amen.",
        "Glory be to the Father, and to the Son, and to the Holy Ghost. As it was in the beginning, is now, and ever shall be, world without end. Amen."
    )

    val all: List<Prayer> = listOf(
        Prayer(
            "sign_cross", "Sign of the Cross", "Signum Crucis",
            "In nómine Patris, et Fílii, et Spíritus Sancti. Amen.",
            "In the name of the Father, and of the Son, and of the Holy Ghost. Amen."
        ),
        pater, ave, gloria,
        Prayer(
            "pag", "Pater, Ave, Gloria", "Our Father · Hail Mary · Glory Be",
            pater.latin + "\n\n" + ave.latin + "\n\n" + gloria.latin,
            pater.english + "\n\n" + ave.english + "\n\n" + gloria.english
        ),
        Prayer(
            "creed", "Apostles’ Creed", "Symbolum Apostolorum",
            "Credo in Deum Patrem omnipoténtem, Creatórem cæli et terræ. Et in Jesum Christum, Fílium ejus únicum, Dóminum nostrum: qui concéptus est de Spíritu Sancto, natus ex María Vírgine, passus sub Póntio Piláto, crucifíxus, mórtuus, et sepúltus: descéndit ad ínferos; tértia die resurréxit a mórtuis; ascéndit ad cælos; sedet ad déxteram Dei Patris omnipoténtis: inde ventúrus est judicáre vivos et mórtuos. Credo in Spíritum Sanctum, sanctam Ecclésiam cathólicam, Sanctórum communiónem, remissiónem peccatórum, carnis resurrectiónem, vitam ætérnam. Amen.",
            "I believe in God, the Father Almighty, Creator of heaven and earth; and in Jesus Christ, His only Son, our Lord; who was conceived by the Holy Ghost, born of the Virgin Mary, suffered under Pontius Pilate, was crucified, died, and was buried. He descended into hell; the third day He rose again from the dead; He ascended into heaven, and sitteth at the right hand of God, the Father Almighty; from thence He shall come to judge the living and the dead. I believe in the Holy Ghost, the Holy Catholic Church, the communion of saints, the forgiveness of sins, the resurrection of the body, and life everlasting. Amen."
        ),
        Prayer(
            "morning_offering", "Morning Offering", "Through the Immaculate Heart of Mary",
            null,
            "O Jesus, through the Immaculate Heart of Mary, I offer Thee my prayers, works, joys and sufferings of this day for all the intentions of Thy Sacred Heart, in union with the Holy Sacrifice of the Mass throughout the world, in reparation for my sins, for the intentions of all our associates, and in particular for the intention recommended this month by the Holy Father. Amen."
        ),
        Prayer(
            "acts", "Acts of Faith, Hope & Charity", "Actus Fidei, Spei et Caritatis",
            null,
            "Act of Faith\nO my God, I firmly believe that Thou art one God in three Divine Persons, Father, Son, and Holy Ghost; I believe that Thy Divine Son became man, and died for our sins, and that He will come to judge the living and the dead. I believe these and all the truths which the Holy Catholic Church teaches, because Thou hast revealed them, who canst neither deceive nor be deceived. Amen.\n\n" +
                "Act of Hope\nO my God, relying on Thy infinite goodness and promises, I hope to obtain pardon of my sins, the help of Thy grace, and life everlasting, through the merits of Jesus Christ, my Lord and Redeemer. Amen.\n\n" +
                "Act of Charity\nO my God, I love Thee above all things, with my whole heart and soul, because Thou art all-good and worthy of all love. I love my neighbour as myself for the love of Thee. I forgive all who have injured me, and ask pardon of all whom I have injured. Amen."
        ),
        Prayer(
            "contrition", "Act of Contrition", "Actus Contritionis",
            null,
            "O my God, I am heartily sorry for having offended Thee, and I detest all my sins, because I dread the loss of heaven and the pains of hell; but most of all because they offend Thee, my God, who art all-good and deserving of all my love. I firmly resolve, with the help of Thy grace, to confess my sins, to do penance, and to amend my life. Amen."
        ),
        Prayer(
            "angele_dei", "Angele Dei", "To my Guardian Angel",
            "Ángele Dei, qui custos es mei, me tibi commíssum pietáte supérna, hódie illúmina, custódi, rege, et gubérna. Amen.",
            "Angel of God, my guardian dear, to whom God’s love commits me here, ever this day be at my side, to light and guard, to rule and guide. Amen."
        ),
        Prayer(
            "angelus", "The Angelus", "Angelus Domini",
            "℣. Ángelus Dómini nuntiávit Maríæ.\n℟. Et concépit de Spíritu Sancto.\nAve María…\n\n" +
                "℣. Ecce ancílla Dómini.\n℟. Fiat mihi secúndum verbum tuum.\nAve María…\n\n" +
                "℣. Et Verbum caro factum est.\n℟. Et habitávit in nobis.\nAve María…\n\n" +
                "℣. Ora pro nobis, sancta Dei Génetrix.\n℟. Ut digni efficiámur promissiónibus Christi.\n\n" +
                "Orémus. Grátiam tuam, quǽsumus, Dómine, méntibus nostris infúnde; ut qui, Ángelo nuntiánte, Christi Fílii tui incarnatiónem cognóvimus, per passiónem ejus et crucem ad resurrectiónis glóriam perducámur. Per eúndem Christum Dóminum nostrum. Amen.",
            "℣. The Angel of the Lord declared unto Mary.\n℟. And she conceived of the Holy Ghost.\nHail Mary…\n\n" +
                "℣. Behold the handmaid of the Lord.\n℟. Be it done unto me according to thy word.\nHail Mary…\n\n" +
                "℣. And the Word was made flesh.\n℟. And dwelt among us.\nHail Mary…\n\n" +
                "℣. Pray for us, O holy Mother of God.\n℟. That we may be made worthy of the promises of Christ.\n\n" +
                "Let us pray. Pour forth, we beseech Thee, O Lord, Thy grace into our hearts; that we, to whom the Incarnation of Christ, Thy Son, was made known by the message of an angel, may by His Passion and Cross be brought to the glory of His Resurrection. Through the same Christ our Lord. Amen."
        ),
        Prayer(
            "regina_caeli", "Regina Cæli", "Said in place of the Angelus in Paschaltide",
            "Regína cæli, lætáre, allelúja.\nQuia quem meruísti portáre, allelúja.\nResurréxit, sicut dixit, allelúja.\nOra pro nobis Deum, allelúja.\n\n℣. Gaude et lætáre, Virgo María, allelúja.\n℟. Quia surréxit Dóminus vere, allelúja.\n\nOrémus. Deus, qui per resurrectiónem Fílii tui, Dómini nostri Jesu Christi, mundum lætificáre dignátus es: præsta, quǽsumus; ut, per ejus Genetrícem Vírginem Maríam, perpétuæ capiámus gáudia vitæ. Per eúndem Christum Dóminum nostrum. Amen.",
            "Queen of Heaven, rejoice, alleluia.\nFor He whom thou didst merit to bear, alleluia.\nHas risen, as He said, alleluia.\nPray for us to God, alleluia.\n\n℣. Rejoice and be glad, O Virgin Mary, alleluia.\n℟. For the Lord is truly risen, alleluia.\n\nLet us pray. O God, who by the resurrection of Thy Son, our Lord Jesus Christ, didst vouchsafe to give joy to the world; grant, we beseech Thee, that through His Mother, the Virgin Mary, we may obtain the joys of everlasting life. Through the same Christ our Lord. Amen."
        ),
        Prayer(
            "salve_regina", "Salve Regina", "Hail, Holy Queen",
            "Salve, Regína, mater misericórdiæ; vita, dulcédo et spes nostra, salve. Ad te clamámus, éxsules fílii Hevæ. Ad te suspirámus geméntes et flentes in hac lacrimárum valle. Eja ergo, advocáta nostra, illos tuos misericórdes óculos ad nos convérte. Et Jesum, benedíctum fructum ventris tui, nobis post hoc exsílium osténde. O clemens, o pia, o dulcis Virgo María.",
            "Hail, holy Queen, Mother of mercy, our life, our sweetness, and our hope. To thee do we cry, poor banished children of Eve; to thee do we send up our sighs, mourning and weeping in this vale of tears. Turn then, most gracious advocate, thine eyes of mercy toward us; and after this our exile show unto us the blessed fruit of thy womb, Jesus. O clement, O loving, O sweet Virgin Mary."
        ),
        Prayer(
            "sub_tuum", "Sub tuum præsidium", "We fly to thy patronage",
            "Sub tuum præsídium confúgimus, sancta Dei Génetrix: nostras deprecatiónes ne despícias in necessitátibus; sed a perículis cunctis líbera nos semper, Virgo gloriósa et benedícta.",
            "We fly to thy patronage, O holy Mother of God; despise not our petitions in our necessities, but deliver us always from all dangers, O glorious and blessed Virgin."
        ),
        Prayer(
            "memorare", "Memorare", "Remember, O most gracious Virgin Mary",
            "Memoráre, o piíssima Virgo María, non esse audítum a sǽculo, quemquam ad tua curréntem præsídia, tua implorántem auxília, tua peténtem suffrágia, esse derelíctum. Ego tali animátus confidéntia, ad te, Virgo Vírginum, Mater, curro, ad te vénio, coram te gemens peccátor assísto. Noli, Mater Verbi, verba mea despícere; sed áudi propítia et exáudi. Amen.",
            "Remember, O most gracious Virgin Mary, that never was it known that anyone who fled to thy protection, implored thy help, or sought thy intercession was left unaided. Inspired by this confidence, I fly unto thee, O Virgin of virgins, my Mother; to thee do I come, before thee I stand, sinful and sorrowful. O Mother of the Word Incarnate, despise not my petitions, but in thy mercy hear and answer me. Amen."
        ),
        Prayer(
            "anima_christi", "Anima Christi", "Soul of Christ, sanctify me",
            "Ánima Christi, sanctífica me. Corpus Christi, salva me. Sanguis Christi, inébria me. Aqua láteris Christi, lava me. Pássio Christi, confórta me. O bone Jesu, exáudi me. Intra tua vúlnera abscónde me. Ne permíttas me separári a te. Ab hoste malígno defénde me. In hora mortis meæ voca me. Et jube me veníre ad te, ut cum Sanctis tuis laudem te in sǽcula sæculórum. Amen.",
            "Soul of Christ, sanctify me. Body of Christ, save me. Blood of Christ, inebriate me. Water from the side of Christ, wash me. Passion of Christ, strengthen me. O good Jesus, hear me. Within Thy wounds hide me. Suffer me not to be separated from Thee. From the malignant enemy defend me. In the hour of my death call me, and bid me come to Thee, that with Thy saints I may praise Thee for ever and ever. Amen."
        ),
        Prayer(
            "flos_carmeli", "Flos Carmeli", "Sequence of Our Lady of Mount Carmel",
            "Flos Carmeli,\nvitis florigera,\nsplendor cæli,\nVirgo puerpera\nsingularis.\n\nMater mitis,\nsed viri nescia,\nCarmelitis\nda privilegia,\nStella maris.\n\nRadix Jesse\ngerminans flosculum,\nnos ad esse\ntecum in sæculum\npatiaris.\n\nInter spinas\nquæ crescis lilium,\nserva puras\nmentes fragilium\ntutelaris.\n\nArmatura\nfortis pugnantium,\nfurunt bella,\ntende præsidium\nscapularis.\n\nPer incerta\nprudens consilium,\nper adversa\njuge solatium\nlargiaris.\n\nMater dulcis,\nCarmeli Domina,\nplebem tuam\nreple lætitia\nqua bearis.\n\nParadisi\nclavis et janua,\nfac nos duci\nquo, Mater, gloria\ncoronaris.\nAmen. Alleluia.",
            "Flower of Carmel, blossoming vine, splendour of heaven, Virgin who alone didst bear a Child.\n\nGentle Mother, who knew not man, grant privileges to the children of Carmel, Star of the Sea.\n\nRoot of Jesse, bringing forth the little Flower, grant that we may be with thee for ever.\n\nLily growing among thorns, keep pure the minds of the frail, our protectress.\n\nStrong armour of those who fight: wars rage; hold out the defence of the Scapular.\n\nIn uncertainty, prudent counsel; in adversity, unfailing comfort: bestow them upon us.\n\nSweet Mother, Lady of Carmel, fill thy people with the gladness that makes thee blessed.\n\nKey and gate of Paradise, bring us to where thou, Mother, art crowned with glory. Amen. Alleluia.\n\nSequence of the Carmelite Rite, attributed to St. Simon Stock. English: a literal rendering."
        ),
        Prayer(
            "st_joseph", "Ad te, beate Joseph", "Prayer to St. Joseph (Leo XIII)",
            null,
            "To thee, O blessed Joseph, do we have recourse in our tribulation, and having implored the help of thy thrice-holy Spouse, we confidently invoke thy patronage also. Through that charity which bound thee to the Immaculate Virgin Mother of God, and through the paternal love with which thou didst embrace the Child Jesus, we humbly beseech thee graciously to regard the inheritance which Jesus Christ hath purchased by His Blood, and with thy power and strength to aid us in our necessities.\n\nO most watchful Guardian of the Holy Family, defend the chosen children of Jesus Christ; O most loving father, ward off from us every contagion of error and corrupting influence; O our most mighty protector, be propitious to us and from heaven assist us in our struggle with the power of darkness; and as once thou didst rescue the Child Jesus from deadly peril, so now protect God’s Holy Church from the snares of the enemy and from all adversity. Shield, too, each one of us by thy constant protection, so that, supported by thy example and thy aid, we may be able to live piously, to die holily, and to obtain eternal happiness in heaven. Amen."
        ),
        Prayer(
            "spiritual_communion", "Spiritual Communion", "St. Alphonsus Liguori",
            null,
            "My Jesus, I believe that Thou art present in the Most Holy Sacrament. I love Thee above all things, and I desire to receive Thee into my soul. Since I cannot at this moment receive Thee sacramentally, come at least spiritually into my heart. I embrace Thee as if Thou wert already there, and unite myself wholly to Thee. Never permit me to be separated from Thee. Amen."
        ),
        Prayer(
            "st_michael", "St. Michael the Archangel", "Sancte Michael Archangele",
            "Sancte Míchael Archángele, defénde nos in prǽlio; contra nequítiam et insídias diáboli esto præsídium. Ímperet illi Deus, súpplices deprecámur: tuque, Princeps milítiæ cæléstis, sátanam aliósque spíritus malígnos, qui ad perditiónem animárum pervagántur in mundo, divína virtúte in inférnum detrúde. Amen.",
            "Saint Michael the Archangel, defend us in battle; be our protection against the wickedness and snares of the devil. May God rebuke him, we humbly pray; and do thou, O Prince of the heavenly host, by the power of God, thrust into hell Satan and all the evil spirits who prowl about the world seeking the ruin of souls. Amen."
        ),
        Prayer(
            "de_profundis", "De Profundis", "Psalm 129, for the faithful departed",
            "De profúndis clamávi ad te, Dómine: Dómine, exáudi vocem meam. Fiant aures tuæ intendéntes in vocem deprecatiónis meæ. Si iniquitátes observáveris, Dómine: Dómine, quis sustinébit? Quia apud te propitiátio est: et propter legem tuam sustínui te, Dómine. Sustínuit ánima mea in verbo ejus: sperávit ánima mea in Dómino. A custódia matutína usque ad noctem: speret Israël in Dómino. Quia apud Dóminum misericórdia: et copiósa apud eum redémptio. Et ipse rédimet Israël ex ómnibus iniquitátibus ejus.\n\n℣. Réquiem ætérnam dona eis, Dómine.\n℟. Et lux perpétua lúceat eis.",
            "Out of the depths I have cried to thee, O Lord: Lord, hear my voice. Let thy ears be attentive to the voice of my supplication. If thou, O Lord, wilt mark iniquities: Lord, who shall stand it? For with thee there is merciful forgiveness: and by reason of thy law, I have waited for thee, O Lord. My soul hath relied on his word: my soul hath hoped in the Lord. From the morning watch even until night, let Israel hope in the Lord. Because with the Lord there is mercy: and with him plentiful redemption. And he shall redeem Israel from all his iniquities.\n\n℣. Eternal rest grant unto them, O Lord.\n℟. And let perpetual light shine upon them."
        ),
        Prayer(
            "examen", "Examination of Conscience", "Before the Act of Contrition",
            null,
            "1. Place yourself in the presence of God.\n2. Give thanks for the benefits of this day.\n3. Ask light to know your faults.\n4. Review the day: thoughts, words, deeds and omissions.\n5. Ask pardon, and make an Act of Contrition.\n6. Resolve to amend, with the help of God’s grace."
        ),
        Prayer(
            "rosary", "Holy Rosary", "The fifteen mysteries",
            null,
            "Joyful (Monday & Thursday): the Annunciation; the Visitation; the Nativity; the Presentation; the Finding in the Temple.\n\n" +
                "Sorrowful (Tuesday & Friday): the Agony in the Garden; the Scourging at the Pillar; the Crowning with Thorns; the Carrying of the Cross; the Crucifixion.\n\n" +
                "Glorious (Wednesday, Saturday & Sunday): the Resurrection; the Ascension; the Descent of the Holy Ghost; the Assumption; the Coronation of Our Lady.\n\nHow to pray it\n1. Make the Sign of the Cross and say the Apostles’ Creed.\n2. Say one Our Father, three Hail Marys and one Glory Be.\n3. For each decade, name the mystery and meditate on it while saying one Our Father, ten Hail Marys and one Glory Be.\n4. After the fifth decade, say the Hail, Holy Queen.\n\n℣. Pray for us, O holy Mother of God.\n℟. That we may be made worthy of the promises of Christ.\n\nLet us pray.\nO God, whose only begotten Son, by his life, death, and resurrection, has purchased for us the rewards of eternal life, grant we beseech thee, that meditating upon those mysteries, in the most holy Rosary of the most blessed Virgin Mary, we may imitate what they contain, and obtain what they promise: through the same Christ our Lord. Amen."
        ),
    )

    val every: List<Prayer> = all + MorePrayers.prayers
    private val byId = every.associateBy { it.id }
    fun get(id: String?): Prayer? = id?.let { byId[it] }

    /** Prayers offered as quick additions on the "My Prayers" screen. */
    val library: List<String> = listOf(
        "rosary", "angelus", "memorare", "anima_christi", "salve_regina", "sub_tuum",
        "flos_carmeli", "st_michael", "spiritual_communion", "contrition", "de_profundis",
        "litany_loreto", "litany_saints", "veni_creator", "scapular", "creed", "acts",
        "litany_holy_name", "litany_sacred_heart", "litany_holy_ghost",
        "therese_morning", "nada_te_turbe", "montfort_consecration", "o_jesu_vivens",
    )
}
