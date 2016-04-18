package com.mindsoon.lexplore;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Entry {
    public String word;
    public Content dictionary;
    public Content synonym;
    public Content slang;
    private String italicsTag;
    private String lineTag;
    private String webLinkTag;
    private String smallTag;
    private String urbanDictionaryTag;
    private String synonymTag;
    private String noResultsTag;

    public Entry(String word) {
        this.word = word.replace("%", "%%").replace("/", "");
        this.dictionary = null;
        this.synonym = null;
        this.slang = null;
    }

    public boolean hasAllContent() {
        return this.dictionary != null && this.synonym != null && this.slang != null;
    }

    public boolean hasAnyContent() {
        return this.dictionary != null || this.synonym != null || this.slang != null;
    }

    public void hydrateContentFromJson(JSONObject json, Verb verb, Context context) {
        initializeHtmlStrings(context);
        try {
            JSONObject lexplore = json.getJSONObject("lexplore");
            if (verb == Verb.Dictionary && lexplore.has("dictionary")) {
                this.dictionary = getDictionaryFromJson(lexplore.getJSONObject("dictionary"));
            } else if (verb == Verb.Slang && lexplore.has("slang")) {
                this.slang = getSlangFromJson(lexplore.getJSONObject("slang"));
            } else if (verb == Verb.Synonym && lexplore.has("synonym")) {
                this.synonym = getSynonymFromJson(lexplore.getJSONObject("synonym"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Content getDictionaryFromJson(JSONObject json) throws JSONException {

        if (json.isNull("name") || json.isNull("link") || json.isNull("entries")) {
            return noResults("definition", "");
        }

        Content content = new Content(json.getString("name"), "");
        JSONArray entries = json.getJSONArray("entries");

        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i);
            if (entry.has("firstUsage") && entry.has("partOfSpeech") && entry.has("definitions")) {
                content.content += String.format(smallTag,
                        entry.getString("firstUsage") + " (" + entry.getString("partOfSpeech") + ")" + lineTag);
                JSONArray defs = entry.getJSONArray("definitions");
                for (int j = 0; j < defs.length(); j++) {
                    JSONObject def = defs.getJSONObject(j);
                    if (def.has("definition")) {
                        content.content += " " + def.getString("definition");
                    }
                }
                content.content += lineTag;
            }
        }
        String webLink = json.getString("link");
        content.content = content.content.substring(0, content.content.length() - lineTag.length()) + String.format(webLinkTag, webLink);
        return content.content.equals("") ? noResults(content.name, webLink) : content;
    }

    private Content getSlangFromJson(JSONObject json) throws JSONException {

        if (json.isNull("name") || json.isNull("link") || json.isNull("definition") || json.isNull("example")) {
            return noResults("slang", "");
        }

        String webLink = json.getString("link");
        if (json.getString("definition").equals("")) {
            return noResults(json.getString("name"), String.format(urbanDictionaryTag, word));
        } else {
            return new Content(json.getString("name"), json.getString("definition") +
                                                       lineTag +
                                                       String.format(italicsTag, json.getString("example")) +
                                                       String.format(webLinkTag, webLink));
        }
    }

    private Content getSynonymFromJson(JSONObject json) throws JSONException {

        if (json.isNull("name") || json.isNull("link") || json.isNull("entries")) {
            return noResults("synonym", "");
        }

        Content content = new Content(json.getString("name"), "");
        String webLink = json.getString("link");
        JSONArray entries = json.getJSONArray("entries");

        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i);
            if (entry.has("partOfSpeech") && entry.has("synonyms")) {
                content.content += String.format(smallTag, entry.getString("partOfSpeech")) + " " +
                        entry.getString("synonyms") +
                        lineTag;
            }
        }
        content.content = content.content.substring(0, content.content.length() - lineTag.length()) + String.format(webLinkTag, webLink);
        return (content.content.equals("")) ? noResults(json.getString("name"), String.format(synonymTag, word)) : content;
    }

    private Content noResults(String verb, String link) {
        return new Content("", String.format(noResultsTag + word, verb) + link);
    }

    private void initializeHtmlStrings(Context context) {
        italicsTag = context.getResources().getString(R.string.italics_tag);
        lineTag = context.getResources().getString(R.string.line_tag);
        smallTag = context.getResources().getString(R.string.small_tag);
        webLinkTag = context.getResources().getString(R.string.web_link_tag);
        urbanDictionaryTag = context.getResources().getString(R.string.urban_dictionary_tag);
        synonymTag = context.getResources().getString(R.string.synonym_tag);
        noResultsTag = context.getResources().getString(R.string.no_results_tag);
    }

    public class Content
    {
        public String name;
        public String content;
        public Content(String name, String content) {
            this.name = name;
            this.content = content;
        }
    }

    public enum Verb {
        Dictionary, Synonym, Slang;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }
}