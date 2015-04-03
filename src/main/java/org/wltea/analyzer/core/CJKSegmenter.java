 package org.wltea.analyzer.core;
 
 import java.util.LinkedList;
 import java.util.List;
 import org.wltea.analyzer.dic.Dictionary;
 import org.wltea.analyzer.dic.Hit;
 
 class CJKSegmenter
   implements ISegmenter
 {
   static final String SEGMENTER_NAME = "CJK_SEGMENTER";
   private List<Hit> tmpHits;
 
   CJKSegmenter()
   {
     this.tmpHits = new LinkedList();
   }
 
   public void analyze(AnalyzeContext context)
   {
     if (context.getCurrentCharType() != 0)
     {
       if (!this.tmpHits.isEmpty())
       {
         Hit[] tmpArray = (Hit[])this.tmpHits.toArray(new Hit[this.tmpHits.size()]);
         for (Hit hit : tmpArray) {
           hit = Dictionary.getSingleton().matchWithHit(context.getSegmentBuff(), context.getCursor(), hit);
           if (hit.isMatch())
           {
             Lexeme newLexeme = new Lexeme(context.getBufferOffset(), hit.getBegin(), context.getCursor() - hit.getBegin() + 1, 4);
             context.addLexeme(newLexeme);
 
             if (!hit.isPrefix())
               this.tmpHits.remove(hit);
           }
           else {
             if (!hit.isUnmatch())
               continue;
             this.tmpHits.remove(hit);
           }
 
         }
 
       }
 
       Hit singleCharHit = Dictionary.getSingleton().matchInMainDict(context.getSegmentBuff(), context.getCursor(), 1);
       if (singleCharHit.isMatch())
       {
         Lexeme newLexeme = new Lexeme(context.getBufferOffset(), context.getCursor(), 1, 4);
         context.addLexeme(newLexeme);
 
         if (singleCharHit.isPrefix())
         {
           this.tmpHits.add(singleCharHit);
         }
       } else if (singleCharHit.isPrefix())
       {
         this.tmpHits.add(singleCharHit);
       }
 
     }
     else
     {
       this.tmpHits.clear();
     }
 
     if (context.isBufferConsumed())
     {
       this.tmpHits.clear();
     }
 
     if (this.tmpHits.size() == 0) {
       context.unlockBuffer("CJK_SEGMENTER");
     }
     else
       context.lockBuffer("CJK_SEGMENTER");
   }
 
   public void reset()
   {
     this.tmpHits.clear();
   }
 }