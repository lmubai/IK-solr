 package org.wltea.analyzer.core;
 
 class CharacterUtil
 {
   public static final int CHAR_USELESS = 0;
   public static final int CHAR_ARABIC = 1;
   public static final int CHAR_ENGLISH = 2;
   public static final int CHAR_CHINESE = 4;
   public static final int CHAR_OTHER_CJK = 8;
 
   static int identifyCharType(char input)
   {
     if ((input >= '0') && (input <= '9')) {
       return 1;
     }
     if (((input >= 'a') && (input <= 'z')) || (
       (input >= 'A') && (input <= 'Z'))) {
       return 2;
     }
 
     Character.UnicodeBlock ub = Character.UnicodeBlock.of(input);
 
     if ((ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) || 
       (ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS) || 
       (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A))
     {
       return 4;
     }
     if ((ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) || 
       (ub == Character.UnicodeBlock.HANGUL_SYLLABLES) || 
       (ub == Character.UnicodeBlock.HANGUL_JAMO) || 
       (ub == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO) || 
       (ub == Character.UnicodeBlock.HIRAGANA) || 
       (ub == Character.UnicodeBlock.KATAKANA) || 
       (ub == Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS)) {
       return 8;
     }
 
     return 0;
   }
 
   static char regularize(char input)
   {
     if (input == '　') {
       input = ' ';
     }
     else if ((input > 65280) && (input < 65375)) {
       input = (char)(input - 65248);
     }
     else if ((input >= 'A') && (input <= 'Z')) {
       input = (char)(input + ' ');
     }
 
     return input;
   }
 }