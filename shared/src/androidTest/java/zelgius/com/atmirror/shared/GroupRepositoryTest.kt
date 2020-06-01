package zelgius.com.atmirror.shared

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import junit.framework.TestCase.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import zelgius.com.atmirror.shared.entity.Group
import zelgius.com.atmirror.shared.entity.GroupItem
import zelgius.com.atmirror.shared.entity.Light
import zelgius.com.atmirror.shared.entity.Switch
import zelgius.com.atmirror.shared.repository.GroupRepository
import zelgius.com.lights.repository.ILight
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.random.Random


class GroupRepositoryTest {
    private val repository = GroupRepository()
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initialize() {
        //FirebaseFirestore.setLoggingEnabled(true)
    }

    @Test
    fun createOrUpdate() {

        // Test create
        runBlocking {
            val g = createSample()

            repository.createOrUpdate(g)
            assertNotNull(g.key)

            repository.getGroup(g.key!!).apply {
                assertEquals(this, g)
            }

            // Updating the item
            g.name += " Updated"
            repository.createOrUpdate(g, lights = g.lights.subList(0,2))

            val subList = g.lights.subList(0,2)
            g.items = g.items.filter {
                it.itemType == GroupItem.ItemType.SWITCH || subList.contains(it)
            }
            repository.getGroup(g.key!!).apply {
                assertEquals(this, g)
            }

            //deleting in order to preserve the db
            repository.delete(g)
        }
    }

    @Test
    fun delete() {
        runBlocking {
            val g = createSample()

            repository.createOrUpdate(g)
            assertNotNull(g.key)

            repository.delete(g)

            //checking if group is still present in the database
            repository.getGroup(g.key!!).apply {
                assertNull(this)
            }
        }
    }

    private lateinit var groups: List<Group>

    @Test
    fun getPagedGroupFlatted() {
        groups = (1..10)
            .map { createSample(name = "Test $it") }
            .sortedBy { it.name }

        runBlocking {
            groups.forEach { repository.createOrUpdate(it) }
        }
        // At first starting DataSource with no paged group
        val factory = repository.getFlattedGroupDataSource()
        val latch = CountDownLatch(1)


        val observer = TestObserver<PagedList<Any>> {
            if (!it.isEmpty()) {
                groups.subList(0, 5).apply {
                    val count = sumBy {g -> 1 + g.switches.size + g.lights.size }
                    assertEquals(count, it.size)

                    val list = mutableListOf<Any>()
                    forEach {g ->
                        list.add(g)

                        g.lights.forEach {s ->
                            list.add(s)
                        }

                        g.switches.forEach {s ->
                            list.add(s)
                        }

                    }

                    list.forEachIndexed {i ,item ->
                        assertEquals(it[i], item)
                    }

                    latch.countDown()
                }
            }
        }

        val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(5)
            .setPageSize(5).build()
        LivePagedListBuilder(factory, pagedListConfig)
            .build()
            .observe(observer, observer)

        latch.await(5, TimeUnit.SECONDS)
        observer.stopObserving()
        assertEquals(0, latch.count)
    }

    @After
    fun removeGroups() {
        if (::groups.isInitialized)
            runBlocking {
                groups.forEach {
                    repository.delete(it)
                }
            }
    }

    private fun createSample(name: String = "Test", switches: Int = 3, lights: Int = 3) =
        Group(name).apply {

            val s = (1..switches).map {
                Switch(
                    name = "Switch $name $it",
                    uid = UUID.randomUUID().toString()
                )
            }

            val l = (1..lights).map {
                Light(
                    name = "Light $name $it",
                    uid = UUID.randomUUID().toString(),
                    type = if (it % 2 == 0) ILight.Type.HUE else ILight.Type.LIFX
                ).apply {
                }
            }

            items = listOf(*s.toTypedArray(), *l.toTypedArray())
        }

    @Test
    fun findCrossCollection() {
        runBlocking {
            val groups = (1 .. 5).map{
                createSample(name = "Test $it").also {g ->
                    repository.createOrUpdate(g)
                }
            }

            val group = groups[Random.nextInt(groups.size)]
            val switch = group.switches.let {
                it[Random.nextInt(it.size)]
            }

            val result = repository.getGroupFromSwitch(switch.uid)
            with(result.find { group.name == it.name }) {
                assertNotNull(this)

                assertNotNull(this!!.switches.find { it.uid == switch.uid })
            }

            groups.forEach {
                repository.delete(it)
            }
        }
    }

    class TestObserver<T>(private val handler: (T) -> Unit) : Observer<T>, LifecycleOwner {
        private val lifecycle = LifecycleRegistry(this)

        init {
            lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }

        override fun getLifecycle(): Lifecycle = lifecycle

        override fun onChanged(t: T) {
            handler(t)
        }

        fun stopObserving() {
            lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
    }
}