package de.hda.simple_example.event;

import de.hda.simple_example.model.SearchResult;

public class Contract {

    public static abstract class LoadingEvent {
        public abstract boolean isLoading();
    }

    public static class LoadingStartedEvent extends LoadingEvent{

        @Override
        public boolean isLoading() {
            return true;
        }
    }

    public static class LoadingFinishedEvent extends LoadingEvent{

        @Override
        public boolean isLoading() {
            return false;
        }
    }

    public static class RepositoriesLoadedEvent {
        private final SearchResult searchResult;
        private final int page;

        public RepositoriesLoadedEvent(SearchResult searchResult, int page){
            this.searchResult = searchResult;
            this.page = page;
        }

        public SearchResult getSearchResult() {
            return searchResult;
        }

        public boolean isFirstPage() {
            return page == 1;
        }
    }

    public static class GithubServiceErrorEvent {
        private String message;

        public GithubServiceErrorEvent(String message){
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof GithubServiceErrorEvent))
                return false;
            return message.equals(((GithubServiceErrorEvent) obj).message);
        }
    }

    public static class SearchRepositoriesEvent {
        private String query;

        public SearchRepositoriesEvent(String query) {
            this.query = query;
        }

        public String getQuery() {
            return query;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SearchRepositoriesEvent)){
                return false;
            }
            return ((SearchRepositoriesEvent) obj).query.equals(query);
        }
    }

}
