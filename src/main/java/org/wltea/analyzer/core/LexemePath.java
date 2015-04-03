 package org.wltea.analyzer.core;
 
 class LexemePath extends QuickSortSet
   implements Comparable<LexemePath>
 {
   private int pathBegin;
   private int pathEnd;
   private int payloadLength;
 
   LexemePath()
   {
     this.pathBegin = -1;
     this.pathEnd = -1;
     this.payloadLength = 0;
   }
 
   boolean addCrossLexeme(Lexeme lexeme)
   {
     if (isEmpty()) {
       addLexeme(lexeme);
       this.pathBegin = lexeme.getBegin();
       this.pathEnd = (lexeme.getBegin() + lexeme.getLength());
       this.payloadLength += lexeme.getLength();
       return true;
     }
     if (checkCross(lexeme)) {
       addLexeme(lexeme);
       if (lexeme.getBegin() + lexeme.getLength() > this.pathEnd) {
         this.pathEnd = (lexeme.getBegin() + lexeme.getLength());
       }
       this.payloadLength = (this.pathEnd - this.pathBegin);
       return true;
     }
 
     return false;
   }
 
   boolean addNotCrossLexeme(Lexeme lexeme)
   {
     if (isEmpty()) {
       addLexeme(lexeme);
       this.pathBegin = lexeme.getBegin();
       this.pathEnd = (lexeme.getBegin() + lexeme.getLength());
       this.payloadLength += lexeme.getLength();
       return true;
     }
     if (checkCross(lexeme)) {
       return false;
     }
 
     addLexeme(lexeme);
     this.payloadLength += lexeme.getLength();
     Lexeme head = peekFirst();
     this.pathBegin = head.getBegin();
     Lexeme tail = peekLast();
     this.pathEnd = (tail.getBegin() + tail.getLength());
     return true;
   }
 
   Lexeme removeTail()
   {
     Lexeme tail = pollLast();
     if (isEmpty()) {
       this.pathBegin = -1;
       this.pathEnd = -1;
       this.payloadLength = 0;
     } else {
       this.payloadLength -= tail.getLength();
       Lexeme newTail = peekLast();
       this.pathEnd = (newTail.getBegin() + newTail.getLength());
     }
     return tail;
   }
 
   boolean checkCross(Lexeme lexeme)
   {
     return ((lexeme.getBegin() >= this.pathBegin) && (lexeme.getBegin() < this.pathEnd)) || (
       (this.pathBegin >= lexeme.getBegin()) && (this.pathBegin < lexeme.getBegin() + lexeme.getLength()));
   }
 
   int getPathBegin() {
     return this.pathBegin;
   }
 
   int getPathEnd() {
     return this.pathEnd;
   }
 
   int getPayloadLength()
   {
     return this.payloadLength;
   }
 
   int getPathLength()
   {
     return this.pathEnd - this.pathBegin;
   }
 
   int getXWeight()
   {
     int product = 1;
     QuickSortSet.Cell c = getHead();
     while ((c != null) && (c.getLexeme() != null)) {
       product *= c.getLexeme().getLength();
       c = c.getNext();
     }
     return product;
   }
 
   int getPWeight()
   {
     int pWeight = 0;
     int p = 0;
     QuickSortSet.Cell c = getHead();
     while ((c != null) && (c.getLexeme() != null)) {
       p++;
       pWeight += p * c.getLexeme().getLength();
       c = c.getNext();
     }
     return pWeight;
   }
 
   LexemePath copy() {
     LexemePath theCopy = new LexemePath();
     theCopy.pathBegin = this.pathBegin;
     theCopy.pathEnd = this.pathEnd;
     theCopy.payloadLength = this.payloadLength;
     QuickSortSet.Cell c = getHead();
     while ((c != null) && (c.getLexeme() != null)) {
       theCopy.addLexeme(c.getLexeme());
       c = c.getNext();
     }
     return theCopy;
   }
 
   public int compareTo(LexemePath o)
   {
     if (this.payloadLength > o.payloadLength)
       return -1;
     if (this.payloadLength < o.payloadLength) {
       return 1;
     }
 
     if (size() < o.size())
       return -1;
     if (size() > o.size()) {
       return 1;
     }
 
     if (getPathLength() > o.getPathLength())
       return -1;
     if (getPathLength() < o.getPathLength()) {
       return 1;
     }
 
     if (this.pathEnd > o.pathEnd)
       return -1;
     if (this.pathEnd < o.pathEnd) {
       return 1;
     }
 
     if (getXWeight() > o.getXWeight())
       return -1;
     if (getXWeight() < o.getXWeight()) {
       return 1;
     }
 
     if (getPWeight() > o.getPWeight())
       return -1;
     if (getPWeight() < o.getPWeight()) {
       return 1;
     }
 
     return 0;
   }
 
   public String toString() {
     StringBuffer sb = new StringBuffer();
     sb.append("pathBegin  : ").append(this.pathBegin).append("\r\n");
     sb.append("pathEnd  : ").append(this.pathEnd).append("\r\n");
     sb.append("payloadLength  : ").append(this.payloadLength).append("\r\n");
     QuickSortSet.Cell head = getHead();
     while (head != null) {
       sb.append("lexeme : ").append(head.getLexeme()).append("\r\n");
       head = head.getNext();
     }
     return sb.toString();
   }
 }