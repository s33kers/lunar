package us.martink.lunar.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class GitRepo {

    private Long id;
    private String name;
    private String login;
    private String description;
    private String licenseName;
    @JsonProperty("html_url")
    private String linkToRepo;
    @JsonProperty("stargazers_count")
    private Long starsCount;

    @JsonProperty("license")
    private void unpackNameFromLicense(Map<String, String> license) {
        if (license == null) {
            return;
        }
        licenseName = license.get("name");
    }

    @JsonProperty("owner")
    private void unpackLoginFromOwner(Map<String, String> owner) {
        if (owner == null) {
            return;
        }
        login = owner.get("login");
    }
}