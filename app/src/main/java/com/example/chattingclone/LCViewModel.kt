package com.example.chattingclone

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.chattingclone.Data.CHATS
import com.example.chattingclone.Data.ChatData
import com.example.chattingclone.Data.ChatUser
import com.example.chattingclone.Data.Event
import com.example.chattingclone.Data.MESSAGES
import com.example.chattingclone.Data.Message
import com.example.chattingclone.Data.STATUS
import com.example.chattingclone.Data.Status
import com.example.chattingclone.Data.USER_NODE
import com.example.chattingclone.Data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LCViewModel @Inject constructor(
//    val auth: FirebaseAuth
//    val db: FirebaseFirestore
//    val storage: FirebaseStorage
) : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore
    private val storage: FirebaseStorage = Firebase.storage

    var inProgress = mutableStateOf(false)
    var inProcessChats = mutableStateOf(false)
    val eventMutabletate = mutableStateOf<Event<String>?>(null)
    val signIn = mutableStateOf(true)
    val userData = mutableStateOf<com.example.chattingclone.Data.UserData?>(null)
    val chats = mutableStateOf<List<ChatData>>(listOf())
    val chatMessages = mutableStateOf<List<Message>>(listOf())
    val inProgressChatMessage = mutableStateOf(false)
    var currentChatMessageListener: ListenerRegistration? = null
    val status = mutableStateOf<List<Status>>(listOf())
    val inProgressStatus = mutableStateOf(false)

    init {
        val currentUser = auth.currentUser
        signIn.value = currentUser != null
        currentUser?.uid?.let {
            getUserData(it)
        }
    }

    fun populateMessages(chatId: String) {
        inProgressChatMessage.value = true
        currentChatMessageListener = db.collection(CHATS).document(chatId).collection(MESSAGES)
            .addSnapshotListener { value, error ->
                if (error != null)
                    handleException(error)
                if (value != null) {
                    chatMessages.value = value.documents.mapNotNull {
                        it.toObject<Message>()
                    }.sortedBy { it.timeStamp }
                    inProgressChatMessage.value = false
                }
            }
    }

    fun depopulateMessages() {
        chatMessages.value = listOf()
        currentChatMessageListener = null
    }

    fun populateChats() {
        inProcessChats.value = true
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId),
                Filter.equalTo("user2.userId", userData.value?.userId)
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error)
            }
            if (value != null) {
                chats.value = value.documents.mapNotNull {
                    it.toObject<ChatData>()
                }
                inProcessChats.value = false
            }
        }
    }

    fun onSendReply(chatID: String, message: String) {
        val time = Calendar.getInstance().time.toString()
        val msg = Message(userData.value?.userId, message, time)
        db.collection(CHATS).document(chatID).collection(MESSAGES).document().set(msg)
    }

    fun signUp(context: Context, name: String, email: String, phone: String, password: String) {
        inProgress.value = true
        if (name.isEmpty() or email.isEmpty() or phone.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please fill all fields ")
            ShowToast(context, "Please fill all fields")
            return
        }
        db.collection(USER_NODE).whereEqualTo("phone", phone).get().addOnSuccessListener {
            if (it.isEmpty) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        createOrUpdateProfile(name, phone)
                        ShowToast(context, "Sign-up Successfully")
                    } else {
                        handleException(it.exception, customMessage = "Sign-up failed")
                        ShowToast(context, "Sign-up failed")
                    }
                }
            } else {
                handleException(customMessage = "Phone Already Exist ")
                ShowToast(context, "Phone already exist")
                inProgress.value = false
            }
        }
    }

    fun handleException(exception: Exception? = null, customMessage: String = "") {
        Log.e("Chatting Clone App", "Chatting Clone Exception: ", exception)
        exception?.printStackTrace()
        val errorMessage = exception?.localizedMessage ?: ""
        val message = if (customMessage.isEmpty()) errorMessage else customMessage

        eventMutabletate.value = Event(message)
        inProgress.value = false

    }

    fun LogIn(navController: NavController, context: Context, email: String, password: String) {
        if (email.isEmpty() or password.isEmpty()) {
            ShowToast(context, "Please fill all the fields")
        } else {
            inProgress.value = true
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    signIn.value = true
                    inProgress.value = false
                    auth.currentUser?.uid?.let {
                        getUserData(it)
                        NavigateTo(navController, DestinationScreen.ChatList.route)
                    }
                } else {
                    handleException(exception = it.exception, customMessage = "Login Failed")
                    ShowToast(context, "Login failed")
                }
            }
        }
    }

    fun uploadProfileImage(uri: Uri) {
        uploadImage(uri) {
            createOrUpdateProfile(imageUrl = it.toString())
        }
    }

    fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        inProgress.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            val result = it.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener(onSuccess)
            inProgress.value = false
        }.addOnFailureListener {
            handleException(it)
        }
    }

    fun createOrUpdateProfile(
        name: String? = null, phone: String? = null, imageUrl: String? = null
    ) {
        val uid = auth.currentUser?.uid
        val userData = UserData(
            userId = uid,
            name = name ?: userData.value?.name,
            phone = phone ?: userData.value?.phone,
            imageUrl = imageUrl ?: userData.value?.imageUrl
        )
        uid?.let {
            inProgress.value = true
            db.collection(USER_NODE).document(uid).get().addOnSuccessListener {
                if (it.exists()) {
//                    update data
                    db.collection(USER_NODE).document(uid).update(
                        mapOf(
                            "name" to name, "phone" to phone, "imageUrl" to imageUrl
                        )
                    ).addOnSuccessListener {
                        inProgress.value = false
                        getUserData(uid)
                    }.addOnFailureListener {
                        handleException(it, "Can not update User")
                    }

                } else {
                    db.collection(USER_NODE).document(uid).set(userData)
                    inProgress.value = false
                    getUserData(uid)
                }
            }.addOnFailureListener {
                handleException(it, "Can not Retrieve USer")
            }
        }
    }

    private fun getUserData(uid: String) {
        inProgress.value = true
        db.collection(USER_NODE).document(uid).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error, "Cannot Retrieve User")
            }
            if (value != null) {
                val user = value.toObject<UserData>()
                userData.value = user
                inProgress.value = false
                populateChats()
                populateStatuses()
            }
        }

    }

    private fun ShowToast(context: Context, message: String) {
        viewModelScope.launch {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun logOut() {
        auth.signOut()
        signIn.value = false
        userData.value = null
        depopulateMessages()
        currentChatMessageListener = null
        eventMutabletate.value = Event("Logged Out")
    }

    fun onAddChat(phone: String) {
        if (phone.isEmpty() or !phone.isDigitsOnly()) {
            handleException(customMessage = "Number Must be contains digit only")
        } else {

            db.collection(CHATS).where(
                Filter.or(
                    Filter.and(
                        Filter.equalTo("user1.phone", phone),
                        Filter.equalTo("user2.phone", userData.value?.phone)
                    ), Filter.and(
                        Filter.equalTo("user1.phone", userData.value?.phone),
                        Filter.equalTo("user2.phone", phone)
                    )
                )
            ).get().addOnSuccessListener {
                if (it.isEmpty) {
                    db.collection(USER_NODE).whereEqualTo("phone", phone).get()
                        .addOnSuccessListener {
                            if (it.isEmpty) {
                                handleException(customMessage = "Number not found")
                            } else {
                                val chatPartner = it.toObjects<UserData>()[0]
                                val id = db.collection(CHATS).document().id
                                val chat = ChatData(
                                    chatId = id, ChatUser(
                                        userData.value?.userId,
                                        userData.value?.name,
                                        userData.value?.imageUrl,
                                        userData.value?.phone
                                    ), ChatUser(
                                        chatPartner.userId,
                                        chatPartner.name,
                                        chatPartner.imageUrl,
                                        chatPartner.phone
                                    )
                                )
                                db.collection(CHATS).document(id).set(chat)
                            }
                        }.addOnFailureListener {
                            handleException(it)
                        }
                } else {
                    handleException(customMessage = "Chats Already Exists")
                }
            }
        }
    }

    fun uploadStatus(uri: Uri) {
        uploadImage(uri) {
            createStatus(it.toString())
        }
    }

    fun createStatus(imageUrl: String) {
        val newStatus = Status(
            ChatUser(
                userData.value?.userId,
                userData.value?.name,
                userData.value?.imageUrl,
                userData.value?.phone
            ),
            imageUrl,
            System.currentTimeMillis()
        )
        db.collection(STATUS).document().set(newStatus)
    }

    fun populateStatuses() {
        val timeDelta = 24L * 60 * 60 * 1000
        val cutOff = System.currentTimeMillis() - timeDelta
        inProgressStatus.value = true
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId),
                Filter.equalTo("user2.useId", userData.value?.userId)
            )
        ).addSnapshotListener { value, error ->
            if (error != null)
                handleException(error)
            if (value != null) {
                val currentConnections = arrayListOf(userData.value?.userId)
                val chats = value.toObjects<ChatData>()
                chats.forEach { chat ->
                    if (chat.user1.userId == userData.value?.userId) {
                        currentConnections.add(chat.user2.userId)
                    } else {
                        currentConnections.add(chat.user1.userId)
                    }
                    db.collection(STATUS).whereGreaterThan("timestamp", cutOff)
                        .whereIn("user.userId", currentConnections)
                        .addSnapshotListener { value, error ->
                            if (error != null)
                                handleException(error)
                            if (value != null) {
                                status.value = value.toObjects()
                                inProgressStatus.value = false
                            }
                        }
                }
            }
        }
    }
}

