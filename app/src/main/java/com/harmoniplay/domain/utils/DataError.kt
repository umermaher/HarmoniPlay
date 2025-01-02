package com.harmoniplay.domain.utils

sealed interface DataError: Error {

    enum class Local: DataError {
        DISK_FULL,
        PERMISSION_REQUIRED,
        DISK_EMPTY,
        FAILED_TO_GET,
    }

    enum class General: DataError {
        NO_NEED_TO_PROCESS
    }

}