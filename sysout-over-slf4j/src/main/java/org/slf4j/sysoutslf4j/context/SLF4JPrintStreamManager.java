package org.slf4j.sysoutslf4j.context;

import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.sysoutslf4j.common.ClassLoaderUtils;
import org.slf4j.sysoutslf4j.common.ReflectionUtils;
import org.slf4j.sysoutslf4j.common.SLF4JPrintStream;
import org.slf4j.sysoutslf4j.common.SystemOutput;
import org.slf4j.sysoutslf4j.context.exceptionhandlers.ExceptionHandlingStrategy;
import org.slf4j.sysoutslf4j.context.exceptionhandlers.ExceptionHandlingStrategyFactory;
import org.slf4j.sysoutslf4j.system.SLF4JPrintStreamImpl;
import org.slf4j.sysoutslf4j.system.SLF4JPrintStreamConfigurator;

class SLF4JPrintStreamManager {

	private final Logger log = LoggerFactory.getLogger(SysOutOverSLF4J.class);

	void sendSystemOutAndErrToSLF4J(final LogLevel outLevel, final LogLevel errLevel,
			final ExceptionHandlingStrategyFactory exceptionHandlingStrategyFactory) {
		synchronized (System.class) {
			makeSystemOutputsSLF4JPrintStreamsIfNecessary();
			sendSystemOutAndErrToSLF4JForThisContext(outLevel, errLevel, exceptionHandlingStrategyFactory);
			log.info("Redirected System.out and System.err to SLF4J for this context");
		}
	}

	private void makeSystemOutputsSLF4JPrintStreamsIfNecessary() {
		if (systemOutputsAreSLF4JPrintStreams()) {
			log.debug("System.out and System.err are already SLF4JPrintStreams");
		} else {
			makeSystemOutputsSLF4JPrintStreams();
			log.info("Replaced standard System.out and System.err PrintStreams with SLF4JPrintStreams");
		}
	}

	private boolean systemOutputsAreSLF4JPrintStreams() {
		return System.out.getClass().getName().equals(SLF4JPrintStreamImpl.class.getName());
	}

	private void makeSystemOutputsSLF4JPrintStreams() {
		ReflectionUtils.invokeStaticMethod("replaceSystemOutputsWithSLF4JPrintStreams", getSlf4jPrintStreamConfiguratorClass());
	}

	private Class<?> getSlf4jPrintStreamConfiguratorClass() {
		final ClassLoader classLoader = ClassLoaderUtils.makeNewClassLoaderForJar(SLF4JPrintStreamConfigurator.class);
		final Class<?> slf4jPrintStreamConfiguratorClass =
			ClassLoaderUtils.loadClass(classLoader, SLF4JPrintStreamConfigurator.class);
		return slf4jPrintStreamConfiguratorClass;
	}

	private void sendSystemOutAndErrToSLF4JForThisContext(final LogLevel outLevel, final LogLevel errLevel, 
			final ExceptionHandlingStrategyFactory exceptionHandlingStrategyFactory) {
		registerNewLoggerAppender(exceptionHandlingStrategyFactory, SLF4JPrintStreamProxy.wrap(SystemOutput.OUT.get()), outLevel);
		registerNewLoggerAppender(exceptionHandlingStrategyFactory, SLF4JPrintStreamProxy.wrap(SystemOutput.ERR.get()), errLevel);
	}

	private void registerNewLoggerAppender(
			final ExceptionHandlingStrategyFactory exceptionHandlingStrategyFactory,
			final SLF4JPrintStream slf4jPrintStream, final LogLevel logLevel) {

		final PrintStream originalPrintStream = slf4jPrintStream.getOriginalPrintStream();
		final ExceptionHandlingStrategy exceptionHandlingStrategy = 
			exceptionHandlingStrategyFactory.makeExceptionHandlingStrategy(logLevel, originalPrintStream);
		final Object loggerAppender = 
			new LoggerAppenderImpl(logLevel, exceptionHandlingStrategy, originalPrintStream);
		ReferenceHolder.preventGarbageCollectionForLifeOfClassLoader(loggerAppender);
		slf4jPrintStream.registerLoggerAppender(loggerAppender);
	}

	void sendSystemOutAndErrToOriginalsIfNecessary() {
		synchronized (System.class) {
			if (systemOutputsAreSLF4JPrintStreams()) {
				sendSystemOutAndErrToOriginals();
				log.info("Restored original System.out and System.err");
			} else {
				log.warn("System.out and System.err are not SLF4JPrintStreams - cannot restore");
			}
		}
	}

	private void sendSystemOutAndErrToOriginals() {
		ReflectionUtils.invokeStaticMethod("restoreOriginalSystemOutputs", getSlf4jPrintStreamConfiguratorClass());
	}
}