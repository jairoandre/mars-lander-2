import java.io.StringReader
import java.util.*

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/

fun main(args: Array<String>) {
    val input2 = """2
0 100
6999 100
"""
    val inputGround = """6
0 1500
1000 2000
2000 500
3500 500
5000 1500
6999 1000"""
    val input = Scanner(StringReader(input2))
    val surfaceN = input.nextInt()
    val points = (0 until surfaceN).map { Point(input.nextDouble(), input.nextDouble()) }
    val surface = Surface((1 until points.size).map { idx -> SurfaceSegment(points[idx - 1], points[idx]) })
    val landerStr = "5000 2500 -50 0 1000 90 0"
    val inputLander = Scanner(StringReader(landerStr))
    val lander = Lander(
        pos = Point(inputLander.nextDouble(), inputLander.nextDouble()),
        vel = Point(inputLander.nextDouble(), inputLander.nextDouble()),
        fuel = inputLander.nextInt(),
        rotate = inputLander.nextInt(),
        power = inputLander.nextInt()
    )
    val gaRunner = GARunner(State(surface, lander))
    val b = Date().time
    val actions = gaRunner.run()
    val e = Date().time
    println("${e - b} ms")
}

fun main2(args: Array<String>) {
    val input = Scanner(System.`in`)
    val surfaceN = input.nextInt() // the number of points used to draw the surface of Mars.
    val points = (0 until surfaceN).map { Point(input.nextDouble(), input.nextDouble()) }
    val surface = Surface((1 until points.size).map { idx -> SurfaceSegment(points[idx - 1], points[idx]) })

    var t = 0
    var actions: List<Action> = emptyList()

    while (true) {

        val lander = Lander(
            pos = Point(input.nextDouble(), input.nextDouble()),
            vel = Point(input.nextDouble(), input.nextDouble()),
            fuel = input.nextInt(),
            rotate = input.nextInt(),
            power = input.nextInt()
        )

        // rotate power. rotate is the desired rotation angle. power is the desired thrust power.
        if (t == 0) {
            val gaRunner = GARunner(State(surface, lander))
            actions = gaRunner.run()
        }

        if (t < actions.size)
            actions[t].print()
        else {
            println("0 0")
            debug("Out of moves")
        }
        t += 1
    }
}

val gravityForce = Point(3.711, 270.0.toRadians())

fun Double.sqr() = this * this
fun Double.sqrt() = Math.sqrt(this)
fun Double.cos() = Math.cos(this)
fun Double.sin() = Math.sin(this)
fun Double.atan2(x: Double) = Math.atan2(this, x)
fun Double.toRadians() = this * Math.PI / 180
fun Double.toDegrees() = this * 180 / Math.PI
fun Double.abs() = Math.abs(this)
fun Double.toRandomInt(max: Int) = (this * max).toInt()
fun Double.inRange(min: Int, max: Int) = min + ((max - min) * this).toInt()

class Point(
    val x: Double,
    val y: Double) {
    fun add(point: Point) = Point(x + point.x, y + point.y)
    fun sub(point: Point) = Point(x - point.x, y - point.y)
    fun mag2() = x.sqr() + y.sqr()
    fun mag() = mag2().sqrt()
    fun distance2(point: Point) = sub(point).mag2()
    fun distance(point: Point) = distance2(point).sqrt()
    fun toPolar() = Point(mag(), y.atan2(x))
    fun toCartesian() = Point(x * y.cos(), x * y.sin())
    override fun toString(): String = "($x, $y)"
}

class Action(
    val angle: Int,
    val power: Int) {
    fun print() = println("$angle $power")
}

class Lander(
    val pos: Point,
    val vel: Point,
    val fuel: Int,
    val rotate: Int,
    val power: Int) {
    private fun copy(pos: Point = this.pos, vel: Point = this.vel, fuel: Int = this.fuel, rotate: Int = this.rotate, power: Int = this.power) = Lander(pos, vel, fuel, rotate, power)
    /**
     * Return a copy of this lander with position, fuel updated.
     */
    fun update(time: Double) = copy(pos = Point(pos.x + vel.x * time, pos.y + vel.y * time), fuel = (fuel - power * time).toInt().coerceAtLeast(0))

    /**
     * Return a copy of this lander with velocity, rotate and power updated by a thrust.
     */
    fun thrust(action: Action): Lander {
        val newPower = power + (action.power - power).coerceIn(-1..1)
        val newRotate = rotate + (action.angle - rotate).coerceIn(-15..15)
        val acceleration = Point(newPower.toDouble(), (90.0 - newRotate).toRadians())
        val composed = acceleration.add(gravityForce).toCartesian()
        return copy(vel = composed, rotate = newRotate, power = newPower)
    }

    override fun toString(): String =
        "Pos: ${pos.x}, ${pos.y} || Fuel: $fuel || Vel: ${vel.x} ${vel.y}"
}

class SurfaceSegment(
    val a: Point,
    val b: Point) {
    val polar = b.sub(a).toPolar()
    fun inRangeX(point: Point) = a.x >= point.x && b.x <= point.x
    fun yForX(x: Double) = x * polar.y.sin() / polar.y.cos()
    fun isBellowLine(point: Point) = point.y < yForX(point.x)
    fun isAboveLine(point: Point) = point.y > yForX(point.x)
    fun isInLine(point: Point) = point.y == yForX(point.x)
    fun isPlane() = a.y == b.y
    fun isLanded(lander: Lander) = isPlane() && lander.pos.y == a.y && lander.vel.x.abs() <= 20 && lander.vel.y.abs() <= 40 && lander.rotate == 0
    fun crash(lander: Lander): Boolean {
        return if (inRangeX(lander.pos)) {
            val y = yForX(lander.pos.x)
            if (y == lander.pos.y) {
                lander.vel.x.abs() > 20 || lander.vel.y.abs() > 40 || lander.rotate != 0
            } else y > lander.pos.y
        } else {
            false
        }
    }

    fun distToPoint(p: Point): Double {
        val dd = Math.abs((b.y - a.y) * p.x + (b.x - a.x) * p.y + b.x * a.y - b.y * a.x)
        val dv = Math.sqrt((b.y - a.y) * (b.y - a.y) + (b.x - a.x) * (b.x - a.x))
        return dd / dv
    }

    override fun toString(): String =
        "Surface from $a to $b"
}

