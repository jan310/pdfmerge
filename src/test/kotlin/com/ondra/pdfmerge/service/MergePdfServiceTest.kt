package com.ondra.pdfmerge.service

import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import com.ondra.pdfmerge.model.FileSpecification
import com.ondra.pdfmerge.model.MergeSpecification
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import org.springframework.mock.web.MockMultipartFile
import java.io.File
import java.util.UUID

class MergePdfServiceTest {

    private val cacheManager = mock(CacheManager::class.java)
    private val cache: Cache<String,ByteArray> = mock(Cache::class.java) as Cache<String, ByteArray>

    private val cut = MergePdfService(cacheManager)

    private val doc1 = File("src/test/resources/doc1.pdf").readBytes()
    private val doc2 = File("src/test/resources/doc2.pdf").readBytes()
    private val doc3 = File("src/test/resources/doc3.pdf").readBytes()

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
    inner class CacheFile{
        @Test
        fun checksIfMethodReturnsExpectedMetaData() {
            //given
            `when`(cacheManager.getCache(any(), eq(String::class.java), eq(ByteArray::class.java))).thenReturn(cache)
            doNothing().`when`(cache).put(any(), any())
            val expectedFileSize = 39469
            val expectedNumberOfPages = 4

            //when
            val expectedMetaData = cut.cacheFile(MockMultipartFile("file", doc1))
            val actualFileSize = expectedMetaData.size
            val actualNumberOfPages = expectedMetaData.numberOfPages

            //then
            assertEquals(expectedFileSize, actualFileSize)
            assertEquals(expectedNumberOfPages, actualNumberOfPages)
        }
    }

    @Nested
    inner class MergeFiles {

        private val fileId1 = UUID.randomUUID().toString()
        private val fileId2 = UUID.randomUUID().toString()
        private val fileId3 = UUID.randomUUID().toString()
        private val mergedFiles = File("src/test/resources/merged-files.pdf").readBytes()

        @Test
        fun checksIfFilesAreMergedCorrectly() {
            //given
            val fileIds = listOf(fileId1, fileId2, fileId3)
            `when`(cacheManager.getCache(any(), eq(String::class.java), eq(ByteArray::class.java))).thenReturn(cache)
            `when`(cache.get(fileId1)).thenReturn(doc1)
            `when`(cache.get(fileId2)).thenReturn(doc2)
            `when`(cache.get(fileId3)).thenReturn(doc3)
            val expectedPdf = mergedFiles

            //when
            val actualPdf = cut.mergeFiles(fileIds)

            //then
            assertTrue(compareFilesByContent(expectedPdf, actualPdf))
        }

        @Test
        fun checksIfDifferentFileOrderCreatesDifferentMergedFile() {
            //given
            val fileIds = listOf(fileId3, fileId2, fileId1)
            `when`(cacheManager.getCache(any(), eq(String::class.java), eq(ByteArray::class.java))).thenReturn(cache)
            `when`(cache.get(fileId1)).thenReturn(doc1)
            `when`(cache.get(fileId2)).thenReturn(doc2)
            `when`(cache.get(fileId3)).thenReturn(doc3)
            val unexpectedPdf = mergedFiles

            //when
            val actualPdf = cut.mergeFiles(fileIds)

            //then
            assertFalse(compareFilesByContent(unexpectedPdf, actualPdf))
        }

    }

    @Nested
    inner class MergePages {

        private val fileId1 = UUID.randomUUID().toString()
        private val fileId2 = UUID.randomUUID().toString()
        private val fileId3 = UUID.randomUUID().toString()
        private val mergedPages = File("src/test/resources/merged-pages.pdf").readBytes()

        @Test
        fun checksIfPagesAreMergedCorrectly() {
            //given
            val mergeSpecification = MergeSpecification(listOf(
                FileSpecification(fileId3, listOf(3,7)),
                FileSpecification(fileId1, listOf(1,2,4)),
                FileSpecification(fileId3, listOf(1)),
                FileSpecification(fileId2, listOf(2,1)),
            ))
            `when`(cacheManager.getCache(any(), eq(String::class.java), eq(ByteArray::class.java))).thenReturn(cache)
            `when`(cache.get(fileId1)).thenReturn(doc1)
            `when`(cache.get(fileId2)).thenReturn(doc2)
            `when`(cache.get(fileId3)).thenReturn(doc3)
            val expectedPdf = mergedPages

            //when
            val actualPdf = cut.mergePages(mergeSpecification)

            //then
            assertTrue(compareFilesByContent(expectedPdf, actualPdf))
        }

        @Test
        fun checksIfDifferentMergeSpecCreatesDifferentMergedFile() {
            //given
            val mergeSpecification = MergeSpecification(listOf(
                FileSpecification(fileId3, listOf(3, 7)),
                FileSpecification(fileId2, listOf(2, 1)),
            ))
            `when`(cacheManager.getCache(any(), eq(String::class.java), eq(ByteArray::class.java))).thenReturn(cache)
            `when`(cache.get(fileId2)).thenReturn(doc2)
            `when`(cache.get(fileId3)).thenReturn(doc3)
            val unexpectedPdf = mergedPages

            //when
            val actualPdf = cut.mergePages(mergeSpecification)

            //then
            assertFalse(compareFilesByContent(unexpectedPdf, actualPdf))
        }

    }
}