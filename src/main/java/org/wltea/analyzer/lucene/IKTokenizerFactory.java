package org.wltea.analyzer.lucene;

import java.util.Map;

/**
 * Created by YBX on 2015/4/3.
 */
public class IKTokenizerFactory extends TokenizerFatory {
    private final boolean useSmart;

    public IKTokenizerFactory(Map<String, String> args) {
        super(args);
        this.useSmart = getBoolean(args, "useSmart", false);
    }

    @Override
    public Tokonizer create(AttributeFactory factory) {
        return new IKTokenizer(factory, this.useSmart);
    }
}
