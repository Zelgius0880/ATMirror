package com.test.buzzer

import com.test.buzzer.driver.Note
import kotlin.math.roundToInt

object Zelda {
    var melody = arrayOf(
        Note.AS4,
        Note.F4,
        Note.F4,
        Note.AS4,//1
        Note.GS4,
        Note.FS4,
        Note.GS4,
        Note.AS4,
        Note.FS4,
        Note.FS4,
        Note.AS4,
        Note.A4,
        Note.G4,
        Note.A4,
        Note.NONE,
        Note.AS4,
        Note.F4,
        Note.AS4,
        Note.AS4,
        Note.C5,
        Note.D5,
        Note.DS5,//7
        Note.F5,
        Note.F5,
        Note.F5,
        Note.F5,
        Note.FS5,
        Note.GS5,
        Note.AS5,
        Note.AS5,
        Note.AS5,
        Note.GS5,
        Note.FS5,
        Note.GS5,
        Note.FS5,
        Note.F5,
        Note.F5,
        Note.DS5,
        Note.F5,
        Note.FS5,
        Note.F5,
        Note.DS5, //11
        Note.CS5,
        Note.DS5,
        Note.F5,
        Note.DS5,
        Note.CS5,
        Note.C5,
        Note.D5,
        Note.E5, 
        Note.G5,
        Note.F5,
        Note.F4,
        Note.F4,
        Note.F4,
        Note.F4,
        Note.F4,
        Note.F4,
        Note.F4,
        Note.F4,
        Note.F4,
        Note.F4,
        Note.AS4,
        Note.F4,
        Note.AS4,
        Note.AS4,
        Note.C5,
        Note.D5,
        Note.DS5,//15
        Note.F5,
        Note.F5,
        Note.F5,
        Note.F5,
        Note.FS5,
        Note.GS5,
        Note.AS5,
        Note.CS6,
        Note.C6,
        Note.A5,
        Note.F5,
        Note.FS5,
        Note.AS5,
        Note.A5,
        Note.F5, 
        Note.F5,
        Note.FS5,
        Note.AS5,
        Note.A5,
        Note.F5, 
        Note.D5,
        Note.DS5,
        Note.FS5,
        Note.F5,
        Note.CS5,
        Note.AS4,
        Note.C5,
        Note.D5,
        Note.E5,
        Note.G5,
        Note.F5,
        Note.F4,
        Note.F4,
        Note.F4,
        Note.F4,
        Note.F4,
        Note.F4,
        Note.F4,
        Note.F4,
        Note.F4,
        Note.F4   ) to intArrayOf(
        -2,
        8,
        8,
        8,
        16,
        16,
        -2
                -2,
        8,
        8,
        8,
        16,
        16,
        -2,
        1,
        4,
        -4,
        8,
        16,
        16,
        16,
        16,
        2,
        8,
        8,
        8,
        16,
        16,
        -2,
        8,
        8,
        8,
        16,
        -8,
        16,
        2,
        4,
        -8,
        16,
        2,
        8,
        8,
        -8,
        16,
        2,
        8,
        8,
        -8,
        16,
        2,
        8,
        16,
        16,
        16,
        16,
        16,
        16,
        16,
        16,
        8,
        16,
        8,
        4,
        -4,
        8,
        16,
        16,
        16,
        16,
        2,
        8,
        8,
        8,
        16,
        16,
        -2,
        4,
        4,
        2,
        4,
        -2,
        4,
        4,
        2,
        4,
        -2,
        4,
        4,
        2,
        4,
        -2,
        4,
        4,
        2,
        4,
        -8,
        16,
        2,
        8,
        16,
        16,
        16,
        16,
        16,
        16,
        16,
        16,
        8,
        16,
        8
    )

    var tempo = intArrayOf(
        3, 3, 3, 3,
        3, 3, 3, 3,
        3, 3, 3, 3,
        3, 3, 3, 3,
        3, 3, 3, 3,
        3, 3, 3, 3,
        3, 3, 3, 3,
        3, 3, 3, 3
    )

    val songOfStorm = arrayOf(
        Note.D4, Note.F4, Note.D5, Note.D4,
        Note.F4, Note.D5, Note.E5, Note.F5,
        Note.E5, Note.F5, Note.E5, Note.C5,
        Note.A4, Note.A4, Note.D4, Note.F4,
        Note.G4, Note.A4, Note.A4, Note.D4,
        Note.F4, Note.G4
    ) to longArrayOf(
        250, 125, 600, 250,
        125, 600, 500, 125,
        125, 125, 125, 125,
        625, 250, 250, 125,
        125, 625, 250, 375,
        125, 250
    ).map { (1000f / it).roundToInt() }.toIntArray()
}

