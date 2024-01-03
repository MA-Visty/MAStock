package be.heh.projet_mastock.DB

import androidx.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM UserTable")
    fun get() : List<UserRecord>

    @Query("SELECT * FROM UserTable WHERE email = :email")
    fun getUser(email : String) : UserRecord

    @Query("SELECT * FROM usertable WHERE isAdmin = :isAdmin")
    fun getAdmin(isAdmin : Boolean = true) : UserRecord

    @Insert
    fun insertUser(vararg listCategories : UserRecord)

    @Update
    fun updateUser(task : UserRecord)

    @Delete
    fun deleteUser(task : UserRecord)
}