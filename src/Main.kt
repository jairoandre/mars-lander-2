import java.util.*

class Point(val x: Double, val y: Double) {
    override fun toString() = "($x, $y)"
}

class Line(val a: Point, val b: Point) {
    fun isPlane() = a.y == b.y
    override fun toString() = "a: $a | b: $b"
}

class Surface(val points: List<Point>) {
    val lines: List<Line>
    val plane: Line

    init {
        lines = points.drop(1).dropLast(2).mapIndexed { index, point -> Line(point, points[index]) }
        plane = lines.first { it.isPlane() }
    }
}

fun debug(msg: String) = System.err.print(msg)

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
fun main(args: Array<String>) {
    val input = Scanner(System.`in`)
    val surfaceN = input.nextInt() // the number of points used to draw the surface of Mars.
    val surface = Surface((0 until surfaceN).map { Point(input.nextDouble(), input.nextDouble()) })
    debug(surface.plane.toString())

    // game loop
    while (true) {
        val X = input.nextInt()
        val Y = input.nextInt()
        val hSpeed = input.nextInt() // the horizontal speed (in m/s), can be negative.
        val vSpeed = input.nextInt() // the vertical speed (in m/s), can be negative.
        val fuel = input.nextInt() // the quantity of remaining fuel in liters.
        val rotate = input.nextInt() // the rotation angle in degrees (-90 to 90).
        val power = input.nextInt() // the thrust power (0 to 4).

        // Write an action using println()
        // To debug: System.err.println("Debug messages...");


        // rotate power. rotate is the desired rotation angle. power is the desired thrust power.
        println("-20 3")
    }
}