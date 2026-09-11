package ru.skfu.softwarestore.presentation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.skfu.softwarestore.foundation.repository.UserRepository;
import ru.skfu.softwarestore.presentation.controller.PageController;
import ru.skfu.softwarestore.security.JwtService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PageController.class)
@AutoConfigureMockMvc(addFilters = false)
class PageControllerMvcTest {
    @Autowired private MockMvc mvc;
    @MockBean private JwtService jwtService;
    @MockBean private UserRepository userRepository;

    @Test void homeIsAvailable() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("index"));
    }

    @Test void catalogIsAvailable() throws Exception {
        mvc.perform(get("/catalog")).andExpect(status().isOk()).andExpect(view().name("catalog"));
    }

    @Test void authIsAvailable() throws Exception {
        mvc.perform(get("/auth")).andExpect(status().isOk()).andExpect(view().name("auth"));
    }
}
