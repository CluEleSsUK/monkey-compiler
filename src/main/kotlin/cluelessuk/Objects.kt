package cluelessuk

sealed class MObject(val type: String)
data class MInteger(val value: Int) : MObject("INTEGER")
data class MBoolean(val value: Boolean) : MObject("BOOLEAN") {
    companion object {
        fun of(input: Boolean): MBoolean {
            return if (input) True else False
        }
    }
}

data class MString(val value: String) : MObject("STRING")

val True = MBoolean(true)
val False = MBoolean(false)
