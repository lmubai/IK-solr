 package org.wltea.analyzer.core;
 
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import org.wltea.analyzer.dic.Dictionary;
 import org.wltea.analyzer.dic.Hit;
 
 class CN_QuantifierSegmenter
   implements ISegmenter
 {
   static final String SEGMENTER_NAME = "QUAN_SEGMENTER";
   private static String Chn_Num = "一二两三四五六七八九十零壹贰叁肆伍陆柒捌玖拾百千万亿拾佰仟萬億兆卅廿";
   private static Set<Character> ChnNumberChars = new HashSet();
   private int nStart;
   private int nEnd;
   private List<Hit> countHits;
 
   static
   {
     char[] ca = Chn_Num.toCharArray();
     char[] arrayOfChar1 = ca; int j = ca.length; for (int i = 0; i < j; i++) { char nChar = arrayOfChar1[i];
       ChnNumberChars.add(Character.valueOf(nChar));
     }
   }
 
   CN_QuantifierSegmenter()
   {
     this.nStart = -1;
     this.nEnd = -1;
     this.countHits = new LinkedList();
   }
 
   public void analyze(AnalyzeContext context)
   {
     processCNumber(context);
 
     processCount(context);
 
     if ((this.nStart == -1) && (this.nEnd == -1) && (this.countHits.isEmpty()))
     {
       context.unlockBuffer("QUAN_SEGMENTER");
     }
     else context.lockBuffer("QUAN_SEGMENTER");
   }
 
   public void reset()
   {
     this.nStart = -1;
     this.nEnd = -1;
     this.countHits.clear();
   }
 
   private void processCNumber(AnalyzeContext context)
   {
     if ((this.nStart == -1) && (this.nEnd == -1)) {
       if ((4 == context.getCurrentCharType()) && 
         (ChnNumberChars.contains(Character.valueOf(context.getCurrentChar()))))
       {
         this.nStart = context.getCursor();
         this.nEnd = context.getCursor();
       }
     }
     else if ((4 == context.getCurrentCharType()) && 
       (ChnNumberChars.contains(Character.valueOf(context.getCurrentChar()))))
     {
       this.nEnd = context.getCursor();
     }
     else {
       outputNumLexeme(context);
 
       this.nStart = -1;
       this.nEnd = -1;
     }
 
     if ((context.isBufferConsumed()) && 
       (this.nStart != -1) && (this.nEnd != -1))
     {
       outputNumLexeme(context);
 
       this.nStart = -1;
       this.nEnd = -1;
     }
   }
 
   private void processCount(AnalyzeContext context)
   {
     if (!needCountScan(context)) {
       return;
     }
 
     if (4 == context.getCurrentCharType())
     {
       if (!this.countHits.isEmpty())
       {
         Hit[] tmpArray = (Hit[])this.countHits.toArray(new Hit[this.countHits.size()]);
         for (Hit hit : tmpArray) {
           hit = Dictionary.getSingleton().matchWithHit(context.getSegmentBuff(), context.getCursor(), hit);
           if (hit.isMatch())
           {
             Lexeme newLexeme = new Lexeme(context.getBufferOffset(), hit.getBegin(), context.getCursor() - hit.getBegin() + 1, 32);
             context.addLexeme(newLexeme);
 
             if (!hit.isPrefix())
               this.countHits.remove(hit);
           }
           else {
             if (!hit.isUnmatch())
               continue;
             this.countHits.remove(hit);
           }
 
         }
 
       }
 
       Hit singleCharHit = Dictionary.getSingleton().matchInQuantifierDict(context.getSegmentBuff(), context.getCursor(), 1);
       if (singleCharHit.isMatch())
       {
         Lexeme newLexeme = new Lexeme(context.getBufferOffset(), context.getCursor(), 1, 32);
         context.addLexeme(newLexeme);
 
         if (singleCharHit.isPrefix())
         {
           this.countHits.add(singleCharHit);
         }
       } else if (singleCharHit.isPrefix())
       {
         this.countHits.add(singleCharHit);
       }
 
     }
     else
     {
       this.countHits.clear();
     }
 
     if (context.isBufferConsumed())
     {
       this.countHits.clear();
     }
   }
 
   private boolean needCountScan(AnalyzeContext context)
   {
     if (((this.nStart != -1) && (this.nEnd != -1)) || (!this.countHits.isEmpty()))
     {
       return true;
     }
 
     if (!context.getOrgLexemes().isEmpty()) {
       Lexeme l = context.getOrgLexemes().peekLast();
       if (((16 == l.getLexemeType()) || (2 == l.getLexemeType())) && 
         (l.getBegin() + l.getLength() == context.getCursor())) {
         return true;
       }
 
     }
 
     return false;
   }
 
   private void outputNumLexeme(AnalyzeContext context)
   {
     if ((this.nStart > -1) && (this.nEnd > -1))
     {
       Lexeme newLexeme = new Lexeme(context.getBufferOffset(), this.nStart, this.nEnd - this.nStart + 1, 16);
       context.addLexeme(newLexeme);
     }
   }
 }