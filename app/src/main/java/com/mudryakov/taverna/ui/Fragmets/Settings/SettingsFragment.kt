package com.mudryakov.taverna.ui.Fragmets.Settings

import ChangeUserNameFragment
import SettingsChangeUserFullName
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.mudryakov.taverna.MainActivity
import com.mudryakov.taverna.Objects.*
import com.mudryakov.taverna.R
import com.mudryakov.taverna.appDatabaseHelper.*
import com.mudryakov.taverna.ui.Fragmets.BaseFragment
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.fragment_settings.*


class SettingsFragment : BaseFragment(R.layout.fragment_settings) {



    override fun onResume() {
        super.onResume()
        setHasOptionsMenu(true)

                addInfoUser()
        btnSettingsChangeUsername.setOnClickListener {
           changeFragment(
                ChangeUserNameFragment()
            )
        }
        btnSettingsChangeAboutMe.setOnClickListener {
            changeFragment(
                SettingsChangeBioFragment()
            )
        }
        SettingsBtnNewAvatar.setOnClickListener {
            startCrop()
        }
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity?.menuInflater?.inflate(R.menu.settings_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.exit -> {
               greateDialogForConfirm("Вы действительно хотите выйти?"){
                   appStatus.changeState(appStatus.OFFLINE)
                   AUTH.signOut()
                   RestartActivity()
                   }
            }
            R.id.changeName ->
                changeFragment(SettingsChangeUserFullName())
        }
        return true
    }


    fun addInfoUser() {

        tvUserNameSettings.text = setFullnameUi()
        SettingsPhoneNumber.text = USER.phoneNumber
        SettingsAboutMe.text = USER.bio
        tvSettingsOnline.text = USER.status
        SettingsUsername.text = USER.username
        try {
            ic_settings_profile.downloadAndSetImage(USER.photoUrl)
        } catch (e: Exception) {
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && data != null
            && resultCode == RESULT_OK
        ) {
            val uri = CropImage.getActivityResult(data).uri

            val path = REF_STORAGE_ROOT.child(NODE_PROFILE_IMG).child(CURRENT_UID)

            putFileToStorage(path, uri) { //uri - kartinka resultat activnosti cropa
                downloadUrl(path) {
                    addUrlBase(it) { //it -> URL
                       USER.photoUrl = it
                      catch{ ic_settings_profile.downloadAndSetImage(USER.photoUrl)}
                        (activity as MainActivity).myDrawer.updateProfile()
                    }
                }
            }
        }
    }
}
