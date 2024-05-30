package org.dicio.skill.standard2

import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import org.dicio.skill.skill.Specificity
import org.dicio.skill.standard2.construct.CapturingConstruct
import org.dicio.skill.standard2.construct.CompositeConstruct
import org.dicio.skill.standard2.construct.OptionalConstruct
import org.dicio.skill.standard2.construct.OrConstruct
import org.dicio.skill.standard2.construct.RegexWordConstruct
import org.dicio.skill.standard2.construct.WordConstruct
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlin.time.measureTime

const val saveFolder = "benchmarks/current"

class PerformanceTest : FunSpec({
    benchmarkContext("current_time", currentTimeData) {
        warmup("test", "time", "what time", "current time")
        runBenchmarks("time", "what time is it?", "what's the time", "hey I would like to know what time it is")
        runIncrementalBenchmarks()
    }

    benchmarkContext("weather", weatherData) {
        warmup("test", "weather", "cold", "weather in rome")
        runBenchmarks("weather", "what's the weather", "is it cold in budapest", "what is the weather in rome")
        runIncrementalBenchmarks()
    }

    benchmarkContext("timer", timerData) {
        warmup( "test", "timer", "timer 1s", "named a")
        runBenchmarks("set a timer", "set a timer of 5s", "set a timer named a")
        runIncrementalBenchmarks()
    }
})

fun FunSpec.benchmarkContext(
    name: String,
    data: StandardRecognizerData<*>,
    f: suspend BenchmarkRunner.() -> Unit
) {
    context(name) {
        val runner = BenchmarkRunner(this, name, data)
        f(runner)
        runner.saveJson()
    }
}

