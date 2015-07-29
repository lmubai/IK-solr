package org.wltea.analyzer.core;

import java.util.Stack;

/**
 * Created by ybxsearch on 2015/7/27.
 * LaTeX公式分词器
 */
public class LaTeXSegmenter implements ISegmenter {
    //子分词器标签
    public static final String SEGMENTER_NAME = "LATEX_SEGMENTER";

    //未匹配的'{'位置
    private Stack<Integer> unBrace;


    //整体公式起始结束位置
    private int fullFormulaStart;

    private int fracStart;
    private int fracBrace;
    private int fracElement;

    private int sqrtStart;
    private int sqrtBrace;

    private int onlyStart;
    private int onlyBrace;

    private int braceStart;

    private boolean buffEqual(AnalyzeContext context, String s) {
        if (context.getSegmentBuff().length > (context.getCursor() + s.length())) {
            return s.equals(String.copyValueOf(context.getSegmentBuff(), context.getCursor(), s.length()));
        }
        return false;
    }

    private boolean oneEqual(AnalyzeContext context, char... chars) {
        for (char c : chars.clone()) {
            if (c == context.getCurrentChar())
                return true;
        }
        return false;
    }

    private boolean buffAroundEqual(AnalyzeContext context, String s) {
        char[] buff = context.getSegmentBuff();
        for (int i = 0; i < s.length() && i <= context.getCursor(); i++) {
            if (s.equals(String.copyValueOf(buff, context.getCursor() - i, s.length()))) {
                return true;
            }
        }
        return false;
    }

    public void analyze(AnalyzeContext context) {
        fullFormula(context);
        fracFormula(context);
        sqrtFormula(context);
        braceFormula(context);
        bigBraceFormula(context);
        onlyNumberLetterPB(context);
    }

    /**
     * 纯数字字母和上下标
     *
     * @param context
     */
    private void onlyNumberLetterPB(AnalyzeContext context) {
        if (onlyStart == -1) {
            if (CharacterUtil.CHAR_ARABIC == context.getCurrentCharType() || CharacterUtil.CHAR_ENGLISH == context.getCurrentCharType()) {
                if (!(buffAroundEqual(context, "\\sqrt") || buffAroundEqual(context, "\\frac"))) {
                    this.onlyStart = context.getCursor();
                }
            }
        } else {
            if (CharacterUtil.CHAR_ARABIC == context.getCurrentCharType() || CharacterUtil.CHAR_ENGLISH == context.getCurrentCharType() ||
                    (context.getCurrentChar() >= 0x3B0 && context.getCurrentChar() <= 0x3C9) ||
                    oneEqual(context, '^', '{', '}', '_')) {
                if ('{' == context.getCurrentChar()) {
                    onlyBrace++;
                } else if ('}' == context.getCurrentChar()) {
                    onlyBrace--;
                    if (onlyBrace < 0) {
                        Lexeme newLexeme = new Lexeme(context.getBufferOffset(), onlyStart, context.getCursor() - onlyStart, Lexeme.TYPE_LATEX);
                        context.addLexeme(newLexeme);
                        this.onlyStart = -1;
                        onlyBrace = 0;
                    }
                }
            } else {
                Lexeme newLexeme = new Lexeme(context.getBufferOffset(), onlyStart, context.getCursor() - onlyStart, Lexeme.TYPE_LATEX);
                context.addLexeme(newLexeme);
                if (oneEqual(context, '+', '-', '=', '*', '÷')) {
                    newLexeme = new Lexeme(context.getBufferOffset(), onlyStart, context.getCursor() - onlyStart + 1, Lexeme.TYPE_LATEX);
                    context.addLexeme(newLexeme);
                }
                onlyBrace = 0;
                this.onlyStart = -1;
            }
        }

        //判断缓冲区是否已经读完
        if (context.isBufferConsumed()) {
            if (this.onlyStart != -1) {
                //缓冲以读完，输出词元
                Lexeme newLexeme = new Lexeme(context.getBufferOffset(), this.onlyStart, context.getCursor() - this.onlyStart + 1, Lexeme.TYPE_LATEX);
                context.addLexeme(newLexeme);
                this.onlyStart = -1;
            }
        }
    }

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
                Lexeme newLexeme = new Lexeme(context.getBufferOffset(), start, context.getCursor() - start + 1, Lexeme.TYPE_LATEX);
                context.addLexeme(newLexeme);
                newLexeme = new Lexeme(context.getBufferOffset(), start + 1, context.getCursor() - start - 1, Lexeme.TYPE_LATEX);
                context.addLexeme(newLexeme);
            }
        }
    }

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
                    Lexeme newLexeme = new Lexeme(context.getBufferOffset(), this.sqrtStart, context.getCursor() - this.sqrtStart + 1, Lexeme.TYPE_LATEX);
                    context.addLexeme(newLexeme);
                    this.sqrtStart = -1;
                }
            }
        }
    }

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
                    Lexeme newLexeme = new Lexeme(context.getBufferOffset(), this.fracStart, context.getCursor() - this.fracStart + 1, Lexeme.TYPE_LATEX);
                    context.addLexeme(newLexeme);
                    if (fracElement == 2) {
                        this.fracElement = 0;
                        this.fracStart = -1;
                    }
                }
            }
        }
    }

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
                Lexeme newLexeme = new Lexeme(context.getBufferOffset(), this.fullFormulaStart + 1, context.getCursor() - this.fullFormulaStart - 1, Lexeme.TYPE_LATEX);
                context.addLexeme(newLexeme);
                this.fullFormulaStart = -1;
            }
        }

    }

    /**
     * 小括号(**)与**
     *
     * @param context
     */
    private void braceFormula(AnalyzeContext context) {
        if (')' == context.getCurrentChar() && this.braceStart > -1) {
            Lexeme newLexeme = new Lexeme(context.getBufferOffset(), this.braceStart, context.getCursor() - this.braceStart + 1, Lexeme.TYPE_LATEX);
            context.addLexeme(newLexeme);
            newLexeme = new Lexeme(context.getBufferOffset(), this.braceStart + 1, context.getCursor() - this.braceStart - 1, Lexeme.TYPE_LATEX);
            context.addLexeme(newLexeme);
            this.braceStart = -1;
        }
        if ('(' == context.getCurrentChar()) {
            this.braceStart = context.getCursor();
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
    }
}