class Surface(
    val segments: List<SurfaceSegment>
) {
    val planeSegments: List<SurfaceSegment> = segments.filter { it.isPlane() }

    init {
        debug("$planeSegments")
    }
}

const val MAX_X = 6999f
const val MIN_X = 0f

enum class LanderStatus { FLYING, LANDED, DEAD }
class State(
    val surface: Surface,
    val lander: Lander
) {
    var status: LanderStatus

    init {
        status = when (true) {
            isDead() -> {
                LanderStatus.DEAD
            }
            isLanded() -> {
                LanderStatus.LANDED
            }
            else -> LanderStatus.FLYING

        }
    }

    private fun nextState(action: Action): State =
        State(surface, lander.thrust(action).update(1.0))


    private fun isDead() = checkOut() || surface.segments.map { it.crash(lander) }.reduce { acc, curr -> acc || curr }
    fun isLanded() = surface.planeSegments.map { it.isLanded(lander) }.reduce { acc, curr -> acc || curr }

    fun computeStates(actions: List<Action>): State {
        // debug("First state $lander")
        var current = this
        for (action in actions) {
            if (current.status == LanderStatus.FLYING) current = current.nextState(action) else break
        }
        return current
    }

    private fun checkOut() = lander.pos.x > MAX_X || lander.pos.x < MIN_X
}

// GA

const val GENOME_SIZE = 1200
const val POPULATION_SIZE = 20
const val GENERATIONS = 30
const val ELITISM = true
const val SELECTION_RATE = .4
const val UNIFORM_RATE = .5
const val MUTATION_RATE = .05

val possibleAngles = (-10..10).toList()
val possibleAnglesSize = possibleAngles.size
val possibleThrusts = (0..4).toList()
val possibleThrustsSize = possibleThrusts.size

class Gene(
    private val a: Double = 0.0, //Math.random(),
    private val b: Double = Math.random(),
    private val c: Int = 1 //Math.random().inRange(1, 5)
) {
    fun toAction() = (1..c).map { Action(
        0, //possibleAngles[a.toRandomInt(possibleAnglesSize)],
        possibleThrusts[b.toRandomInt(possibleThrustsSize)]) }

}

class Genome(
    val genes: Array<Gene> = Array(GENOME_SIZE) { Gene() }
) {
    fun toActions() = genes.map { it.toAction() }.flatten()

    private fun mutate(gene: Gene): Gene = if (Math.random() <= MUTATION_RATE) Gene() else gene

    fun crossover(partner: Genome): Genome {
        val newGenes = Array(GENOME_SIZE) {
            if (Math.random() <= UNIFORM_RATE) {
                mutate(this.genes[it])
            } else {
                mutate(partner.genes[it])
            }
        }
        return Genome(newGenes)
    }
}

val MAX_VALUE = Double.MAX_VALUE - 100

class GenomeState(private val initialState: State, val genome: Genome) {

    var fitness: Double? = null
    var state: LanderStatus = initialState.status

    fun simulate(): GenomeState {
        val lastState = initialState.computeStates(genome.toActions())
        state = lastState.status
        fitness = when (lastState.status) {
            LanderStatus.FLYING -> with(lastState.lander) { - lastState.surface.planeSegments.first().distToPoint(pos) + lastState.lander.fuel }
            LanderStatus.DEAD -> -100000.0
            LanderStatus.LANDED -> lastState.lander.fuel.toDouble()
        }
        return this
    }

}

fun debug(msg: String) = System.err.println(msg)

class GARunner(private val initState: State) {

    var population = Array(POPULATION_SIZE) { GenomeState(initState, Genome()) }

    val elitismOffset = if (ELITISM) 0 else 1

    private fun select(population: List<GenomeState>): GenomeState {
        population.forEachIndexed { i, sample ->
            if (Math.random() <= SELECTION_RATE * (POPULATION_SIZE - i) / POPULATION_SIZE) {
                return sample
            }
        }
        return population.first()
    }

    fun run(): List<Action> {
        var best: GenomeState?
        var t = 1
        while (true) {
            //for (i in 1..GENERATIONS) {
            val b = Date().time
            population.forEach { it.simulate() }
            val e = Date().time
            //debug("${e - b} ms")
            best = population.sortedBy { it.fitness }.last()
            if (best.state == LanderStatus.LANDED) {
                break
            }
            //debug(population.joinToString { genomeState ->  genomeState.fitness.toString() })
            val orderedByFitness = population.sortedBy { it.fitness }.reversed().drop(elitismOffset)
            val newPopulation = Array(POPULATION_SIZE) {
                val genome1 = select(orderedByFitness).genome
                val genome2 = select(orderedByFitness).genome
                GenomeState(initState, genome1.crossover(genome2))
            }
            population = newPopulation
            if (t % 200 == 0) debug("${best.fitness}")
            t += 1
        }
        println("$t generations")
        return best!!.genome.genes.map { it.toAction() }.flatten()

    }

}