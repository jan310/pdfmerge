package com.ondra.pdfmerge.controller

import com.ondra.pdfmerge.model.MergeSpecification
import com.ondra.pdfmerge.service.MergePdfService
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@CrossOrigin
@RestController
@RequestMapping("/api/pdf")
class MergePdfController(private val mergePdfService: MergePdfService) {

    @PostMapping("/merge-files", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], produces = [MediaType.APPLICATION_PDF_VALUE])
    fun mergeFiles(@RequestPart files: Array<MultipartFile>): ResponseEntity<ByteArray> {
        val headers = HttpHeaders()
        headers.contentDisposition = ContentDisposition.builder("attachment").filename("merged.pdf").build()
        return ResponseEntity(mergePdfService.mergeFiles(files), headers, HttpStatus.CREATED)
    }

    @PostMapping("/merge-pages", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], produces = [MediaType.APPLICATION_PDF_VALUE])
    fun mergePages(@RequestPart files: Array<MultipartFile>, @RequestPart mergeSpecification: MergeSpecification): ResponseEntity<ByteArray> {
        val headers = HttpHeaders()
        headers.contentDisposition = ContentDisposition.builder("attachment").filename("merged.pdf").build()
        return ResponseEntity(mergePdfService.mergePages(files, mergeSpecification), headers, HttpStatus.CREATED)
    }

}