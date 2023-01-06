package com.ondra.pdfmerge.controller

import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream

@RestController
@RequestMapping("/api/pdf")
class Controller {

    @PostMapping("/merge", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun mergePdfs(@RequestParam("files") files: Array<MultipartFile>): ResponseEntity<ByteArray> {
        val document = Document()
        val outputStream = ByteArrayOutputStream()
        val pdfCopy = PdfCopy(document, outputStream)

        document.open()

        files.forEach {
            val pdfReader = PdfReader(it.inputStream)
            val numberOfPages = pdfReader.numberOfPages
            for (i in 1..numberOfPages) {
                pdfCopy.addPage(pdfCopy.getImportedPage(pdfReader,i))
            }
            //println(pdfReader.info["Title"])
            pdfReader.close()
        }

        document.close()

        return ResponseEntity(outputStream.toByteArray(), HttpStatus.CREATED)
    }

}