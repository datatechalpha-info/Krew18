package com.krew18.app.data.remote

import com.krew18.app.data.remote.dto.ApplicationDto
import com.krew18.app.data.remote.dto.ApplyRequest
import com.krew18.app.data.remote.dto.GigDto
import com.krew18.app.data.remote.dto.PostGigRequest
import com.krew18.app.data.remote.dto.StatusUpdateRequest
import javax.inject.Inject

/** Abstraction over the backend so the repository does not care whether it is real or fake. */
interface KrewRemoteDataSource {
    suspend fun fetchGigs(): List<GigDto>
    suspend fun fetchApplications(): List<ApplicationDto>
    suspend fun postGig(request: PostGigRequest): GigDto
    suspend fun apply(request: ApplyRequest): ApplicationDto
    suspend fun updateApplicationStatus(applicationId: String, status: String)
}

/** Production implementation backed by Retrofit. */
class RetrofitKrewRemoteDataSource @Inject constructor(
    private val api: KrewApi,
) : KrewRemoteDataSource {
    override suspend fun fetchGigs(): List<GigDto> = api.getGigs()
    override suspend fun fetchApplications(): List<ApplicationDto> = api.getApplications()
    override suspend fun postGig(request: PostGigRequest): GigDto = api.postGig(request)
    override suspend fun apply(request: ApplyRequest): ApplicationDto = api.apply(request.gigId, request)
    override suspend fun updateApplicationStatus(applicationId: String, status: String) {
        api.updateApplicationStatus(applicationId, StatusUpdateRequest(status))
    }
}
