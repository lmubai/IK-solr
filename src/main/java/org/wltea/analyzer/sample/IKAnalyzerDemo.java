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
package org.wltea.analyzer.sample;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.io.StringReader;

/**
 * 使用IKAnalyzer进行分词的演示
 * 2012-10-22
 */
public class IKAnalyzerDemo {

    public static void main(String[] args) {
        String test_content = "中文: 简体汉字 没有太多的测试句子, 求解下列方程组的解\n" +
                "数量词: 二十一世纪 长四十二米约等于六十二寸 十寸" +
                "English: can't it's not errror words.\n" +
                "Numbers: 1 2 0.1 3.14 -1 -1.0 1.1.1.1\n" +
                "Letter: 100,000 2000-01-01 linliangyi2005@gmail.com\n" +
                "Formula: 1+1=2 1<2 1+(-1-1) 0.12+0.34 原点(0,0)\n"+
                "Latex: 99.9% △ABC f(x) \\sqrt{2} \\cos A 0..%";
        //构建IK分词器，使用smart分词模式
        Analyzer analyzer = new IKAnalyzer(false);

        //获取Lucene的TokenStream对象
        TokenStream ts = null;
        try {
            ts = analyzer.tokenStream("myfield", new StringReader(test_content));
//            ts = analyzer.tokenStream("myfield",new StringReader("   +4 "));
            //获取词元位置属性
            OffsetAttribute offset = ts.addAttribute(OffsetAttribute.class);
            //获取词元文本属性
            CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
            //获取词元文本属性
            TypeAttribute type = ts.addAttribute(TypeAttribute.class);


            //重置TokenStream（重置StringReader）
            ts.reset();
            //迭代获取分词结果
            while (ts.incrementToken()) {
                System.out.println(offset.startOffset() + " - " + offset.endOffset() + " : " + term.toString() + " | " + type.type());
            }
            //关闭TokenStream（关闭StringReader）
            ts.end();   // Perform end-of-stream operations, e.g. set the final offset.

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //释放TokenStream的所有资源
            if (ts != null) {
                try {
                    ts.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
