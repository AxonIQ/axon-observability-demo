package com.demo.query;

import com.demo.api.FindAllGiftCardQry;
import com.demo.api.FindGiftCardQry;
import com.demo.api.GiftCardRecord;
import com.demo.api.IssuedEvt;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
class GiftCardHandler {

    private final GiftCardJpaRepository giftCardJpaRepository;
    private final QueryUpdateEmitter queryUpdateEmitter;

    public GiftCardHandler(GiftCardJpaRepository giftCardJpaRepository, QueryUpdateEmitter queryUpdateEmitter) {
        this.giftCardJpaRepository = giftCardJpaRepository;
        this.queryUpdateEmitter = queryUpdateEmitter;
    }

    @EventHandler
    void on(IssuedEvt event) {
        /*
         * Update our read model by inserting the new card.
         */
        giftCardJpaRepository.save(new GiftCardEntity(event.getId(), event.getAmount(), event.getAmount()));

        /* Send it to subscription queries of type FindGiftCardQry, but only if the card id matches. */
        queryUpdateEmitter.emit(FindGiftCardQry.class, findGiftCardQry -> Objects.equals(event.getId(), findGiftCardQry.getId()), new GiftCardRecord(event.getId(), event.getAmount(), event.getAmount()));

    }

    @QueryHandler
    GiftCardRecord handle(FindGiftCardQry query) {
        GiftCardEntity giftCardEntity = giftCardJpaRepository.findById(query.getId()).orElse(new GiftCardEntity());
        return new GiftCardRecord(giftCardEntity.getId(), giftCardEntity.getInitialValue(), giftCardEntity.getRemainingValue());
    }

    @QueryHandler
    List<GiftCardRecord> handle(FindAllGiftCardQry query) {
        return StreamSupport.stream(giftCardJpaRepository.findAll().spliterator(), false)
                .map(giftCardEntity->new GiftCardRecord(giftCardEntity.getId(), giftCardEntity.getInitialValue(), giftCardEntity.getRemainingValue()))
                .collect(Collectors.toList());
    }
}
