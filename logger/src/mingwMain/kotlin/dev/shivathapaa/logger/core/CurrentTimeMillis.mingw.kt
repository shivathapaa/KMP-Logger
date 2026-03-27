package dev.shivathapaa.logger.core

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.windows.FILETIME
import platform.windows.GetSystemTimeAsFileTime

@OptIn(ExperimentalForeignApi::class)
internal actual fun currentTimeMillis(): Long = memScoped {
    val ft = alloc<FILETIME>()
    GetSystemTimeAsFileTime(ft.ptr)
    val windows100Ns = (ft.dwHighDateTime.toLong() shl 32) or ft.dwLowDateTime.toLong()
    // Windows FILETIME epoch is Jan 1 1601; Unix epoch is Jan 1 1970
    // Difference is 11644473600 seconds
    (windows100Ns - 116444736000000000L) / 10000L
}