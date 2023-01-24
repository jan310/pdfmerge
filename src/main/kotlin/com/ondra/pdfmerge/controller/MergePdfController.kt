package com.ondra.pdfmerge.controller

import com.ondra.pdfmerge.model.FileIds
import com.ondra.pdfmerge.model.FileMetaData
import com.ondra.pdfmerge.model.MergeSpecification
import com.ondra.pdfmerge.service.MergePdfService
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@CrossOrigin
@RestController
@RequestMapping("/api/pdf")
class MergePdfController(private val mergePdfService: MergePdfService) {

    /**
     * Endpoint for caching a PDF file.
     * @param file the PDF file that should be cached
     * @return an object containing the file ID (id), the size (fileSize) and the number of pages (numberOfPages) of the document
     */
    @PostMapping("/cache-file", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun cacheFile(@RequestPart file: MultipartFile): ResponseEntity<FileMetaData> {
        return ResponseEntity(mergePdfService.cacheFile(file), HttpStatus.OK)
    }

    /**
     * Endpoint for merging entire PDF files.
     * @param fileIds an object containing a list of file IDs of the files to be merged
     * @return a ByteArray of the merged PDF file
     */
    @PostMapping("/merge-files", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun mergeFiles(@RequestBody fileIds: FileIds): ResponseEntity<ByteArray> {
        val headers = HttpHeaders()
        headers.contentDisposition = ContentDisposition.builder("attachment").filename("merged.pdf").build()
        return ResponseEntity(mergePdfService.mergeFiles(fileIds.ids), headers, HttpStatus.CREATED)
    }

    /**
     * Endpoint for merging specific pages of PDF files
     * @param mergeSpecification an object containing information about how the files should be merged
     * @return a ByteArray of the merged PDF file
     */
    @PostMapping("/merge-pages", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun mergePages(@RequestBody mergeSpecification: MergeSpecification): ResponseEntity<ByteArray> {
        val headers = HttpHeaders()
        headers.contentDisposition = ContentDisposition.builder("attachment").filename("merged.pdf").build()
        return ResponseEntity(mergePdfService.mergePages(mergeSpecification), headers, HttpStatus.CREATED)
    }

}