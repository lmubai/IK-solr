package org.wltea.analyzer.core;

import java.util.Arrays;

/**
 * 英文分词器
 * Created by two8g on 16-7-25.
 */
class EnglishSegmenter implements ISegmenter {
    //子分词器标签
    static final String SEGMENTER_NAME = "ENGLISH_SEGMENTER";
    private static final char[] ENGLISH_CONNECTOR = {'-', '\''};

    private int start;
    private int end;
    private boolean connected;

    EnglishSegmenter() {
        Arrays.sort(ENGLISH_CONNECTOR);
        this.start = -1;
        this.end = -1;
        this.connected = false;
    }

    public void analyze(AnalyzeContext context) {
        analyzeEnglishWords(context);
        if (this.start == -1 && this.end == -1 && !connected) {
            context.unlockBuffer(SEGMENTER_NAME);
        } else {
            context.lockBuffer(SEGMENTER_NAME);
        }
    }

    public void analyzeEnglishWords(AnalyzeContext context) {
        if (this.start == -1) {
            if (CharacterUtil.CHAR_ENGLISH == context.getCurrentCharType()) {
                this.start = context.getCursor();
                this.end = context.getCursor();
            }
        } else {
            if (CharacterUtil.CHAR_ENGLISH == context.getCurrentCharType()) {
                this.end = context.getCursor();
            } else if (isEnglishConnector(context.getCurrentChar())) {
                if (connected) {
                    reset();
                    return;
                } else {
                    connected = true;
                }
            } else {
                Lexeme lexeme = new Lexeme(context.getBufferOffset(), start, end - start + 1, Lexeme.TYPE_ENGLISH);
                context.addLexeme(lexeme);
                reset();
            }
        }

        //判断缓冲区是否已经读完
        if (context.isBufferConsumed() && this.start != -1 && this.end != -1) {
            //缓冲已读完，输出词元
            Lexeme newLexeme = new Lexeme(context.getBufferOffset(), this.start, this.end - this.start + 1, Lexeme.TYPE_ENGLISH);
            context.addLexeme(newLexeme);
            reset();
        }
    }

    public void reset() {
        this.connected = false;
        this.start = -1;
        this.end = -1;
    }

    /**
     * 判断是否是字母连接符号
     *
     * @param input
     * @return
     */
    private boolean isEnglishConnector(char input) {
        int index = Arrays.binarySearch(ENGLISH_CONNECTOR, input);
        return index >= 0;
    }
}
