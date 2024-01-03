package be.heh.projet_mastock.DB

import java.io.Serializable

class Product(i: Int) : Serializable {
    var id: Int = 0
        private set

    var type: String = ""
        private set

    var brandModel: String = ""
        private set

    var refNumber: String = ""
        private set

    var webSite: String = ""
        private set

    var isBorrow: Boolean = false
        private set

    constructor(i: Int, t: String, bm: String, rn: String, ws: String, ib: Boolean) : this(i) {
        id = i
        type = t
        brandModel = bm
        refNumber = rn
        webSite = ws
        isBorrow = ib
    }

    override fun toString(): String {
        return "ID: $id\n" +
                "Type: $type\n" +
                "Brand Model: $brandModel\n" +
                "Reference Number: $refNumber\n" +
                "Web Site: $webSite\n" +
                "Is borrow: $isBorrow"
    }
}