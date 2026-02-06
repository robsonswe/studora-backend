package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.request.BancaCreateRequest;
import com.studora.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StringNormalizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testJsonRequestBodyNormalization() throws Exception {
        BancaCreateRequest request = new BancaCreateRequest();
        // Input with extra spaces
        request.setNome("  FGV   (Fundação   Getúlio Vargas)  ");

        mockMvc
            .perform(
                post("/api/v1/bancas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isCreated())
            // Should be normalized: "FGV (Fundação Getúlio Vargas)"
            .andExpect(jsonPath("$.nome").value("FGV (Fundação Getúlio Vargas)"));
    }
}
