package us.martink.lunar.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import us.martink.lunar.client.model.GitRepoContributorsResponse;
import us.martink.lunar.client.model.GitRepoStarredResponse;
import us.martink.lunar.client.model.GitReposResponse;
import us.martink.lunar.context.RequestContextHolder;

import java.util.List;
import java.util.Set;

@Component
public class GithubClient {

    private static final String SEARCH_REPOSITORIES = "/search/repositories?q={query}&sort={sort}&order={order}&page=1&per_page=10";
    private static final String REPOSITORY_CONTRIBUTORS = "/repos/{name}/{repo}/stats/contributors?anon=true";
    private static final String STARRED_BY_USER = "/user/starred";
    private static final String STAR_REPOSITORY = "/user/starred/{name}/{repo}";
    private RestTemplate githubConnector;

    @Autowired
    public GithubClient(RestTemplateBuilder builder,
                        @Value("${api.git-uri}") String githubUri) {
        githubConnector = builder.rootUri(githubUri).build();
    }

    public GitReposResponse getRepos() {
        return githubConnector.getForEntity(SEARCH_REPOSITORIES, GitReposResponse.class, "Java+frameworks", "stars", "desc").getBody();
    }

    public Set<GitRepoStarredResponse> getReposStarredByUser() {
        return githubConnector.exchange(STARRED_BY_USER, HttpMethod.GET, authorizationHeader(), new ParameterizedTypeReference<Set<GitRepoStarredResponse>>() {}).getBody();
    }

    //TODO fetch Link header from response and calculate contributors count by it
    public Long getReposContributors(String owner, String repoName) {
        ResponseEntity<List<GitRepoContributorsResponse>> exchange = githubConnector.exchange(REPOSITORY_CONTRIBUTORS, HttpMethod.GET, authorizationHeader(), new ParameterizedTypeReference<List<GitRepoContributorsResponse>>() {}, owner, repoName);
        int contributorsPerPage = exchange.getBody().size();
        exchange.getHeaders().getFirst(HttpHeaders.LINK);
        return (long) contributorsPerPage;
    }

    public void starRepositoryForUser(String owner, String repoName) {
        githubConnector.exchange(STAR_REPOSITORY, HttpMethod.PUT, authorizationHeader(), Void.class, owner, repoName);
    }

    public void unStarRepositoryForUser(String owner, String repoName) {
        githubConnector.exchange(STAR_REPOSITORY, HttpMethod.DELETE, authorizationHeader(), Void.class, owner, repoName);
    }

    private HttpEntity authorizationHeader() {
        HttpHeaders headers = new HttpHeaders();
        String authorization = RequestContextHolder.getContext().getAuthorization();
        if (authorization != null) {
            headers.add(HttpHeaders.AUTHORIZATION, authorization);
        }
        return new HttpEntity<>(null, headers);
    }

}
