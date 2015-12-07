package org.wltea.analyzer.lucene;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

import java.util.Map;

/**
 * Created by 易斌鑫 on 15-12-7.
 */
public class LaTeXFilterFactory extends TokenFilterFactory {
    protected LaTeXFilterFactory(Map<String, String> args) {
        super(args);
    }

    @Override
    public TokenStream create(TokenStream input) {
        return new LaTeXFilter(input);
    }
}
