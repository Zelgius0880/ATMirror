package zelgius.com.networking.entity

import zelgius.com.lights.repository.Light


data class Group (
    val name: String,
    val switches: List<Switch>,
    val lamps: List<Light>

)