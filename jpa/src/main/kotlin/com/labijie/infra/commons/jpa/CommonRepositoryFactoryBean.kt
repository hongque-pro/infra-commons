package com.labijie.infra.commons.jpa

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean
import org.springframework.data.repository.Repository
import org.springframework.data.repository.core.RepositoryMetadata
import org.springframework.data.repository.core.support.RepositoryFactorySupport
import java.io.Serializable
import javax.persistence.EntityManager

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-10-23
 */

class CommonRepositoryFactoryBean<T : Repository<S, ID>?, S, ID : Serializable>(repositoryInterface: Class<out T>)
    : JpaRepositoryFactoryBean<T, S, ID>(repositoryInterface) {


    override fun createRepositoryFactory(entityManager: EntityManager): RepositoryFactorySupport {
        return CustomRepositoryFactory<S, ID>(entityManager)
    }

    private open class CustomRepositoryFactory<T, I : Serializable>(private val entityManager: EntityManager)
        : JpaRepositoryFactory(entityManager) {

        override fun getRepositoryBaseClass(metadata: RepositoryMetadata): Class<*> {
            if(CommonRepository::class.java.isAssignableFrom(metadata.repositoryInterface)){
                return CommonRepositoryImpl::class.java
            }else {
                return super.getRepositoryBaseClass(metadata)
            }
        }
    }

}