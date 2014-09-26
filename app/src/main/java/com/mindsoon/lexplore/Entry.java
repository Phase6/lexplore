package com.mindsoon.lexplore;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Entry {
    final String word;
    private String dictionaryName;
    private String slangName;
    private String thesaurusName;
    private String dictionaryContent;
    private String slangContent;
    private String thesaurusContent;

    public Entry(String word) {
        this.word = word;
    }

    public String getWord() { return this.word; }

    public void insertContent(JSONObject definitionObject, Context context) {
        String lineTag = context.getResources().getString(R.string.line_tag),
                smallTag = context.getResources().getString(R.string.small_tag),
                smallEndTag = context.getResources().getString(R.string.small_end_tag);

        try {
            //dictionary content
            this.dictionaryName = definitionObject.getJSONObject("dictionary").getString("name");
            this.dictionaryContent = getDictionaryContent(context, this.dictionaryName, smallTag,
                smallEndTag, lineTag, definitionObject.getJSONObject("dictionary").getString("link"),
                definitionObject.getJSONObject("dictionary").getJSONArray("entries"));

            //slang content
            this.slangName = definitionObject.getJSONObject("slang").getString("name");
            this.slangContent = getSlangContent(context, this.slangName, lineTag,
                definitionObject.getJSONObject("slang").getString("link"),
                definitionObject.getJSONObject("slang"));

            //thesaurus content
            this.thesaurusName = definitionObject.getJSONObject("thesaurus").getString("name");
            this.thesaurusContent = getThesaurusContent(context, this.thesaurusName, smallTag,
                smallEndTag, lineTag, definitionObject.getJSONObject("thesaurus").getString("link"),
                definitionObject.getJSONObject("thesaurus").getJSONArray("entries"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getDictionaryContent(Context c, String name, String smallTag, String smallEndTag,
                       String lineTag, String link, JSONArray entries) throws JSONException {
        JSONArray defs;
        String content = "";
        for (int i = 0; i < entries.length(); i++) {
            content += smallTag + entries.getJSONObject(i).getString("firstUsage") + " (" +
                    entries.getJSONObject(i).getString("partOfSpeech") + ")" +
                    lineTag + smallEndTag;
            defs = entries.getJSONObject(i).getJSONArray("definitions");
            for (int j = 0; j < defs.length(); j++) {
                content += " " + defs.getJSONObject(j).getString("def") + lineTag;
            }
        }
        if (!content.equals(""))
            return content.substring(0, content.length() - lineTag.length()) + linkToWeb(c, link);
        else return noResults(c, name, link);
    }

    private String getSlangContent(Context c, String name, String lineTag, String link,
                                   JSONObject slangObject) throws JSONException {
        if (slangObject.getString("definition").length() > 0) {
            return slangObject.getString("definition") + lineTag +
                    "<i>" + slangObject.getString("example") + "</i>" + linkToWeb(c, link);
        } else return noResults(c, name, link);
    }

    private String getThesaurusContent(Context c, String name, String smallTag, String smallEndTag,
                            String lineTag, String link, JSONArray entries) throws JSONException {
        String content = "";
        for (int i = 0; i < entries.length(); i++) {
            content += smallTag + entries.getJSONObject(i).getString("partOfSpeech") +
                    smallEndTag + entries.getJSONObject(i).getString("synonyms") + lineTag;
        }
        if (!content.equals(""))
            return content.substring(0, content.length() - lineTag.length()) + linkToWeb(c, link);
        else return noResults(c, name, link);
    }

    private String noResults(Context c, String name, String link) {
        String addDefinition = "",
               urbanDictionaryOpenTag = c.getResources().getString(R.string.urban_dictionary_open_tag),
               urbanDictionaryCloseTag = c.getResources().getString(R.string.urban_dictionary_close_tag),
               wikisaurusOpenTag = c.getResources().getString(R.string.wikisaurus_open_tag),
               wikisaurusCloseTag = c.getResources().getString(R.string.wikisaurus_close_tag),
               noResultsOpenTag = c.getResources().getString(R.string.no_results_open_tag),
               noResultsCloseTag = c.getResources().getString(R.string.no_results_close_tag);

        if (name.equals("urban dictionary")) {
            addDefinition = urbanDictionaryOpenTag + link + urbanDictionaryCloseTag;
        } else if (name.equals("wikisaurus")) {
            addDefinition = wikisaurusOpenTag + this.word + wikisaurusCloseTag;
        }
        return noResultsOpenTag + name + addDefinition + noResultsCloseTag;
    }

    private String linkToWeb(Context c, String link) {
        return c.getResources().getString(R.string.web_link_open_tag) + link +
               c.getResources().getString(R.string.web_link_close_tag);
    }

    // getter methods
    public String getDictionaryName(){ return dictionaryName; }
    public String getSlangName(){ return slangName; }
    public String getThesaurusName(){ return thesaurusName; }
    public String getDictionaryContent(){ return dictionaryContent; }
    public String getSlangContent(){ return slangContent; }
    public String getThesaurusContent(){ return thesaurusContent; }
}