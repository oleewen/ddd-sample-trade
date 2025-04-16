package com.transformer.es.service.model;

import com.transformer.context.Environment;
import com.transformer.es.client.ElasticSearchIndex;
import com.transformer.helper.DateHelper;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ouliyuan 2023/7/18
 */
public class Indexes {

    public static final String INDEX_BEFORE_PREFIX = "index-before-";
    public static final String INDEX_PREFIX = "index-";
    private static final int DEFAULT_INDEX_NUM = 40;
    @Setter
    private int indexNum;

    public ElasticSearchIndex getIndex(Date indexTime) {

        String day = DateHelper.formatDate(indexTime);

        int days = DateHelper.days(indexTime, new Date());
        String indexName;
        if (days < getDayIndexNum()) {
            indexName = INDEX_PREFIX + day;
        } else {
            indexName = INDEX_BEFORE_PREFIX + day;
        }

        return new ElasticSearchIndex(indexName);
    }

    private int getDayIndexNum() {
        String indexNum = Environment.getEnvProperty("es_index_contract");
        return indexNum!=null?Integer.parseInt(indexNum): DEFAULT_INDEX_NUM;
    }

    public ElasticSearchIndex getLastDayIndex() {
        String last = getLastDay();

        return new ElasticSearchIndex(INDEX_PREFIX + last);
    }

    public ElasticSearchIndex getHistoryIndex() {
        String last = getLastDay();

        return new ElasticSearchIndex(INDEX_BEFORE_PREFIX + last);
    }

    public String getLastDay() {
        // day index有indexNum-1个
        int n = getDayIndexNum();

        Date last = new Date();
        // n=0，即无day index，全部放在历史索引
        // n=1，即仅一个day index
        // n>1，超过一个day index
        if (n > 1) {
            // 下标少减一天，得到正确的天数：例如n=2时，0718-2=0716，实际上是0718/0717两个索引
            last = DateHelper.addDays(new Date(), -(n - 1));
        }

        return DateHelper.formatDate(last);
    }

    public List<ElasticSearchIndex> getReindexIndexes(List<ElasticSearchIndex> availableIndexes) {
        if (CollectionUtils.isEmpty(availableIndexes)) {
            return Collections.emptyList();
        }

        String lastDay = getLastDay();

        return availableIndexes.stream().filter(e -> e.getIndexName().contains(lastDay) || before(e.getIndexName(), lastDay)).collect(Collectors.toList());
    }

    private boolean before(String indexName, String lastDay) {
        if (StringUtils.isBlank(indexName)) {
            return false;
        }

        indexName = StringUtils.remove(indexName, Indexes.INDEX_BEFORE_PREFIX);
        indexName = StringUtils.remove(indexName, Indexes.INDEX_PREFIX);

        return StringUtils.compare(indexName, lastDay) < 0;
    }

}
