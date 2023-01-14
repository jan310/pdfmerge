package com.ondra.pdfmerge.service

import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.ondra.pdfmerge.model.FileSpecification
import com.ondra.pdfmerge.model.MergeSpecification
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import java.io.File

class MergePdfServiceTest {

    private val cut = MergePdfService()

    private val doc1: MultipartFile = MockMultipartFile("files", File("src/test/resources/doc1.pdf").readBytes())
    private val doc2: MultipartFile = MockMultipartFile("files", File("src/test/resources/doc2.pdf").readBytes())
    private val doc3: MultipartFile = MockMultipartFile("files", File("src/test/resources/doc3.pdf").readBytes())
    private val mergedFiles = File("src/test/resources/merged-files.pdf").readBytes()
    private val mergedPages = File("src/test/resources/merged-pages.pdf").readBytes()

    private fun compareFilesByContent(file1: ByteArray, file2: ByteArray): Boolean {
        val reader1 = PdfReader(file1)
        val reader2 = PdfReader(file2)

        if (reader1.numberOfPages != reader2.numberOfPages) {
            reader1.close()
            reader2.close()
            return false
        }

        for (i in 1 .. reader1.numberOfPages) {
            if (PdfTextExtractor.getTextFromPage(reader1, i) != PdfTextExtractor.getTextFromPage(reader2, i)) {
                reader1.close()
                reader2.close()
                return false
            }
        }

        reader1.close()
        reader2.close()
        return true
    }

    @Nested
    inner class MergeFiles {

        @Test
        fun checksIfFilesAreMergedCorrectly() {
            //given
            val expectedPdf = mergedFiles

            //when
            val actualPdf = cut.mergeFiles(arrayOf(doc1, doc2, doc3))

            //then
            assertTrue(compareFilesByContent(expectedPdf, actualPdf))
        }

        @Test
        fun checksIfDifferentFileOrderCreatesDifferentMergedFile() {
            //given
            val unexpectedPdf = mergedFiles

            //when
            val actualPdf = cut.mergeFiles(arrayOf(doc2, doc3, doc1))

            //then
            assertFalse(compareFilesByContent(unexpectedPdf, actualPdf))
        }

    }

    @Nested
    inner class MergePages {

        @Test
        fun checksIfPagesAreMergedCorrectly() {
            //given
            val mergeSpecification = MergeSpecification(listOf(
                FileSpecification(2, listOf(3, 7)),
                FileSpecification(0, listOf(1, 2, 4)),
                FileSpecification(2, listOf(1)),
                FileSpecification(1, listOf(2, 1)),
            ))
            val expectedPdf = mergedPages

            //when
            val actualPdf = cut.mergePages(arrayOf(doc1, doc2, doc3), mergeSpecification)

            //then
            assertTrue(compareFilesByContent(expectedPdf, actualPdf))
        }

        @Test
        fun checksIfDifferentMergeSpecCreatesDifferentMergedFile() {
            //given
            val mergeSpecification = MergeSpecification(listOf(
                FileSpecification(2, listOf(3, 7)),
                FileSpecification(1, listOf(2, 1)),
            ))
            val unexpectedPdf = mergedPages

            //when
            val actualPdf = cut.mergePages(arrayOf(doc1, doc2, doc3), mergeSpecification)

            //then
            assertFalse(compareFilesByContent(unexpectedPdf, actualPdf))
        }

    }
}