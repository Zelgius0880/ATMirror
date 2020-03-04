package zelgius.com.atmirror.shared.repository

import androidx.paging.ItemKeyedDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.TestOnly
import zelgius.com.atmirror.shared.entity.Group
import zelgius.com.atmirror.shared.entity.Light
import zelgius.com.atmirror.shared.entity.Switch
import zelgius.com.lights.repository.ILight

class GroupRepository : FirebaseRepository() {

    /**
     * Should be only called from the [DataSource]
     * @param group Group?
     * @param size Int
     * @param callback LoadCallback<Group>
     */
    suspend fun getPagedGroup(
        group: Group?, size: Int,
        callback: ItemKeyedDataSource.LoadCallback<Group>
    ) {
        withContext(Dispatchers.Default) {
            val list = if (group == null) {
                getPaged(Group.FIREBASE_PATH, size.toLong(), Group::class.java, "name")
            } else {
                getPaged(
                    getSnapshot(group.key!!, Group.FIREBASE_PATH),
                    Group.FIREBASE_PATH, size.toLong(), Group::class.java, "name"
                )
            }.apply {
                forEach { g ->
                    g.switches = getSwitches(g)

                    g.lights = getLights(g)
                }
            }

            if (list.isEmpty()) {
                return@withContext
            }

            if (callback is ItemKeyedDataSource.LoadInitialCallback) { //initial load
                callback.onResult(list, 0, list.size)
            } else { //next pages load
                callback.onResult(list)
            }
        }
    }

    private suspend fun getSwitches(g: Group) =
        getSublist(
            g,
            Switch.FIREBASE_PATH,
            Switch::class.java,
            "name"
        ).apply {
            forEach {
                it.group = g
            }
        }

    private suspend fun getLights(g: Group) =
        getSublist(
            g,
            Light.FIREBASE_PATH,
            Light::class.java,
            "name"
        ).apply {
            forEach {
                it.group = g
            }
        }


    /**
     * Should be only called from the [DataSource]
     * @param group Group?
     * @param size Int
     * @param callback LoadCallback<Group>
     */
    suspend fun getPagedGroupFlatted(
        group: Group?, size: Int,
        callback: ItemKeyedDataSource.LoadCallback<Any>
    ) {
        withContext(Dispatchers.Default) {
            val list = if (group == null) {
                getPaged(Group().firebasePath, size.toLong(), Group::class.java, "name")
            } else {
                with(getSnapshot(group)) {
                    getPaged(this, group.firebasePath, size.toLong(), Group::class.java, "name")
                }
            }

            if (list.isEmpty()) {
                return@withContext
            }

            val result = mutableListOf<Any>()
            list.forEach { g ->
                result.add(g)
                g.switches = getSwitches(g).apply {
                    forEach {
                        result.add(it)
                    }
                }

                g.lights = getLights(g).apply {
                    forEach {
                        result.add(it)
                    }
                }
            }

            if (callback is ItemKeyedDataSource.LoadInitialCallback) { //initial load
                callback.onResult(result, 0, result.size)
            } else { //next pages load
                callback.onResult(result)
            }
        }
    }

    suspend fun getGroup(key: String) =
        withContext(Dispatchers.Default) {
            getSnapshot(key, "groups").run {
                toObject(Group::class.java)?.also {
                    it.key = id
                    it.lights = getLights(it)

                    it.switches = getSwitches(it)
                }
            }
        }

    suspend fun createOrUpdate(
        group: Group,
        switches: List<Switch>? = null,
        lights: List<Light>? = null
    ) =
        withContext(Dispatchers.Default) {
            createOrUpdate(group, Group.FIREBASE_PATH)

            if (switches == null) {
                group.switches.forEach {
                    it.group = group
                    createOrUpdate(it, it.firebasePath)
                }
            } else {
                switches.forEach {
                    it.group = group
                    createOrUpdate(it, it.firebasePath)
                }

                group.switches.filterNot { switches.contains(it) }
                    .forEach {
                        it.group = group
                        if (it.key != null)
                            delete(it, it.firebasePath)
                    }
            }

            if (lights == null) {
                group.lights.forEach {
                    it.group = group
                    createOrUpdate(it, it.firebasePath)
                }
            } else {
                lights.forEach {
                    it.group = group
                    createOrUpdate(it, it.firebasePath)
                }

                group.lights.filterNot { lights.contains(it) }
                    .forEach {
                        it.group = group

                        if (it.key != null)
                            delete(it, it.firebasePath)
                    }
            }
        }


    @TestOnly // Deleting from android is not recommended
    suspend fun delete(group: Group) =
        withContext(Dispatchers.Default) {
            group.lights.forEach {
                it.group = group
                delete(it, it.firebasePath)
            }

            group.switches.forEach {
                it.group = group
                delete(it, it.firebasePath)
            }

            delete(group, Group.FIREBASE_PATH)
        }

    fun getGroupDataSource() = GroupDataSourceFactory()
    fun getFlattedGroupDataSource() = FlattedGroupDataSourceFactory()
}