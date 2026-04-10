package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.request.BancaCreateRequest;
import com.studora.entity.Banca;
import com.studora.repository.BancaRepository;
import com.studora.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    private BancaRepository bancaRepository;

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
            .andExpect(status().isCreated());

        // Verify the name was normalized in the database
        var savedBancas = bancaRepository.findByNomeContainingIgnoreCase("FGV", Pageable.unpaged());
        assert savedBancas.getContent().size() > 0;
        Banca saved = savedBancas.getContent().get(0);
        assert saved.getNome().equals("FGV (Fundação Getúlio Vargas)") : 
            "Expected normalized name but got: '" + saved.getNome() + "'";
    }
}
