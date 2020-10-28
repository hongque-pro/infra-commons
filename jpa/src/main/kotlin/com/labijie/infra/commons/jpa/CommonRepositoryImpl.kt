package com.labijie.infra.commons.jpa

import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.util.Assert
import java.util.*
import javax.persistence.EntityManager

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-10-23
 */
class CommonRepositoryImpl<T, Id>(entityInformation: JpaEntityInformation<T, *>, private val entityManager: EntityManager) : SimpleJpaRepository<T, Id>(entityInformation, entityManager), CommonRepository<T, Id> {

    override fun insert(entity: T): T {
        this.entityManager.persist(entity)
        return entity
    }

    override fun insertAll(entities: Iterable<T>):List<T> {
        Assert.notNull(entities, "The given Iterable of entities not be null!")

        val result = ArrayList<T>()

        for (entity in entities) {
            result.add(insert(entity))
        }

        return result
    }

    override fun insertAndFlush(entity: T): T {
        //this.entityManager.clear()
        this.entityManager.persist(entity)
        this.flush()
        return entity
    }

    override fun updateAndFlush(entity: T): T {
        //this.entityManager.clear()
        this.entityManager.merge(entity)
        this.flush()
        return entity
    }

    //    constructor(domainClass: Class<T>?, em: EntityManager) : super(domainClass, em){
//        this.entityManager = em
//    }
}