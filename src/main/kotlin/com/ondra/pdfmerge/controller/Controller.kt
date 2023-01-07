package com.ondra.pdfmerge.controller

import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import com.ondra.pdfmerge.model.MergeSpecification
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream

@RestController
@RequestMapping("/api/pdf")
class Controller {

    @PostMapping("/merge-files", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], produces = [MediaType.APPLICATION_PDF_VALUE])
    fun mergeFiles(@RequestPart files: Array<MultipartFile>): ResponseEntity<ByteArray> {
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
            pdfReader.close()
        }

        document.close()

        return ResponseEntity(outputStream.toByteArray(), HttpStatus.CREATED)
    }

    @PostMapping("/merge-pages", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], produces = [MediaType.APPLICATION_PDF_VALUE])
    fun mergePages(@RequestPart files: Array<MultipartFile>, @RequestPart mergeSpecification: MergeSpecification): ResponseEntity<ByteArray> {
        val document = Document()
        val outputStream = ByteArrayOutputStream()
        val pdfCopy = PdfCopy(document, outputStream)

        val pdfReaderList = files.map { PdfReader(it.inputStream) }

        document.open()

        mergeSpecification.fileSpecifications.forEach { fileSpec ->
            fileSpec.pageNumbers.forEach { pageNumber ->
                pdfCopy.addPage(pdfCopy.getImportedPage(pdfReaderList[fileSpec.fileNumber], pageNumber))
            }
            pdfReaderList[fileSpec.fileNumber].close()
        }

        document.close()

        return ResponseEntity(outputStream.toByteArray(), HttpStatus.CREATED)
    }

}