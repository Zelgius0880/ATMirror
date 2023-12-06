package zelgius.com.atmirror.things.drivers.sht21

import kotlin.experimental.and

/**
 *
 * The end of battery alert is activated when the battery
 * power falls below 2.25V.
 * Value of the status bit:
 *
 *  * 0: VDD greater than 2.25 Volt
 *  * 1: VDD less than 2.25 Volt
 *
 * @author Stefan Freitag
 */
enum class EndOfBatteryAlert
/**
 * Create a new `EndOfBatteryAlert`.
 *
 * @param eobByte The byte-encoded `EndOfBatteryAlert`.
 */
constructor(
    /**
     * Return the byte-encoded alert status.
     *
     * @return byte-encoded alert status.
     */
    val byte: Byte
) {
    /**
     * Set End of battery alert.
     */
    EOB_ALERT_ON(0x40.toByte()),
    /**
     * Cleared End of battery alert.
     */
    EOB_ALERT_OFF(0x00.toByte());

    override fun toString(): String {
        return this.name
    }

    companion object {
        /**
         * Mask for accessing the end of battery alert-bit (bit 6) in user register.
         */
        private val EOB_ALERT_MASK: Byte = 0x40

        /**
         * Returns the EndOfBattery status (alert on/off) for the given
         * `eobByte`.
         *
         * @param eobByte Byte to evaluate.
         * @return EndOfBattery status (alert on/off) for the given
         * `eobByte`.
         */
        fun getEOBAlert(eobByte: Byte): EndOfBatteryAlert {
            return if (EOB_ALERT_OFF.byte == EOB_ALERT_MASK and eobByte) {
                EOB_ALERT_OFF
            } else EOB_ALERT_ON

        }
    }
}

