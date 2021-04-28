package com.demo;

import io.micrometer.core.instrument.MeterRegistry;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.Snapshotter;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"com.demo.query"})
@EnableCaching(proxyTargetClass = true)
public class AxonDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AxonDemoApplication.class, args);
    }

    /**
     * Aggregate snapshot definition
     *
     * @param snapshotter
     * @return SnapshotTriggerDefinition
     */
    @Bean
    public SnapshotTriggerDefinition snapshotTriggerDefinition(Snapshotter snapshotter) {
        return new EventCountSnapshotTriggerDefinition(snapshotter, 2);
    }

    /**
     * Register dummy command dispatch interceptor
     *
     * @param commandGateway
     * @param userIdCommandDispatcher
     */
    @Autowired
    void registerInterceptors(CommandGateway commandGateway, UserIdCommandDispatcher userIdCommandDispatcher) {
        commandGateway.registerDispatchInterceptor(userIdCommandDispatcher);
    }

    /**
     * Register dummy command dispatch interceptor
     *
     * @param queryGateway
     * @param userIdQueryDispatcher
     */
    @Autowired
    void registerInterceptors(QueryGateway queryGateway, UserIdQueryDispatcher userIdQueryDispatcher) {
        queryGateway.registerDispatchInterceptor(userIdQueryDispatcher);
    }

    /**
     * Meter Customizer
     *
     * Customize the name of the application
     *
     * @return MeterRegistryCustomizer
     */
    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags("app", "demo-app");
    }

    /**
     * A dummy command dispatch interceptor that is adding `user id` to the metadata of the command message
     */
    @Component
    class UserIdCommandDispatcher implements MessageDispatchInterceptor<CommandMessage<?>> {
        static final String USER_ID = "userId";

        @Override
        public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(List<? extends CommandMessage<?>> messages) {
            return (i, message) -> message.andMetaData(Collections.singletonMap(USER_ID, "JohnDoe"));
        }
    }

    /**
     * A dummy query dispatch interceptor that is adding `user id` to the metadata of the query message
     */
    @Component
    class UserIdQueryDispatcher implements MessageDispatchInterceptor<QueryMessage<?, ?>> {
        static final String USER_ID = "userId";

        @Override
        public BiFunction<Integer, QueryMessage<?, ?>, QueryMessage<?, ?>> handle(List<? extends QueryMessage<?, ?>> messages) {
            return (i, message) -> message.andMetaData(Collections.singletonMap(USER_ID, "DoeJohn"));
        }
    }

}
