package com.krew18.app.data.remote

import com.krew18.app.data.remote.dto.ApplicationDto
import com.krew18.app.data.remote.dto.ApplyRequest
import com.krew18.app.data.remote.dto.GigDto
import com.krew18.app.data.remote.dto.PostGigRequest
import com.krew18.app.data.remote.dto.StatusUpdateRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit description of a Krew18 backend. No server ships with this demo; the app uses
 * [FakeKrewRemoteDataSource] unless BuildConfig.USE_FAKE_BACKEND is set to false.
 */
interface KrewApi {
    @GET("gigs")
    suspend fun getGigs(): List<GigDto>

    @GET("applications")
    suspend fun getApplications(): List<ApplicationDto>

    @POST("gigs")
    suspend fun postGig(@Body request: PostGigRequest): GigDto

    @POST("gigs/{gigId}/applications")
    suspend fun apply(@Path("gigId") gigId: String, @Body request: ApplyRequest): ApplicationDto

    @PATCH("applications/{applicationId}")
    suspend fun updateApplicationStatus(
        @Path("applicationId") applicationId: String,
        @Body request: StatusUpdateRequest,
    ): ApplicationDto
}
