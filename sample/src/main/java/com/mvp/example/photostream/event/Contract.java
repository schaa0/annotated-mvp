package com.mvp.example.photostream.event;

import com.mvp.example.photostream.model.SearchResult;

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

}
