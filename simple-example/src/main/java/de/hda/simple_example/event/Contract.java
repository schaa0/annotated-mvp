package de.hda.simple_example.event;

import de.hda.simple_example.model.SearchResult;

public class Contract {

    public static abstract class LoadingEvent { }

    public static class LoadingStartedEvent extends LoadingEvent{ }

    public static class LoadingFinishedEvent extends LoadingEvent{ }

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
}
