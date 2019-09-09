package us.martink.lunar.client;

import lombok.extern.log4j.Log4j2;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Log4j2
@Component
public class GithubClient {

    private static final String SEARCH_REPOSITORIES = "/search/repositories?q={query}&sort={sort}&order={order}&page=1&per_page=10";
    private static final String REPOSITORY_CONTRIBUTORS = "/repos/{name}/{repo}/contributors?anon=true";
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

    public Long getReposContributors(String owner, String repoName) {
        ResponseEntity<List<GitRepoContributorsResponse>> response = fetchContributors(REPOSITORY_CONTRIBUTORS, githubConnector, owner, repoName);
        int contributorsPerPage = response.getBody().size();
        String linkHeader = response.getHeaders().getFirst(HttpHeaders.LINK);
        if (linkHeader == null) {
            return (long) contributorsPerPage;
        }

        Optional<String> lastPageLink = Arrays.stream(linkHeader.split(",")).filter(l -> l.contains("rel=\"last\"")).findFirst();
        if (lastPageLink.isPresent()) {
            String url = lastPageLink.get().replace("<", "").replace(">", "").split(";")[0].replaceAll("\\s+", "");

            ResponseEntity<List<GitRepoContributorsResponse>> lastPageResponse = fetchContributors(url, new RestTemplate());

            int lastPage = resolvePageCountFromUrlParams(url);
            contributorsPerPage = (lastPage - 1) * contributorsPerPage + lastPageResponse.getBody().size();
        }
        return (long) contributorsPerPage;
    }

    private ResponseEntity<List<GitRepoContributorsResponse>> fetchContributors(String link, RestTemplate restTemplate, String... params) {
        return restTemplate.exchange(link, HttpMethod.GET, authorizationHeader(), new ParameterizedTypeReference<List<GitRepoContributorsResponse>>() {}, (Object[]) params);
    }

    private int resolvePageCountFromUrlParams(String url) {
        int lastPage = 1;
        try {
            Optional<String> pageNumber = URLEncodedUtils.parse(new URI(url), StandardCharsets.UTF_8).stream().filter(u -> "page".equals(u.getName())).map(NameValuePair::getValue).findFirst();
            if (pageNumber.isPresent()) {
                lastPage = Integer.parseInt(pageNumber.get());
            }
        } catch (URISyntaxException e) {
            log.error(e);
        }
        return lastPage;
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
