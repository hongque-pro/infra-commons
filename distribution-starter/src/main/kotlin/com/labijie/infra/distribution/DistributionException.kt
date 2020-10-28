package com.labijie.infra.distribution

import com.labijie.infra.InfrastructureException

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-09-19
 */
class DistributionException @JvmOverloads constructor(message: String?, cause: Throwable? = null) : InfrastructureException(message, cause) {
}