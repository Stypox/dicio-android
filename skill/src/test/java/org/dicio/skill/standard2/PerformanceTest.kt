package org.dicio.skill.standard2

import io.kotest.core.spec.style.FunSpec
import org.dicio.skill.benchmarkContext
import org.dicio.skill.skill.Specificity
import org.dicio.skill.standard2.construct.CapturingConstruct
import org.dicio.skill.standard2.construct.CompositeConstruct
import org.dicio.skill.standard2.construct.OptionalConstruct
import org.dicio.skill.standard2.construct.OrConstruct
import org.dicio.skill.standard2.construct.WordConstruct

class PerformanceTest : FunSpec({
    benchmarkContext("current_time", currentTimeData::score) {
        warmup("test", "time", "what time", "current time")
        runBenchmarks("time", "what time is it?", "what's the time", "hey I would like to know what time it is")
        runIncrementalBenchmarks()
    }

    benchmarkContext("weather", weatherData::score) {
        warmup("test", "weather", "cold", "weather in rome")
        runBenchmarks("weather", "what's the weather", "is it cold in budapest", "what is the weather in rome")
        runIncrementalBenchmarks()
    }

    benchmarkContext("timer", timerData::score) {
        warmup( "test", "timer", "timer 1s", "named a")
        runBenchmarks("set a timer", "set a timer of 5s", "set a timer named a")
        runIncrementalBenchmarks()
    }
})

// @formatter:off
@Suppress("BooleanLiteralArgument")
val currentTimeData = StandardRecognizerData(Specificity.HIGH, { _,_,_ -> },
          listOf(Pair("query", CompositeConstruct(listOf(OrConstruct(listOf(WordConstruct("what",
          false, false, 1.0f),CompositeConstruct(listOf(WordConstruct("what", false, false,
          1.0f),OrConstruct(listOf(WordConstruct("s", false, false, 1.0f),WordConstruct("is", false,
          false, 1.0f),)),)),WordConstruct("whats", false, false,
          1.0f),)),OrConstruct(listOf(WordConstruct("the", false, false,
          1.0f),OptionalConstruct(),)),WordConstruct("time", false, false,
          1.0f),OrConstruct(listOf(CompositeConstruct(listOf(WordConstruct("is", false, false,
          1.0f),WordConstruct("it", false, false, 1.0f),)),OptionalConstruct(),)),))),))
@Suppress("BooleanLiteralArgument")
val weatherData = StandardRecognizerData(Specificity.HIGH, { _,_,_ -> }, listOf(Pair("current",
          CompositeConstruct(listOf(OrConstruct(listOf(CompositeConstruct(listOf(WordConstruct("what",
          false, false, 1.0f),OrConstruct(listOf(WordConstruct("is", false, false,
          1.0f),WordConstruct("s", false, false, 1.0f),)),)),WordConstruct("whats", false, false,
          1.0f),)),WordConstruct("the", false, false, 1.0f),WordConstruct("weather", false, false,
          1.0f),OrConstruct(listOf(WordConstruct("like", false, false,
          1.0f),OptionalConstruct(),)),OrConstruct(listOf(CompositeConstruct(listOf(OrConstruct(listOf(WordConstruct("in",
          false, false, 1.0f),WordConstruct("on", false, false, 1.0f),)),CapturingConstruct("where",
          1.0f),)),OptionalConstruct(),)),))),Pair("current",
          CompositeConstruct(listOf(WordConstruct("weather", false, false,
          1.0f),OrConstruct(listOf(CompositeConstruct(listOf(OrConstruct(listOf(WordConstruct("in",
          false, false, 1.0f),WordConstruct("on", false, false,
          1.0f),OptionalConstruct(),)),CapturingConstruct("where",
          1.0f),)),OptionalConstruct(),)),))),Pair("current",
          CompositeConstruct(listOf(WordConstruct("how", false, false, 1.0f),WordConstruct("is",
          false, false, 1.0f),WordConstruct("it", false, false, 1.0f),WordConstruct("outside",
          false, false, 1.0f),))),Pair("current", CompositeConstruct(listOf(WordConstruct("is",
          false, false, 1.0f),WordConstruct("it", false, false,
          1.0f),OrConstruct(listOf(WordConstruct("cold", false, false, 1.0f),WordConstruct("cool",
          false, false, 1.0f),WordConstruct("warm", false, false, 1.0f),WordConstruct("hot", false,
          false, 1.0f),WordConstruct("sunny", false, false, 1.0f),WordConstruct("rainy", false,
          false, 1.0f),WordConstruct("raining", false, false,
          1.0f),)),OrConstruct(listOf(CompositeConstruct(listOf(OrConstruct(listOf(WordConstruct("in",
          false, false, 1.0f),WordConstruct("on", false, false, 1.0f),)),CapturingConstruct("where",
          1.0f),)),WordConstruct("outside", false, false, 1.0f),OptionalConstruct(),)),))),))
