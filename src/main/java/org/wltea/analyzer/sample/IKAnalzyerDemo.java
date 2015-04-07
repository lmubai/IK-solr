 package org.wltea.analyzer.sample;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.StringReader;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
 import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
 import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
 import org.wltea.analyzer.lucene.IKAnalyzer;
 
 public class IKAnalzyerDemo
 {
   public static void main(String[] args)
   {
     Analyzer analyzer = new IKAnalyzer(true);
 
     TokenStream ts = null;
     try {
       ts = analyzer.tokenStream("myfield", new StringReader("1,2"));
 
       OffsetAttribute offset = (OffsetAttribute)ts.addAttribute(OffsetAttribute.class);
 
       CharTermAttribute term = (CharTermAttribute)ts.addAttribute(CharTermAttribute.class);
 
       TypeAttribute type = (TypeAttribute)ts.addAttribute(TypeAttribute.class);
 
       ts.reset();
 
       while (ts.incrementToken()) {
         System.out.println(offset.startOffset() + " - " + offset.endOffset() + " : " + term.toString() + " | " + type.type());
       }
 
       ts.end();
     }
     catch (IOException e) {
       e.printStackTrace();
 
       if (ts != null)
         try {
           ts.close();
         } catch (IOException ioe) {
           ioe.printStackTrace();
         }
     }
     finally
     {
       if (ts != null)
         try {
           ts.close();
         } catch (IOException e) {
           e.printStackTrace();
         }
     }
   }
 }