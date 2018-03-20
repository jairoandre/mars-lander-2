import java.util.*

class Point(val x: Double, val y: Double) {
    override fun toString() = "($x, $y)"
}

class Vector(val x: Double, val y: Double) {
    fun toCartesian() = Vector(x * y.cos(), x * y.sin())
    fun toPolar() = Vector((x.sqr() + y.sqr()).sqrt(), y.atan2(x))
    fun add(other: Vector) = Vector(x + other.x, y + other.y)
    fun mag() = (x.sqr() + y.sqr()).sqrt()
}

class Line(val a: Point, val b: Point) {
    fun inRangeX(point: Point) = (point.x >= a.x && point.x <= b.x) || (point.x >= b.x && point.x <= a.x)
    fun isPlane() = a.y == b.y
    fun dist(point: Point): Double {
        val dy = b.y - a.y
        val dx = b.x - b.y
        val n = (point.x * dy - point.y * dx + b.x * a.y - b.y * a.x).abs()
        val d = (dy.sqr() + dx.sqr()).sqrt()
        return n / d
    }

    override fun toString() = "a: $a | b: $b"
}

class Surface(val points: List<Point>) {
    val lines: List<Line>
    val plane: Line

    init {
        lines = points.dropLast(1).mapIndexed { index, point -> Line(point, points[index + 1]) }
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

fun createLander(input: Scanner, status: Status? = null) = Lander(
    input.nextDouble(),
    input.nextDouble(),
    input.nextDouble(),
    input.nextDouble(),
    input.nextInt(),
    input.nextInt(),
    input.nextInt(),
    status ?: Status.FLYING
)

enum class Status { FLYING, CRASHED, LANDED }

class Lander(x: Double, y: Double, vx: Double, vy: Double, val fuel: Int, val rotate: Int, val power: Int, val status: Status = Status.FLYING) {
    val position: Point
    val velocity: Vector

    init {
        position = Point(x, y)
        velocity = Vector(vx, vy)
    }

    fun update(gene: Gene, surface: Surface): Lander {
        return if (gene.turns > 0 && status == Status.FLYING) {
            val newRotate = (rotate + (gene.angle - rotate).coerceIn(-15..15)).coerceIn(-90..90)
            val newPower = (power + (gene.power - power).coerceIn(-1..1))
            val newFuel = fuel - newPower
            val thrust = Vector(newPower.toDouble(), (90 + newRotate).toDouble().toRadians()).toCartesian().add(gravity)
            val newVelocity = velocity.add(thrust)
            val newPosition = Point(position.x + newVelocity.x, position.y + newVelocity.y)
            val status = if (surface.inside(newPosition)) {
                if (surface.plane.inRangeX(newPosition) && newRotate == 0 && newVelocity.x.abs() <= 20 && newVelocity.y.abs() <= 40)
                    Status.LANDED else Status.CRASHED
            } else Status.FLYING
            Lander(
                newPosition.x,
                newPosition.y,
                newVelocity.x,
                newVelocity.y,
                newFuel,
                newRotate,
                newPower,
                status).update(gene.copy(turns = gene.turns - 1), surface)
        } else {
            this
        }

    }
}

val gravity = Vector(0.0, -3.711)

fun debug(msg: String) = System.err.println(msg)

fun main(args: Array<String>) {
    val t = (0 until 4).map { Point(it.toDouble(), it.toDouble()) }
    debug("$t")
}
/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
fun main1(args: Array<String>) {
    val input = Scanner(System.`in`)
    val surfaceN = input.nextInt() // the number of points used to draw the surface of Mars.
    val surface = Surface((0 until surfaceN).map { Point(input.nextDouble(), input.nextDouble()) })
    // game loop
    var t = 0
    var actions = emptyList<Gene>()
    while (true) {
        val lander = createLander(input)
        if (t == 0) {
            val runner = GARunner(surface, lander)
            actions = runner.simulate()
        }

        if (t >= actions.size) actions[t].print() else println("0 0")
        // rotate power. rotate is the desired rotation angle. power is the desired thrust power.
        t += 1
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

data class Gene(
    val power: Int = Math.random().map(0, 4),
    val angle: Int = Math.random().map(-90, 90),
    val turns: Int = Math.random().map(1, 5)
) {
    fun mutate() = if (Math.random() < MUTATION_RATE) Gene() else this

    override fun toString(): String {
        return "$power | $angle | $turns"
    }

    fun print() {
        println("$angle $power")
    }
}

const val DNA_SIZE = 1200
const val POPULATION_SIZE = 20
const val UNIFORM_RATE = 0.5
const val SELECTION_RATE = 0.4
const val MUTATION_RATE = 0.06
const val GENERATIONS = 50

class Dna(
    val genes: List<Gene> = (0 until DNA_SIZE).map { Gene() }
) {
    fun crossover(partner: Dna) =
        Dna(genes = genes.mapIndexed { idx, m -> if (Math.random() < UNIFORM_RATE) m.mutate() else partner.genes[idx].mutate() })

    fun toActions() =
        genes.map { gene -> List(gene.turns) { gene } }.flatten()
}

class State(
    private val surface: Surface,
    lander: Lander,
    val dna: Dna
) {
    val fitness: Double

    init {
        var l = lander
        dna.genes.forEach {
            l = l.update(it, surface)
            if (l.status != Status.FLYING) return@forEach
        }
        fitness = when (l.status) {
            Status.FLYING -> -(surface.plane.dist(l.position))
            Status.CRASHED -> -(surface.plane.dist(l.position) + l.fuel)
            else -> l.fuel.toDouble()
        }

    }
}

class GARunner(private val surface: Surface, private val lander: Lander) {
    var population = emptyList<State>()

    private fun select(orderedPopulation: List<State>): State {
        var result = population.first()
        orderedPopulation.forEachIndexed { idx, state ->
            if (Math.random() <= SELECTION_RATE * (POPULATION_SIZE - idx) / POPULATION_SIZE) {
                result = state
                return@forEachIndexed
            }
        }
        return result
    }

    fun simulate(): List<Gene> {
        // First simulation
        population = Array(POPULATION_SIZE) { State(surface, lander, Dna()) }.sortedBy { it.fitness }.reversed()

        (0 until GENERATIONS - 1).forEach {
            val newPopulation = Array(POPULATION_SIZE) {
                val partner1 = select(population).dna
                val partner2 = select(population).dna
                State(surface, lander, partner1.crossover(partner2))
            }.sortedBy { it.fitness }.reversed()
            population = newPopulation
        }

        val best = population.first().dna

        return best.toActions()

    }


}