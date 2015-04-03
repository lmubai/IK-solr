package org.wltea.analyzer.lucene;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;

import java.util.Map;

/**
 * Created by YBX on 2015/4/3.
 */
public class IKTokenizerFactory extends TokenizerFactory {
    private final boolean useSmart;

    public IKTokenizerFactory(Map<String, String> args) {
        super(args);
        this.useSmart = getBoolean(args, "useSmart", false);
    }

    @Override
    public Tokenizer create(AttributeFactory factory) {
        return new IKTokenizer(factory, this.useSmart);
    }
}
