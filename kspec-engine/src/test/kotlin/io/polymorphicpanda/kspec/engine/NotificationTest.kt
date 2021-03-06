package io.polymorphicpanda.kspec.engine

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.polymorphicpanda.kspec.KSpec
import io.polymorphicpanda.kspec.config.KSpecConfig
import io.polymorphicpanda.kspec.context.Context
import io.polymorphicpanda.kspec.describe
import io.polymorphicpanda.kspec.engine.discovery.DiscoveryRequest
import io.polymorphicpanda.kspec.engine.execution.ExecutionListenerAdapter
import io.polymorphicpanda.kspec.engine.execution.ExecutionNotifier
import io.polymorphicpanda.kspec.engine.execution.ExecutionResult
import io.polymorphicpanda.kspec.it
import org.junit.Test

/**
 * @author Ranie Jade Ramiso
 */
class NotificationTest {
    @Test
    fun testNotifyExampleFailure() {
        val builder = StringBuilder()
        val notifier = ExecutionNotifier()
        val engine = KSpecEngine(notifier)

        notifier.addListener(object: ExecutionListenerAdapter() {
            override fun exampleFinished(example: Context.Example, result: ExecutionResult) {
                if (result is ExecutionResult.Failure) {
                    builder.appendln(example.description)
                }
            }
        })

        class TestSpec: KSpec() {
            override fun spec() {
                describe("group") {
                    it("fail") {
                        TODO()
                    }
                }
            }
        }

        val result = engine.discover(DiscoveryRequest(listOf(TestSpec::class), KSpecConfig(), null))

        val expected = """
        it: fail
        """.trimIndent()

        engine.execute(result)

        assertThat(builder.trimEnd().toString(), equalTo(expected))
    }

    @Test
    fun testNotifyExampleBeforeEachFailure() {
        val builder = StringBuilder()
        val notifier = ExecutionNotifier()
        val engine = KSpecEngine(notifier)

        notifier.addListener(object: ExecutionListenerAdapter() {
            override fun exampleFinished(example: Context.Example, result: ExecutionResult) {
                if (result is ExecutionResult.Failure) {
                    builder.appendln(example.description)
                }
            }
        })

        class TestSpec: KSpec() {
            override fun spec() {
                describe("group") {
                    beforeEach { TODO() }
                    it("example") { }
                }
            }
        }

        val result = engine.discover(DiscoveryRequest(listOf(TestSpec::class), KSpecConfig(), null))

        val expected = """
        it: example
        """.trimIndent()

        engine.execute(result)

        assertThat(builder.trimEnd().toString(), equalTo(expected))
    }

    @Test
    fun testNotifyExampleAfterEachFailure() {
        val builder = StringBuilder()
        val notifier = ExecutionNotifier()
        val engine = KSpecEngine(notifier)

        notifier.addListener(object: ExecutionListenerAdapter() {
            override fun exampleFinished(example: Context.Example, result: ExecutionResult) {
                if (result is ExecutionResult.Failure) {
                    builder.appendln(example.description)
                }
            }
        })

        class TestSpec: KSpec() {
            override fun spec() {
                describe("group") {
                    it("example") { }
                    afterEach { TODO() }
                }
            }
        }

        val result = engine.discover(DiscoveryRequest(listOf(TestSpec::class), KSpecConfig()))

        val expected = """
        it: example
        """.trimIndent()

        engine.execute(result)

        assertThat(builder.trimEnd().toString(), equalTo(expected))
    }

    @Test
    fun testNotifyExampleGroupBeforeFailure() {
        val builder = StringBuilder()
        val notifier = ExecutionNotifier()
        val engine = KSpecEngine(notifier)

        notifier.addListener(object: ExecutionListenerAdapter() {
            override fun exampleGroupFinished(group: Context.ExampleGroup, result: ExecutionResult) {
                if (result is ExecutionResult.Failure) {
                    builder.appendln(group.description)
                }
            }
        })

        class TestSpec: KSpec() {
            override fun spec() {
                describe("group") {
                    before {
                        TODO()
                    }
                    it("example") { }
                }
            }
        }

        val result = engine.discover(DiscoveryRequest(listOf(TestSpec::class), KSpecConfig()))

        val expected = """
        describe: group
        """.trimIndent()

        engine.execute(result)

        assertThat(builder.trimEnd().toString(), equalTo(expected))
    }

    @Test
    fun testNotifyExampleGroupAfterFailure() {
        val builder = StringBuilder()
        val notifier = ExecutionNotifier()
        val engine = KSpecEngine(notifier)

        notifier.addListener(object: ExecutionListenerAdapter() {
            override fun exampleGroupFinished(group: Context.ExampleGroup, result: ExecutionResult) {
                if (result is ExecutionResult.Failure) {
                    builder.appendln(group.description)
                }
            }
        })

        class TestSpec: KSpec() {
            override fun spec() {
                describe("group") {
                    it("example") { }
                    after { TODO() }
                }
            }
        }

        val result = engine.discover(DiscoveryRequest(listOf(TestSpec::class), KSpecConfig(), null))

        val expected = """
        describe: group
        """.trimIndent()

        engine.execute(result)

        assertThat(builder.trimEnd().toString(), equalTo(expected))
    }

    @Test
    fun testNotifyExecutionStartedAndFinished() {
        val builder = StringBuilder()
        val notifier = ExecutionNotifier()
        val engine = KSpecEngine(notifier)

        notifier.addListener(object: ExecutionListenerAdapter() {
            override fun executionStarted() {
                builder.appendln("execution started")
            }

            override fun executionFinished() {
                builder.appendln("execution finished")
            }
        })

        class TestSpec: KSpec() {
            override fun spec() {
                describe("group") {
                    it("example") { }
                }
            }
        }

        val result = engine.discover(DiscoveryRequest(listOf(TestSpec::class), KSpecConfig(), null))

        val expected = """
        execution started
        execution finished
        """.trimIndent()

        engine.execute(result)

        assertThat(builder.trimEnd().toString(), equalTo(expected))
    }
}
