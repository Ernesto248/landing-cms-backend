package com.jenislashes.common.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void maxUploadSizeExceeded_should_return_bad_request() throws Exception {
        mockMvc.perform(get("/test/max-upload"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La imagen excede el limite de 10MB"));
    }

    @Test
    void missingServletRequestPart_should_return_bad_request() throws Exception {
        mockMvc.perform(get("/test/missing-part"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Falta la parte 'file' en el formulario"));
    }

    @Test
    void multipartException_should_return_bad_request() throws Exception {
        mockMvc.perform(get("/test/multipart-error"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La peticion multipart no es valida"));
    }

    @Test
    void httpMediaTypeNotSupported_should_return_bad_request() throws Exception {
        mockMvc.perform(get("/test/unsupported-media"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El Content-Type de la peticion no esta soportado"));
    }

    @RestController
    private static class ThrowingController {

        @GetMapping("/test/max-upload")
        void throwMaxUploadSizeExceeded() {
            throw new MaxUploadSizeExceededException(10L * 1024L * 1024L);
        }

        @GetMapping("/test/missing-part")
        void throwMissingServletRequestPart() throws MissingServletRequestPartException {
            throw new MissingServletRequestPartException("file");
        }

        @GetMapping("/test/multipart-error")
        void throwMultipartException() {
            throw new MultipartException("bad multipart") {};
        }

        @GetMapping("/test/unsupported-media")
        void throwHttpMediaTypeNotSupported() throws HttpMediaTypeNotSupportedException {
            throw new HttpMediaTypeNotSupportedException("text/plain");
        }
    }
}
