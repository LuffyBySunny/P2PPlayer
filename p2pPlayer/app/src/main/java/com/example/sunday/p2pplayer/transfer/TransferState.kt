package com.example.sunday.p2pplayer.transfer

/**
 *Created by sunday on 19-4-24.
 */
enum class TransferState {
    FINISHING,
    CHECKING,
    DOWNLOADING_METADATA,
    DOWNLOADING_TORRENT,
    DOWNLOADING,
    FINISHED,
    SEEDING,
    ALLOCATING,
    PAUSED,
    ERROR,
    ERROR_MOVING_INCOMPLETE,
    ERROR_HASH_MD5,
    ERROR_SIGNATURE,
    ERROR_NOT_ENOUGH_PEERS,
    ERROR_NO_INTERNET,
    ERROR_SAVE_DIR,
    ERROR_TEMP_DIR,
    STOPPED,
    PAUSING,
    CANCELING,
    CANCELED,
    WAITING,
    COMPLETE,
    UPLOADING,
    UNCOMPRESSING,
    DEMUXING,
    UNKNOWN,
    ERROR_DISK_FULL,
    REDIRECTING,
    STREAMING,
    SCANNING,
    ERROR_CONNECTION_TIMED_OUT;

    fun isErrored(state: TransferState): Boolean {
        return state == ERROR ||
                state == ERROR_MOVING_INCOMPLETE ||
                state == ERROR_HASH_MD5 ||
                state == ERROR_SIGNATURE ||
                state == ERROR_NOT_ENOUGH_PEERS ||
                state == ERROR_NO_INTERNET ||
                state == ERROR_SAVE_DIR ||
                state == ERROR_TEMP_DIR ||
                state == ERROR_DISK_FULL ||
                state == ERROR_CONNECTION_TIMED_OUT
    }
}