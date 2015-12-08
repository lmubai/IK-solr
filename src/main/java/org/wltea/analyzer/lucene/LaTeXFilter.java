package org.wltea.analyzer.lucene;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;

/**
 * Created by 易斌鑫 on 15-12-7.
 */
public class LaTeXFilter extends TokenFilter {
    private final TypeAttribute typeAtt = (TypeAttribute) this.addAttribute(TypeAttribute.class);
    private final CharTermAttribute termAtt = (CharTermAttribute) this.addAttribute(CharTermAttribute.class);

    protected LaTeXFilter(TokenStream input) {
        super(input);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (!this.input.incrementToken()) {
            return false;
        } else {
            char[] buffer = this.termAtt.buffer();
            int bufferLength = this.termAtt.length();
            String type = this.typeAtt.type();
            if (type.startsWith("TYPE_LATEX")) {
                int upto = 0;
                for (int i = 0; i < bufferLength; ++i) {
                    char c = buffer[i];
                    if (c != 32 && c != 123 && c != 125) {
                        buffer[upto++] = c;
                    } else if (i + 1 < bufferLength && c == 123 && buffer[i + 1] == 125) {
                        //用空格代替}{
                        buffer[upto++] = 32;
                    }
                }
                this.termAtt.setLength(upto);
            }
        }
        return true;
    }
}
