package com.windstrom5.tugasakhir.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.windstrom5.tugasakhir.R
import com.windstrom5.tugasakhir.model.Dinas

class DinasExpandablePekerjaListAdapter(
    private val context: Context,
    private val dinasList: List<Dinas>
) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return dinasList.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        // Assuming that each parent has only one child
        return 1
    }

    override fun getGroup(groupPosition: Int): Any {
        return dinasList[groupPosition].status
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return dinasList[groupPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var convertView = convertView

        if (convertView == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.list_group, null)
        }

        val groupTitle = convertView!!.findViewById<TextView>(R.id.title)

        // Assuming status is a String
        val headerTitle = getGroup(groupPosition) as String
        groupTitle.text = headerTitle

        return convertView
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var convertView = convertView

        if (convertView == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.history_dinas, null)
        }

        val cardView = convertView!!.findViewById<CardView>(R.id.cardView)
        val tanggalTextView = convertView.findViewById<TextView>(R.id.tanggal)
        val tujuanTextView = convertView.findViewById<TextView>(R.id.tujuan)
        val actionButton = convertView.findViewById<Button>(R.id.actionButton)

        // Assuming there is a status field in Dinas model
        val parentStatus: String = dinasList[groupPosition].status

        // Find the relevant child data based on the parent's status
        val filteredChildDataList: List<Dinas> = dinasList.filter { it.status == parentStatus }

        // Use the first element from filteredChildDataList to set the child view
        if (filteredChildDataList.isNotEmpty()) {
            val filteredChildData: Dinas = filteredChildDataList[0]
            tanggalTextView.text = "${filteredChildData.tanggal_berangkat} - ${filteredChildData.tanggal_pulang}"
            tujuanTextView.text = filteredChildData.tujuan
            // Set other data as needed
        }

        return convertView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return false
    }
}
