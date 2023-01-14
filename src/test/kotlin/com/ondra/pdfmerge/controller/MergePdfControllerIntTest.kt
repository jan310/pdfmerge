package com.ondra.pdfmerge.controller

import com.ondra.pdfmerge.model.FileSpecification
import com.ondra.pdfmerge.model.MergeSpecification
import com.ondra.pdfmerge.service.MergePdfService
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.io.File


@WebMvcTest(MergePdfController::class)
class MergePdfControllerIntTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockBean
    private lateinit var mergePdfService: MergePdfService

    private val doc1 = MockMultipartFile("files", File("src/test/resources/doc1.pdf").readBytes())
    private val doc2 = MockMultipartFile("files", File("src/test/resources/doc2.pdf").readBytes())
    private val doc3 = MockMultipartFile("files", File("src/test/resources/doc3.pdf").readBytes())
    private val json = MockMultipartFile("mergeSpecification", "mergeSpecification", MediaType.APPLICATION_JSON_VALUE, "{\"fileSpecifications\":[{\"fileNumber\":2,\"pageNumbers\":[3,7]},{\"fileNumber\":0,\"pageNumbers\":[1,2,4]},{\"fileNumber\":2,\"pageNumbers\":[1]},{\"fileNumber\":1,\"pageNumbers\":[2,1]}]}".toByteArray())
    private val mergedFiles = File("src/test/resources/merged-files.pdf").readBytes()
    private val mergedPages = File("src/test/resources/merged-pages.pdf").readBytes()

    @Nested
    inner class MergeFiles {
        @Test
        fun checksIfEndpointReturnsExpectedResult() {
            `when`(mergePdfService.mergeFiles(arrayOf(doc1, doc2, doc3))).thenReturn(mergedFiles)

            mvc.perform(MockMvcRequestBuilders.multipart("/api/pdf/merge-files").file(doc1).file(doc2).file(doc3))
                .andExpect(status().isCreated)
                .andExpect(content().bytes(mergedFiles))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string("content-disposition", "attachment; filename=\"merged.pdf\""))
        }
    }

    @Nested
    inner class MergePages {
        @Test
        fun checksIfEndpointReturnsExpectedResult() {
            val mergeSpecification = MergeSpecification(listOf(
                FileSpecification(2, listOf(3, 7)),
                FileSpecification(0, listOf(1, 2, 4)),
                FileSpecification(2, listOf(1)),
                FileSpecification(1, listOf(2, 1)),
            ))

            `when`(mergePdfService.mergePages(arrayOf(doc1, doc2, doc3), mergeSpecification)).thenReturn(mergedPages)

            mvc.perform(MockMvcRequestBuilders.multipart("/api/pdf/merge-pages").file(doc1).file(doc2).file(doc3).file(json))
                .andExpect(status().isCreated)
                .andExpect(content().bytes(mergedPages))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string("content-disposition", "attachment; filename=\"merged.pdf\""))
        }
    }

}