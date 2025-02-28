package org.stypox.dicio.io.input.external_popup

class ResultCodeException(resultCode: Int) : Exception("Invalid activity result code: $resultCode")