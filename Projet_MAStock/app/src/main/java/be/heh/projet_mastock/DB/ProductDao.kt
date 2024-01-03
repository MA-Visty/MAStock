package be.heh.projet_mastock.DB

import androidx.room.*

@Dao
interface ProductDao {
    @Query("SELECT * FROM ProductTable")
    fun get() : List<ProductRecord>

    @Query("SELECT * FROM ProductTable WHERE refNumber = :refNumber")
    fun getProduct(refNumber : String) : ProductRecord

    @Insert
    fun insertProduct(vararg listCategories : ProductRecord)

    @Update
    fun updateProduct(task : ProductRecord)

    @Delete
    fun deleteProduct(task : ProductRecord)
}
