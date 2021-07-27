package com.example.trelloclone.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.trelloclone.MainActivity
import com.example.trelloclone.activities.*
import com.example.trelloclone.model.Board
import com.example.trelloclone.model.Card
import com.example.trelloclone.model.User
import com.example.trelloclone.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity,userInfo: User){
        mFireStore.collection(Constants.USERS).document(getCurrentUserID()).set(userInfo, SetOptions.merge())
                .addOnSuccessListener {
                    activity.userRegisteredSucess()
                }.addOnFailureListener {
                    Log.e("sign Up","Unable to sign Up, please try again later")
                }
    }

    fun loadUserData(activity: Activity, readBoardList: Boolean = false){
        mFireStore.collection(Constants.USERS).document(getCurrentUserID()).get()
                .addOnSuccessListener { document ->
                    val loggedUser = document.toObject(User::class.java)

                    if(loggedUser != null)
                    when(activity){
                        is SignInActivity -> {
                            activity.signInSucess(loggedUser)
                        }
                        is MainActivity -> {
                            activity.updateNavigationUserDetail(loggedUser,readBoardList)
                        }
                        is MyProfileActivity -> {
                            activity.setUserDataInUi(loggedUser)
                        }
                    }

                }.addOnFailureListener {
                e -> when(activity){
                        is SignInActivity -> {
                            activity.hideCustomProgressDialog()
                        }
                        is MainActivity -> {
                            activity.hideCustomProgressDialog()
                        }
                    }
                }
    }

    fun getCurrentUserID(): String {

        val currentUser = FirebaseAuth.getInstance().currentUser

        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }

        Log.e("userId",currentUserID)

        return currentUserID
    }

    fun updateUserProfileData(activity: MyProfileActivity,userHashMap: HashMap<String,Any>){
        mFireStore.collection(Constants.USERS).document(getCurrentUserID()).update(userHashMap).addOnSuccessListener {
            Log.e("user profile","UserProfile Updated successfully")
            Toast.makeText(activity,"Profile Updated Successfully",Toast.LENGTH_SHORT).show()
            activity.profileUpdateSuccess()
        }.addOnFailureListener {
            e ->
            activity.hideCustomProgressDialog()
            Log.e("Update","Something Went Wrong")
            Toast.makeText(activity,"Something went wrong please try again later",Toast.LENGTH_SHORT).show()
        }
    }


    fun createBoard(activity: CreatBoardActivity, boardInfo: Board){
        mFireStore.collection(Constants.BOARDS).document().set(boardInfo, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(activity,"Board created successfully",Toast.LENGTH_SHORT).show()
                    activity.boardCreatedSuccessfully()
                }.addOnFailureListener {
                    exception ->
                    activity.hideCustomProgressDialog()
                    Log.e("Update","Something Went Wrong")
                    Toast.makeText(activity,"Something went wrong please try again later",Toast.LENGTH_SHORT).show()
                }
    }

    fun getBoardList(activity: MainActivity){
        mFireStore.collection(Constants.BOARDS).whereArrayContains(Constants.ASSIGNED_TO,getCurrentUserID())
                .get().addOnSuccessListener {
                    document ->
                    val boardlist: ArrayList<Board> = ArrayList()
                    for (i in document.documents){
                        val board = i.toObject(Board::class.java)!!
                        board.documentId = i.id
                        boardlist.add(board)
                    }
                    activity.populateBoardListInUI(boardlist)
                    activity.hideCustomProgressDialog()
                }.addOnFailureListener {
                    Log.e("Board List","Getting Board list failed")
                    activity.hideCustomProgressDialog()
                }
    }

    fun getBoardDetails(activity: TaskListActivity,documentId: String){
        mFireStore.collection(Constants.BOARDS).document(documentId)
            .get().addOnSuccessListener {
                    document ->
                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                activity.boardDetails(board)
            }.addOnFailureListener {
                Log.e("Board List","Getting Board list failed")
                activity.hideCustomProgressDialog()
            }
    }

    fun addUpdateTaskList(activity: Activity,board: Board){
        val taskListHashMap = HashMap<String,Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS).document(board.documentId!!).update(taskListHashMap)
                .addOnSuccessListener {
                    Log.e("TaskList","Updated Task List")

                    if(activity is TaskListActivity)
                        activity.addUpdateTaskLIstSuccess()
                    if(activity is CardDetailsActivity)
                        activity.addUpdateTaskListSuccess()
                }.addOnFailureListener {
                    exception ->
                if(activity is TaskListActivity)
                    activity.hideCustomProgressDialog()
                if(activity is CardDetailsActivity)
                    activity.hideCustomProgressDialog()
                    Log.e("TaskList","Task List Update Failed")
                }
    }

    fun getAssignedMembersList(activity: Activity,assignedTo: ArrayList<String>){
        mFireStore.collection(Constants.USERS).whereIn(Constants.ID,assignedTo).get()
            .addOnSuccessListener {
                document ->
                val usersList: ArrayList<User> = ArrayList()

                for (i in document){
                    val user = i.toObject(User::class.java)
                    usersList.add(user)
                }

                if(activity is MembersActivity)
                    activity.setUpMembersList(usersList)
                if (activity is TaskListActivity)
                    activity.membersDetailList(usersList)

            }.addOnFailureListener {
                    exception ->
                    if(activity is MembersActivity)
                        activity.hideCustomProgressDialog()
                    if(activity is TaskListActivity)
                        activity.hideCustomProgressDialog()
                Log.e("TaskList","failed to get Members list ")
            }
    }

    fun getMembersDetails(activity: MembersActivity,email: String){
        mFireStore.collection(Constants.USERS).whereEqualTo(Constants.EMAIL,email).get()
                .addOnSuccessListener {
                    document ->
                    if(document.documents.size > 0){
                        val user = document.documents[0].toObject(User::class.java)!!
                        activity.membersDetail(user)
                    }else{
                        activity.hideCustomProgressDialog()
                        activity.showErrorSnackBar("User with this email not found")
                    }
                }.addOnFailureListener {
                    Toast.makeText(activity,"Something went wrong please try again later",Toast.LENGTH_SHORT).show()
                }
    }

    fun assignMemberToBoard(activity: MembersActivity,board: Board,user: User){

        val assignedToHashMap = HashMap<String,Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS).document(board.documentId!!).update(assignedToHashMap)
                .addOnSuccessListener {
                    activity.memberAssignedSuccess(user)
                }.addOnFailureListener {
                    activity.hideCustomProgressDialog()
                    Toast.makeText(activity,"Something went wrong please try again later",Toast.LENGTH_SHORT).show()
                }
    }
}

