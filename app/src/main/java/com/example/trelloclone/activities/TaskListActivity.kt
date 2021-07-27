package com.example.trelloclone.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclone.R
import com.example.trelloclone.adapters.TaskListItemAdapter
import com.example.trelloclone.firebase.FirestoreClass
import com.example.trelloclone.model.Board
import com.example.trelloclone.model.Card
import com.example.trelloclone.model.Tasks
import com.example.trelloclone.model.User
import com.example.trelloclone.utils.Constants
import java.sql.Array

@Suppress("DEPRECATION")
class TaskListActivity : BaseActivity() {

    private lateinit var mBoardDetails : Board
    private lateinit var mDocumentId: String

    lateinit var mMembersDetailList: ArrayList<User>

    companion object{
        const val MEMBERS_REQUEST_CODE: Int = 13
        const val CARD_DETAILS_REQUEST_CODE = 14
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        if(intent.hasExtra(Constants.DOCUMENT_ID)){
            mDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }

        showCustomProgressBar()
        FirestoreClass().getBoardDetails(this,mDocumentId)
    }

    private fun setupActionBar(){
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_task_list_activity)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = mBoardDetails.name
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_members -> {
                val intent = Intent(this,MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAILS,mBoardDetails)
                startActivityForResult(intent, MEMBERS_REQUEST_CODE)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MEMBERS_REQUEST_CODE || requestCode == CARD_DETAILS_REQUEST_CODE){
            showCustomProgressBar()
            FirestoreClass().getBoardDetails(this,mDocumentId)
        }else{
            Log.e("Members","Canceled")
        }
    }

    fun boardDetails(board: Board){

        mBoardDetails = board

        hideCustomProgressDialog()
        setupActionBar()

        showCustomProgressBar()
        FirestoreClass().getAssignedMembersList(this,mBoardDetails.assignedTo)
    }

    fun addUpdateTaskLIstSuccess(){
        hideCustomProgressDialog()

        showCustomProgressBar()
        FirestoreClass().getBoardDetails(this, mBoardDetails.documentId!!)
    }

    fun createTaskList(taskListName: String) {
        val task = Tasks(taskListName, FirestoreClass().getCurrentUserID())
        mBoardDetails.taskList.add(0,task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        showCustomProgressBar()
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    fun updateTaskList(position: Int, listName: String, model: Tasks){
        val task = Tasks(listName,model.createdBy)

        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        showCustomProgressBar()
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    fun deleteTaskList(position: Int){
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        showCustomProgressBar()
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    fun addCardToArrayList(position: Int,cardName: String){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        val cardAssignedUserList: ArrayList<String> = ArrayList()
        cardAssignedUserList.add(FirestoreClass().getCurrentUserID())

        val card = Card(cardName,FirestoreClass().getCurrentUserID(),cardAssignedUserList)

        val cardList = mBoardDetails.taskList[position].cards
        cardList.add(card)

        val task = Tasks(
                mBoardDetails.taskList[position].title,
                mBoardDetails.taskList[position].createdBy,
                cardList
        )

        mBoardDetails.taskList[position] = task

        showCustomProgressBar()
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    fun cardDetails(taskListPosition: Int,cardListPosition: Int){
        val intent = Intent(this,CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAILS,mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION,taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION,cardListPosition)
        intent.putExtra(Constants.BOARDS_MEMBERS_LIST,mMembersDetailList)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }

    fun membersDetailList(list: ArrayList<User>){
        mMembersDetailList = list

        hideCustomProgressDialog()

        val addTaskList = Tasks(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)

        val rvTaskList = findViewById<RecyclerView>(R.id.rv_task_list)
        rvTaskList.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        rvTaskList.setHasFixedSize(true)

        val adapter =  TaskListItemAdapter(this,mBoardDetails.taskList)
        rvTaskList.adapter = adapter
    }

    fun updateCardsInTaskList(taskListPosition: Int, cards: ArrayList<Card>){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        mBoardDetails.taskList[taskListPosition].cards = cards

        showCustomProgressBar()
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }
}