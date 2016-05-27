package org.wltea.analyzer.core;

import java.util.Stack;

/**
 * Created by two8g on 2015/7/27.
 * LaTeX公式分词器
 */
public class LaTeXSegmenter implements ISegmenter {
    //子分词器标签
    public static final String SEGMENTER_NAME = "LATEX_SEGMENTER";

    public void analyze(AnalyzeContext context) {
        //不以$为分词临界
        percentFormula(context);
        //fullFormula(context);
        fracFormula(context);
        sqrtFormula(context);
        braceFormula(context);
        bigBraceFormula(context);
        onlyNumberLetterPB(context);
    }


    //char[] Buff 当前位置是否以s开头
    private boolean buffEqual(AnalyzeContext context, String s) {
        if (context.getSegmentBuff().length > (context.getCursor() + s.length())) {
            return s.equals(String.copyValueOf(context.getSegmentBuff(), context.getCursor(), s.length()));
        }
        return false;
    }

    //char[] Buff 当前位置是否以chars之一开头
    private boolean oneEqual(AnalyzeContext context, char... chars) {
        for (char c : chars.clone()) {
            if (c == context.getCurrentChar())
                return true;
        }
        return false;
    }

    //char[] Buff 当前位置附近是否与s相等
    private boolean buffAroundEqual(AnalyzeContext context, String s) {
        char[] buff = context.getSegmentBuff();
        for (int i = 0; i < s.length() && i <= context.getCursor(); i++) {
            if (s.equals(String.copyValueOf(buff, context.getCursor() - i, s.length()))) {
                return true;
            }
        }
        return false;
    }

    private int onlyStart;
    private int onlyBrace;

    /**
     * 纯数字字母和上下标
     *
     * @param context
     */
    private void onlyNumberLetterPB(AnalyzeContext context) {
        if (onlyStart == -1) {
            if (CharacterUtil.CHAR_ARABIC == context.getCurrentCharType()
                    || CharacterUtil.CHAR_ENGLISH == context.getCurrentCharType()) {
                if (!(buffAroundEqual(context, "\\sqrt")
                        || buffAroundEqual(context, "\\frac"))) {
                    this.onlyStart = context.getCursor();
                }
            }
        } else {
            if (CharacterUtil.CHAR_ARABIC == context.getCurrentCharType()
                    || CharacterUtil.CHAR_ENGLISH == context.getCurrentCharType()
                    || (context.getCurrentChar() >= 0x3B0 && context.getCurrentChar() <= 0x3C9)
                    || oneEqual(context, '^', '{', '}', '_')) {
                if ('{' == context.getCurrentChar()) {
                    onlyBrace++;
                } else if ('}' == context.getCurrentChar()) {
                    onlyBrace--;
                    if (onlyBrace < 0) {
                        Lexeme newLexeme = new Lexeme(context.getBufferOffset(), this.onlyStart, context.getCursor() - this.onlyStart, Lexeme.TYPE_LATEX_ONLYNLPB);
                        context.addLexeme(newLexeme);
                        this.onlyStart = -1;
                        onlyBrace = 0;
                    }
                }
            } else {
                Lexeme newLexeme = new Lexeme(context.getBufferOffset(), this.onlyStart, context.getCursor() - this.onlyStart, Lexeme.TYPE_LATEX_ONLYNLPB);
                context.addLexeme(newLexeme);
                addOperatorFormula(context, onlyStart, context.getCursor());

                onlyBrace = 0;
                this.onlyStart = -1;
            }
        }

        //判断缓冲区是否已经读完
        if (context.isBufferConsumed()) {
            if (this.onlyStart != -1) {
                //缓冲以读完，输出词元
                Lexeme newLexeme = new Lexeme(context.getBufferOffset(), this.onlyStart, context.getCursor() - this.onlyStart + 1, Lexeme.TYPE_LATEX_ONLYNLPB);
                context.addLexeme(newLexeme);
                this.onlyStart = -1;
            }
        }
    }

    //未匹配的'{'位置
    private Stack<Integer> unBrace;

