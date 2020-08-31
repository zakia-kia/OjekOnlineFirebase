package com.zakia.idn.ojekonlinefirebase.network

import com.google.gson.annotations.SerializedName
import com.zakia.idn.ojekonlinefirebase.model.Booking

class RequestNotification {
    @SerializedName("to")
    var token : String? = null

    @SerializedName("data")
    var sendNotificationModel : Booking? = null
}