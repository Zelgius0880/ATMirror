package zelgius.com.atmirror.shared.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.isActive
import zelgius.com.atmirror.shared.entity.FirebaseObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


open class FirebaseRepository {
    protected val db = FirebaseFirestore.getInstance()

    init {
    }

    /**
     * Get a list of object (type defined with [objClass]) from the targeted collection started at [snapshot] and sized with [size]. The collection is defined with [path] and should correspond to a Firestore collection path
     * The collections are ordered by [order]. If null, no ordering is applied
     * @param snapshot DocumentSnapshot
     * @param path String
     * @param size Long
     * @param objClass Class<T>
     * @return List<T>
     */
    suspend fun <T> getPaged(
        snapshot: DocumentSnapshot,
        path: String,
        size: Long,
        objClass: Class<T>,
        order: String? = null
    ) =
        suspendCoroutine<List<T>> { continuation ->
            db.collection(path)
                .let {
                    if (order != null) {
                        it.orderBy(order)
                    } else it
                }
                .startAfter(snapshot)
                .limit(size)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        continuation.resumeWithException(e)
                    }

                    if (snapshots != null) {
                        val list: MutableList<T> = ArrayList()
                        for (doc in snapshots.documents) {
                            doc.toObject(objClass)?.apply {
                                list.add(this)
                            }
                        }

                        if(continuation.context.isActive)
                            continuation.resume(list)
                    }
                }
        }

    /**
     * See [getPaged]
     * Call for initial -> nothing paged yet
     * @param path String
     * @param size Long
     * @param objClass Class<T>
     * @return List<T>
     */
    protected suspend fun <T : FirebaseObject> getPaged(
        path: String,
        size: Long,
        objClass: Class<T>,
        order: String? = null
    ) =
        suspendCoroutine<List<T>> { continuation ->
            db.collection(path)
                .limit(size)
                .let {
                    if (order != null) {
                        it.orderBy(order)
                    } else it
                }
                .get()
                .addOnSuccessListener {
                    /* val list: MutableList<T> = //snapshots.toObjects(objClass)
                     ArrayList()
                     for (doc in snapshots.documents) {
                         doc.toObject(objClass)?.apply {
                             list.add(this)
                         }
                     }

                     continuation.resume(list)*/
                    continuation.resume(it.toObjects(objClass))
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }


    protected suspend fun getSnapshot(firebaseObject: FirebaseObject): DocumentSnapshot =
        suspendCoroutine { continuation ->
            db.collection(firebaseObject.firebasePath)
                .document(firebaseObject.key!!)
                .get()
                .addOnSuccessListener {
                    continuation.resume(it)
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }

    /**
     * Retrieve a [QuerySnapshot] from [parent] parent. [path] is the full path of the collection -> {parent.path}/{collection}
     * @param parent FirebaseObject
     * @param path String
     * @return QuerySnapshot
     */
    protected suspend fun getSubListSnapshot(
        parent: FirebaseObject,
        path: String,
        vararg order: String
    ): QuerySnapshot =
        suspendCoroutine { continuation ->
            db.collection(parent.firebasePath)
                .document(parent.key!!)
                .collection(path.replace("${parent.firebasePath}/", ""))
                .run {
                    if(order.isNotEmpty()) {
                        var query: Query? = null
                        order.forEach {
                            query = if(query == null) orderBy(it)
                            else query!!.orderBy(it)
                        }
                        query!!
                    } else
                        this
                }
                .get()
                .addOnSuccessListener {
                    continuation.resume(it)
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }

    /**
     * Retrieve a [QuerySnapshot] from [parent] parent. [path] is the full path of the collection -> {parent.path}/{collection}
     * @param parent FirebaseObject
     * @param path String
     * @return QuerySnapshot
     */
    protected suspend fun <T : FirebaseObject> getSublist(
        parent: FirebaseObject,
        path: String,
        clazz: Class<T>,
        vararg order: String
    ): List<T> =
        mutableListOf<T>().apply {
            getSubListSnapshot(parent, path, *order).forEach { document ->
                document.toObject(clazz).let {
                    it.key = document.id
                    add(it)
                }
            }
        }

    protected suspend fun getSnapshot(key: String, path: String): DocumentSnapshot =
        suspendCoroutine { continuation ->
            db.collection(path)
                .document(key)
                .get()
                .addOnSuccessListener {
                    continuation.resume(it)
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }


    protected suspend fun <T : FirebaseObject> createOrUpdate(item: T, path: String) =
        suspendCoroutine<T> { continuation ->
            db.collection(path)
                .run {
                    if (item.key != null) {
                        document(item.key!!)
                    } else {
                        document()
                    }
                }.apply {
                    set(item)
                        .addOnSuccessListener {
                            item.key = id
                            continuation.resume(item)
                        }
                        .addOnFailureListener {
                            continuation.resumeWithException(it)
                        }
                }
        }


    protected suspend fun <T : FirebaseObject> delete(item: T, path: String) =
        suspendCoroutine<T> { continuation ->
            db.collection(path)
                .document(item.key!!)
                .delete()
                .addOnSuccessListener {
                    continuation.resume(item)
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }

}