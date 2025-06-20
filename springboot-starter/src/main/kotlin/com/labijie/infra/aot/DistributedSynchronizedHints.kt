/**
 * @author Anders Xiao
 * @date 2025-06-14
 */
package com.labijie.infra.aot


import com.labijie.infra.annotation.DistributedSynchronized
import com.labijie.infra.snowflake.ISlotProvider
import com.labijie.infra.snowflake.SnowflakeIdGenerator
import com.labijie.infra.snowflake.providers.JdbcSlotProvider
import com.labijie.infra.snowflake.providers.RedisSlotProvider
import com.labijie.infra.snowflake.providers.StaticSlotProvider
import com.labijie.infra.snowflake.providers.ZookeeperSlotProvider
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.aot.hint.TypeReference

class DistributedSynchronizedHints : RuntimeHintsRegistrar {
    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        hints.reflection().registerType(
            DistributedSynchronized::class.java
        ) {
            it.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS)
                .withMembers(MemberCategory.INVOKE_DECLARED_METHODS)
                .withMembers(MemberCategory.DECLARED_FIELDS)
                .withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
        }

        hints.reflection().registerType(ISlotProvider::class.java)
        hints.reflection().registerType(JdbcSlotProvider::class.java)
        hints.reflection().registerType(RedisSlotProvider::class.java)
        hints.reflection().registerType(ZookeeperSlotProvider::class.java)
        hints.reflection().registerType(StaticSlotProvider::class.java)

        hints.reflection().registerType(SnowflakeIdGenerator::class.java)
        hints.reflection().registerType(TypeReference.of("io.etcd.jetcd.Client"))
    }
}