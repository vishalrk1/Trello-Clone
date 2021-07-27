package com.example.trelloclone.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trelloclone.R
import com.example.trelloclone.activities.TaskListActivity
import com.example.trelloclone.model.Card
import com.example.trelloclone.model.SelectedMembers

open class CardListItemAdapter(private val context: Context, private var list: ArrayList<Card>):
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
                LayoutInflater.from(context).inflate(
                        R.layout.item_card,
                        parent,
                        false
                )
        )
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            holder.itemView.findViewById<TextView>(R.id.tv_card_name).text = model.name
        }

        if(model.labelColor.isNotEmpty()){
            holder.itemView.findViewById<View>(R.id.view_label_color).visibility = View.VISIBLE
            holder.itemView.findViewById<View>(R.id.view_label_color).setBackgroundColor(Color.parseColor(model.labelColor))
        }else{
            holder.itemView.findViewById<View>(R.id.view_label_color).visibility = View.GONE
        }

        if((context as TaskListActivity).mMembersDetailList.size > 0 ){
            val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

            for (i in context.mMembersDetailList.indices){
                for (j in model.assignedTo){
                    if(context.mMembersDetailList[i].id == j){
                        val selectedMember = SelectedMembers(
                                context.mMembersDetailList[i].id!!,
                                context.mMembersDetailList[i].image!!,
                        )
                        selectedMembersList.add(selectedMember)
                    }
                }
            }

            if (selectedMembersList.size > 0){
                if(selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy){
                    holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list).visibility = View.GONE
                }else{
                    val rvCardMembers = holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list)
                    rvCardMembers.visibility = View.VISIBLE

                    rvCardMembers.layoutManager = GridLayoutManager(context,4)

                    val adapter = CardsMembersListAdapter(context,selectedMembersList,true)
                    rvCardMembers.adapter = adapter
                    adapter.setOnClickListener(object : CardsMembersListAdapter.OnClickListener{
                        override fun onClick() {
                            if(onClickListener != null){
                                onClickListener!!.onClick(position)
                            }
                        }
                    })
                }
            }else {
                holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list).visibility = View.GONE
            }
        }

        holder.itemView.setOnClickListener {
            if(onClickListener != null){
                onClickListener!!.onClick(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }


    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int)
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}