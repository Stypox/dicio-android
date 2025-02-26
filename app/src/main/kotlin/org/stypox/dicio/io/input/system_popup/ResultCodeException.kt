package org.stypox.dicio.io.input.system_popup

class ResultCodeException(resultCode: Int) : Exception("Invalid activity result code: $resultCode")