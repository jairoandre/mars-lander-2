import java.util.*

class Point(val x: Double, val y: Double) {
    override fun toString() = "($x, $y)"
}

class Vector(val x: Double, val y: Double) {
    fun toCartesian() = Vector(x * y.cos(), x * y.sin())
    fun toPolar() = Vector((x.sqr() + y.sqr()).sqrt(), y.atan2(x))
    fun add(other: Vector) = Vector(x + other.x, y + other.y)
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

    fun inside(point: Point): Boolean {
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

fun createLander(input: Scanner) = Lander(
    input.nextDouble(),
    input.nextDouble(),
    input.nextDouble(),
    input.nextDouble(),
    input.nextInt(),
    input.nextInt(),
    input.nextInt()
)

class Lander(x: Double, y: Double, vx: Double, vy: Double, val fuel: Int, val rotate: Int, val power: Int) {
    val position: Point
    val velocity: Vector

    init {
        position = Point(x, y)
        velocity = Vector(vx, vy)
    }

    fun update(gene: Gene): Lander {
        val newRotate = (rotate + gene.angle).coerceIn(-90..90)
        val newPower = (power + (gene.power - power).coerceIn(-1..1))
        val newFuel = fuel - newPower
        val thrust = Vector(newPower.toDouble(), newRotate.toDouble().toRadians()).toCartesian().add(gravity)
        val newVelocity = velocity.add(thrust)
        val newPosition = Point(position.x + newVelocity.x, position.y + newVelocity.y)
        return Lander(newPosition.x, newPosition.y, newVelocity.x, newVelocity.y, newFuel, newRotate, newPower)
    }
}

val gravity = Vector(0.0, -3.711)

fun debug(msg: String) = System.err.print(msg)

fun main(args: Array<String>) {
    (0..10).forEach { println(Gene()) }
}

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
fun main2(args: Array<String>) {
    val input = Scanner(System.`in`)
    val surfaceN = input.nextInt() // the number of points used to draw the surface of Mars.
    val surface = Surface((0 until surfaceN).map { Point(input.nextDouble(), input.nextDouble()) })

    debug(surface.plane.toString())

    // game loop
    while (true) {
        val lander = createLander(input)
        // rotate power. rotate is the desired rotation angle. power is the desired thrust power.
        println("-20 3")
    }
}

// Extensions

fun Double.sqr() = this * this
fun Double.sqrt() = Math.sqrt(this)
fun Double.cos() = Math.cos(this)
fun Double.sin() = Math.sin(this)
fun Double.atan2(x: Double) = Math.atan2(this, x)
fun Double.toRadians() = this * Math.PI / 180
fun Double.toDegrees() = this * 180 / Math.PI
fun Double.abs() = Math.abs(this)
fun Double.map(min: Int, max: Int) = min + ((max - min + 1) * this).toInt()

// Gene

class Gene(
    val power: Int = Math.random().map(0, 4),
    val angle: Int = Math.random().map(-15, 15),
    val turns: Int = Math.random().map(1, 5)
) {
    override fun toString(): String {
        return "$power | $angle | $turns"
    }
}