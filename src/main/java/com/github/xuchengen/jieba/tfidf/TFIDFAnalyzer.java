package com.github.xuchengen.jieba.tfidf;

import com.github.xuchengen.jieba.JiebaException;
import com.github.xuchengen.jieba.JiebaSegmenter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * <p>TFIDF算法
 * <p><a href="http://www.cnblogs.com/ywl925/p/3275878.html">TFIDF算法原理参考</a>
 * <p><a href="https://github.com/fxsjy/jieba">实现思路参考jieba分词</a>
 * <p>作者：徐承恩
 * <p>邮箱：<a href="mailto:xuchengen@gmail.com">xuchengen@gmail.com</a>
 * <p>日期：2022-09-27 14:19
 **/
public class TFIDFAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(TFIDFAnalyzer.class);

    private static HashMap<String, Double> idfMap;
    private static HashSet<String> stopWordsSet;
    private static double idfMedian;

    private final JiebaSegmenter JIEBA_SEGMENTER;

    public TFIDFAnalyzer(JiebaSegmenter jiebaSegmenter) {
        this.JIEBA_SEGMENTER = jiebaSegmenter;
    }

    /**
     * TFIDF分析方法
     *
     * @param content 需要分析的文本/文档内容
     * @param topN    需要返回的TFIDF值最高的N个关键词，若超过content本身含有的词语上限数目，则默认返回全部
     * @return List&lt;Keyword&gt;
     */
    public List<Keyword> analyze(String content, int topN) {
        List<Keyword> keywordList = new ArrayList<>();

        if (stopWordsSet == null) {
            stopWordsSet = new HashSet<>();
            loadStopWords(stopWordsSet, this.getClass().getResourceAsStream("/stop_words.txt"));
        }

        if (idfMap == null) {
            idfMap = new HashMap<>();
            loadIDFMap(idfMap, this.getClass().getResourceAsStream("/idf_dict.txt"));
        }

        Map<String, Double> tfMap = getTF(content);
        for (String word : tfMap.keySet()) {
            // 若该词不在IDF文档中，则使用平均的IDF值(可能定期需要对新出现的网络词语进行纳入)
            if (idfMap.containsKey(word)) {
                keywordList.add(new Keyword(word, idfMap.get(word) * tfMap.get(word)));
            } else
                keywordList.add(new Keyword(word, idfMedian * tfMap.get(word)));
        }

        Collections.sort(keywordList);

        if (keywordList.size() > topN) {
            int num = keywordList.size() - topN;
            for (int i = 0; i < num; i++) {
                keywordList.remove(topN);
            }
        }
        return keywordList;
    }

    /**
     * TF值计算公式
     * <pre>tf = N(i,j) / (sum(N(k,j) for all k))</pre>
     * <p><code>N(i,j)</code>表示词语<code>Ni</code>
     * 在该文档<code>d(content)</code>中出现的频率，<code>sum(N(k,j))</code>代表所有词语在文档d中出现的频率之和
     *
     * @param content 文本
     * @return Map&lt;String, Double&gt;
     */
    private Map<String, Double> getTF(String content) {
        Map<String, Double> tfMap = new HashMap<>();
        if (content == null || content.equals(StringUtils.EMPTY)) return tfMap;

        List<String> words = JIEBA_SEGMENTER.sentenceProcess(content);
        Map<String, Integer> freqMap = new HashMap<>();

        int wordSum = 0;
        for (String word : words) {
            //停用词不予考虑，单字词不予考虑，标点符号词不予考虑
            if (!stopWordsSet.contains(word) && word.length() > 1) {
                word = word.replaceAll("\\p{P}", StringUtils.EMPTY);
                if (word.length() <= 1) continue;
                wordSum++;
                if (freqMap.containsKey(word)) {
                    freqMap.put(word, freqMap.get(word) + 1);
                } else {
                    freqMap.put(word, 1);
                }
            }
        }

        // 计算double型的TF值
        for (String word : freqMap.keySet()) {
            tfMap.put(word, freqMap.get(word) * 0.1 / wordSum);
        }

        return tfMap;
    }

    /**
     * 加载停用词表
     * <p><a href="https://github.com/yanyiwu/nodejieba/blob/master/dict/stop_words.utf8">结巴分词停用词表</a>
     *
     * @param set set集合
     * @param in  输入流
     */
    private void loadStopWords(Set<String> set, InputStream in) {
        try (BufferedReader bufr = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = bufr.readLine()) != null) {
                set.add(line.trim());
            }
        } catch (Exception e) {
            log.error("加载停用词异常：", e);
            throw new JiebaException(e);
        }
    }

    /**
     * 加载IDF表
     * <p>IDF值本来需要语料库来自己按照公式进行计算，不过jieba分词已经提供了一份很好的IDF字典，所以默认直接使用jieba分词的IDF字典
     * <p><a href="https://raw.githubusercontent.com/yanyiwu/nodejieba/master/dict/idf.utf8">结巴分词IDF表</a>
     *
     * @param map map集合
     * @param in  输入流
     */
    private void loadIDFMap(Map<String, Double> map, InputStream in) {
        try (BufferedReader bufr = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = bufr.readLine()) != null) {
                String[] kv = line.trim().split(" ");
                map.put(kv[0], Double.parseDouble(kv[1]));
            }

            // 计算IDF值的中位数
            List<Double> idfList = new ArrayList<>(map.values());
            Collections.sort(idfList);
            idfMedian = idfList.get(idfList.size() / 2);
        } catch (Exception e) {
            log.error("加载IDF异常：", e);
            throw new JiebaException(e);
        }
    }
}
