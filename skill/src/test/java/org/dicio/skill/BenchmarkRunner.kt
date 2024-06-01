package org.dicio.skill

import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlin.time.measureTime

const val saveFolder = "benchmarks/999_current"

fun FunSpec.benchmarkContext(
    name: String,
    scoreFunction: (String) -> Unit,
    f: suspend BenchmarkRunner.() -> Unit
) {
    context(name) {
        val runner = BenchmarkRunner(this, name, scoreFunction)
        f(runner)
        runner.saveJson()
    }
}

class BenchmarkRunner(
    private val funSpec: FunSpecContainerScope,
    private val name: String,
    private val scoreFunction: (String) -> Unit,
) {
    private var incrementalJsonData = "[]"
    private var benchmarksJsonData = ArrayList<String>()

    fun warmup(vararg inputs: String) {
        for (input in inputs) {
            scoreFunction(input)
        }
    }

    suspend fun runBenchmarks(vararg inputs: String) {
        for (input in inputs) {
            funSpec.test("INPUT = $input") {
                val time = getBenchmarkTime(input)
                println("[$name] [$input] $time")
                benchmarksJsonData.add("""{"input": "${
                    input.replace("\\", "\\\\").replace("\"", "\\\"")
                }", "time": ${
                    time.inWholeNanoseconds
                }}""")
            }
        }
    }

    suspend fun runIncrementalBenchmarks() {
        funSpec.test("INCREMENTAL") {
            val maxDuration = 1.seconds
            val wantedPoints = 20
            val results = ArrayList<Pair<Int, Duration>>()
            var input = ""
            results.add(Pair(0, measureTime { scoreFunction(input) }))

            var skipUntilSize = 1
            // limit increment initially, since at small values the slope is not reliable
            var maxIncrement = 1.0f
            var prevIncrement = 1.0f
            while (results.last().second < maxDuration) {
                input += if (input.length % 4 == 3) " " else "a"
                if (input.length >= skipUntilSize) {
                    val (prevSize, prevTime) = results.last()
                    val currSize = input.length
                    val currTime = try {
                        measureTime { scoreFunction(input) }
                    } catch (e: StackOverflowError) {
                        break
                    }
                    results.add(Pair(currSize, currTime))

                    val dx = (currSize - prevSize).toFloat()
                    val dy = (currTime - prevTime).inWholeNanoseconds * 1e-9f
                    val rawIncrement = dx / dy *
                            (maxDuration - currTime).inWholeNanoseconds * 1e-9f /
                            max(1, wantedPoints - results.size)
                    val newIncrement = max(1f, min(maxIncrement, rawIncrement))
                    prevIncrement = (prevIncrement + newIncrement) / 2
                    skipUntilSize += prevIncrement.toInt()
                    maxIncrement *= 1.8f
                }
            }

            incrementalJsonData = "[${
                results.joinToString { (size, time) ->
                    """{"size": $size, "time": ${time.inWholeNanoseconds}}"""
                }
            }]"
        }
    }

    private fun getBenchmarkTime(input: String): Duration {
        val timeSource = TimeSource.Monotonic

        // benchmark phase
        val startMark = timeSource.markNow()
        val endMark = startMark.plus(2.seconds)
        var times = 0
        while (endMark.hasNotPassedNow()) {
            scoreFunction(input)
            times += 1
        }

        return startMark.elapsedNow() / times
    }

    fun saveJson() {
        Files.createDirectories(Path(saveFolder))
        File("$saveFolder/$name.json")
            .writeText("""{"incremental": $incrementalJsonData, "benchmarks": [${
                benchmarksJsonData.joinToString()
            }]}""")
    }
}
