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
				int cn = 0;
				for (int i = 0; i < bufferLength; ++i) {
					char c = buffer[i];
					//过滤无效字符
					if (c >= '\u4e00' && c <= '\u9fa5' && c != '、') cn++;
					if (c == ' ' || c == '$' || c == '\n' || c == '\r' || c == '\t') {
						continue;
					}
					//空格代替{ }
					if (c == '{') {
						continue;
					}
					if (c == '}') {
						if (buffer[i + 1] == '{') {
							buffer[upto++] = ' ';
							i++;
						}
						continue;
					}
					buffer[upto++] = c;
				}
				if (upto <= 0 || upto > 20 || cn > 8) {
					//词长度0,删除//词元太长，分词异常，删除
					return false;
				} else {
					this.termAtt.setLength(upto);
				}
			}
		}
		return true;
	}
}
