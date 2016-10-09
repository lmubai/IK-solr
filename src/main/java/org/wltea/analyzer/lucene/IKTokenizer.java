/**
 * IK 中文分词  版本 5.0.1
 * IK Analyzer release 5.0.1
 * <p/>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * 源代码由林良益(linliangyi2005@gmail.com)提供
 * 版权声明 2012，乌龙茶工作室
 * provided by Linliangyi and copyright 2012 by Oolong studio
 */
package org.wltea.analyzer.lucene;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.search.BoostAttribute;
import org.apache.lucene.util.AttributeFactory;
import org.apache.lucene.util.BytesRef;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;
import org.wltea.analyzer.util.Utils;

import java.io.IOException;

/**
 * IK分词器 Lucene Tokenizer适配器类
 * 兼容Lucene 4.0版本
 */
public final class IKTokenizer extends Tokenizer {

    //词元长度计算词元权重
    private static float[] boosts = {0.0f, 1.0f, 10.0f, 20.0f, 40.0f, 80.0f};

    //IK分词器实现
    private IKSegmenter _IKImplement;

    //词元文本属性
    private final CharTermAttribute termAtt;
    //词元位移属性
    private final OffsetAttribute offsetAtt;
    //词元分类属性（该属性分类参考org.wltea.analyzer.core.Lexeme中的分类常量）
    private final TypeAttribute typeAtt;
    // 词元权重
    private final BoostAttribute boostAttribute;
    private final PayloadAttribute payloadAttribute;
    //记录最后一个词元的结束位置
    private int endPosition;

    /**
     * Lucene 5.0 Tokenizer适配器类构造函数
     *
     * @param factory  AttributeFactory
     * @param useSmart 智能切分
     */
    public IKTokenizer(AttributeFactory factory, boolean useSmart) {
        super(factory);
        offsetAtt = addAttribute(OffsetAttribute.class);
        termAtt = addAttribute(CharTermAttribute.class);
        typeAtt = addAttribute(TypeAttribute.class);
        boostAttribute = addAttribute(BoostAttribute.class);
        payloadAttribute = addAttribute(PayloadAttribute.class);
        _IKImplement = new IKSegmenter(input, useSmart);
    }

    public IKTokenizer(boolean useSmart) {
        offsetAtt = addAttribute(OffsetAttribute.class);
        termAtt = addAttribute(CharTermAttribute.class);
        typeAtt = addAttribute(TypeAttribute.class);
        boostAttribute = addAttribute(BoostAttribute.class);
        payloadAttribute = addAttribute(PayloadAttribute.class);
        _IKImplement = new IKSegmenter(input, useSmart);
    }

    /**
     * Lucene 5.0 支持solrcloud词典文件统一管理
     *
     * @param factory   AttributeFactory
     * @param useSmart  智能切分
     * @param words     分词词典
     * @param stopWords 停止词词典
     */
    public IKTokenizer(AttributeFactory factory, boolean useSmart, CharArraySet words, CharArraySet stopWords) {
        super(factory);
        offsetAtt = addAttribute(OffsetAttribute.class);
        termAtt = addAttribute(CharTermAttribute.class);
        typeAtt = addAttribute(TypeAttribute.class);
        boostAttribute = addAttribute(BoostAttribute.class);
        payloadAttribute = addAttribute(PayloadAttribute.class);
        _IKImplement = new IKSegmenter(input, useSmart, words, stopWords);
    }

    /* (non-Javadoc)
     * @see org.apache.lucene.analysis.TokenStream#incrementToken()
     */
    @Override
    public boolean incrementToken() throws IOException {
        //清除所有的词元属性
        clearAttributes();
        Lexeme nextLexeme = _IKImplement.next();
        if (nextLexeme != null) {
            //将Lexeme转成Attributes
            //设置词元文本
            termAtt.append(nextLexeme.getLexemeText());
            //设置词元长度
            termAtt.setLength(nextLexeme.getLength());
            //设置词元位移
            offsetAtt.setOffset(nextLexeme.getBeginPosition(), nextLexeme.getEndPosition());
            //记录分词的最后位置
            endPosition = nextLexeme.getEndPosition();
            //记录词元分类
            typeAtt.setType(nextLexeme.getLexemeTypeString());
            //设置词元权重
            if (nextLexeme.getLexemeType() == Lexeme.TYPE_CNWORD
                    || nextLexeme.getLexemeType() == Lexeme.TYPE_CNCHAR
                    || nextLexeme.getLexemeType() == Lexeme.TYPE_CNUM
                    || nextLexeme.getLexemeType() == Lexeme.TYPE_CQUAN
                    || nextLexeme.getLexemeType() == Lexeme.TYPE_ARABIC) {
                boostAttribute.setBoost(boosts[Math.min(boosts.length-1, nextLexeme.getLength())]);
                payloadAttribute.setPayload(new BytesRef(Utils.int2Bytes(nextLexeme.getLength())));
            } else if (nextLexeme.getLexemeType() == Lexeme.TYPE_ENGLISH_2) {
                boostAttribute.setBoost(boosts[2]);
                payloadAttribute.setPayload(new BytesRef(Utils.int2Bytes(2)));
            } else if (nextLexeme.getLexemeType() == Lexeme.TYPE_ENGLISH_3) {
                boostAttribute.setBoost(boosts[3]);
                payloadAttribute.setPayload(new BytesRef(Utils.int2Bytes(3)));
            } else {
                boostAttribute.setBoost(boosts[1]);
                payloadAttribute.setPayload(new BytesRef(Utils.int2Bytes(1)));
            }
            //返会true告知还有下个词元
            return true;
        }
        //返会false告知词元输出完毕
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.lucene.analysis.Tokenizer#reset(java.io.Reader)
     */
    @Override
    public void reset() throws IOException {
        super.reset();
        _IKImplement.reset(input);
    }

    @Override
    public final void end() {
        // set final offset
        int finalOffset = correctOffset(this.endPosition);
        offsetAtt.setOffset(finalOffset, finalOffset);
    }
}
