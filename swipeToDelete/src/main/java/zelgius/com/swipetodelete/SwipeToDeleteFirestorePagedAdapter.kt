package zelgius.com.swipetodelete

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadType
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions

abstract class SwipeToDeleteFirestorePagedAdapter<T, VH : RecyclerView.ViewHolder>(
    private val clazz: Class<T>,
    options: FirestorePagingOptions<T>,
    val dragOnView: Boolean = false,
    var deleteListener: (item: T) -> Unit = {}
) : FirestorePagingAdapter<T, VH>(options) {
    lateinit var itemTouchHelper: ItemTouchHelper

    private val loadStateListener: (CombinedLoadStates) -> Unit = {
        onLoadStateChanged(it.refresh)
    }

    open fun onLoadStateChanged(state: LoadState) {
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        val swipeToDeleteCallback: SwipeToDeleteCallback =
            object : SwipeToDeleteCallback(recyclerView.context, dragOnView) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
                    val position = viewHolder.adapterPosition
                    val item = getData(position)

                    if (item != null)
                        deleteListener(item)
                }
            }

        itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        addLoadStateListener(loadStateListener)
    }

    open fun getData(position: Int) = getItem(position)?.toObject(clazz)

    open fun startDrag(viewHolder: VH) {
        itemTouchHelper.startSwipe(viewHolder)
    }
}