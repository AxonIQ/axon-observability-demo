package com.demo.web;

import com.demo.api.AddCmd;
import com.demo.api.FindAllGiftCardQry;
import com.demo.api.FindGiftCardQry;
import com.demo.api.GiftCardRecord;
import com.demo.api.IssueCmd;
import com.demo.api.IssuedEvt;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.UUID;

/**
 * Repository REST Controller for handling 'commands' only
 * <p>
 * Sometimes you may want to write a custom handler for a specific resource. To take advantage of Spring Data REST’s
 * settings, message converters, exception handling, and more, we use the @RepositoryRestController annotation instead
 * of a standard Spring MVC @Controller or @RestController
 */
@RestController
public class AxonDemoRestController {


    private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final org.axonframework.eventhandling.EventBus eventBus;


    public AxonDemoRestController(CommandGateway commandGateway, QueryGateway queryGateway, EventBus eventBus) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
        this.eventBus = eventBus;
    }

    @GetMapping(value = "/send/query")
    public void sendQuery(@RequestParam int count, @RequestParam int delay) throws InterruptedException {
        for (int i = 0; i < count; i++) {
            log.info("Sending {}/{} queries delayed by {}", i + 1, count, delay);
            queryGateway.query(new FindAllGiftCardQry(), ResponseTypes.multipleInstancesOf(GiftCardRecord.class));
            Thread.sleep(delay);
        }
    }

    @GetMapping(value = "/send/query/subscription")
    public void subscriptionQuery(@RequestParam int count, @RequestParam int delay, @RequestParam int worktime)
            throws InterruptedException {
        for (int i = 0; i < count; i++) {
            final String giftCardId = UUID.randomUUID().toString();

            log.info("Sending {}/{} commands with subscription query delayed by {}, with work time: {}",
                     i + 1,
                     count,
                     delay,
                     worktime);

            try (SubscriptionQueryResult<GiftCardRecord, GiftCardRecord> queryResult = queryGateway.subscriptionQuery(
                    new FindGiftCardQry(giftCardId),
                    ResponseTypes.instanceOf(GiftCardRecord.class),
                    ResponseTypes.instanceOf(GiftCardRecord.class))) {
                commandGateway.sendAndWait(new IssueCmd(giftCardId, worktime));

                queryResult.updates().timeout(Duration.ofSeconds(1)).onErrorResume(t -> Mono.empty()).subscribe();
            }

            Thread.sleep(delay);
        }
    }

    @GetMapping(value = "/send/events")
    public void sendEvents(@RequestParam int count, @RequestParam int delay) throws InterruptedException {
        final String giftCardId = UUID.randomUUID().toString();
        commandGateway.sendAndWait(new IssueCmd(giftCardId, 1));

        for (int i = 0; i < count; i++) {
            log.info("Sending {}/{} events delayed by {}", i + 1, count, delay);
            eventBus.publish(GenericEventMessage.asEventMessage(new IssuedEvt(giftCardId, i)));
            Thread.sleep(delay);
        }
    }

    @GetMapping(value = "/send/commands/new-aggregate")
    public void sendCommands(@RequestParam int count, @RequestParam int delay, @RequestParam int worktime)
            throws InterruptedException {
        for (int i = 0; i < count; i++) {
            log.info("Sending {}/{} commands delayed by {}, with work time: {}", i + 1, count, delay, worktime);
            commandGateway.send(new IssueCmd(UUID.randomUUID().toString(), worktime));
            Thread.sleep(delay);
        }
    }

    @GetMapping(value = "/send/commands/aggregate")
    public void sendAggregateCommands(@RequestParam int count) {

        log.info("Starting!");

        final String giftCardId = UUID.randomUUID().toString();

        commandGateway.sendAndWait(new IssueCmd(giftCardId, 1));
        for (int i = 0; i < count; i++) {

            commandGateway.sendAndWait(new AddCmd(giftCardId, 1));

            log.info("Done!");
        }
    }
}