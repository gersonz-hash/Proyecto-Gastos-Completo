
package edu.umg.programacion2.gastoscore.datos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface GastoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(gasto: GastoEntity): Long

    @Update
    suspend fun actualizar(gasto: GastoEntity): Int

    @Delete
    suspend fun eliminar(gasto: GastoEntity): Int

    @Query("SELECT * FROM gastos ORDER BY fecha DESC")
    suspend fun listar(): List<GastoEntity>

    @Query("SELECT * FROM gastos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): GastoEntity?

    @Query("SELECT * FROM gastos WHERE tipo = :tipo ORDER BY fecha DESC")
    suspend fun listarPorTipo(tipo: String): List<GastoEntity>

    @Query("SELECT COALESCE(SUM(monto), 0) FROM gastos WHERE tipo = :tipo")
    suspend fun totalPorTipo(tipo: String): Double
}
