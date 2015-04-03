 package org.wltea.analyzer.core;
 
 class QuickSortSet
 {
   private Cell head;
   private Cell tail;
   private int size;
 
   QuickSortSet()
   {
     this.size = 0;
   }
 
   boolean addLexeme(Lexeme lexeme)
   {
     Cell newCell = new Cell(lexeme);
     if (this.size == 0) {
       this.head = newCell;
       this.tail = newCell;
       this.size += 1;
       return true;
     }
 
     if (this.tail.compareTo(newCell) == 0) {
       return false;
     }
     if (this.tail.compareTo(newCell) < 0) {
       this.tail.next = newCell;
       newCell.prev = this.tail;
       this.tail = newCell;
       this.size += 1;
       return true;
     }
     if (this.head.compareTo(newCell) > 0) {
       this.head.prev = newCell;
       newCell.next = this.head;
       this.head = newCell;
       this.size += 1;
       return true;
     }
 
     Cell index = this.tail;
     while ((index != null) && (index.compareTo(newCell) > 0)) {
       index = index.prev;
     }
     if (index.compareTo(newCell) == 0) {
       return false;
     }
     if (index.compareTo(newCell) < 0) {
       newCell.prev = index;
       newCell.next = index.next;
       index.next.prev = newCell;
       index.next = newCell;
       this.size += 1;
       return true;
     }
 
     return false;
   }
 
   Lexeme peekFirst()
   {
     if (this.head != null) {
       return this.head.lexeme;
     }
     return null;
   }
 
   Lexeme pollFirst()
   {
     if (this.size == 1) {
       Lexeme first = this.head.lexeme;
       this.head = null;
       this.tail = null;
       this.size -= 1;
       return first;
     }if (this.size > 1) {
       Lexeme first = this.head.lexeme;
       this.head = this.head.next;
       this.size -= 1;
       return first;
     }
     return null;
   }
 
   Lexeme peekLast()
   {
     if (this.tail != null) {
       return this.tail.lexeme;
     }
     return null;
   }
 
   Lexeme pollLast()
   {
     if (this.size == 1) {
       Lexeme last = this.head.lexeme;
       this.head = null;
       this.tail = null;
       this.size -= 1;
       return last;
     }
     if (this.size > 1) {
       Lexeme last = this.tail.lexeme;
       this.tail = this.tail.prev;
       this.size -= 1;
       return last;
     }
 
     return null;
   }
 
   int size()
   {
     return this.size;
   }
 
   boolean isEmpty()
   {
     return this.size == 0;
   }
 
   Cell getHead()
   {
     return this.head;
   }
 
   class Cell
     implements Comparable<Cell>
   {
     private Cell prev;
     private Cell next;
     private Lexeme lexeme;
 
     Cell(Lexeme lexeme)
     {
       if (lexeme == null) {
         throw new IllegalArgumentException("lexeme must not be null");
       }
       this.lexeme = lexeme;
     }
 
     public int compareTo(Cell o) {
       return this.lexeme.compareTo(o.lexeme);
     }
 
     public Cell getPrev() {
       return this.prev;
     }
 
     public Cell getNext() {
       return this.next;
     }
 
     public Lexeme getLexeme() {
       return this.lexeme;
     }
   }
 }