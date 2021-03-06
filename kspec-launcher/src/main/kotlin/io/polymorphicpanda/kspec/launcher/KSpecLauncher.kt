package io.polymorphicpanda.kspec.launcher

import io.polymorphicpanda.kspec.KSpec
import io.polymorphicpanda.kspec.config.KSpecConfig
import io.polymorphicpanda.kspec.context.Context
import io.polymorphicpanda.kspec.engine.KSpecEngine
import io.polymorphicpanda.kspec.engine.discovery.DiscoveryRequest
import io.polymorphicpanda.kspec.engine.execution.ExecutionListenerAdapter
import io.polymorphicpanda.kspec.engine.execution.ExecutionNotifier
import io.polymorphicpanda.kspec.engine.execution.ExecutionRequest
import io.polymorphicpanda.kspec.engine.execution.ExecutionResult
import io.polymorphicpanda.kspec.engine.query.Query
import io.polymorphicpanda.kspec.launcher.reporter.Reporter
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import java.util.regex.Pattern
import kotlin.reflect.KClass

/**
 * @author Ranie Jade Ramiso
 */
class KSpecLauncher {
    private val reporters = LinkedHashSet<Reporter>()

    private val listener = object: ExecutionListenerAdapter() {
        override fun executionStarted() {
            reporters.forEach { it.executionStarted() }
        }

        override fun executionFinished() {
            reporters.forEach { it.executionFinished() }
        }

        override fun exampleFinished(example: Context.Example, result: ExecutionResult) {
            reporters.forEach { it.exampleFinished(example, result) }
        }

        override fun exampleIgnored(example: Context.Example) {
            reporters.forEach { it.exampleIgnored(example) }
        }

        override fun exampleGroupIgnored(group: Context.ExampleGroup) {
            reporters.forEach { it.exampleGroupIgnored(group) }
        }

        override fun exampleGroupFinished(group: Context.ExampleGroup, result: ExecutionResult) {
            reporters.forEach { it.exampleGroupFinished(group, result) }
        }
    }

    private val notifier by lazy(LazyThreadSafetyMode.NONE) {
        ExecutionNotifier().apply {
            addListener(listener)
        }
    }

    private val engine by lazy(LazyThreadSafetyMode.NONE) {
        KSpecEngine(notifier)
    }

    fun addReporter(reporter: Reporter) {
        reporters.add(reporter)
    }

    fun launch(configuration: LaunchConfiguration) {
        val target = configuration.specs

        if (!target.isDirectory()) {
            throw IllegalArgumentException("Invalid target.")
        }

        val classes = searchClasses(target)

        val runtime = ArrayList<URL>()
        runtime.add(target.toUri().toURL())
        runtime.addAll(configuration.classpath.map { it.toUri().toURL() })

        val classloader = URLClassLoader(runtime.toTypedArray(), Thread.currentThread().contextClassLoader)

        Thread.currentThread().contextClassLoader = classloader

        val pattern = Pattern.compile(escape(configuration.filter))

        val filtered = classes.map { classloader.loadClass(it) }
            .filter { KSpec::class.java.isAssignableFrom(it)}
            .filter {
                if (configuration.filter.isEmpty()) {
                    true
                } else {
                    pattern.matcher(it.name).matches()
                }
            }
            .sortedBy { it.name }
            .map { it.kotlin as KClass<out KSpec> }

        runSpecs(filtered, configuration.query)
    }

    private fun runSpecs(specs: List<KClass<out KSpec>>, query: String) {
        val discoveryResult = engine.discover(
            DiscoveryRequest(
                specs,
                KSpecConfig(),
                if (query.isNotBlank()) Query.parse(query) else null
            )
        )

        engine.execute(
            ExecutionRequest(
                discoveryResult
            )
        )
    }

    private fun escape(pattern: String): String {
        return pattern.replace(".", "\\.")
            .replace("*", ".*")
    }
}
