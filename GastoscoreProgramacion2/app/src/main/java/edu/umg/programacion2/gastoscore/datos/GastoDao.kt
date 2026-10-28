package edu.umg.programacion2.gastoscore.datos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao

interface GastoDao {

    @Insert
    suspend fun insertar(gasto: GastoEntity): Long

    @Query("SELECT * FROM gastos ORDER BY id")
    suspend fun listar(): List<GastoEntity>

    @Query("SELECT * FROM gastos WHERE id = :id")
    suspend fun buscarPorId(id: Int): GastoEntity?

    @Update
    suspend fun actualizar(gasto: GastoEntity): Int

    @Query("DELETE FROM gastos WHERE id = :id")
    suspend fun eliminar(id: Int): Int
}