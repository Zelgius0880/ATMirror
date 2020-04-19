package zelgius.com.atmirror.mobile

import android.app.Activity
import android.content.Context
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

val ViewBinding.context
    get() = this.root.context!!


var TextInputLayout.text
    get() = editText?.text?.toString()
    set(value) = editText!!.setText(value)

fun Activity.hideKeyboard() {
    val view = this.currentFocus
    view?.let { v ->
        val imm = (Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(v.windowToken, 0)
    }
}
fun Fragment.hideKeyboard(){
    // Check if no view has focus:
    val view = this.requireActivity().currentFocus
    view?.let { v ->
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(v.windowToken, 0)
    }
}