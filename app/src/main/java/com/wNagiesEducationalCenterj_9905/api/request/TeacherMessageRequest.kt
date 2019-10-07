package com.wNagiesEducationalCenterj_9905.api.request

import com.google.gson.annotations.SerializedName
import com.wNagiesEducationalCenterj_9905.vo.IMessageRequestModel

data class TeacherMessageRequest(
    @SerializedName("message")
    override val content: String
) : IMessageRequestModel{
    var title: String = "message from class teacher"
}