    /**
     * 花括号内
     *
     * @param context
     */
    private void bigBraceFormula(AnalyzeContext context) {
        if ('{' == context.getCurrentChar()) {
            unBrace.push(context.getCursor());
        } else if ('}' == context.getCurrentChar()) {
            if (!unBrace.isEmpty()) {
                int start = unBrace.pop();
                if (context.getCursor() - start - 1 > 0) {
                    Lexeme newLexeme = new Lexeme(context.getBufferOffset(), start + 1, context.getCursor() - start - 1, Lexeme.TYPE_LATEX_BIGBRACE_1);
                    context.addLexeme(newLexeme);

                    int pbEnd = getPBEnd(context, context.getCursor());
                    if (context.getCursor() + 3 < pbEnd) {
                        newLexeme = new Lexeme(context.getBufferOffset(), start, pbEnd - start, Lexeme.TYPE_LATEX);
                        context.addLexeme(newLexeme);
                    }
                }

            }
        }
        //判断缓冲区是否已经读完
        if (context.isBufferConsumed()) {
            unBrace = new Stack<Integer>();
        }
    }

    private int sqrtStart;
    private int sqrtBrace;

    /**
     * 开方
     *
     * @param context
     */
    private void sqrtFormula(AnalyzeContext context) {
        if (this.sqrtStart == -1) {
            if (buffEqual(context, "\\sqrt")) {
                this.sqrtStart = context.getCursor();
            }
        } else {
            if ('{' == context.getCurrentChar()) {
                sqrtBrace++;
            }
            if ('}' == context.getCurrentChar()) {
                sqrtBrace--;
                if (sqrtBrace == 0) {
                    Lexeme newLexeme = new Lexeme(context.getBufferOffset(), this.sqrtStart, context.getCursor() - this.sqrtStart + 1, Lexeme.TYPE_LATEX_SQRT);
                    context.addLexeme(newLexeme);
                    this.sqrtStart = -1;
                }
            }
        }
        //判断缓冲区是否已经读完
        if (context.isBufferConsumed()) {
            this.sqrtStart = -1;
            sqrtBrace = 0;
        }
    }

    private int fracStart;
    private int fracBrace;
    private int fracElement;

    /**
     * 分式及分子
     *
     * @param context
     */
    private void fracFormula(AnalyzeContext context) {
        if (fracStart == -1) {
            if (buffEqual(context, "\\frac")) {
                this.fracStart = context.getCursor();
            }
        } else {
            if ('{' == context.getCurrentChar()) {
                fracBrace++;
            }
            if ('}' == context.getCurrentChar()) {
                fracBrace--;
                if (fracBrace == 0) {
                    fracElement++;
                    Lexeme newLexeme = new Lexeme(context.getBufferOffset(), this.fracStart, context.getCursor() - this.fracStart + 1, fracElement == 2 ? Lexeme.TYPE_LATEX_FRAC : Lexeme.TYPE_LATEX_FRAC_1);
                    context.addLexeme(newLexeme);
                    if (fracElement == 2) {
                        //如果分式后为运算符，也成词
                        addOperatorFormula(context, fracStart, context.getCursor());
                        this.fracElement = 0;
                        this.fracStart = -1;
                    }
                }
            }
        }
        //判断缓冲区是否已经读完
        if (context.isBufferConsumed()) {
            this.fracElement = 0;
            this.fracStart = -1;
            this.fracBrace = 0;
        }
    }

    //整体公式起始结束位置
    private int fullFormulaStart;

    /**
     * 整体公式$$之间的内容
     *
     * @param context
     */
    private void fullFormula(AnalyzeContext context) {
        if (fullFormulaStart == -1) {//当前分词未处理整体公式
            if ('$' == context.getCurrentChar()) {
                this.fullFormulaStart = context.getCursor();
            }
        } else {//当前分词正在处理整体公式
            if ('$' == context.getCurrentChar()) {
                //遇到$字符,输出词元
                if (context.getCursor() - this.fullFormulaStart - 1 > 0) {
                    Lexeme newLexeme = new Lexeme(context.getBufferOffset(), this.fullFormulaStart + 1, context.getCursor() - this.fullFormulaStart - 1, Lexeme.TYPE_LATEX);
                    context.addLexeme(newLexeme);
                }
                this.fullFormulaStart = -1;
            }
        }
        //判断缓冲区是否已经读完
        if (context.isBufferConsumed()) {
            this.fullFormulaStart = -1;
        }
    }

    private int braceStart;

