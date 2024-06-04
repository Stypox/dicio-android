package org.dicio.skill.standard.construct

import io.kotest.core.spec.style.DescribeSpec

class OptionalConstructTest : DescribeSpec({
    describe("OptionalConstruct should never do anything") {
        it("with empty input") {
            OptionalConstruct()
                .withInput("")
                .withStartingZeroedMemToEnd()
                .shouldChangeMemToEndInto(s(0f,0f,0f,0f))
        }
        it("with zeroed memToEnd") {
            OptionalConstruct()
                .withInput("a. d")
                .withStartingZeroedMemToEnd()
                .shouldChangeMemToEndInto(s(0f,0f,0f,0f),s(0f,0f,0f,0f),s(0f,0f,0f,0f),s(0f,0f,0f,0f),s(0f,0f,0f,0f))
        }
        it("with initial memToEnd") {
            OptionalConstruct()
                .withInput(".ÀD ")
                .withStartingInitialMemToEnd()
                .shouldChangeMemToEndInto(s(0f,1.05f,0f,0f),s(0f,1.0f,0f,0f),s(0f,0.5f,0f,0f),s(0f,0f,0f,0f),s(0f,0f,0f,0f))
        }
        it("with random memToEnd") {
            val starting = arrayOf(s(0.4f,0.4f,2.0f,2.0f),s(0.3f,0.3f,1.5f,1.5f),s(0.2f,0.2f,1.0f,1.5f),s(0.0f,0.1f,0.0f,1.5f),s(0.0f,0.0f,0.0f,1.5f))
            OptionalConstruct()
                .withInput("1234")
                .withStartingMemToEnd(*starting)
                .shouldChangeMemToEndInto(*starting)
        }
    }
})