class BenchmarkRunner(
    private val funSpec: FunSpecContainerScope,
    private val name: String,
    private val data: StandardRecognizerData<*>,
) {
    private var incrementalJsonData = "[]"
    private var benchmarksJsonData = ArrayList<String>()

    fun warmup(vararg inputs: String) {
        for (input in inputs) {
            data.score(input)
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
            val results = ArrayList<Duration>()
            var input = ""

            do {
                results.add(measureTime { data.score(input) })
                input += if (input.length % 4 == 3) " " else "a"
            } while (results.last() < 1.seconds)

            incrementalJsonData = "[${
                results.mapIndexed { i, time ->
                    """{"size": $i, "time": ${time.inWholeNanoseconds}}"""
                }.joinToString()
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
            data.score(input)
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

// @formatter:off
val currentTimeData = StandardRecognizerData(Specificity.HIGH, { _,_,_ -> },
          listOf(Pair("query", CompositeConstruct(listOf(OrConstruct(listOf(WordConstruct("what",
          false, 1.0f),CompositeConstruct(listOf(WordConstruct("what", false,
          1.0f),OrConstruct(listOf(WordConstruct("s", false, 1.0f),WordConstruct("is", false,
          1.0f),)),)),WordConstruct("whats", false, 1.0f),)),OrConstruct(listOf(WordConstruct("the",
          false, 1.0f),OptionalConstruct,)),WordConstruct("time", false,
          1.0f),OrConstruct(listOf(CompositeConstruct(listOf(WordConstruct("is", false,
          1.0f),WordConstruct("it", false, 1.0f),)),OptionalConstruct,)),))),))
val weatherData = StandardRecognizerData(Specificity.HIGH, { _,_,_ -> }, listOf(Pair("current",
          CompositeConstruct(listOf(OrConstruct(listOf(CompositeConstruct(listOf(WordConstruct("what",
          false, 1.0f),OrConstruct(listOf(WordConstruct("is", false, 1.0f),WordConstruct("s", false,
          1.0f),)),)),WordConstruct("whats", false, 1.0f),)),WordConstruct("the", false,
          1.0f),WordConstruct("weather", false, 1.0f),OrConstruct(listOf(WordConstruct("like",
          false,
          1.0f),OptionalConstruct,)),OrConstruct(listOf(CompositeConstruct(listOf(OrConstruct(listOf(WordConstruct("in",
          false, 1.0f),WordConstruct("on", false, 1.0f),)),CapturingConstruct("where",
          1.0f),)),OptionalConstruct,)),))),Pair("current",
          CompositeConstruct(listOf(WordConstruct("weather", false,
          1.0f),OrConstruct(listOf(CompositeConstruct(listOf(OrConstruct(listOf(WordConstruct("in",
          false, 1.0f),WordConstruct("on", false,
          1.0f),OptionalConstruct,)),CapturingConstruct("where",
          1.0f),)),OptionalConstruct,)),))),Pair("current",
          CompositeConstruct(listOf(WordConstruct("how", false, 1.0f),WordConstruct("is", false,
          1.0f),WordConstruct("it", false, 1.0f),WordConstruct("outside", false,
          1.0f),))),Pair("current", CompositeConstruct(listOf(WordConstruct("is", false,
          1.0f),WordConstruct("it", false, 1.0f),OrConstruct(listOf(WordConstruct("cold", false,
          1.0f),WordConstruct("cool", false, 1.0f),WordConstruct("warm", false,
          1.0f),WordConstruct("hot", false, 1.0f),WordConstruct("sunny", false,
          1.0f),WordConstruct("rainy", false, 1.0f),WordConstruct("raining", false,
          1.0f),)),OrConstruct(listOf(CompositeConstruct(listOf(OrConstruct(listOf(WordConstruct("in",
          false, 1.0f),WordConstruct("on", false, 1.0f),)),CapturingConstruct("where",
          1.0f),)),WordConstruct("outside", false, 1.0f),OptionalConstruct,)),))),))
val timerData = StandardRecognizerData(Specificity.HIGH, { _,_,_ -> }, listOf(Pair("set",
          CompositeConstruct(listOf(OrConstruct(listOf(WordConstruct("timer", false,
          1.0f),CompositeConstruct(listOf(WordConstruct("ping", false, 1.0f),WordConstruct("me",
          false, 1.0f),WordConstruct("in", false, 1.0f),)),)),CapturingConstruct("duration",
          1.0f),))),Pair("set",
          CompositeConstruct(listOf(OrConstruct(listOf(CompositeConstruct(listOf(WordConstruct("set",
          false, 1.0f),OrConstruct(listOf(WordConstruct("up", false,
          1.0f),OptionalConstruct,)),)),WordConstruct("setup", false, 1.0f),WordConstruct("start",
          false, 1.0f),WordConstruct("create", false, 1.0f),)),OrConstruct(listOf(WordConstruct("a",
          false,
          1.0f),OptionalConstruct,)),OrConstruct(listOf(CompositeConstruct(listOf(OrConstruct(listOf(CapturingConstruct("duration",
          1.0f),OptionalConstruct,)),WordConstruct("timer", false,
          1.0f),)),CompositeConstruct(listOf(WordConstruct("timer", false,
          1.0f),OrConstruct(listOf(WordConstruct("for", false, 1.0f),WordConstruct("of", false,
          1.0f),)),CapturingConstruct("duration",
          1.0f),OrConstruct(listOf(CompositeConstruct(listOf(OrConstruct(listOf(WordConstruct("called",
          false, 1.0f),WordConstruct("named", false,
          1.0f),CompositeConstruct(listOf(WordConstruct("with", false, 1.0f),WordConstruct("the",
          false, 1.0f),WordConstruct("name", false, 1.0f),)),)),CapturingConstruct("name",
          1.0f),)),OptionalConstruct,)),)),)),))),Pair("cancel",
          CompositeConstruct(listOf(OrConstruct(listOf(WordConstruct("cancel", false,
          1.0f),WordConstruct("stop", false, 1.0f),WordConstruct("disable", false,
          1.0f),WordConstruct("end", false, 1.0f),WordConstruct("terminate", false,
          1.0f),)),OrConstruct(listOf(WordConstruct("the", false,
          1.0f),OptionalConstruct,)),OrConstruct(listOf(CompositeConstruct(listOf(OrConstruct(listOf(CapturingConstruct("name",
          1.0f),OptionalConstruct,)),RegexWordConstruct("time(?:r|rs|)", false,
          1.0f),)),CompositeConstruct(listOf(RegexWordConstruct("time(?:r|rs|)", false,
          1.0f),OrConstruct(listOf(WordConstruct("called", false, 1.0f),WordConstruct("named",
          false, 1.0f),CompositeConstruct(listOf(WordConstruct("with", false,
          1.0f),WordConstruct("the", false, 1.0f),WordConstruct("name", false,
          1.0f),)),)),CapturingConstruct("name", 1.0f),)),)),))),Pair("cancel",
          CompositeConstruct(listOf(OrConstruct(listOf(WordConstruct("silence", false,
          1.0f),CompositeConstruct(listOf(OrConstruct(listOf(WordConstruct("shut", false,
          1.0f),WordConstruct("turn", false, 1.0f),)),WordConstruct("off", false,
          1.0f),)),WordConstruct("quiet", false, 1.0f),WordConstruct("mute", false,
          1.0f),)),OrConstruct(listOf(WordConstruct("the", false,
          1.0f),OptionalConstruct,)),OrConstruct(listOf(CompositeConstruct(listOf(OrConstruct(listOf(CapturingConstruct("name",
          1.0f),OptionalConstruct,)),OrConstruct(listOf(RegexWordConstruct("time(?:r|rs|)", false,
          1.0f),WordConstruct("bell", false, 1.0f),WordConstruct("alert", false,
          1.0f),WordConstruct("sound", false, 1.0f),WordConstruct("ringtone", false,
          1.0f),)),)),CompositeConstruct(listOf(OrConstruct(listOf(RegexWordConstruct("time(?:r|rs|)",
          false, 1.0f),WordConstruct("bell", false, 1.0f),WordConstruct("alert", false,
          1.0f),WordConstruct("sound", false, 1.0f),WordConstruct("ringtone", false,
          1.0f),)),OrConstruct(listOf(WordConstruct("called", false, 1.0f),WordConstruct("named",
          false, 1.0f),CompositeConstruct(listOf(WordConstruct("with", false,
          1.0f),WordConstruct("the", false, 1.0f),WordConstruct("name", false,
          1.0f),)),)),CapturingConstruct("name", 1.0f),)),)),))),Pair("query",
          CompositeConstruct(listOf(WordConstruct("how", false,
          1.0f),OrConstruct(listOf(WordConstruct("long", false,
          1.0f),CompositeConstruct(listOf(WordConstruct("much", false, 1.0f),WordConstruct("time",
          false, 1.0f),)),)),WordConstruct("is", false,
          1.0f),OrConstruct(listOf(WordConstruct("left", false,
          1.0f),OptionalConstruct,)),WordConstruct("on", false,
          1.0f),OrConstruct(listOf(WordConstruct("the", false,
          1.0f),OptionalConstruct,)),OrConstruct(listOf(CompositeConstruct(listOf(OrConstruct(listOf(CapturingConstruct("name",
          1.0f),OptionalConstruct,)),RegexWordConstruct("time(?:r|rs|)", false,
          1.0f),)),CompositeConstruct(listOf(RegexWordConstruct("time(?:r|rs|)", false,
          1.0f),OrConstruct(listOf(WordConstruct("called", false, 1.0f),WordConstruct("named",
          false, 1.0f),CompositeConstruct(listOf(WordConstruct("with", false,
          1.0f),WordConstruct("the", false, 1.0f),WordConstruct("name", false,
          1.0f),)),)),CapturingConstruct("name", 1.0f),)),)),))),Pair("query",
          CompositeConstruct(listOf(WordConstruct("when", false,
          1.0f),OrConstruct(listOf(WordConstruct("will", false, 1.0f),WordConstruct("is", false,
          1.0f),)),OrConstruct(listOf(WordConstruct("the", false,
          1.0f),OptionalConstruct,)),OrConstruct(listOf(CompositeConstruct(listOf(OrConstruct(listOf(CapturingConstruct("name",
          1.0f),OptionalConstruct,)),RegexWordConstruct("time(?:r|rs|)", false,
          1.0f),)),CompositeConstruct(listOf(RegexWordConstruct("time(?:r|rs|)", false,
          1.0f),OrConstruct(listOf(WordConstruct("called", false, 1.0f),WordConstruct("named",
          false, 1.0f),CompositeConstruct(listOf(WordConstruct("with", false,
          1.0f),WordConstruct("the", false, 1.0f),WordConstruct("name", false,
          1.0f),)),)),CapturingConstruct("name",
          1.0f),)),)),OrConstruct(listOf(CompositeConstruct(listOf(WordConstruct("going", false,
          1.0f),WordConstruct("to", false, 1.0f),)),OptionalConstruct,)),WordConstruct("expire",
          false, 1.0f),))),))
// @formatter:on 
