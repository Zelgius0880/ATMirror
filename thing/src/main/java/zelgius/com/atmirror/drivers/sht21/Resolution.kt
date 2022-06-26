package zelgius.com.atmirror.drivers.sht21

import kotlin.experimental.and

/**
 * The Sensor resolution for temperature and relative humidity.
 */
enum class Resolution
/**
 * Create a new `Resolution`.
 *
 * @param resolutionByte The byte-encoded resolution.
 */
constructor(
    /**
     * The byte value representing the resolution.
     */
    /**
     * Return the byte-encoded resolution.
     *
     * @return byte-encoded resolution.
     */
    val resolutionByte: Byte
) {

    /**
     * Resolution:
     *
     *  * Relative humidity: 12 bit
     *  * Temperature: 14 bit
     *
     */
    RES_12_14BIT(0x00.toByte()),
    /**
     * Resolution:
     *
     *  * Relative humidity: 8 bit
     *  * Temperature: 12 bit
     *
     */
    RES_8_12BIT(0x01.toByte()),
    /**
     * Resolution:
     *
     *  * Relative humidity: 10 bit
     *  * Temperature: 13 bit
     *
     */
    RES_10_13BIT(0x80.toByte()),
    /**
     * Resolution:
     *
     *  * Relative humidity: 11 bit
     *  * Temperature: 11 bit
     *
     */
    RES_11_11BIT(0x81.toByte());

    override fun toString(): String {
        return ("Resolution{"
                + this.name +
                '}'.toString())
    }

    companion object {
        /**
         * The mask for accessing the resolution bits (bit 7 and 0) in user register.
         */
        private val MASK = 0x81.toByte()

        /**
         * Return the [Resolution] for a given byte value.
         *
         * @param resolution The byte value to analyze.
         * @return The matching [Resolution].
         */
        fun getResolution(resolution: Byte): Resolution {
            val maskedValue = (MASK and resolution)
            return if (maskedValue.toInt() == 0x00) {
                RES_12_14BIT
            } else if (maskedValue.toInt() == 0x01) {
                RES_8_12BIT
            } else if (maskedValue == 0x80.toByte()) {
                RES_10_13BIT
            } else {
                RES_11_11BIT
            }
        }
    }
}
