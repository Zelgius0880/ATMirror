package zelgius.com.atmirror.shared.repository

import com.google.firebase.firestore.DocumentSnapshot
import zelgius.com.atmirror.shared.entity.GroupItem
import zelgius.com.atmirror.shared.entity.Light
import zelgius.com.atmirror.shared.entity.Switch
import zelgius.com.lights.repository.ILight

class FirestoreGroupItemMapper {
    companion object {
        fun map(document: DocumentSnapshot) =
            when(document.get("itemType", GroupItem.ItemType::class.java)!!) {
                GroupItem.ItemType.SWITCH -> {
                    Switch(
                        key = document.id,
                        uid = document.getString("uid")!!,
                        name = document.getString("name")!!
                    )
                }

                GroupItem.ItemType.LIGHT -> {
                    Light(
                        key = document.id,
                        uid = document.getString("uid")!!,
                        name = document.getString("name")!!,
                        type = document.get("type", ILight.Type::class.java)!!,
                        state = document.get("state", Light.State::class.java)!!
                    )
                }
            }

    }
}