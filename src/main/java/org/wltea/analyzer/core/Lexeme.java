 package org.wltea.analyzer.core;
 
 public class Lexeme
   implements Comparable<Lexeme>
 {
   public static final int TYPE_UNKNOWN = 0;
   public static final int TYPE_ENGLISH = 1;
   public static final int TYPE_ARABIC = 2;
   public static final int TYPE_LETTER = 3;
   public static final int TYPE_CNWORD = 4;
   public static final int TYPE_CNCHAR = 64;
   public static final int TYPE_OTHER_CJK = 8;
   public static final int TYPE_CNUM = 16;
   public static final int TYPE_COUNT = 32;
   public static final int TYPE_CQUAN = 48;
   private int offset;
   private int begin;
   private int length;
   private String lexemeText;
   private int lexemeType;
 
   public Lexeme(int offset, int begin, int length, int lexemeType)
   {
     this.offset = offset;
     this.begin = begin;
     if (length < 0) {
       throw new IllegalArgumentException("length < 0");
     }
     this.length = length;
     this.lexemeType = lexemeType;
   }
 
   public boolean equals(Object o)
   {
     if (o == null) {
       return false;
     }
 
     if (this == o) {
       return true;
     }
 
     if ((o instanceof Lexeme)) {
       Lexeme other = (Lexeme)o;
 
       return (this.offset == other.getOffset()) && 
         (this.begin == other.getBegin()) && 
         (this.length == other.getLength());
     }
 
     return false;
   }
 
   public int hashCode()
   {
     int absBegin = getBeginPosition();
     int absEnd = getEndPosition();
     return absBegin * 37 + absEnd * 31 + absBegin * absEnd % getLength() * 11;
   }
 
   public int compareTo(Lexeme other)
   {
     if (this.begin < other.getBegin())
       return -1;
     if (this.begin == other.getBegin())
     {
       if (this.length > other.getLength())
         return -1;
       if (this.length == other.getLength()) {
         return 0;
       }
       return 1;
     }
 
     return 1;
   }
 
   public int getOffset()
   {
     return this.offset;
   }
 
   public void setOffset(int offset) {
     this.offset = offset;
   }
 
   public int getBegin() {
     return this.begin;
   }
 
   public int getBeginPosition()
   {
     return this.offset + this.begin;
   }
 
   public void setBegin(int begin) {
     this.begin = begin;
   }
 
   public int getEndPosition()
   {
     return this.offset + this.begin + this.length;
   }
 
   public int getLength()
   {
     return this.length;
   }
 
   public void setLength(int length) {
     if (this.length < 0) {
       throw new IllegalArgumentException("length < 0");
     }
     this.length = length;
   }
 
   public String getLexemeText()
   {
     if (this.lexemeText == null) {
       return "";
     }
     return this.lexemeText;
   }
 
   public void setLexemeText(String lexemeText) {
     if (lexemeText == null) {
       this.lexemeText = "";
       this.length = 0;
     } else {
       this.lexemeText = lexemeText;
       this.length = lexemeText.length();
     }
   }
 
   public int getLexemeType()
   {
     return this.lexemeType;
   }
 
   public String getLexemeTypeString()
   {
     switch (this.lexemeType)
     {
     case 1:
       return "ENGLISH";
     case 2:
       return "ARABIC";
     case 3:
       return "LETTER";
     case 4:
       return "CN_WORD";
     case 64:
       return "CN_CHAR";
     case 8:
       return "OTHER_CJK";
     case 32:
       return "COUNT";
     case 16:
       return "TYPE_CNUM";
     case 48:
       return "TYPE_CQUAN";
     }
 
     return "UNKONW";
   }
 
   public void setLexemeType(int lexemeType)
   {
     this.lexemeType = lexemeType;
   }
 
   public boolean append(Lexeme l, int lexemeType)
   {
     if ((l != null) && (getEndPosition() == l.getBeginPosition())) {
       this.length += l.getLength();
       this.lexemeType = lexemeType;
       return true;
     }
     return false;
   }
 
   public String toString()
   {
     StringBuffer strbuf = new StringBuffer();
     strbuf.append(getBeginPosition()).append("-").append(getEndPosition());
     strbuf.append(" : ").append(this.lexemeText).append(" : \t");
     strbuf.append(getLexemeTypeString());
     return strbuf.toString();
   }
 }