package droidninja.filepicker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import droidninja.filepicker.fragments.*
import droidninja.filepicker.utils.FragmentUtil
import droidninja.filepicker.utils.MediaPlayerManager
import java.util.ArrayList

class FilePickerActivity : BaseFilePickerActivity(),
        PhotoPickerFragmentListener, MediaPickerFragment.MediaPickerFragmentListener,
        DocFragment.DocFragmentListener, DocPickerFragment.DocPickerFragmentListener,
        AudioFragment.AudioFragmentListener, AudioPickerFragment.AudioPickerFragmentListener {

    private var type: Int = 0

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_file_picker)
    }

    override fun initView() {
        val intent = intent
        if (intent != null) {
            var selectedPaths: ArrayList<String>? = intent.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)
            type = intent.getIntExtra(FilePickerConst.EXTRA_PICKER_TYPE, FilePickerConst.MEDIA_PICKER)

            if (selectedPaths != null) {

                if (PickerManager.getMaxCount() == 1) {
                    selectedPaths.clear()
                }

                PickerManager.clearSelections()
                when (type) {
                    FilePickerConst.MEDIA_PICKER -> PickerManager.add(selectedPaths, FilePickerConst.FILE_TYPE_MEDIA)
                    FilePickerConst.AUDIO_PICKER -> PickerManager.add(selectedPaths, FilePickerConst.FILE_TYPE_AUDIO)
                    else -> PickerManager.add(selectedPaths, FilePickerConst.FILE_TYPE_DOCUMENT)
                }
            }

            setToolbarTitle(PickerManager.currentCount)
            openSpecificFragment(type)
        }
    }

    private fun setToolbarTitle(count: Int) {
        val actionBar = supportActionBar
        if (actionBar != null) {
            val maxCount = PickerManager.getMaxCount()
            if (maxCount == -1 && count > 0) {
                actionBar.title = String.format(getString(R.string.attachments_num), count)
            } else if (maxCount > 0 && count > 0) {
                actionBar.title = String.format(getString(R.string.attachments_title_text), count, maxCount)
            } else if (!TextUtils.isEmpty(PickerManager.title)) {
                actionBar.title = PickerManager.title
            } else {
                when (type) {
                    FilePickerConst.MEDIA_PICKER -> actionBar.setTitle(R.string.select_photo_text)
                    FilePickerConst.AUDIO_PICKER -> actionBar.setTitle(R.string.select_audio_text)
                    else -> actionBar.setTitle(R.string.select_doc_text)
                }
            }
        }
    }

    private fun openSpecificFragment(type: Int) {
        when (type) {
            FilePickerConst.MEDIA_PICKER -> {
                val photoFragment = MediaPickerFragment.newInstance()
                FragmentUtil.replaceFragment(this, R.id.container, photoFragment)
            }
            FilePickerConst.AUDIO_PICKER -> {
                val audioFragment = AudioPickerFragment.newInstance()
                FragmentUtil.replaceFragment(this, R.id.container, audioFragment)
            }
            else -> {
                if (PickerManager.isDocSupport) PickerManager.addDocTypes()
                val photoFragment = DocPickerFragment.newInstance()
                FragmentUtil.replaceFragment(this, R.id.container, photoFragment)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.picker_menu, menu)
        val menuItem = menu.findItem(R.id.action_done)
        if (menuItem != null) {
            menuItem.isVisible = PickerManager.getMaxCount() != 1
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == R.id.action_done) {
            when (type) {
                FilePickerConst.MEDIA_PICKER -> returnData(PickerManager.selectedPhotos)
                FilePickerConst.AUDIO_PICKER -> returnData(PickerManager.selectedAudio)
                else -> returnData(PickerManager.selectedFiles)
            }

            return true
        } else if (i == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        MediaPlayerManager.getInstance().stop()
        PickerManager.reset()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FilePickerConst.REQUEST_CODE_MEDIA_DETAIL -> if (resultCode == Activity.RESULT_OK) {
                when (type) {
                    FilePickerConst.MEDIA_PICKER -> returnData(PickerManager.selectedPhotos)
                    FilePickerConst.AUDIO_PICKER -> {
                        MediaPlayerManager.getInstance().stop()
                        returnData(PickerManager.selectedAudio)
                    }
                    else -> returnData(PickerManager.selectedFiles)
                }
            } else {
                setToolbarTitle(PickerManager.currentCount)
            }
        }
    }

    private fun returnData(paths: ArrayList<String>) {
        val intent = Intent()
        when (type) {
            FilePickerConst.MEDIA_PICKER -> intent.putStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA, paths)
            FilePickerConst.AUDIO_PICKER -> intent.putStringArrayListExtra(FilePickerConst.KEY_SELECTED_AUDIO, paths)
            else -> intent.putStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS, paths)
        }

        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onItemSelected() {
        val currentCount = PickerManager.currentCount
        setToolbarTitle(currentCount)

        if (PickerManager.getMaxCount() == 1 && currentCount == 1) {
            returnData(
                    when (type) {
                        FilePickerConst.MEDIA_PICKER -> PickerManager.selectedPhotos
                        FilePickerConst.AUDIO_PICKER -> PickerManager.selectedPhotos
                        else -> PickerManager.selectedFiles
                    })
        }
    }

}
