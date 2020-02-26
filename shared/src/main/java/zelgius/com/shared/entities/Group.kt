package zelgius.com.shared.entities

import zelgius.com.shared.entities.Switch

data class Group (
    val name: String,
    val switches: List<Switch>,
    val lamps: List<Lamp>

)