package zelgius.com.atmirror.shared.entity

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
interface FirebaseObject {
    var key: String?
    val firebasePath: String

}