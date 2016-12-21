package de.hda.simple_example.container;

import com.google.gson.Gson;

import java.io.IOException;

import de.hda.simple_example.model.SearchResult;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Andy on 20.12.2016.
 */

public class SucceedingCallAdapter implements Call<SearchResult> {

    private Gson gson = new Gson();

    @Override
    public Response<SearchResult> execute() throws IOException {
        SearchResult searchResult = gson.fromJson(MESSAGE_BODY, SearchResult.class);
        return Response.success(searchResult);
    }

    @Override
    public void enqueue(Callback<SearchResult> callback) {

    }

    @Override
    public boolean isExecuted() {
        return false;
    }

    @Override
    public void cancel() {

    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public Call<SearchResult> clone() {
        return null;
    }

    @Override
    public Request request() {
        return null;
    }

    public static final Integer ID_FROM_MESSAGE = 6333619;

    private static final String MESSAGE_BODY = "{\n"+
            "  \"total_count\": 2,\n"+
            "  \"incomplete_results\": false,\n"+
            "  \"items\": [\n"+
            "    {\n"+
            "      \"id\": 6333619,\n"+
            "      \"name\": \"google\",\n"+
            "      \"full_name\": \"MarioVilas/google\",\n"+
            "      \"owner\": {\n"+
            "        \"login\": \"MarioVilas\",\n"+
            "        \"id\": 227923,\n"+
            "        \"avatar_url\": \"https://avatars.githubusercontent.com/u/227923?v=3\",\n"+
            "        \"gravatar_id\": \"\",\n"+
            "        \"url\": \"https://api.github.com/users/MarioVilas\",\n"+
            "        \"html_url\": \"https://github.com/MarioVilas\",\n"+
            "        \"followers_url\": \"https://api.github.com/users/MarioVilas/followers\",\n"+
            "        \"following_url\": \"https://api.github.com/users/MarioVilas/following{/other_user}\",\n"+
            "        \"gists_url\": \"https://api.github.com/users/MarioVilas/gists{/gist_id}\",\n"+
            "        \"starred_url\": \"https://api.github.com/users/MarioVilas/starred{/owner}{/repo}\",\n"+
            "        \"subscriptions_url\": \"https://api.github.com/users/MarioVilas/subscriptions\",\n"+
            "        \"organizations_url\": \"https://api.github.com/users/MarioVilas/orgs\",\n"+
            "        \"repos_url\": \"https://api.github.com/users/MarioVilas/repos\",\n"+
            "        \"events_url\": \"https://api.github.com/users/MarioVilas/events{/privacy}\",\n"+
            "        \"received_events_url\": \"https://api.github.com/users/MarioVilas/received_events\",\n"+
            "        \"type\": \"User\",\n"+
            "        \"site_admin\": false\n"+
            "      },\n"+
            "      \"private\": false,\n"+
            "      \"html_url\": \"https://github.com/MarioVilas/google\",\n"+
            "      \"description\": \"Google search from Python.\",\n"+
            "      \"fork\": false,\n"+
            "      \"url\": \"https://api.github.com/repos/MarioVilas/google\",\n"+
            "      \"forks_url\": \"https://api.github.com/repos/MarioVilas/google/forks\",\n"+
            "      \"keys_url\": \"https://api.github.com/repos/MarioVilas/google/keys{/key_id}\",\n"+
            "      \"collaborators_url\": \"https://api.github.com/repos/MarioVilas/google/collaborators{/collaborator}\",\n"+
            "      \"teams_url\": \"https://api.github.com/repos/MarioVilas/google/teams\",\n"+
            "      \"hooks_url\": \"https://api.github.com/repos/MarioVilas/google/hooks\",\n"+
            "      \"issue_events_url\": \"https://api.github.com/repos/MarioVilas/google/issues/events{/number}\",\n"+
            "      \"events_url\": \"https://api.github.com/repos/MarioVilas/google/events\",\n"+
            "      \"assignees_url\": \"https://api.github.com/repos/MarioVilas/google/assignees{/user}\",\n"+
            "      \"branches_url\": \"https://api.github.com/repos/MarioVilas/google/branches{/branch}\",\n"+
            "      \"tags_url\": \"https://api.github.com/repos/MarioVilas/google/tags\",\n"+
            "      \"blobs_url\": \"https://api.github.com/repos/MarioVilas/google/git/blobs{/sha}\",\n"+
            "      \"git_tags_url\": \"https://api.github.com/repos/MarioVilas/google/git/tags{/sha}\",\n"+
            "      \"git_refs_url\": \"https://api.github.com/repos/MarioVilas/google/git/refs{/sha}\",\n"+
            "      \"trees_url\": \"https://api.github.com/repos/MarioVilas/google/git/trees{/sha}\",\n"+
            "      \"statuses_url\": \"https://api.github.com/repos/MarioVilas/google/statuses/{sha}\",\n"+
            "      \"languages_url\": \"https://api.github.com/repos/MarioVilas/google/languages\",\n"+
            "      \"stargazers_url\": \"https://api.github.com/repos/MarioVilas/google/stargazers\",\n"+
            "      \"contributors_url\": \"https://api.github.com/repos/MarioVilas/google/contributors\",\n"+
            "      \"subscribers_url\": \"https://api.github.com/repos/MarioVilas/google/subscribers\",\n"+
            "      \"subscription_url\": \"https://api.github.com/repos/MarioVilas/google/subscription\",\n"+
            "      \"commits_url\": \"https://api.github.com/repos/MarioVilas/google/commits{/sha}\",\n"+
            "      \"git_commits_url\": \"https://api.github.com/repos/MarioVilas/google/git/commits{/sha}\",\n"+
            "      \"comments_url\": \"https://api.github.com/repos/MarioVilas/google/comments{/number}\",\n"+
            "      \"issue_comment_url\": \"https://api.github.com/repos/MarioVilas/google/issues/comments{/number}\",\n"+
            "      \"contents_url\": \"https://api.github.com/repos/MarioVilas/google/contents/{+path}\",\n"+
            "      \"compare_url\": \"https://api.github.com/repos/MarioVilas/google/compare/{base}...{head}\",\n"+
            "      \"merges_url\": \"https://api.github.com/repos/MarioVilas/google/merges\",\n"+
            "      \"archive_url\": \"https://api.github.com/repos/MarioVilas/google/{archive_format}{/ref}\",\n"+
            "      \"downloads_url\": \"https://api.github.com/repos/MarioVilas/google/downloads\",\n"+
            "      \"issues_url\": \"https://api.github.com/repos/MarioVilas/google/issues{/number}\",\n"+
            "      \"pulls_url\": \"https://api.github.com/repos/MarioVilas/google/pulls{/number}\",\n"+
            "      \"milestones_url\": \"https://api.github.com/repos/MarioVilas/google/milestones{/number}\",\n"+
            "      \"notifications_url\": \"https://api.github.com/repos/MarioVilas/google/notifications{?since,all,participating}\",\n"+
            "      \"labels_url\": \"https://api.github.com/repos/MarioVilas/google/labels{/name}\",\n"+
            "      \"releases_url\": \"https://api.github.com/repos/MarioVilas/google/releases{/id}\",\n"+
            "      \"deployments_url\": \"https://api.github.com/repos/MarioVilas/google/deployments\",\n"+
            "      \"created_at\": \"2012-10-22T11:06:01Z\",\n"+
            "      \"updated_at\": \"2016-12-20T08:48:31Z\",\n"+
            "      \"pushed_at\": \"2016-11-08T11:07:17Z\",\n"+
            "      \"git_url\": \"git://github.com/MarioVilas/google.git\",\n"+
            "      \"ssh_url\": \"git@github.com:MarioVilas/google.git\",\n"+
            "      \"clone_url\": \"https://github.com/MarioVilas/google.git\",\n"+
            "      \"svn_url\": \"https://github.com/MarioVilas/google\",\n"+
            "      \"homepage\": null,\n"+
            "      \"size\": 108,\n"+
            "      \"stargazers_count\": 230,\n"+
            "      \"watchers_count\": 230,\n"+
            "      \"language\": \"Python\",\n"+
            "      \"has_issues\": true,\n"+
            "      \"has_downloads\": true,\n"+
            "      \"has_wiki\": true,\n"+
            "      \"has_pages\": false,\n"+
            "      \"forks_count\": 187,\n"+
            "      \"mirror_url\": null,\n"+
            "      \"open_issues_count\": 5,\n"+
            "      \"forks\": 187,\n"+
            "      \"open_issues\": 5,\n"+
            "      \"watchers\": 230,\n"+
            "      \"default_branch\": \"master\",\n"+
            "      \"score\": 102.664986\n"+
            "    }\n"+
            "  ]\n"+
            "}";

}
