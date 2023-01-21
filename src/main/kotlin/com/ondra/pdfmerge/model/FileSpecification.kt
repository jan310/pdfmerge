package com.ondra.pdfmerge.model

data class FileSpecification(
    val fileId: String,
    val pageNumbers: List<Int>
)
