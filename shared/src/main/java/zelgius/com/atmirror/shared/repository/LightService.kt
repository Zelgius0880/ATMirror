package zelgius.com.atmirror.shared.repository

import zelgius.com.atmirror.shared.entity.Light

interface LightService {

    suspend fun setLightState(light: Light, state: Light.State, name: String? = null): Boolean
    suspend fun getLightList(name: String? = null): List<Light>?
}