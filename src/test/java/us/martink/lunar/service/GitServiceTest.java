package us.martink.lunar.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import us.martink.lunar.api.model.GitRepoResponse;
import us.martink.lunar.client.GithubClient;
import us.martink.lunar.client.model.GitRepo;
import us.martink.lunar.client.model.GitRepoStarredResponse;
import us.martink.lunar.client.model.GitReposResponse;
import us.martink.lunar.context.RequestContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GitServiceTest {

    @InjectMocks
    private GitService gitService;

    @Mock
    private GithubClient githubClient;

    @Before
    public void setUp() {
        RequestContextHolder.getContext().setAuthorization(null);
    }

    @Test
    public void starRepo_verifyClientCall() {
        gitService.starRepository("owner", "repo");

        verify(githubClient, only()).starRepositoryForUser(eq("owner"), eq("repo"));
    }

    @Test
    public void unstarRepo_verifyClientCall() {
        gitService.unStarRepository("owner", "repo");

        verify(githubClient, only()).unStarRepositoryForUser(eq("owner"), eq("repo"));
    }

    @Test
    public void getGitRepositories_emptyRepo() {
        when(githubClient.getRepos()).thenReturn(new GitReposResponse());

        List<GitRepoResponse> gitRepositories = gitService.getGitRepositories();

        assertNotNull(gitRepositories);
        assertEquals(gitRepositories, Collections.emptyList());
    }

    @Test
    public void getGitRepositories_someRepos_authorized() {
        RequestContextHolder.getContext().setAuthorization("YRA");

        when(githubClient.getRepos()).thenReturn(createReposResponse(3));
        when(githubClient.getReposStarredByUser()).thenReturn(createReposStarredByUserResponse());
        when(githubClient.getReposContributors(anyString(), anyString())).thenReturn(10L);

        List<GitRepoResponse> gitRepositories = gitService.getGitRepositories();

        assertNotNull(gitRepositories);
        assertEquals(gitRepositories.size(), 3);
        assertTrue(gitRepositories.stream().anyMatch(GitRepoResponse::getFavorite));
        assertFalse(gitRepositories.stream().allMatch(GitRepoResponse::getFavorite));
    }

    @Test
    public void getGitRepositories_someRepos_unAuthorized() {
        when(githubClient.getRepos()).thenReturn(createReposResponse(3));
        when(githubClient.getReposContributors(anyString(), anyString())).thenReturn(10L);

        List<GitRepoResponse> gitRepositories = gitService.getGitRepositories();

        assertNotNull(gitRepositories);
        assertEquals(gitRepositories.size(), 3);
        assertTrue(gitRepositories.stream().allMatch(g -> g.getFavorite() == null));
        verify(githubClient, never()).getReposStarredByUser();
    }

    private Set<GitRepoStarredResponse> createReposStarredByUserResponse() {
        return Set.of(new GitRepoStarredResponse(1L));
    }

    private GitReposResponse createReposResponse(int count) {
        GitReposResponse gitReposResponse = new GitReposResponse();
        for (int i = 0; i < count; i++) {
            gitReposResponse.getItems().add(createRepo(i));
        }
        return gitReposResponse;
    }

    private GitRepo createRepo(int index) {
        GitRepo gitRepo = new GitRepo();
        gitRepo.setId((long) index);
        gitRepo.setDescription("DESCRIPTION_" + index);
        gitRepo.setLicenseName("LICENSE_NAME_" + index);
        gitRepo.setLinkToRepo("LINK_TO_REPO_" + index);
        gitRepo.setName("NAME_" + index);
        gitRepo.setName("LOGIN_" + index);
        gitRepo.setStarsCount((long) (index  * 2));
        return gitRepo;
    }
}
