package com.ondra.pdfmerge.service

import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import com.ondra.pdfmerge.model.FileMetaData
import com.ondra.pdfmerge.model.MergeSpecification
import org.ehcache.CacheManager
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream
import java.util.UUID

@Service
class MergePdfService(private val cacheManager: CacheManager) {

    /**
     * Function for caching a PDF file
     * @param file the PDF file that should be cached
     * @return an object containing the file ID (id), the size (fileSize) and the number of pages (numberOfPages) of the document
     */
    fun cacheFile(file: MultipartFile): FileMetaData {
        val fileId = UUID.randomUUID().toString()
        cacheManager.getCache("fileCache", String::class.java, ByteArray::class.java).put(fileId, file.bytes)
        return FileMetaData(id = fileId, size = file.bytes.size, numberOfPages = PdfReader(file.bytes).numberOfPages)
    }

    /**
     * Function for merging entire PDF files.
     * @param fileIds a list of file IDs of the files to be merged
     * @return a ByteArray of the merged PDF file
     */
    fun mergeFiles(fileIds: List<String>): ByteArray {
        val cache = cacheManager.getCache("fileCache", String::class.java, ByteArray::class.java)

        val document = Document()
        val outputStream = ByteArrayOutputStream()
        val pdfCopy = PdfCopy(document, outputStream)

        document.open()

        fileIds.forEach {
            val pdfReader = PdfReader(cache.get(it))
            val numberOfPages = pdfReader.numberOfPages
            for (i in 1..numberOfPages) {
                pdfCopy.addPage(pdfCopy.getImportedPage(pdfReader,i))
            }
            pdfReader.close()
        }

        document.close()

        return outputStream.toByteArray()
    }

    /**
     * Function for merging specific pages of PDF files
     * @param mergeSpecification an object containing information about how the files should be merged
     * @return a ByteArray of the merged PDF file
     */
    fun mergePages(mergeSpecification: MergeSpecification): ByteArray {
        val cache = cacheManager.getCache("fileCache", String::class.java, ByteArray::class.java)

        val document = Document()
        val outputStream = ByteArrayOutputStream()
        val pdfCopy = PdfCopy(document, outputStream)

        document.open()

        mergeSpecification.fileSpecifications.forEach { fileSpec ->
            val pdfReader = PdfReader(cache.get(fileSpec.fileId))
            fileSpec.pageNumbers.forEach { pageNumber ->
                pdfCopy.addPage(pdfCopy.getImportedPage(pdfReader, pageNumber))
            }
            pdfReader.close()
        }

        document.close()

        return outputStream.toByteArray()
    }

}