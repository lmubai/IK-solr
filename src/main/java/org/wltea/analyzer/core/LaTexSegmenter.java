package org.wltea.analyzer.core;

import java.util.Arrays;

/**
 * Created by ybxsearch on 2015/4/7.
 */
 class LaTexSegmenter implements ISegmenter {
    static final String SEGMENTER_NAME = "LATEX_SEGMENTER";
    private static final char[] USED_CHARS = {32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126};
    private int start;
    private int end;

    LaTexSegmenter()
    {
        Arrays.sort(USED_CHARS);
        this.start = -1;
        this.end = -1;
    }

    public void analyze(AnalyzeContext context)
    {
        boolean bufferLockFlag = false;

        bufferLockFlag = (processLatex(context)) || (bufferLockFlag);

        if (bufferLockFlag) {
            context.lockBuffer("LATEX_SEGMENTER");
        }
        else
            context.unlockBuffer("LATEX_SEGMENTER");
    }

    public void reset()
    {
        this.start = -1;
        this.end = -1;
    }

    private boolean processLatex(AnalyzeContext context)
    {
        boolean needLock = false;

        if (this.start == -1) {
            if ((1 == context.getCurrentCharType()) ||
                    (2 == context.getCurrentCharType()))
            {
                this.start = context.getCursor();
                this.end = this.start;
            }

        }
        else if ((1 == context.getCurrentCharType()) ||
                (2 == context.getCurrentCharType()))
        {
            this.end = context.getCursor();
        }
        else if ((context.getCurrentCharType() == 0) &&
                (isUsedChar(context.getCurrentChar())))
        {
            this.end = context.getCursor();
        }
        else {
            Lexeme newLexeme = new Lexeme(context.getBufferOffset(), this.start, this.end - this.start + 1, 3);
            context.addLexeme(newLexeme);
            this.start = -1;
            this.end = -1;
        }

        if ((context.isBufferConsumed()) &&
                (this.start != -1) && (this.end != -1))
        {
            Lexeme newLexeme = new Lexeme(context.getBufferOffset(), this.start, this.end - this.start + 1, 5);
            context.addLexeme(newLexeme);
            this.start = -1;
            this.end = -1;
        }

        if ((this.start == -1) && (this.end == -1))
        {
            needLock = false;
        }
        else needLock = true;

        return needLock;
    }

    private boolean isUsedChar(char input)
    {
        int index = Arrays.binarySearch(USED_CHARS, input);
        return index >= 0;
    }

}
