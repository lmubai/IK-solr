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
			if (bufferLength <= 0) {
				//词长度0,删除
				return false;
			}
			if (type.startsWith("TYPE_LATEX")) {
				int upto = 0;
				for (int i = 0; i < bufferLength; ++i) {
					char c = buffer[i];
					//过滤无效字符
					if (c == ' ' || c == '$' || Character.isSpaceChar(c)) {
						continue;
					}
					//空格代替{ }
					if (c == '{' && i != 0) {
						buffer[upto++] = ' ';
						continue;
					}
					if (c == '}' && i < bufferLength - 1) {
						buffer[upto++] = ' ';
						continue;
					}
					buffer[upto++] = c;
				}
				if (upto <= 0) {
					//词长度0,删除
					return false;
				} else {
					this.termAtt.setLength(upto);
				}
			}
		}
		return true;
	}
}
