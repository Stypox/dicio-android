package org.dicio.skill.standard.construct

import io.kotest.core.spec.style.DescribeSpec

class OptionalConstructTest : DescribeSpec({
    describe("OptionalConstruct should never do anything") {
        it("with empty input") {
            OptionalConstruct()
                .withInput("")
                .withStartingZeroedMemToEnd()
                .shouldNotMatchAnything(additionalRefWeight = 0f)
        }
        it("with zeroed memToEnd") {
            OptionalConstruct()
                .withInput("a. d")
                .withStartingZeroedMemToEnd()
                .shouldNotMatchAnything(additionalRefWeight = 0f)
        }
        it("with initial memToEnd") {
            OptionalConstruct()
                .withInput(".ÀD ")
                .withStartingInitialMemToEnd()
                .shouldNotMatchAnything(additionalRefWeight = 0f)
        }
        it("with random memToEnd") {
            OptionalConstruct()
                .withInput("1234")
                .withStartingMemToEnd(s(0.4f,0.4f,2.0f,2.0f),s(0.3f,0.3f,1.5f,1.5f),s(0.2f,0.2f,1.0f,1.5f),s(0.0f,0.1f,0.0f,1.5f),s(0.0f,0.0f,0.0f,1.5f))
                .shouldNotMatchAnything(additionalRefWeight = 0f)
        }
    }
})
