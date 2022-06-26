package zelgius.com.atmirror.drivers.sht21

import kotlin.experimental.and


/**
 * The heater is intended to be used for functionality diagnosis – relative humidity drops upon rising
 * temperature. The heater consumes about 5.5mW and provides a temperature increase of about 0.5 – 1.5°C.
 */
enum class HeaterStatus
/**
 * Create a new `HeaterStatus`.
 *
 * @param heaterByte The byte-encoded heater status
 */
constructor(val byte: Byte) {
    /**
     * Enabled heater.
     */
    HEATER_ON(0x04.toByte()),
    /**
     * Disabled heater.
     */
    HEATER_OFF(0x00.toByte());

    override fun toString(): String {
        return this.name
    }

    companion object {
        /**
         * Mask for accessing the heater bit (bit 2) in the user register.
         */
        private val MASK: Byte = 0x04

        fun getStatus(heaterByte: Byte): HeaterStatus {
            return if (heaterByte and MASK == HEATER_OFF.byte) {
                HEATER_OFF
            } else HEATER_ON
        }
    }

}