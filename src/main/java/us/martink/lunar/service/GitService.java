package us.martink.lunar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.martink.lunar.api.model.GitRepoResponse;
import us.martink.lunar.client.GithubClient;
import us.martink.lunar.client.model.GitRepo;
import us.martink.lunar.client.model.GitRepoStarredResponse;
import us.martink.lunar.context.RequestContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GitService {

    private GithubClient githubClient;

    @Autowired
    public GitService(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    public void starRepository(String owner, String repoName) {
        githubClient.starRepositoryForUser(owner, repoName);
    }

    public void unStarRepository(String owner, String repoName) {
        githubClient.unStarRepositoryForUser(owner, repoName);
    }

    public List<GitRepoResponse> getGitRepositories() {
        List<GitRepo> items = githubClient.getRepos().getItems();
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> reposStarredByUser = getUserFavoritedRepoIds();

        return items
                .parallelStream()
                .map(g -> buildGitRepoResponse(g, reposStarredByUser))
                .collect(Collectors.toList());
    }

    private Set<Long> getUserFavoritedRepoIds() {
        Set<Long> reposStarredByUser = Collections.emptySet();
        String authorization = RequestContextHolder.getContext().getAuthorization();
        if (authorization != null && !authorization.isEmpty()) {
            reposStarredByUser = githubClient.getReposStarredByUser().stream().map(GitRepoStarredResponse::getId).collect(Collectors.toSet());
        }
        return reposStarredByUser;
    }

    private GitRepoResponse buildGitRepoResponse(GitRepo gitRepo, Set<Long> reposStarredByUser) {
        GitRepoResponse gitRepoResponse = new GitRepoResponse();
        gitRepoResponse.setName(gitRepo.getName());
        gitRepoResponse.setDescription(gitRepo.getDescription());
        gitRepoResponse.setLicenseName(gitRepo.getLicenseName());
        gitRepoResponse.setLinkToRepo(gitRepo.getLinkToRepo());
        gitRepoResponse.setStarsCount(gitRepo.getStarsCount());
//TODO        gitRepoResponse.setContributors(githubClient.getReposContributors(gitRepo.getLogin(), gitRepo.getName()));
        if (!reposStarredByUser.isEmpty()) {
            gitRepoResponse.setFavorite(reposStarredByUser.contains(gitRepo.getId()));
        }
        return gitRepoResponse;
    }
}
