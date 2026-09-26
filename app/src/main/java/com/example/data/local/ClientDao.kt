package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Client
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY id DESC")
    fun getAllClients(): Flow<List<Client>>

    @Query("SELECT * FROM clients")
    suspend fun getAllClientsOnce(): List<Client>

    @Query("SELECT * FROM clients WHERE id = :id")
    fun getClientById(id: Long): Flow<Client?>

    @Query("SELECT * FROM clients WHERE id = :id")
    suspend fun getClientByIdOnce(id: Long): Client?

    @Query("SELECT * FROM clients WHERE operationalStatus = :status ORDER BY fullName ASC")
    fun getClientsByStatus(status: String): Flow<List<Client>>

    @Query("SELECT * FROM clients WHERE fullName LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY fullName ASC")
    fun searchClients(query: String): Flow<List<Client>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: Client): Long

    @Update
    suspend fun updateClient(client: Client)

    @Query("UPDATE clients SET operationalStatus = :newStatus WHERE id = :id")
    suspend fun updateOperationalStatus(id: Long, newStatus: String)

    @Delete
    suspend fun deleteClient(client: Client)

    @Query("DELETE FROM clients WHERE id = :id")
    suspend fun deleteClientById(id: Long)

    @Query("SELECT COUNT(*) FROM clients")
    fun getTotalClientsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM clients WHERE operationalStatus = 'Activo'")
    fun getActiveClientsCount(): Flow<Int>
}
