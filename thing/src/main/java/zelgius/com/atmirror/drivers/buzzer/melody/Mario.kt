package com.test.buzzer

import com.test.buzzer.driver.Note
import kotlin.math.roundToInt

object Mario {

    var short = arrayOf(
        Note.E7, Note.E7, Note.NONE, Note.E7,
        Note.NONE, Note.C7, Note.E7, Note.NONE
    ) to intArrayOf(
        12, 12, 12, 12,
        12, 12, 12, 12
    )

    //Mario main theme melody
    var melody = arrayOf(
        Note.E7, Note.E7, Note.NONE, Note.E7,
        Note.NONE, Note.C7, Note.E7, Note.NONE,
        Note.G7, Note.NONE, Note.NONE, Note.NONE,
        Note.G6, Note.NONE, Note.NONE, Note.NONE,
        Note.C7, Note.NONE, Note.NONE, Note.G6,
        Note.NONE, Note.NONE, Note.E6, Note.NONE,
        Note.NONE, Note.A6, Note.NONE, Note.B6,
        Note.NONE, Note.AS6, Note.A6, Note.NONE,
        Note.G6, Note.E7, Note.G7,
        Note.A7, Note.NONE, Note.F7, Note.G7,
        Note.NONE, Note.E7, Note.NONE, Note.C7,
        Note.D7, Note.B6, Note.NONE, Note.NONE,
        Note.C7, Note.NONE, Note.NONE, Note.G6,
        Note.NONE, Note.NONE, Note.E6, Note.NONE,
        Note.NONE, Note.A6, Note.NONE, Note.B6,
        Note.NONE, Note.AS6, Note.A6, Note.NONE,
        Note.G6, Note.E7, Note.G7,
        Note.A7, Note.NONE, Note.F7, Note.G7,
        Note.NONE, Note.E7, Note.NONE, Note.C7,
        Note.D7, Note.B6, Note.NONE, Note.NONE
    ) to intArrayOf(
        12, 12, 12, 12,
        12, 12, 12, 12,
        12, 12, 12, 12,
        12, 12, 12, 12,
        12, 12, 12, 12,
        12, 12, 12, 12,
        12, 12, 12, 12,
        12, 12, 12, 12,
        9, 9, 9,
        12, 12, 12, 12,
        12, 12, 12, 12,
        12, 12, 12, 12,
        12, 12, 12, 12,
        12, 12, 12, 12,
        12, 12, 12, 12,
        12, 12, 12, 12,
        9, 9, 9,
        12, 12, 12, 12,
        12, 12, 12, 12,
        12, 12, 12, 12
    )
 //Mario main theme melody
    var melodyShort = arrayOf(
        Note.E7, Note.E7, Note.NONE, Note.E7,
        Note.NONE, Note.C7, Note.E7, Note.NONE,
        Note.G7, Note.NONE, Note.NONE, Note.NONE,
        Note.G6, Note.NONE, Note.NONE, Note.NONE,
        /*Note.C7, Note.NONE, Note.NONE, Note.G6,*/
        /*Note.NONE, Note.NONE, Note.E6, Note.NONE,
        Note.NONE, Note.A6, Note.NONE, Note.B6,
        Note.NONE, Note.AS6, Note.A6, Note.NONE,
        Note.G6, Note.E7, Note.G7,*/
        /*Note.A7, Note.NONE, Note.F7, Note.G7,
        Note.NONE, Note.E7, Note.NONE, Note.C7,
        Note.D7, Note.B6, Note.NONE, Note.NONE,
        Note.C7, Note.NONE, Note.NONE, Note.G6,
        Note.NONE, Note.NONE, Note.E6, Note.NONE,
        Note.NONE, Note.A6, Note.NONE, Note.B6,
        Note.NONE, Note.AS6, Note.A6, Note.NONE,
        Note.G6, Note.E7, Note.G7,
        Note.A7, Note.NONE, Note.F7, Note.G7,
        Note.NONE, Note.E7, Note.NONE, Note.C7,
        Note.D7, Note.B6, Note.NONE, Note.NONE*/
    ) to intArrayOf(
        12, 12, 12, 12,
        12, 12, 12, 12,
        12, 12, 12, 12,
        12, 12, 12, 12,
        /*12, 12, 12, 12,*/
        /*12, 12, 12, 12,
        12, 12, 12, 12,
        12, 12, 12, 12,
        9, 9, 9,*/
        /*12, 12, 12, 12,
        12, 12, 12, 12,
        12, 12, 12, 12,
        12, 12, 12, 12,
        12, 12, 12, 12,
        12, 12, 12, 12,
        12, 12, 12, 12,
        9, 9, 9,
        12, 12, 12, 12,
        12, 12, 12, 12,
        12, 12, 12, 12*/
    )

    //Underworld melody
    var underworld = arrayOf(
        Note.C4, Note.C5, Note.A3, Note.A4,
        Note.AS3, Note.AS4, Note.NONE,
        Note.NONE,
        Note.C4, Note.C5, Note.A3, Note.A4,
        Note.AS3, Note.AS4, Note.NONE,
        Note.NONE,
        Note.F3, Note.F4, Note.D3, Note.D4,
        Note.DS3, Note.DS4, Note.NONE,
        Note.NONE,
        Note.F3, Note.F4, Note.D3, Note.D4,
        Note.DS3, Note.DS4, Note.NONE,
        Note.NONE, Note.DS4, Note.CS4, Note.D4,
        Note.CS4, Note.DS4,
        Note.DS4, Note.GS3,
        Note.G3, Note.CS4,
        Note.C4, Note.FS4, Note.F4, Note.E3, Note.AS4, Note.A4,
        Note.GS4, Note.DS4, Note.B3,
        Note.AS3, Note.A3, Note.GS3,
        Note.NONE, Note.NONE, Note.NONE
    ) to intArrayOf(
        12, 12, 12, 12,
        12, 12, 6,
        3,
        12, 12, 12, 12,
        12, 12, 6,
        3,
        12, 12, 12, 12,
        12, 12, 6,
        3,
        12, 12, 12, 12,
        12, 12, 6,
        6, 18, 18, 18,
        6, 6,
        6, 6,
        6, 6,
        18, 18, 18, 18, 18, 18,
        10, 10, 10,
        10, 10, 10,
        3, 3, 3
    )
    val coin = arrayOf(Note.B5, Note.E6) to arrayOf(100, 200).map { (1000f / it).roundToInt() }.toIntArray()
    val fireBall = arrayOf(Note.G4, Note.G5, Note.G6) to arrayOf(35, 35, 35).map { (1000f / it).roundToInt() }.toIntArray()
}
