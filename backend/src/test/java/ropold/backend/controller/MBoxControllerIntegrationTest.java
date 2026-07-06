package ropold.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MBoxControllerIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    void getMapBoxy_expectMapBoxy() throws Exception {
        // WHEN
        mockMvc.perform(get("/api/mbox/72c81498-f6b2-4a8a-911c-cd217a65e0da"))
                // THEN
                .andExpect(status().isOk())
                .andExpect(content().string("456"));
    }
}