    /**
     * 小括号(**)与**
     *
     * @param context
     */
    private void braceFormula(AnalyzeContext context) {
        char currentChar = context.getCurrentChar();
        int cursor = context.getCursor();
        if (this.braceStart == -1 && '(' == currentChar) {
            this.braceStart = cursor;
        } else if (')' == currentChar
                && cursor - this.braceStart - 1 > 2) {
            //fixed ()内必须有内容 2015年11月25日
            Lexeme newLexeme = new Lexeme(context.getBufferOffset(), this.braceStart, cursor - this.braceStart + 1, Lexeme.TYPE_LATEX_BRACE);
            context.addLexeme(newLexeme);

            newLexeme = new Lexeme(context.getBufferOffset(), this.braceStart + 1, cursor - this.braceStart - 1, Lexeme.TYPE_LATEX_BRACE_1);
            context.addLexeme(newLexeme);

            //幂低组合
            int pbend = getPBEnd(context, cursor);
            if (cursor + 3 < pbend) {
                newLexeme = new Lexeme(context.getBufferOffset(), this.braceStart, pbend - this.braceStart, Lexeme.TYPE_LATEX);
                context.addLexeme(newLexeme);
            }
            this.braceStart = -1;
        }
        //判断缓冲区是否已经读完
        if (context.isBufferConsumed()) {
            this.braceStart = -1;
        }
    }

    /**
     * 幂低组合,返回结束光标
     *
     * @param context
     * @param start
     * @return
     */
    private int getPBEnd(AnalyzeContext context, int start) {
        if (start + 2 >= context.getSegmentBuff().length - 1) {
            return -1;
        }
        if ((context.getSegmentBuff()[++start] == '^'
                || context.getSegmentBuff()[start] == '_')
                && context.getSegmentBuff()[++start] == '{') {
            int unMatch = 1;
            while (unMatch > 0 && start < context.getSegmentBuff().length - 1) {
                if (context.getSegmentBuff()[start++] == '}') {
                    unMatch--;
                } else if (context.getSegmentBuff()[start++] == '{') {
                    unMatch++;
                }
            }
            if (unMatch > 0) {
                return -1;
            }
        }
        return start;
    }

    private int percentStart;

    /**
     * 百分数
     *
     * @param context
     */
    private void percentFormula(AnalyzeContext context) {
        if (percentStart == -1) {
            if (CharacterUtil.CHAR_ARABIC == context.getCurrentCharType()) {
                percentStart = context.getCursor();
            }
        } else {
            if (context.getCurrentChar() == '%') {
                Lexeme percentLexeme = new Lexeme(context.getBufferOffset(), this.percentStart, context.getCursor() - this.percentStart + 1, Lexeme.TYPE_LATEX);
                context.addLexeme(percentLexeme);
                this.percentStart = -1;
            }
            if (CharacterUtil.CHAR_ARABIC != context.getCurrentCharType() &&
                    context.getCurrentChar() != '.') {
                this.percentStart = -1;
            }
        }
        //判断缓冲区是否已经读完
        if (context.isBufferConsumed()) {
            this.percentStart = -1;
        }
    }

    /**
     * //context后为运算符，也成词
     *
     * @param context
     * @param start
     */
    private void addOperatorFormula(AnalyzeContext context, int start, int end) {
        //后跟运算符
        char c = context.getSegmentBuff()[end];
        if (c == '+' || c == '-' || c == '*' || c == '÷' || c == '=' || c == '%') {
            Lexeme lexeme = new Lexeme(context.getBufferOffset(), start, end - start + 1, Lexeme.TYPE_LATEX);
            context.addLexeme(lexeme);
            //前后均为运算符
            if (start > 0) {
                c = context.getSegmentBuff()[start - 1];
                if (c == '+' || c == '-' || c == '*' || c == '÷' || c == '=' || c == '%') {
                    lexeme = new Lexeme(context.getBufferOffset(), start - 1, end - start + 2, Lexeme.TYPE_LATEX);
                    context.addLexeme(lexeme);
                }
            }
        }

        //前跟运算符
        if (start > 0) {
            c = context.getSegmentBuff()[start - 1];
            if (c == '+' || c == '-' || c == '*' || c == '÷' || c == '=' || c == '%') {
                Lexeme lexeme = new Lexeme(context.getBufferOffset(), start - 1, end - start + 1, Lexeme.TYPE_LATEX);
                context.addLexeme(lexeme);
            }
        }

    }

    public void reset() {
        this.fullFormulaStart = -1;
        this.fracStart = -1;
        this.fracBrace = 0;
        this.fracElement = 0;
        this.sqrtStart = -1;
        this.sqrtBrace = 0;
        this.braceStart = -1;
        this.unBrace = new Stack<Integer>();
        this.onlyStart = -1;
        this.onlyBrace = 0;
        this.percentStart = -1;
    }

}
