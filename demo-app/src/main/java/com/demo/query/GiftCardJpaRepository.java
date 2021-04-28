package com.demo.query;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface GiftCardJpaRepository extends CrudRepository<GiftCardEntity, String> {


}
