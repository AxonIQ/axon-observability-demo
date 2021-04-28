package com.demo;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.Configurer;
import org.axonframework.config.ConfigurerModule;
import org.axonframework.config.MessageMonitorFactory;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.micrometer.CapacityMonitor;
import org.axonframework.micrometer.MessageCountingMonitor;
import org.axonframework.micrometer.MessageTimerMonitor;
import org.axonframework.micrometer.TagsUtil;
import org.axonframework.monitoring.MultiMessageMonitor;
import org.axonframework.queryhandling.QueryBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Collectors;

/**
 * Spring configuration for Axon Metrics
 */
//@Configuration
class MetricsConfig {

    /**
     * Configure custom metrics on all Axon messaging components.
     * Demonstrates: using message metadata as metrics tags/dimensions.
     *
     * @param meterRegistry
     * @return ConfigurerModule with the new setup
     */
    @Bean
    ConfigurerModule metricConfigurer(MeterRegistry meterRegistry) {
        return configurer -> {
            instrumentEventStore(meterRegistry, configurer);
            instrumentEventProcessors(meterRegistry, configurer);
            instrumentCommandBus(meterRegistry, configurer);
            instrumentQueryBus(meterRegistry, configurer);
        };
    }

    private void instrumentEventStore(MeterRegistry meterRegistry, Configurer configurer) {
        MessageMonitorFactory messageMonitorFactory = (configuration, componentType, componentName) -> {
            MessageCountingMonitor messageCounter = MessageCountingMonitor.buildMonitor(
                    componentName, meterRegistry,
                    message -> Tags.of(TagsUtil.PAYLOAD_TYPE_TAG, message.getPayloadType().getSimpleName())
                            .and(message.getMetaData().entrySet().stream()
                                    .map(s -> Tag.of(s.getKey(), s.getValue().toString()))
                                    .collect(Collectors.toList()))
            );
            MessageTimerMonitor messageTimer = MessageTimerMonitor.buildMonitor(
                    componentName, meterRegistry,
                    message -> Tags.of(TagsUtil.PAYLOAD_TYPE_TAG, message.getPayloadType().getSimpleName())
                            .and(message.getMetaData().entrySet().stream()
                                    .map(s -> Tag.of(s.getKey(), s.getValue().toString()))
                                    .collect(Collectors.toList()))
            );
            return new MultiMessageMonitor<>(messageCounter, messageTimer);
        };
        configurer.configureMessageMonitor(EventStore.class, messageMonitorFactory);
    }

    private void instrumentEventProcessors(MeterRegistry meterRegistry, Configurer configurer) {
        MessageMonitorFactory messageMonitorFactory = (configuration, componentType, componentName) -> {
            MessageCountingMonitor messageCounter = MessageCountingMonitor.buildMonitor(
                    "eventProcessor", meterRegistry,
                    message -> Tags.of(
                            TagsUtil.PAYLOAD_TYPE_TAG, message.getPayloadType().getSimpleName(),
                            TagsUtil.PROCESSOR_NAME_TAG, componentName)
                            .and(message.getMetaData().entrySet().stream()
                                    .map(s -> Tag.of(s.getKey(), s.getValue().toString()))
                                    .collect(Collectors.toList()))
            );
            MessageTimerMonitor messageTimer = MessageTimerMonitor.buildMonitor(
                    "eventProcessor", meterRegistry,
                    message -> Tags.of(
                            TagsUtil.PAYLOAD_TYPE_TAG, message.getPayloadType().getSimpleName(),
                            TagsUtil.PROCESSOR_NAME_TAG, componentName)
                            .and(message.getMetaData().entrySet().stream()
                                    .map(s -> Tag.of(s.getKey(), s.getValue().toString()))
                                    .collect(Collectors.toList()))
            );
            CapacityMonitor capacityMonitor1Minute = CapacityMonitor.buildMonitor(
                    "eventProcessor", meterRegistry,
                    message -> Tags.of(
                            TagsUtil.PAYLOAD_TYPE_TAG, message.getPayloadType().getSimpleName(),
                            TagsUtil.PROCESSOR_NAME_TAG, componentName)
                            .and(message.getMetaData().entrySet().stream()
                                    .map(s -> Tag.of(s.getKey(), s.getValue().toString()))
                                    .collect(Collectors.toList()))
            );

            return new MultiMessageMonitor<>(messageCounter, messageTimer, capacityMonitor1Minute);
        };
        configurer.configureMessageMonitor(TrackingEventProcessor.class, messageMonitorFactory);
    }

