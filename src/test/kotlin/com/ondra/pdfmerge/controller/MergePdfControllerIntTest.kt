package com.ondra.pdfmerge.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ondra.pdfmerge.model.FileIds
import com.ondra.pdfmerge.model.FileMetaData
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
import java.util.UUID


@WebMvcTest(MergePdfController::class)
class MergePdfControllerIntTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockBean
    private lateinit var mergePdfService: MergePdfService

    @Nested
    inner class CacheFile {
        @Test
        fun checksIfEndpointReturnsExpectedResult() {
            val fileMetaData = FileMetaData(UUID.randomUUID().toString(), 39469, 4)
            val multipartFile = MockMultipartFile("file", ByteArray(1))
            `when`(mergePdfService.cacheFile(multipartFile)).thenReturn(fileMetaData)

            mvc.perform(MockMvcRequestBuilders.multipart("/api/pdf/cache-file").file(multipartFile))
                .andExpect(status().isOk)
                .andExpect(content().json(ObjectMapper().writeValueAsString(fileMetaData)))
        }
    }

    @Nested
    inner class MergeFiles {
        @Test
        fun checksIfEndpointReturnsExpectedResult() {
            val fileIds = FileIds(listOf(UUID.randomUUID().toString(), UUID.randomUUID().toString(), UUID.randomUUID().toString()))
            `when`(mergePdfService.mergeFiles(fileIds.ids)).thenReturn(ByteArray(1))

            mvc.perform(MockMvcRequestBuilders.post("/api/pdf/merge-files").contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(fileIds)))
                .andExpect(status().isCreated)
                .andExpect(content().bytes(ByteArray(1)))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string("content-disposition", "attachment; filename=\"merged.pdf\""))
        }
    }

    @Nested
    inner class MergePages {
        @Test
        fun checksIfEndpointReturnsExpectedResult() {
            val mergeSpecification = MergeSpecification(listOf(
                FileSpecification(UUID.randomUUID().toString(), listOf(3,7)),
                FileSpecification(UUID.randomUUID().toString(), listOf(2,1)),
            ))
            `when`(mergePdfService.mergePages(mergeSpecification)).thenReturn(ByteArray(1))

            mvc.perform(MockMvcRequestBuilders.post("/api/pdf/merge-pages").contentType(MediaType.APPLICATION_JSON).content(ObjectMapper().writeValueAsString(mergeSpecification)))
                .andExpect(status().isCreated)
                .andExpect(content().bytes(ByteArray(1)))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string("content-disposition", "attachment; filename=\"merged.pdf\""))
        }
    }

}