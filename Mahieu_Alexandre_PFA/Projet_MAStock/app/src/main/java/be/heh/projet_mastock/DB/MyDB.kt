package be.heh.projet_mastock.DB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [UserRecord::class, ProductRecord::class], version = 1)

abstract class MyDB : RoomDatabase() {

    abstract fun userDao() : UserDao
    abstract fun productDao() : ProductDao

    companion object {

        @Volatile
        private var INSTANCE : MyDB? = null

        fun getDB(context: Context): MyDB {
            val tempInstance = INSTANCE
            if(tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyDB::class.java,
                    "MyDataBase"
                ).build()

                INSTANCE = instance
                return instance
            }
        }
    }
}