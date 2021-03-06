package com.mudryakov.taverna.appDatabaseHelper

import android.net.Uri
import android.provider.ContactsContract
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mudryakov.taverna.Objects.READ_CONTACTS
import com.mudryakov.taverna.Objects.appValueEventListener
import com.mudryakov.taverna.Objects.checkPermission
import com.mudryakov.taverna.Objects.showToast
import com.mudryakov.taverna.models.CommonModel
import com.mudryakov.taverna.models.Users
import com.mudryakov.taverna.ui.Fragmets.Groups.GroupsAddAdapter

lateinit var TOOLBAR: androidx.appcompat.widget.Toolbar
lateinit var APP_ACTIVITY: AppCompatActivity
lateinit var AUTH: FirebaseAuth
lateinit var REF_DATABASE_ROOT: DatabaseReference
lateinit var REF_STORAGE_ROOT: StorageReference
lateinit var USER: Users
lateinit var CURRENT_UID: String

const val TYPE_IMAGE = "image"
const val TYPE_TEXT = "text"
const val TYPE_VOICE = "voice"
const val TYPE_FILE = "file"

const val ATTACH_FILE_CODE = 313

const val NODE_FILES = "files"
const val NODE_PHONES_CONTACTS = "phone_contacts"
const val NODE_PHONES = "phones"
const val NODE_PROFILE_IMG = "profileImg"
const val NODE_USERNAMES = "usernames"
const val NODE_USERS = "users"
const val NODE_MESSAGES = "messages"
const val NODE_DIALOGS = "dialogs"
const val NODE_GROUPS_PHOTO = "groupPhoto"
const val NODE_GROUPS = "groups"


const val CHILD_DURATION = "duration"
const val CHILD_TIME = "time"
const val CHILD_TYPE = "type"
const val CHILD_FROM = "from"
const val CHILD_TEXT = "text"

const val CHILD_ID = "id"
const val CHILD_PHONE = "phoneNumber"
const val CHILD_USERNAME = "username"
const val CHILD_STATUS = "status"
const val CHILD_FULL_NAME = "fullName"
const val CHILD_FILE_URL = "fileUrl"
const val CHILD_BIO = "bio"
const val CHILD_PHOTO_URL = "photoUrl"

const val GROUP_CREATOR = "creator"
const val GROUP_MEMBER = "member"
const val GROUP_NAME ="groupName"


fun initFireBase() {
    AUTH = FirebaseAuth.getInstance()

    REF_DATABASE_ROOT = FirebaseDatabase.getInstance().reference
    USER = Users()
    CURRENT_UID = AUTH.currentUser?.uid.toString()
    REF_STORAGE_ROOT = FirebaseStorage.getInstance().reference
}

inline fun putFileToStorage(path: StorageReference, uri: Uri, crossinline function: () -> Unit) {
    path.putFile(uri)
        .addOnSuccessListener { function() }
        .addOnFailureListener { showToast("Произошла ошибка") }

}

inline fun downloadUrl(path: StorageReference, crossinline function: (url: String) -> Unit) {
    path.downloadUrl
        .addOnSuccessListener { function(it.toString()) }
        .addOnFailureListener { showToast("Произошла ошибка") }
}

inline fun addUrlBase(url: String, crossinline function: () -> Unit) {
    REF_DATABASE_ROOT.child(NODE_USERS).child(CURRENT_UID)
        .child(CHILD_PHOTO_URL)
        .setValue(url)
        .addOnSuccessListener { function() }
        .addOnFailureListener { showToast("Произошла ошибка") }
}

inline fun initUser(crossinline function: () -> Unit) {
    REF_DATABASE_ROOT.child(NODE_USERS).child(CURRENT_UID)
        .addListenerForSingleValueEvent(appValueEventListener {
            USER = it.getValue(Users::class.java) ?: Users()
            function()
        })
}

