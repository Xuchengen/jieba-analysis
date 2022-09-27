package com.github.xuchengen.jieba;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;

/**
 * <p>词典
 * <p>作者：徐承恩
 * <p>邮箱：<a href="mailto:xuchengen@gmail.com">xuchengen@gmail.com</a>
 * <p>日期：2022-09-27 12:27
 **/
public class WordDictionary {

    private static final Logger log = LoggerFactory.getLogger(WordDictionary.class);

    private static volatile WordDictionary singleton;
    private static final String MAIN_DICT = "/dict.txt";
    private static final String USER_DICT_SUFFIX = ".dict";

    public final Map<String, Double> freqs = new HashMap<>();
    public final Set<String> loadedPath = new HashSet<>();
    private Double minFreq = Double.MAX_VALUE;
    private Double total = 0.0;
    private DictSegment _dict;


    private WordDictionary() {
        this.loadDict();
    }


    public static WordDictionary getInstance() {
        if (singleton == null) {
            synchronized (WordDictionary.class) {
                if (singleton == null) {
                    singleton = new WordDictionary();
                    return singleton;
                }
            }
        }
        return singleton;
    }


    /**
     * 初始化词典
     *
     * @param configPath 词典路径
     */
    public void init(Path configPath) {
        String absPath = configPath.toAbsolutePath().toString();
        log.debug("initialize user dictionary:{}", absPath);
        synchronized (WordDictionary.class) {
            if (loadedPath.contains(absPath)) {
                return;
            }

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(configPath,
                    String.format(Locale.getDefault(), "*%s", USER_DICT_SUFFIX))) {

                for (Path path : stream) {
                    log.debug(String.format(Locale.getDefault(), "loading dict %s", path));
                    singleton.loadUserDict(path);
                }

                loadedPath.add(absPath);
            } catch (Exception e) {
                log.error(String.format(Locale.getDefault(), "%s: load user dict failure!", configPath), e);
                throw new JiebaException(e);
            }
        }
    }

    /**
     * 初始化词典
     *
     * @param paths 词典路径
     */
    public void init(String[] paths) {
        synchronized (WordDictionary.class) {
            for (String path : paths) {
                if (!loadedPath.contains(path)) {
                    try {
                        log.debug("initialize user dictionary: {}", path);
                        singleton.loadUserDict(path);
                        loadedPath.add(path);
                    } catch (Exception e) {
                        log.error(String.format(Locale.getDefault(), "%s: load user dict failure!", path), e);
                        throw new JiebaException(e);
                    }
                }
            }
        }
    }

    /**
     * 重置词典
     */
    public void resetDict() {
        _dict = new DictSegment((char) 0);
        freqs.clear();
    }

    /**
     * 加载词典
     */
    public void loadDict() {
        _dict = new DictSegment((char) 0);
        try (InputStream is = this.getClass().getResourceAsStream(MAIN_DICT)) {
            if (Objects.isNull(is)) throw new FileNotFoundException("dictionary file not found");
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            long s = System.currentTimeMillis();

            while (br.ready()) {
                String line = br.readLine();
                String[] tokens = line.split("[\t ]+");

                if (tokens.length < 2) continue;

                String word = tokens[0];
                double freq = Double.parseDouble(tokens[1]);
                total += freq;
                word = addWord(word);
                freqs.put(word, freq);
            }

            for (Entry<String, Double> entry : freqs.entrySet()) {
                entry.setValue((Math.log(entry.getValue() / total)));
                minFreq = Math.min(entry.getValue(), minFreq);
            }

            log.debug(String.format(Locale.getDefault(), "main dict load finished, time elapsed %d ms",
                    System.currentTimeMillis() - s));

        } catch (Exception e) {
            log.error(String.format(Locale.getDefault(), "%s load failure!", MAIN_DICT), e);
            throw new JiebaException(e);
        }
    }

    private String addWord(String word) {
        if (null != word && !StringUtils.EMPTY.equals(word.trim())) {
            String key = word.trim().toLowerCase(Locale.getDefault());
            _dict.fillSegment(key.toCharArray());
            return key;
        } else {
            return null;
        }
    }

    /**
     * 加载用户词典
     *
     * @param userDict 用户词典路径
     */
    public void loadUserDict(Path userDict) {
        loadUserDict(userDict, StandardCharsets.UTF_8);
    }

    /**
     * 加载用户词典
     *
     * @param userDictPath 用户词典路径
     */
    public void loadUserDict(String userDictPath) {
        loadUserDict(userDictPath, StandardCharsets.UTF_8);
    }

    /**
     * 加载用户词典
     *
     * @param userDict 用户词典路径
     * @param charset  字符编码
     */
    public void loadUserDict(Path userDict, Charset charset) {
        try (BufferedReader br = Files.newBufferedReader(userDict, charset)) {
            loadUserdict(br);
        } catch (Exception e) {
            log.error(String.format(Locale.getDefault(), "%s: load user dict failure!", userDict), e);
            throw new JiebaException(e);
        }
    }

    public void loadUserDict(String userDictPath, Charset charset) {
        try (InputStream is = this.getClass().getResourceAsStream(userDictPath)) {
            if (Objects.isNull(is)) throw new FileNotFoundException("dictionary file not found");
            BufferedReader br = new BufferedReader(new InputStreamReader(is, charset));
            loadUserdict(br);
            br.close();
        } catch (Exception e) {
            log.error(String.format(Locale.getDefault(), "%s: load user dict failure!", userDictPath), e);
            throw new JiebaException(e);
        }
    }

    private void loadUserdict(BufferedReader bufferedReader) throws IOException {
        long s = System.currentTimeMillis();
        while (bufferedReader.ready()) {
            String line = bufferedReader.readLine();
            String[] tokens = line.split("[\t ]+");

            if (tokens.length < 1) continue;

            String word = tokens[0];

            double freq = 3.0d;
            if (tokens.length == 2)
                freq = Double.parseDouble(tokens[1]);
            word = addWord(word);
            freqs.put(word, Math.log(freq / total));
        }

        log.debug(String.format(Locale.getDefault(), "user dict load finished, time elapsed %d ms",
                System.currentTimeMillis() - s));
    }

    public DictSegment getTrie() {
        return this._dict;
    }


    public boolean containsWord(String word) {
        return freqs.containsKey(word);
    }


    public Double getFreq(String key) {
        if (containsWord(key))
            return freqs.get(key);
        else
            return minFreq;
    }
}
