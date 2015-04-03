 package org.wltea.analyzer.core;
 
 import java.util.Stack;
 import java.util.TreeSet;
 
 class IKArbitrator
 {
   void process(AnalyzeContext context, boolean useSmart)
   {
     QuickSortSet orgLexemes = context.getOrgLexemes();
     Lexeme orgLexeme = orgLexemes.pollFirst();
 
     LexemePath crossPath = new LexemePath();
     while (orgLexeme != null) {
       if (!crossPath.addCrossLexeme(orgLexeme))
       {
         if ((crossPath.size() == 1) || (!useSmart))
         {
           context.addLexemePath(crossPath);
         }
         else {
           QuickSortSet.Cell headCell = crossPath.getHead();
           LexemePath judgeResult = judge(headCell, crossPath.getPathLength());
 
           context.addLexemePath(judgeResult);
         }
 
         crossPath = new LexemePath();
         crossPath.addCrossLexeme(orgLexeme);
       }
       orgLexeme = orgLexemes.pollFirst();
     }
 
     if ((crossPath.size() == 1) || (!useSmart))
     {
       context.addLexemePath(crossPath);
     }
     else {
       QuickSortSet.Cell headCell = crossPath.getHead();
       LexemePath judgeResult = judge(headCell, crossPath.getPathLength());
 
       context.addLexemePath(judgeResult);
     }
   }
 
   private LexemePath judge(QuickSortSet.Cell lexemeCell, int fullTextLength)
   {
     TreeSet pathOptions = new TreeSet();
 
     LexemePath option = new LexemePath();
 
     Stack lexemeStack = forwardPath(lexemeCell, option);
 
     pathOptions.add(option.copy());
 
     QuickSortSet.Cell c = null;
     while (!lexemeStack.isEmpty()) {
       c = (QuickSortSet.Cell)lexemeStack.pop();
 
       backPath(c.getLexeme(), option);
 
       forwardPath(c, option);
       pathOptions.add(option.copy());
     }
 
     return (LexemePath)pathOptions.first();
   }
 
   private Stack<QuickSortSet.Cell> forwardPath(QuickSortSet.Cell lexemeCell, LexemePath option)
   {
     Stack conflictStack = new Stack();
     QuickSortSet.Cell c = lexemeCell;
 
     while ((c != null) && (c.getLexeme() != null)) {
       if (!option.addNotCrossLexeme(c.getLexeme()))
       {
         conflictStack.push(c);
       }
       c = c.getNext();
     }
     return conflictStack;
   }
 
   private void backPath(Lexeme l, LexemePath option)
   {
     while (option.checkCross(l))
       option.removeTail();
   }
 }