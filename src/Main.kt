import java.util.*

class Point(val x: Double, val y: Double) {
    override fun toString() = "($x, $y)"
}

class Vector(val x: Double, val y: Double)

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

    fun inside(point: Point) : Boolean {
        val polygon = listOf(listOf(Point(0.0, 0.0)), points, listOf(Point(6999.0, 0.0))).flatten()
        var inside = false
        var i = 0
        var j = polygon.size - 1
        while (i < polygon.size) {
            val iPoint = polygon[i]
            val jPoint = polygon[j]
            val intersect = iPoint.y > point.y != jPoint.y > point.y && point.x < (jPoint.x - iPoint.x) * (point.y - iPoint.y) / (jPoint.y - iPoint.y) + iPoint.x
            if (intersect) inside = !inside
            j = i++
        }
        return inside
    }
}

class Lander(input: Scanner) {
    val position: Point
    val velocity: Vector
    val fuel: Int
    val rotate: Int
    val power: Int

    init {
        position = Point(input.nextDouble(), input.nextDouble())
        velocity = Vector(input.nextDouble(), input.nextDouble())
        fuel = input.nextInt()
        rotate = input.nextInt()
        power = input.nextInt()
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
        val lander = Lander(input)
        // rotate power. rotate is the desired rotation angle. power is the desired thrust power.
        println("-20 3")
    }
}

// Extensions

fun Double.randomInt(min: Int, max: Int) = min + (max * this)

// Gene

class Gene(
    val power: Int = Math.random().randomInt(0, 4)

)