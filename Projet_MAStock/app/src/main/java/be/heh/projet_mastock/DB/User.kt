package be.heh.projet_mastock.DB

class User (i : Int) {
    var id : Int = 0
        private set

    var email : String = "null"
        private set

    var pwd : String = "null"
        private set

    var isEnable : Boolean = false
        private set

    var isAdmin : Boolean = false
        private set

    constructor(i : Int, e : String, p : String, ie : Boolean = false, ia : Boolean = false) : this(i) {
        id = i
        email = e
        pwd = p
        isEnable = ie
        isAdmin = ia
    }

    override fun toString(): String {
        return "ID: $id\n" +
                "Email: $email\n" +
                "Password: $pwd\n" +
                "Is enable: $isEnable\n" +
                "Is admin: $isAdmin"
    }
}