package com.mudryakov.taverna.Objects

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.mudryakov.taverna.MainActivity
import com.mudryakov.taverna.R
import com.mudryakov.taverna.appDatabaseHelper.APP_ACTIVITY
import com.mudryakov.taverna.appDatabaseHelper.USER
import com.mudryakov.taverna.models.CommonModel
import com.mudryakov.taverna.models.MessageModel
import com.mudryakov.taverna.ui.Fragmets.BaseFragment
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.text.SimpleDateFormat
import java.util.*


fun showToast(mesg: String) {
    Toast.makeText(APP_ACTIVITY, mesg, Toast.LENGTH_LONG).show()
}


fun changeFragment(newFragment: Fragment, addStack: Boolean = true) {
    if (addStack) {
        APP_ACTIVITY.supportFragmentManager.beginTransaction()
            .replace(R.id.dataConteiner, newFragment)
            .addToBackStack(null)
            .commit()
    } else {
        APP_ACTIVITY.supportFragmentManager.beginTransaction()
            .replace(R.id.dataConteiner, newFragment)
            .commit()
    }
}

fun ImageView.downloadAndSetImage(url: String) {

    Picasso.get()
        .load(url)
        .error(R.drawable.bomjara)
        .placeholder(R.drawable.ajax_loader)
        .fit()
        .into(this)

}

fun hideKeyBoard() {
    val imm: InputMethodManager =
        APP_ACTIVITY.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(APP_ACTIVITY.window.decorView.windowToken, 0)
}


fun setFullnameUi(): String {
    if (USER.fullName.isEmpty()) return "Unknown"
    if (USER.fullName.contains(" ")) {
        val name = USER.fullName.split(" ")[0].capitalize()
        val surName = USER.fullName.split(" ")[1].capitalize()
        return "$name $surName"
    } else return USER.fullName.capitalize()

}

fun DataSnapshot.getCommonModel() = this.getValue(CommonModel::class.java) ?: CommonModel()

fun DataSnapshot.getCommonMessage() = this.getValue(MessageModel::class.java) ?: MessageModel()


fun greateDialogForConfirm(text: String, function: () -> Unit) {
    val builder = AlertDialog.Builder(APP_ACTIVITY)
    builder.setTitle("Подтвердите действие")
        .setMessage(text)
        .setPositiveButton("да") { dialog, wich -> function() }
        .setNegativeButton("Нет") { dialog, wich -> }
        .show()
}

fun String.transformTime(): String {
    val date = Date(this.toLong())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return timeFormat.format(date)

}

fun RestartActivity() {
    val intent = Intent(APP_ACTIVITY, MainActivity::class.java)
    APP_ACTIVITY.startActivity(intent)
    APP_ACTIVITY.finish()
}

fun View.invisible() {
    this.visibility = View.GONE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun catch(function: () -> Unit) {
    try {
        function()
    } catch (e: Exception) {
        showToast(e.message.toString())
    }
}

fun Int.transformForTimer(format: String): String {
    val timeFormat = SimpleDateFormat(format)
    return timeFormat.format(this)
}

fun getFileNameFromUri(uri: Uri): String {
    var result = ""
    val cursor = APP_ACTIVITY.contentResolver.query(uri, null, null, null, null)
try {
    if(cursor!= null && cursor.moveToFirst()){
        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
    }
}catch(e:java.lang.Exception){
    showToast(e.message.toString())
}finally {
    cursor?.close()
}
    return result
}
fun sizeOfMembers(size:Int) = APP_ACTIVITY.resources.getQuantityString(R.plurals.plurals_greate_group,size,size)
fun BaseFragment.startCrop() {
    CropImage.activity()
        .setAspectRatio(1, 1)
        .setRequestedSize(250, 250)
        .setCropShape(CropImageView.CropShape.OVAL)
        .start(APP_ACTIVITY,this)

}