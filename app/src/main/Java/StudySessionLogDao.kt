package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: StudySessionLogEntity)

    @Query("SELECT * FROM study_session_logs WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getLogsSince(startTime: Long): Flow<List<StudySessionLogEntity>>

    @Query("SELECT * FROM study_session_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<StudySessionLogEntity>>

    @Query("SELECT COUNT(*) FROM study_session_logs WHERE timestamp >= :startTime")
    fun getReviewCountSince(startTime: Long): Flow<Int>
}
