package us.martink.lunar.api;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import us.martink.lunar.api.model.GitRepoResponse;
import us.martink.lunar.context.RequestContextHolder;
import us.martink.lunar.service.GitService;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping(value = "/git", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class GitController {

    private GitService gitService;

    @GetMapping
    public List<GitRepoResponse> getRepos() {
        return gitService.getGitRepositories();
    }

    @PutMapping("/star/{owner}/{repo}")
    public ResponseEntity starRepository(@PathVariable String owner,
                                         @PathVariable String repo) {
        String authorization = RequestContextHolder.getContext().getAuthorization();
        if (authorization == null || authorization.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        gitService.starRepository(owner, repo);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/star/{owner}/{repo}")
    public ResponseEntity updateRepoStar(@PathVariable String owner,
                                         @PathVariable String repo) {
        String authorization = RequestContextHolder.getContext().getAuthorization();
        if (authorization == null || authorization.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        gitService.unStarRepository(owner, repo);
        return ResponseEntity.ok().build();
    }
}
