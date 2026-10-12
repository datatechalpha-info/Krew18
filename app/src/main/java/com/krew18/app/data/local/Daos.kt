package com.krew18.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface GigDao {
    @Query("SELECT * FROM gigs ORDER BY startEpochDay ASC, id ASC")
    fun observeGigs(): Flow<List<GigEntity>>

    @Query("SELECT COUNT(*) FROM gigs")
    suspend fun countGigs(): Int

    @Upsert
    suspend fun upsertGigs(gigs: List<GigEntity>)

    @Query("SELECT * FROM applications ORDER BY appliedAtEpochMillis DESC, id ASC")
    fun observeApplications(): Flow<List<ApplicationEntity>>

    @Upsert
    suspend fun upsertApplications(applications: List<ApplicationEntity>)

    @Query("UPDATE applications SET status = :status WHERE id = :applicationId")
    suspend fun updateApplicationStatus(applicationId: String, status: String)
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM talent_profile WHERE id = 0")
    fun observeProfile(): Flow<ProfileEntity?>

    @Upsert
    suspend fun upsertProfile(profile: ProfileEntity)

    @Query("SELECT * FROM session WHERE id = 0")
    fun observeSession(): Flow<SessionEntity?>

    @Query("SELECT * FROM session WHERE id = 0")
    suspend fun getSession(): SessionEntity?

    @Upsert
    suspend fun upsertSession(session: SessionEntity)
}
