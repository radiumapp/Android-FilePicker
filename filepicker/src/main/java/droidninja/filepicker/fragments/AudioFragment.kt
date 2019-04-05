package droidninja.filepicker.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.PickerManager
import droidninja.filepicker.R
import droidninja.filepicker.adapters.AudioListAdapter
import droidninja.filepicker.adapters.FileAdapterListener
import droidninja.filepicker.models.Document
import droidninja.filepicker.models.FileType

class AudioFragment : BaseFragment(), FileAdapterListener {
    lateinit var recyclerView: RecyclerView
    lateinit var emptyView: TextView

    private var mListener: AudioFragmentListener? = null
    private var selectAllItem: MenuItem? = null
    private var audioListAdapter: AudioListAdapter? = null

    val fileType: FileType?
        get() = arguments?.getParcelable(BaseFragment.FILE_TYPE)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_photo_picker, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AudioFragmentListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement AudioPickerFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onItemSelected() {
        mListener?.onItemSelected()
        audioListAdapter?.let { adapter ->
            selectAllItem?.let { menuItem ->
                if (adapter.itemCount == adapter.selectedItemCount) {
                    menuItem.setIcon(R.drawable.ic_select_all)
                    menuItem.isChecked = true
                }
            }
        }
    }

    interface AudioFragmentListener {
        fun onItemSelected()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    private fun initView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerview)
        emptyView = view.findViewById(R.id.empty_view)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.visibility = View.GONE
    }

    fun updateList(dirs: List<Document>) {
        view?.let {
            if (dirs.size > 0) {
                recyclerView.visibility = View.VISIBLE
                emptyView.visibility = View.GONE

                context?.let {
                    audioListAdapter = recyclerView.adapter as? AudioListAdapter
                    if (audioListAdapter == null) {
                        audioListAdapter = AudioListAdapter(it, dirs, PickerManager.selectedFiles,
                                this)

                        recyclerView.adapter = audioListAdapter
                    } else {
                        audioListAdapter?.setData(dirs)
                        audioListAdapter?.notifyDataSetChanged()
                    }
                    onItemSelected()
                }
            } else {
                recyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.doc_picker_menu, menu)
        selectAllItem = menu.findItem(R.id.action_select)
        if (PickerManager.hasSelectAll()) {
            selectAllItem?.isVisible = true
            onItemSelected()
        } else {
            selectAllItem?.isVisible = false
        }

        val search = menu.findItem(R.id.search)
        val searchView = search?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {

                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                audioListAdapter?.filter?.filter(newText)
                return true
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.action_select) {
            audioListAdapter?.let { adapter ->
                selectAllItem?.let { menuItem ->
                    if (menuItem.isChecked) {
                        adapter.clearSelection()
                        PickerManager.clearSelections()

                        menuItem.setIcon(R.drawable.ic_deselect_all)
                    } else {
                        adapter.selectAll()
                        PickerManager
                                .add(adapter.selectedPaths, FilePickerConst.FILE_TYPE_AUDIO)
                        menuItem.setIcon(R.drawable.ic_select_all)
                    }

                    menuItem.isChecked = !menuItem.isChecked
                    mListener?.onItemSelected()
                }
            }
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    companion object {

        fun newInstance(fileType: FileType): AudioFragment {
            val audioPickerFragment = AudioFragment()
            val bun = Bundle()
            bun.putParcelable(BaseFragment.FILE_TYPE, fileType)
            audioPickerFragment.arguments = bun
            return audioPickerFragment
        }
    }


}