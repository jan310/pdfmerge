package com.ondra.pdfmerge

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PdfmergeApplication

fun main(args: Array<String>) {
    runApplication<PdfmergeApplication>(*args)
}