fun initContacts() {
    if (AUTH.currentUser != null) {
        if (checkPermission(READ_CONTACTS)) {
            var arrayContacts = arrayListOf<CommonModel>()
            val cursor = APP_ACTIVITY.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
            )
            cursor?.let {
                while (it.moveToNext()) {
                    val fullName =
                        it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    var phone =
                        it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            .replace(Regex("[\\s,-]"), "")
                    if (phone[0] == '8') phone = phone.replaceFirst("8", "+7")
                    val newModel = CommonModel(fullName = fullName, phoneNumber = phone)
                    if (!arrayContacts.contains(newModel)) arrayContacts.add(newModel)
                }
            }
            cursor?.close()
            REF_DATABASE_ROOT.child(NODE_PHONES)
                .addListenerForSingleValueEvent(appValueEventListener {
                    it.children.forEach { snapshot ->
                        arrayContacts.forEach { contact ->
                            if (snapshot.key == contact.phoneNumber) {
                                REF_DATABASE_ROOT.child(NODE_PHONES_CONTACTS)
                                    .child(CURRENT_UID)
                                    .child(snapshot.value.toString())
                                    .child(CHILD_ID).setValue(snapshot.value.toString())
                                REF_DATABASE_ROOT.child(NODE_PHONES_CONTACTS)
                                    .child(CURRENT_UID)
                                    .child(snapshot.value.toString())
                                    .child(CHILD_FULL_NAME).setValue(contact.fullName)

                            }

                        }

                    }
                })
        }
    }
}

fun sendMessage(
    text: String,
    friendId: String,
    type: String,
    fileUrl: String = "",
    key: String = "",
    duration: String = "",
    dialogType: String = "Text",
    function: () -> Unit

) {

    val refUser = "/$NODE_MESSAGES/$CURRENT_UID/$friendId"
    val refFriend = "/$NODE_MESSAGES/$friendId/$CURRENT_UID"
    val addMessage = HashMap<String, Any>()
    addMessage[CHILD_TEXT] = text
    addMessage[CHILD_FROM] = CURRENT_UID
    addMessage[CHILD_TYPE] = type
    addMessage[CHILD_TIME] = ServerValue.TIMESTAMP
    addMessage[CHILD_ID] = key
    addMessage[CHILD_FILE_URL] = fileUrl
    addMessage[CHILD_DURATION] = duration

    val hashForUpdate = HashMap<String, Any>()
    hashForUpdate["$refUser/$key"] = addMessage
    hashForUpdate["$refFriend/$key"] = addMessage

    REF_DATABASE_ROOT.updateChildren(hashForUpdate)
        .addOnSuccessListener { function() }
        .addOnFailureListener { showToast(it.message.toString()) }.addOnCompleteListener {
            addDialogtoRealTimeDataBase(friendId, dialogType)
        }
}

fun addDialogtoRealTimeDataBase(friendId: String, dialogType: String) {
    val mUser = "$NODE_DIALOGS/$CURRENT_UID/$friendId"
    val mfriend = "$NODE_DIALOGS/$friendId/$CURRENT_UID"

    val mapUser = HashMap<String, Any>()
    val mapFriend = HashMap<String, Any>()

    mapFriend[CHILD_ID] = CURRENT_UID
    mapFriend[CHILD_TYPE] = dialogType
    mapUser[CHILD_TYPE] = dialogType
    mapUser[CHILD_ID] = friendId

    val hashForUpdate = HashMap<String, Any>()
    hashForUpdate[mUser] = mapUser
    hashForUpdate[mfriend] = mapFriend
    REF_DATABASE_ROOT.updateChildren(hashForUpdate)

}

fun getMessageKey(id: String) =
    REF_DATABASE_ROOT.child(NODE_MESSAGES).child(CURRENT_UID).child(id).push().key.toString()

fun createGroup(uri: Uri?, groupName: String, function: () -> Unit) {
    val key = REF_DATABASE_ROOT.child(NODE_GROUPS).push().key.toString()
    val storagePath = REF_STORAGE_ROOT.child(NODE_GROUPS_PHOTO).child(key)
    val groupPath = REF_DATABASE_ROOT.child(NODE_GROUPS).child(key)
    if (uri != null) {

        putFileToStorage(storagePath, uri) {
            downloadUrl(storagePath) { it ->
                val hashMapForGroups: HashMap<String, Any> = hashMapOf()

                hashMapForGroups[GROUP_NAME] = groupName
                hashMapForGroups[CHILD_PHOTO_URL] = it
                hashMapForGroups[CURRENT_UID] = GROUP_CREATOR

                GroupsAddAdapter.listUsersForGroup.forEach { user ->
                    hashMapForGroups[user.id] = GROUP_MEMBER
                }

                groupPath.updateChildren(hashMapForGroups).addOnSuccessListener { function() }
                    .addOnFailureListener{ showToast(it.message.toString())}
            }
function()
        }
    } else showToast("Добавте фото для группы")
}


