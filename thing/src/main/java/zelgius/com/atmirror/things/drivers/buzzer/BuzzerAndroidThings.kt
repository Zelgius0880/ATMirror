package zelgius.com.atmirror.things.drivers.buzzer

import com.google.android.things.pio.Pwm
import com.test.buzzer.driver.Note
import kotlinx.coroutines.delay

class BuzzerAndroidThings(private val pwm: Pwm) : Buzzer {
    override fun buzz(note: Note, divider: Float) {
        if(note == Note.NONE) pwm.setPwmDutyCycle(0.0)
        else {
            pwm.setPwmDutyCycle(90.0)
            pwm.setPwmFrequencyHz(note.frequency.toDouble() / divider)
        }
    }

    override suspend fun buzz(note: Note, duration: Int, divider: Float) {
        buzz(note, divider)
        delay(duration.toLong())
    }

    init {
        pwm.setPwmDutyCycle(50.0)
        pwm.setPwmFrequencyHz(01.0)
        pwm.setEnabled(true)
    }
}