@Suppress("BooleanLiteralArgument")
val timerData = StandardRecognizerData(Specificity.HIGH, { _,_,_ -> }, listOf(Pair("set",
          CompositeConstruct(listOf(OrConstruct(listOf(WordConstruct("timer", false, false,
          1.0f),CompositeConstruct(listOf(WordConstruct("ping", false, false,
          1.0f),WordConstruct("me", false, false, 1.0f),WordConstruct("in", false, false,
          1.0f),)),)),CapturingConstruct("duration", 1.0f),))),Pair("set",
          CompositeConstruct(listOf(OrConstruct(listOf(CompositeConstruct(listOf(WordConstruct("set",
          false, false, 1.0f),OrConstruct(listOf(WordConstruct("up", false, false,
          1.0f),OptionalConstruct(),)),)),WordConstruct("setup", false, false,
          1.0f),WordConstruct("start", false, false, 1.0f),WordConstruct("create", false, false,
          1.0f),)),OrConstruct(listOf(WordConstruct("a", false, false,
          1.0f),OptionalConstruct(),)),OrConstruct(listOf(CompositeConstruct(listOf(OrConstruct(listOf(CapturingConstruct("duration",
          1.0f),OptionalConstruct(),)),WordConstruct("timer", false, false,
          1.0f),)),CompositeConstruct(listOf(WordConstruct("timer", false, false,
          1.0f),OrConstruct(listOf(WordConstruct("for", false, false, 1.0f),WordConstruct("of",
          false, false, 1.0f),)),CapturingConstruct("duration",
          1.0f),OrConstruct(listOf(CompositeConstruct(listOf(OrConstruct(listOf(WordConstruct("called",
          false, false, 1.0f),WordConstruct("named", false, false,
          1.0f),CompositeConstruct(listOf(WordConstruct("with", false, false,
          1.0f),WordConstruct("the", false, false, 1.0f),WordConstruct("name", false, false,
          1.0f),)),)),CapturingConstruct("name",
          1.0f),)),OptionalConstruct(),)),)),)),))),Pair("cancel",
          CompositeConstruct(listOf(OrConstruct(listOf(WordConstruct("cancel", false, false,
          1.0f),WordConstruct("stop", false, false, 1.0f),WordConstruct("disable", false, false,
          1.0f),WordConstruct("end", false, false, 1.0f),WordConstruct("terminate", false, false,
          1.0f),)),OrConstruct(listOf(WordConstruct("the", false, false,
          1.0f),OptionalConstruct(),)),OrConstruct(listOf(CompositeConstruct(listOf(OrConstruct(listOf(CapturingConstruct("name",
          1.0f),OptionalConstruct(),)),WordConstruct("time(?:r|rs|)", true, false,
          1.0f),)),CompositeConstruct(listOf(WordConstruct("time(?:r|rs|)", true, false,
          1.0f),OrConstruct(listOf(WordConstruct("called", false, false,
          1.0f),WordConstruct("named", false, false,
          1.0f),CompositeConstruct(listOf(WordConstruct("with", false, false,
          1.0f),WordConstruct("the", false, false, 1.0f),WordConstruct("name", false, false,
          1.0f),)),)),CapturingConstruct("name", 1.0f),)),)),))),Pair("cancel",
          CompositeConstruct(listOf(OrConstruct(listOf(WordConstruct("silence", false, false,
          1.0f),CompositeConstruct(listOf(OrConstruct(listOf(WordConstruct("shut", false, false,
          1.0f),WordConstruct("turn", false, false, 1.0f),)),WordConstruct("off", false, false,
          1.0f),)),WordConstruct("quiet", false, false, 1.0f),WordConstruct("mute", false, false,
          1.0f),)),OrConstruct(listOf(WordConstruct("the", false, false,
          1.0f),OptionalConstruct(),)),OrConstruct(listOf(CompositeConstruct(listOf(OrConstruct(listOf(CapturingConstruct("name",
          1.0f),OptionalConstruct(),)),OrConstruct(listOf(WordConstruct("time(?:r|rs|)", true,
          false, 1.0f),WordConstruct("bell", false, false, 1.0f),WordConstruct("alert", false,
          false, 1.0f),WordConstruct("sound", false, false, 1.0f),WordConstruct("ringtone", false,
          false,
          1.0f),)),)),CompositeConstruct(listOf(OrConstruct(listOf(WordConstruct("time(?:r|rs|)",
          true, false, 1.0f),WordConstruct("bell", false, false, 1.0f),WordConstruct("alert", false,
          false, 1.0f),WordConstruct("sound", false, false, 1.0f),WordConstruct("ringtone", false,
          false, 1.0f),)),OrConstruct(listOf(WordConstruct("called", false, false,
          1.0f),WordConstruct("named", false, false,
          1.0f),CompositeConstruct(listOf(WordConstruct("with", false, false,
          1.0f),WordConstruct("the", false, false, 1.0f),WordConstruct("name", false, false,
          1.0f),)),)),CapturingConstruct("name", 1.0f),)),)),))),Pair("query",
          CompositeConstruct(listOf(WordConstruct("how", false, false,
          1.0f),OrConstruct(listOf(WordConstruct("long", false, false,
          1.0f),CompositeConstruct(listOf(WordConstruct("much", false, false,
          1.0f),WordConstruct("time", false, false, 1.0f),)),)),WordConstruct("is", false, false,
          1.0f),OrConstruct(listOf(WordConstruct("left", false, false,
          1.0f),OptionalConstruct(),)),WordConstruct("on", false, false,
          1.0f),OrConstruct(listOf(WordConstruct("the", false, false,
          1.0f),OptionalConstruct(),)),OrConstruct(listOf(CompositeConstruct(listOf(OrConstruct(listOf(CapturingConstruct("name",
          1.0f),OptionalConstruct(),)),WordConstruct("time(?:r|rs|)", true, false,
          1.0f),)),CompositeConstruct(listOf(WordConstruct("time(?:r|rs|)", true, false,
          1.0f),OrConstruct(listOf(WordConstruct("called", false, false,
          1.0f),WordConstruct("named", false, false,
          1.0f),CompositeConstruct(listOf(WordConstruct("with", false, false,
          1.0f),WordConstruct("the", false, false, 1.0f),WordConstruct("name", false, false,
          1.0f),)),)),CapturingConstruct("name", 1.0f),)),)),))),Pair("query",
          CompositeConstruct(listOf(WordConstruct("when", false, false,
          1.0f),OrConstruct(listOf(WordConstruct("will", false, false, 1.0f),WordConstruct("is",
          false, false, 1.0f),)),OrConstruct(listOf(WordConstruct("the", false, false,
          1.0f),OptionalConstruct(),)),OrConstruct(listOf(CompositeConstruct(listOf(OrConstruct(listOf(CapturingConstruct("name",
          1.0f),OptionalConstruct(),)),WordConstruct("time(?:r|rs|)", true, false,
          1.0f),)),CompositeConstruct(listOf(WordConstruct("time(?:r|rs|)", true, false,
          1.0f),OrConstruct(listOf(WordConstruct("called", false, false,
          1.0f),WordConstruct("named", false, false,
          1.0f),CompositeConstruct(listOf(WordConstruct("with", false, false,
          1.0f),WordConstruct("the", false, false, 1.0f),WordConstruct("name", false, false,
          1.0f),)),)),CapturingConstruct("name",
          1.0f),)),)),OrConstruct(listOf(CompositeConstruct(listOf(WordConstruct("going", false,
          false, 1.0f),WordConstruct("to", false, false,
          1.0f),)),OptionalConstruct(),)),WordConstruct("expire", false, false, 1.0f),))),))
// @formatter:on 
