package zelgius.com.swipetodelete

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

abstract class SwipeToDeleteFirestoreAdapter<T, VH: RecyclerView.ViewHolder>(
    options: FirestoreRecyclerOptions<T>,
    val dragOnView: Boolean = false,
    var deleteListener: (item: T) -> Unit = {}
) : FirestoreRecyclerAdapter<T, VH>(options) {
    lateinit var itemTouchHelper: ItemTouchHelper

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        val swipeToDeleteCallback: SwipeToDeleteCallback = object : SwipeToDeleteCallback(recyclerView.context, dragOnView) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
                val position = viewHolder.adapterPosition
                val item = getData(position)

                if(item != null)
                    deleteListener(item)
            }
        }

        itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    open fun getData(position: Int) = getItem(position)

    open fun startDrag(viewHolder: VH) {
        itemTouchHelper.startSwipe(viewHolder)
    }
}