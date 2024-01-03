package be.heh.projet_mastock.DB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ProductTable")

data class ProductRecord (
    @ColumnInfo(name="id") @PrimaryKey(autoGenerate = true) var id : Int = 0,
    @ColumnInfo(name="type") var type : String,
    @ColumnInfo(name="brandModel") var brandModel : String,
    @ColumnInfo(name="refNumber") var refNumber : String,
    @ColumnInfo(name="webSite") var webSite : String,
    @ColumnInfo(name="isBorrow") var isBorrow : Boolean
)