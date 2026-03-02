import java.util.Locale
fun main() {
    val timeStr = String.format(Locale.ENGLISH, "%02d%02d%02d", 12, 34, 56)
    val map = mapOf('1' to "one", '2' to "two")
    println("timeStr: '$timeStr'")
    println("char0: '${timeStr[0]}'")
    println("map['1']: ${map['1']}")
    println("map[timeStr[0]]: ${map[timeStr[0]]}")
}
