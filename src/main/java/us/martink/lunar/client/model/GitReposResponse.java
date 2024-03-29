package us.martink.lunar.client.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GitReposResponse {
    private List<GitRepo> items = new ArrayList<>();
}
