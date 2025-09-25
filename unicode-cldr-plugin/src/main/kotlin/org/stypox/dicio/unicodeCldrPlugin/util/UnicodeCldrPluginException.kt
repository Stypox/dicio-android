package org.stypox.dicio.unicodeCldrPlugin.util

class UnicodeCldrPluginException : Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
