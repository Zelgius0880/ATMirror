package zelgius.com.atmirror.shared.repository

import androidx.paging.ItemKeyedDataSource
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.TestOnly
import zelgius.com.atmirror.shared.entity.Group
import zelgius.com.atmirror.shared.entity.GroupItem
import zelgius.com.atmirror.shared.entity.Light
import zelgius.com.atmirror.shared.entity.Switch

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
                    g.items = getItems(g)
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
            GroupItem.FIREBASE_PATH,
            Switch::class.java,
            "name"
        ).apply {
            forEach {
                it.group = g
            }
        }

    suspend fun getItems(g: Group) =
        getSubListSnapshot(g, GroupItem.FIREBASE_PATH, "itemType", "name")
            .map {
                FirestoreGroupItemMapper.map(it).apply {
                    when(this) {
                        is Switch -> group = g
                        is Light -> group = g
                    }
                }
            }


    /**
     * Should be only called from the [DataSource]
     * @param group Group?
     * @param size Int
     * @param callback LoadCallback<Group>
     */
    suspend fun getPagedGroupFlatted(
        group: Group?,
        size: Int,
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
                g.items = getItems(g).apply {
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
                    it.items = getItems(it)
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

    suspend fun delete(item: GroupItem) {
        delete(item, item.firebasePath)
    }

    suspend fun createOrUpdate(item: GroupItem, checkUnique: Boolean = false) {
        createOrUpdate(item, item.firebasePath, if(checkUnique) "uid" to item.uid else null)
    }
    fun getFlattedGroupDataSource() = FlattedGroupDataSourceFactory()

    fun getItemsQuery(group: Group): Query =
        db.collection(Group.FIREBASE_PATH).document(group.key!!).collection(GroupItem.FIREBASE_PATH)
            .orderBy("itemType")
            .orderBy("name")

}