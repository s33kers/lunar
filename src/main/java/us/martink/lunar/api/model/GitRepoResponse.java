package us.martink.lunar.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class GitRepoResponse {
    private String name;
    private String description;
    private String licenseName;
    private String linkToRepo;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean favorite;
    private Long contributors;
    private Long starsCount;
}
