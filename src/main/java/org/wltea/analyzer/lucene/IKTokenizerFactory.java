package org.wltea.analyzer.lucene;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Created by YBX on 2015/4/3.
 * 支持solr 5.0
 */
public class IKTokenizerFactory extends TokenizerFactory implements ResourceLoaderAware {
	private final boolean useSmart;
	private final String wordFiles;
	private final String stopWordFiles;
	private final String englishWordFiles;
	private final String quantifierWordFiles;
	private CharArraySet words;
	private CharArraySet stopWords;
	private CharArraySet englishWords;
	private CharArraySet quantifierWords;

	public IKTokenizerFactory(Map<String, String> args) {
		super(args);
		this.useSmart = getBoolean(args, "useSmart", false);
		this.wordFiles = get(args, "words");
		this.stopWordFiles = get(args, "stopWords");
		this.englishWordFiles = get(args, "englishWords");
		this.quantifierWordFiles = get(args, "quantifierWords");
	}

	@Override
	public Tokenizer create(AttributeFactory factory) {
		return new IKTokenizer(factory, this.useSmart, this.words, this.stopWords, this.englishWords, this.quantifierWords);
	}

	public void inform(ResourceLoader resourceLoader) throws IOException {
		if (this.wordFiles != null) {
			this.words = getWordSet(resourceLoader, wordFiles, true);
		}
		if (this.stopWordFiles != null) {
			this.stopWords = getWordSet(resourceLoader, stopWordFiles, true);
		}
		if (this.englishWordFiles != null) {
			this.englishWords = getWordSet(resourceLoader, englishWordFiles, true);
		}
		if (this.quantifierWordFiles != null) {
			this.quantifierWords = getWordSet(resourceLoader, quantifierWordFiles, true);
		}
	}

}
