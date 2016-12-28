package com.here.tcsdemo;

import java.util.List;

/**
 * ResponseModel for GeoComplete api response
 */
public class ResponseModel {

    private List<Suggestion> suggestions;

    /**
     * @return the suggestions
     */
    public List<Suggestion> getSuggestions() {
        return suggestions;
    }

    /**
     * @param suggestions the suggestions to set
     */
    public void setSuggestions(List<Suggestion> suggestions) {
        this.suggestions = suggestions;
    }

    public static class Suggestion {

        private String label;

        /**
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * @param label the label to set
         */
        public void setLabel(String label) {
            this.label = label;
        }
    }
}
