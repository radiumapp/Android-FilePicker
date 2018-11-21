package droidninja.filepicker.adapters

import android.content.Context
import android.net.Uri
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.PickerManager
import droidninja.filepicker.R
import droidninja.filepicker.models.Document
import droidninja.filepicker.views.SmoothCheckBox
import java.util.ArrayList
import droidninja.filepicker.utils.MediaPlayerManager

class AudioListAdapter(private val context: Context, private var mFilteredList: List<Document>, selectedPaths: List<String>,
                       private val mListener: FileAdapterListener?) : SelectableAdapter<AudioListAdapter.AudioViewHolder,
        Document>(mFilteredList, selectedPaths), Filterable {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_audio_layout, parent, false)

        return AudioViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return mFilteredList.size
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val document = mFilteredList[position]

        holder.imageView.setImageResource(R.drawable.ic_play)

        holder.fileNameTextView.text = document.title
        holder.fileSizeTextView.text = Formatter.formatShortFileSize(context, java.lang.Long.parseLong(document.size))

        if (document.duration != null) {
            holder.fileTimeTextView.visibility = View.VISIBLE
            holder.fileTimeTextView.text = " - " + String.format("%02d:%02d", document.duration / 60, document.duration % 60)
        } else {
            holder.fileTimeTextView.visibility = View.INVISIBLE
        }

        holder.layoutContent.setOnClickListener { onItemClicked(document, holder) }

        //in some cases, it will prevent unwanted situations
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.setOnClickListener { onItemClicked(document, holder) }

        holder.imageView.setOnClickListener {
            if (MediaPlayerManager.getInstance().isPlaying) {
                MediaPlayerManager.getInstance().stop()
                holder.imageView.setImageResource(R.drawable.ic_play)
            } else {
                MediaPlayerManager.getInstance().play(context, Uri.parse(document.path))
                holder.imageView.setImageResource(R.drawable.ic_stop)
            }
        }

        //if true, your checkbox will be selected, else unselected
        holder.checkBox.isChecked = isSelected(document)

        holder.itemView.setBackgroundResource(
                if (isSelected(document)) R.color.bg_gray else android.R.color.white)
        holder.checkBox.visibility = if (isSelected(document)) View.VISIBLE else View.INVISIBLE

        holder.checkBox.setOnCheckedChangeListener(object : SmoothCheckBox.OnCheckedChangeListener {
            override fun onCheckedChanged(checkBox: SmoothCheckBox, isChecked: Boolean) {
                toggleSelection(document)
                holder.itemView.setBackgroundResource(if (isChecked) R.color.bg_gray else android.R.color.white)
            }
        })
    }

    private fun onItemClicked(document: Document, holder: AudioViewHolder) {
        if (java.lang.Long.parseLong(document.size) > 10000000) {
            Toast.makeText(context, "" + context.resources.getString(R.string.out_size), Toast.LENGTH_SHORT).show()
        } else {
            if (PickerManager.getMaxCount() == 1) {
                PickerManager.add(document.path, FilePickerConst.FILE_TYPE_AUDIO)
            } else {
                if (holder.checkBox.isChecked) {
                    PickerManager.remove(document.path, FilePickerConst.FILE_TYPE_AUDIO)
                    holder.checkBox.setChecked(!holder.checkBox.isChecked, true)
                    holder.checkBox.visibility = View.INVISIBLE
                } else if (PickerManager.shouldAdd()) {
                    PickerManager.add(document.path, FilePickerConst.FILE_TYPE_AUDIO)
                    holder.checkBox.setChecked(!holder.checkBox.isChecked, true)
                    holder.checkBox.visibility = View.VISIBLE
                }
            }
            mListener?.onItemSelected()
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): Filter.FilterResults {

                val charString = charSequence.toString()

                if (charString.isEmpty()) {

                    mFilteredList = items
                } else {

                    val filteredList = ArrayList<Document>()

                    for (document in items) {

                        if (document.title.toLowerCase().contains(charString)) {

                            filteredList.add(document)
                        }
                    }

                    mFilteredList = filteredList
                }

                val filterResults = Filter.FilterResults()
                filterResults.values = mFilteredList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: Filter.FilterResults) {
                mFilteredList = filterResults.values as List<Document>
                notifyDataSetChanged()
            }
        }
    }

    class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var layoutContent: RelativeLayout
        var checkBox: SmoothCheckBox
        var imageView: ImageView
        var fileNameTextView: TextView
        var fileSizeTextView: TextView
        var fileTimeTextView: TextView

        init {
            layoutContent = itemView.findViewById(R.id.layoutContent)
            checkBox = itemView.findViewById(R.id.checkbox)
            imageView = itemView.findViewById(R.id.file_iv)
            fileNameTextView = itemView.findViewById(R.id.file_name_tv)
            fileSizeTextView = itemView.findViewById(R.id.file_size_tv)
            fileTimeTextView = itemView.findViewById(R.id.file_time_tv)
        }
    }

}

