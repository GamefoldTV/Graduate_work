package ru.netology.nework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.entity.JobEntity

@Dao
interface JobDao {
    @Query("SELECT * FROM JobEntity WHERE userId =:user_id ORDER BY start DESC")
    fun getJobsByIdUser(user_id: Long): Flow<List<JobEntity>>

    @Query("SELECT * FROM JobEntity ORDER BY start DESC")
    fun getJobs(): Flow<List<JobEntity>>

    @Query("SELECT COUNT(*) == 0 FROM JobEntity WHERE userId =:user_id")
    suspend fun isEmpty(user_id: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: JobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<JobEntity>)

    @Query("DELETE FROM JobEntity WHERE id = :id")
    suspend fun removeById(id: Long)
}