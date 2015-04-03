package org.wltea.analyzer.core;

abstract interface ISegmenter
{
  public abstract void analyze(AnalyzeContext paramAnalyzeContext);

  public abstract void reset();
}