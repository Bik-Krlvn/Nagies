package com.cheise_proj.remote_source

import com.cheise_proj.data.model.user.UserData
import com.cheise_proj.data.source.RemoteSource
import com.cheise_proj.remote_source.api.ApiService
import com.cheise_proj.remote_source.mapper.UserDtoDataMapper
import io.reactivex.Observable
import javax.inject.Inject

class RemoteSourceImpl @Inject constructor(
    private val apiService: ApiService,
    private val userDtoDataMapper: UserDtoDataMapper
) : RemoteSource {
    companion object {
        const val OK_STATUS = 200
    }

    override fun authenticateUser(
        role: String,
        username: String,
        password: String
    ): Observable<UserData> {
        return apiService.getAuthenticateUser(role, username, password).map {
            if (it.status == OK_STATUS) {
                return@map userDtoDataMapper.dtoToData(it)
            }
            return@map null
        }
    }

}