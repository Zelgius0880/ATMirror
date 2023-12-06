package zelgius.com.atmirror.things.drivers.sht21

/**
 *
 * Sensor commands, taken from data sheet, table 6.
 *
 * There are two different operation modes to communicate
 * with the sensor: Hold Master mode or No Hold Master
 * mode. In the first case the SCL line is blocked (controlled
 * by sensor) during measurement process while in the latter
 * case the SCL line remains open for other communication
 * while the sensor is processing the measurement. No hold
 * master mode allows for processing other I2C
 * communication tasks on a bus while the sensor is
 * measuring.
 * @author Stefan Freitag
 */
enum class Command
/**
 * Create a new [Command].
 * @param commandByte Command encoded as byte.
 */
constructor(
    /**
     * Return the command byte.
     *
     * @return The command byte.
     */
    val commandByte: Byte
) {
    /**
     * Trigger temperature measurement hold master. Byte code 1110’0011.
     */
    TRIG_T_MEASUREMENT_HM(0xe3.toByte()),
    /**
     * Trigger humidity measurement hold master. Byte code 1110’0101.
     */
    TRIG_RH_MEASUREMENT_HM(0xe5.toByte()),
    /**
     * Trigger temperature measurement no hold master. Byte code 1111’0011.
     */
    TRIG_T_MEASUREMENT_POLL(0xf3.toByte()),
    /**
     * Trigger humidity measurement no hold master. Byte code 1111’0101.
     */
    TRIG_RH_MEASUREMENT_POLL(0xf5.toByte()),
    /**
     * Writing user register. Byte code 1110’0110.
     */
    USER_REG_W(0xe6.toByte()),
    /**
     * Reading user register. Byte code 1110’0111.
     */
    USER_REG_R(0xe7.toByte()),
    /**
     * Soft reset. Byte code 1111’1110.
     * This command is used for rebooting the sensor system without switching the power off and on
     * again. Upon reception of this command, the sensor system reinitialises and starts operation according to the
     * default settings – with the exception of the heater bit in the user register. The soft reset takes less than
     * 15ms.
     */
    SOFT_RESET(0xfe.toByte())
}
