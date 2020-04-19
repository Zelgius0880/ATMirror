package zelgius.com.atmirror.shared.repository

import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.runBlocking
import zelgius.com.atmirror.shared.entity.Group
import zelgius.com.atmirror.shared.entity.Light
import zelgius.com.atmirror.shared.entity.Switch


class GroupDataSourceFactory : DataSource.Factory<Group, Group>() {
    override fun create() = GroupDataSource()

}

class GroupDataSource : ItemKeyedDataSource<Group, Group>() {
    private val firebaseRepository = GroupRepository()
    override fun loadInitial(
        params: LoadInitialParams<Group>,
        callback: LoadInitialCallback<Group>
    ) {
        runBlocking {
            firebaseRepository.getPagedGroup(null, params.requestedLoadSize, callback)
        }
    }

    override fun loadAfter(
        params: LoadParams<Group>,
        callback: LoadCallback<Group>
    ) {
        runBlocking {
            firebaseRepository.getPagedGroup(params.key, params.requestedLoadSize, callback)
        }
    }

    override fun loadBefore(
        params: LoadParams<Group>,
        callback: LoadCallback<Group>
    ) {
    }

    override fun getKey(group: Group): Group {
        return group
    }

}

class FlattedGroupDataSourceFactory : DataSource.Factory<Group, Any>() {
    override fun create() = FlattedGroupDataSource()

}

class FlattedGroupDataSource : ItemKeyedDataSource<Group, Any>() {
    private val firebaseRepository= GroupRepository()
    override fun loadInitial(
        params: LoadInitialParams<Group>,
        callback: LoadInitialCallback<Any>
    ) {
        runBlocking (CoroutineExceptionHandler{_, e -> e.printStackTrace()}){
            firebaseRepository.getPagedGroupFlatted(null, params.requestedLoadSize, callback)
        }
    }

    override fun loadAfter(
        params: LoadParams<Group>,
        callback: LoadCallback<Any>
    ) {
        runBlocking {
            firebaseRepository.getPagedGroupFlatted(params.key, params.requestedLoadSize, callback)
        }
    }

    override fun loadBefore(
        params: LoadParams<Group>,
        callback: LoadCallback<Any>
    ) {
    }

    override fun getKey(item: Any): Group =
        when(item) {
            is Group -> {item}
            is Switch -> {item.group!!}
            is Light -> {item.group!!}
            else -> { error("Cannot get the group of ${item::class.java.simpleName}")}
        }

}

/*
public class ATPViewModel extends ViewModel {

    private ATPRanksDataSourceFactory dataSourceFactory;
    private PagedList.Config config;
    public ATPViewModel() {
        dataSourceFactory = new ATPRanksDataSourceFactory();

        config = (new PagedList.Config.Builder()).setEnablePlaceholders(false)
                .setInitialLoadSizeHint(20)
                .setPageSize(25).build();
    }

    public Observable<PagedList> getPagedListObservable(){
        return new RxPagedListBuilder(dataSourceFactory, config).buildObservable();
    }
}
 */