    private void instrumentCommandBus(MeterRegistry meterRegistry, Configurer configurer) {
        MessageMonitorFactory messageMonitorFactory = (configuration, componentType, componentName) -> {
            MessageCountingMonitor messageCounter = MessageCountingMonitor.buildMonitor(
                    componentName, meterRegistry,
                    message -> Tags.of(TagsUtil.PAYLOAD_TYPE_TAG, message.getPayloadType().getSimpleName())
                            .and(message.getMetaData().entrySet().stream()
                                    .map(s -> Tag.of(s.getKey(), s.getValue().toString()))
                                    .collect(Collectors.toList()))
            );
            MessageTimerMonitor messageTimer = MessageTimerMonitor.buildMonitor(
                    componentName, meterRegistry,
                    message -> Tags.of(TagsUtil.PAYLOAD_TYPE_TAG, message.getPayloadType().getSimpleName())
                            .and(message.getMetaData().entrySet().stream()
                                    .map(s -> Tag.of(s.getKey(), s.getValue().toString()))
                                    .collect(Collectors.toList()))
            );

            CapacityMonitor capacityMonitor1Minute = CapacityMonitor.buildMonitor(
                    componentName, meterRegistry,
                    message -> Tags.of(TagsUtil.PAYLOAD_TYPE_TAG, message.getPayloadType().getSimpleName())
                            .and(message.getMetaData().entrySet().stream()
                                    .map(s -> Tag.of(s.getKey(), s.getValue().toString()))
                                    .collect(Collectors.toList()))
            );

            return new MultiMessageMonitor<>(messageCounter, messageTimer, capacityMonitor1Minute);
        };
        configurer.configureMessageMonitor(CommandBus.class, messageMonitorFactory);
    }

    private void instrumentQueryBus(MeterRegistry meterRegistry, Configurer configurer) {
        MessageMonitorFactory messageMonitorFactory = (configuration, componentType, componentName) -> {
            MessageCountingMonitor messageCounter = MessageCountingMonitor.buildMonitor(
                    componentName, meterRegistry,
                    message -> Tags.of(TagsUtil.PAYLOAD_TYPE_TAG, message.getPayloadType().getSimpleName())
                            .and(message.getMetaData().entrySet().stream()
                                    .map(s -> Tag.of(s.getKey(), s.getValue().toString()))
                                    .collect(Collectors.toList()))
            );
            MessageTimerMonitor messageTimer = MessageTimerMonitor.buildMonitor(
                    componentName, meterRegistry,
                    message -> Tags.of(TagsUtil.PAYLOAD_TYPE_TAG, message.getPayloadType().getSimpleName())
                            .and(message.getMetaData().entrySet().stream()
                                    .map(s -> Tag.of(s.getKey(), s.getValue().toString()))
                                    .collect(Collectors.toList()))
            );
            CapacityMonitor capacityMonitor1Minute = CapacityMonitor.buildMonitor(
                    componentName, meterRegistry,
                    message -> Tags.of(TagsUtil.PAYLOAD_TYPE_TAG, message.getPayloadType().getSimpleName())
                            .and(message.getMetaData().entrySet().stream()
                                    .map(s -> Tag.of(s.getKey(), s.getValue().toString()))
                                    .collect(Collectors.toList()))
            );

            return new MultiMessageMonitor<>(messageCounter, messageTimer, capacityMonitor1Minute);
        };
        configurer.configureMessageMonitor(QueryBus.class, messageMonitorFactory);
    }
}
