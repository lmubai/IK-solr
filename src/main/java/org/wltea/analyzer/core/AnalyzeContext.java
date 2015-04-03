 package org.wltea.analyzer.core;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Set;
 import org.wltea.analyzer.cfg.Configuration;
 import org.wltea.analyzer.dic.Dictionary;
 
 class AnalyzeContext
 {
   private static final int BUFF_SIZE = 4096;
   private static final int BUFF_EXHAUST_CRITICAL = 100;
   private char[] segmentBuff;
   private int[] charTypes;
   private int buffOffset;
   private int cursor;
   private int available;
   private Set<String> buffLocker;
   private QuickSortSet orgLexemes;
   private Map<Integer, LexemePath> pathMap;
   private LinkedList<Lexeme> results;
   private Configuration cfg;
 
   public AnalyzeContext(Configuration cfg)
   {
     this.cfg = cfg;
     this.segmentBuff = new char[4096];
     this.charTypes = new int[4096];
     this.buffLocker = new HashSet();
     this.orgLexemes = new QuickSortSet();
     this.pathMap = new HashMap();
     this.results = new LinkedList();
   }
 
   int getCursor() {
     return this.cursor;
   }
 
   char[] getSegmentBuff()
   {
     return this.segmentBuff;
   }
 
   char getCurrentChar() {
     return this.segmentBuff[this.cursor];
   }
 
   int getCurrentCharType() {
     return this.charTypes[this.cursor];
   }
 
   int getBufferOffset() {
     return this.buffOffset;
   }
 
   int fillBuffer(Reader reader)
     throws IOException
   {
     int readCount = 0;
     if (this.buffOffset == 0)
     {
       readCount = reader.read(this.segmentBuff);
     } else {
       int offset = this.available - this.cursor;
       if (offset > 0)
       {
         System.arraycopy(this.segmentBuff, this.cursor, this.segmentBuff, 0, offset);
         readCount = offset;
       }
 
       readCount += reader.read(this.segmentBuff, offset, 4096 - offset);
     }
 
     this.available = readCount;
 
     this.cursor = 0;
     return readCount;
   }
 
   void initCursor()
   {
     this.cursor = 0;
     this.segmentBuff[this.cursor] = CharacterUtil.regularize(this.segmentBuff[this.cursor]);
     this.charTypes[this.cursor] = CharacterUtil.identifyCharType(this.segmentBuff[this.cursor]);
   }
 
   boolean moveCursor()
   {
     if (this.cursor < this.available - 1) {
       this.cursor += 1;
       this.segmentBuff[this.cursor] = CharacterUtil.regularize(this.segmentBuff[this.cursor]);
       this.charTypes[this.cursor] = CharacterUtil.identifyCharType(this.segmentBuff[this.cursor]);
       return true;
     }
     return false;
   }
 
   void lockBuffer(String segmenterName)
   {
     this.buffLocker.add(segmenterName);
   }
 
   void unlockBuffer(String segmenterName)
   {
     this.buffLocker.remove(segmenterName);
   }
 
   boolean isBufferLocked()
   {
     return this.buffLocker.size() > 0;
   }
 
   boolean isBufferConsumed()
   {
     return this.cursor == this.available - 1;
   }
 
   boolean needRefillBuffer()
   {
     return (this.available == 4096) && 
       (this.cursor < this.available - 1) && 
       (this.cursor > this.available - 100) && 
       (!isBufferLocked());
   }
 
   void markBufferOffset()
   {
     this.buffOffset += this.cursor;
   }
 
   void addLexeme(Lexeme lexeme)
   {
     this.orgLexemes.addLexeme(lexeme);
   }
 
   void addLexemePath(LexemePath path)
   {
     if (path != null)
       this.pathMap.put(Integer.valueOf(path.getPathBegin()), path);
   }
 
   QuickSortSet getOrgLexemes()
   {
     return this.orgLexemes;
   }
 
   void outputToResult()
   {
     int index = 0;
     while (index <= this.cursor)
     {
       if (this.charTypes[index] == 0) {
         index++;
       }
       else
       {
         LexemePath path = (LexemePath)this.pathMap.get(Integer.valueOf(index));
         if (path != null)
         {
           Lexeme l = path.pollFirst();
           while (l != null) {
             this.results.add(l);
 
             index = l.getBegin() + l.getLength();
             l = path.pollFirst();
             if (l == null)
               continue;
             for (; index < l.getBegin(); index++) {
               outputSingleCJK(index);
             }
           }
         }
         else
         {
           outputSingleCJK(index);
           index++;
         }
       }
     }
     this.pathMap.clear();
   }
 
   private void outputSingleCJK(int index)
   {
     if (4 == this.charTypes[index]) {
       Lexeme singleCharLexeme = new Lexeme(this.buffOffset, index, 1, 64);
       this.results.add(singleCharLexeme);
     } else if (8 == this.charTypes[index]) {
       Lexeme singleCharLexeme = new Lexeme(this.buffOffset, index, 1, 8);
       this.results.add(singleCharLexeme);
     }
   }
 
   Lexeme getNextLexeme()
   {
     Lexeme result = (Lexeme)this.results.pollFirst();
     while (result != null)
     {
       compound(result);
       if (Dictionary.getSingleton().isStopWord(this.segmentBuff, result.getBegin(), result.getLength()))
       {
         result = (Lexeme)this.results.pollFirst();
       }
       else {
         result.setLexemeText(String.valueOf(this.segmentBuff, result.getBegin(), result.getLength()));
         break;
       }
     }
     return result;
   }
 
   void reset()
   {
     this.buffLocker.clear();
     this.orgLexemes = new QuickSortSet();
     this.available = 0;
     this.buffOffset = 0;
     this.charTypes = new int[4096];
     this.cursor = 0;
     this.results.clear();
     this.segmentBuff = new char[4096];
     this.pathMap.clear();
   }
 
   private void compound(Lexeme result)
   {
     if (!this.cfg.useSmart()) {
       return;
     }
 
     if (!this.results.isEmpty())
     {
       if (2 == result.getLexemeType()) {
         Lexeme nextLexeme = (Lexeme)this.results.peekFirst();
         boolean appendOk = false;
         if (16 == nextLexeme.getLexemeType())
         {
           appendOk = result.append(nextLexeme, 16);
         } else if (32 == nextLexeme.getLexemeType())
         {
           appendOk = result.append(nextLexeme, 48);
         }
         if (appendOk)
         {
           this.results.pollFirst();
         }
 
       }
 
       if ((16 == result.getLexemeType()) && (!this.results.isEmpty())) {
         Lexeme nextLexeme = (Lexeme)this.results.peekFirst();
         boolean appendOk = false;
         if (32 == nextLexeme.getLexemeType())
         {
           appendOk = result.append(nextLexeme, 48);
         }
         if (appendOk)
         {
           this.results.pollFirst();
         }
       }
     }
   }
 }