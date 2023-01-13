package com.ondra.pdfmerge.service

import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import com.ondra.pdfmerge.model.MergeSpecification
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream

@Service
class MergePdfService {

    fun mergeFiles(files: Array<MultipartFile>): ByteArray {
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

        return outputStream.toByteArray()
    }

    fun mergePages(files: Array<MultipartFile>, mergeSpecification: MergeSpecification): ByteArray {
        val document = Document()
        val outputStream = ByteArrayOutputStream()
        val pdfCopy = PdfCopy(document, outputStream)

        val pdfReaderList = files.map { PdfReader(it.inputStream) }

        document.open()

        mergeSpecification.fileSpecifications.forEach { fileSpec ->
            fileSpec.pageNumbers.forEach { pageNumber ->
                pdfCopy.addPage(pdfCopy.getImportedPage(pdfReaderList[fileSpec.fileNumber], pageNumber))
            }
        }
        //since the same file can occur multiple times in a mergeSpec, it is necessary to wait until the merging process is over before the readers can be closed
        pdfReaderList.forEach { it.close() }

        document.close()

        return outputStream.toByteArray()
    }

}