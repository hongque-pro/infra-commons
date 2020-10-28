package com.labijie.infra.commons.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-10-23
 */
@NoRepositoryBean
interface CommonRepository<T, ID>: JpaRepository<T, ID> {
    fun insertAndFlush(entity:T):T
    fun updateAndFlush(entity:T):T

    fun insertAll(entities:Iterable<T>):List<T>

    fun insert(entity:T):T
}