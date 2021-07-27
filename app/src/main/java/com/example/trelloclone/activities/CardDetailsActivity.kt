package com.example.trelloclone.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclone.R
import com.example.trelloclone.adapters.CardsMembersListAdapter
import com.example.trelloclone.adapters.MembersListItemAdapter
import com.example.trelloclone.dialogs.LabelColorListDialog
import com.example.trelloclone.dialogs.MembersListDialog
import com.example.trelloclone.firebase.FirestoreClass
import com.example.trelloclone.model.*
import com.example.trelloclone.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {

    private lateinit var mBoardDetails: Board
    private var mTaskListPosition: Int = -1
    private var mCardListPosition: Int = -1

    private var mSelectedColor = ""
    private lateinit var mAssignedMembersDetailList: ArrayList<User>

    private var mSelectedDueDateMilliSecond: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)

        getIntentData()
        setupActionBar()

        val etCardNameDetail = findViewById<TextView>(R.id.et_name_card_details)
        etCardNameDetail.text = mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name
        etCardNameDetail.setSelectAllOnFocus(true)

        val btnUpdate = findViewById<Button>(R.id.btn_update_card_details)
        btnUpdate.setOnClickListener {
            if(etCardNameDetail.text.toString().isNotEmpty()){
                upDateCardDetails()
            }else{
                Toast.makeText(this,"Please enter a card name",Toast.LENGTH_SHORT).show()
            }
        }

        mSelectedColor = mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].labelColor
        if(mSelectedColor.isNotEmpty()){
            setColor()
        }

        val btnSelectLabelColor = findViewById<TextView>(R.id.tv_select_label_color)
        btnSelectLabelColor.setOnClickListener {
            labelColorsListDialog()
        }

        val tvSelectMembers = findViewById<TextView>(R.id.tv_select_members)
        tvSelectMembers.setOnClickListener {
            membersListDialog()
        }

        setupSelectedMembersList()
        mSelectedDueDateMilliSecond = mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].dueDate

        val selectDueDate = findViewById<TextView>(R.id.tv_select_due_date)

        if(mSelectedDueDateMilliSecond > 0){
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate = sdf.format(Date(mSelectedDueDateMilliSecond))
            selectDueDate.text = selectedDate
        }

        selectDueDate.setOnClickListener {
            showDataPicker()
        }
    }

    private fun setupActionBar(){
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_card_details_activity)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun getIntentData(){
        if(intent.hasExtra(Constants.BOARD_DETAILS)){
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAILS)!!
        }
        if(intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION,-1)
        }
        if(intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardListPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION,-1)
        }
        if(intent.hasExtra(Constants.BOARDS_MEMBERS_LIST)){
            mAssignedMembersDetailList = intent.getParcelableArrayListExtra<User>(Constants.BOARDS_MEMBERS_LIST)!!
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_delete_card -> {
                alertDialogForDeleteCard(mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun addUpdateTaskListSuccess(){
        hideCustomProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun upDateCardDetails(){
        val card = Card(

                findViewById<TextView>(R.id.et_name_card_details).text.toString(),
                mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].createdBy,
                mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo,
                mSelectedColor,
                mSelectedDueDateMilliSecond,
        )

        val taskList: ArrayList<Tasks> = mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)

        mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition] = card

        showCustomProgressBar()
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    private fun deleteCard(){
        val cardsList: ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards

        cardsList.removeAt(mCardListPosition)

        val tasksList: ArrayList<Tasks> = mBoardDetails.taskList
        tasksList.removeAt(tasksList.size-1)

        tasksList[mTaskListPosition].cards = cardsList

        showCustomProgressBar()
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setMessage(
            resources.getString(
                R.string.confirmation_message_to_delete_card,
                cardName
            )
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, which ->
            dialogInterface.dismiss()
            deleteCard()
        }

        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun colorsList(): ArrayList<String> {

        val colorsList: ArrayList<String> = ArrayList()
        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")

        return colorsList
    }

    private fun setColor() {
        val tvSelectLabelColor = findViewById<TextView>(R.id.tv_select_label_color)
        tvSelectLabelColor.text = ""
        tvSelectLabelColor.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    private fun labelColorsListDialog() {

        val colorsList: ArrayList<String> = colorsList()
        val listDialog = object : LabelColorListDialog(
                this@CardDetailsActivity,
                colorsList,
                resources.getString(R.string.str_select_label_color),
                mSelectedColor
        ) {
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }

    fun membersListDialog(){
        val cardAssignedMemberList = mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo

        if(cardAssignedMemberList.size > 0){
            for (i in mAssignedMembersDetailList.indices){
                for(j in cardAssignedMemberList){
                    if(mAssignedMembersDetailList[i].id == j){
                        mAssignedMembersDetailList[i].selected = true
                    }
                }
            }
        }else {
            for (i in mAssignedMembersDetailList.indices){
                mAssignedMembersDetailList[i].selected = false
            }
        }

        val listDialog = object : MembersListDialog(
                this,
                mAssignedMembersDetailList,
                "Select Member",
        ){
            override fun onItemSelected(user: User, action: String) {
                if(action == Constants.SELECT){
                    if(!mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.contains(user.id)){
                        mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.add(user.id!!)
                    }
                }else{
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.remove(user.id!!)
                    for(i in mAssignedMembersDetailList.indices){
                        if (mAssignedMembersDetailList[i].id == user.id){
                            mAssignedMembersDetailList[i].selected = false
                        }
                    }
                }
                setupSelectedMembersList()
            }
        }
        listDialog.show()
    }

    private fun setupSelectedMembersList(){
        val cardAssignedMembersList = mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo

        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()
        for (i in mAssignedMembersDetailList.indices){
            for(j in cardAssignedMembersList){
                if(mAssignedMembersDetailList[i].id == j){
                    val selectedMember = SelectedMembers(
                            mAssignedMembersDetailList[i].id!!,
                            mAssignedMembersDetailList[i].image!!
                    )
                    selectedMembersList.add(selectedMember)
                }
            }
        }

        if(selectedMembersList.size > 0){
            val tvSelectMember = findViewById<TextView>(R.id.tv_select_members)
            val rvSelectedMembers = findViewById<RecyclerView>(R.id.rv_selected_members_list)

            selectedMembersList.add(SelectedMembers("", ""))

            tvSelectMember.visibility = View.GONE
            rvSelectedMembers.visibility = View.VISIBLE

            rvSelectedMembers.layoutManager = GridLayoutManager(this, 6, )

            val adapter = CardsMembersListAdapter(this, selectedMembersList,true)
            rvSelectedMembers.adapter = adapter
            adapter.setOnClickListener(
                    object : CardsMembersListAdapter.OnClickListener{
                        override fun onClick() {
                            membersListDialog()
                        }
                    }
            )
        }else{
            val tvSelectMember = findViewById<TextView>(R.id.tv_select_members)
            val rvSelectedMembers = findViewById<RecyclerView>(R.id.rv_selected_members_list)

            tvSelectMember.visibility = View.VISIBLE
            rvSelectedMembers.visibility = View.GONE
        }
    }

    private fun showDataPicker() {
        val c = Calendar.getInstance()
        val year =
                c.get(Calendar.YEAR) // Returns the value of the given calendar field. This indicates YEAR
        val month = c.get(Calendar.MONTH) // This indicates the Month
        val day = c.get(Calendar.DAY_OF_MONTH) // This indicates the Day


        val dpd = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                    // Here we have appended 0 if the selected month is smaller than 10 to make it double digit value.
                    val sMonthOfYear =
                            if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                    val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                    // Selected date it set to the TextView to make it visible to user.
                    val selectDueDate = findViewById<TextView>(R.id.tv_select_due_date)
                    selectDueDate.text = selectedDate

                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

                    // The formatter will parse the selected date in to Date object
                    // so we can simply get date in to milliseconds.
                    val theDate = sdf.parse(selectedDate)


                    mSelectedDueDateMilliSecond = theDate!!.time
                },
                year,
                month,
                day
        )
        dpd.show()
    }
}