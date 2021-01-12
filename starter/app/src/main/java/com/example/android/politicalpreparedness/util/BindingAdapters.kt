package com.example.android.politicalpreparedness.util

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import java.text.SimpleDateFormat
import java.util.*


@BindingAdapter("date")
fun setDateToString(textView: TextView, voterInfo: VoterInfoResponse?) {
    voterInfo?.let {
        val format = SimpleDateFormat("EEEE, dd MMMM yyyy, HH:mm:ss z", Locale.getDefault())
        textView.text = format.format(it.election.electionDay)
    }
}

@BindingAdapter("address")
fun setAddress(textView: TextView, voterInfo: VoterInfoResponse?) {
    voterInfo?.let {
        if (!voterInfo.state.isNullOrEmpty()) {
            textView.text = voterInfo.state.first().electionAdministrationBody.correspondenceAddress?.toFormattedString()
        }
    }
}