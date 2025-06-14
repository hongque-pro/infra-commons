/**
 * @author Anders Xiao
 * @date 2025-06-14
 */
package com.labijie.infra.aot


import com.labijie.infra.annotation.DistributedSynchronized
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar

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
    }
}