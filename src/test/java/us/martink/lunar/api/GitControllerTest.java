package us.martink.lunar.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.Base64Utils;
import us.martink.lunar.api.model.GitRepoResponse;
import us.martink.lunar.service.GitService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(GitController.class)
public class GitControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private GitService gitService;

    @Test
    public void getRepos_noRepos_emptyResponse() throws Exception {
        when(gitService.getGitRepositories()).thenReturn(Collections.emptyList());

        mvc.perform(get("/git")
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(convertObjectToJsonString(Collections.emptyList())));

    }

    @Test
    public void getRepos_someRepos_nonAuthorized() throws Exception {
        List<GitRepoResponse> response = createGetGitRepositoriesResponse(false);
        when(gitService.getGitRepositories()).thenReturn(response);

        mvc.perform(get("/git")
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(convertObjectToJsonString(response)));

    }

    @Test
    public void getRepos_someRepos_authorized() throws Exception {
        List<GitRepoResponse> response = createGetGitRepositoriesResponse(true);
        when(gitService.getGitRepositories()).thenReturn(response);

        mvc.perform(get("/git")
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(convertObjectToJsonString(response)));

    }

    @Test
    public void getRepos_starRepository_unauthorized() throws Exception {
        mvc.perform(put("/git/star/{owner}/{repo}", "owner", "repo")
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isUnauthorized());

        verify(gitService, never()).starRepository(anyString(), anyString());
    }

    @Test
    public void getRepos_starRepository_authorized() throws Exception {
        mvc.perform(put("/git/star/{owner}/{repo}", "owner", "repo")
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64Utils.encodeToString("user:secret".getBytes())))
                .andExpect(status().isOk());

        verify(gitService, only()).starRepository(eq("owner"), eq("repo"));
    }


    @Test
    public void getRepos_unstarRepository_unauthorized() throws Exception {
        mvc.perform(delete("/git/star/{owner}/{repo}", "owner", "repo")
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isUnauthorized());

        verify(gitService, never()).unStarRepository(anyString(), anyString());
    }

    @Test
    public void getRepos_unstarRepository_authorized() throws Exception {
        mvc.perform(delete("/git/star/{owner}/{repo}", "owner", "repo")
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64Utils.encodeToString("user:secret".getBytes())))
                .andExpect(status().isOk());

        verify(gitService, only()).unStarRepository(eq("owner"), eq("repo"));
    }

    private List<GitRepoResponse> createGetGitRepositoriesResponse(boolean authorized) {
        List<GitRepoResponse> gitRepoResponse = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            gitRepoResponse.add(getGitRepoResponse(i, authorized));
        }

        return gitRepoResponse;
    }

    private GitRepoResponse getGitRepoResponse(int index, boolean authorized) {
        GitRepoResponse gitRepoResponse = new GitRepoResponse();
        if (authorized) {
            gitRepoResponse.setFavorite(Boolean.TRUE);
        }
        gitRepoResponse.setContributors((long) index);
        gitRepoResponse.setStarsCount((long) index);
        gitRepoResponse.setLinkToRepo("LINK_TO_REPO_" + index);
        gitRepoResponse.setLicenseName("LICENSE_NAME_" + index);
        gitRepoResponse.setDescription("DESCRIPTION_" + index);
        gitRepoResponse.setName("NAME_" + index);
        return gitRepoResponse;
    }

    public static String convertObjectToJsonString(Object object) throws JsonProcessingException {
        ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return writer.writeValueAsString(object);
    }
}
