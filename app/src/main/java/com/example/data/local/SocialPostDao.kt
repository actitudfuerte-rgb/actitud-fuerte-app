package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SocialPost
import kotlinx.coroutines.flow.Flow

@Dao
interface SocialPostDao {

    @Query("SELECT * FROM social_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<SocialPost>>

    @Query("SELECT * FROM social_posts WHERE authorId = :authorId ORDER BY timestamp DESC")
    fun getPostsForAuthor(authorId: Long): Flow<List<SocialPost>>

    @Query("SELECT * FROM social_posts WHERE authorId = :authorId ORDER BY timestamp DESC")
    suspend fun getPostsForAuthorOnce(authorId: Long): List<SocialPost>

    @Query("SELECT * FROM social_posts ORDER BY timestamp DESC")
    suspend fun getAllPostsOnce(): List<SocialPost>

    @Query("SELECT * FROM social_posts WHERE id = :id LIMIT 1")
    suspend fun getPostById(id: Long): SocialPost?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: SocialPost): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<SocialPost>)

    @Update
    suspend fun updatePost(post: SocialPost)

    @Delete
    suspend fun deletePost(post: SocialPost)

    @Query("DELETE FROM social_posts WHERE id = :id")
    suspend fun deletePostById(id: Long)

    @Query("UPDATE social_posts SET likesCount = :likesCount WHERE id = :id")
    suspend fun updateLikesCountOnly(id: Long, likesCount: Int)

    @Query("UPDATE social_posts SET likesCount = :likesCount, isLikedByMe = :isLiked WHERE id = :id")
    suspend fun updateLike(id: Long, likesCount: Int, isLiked: Boolean)

    @Query("UPDATE social_posts SET authorAvatarUrl = :avatarUrl WHERE authorId = :authorId OR (authorAccessId != '' AND authorAccessId = :authorAccessId)")
    suspend fun updateAuthorAvatar(authorId: Long, authorAccessId: String, avatarUrl: String)
}
