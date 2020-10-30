package com.mudryakov.taverna.ui.Fragmets.MainChatList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.mudryakov.taverna.Objects.*
import com.mudryakov.taverna.R
import com.mudryakov.taverna.appDatabaseHelper.*
import com.mudryakov.taverna.models.MainListModel
import com.mudryakov.taverna.ui.Fragmets.SingleChat.SingleChatFragment
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.main_list_item.view.*

class MainLIstRecycleAdapter : RecyclerView.Adapter<MainLIstRecycleAdapter.MainListViewHolder>() {

    var mutableList = mutableListOf<MainListModel>()
lateinit var currentMainListModel:MainListModel

    class MainListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout1:ConstraintLayout = view.contact_item_layout
        val mainListPhoto: CircleImageView = view.mainListPhoto
        val mainListFullName: TextView = view.contact_fullname
        val mainListLastMessage: TextView = view.mainListLastMessage
        val mainListLastMessageTime: TextView = view.mainListMessageTime

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainListViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.main_list_item, parent, false)
        return MainListViewHolder(view)

    }

    override fun onBindViewHolder(holder: MainListViewHolder, position: Int) {
        currentMainListModel = mutableList[position]
        holder.mainListFullName.text =
            if  (currentMainListModel.user.fullName!="") currentMainListModel.user.fullName
            else currentMainListModel.user.phoneNumber

        holder.mainListPhoto.downloadAndSetImage(currentMainListModel.user.photoUrl)
        holder.mainListLastMessageTime.text =
            currentMainListModel.message.time.toString().transformTime()
        holder.mainListLastMessage.text =
            when (currentMainListModel.message.type) {
                TYPE_TEXT -> currentMainListModel.message.text
                TYPE_FILE -> "Вложенный файл"
                TYPE_VOICE -> "Голосовое сообщение ${
                    currentMainListModel.message.duration.toInt().transformForTimer("mm:ss")}"
                TYPE_IMAGE -> "Картинка"
            else -> "ошибка"
            }


    }

    override fun getItemCount(): Int {
        return mutableList.size
    }

fun addItem(item:MainListModel){
    if (!mutableList.any {it.user == item.user}){
        mutableList.add(item)
        notifyItemInserted(mutableList.size)


    }
}

    override fun onViewAttachedToWindow(holder: MainListViewHolder) {
        super.onViewAttachedToWindow(holder)
    holder.layout1.setOnClickListener {
        changeFragment(SingleChatFragment(currentMainListModel.user))
    }
    }

    override fun onViewDetachedFromWindow(holder: MainListViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.layout1.setOnClickListener {}
    }
}