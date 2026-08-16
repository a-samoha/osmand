package com.samos.osmand.domain.network

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.value
import platform.SystemConfiguration.SCNetworkReachabilityCreateWithAddress
import platform.SystemConfiguration.SCNetworkReachabilityGetFlags
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsConnectionRequired
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsReachable
import platform.posix.AF_INET
import platform.posix.sockaddr
import platform.posix.sockaddr_in

class IosNetworkMonitor : NetworkMonitor {

    @OptIn(ExperimentalForeignApi::class)
    override val isOnline: Boolean
        get() = memScoped {
            val zeroAddress = alloc<sockaddr_in>()
            zeroAddress.sin_len = sizeOf<sockaddr_in>().toUByte()
            zeroAddress.sin_family = AF_INET.toUByte()

            // 💡 FIX: Cast CPointer<sockaddr_in> to CPointer<sockaddr> using .reinterpret()
            val sockaddrPtr = zeroAddress.ptr.reinterpret<sockaddr>()

            val reachability =
                SCNetworkReachabilityCreateWithAddress(null, sockaddrPtr) ?: return false
            val flags = alloc<platform.SystemConfiguration.SCNetworkReachabilityFlagsVar>()

            if (SCNetworkReachabilityGetFlags(reachability, flags.ptr)) {
                val isReachable = (flags.value and kSCNetworkReachabilityFlagsReachable) != 0U
                val needsConnection =
                    (flags.value and kSCNetworkReachabilityFlagsConnectionRequired) != 0U
                isReachable && !needsConnection
            } else {
                false
            }
        }
}
