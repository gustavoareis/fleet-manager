package com.medagenda.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

/**
 * Who may call what. Runs the real filter chains and the dev seed data (PostgreSQL), so it
 * proves the three chains (API / admin / default) and the role rules end to end.
 * Only reads and rejected writes, so it never changes the database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SecurityMatrixIntegrationTest {

    private static final String PASSWORD = "Senha@123";

    @Autowired MockMvc mvc;
    @Autowired JsonMapper json;

    private Map<String, String> tokens;

    @BeforeAll
    void logIn() throws Exception {
        tokens = Map.of(
                "patient", login("paciente@medagenda.local"),
                "doctor", login("medico@medagenda.local"),
                "reception", login("recepcao@medagenda.local"),
                "admin", login("admin@medagenda.local"));
    }

    private String login(String email) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "password", PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("accessToken").asString();
    }

    private int statusOf(String path, String role) throws Exception {
        var request = get(path);
        if (role != null) {
            request.header("Authorization", "Bearer " + tokens.get(role));
        }
        return mvc.perform(request).andReturn().getResponse().getStatus();
    }

    // ---- API: authentication ----

    @Test
    void apiRejectsRequestsWithoutToken() throws Exception {
        mvc.perform(get("/api/patients"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Content-Type", "application/problem+json"));
    }

    @Test
    void apiRejectsAGarbageToken() throws Exception {
        mvc.perform(get("/api/patients").header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"paciente@medagenda.local\",\"password\":\"errada\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void referenceDataIsPublicForTheSignUpForm() throws Exception {
        assertThat(statusOf("/api/specialties", null)).isEqualTo(200);
        assertThat(statusOf("/api/health-insurances", null)).isEqualTo(200);
    }

    // ---- API: authorization by role ----

    @Test
    void patientListIsStaffOnly() throws Exception {
        assertThat(statusOf("/api/patients", "patient")).isEqualTo(403);
        assertThat(statusOf("/api/patients", "doctor")).isEqualTo(403);
        assertThat(statusOf("/api/patients", "reception")).isEqualTo(200);
        assertThat(statusOf("/api/patients", "admin")).isEqualTo(200);
    }

    @Test
    void myAppointmentsIsForPatientsOnly() throws Exception {
        assertThat(statusOf("/api/appointments/me", "patient")).isEqualTo(200);
        assertThat(statusOf("/api/appointments/me", "doctor")).isEqualTo(403);
        assertThat(statusOf("/api/appointments/me", "reception")).isEqualTo(403);
    }

    @Test
    void creatingDoctorsIsAdminOnly() throws Exception {
        for (String role : new String[] {"patient", "doctor", "reception"}) {
            // DELETE has no body: with a POST, body validation (400) would run before the role check
            mvc.perform(delete("/api/doctors/1").header("Authorization", "Bearer " + tokens.get(role)))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void sortingByASensitiveFieldIsRejectedInsteadOfLeakingIt() throws Exception {
        mvc.perform(get("/api/patients?sort=user.passwordHash")
                        .header("Authorization", "Bearer " + tokens.get("reception")))
                .andExpect(status().isBadRequest());
    }

    // ---- Everything else is closed ----

    @Test
    void unknownPathsOutsideTheChainsAreDenied() throws Exception {
        assertThat(statusOf("/qualquer-coisa", "admin")).isIn(401, 403, 404);
        assertThat(statusOf("/actuator/env", null)).isIn(401, 403, 404);
    }

    @Test
    void jspFilesCannotBeRequestedDirectly() throws Exception {
        assertThat(statusOf("/WEB-INF/jsp/admin/login.jsp", null)).isNotEqualTo(200);
    }

    // ---- /admin: session + form login + CSRF ----

    @Test
    void adminRedirectsAnonymousVisitorsToTheLoginForm() throws Exception {
        mvc.perform(get("/admin/consultas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login"));
    }

    @Test
    void adminIgnoresTheApiBearerToken() throws Exception {
        // The JWT belongs to the API chain; the admin chain only knows its own session
        mvc.perform(get("/admin").header("Authorization", "Bearer " + tokens.get("admin")))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void adminLoginFormIsPublic() throws Exception {
        mvc.perform(get("/admin/login")).andExpect(status().isOk());
    }

    @Test
    void adminLoginRequiresACsrfToken() throws Exception {
        mvc.perform(post("/admin/login").param("username", "recepcao@medagenda.local").param("password", PASSWORD))
                .andExpect(status().is(403));
    }

    @Test
    void receptionLogsInAndOpensTheDashboard() throws Exception {
        MockHttpSession session = adminSession("recepcao@medagenda.local", PASSWORD);

        mvc.perform(get("/admin").session(session)).andExpect(status().isOk());
        mvc.perform(get("/admin/consultas").session(session)).andExpect(status().isOk());
        mvc.perform(get("/admin/relatorio").session(session)).andExpect(status().isOk());
    }

    @Test
    void wrongPasswordGoesBackToTheLoginWithAnError() throws Exception {
        mvc.perform(post("/admin/login").with(csrf())
                        .param("username", "recepcao@medagenda.local").param("password", "errada"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login?erro"));
    }

    @Test
    void patientAndDoctorAccountsCannotUseTheAdminModule() throws Exception {
        for (String email : new String[] {"paciente@medagenda.local", "medico@medagenda.local"}) {
            MockHttpSession session = adminSession(email, PASSWORD);

            mvc.perform(get("/admin/consultas").session(session))
                    .andExpect(status().isForbidden())
                    .andExpect(forwardedUrl("/admin/acesso-negado"));
        }
    }

    @Test
    void adminPostWithoutCsrfTokenIsRefused() throws Exception {
        MockHttpSession session = adminSession("recepcao@medagenda.local", PASSWORD);

        mvc.perform(post("/admin/especialidades").session(session).param("name", "Sem token"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/admin/acesso-negado?motivo=csrf"));
    }

    @Test
    void logoutIsPostOnly() throws Exception {
        MockHttpSession session = adminSession("recepcao@medagenda.local", PASSWORD);

        // GET /admin/logout is not a logout endpoint, so it must not end the session
        mvc.perform(get("/admin/logout").session(session));
        mvc.perform(get("/admin").session(session)).andExpect(status().isOk());

        mvc.perform(post("/admin/logout").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login?saiu"));
    }

    private MockHttpSession adminSession(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/admin/login").with(csrf())
                        .param("username", email).param("password", password))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }
}
