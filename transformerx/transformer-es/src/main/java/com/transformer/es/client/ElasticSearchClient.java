package com.transformer.es.client;

import com.transformer.helper.JsonHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Component
@ConfigurationProperties(prefix = "es")
@Slf4j
public class ElasticSearchClient {

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(ElasticSearchClient.class);

    private RestHighLevelClient client;

    private List<ElasticSearchAddress> addresses;

    private String username;

    private String password;

    @Value("${maxConnectTotal:100}")
    private Integer maxConnectTotal;

    @Value("${maxConnPerRoute:100}")
    private Integer maxConnPerRoute;

    private ScheduledExecutorService service;
    /**
     * 描述：数据写入
     * 时间：2019-10-10 11:02
     */
    public String insert(String indexName, String id, String jsonData) {
        int op = 99;
        try {
            IndexResponse response = client.index(new IndexRequest(indexName).id(id).source(jsonData, XContentType.JSON), RequestOptions.DEFAULT);
            op = response.getResult().getOp();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return doOp(op);
    }

    /**
     * 作者：WangLei
     * 描述：批量写入
     * 时间：2019-10-16 09:32
     */
    public String batchInsert(List<ElasticSearchIndex> elasticSearchIndexList) {
        return batchInsert(elasticSearchIndexList, null);
    }

    public String batchInsertImmediately(List<ElasticSearchIndex> elasticSearchIndexList) {
        return batchInsert(elasticSearchIndexList, WriteRequest.RefreshPolicy.IMMEDIATE);
    }

    public String batchInsert(List<ElasticSearchIndex> elasticSearchIndexList, WriteRequest.RefreshPolicy refreshPolicy) {
        if (CollectionUtils.isEmpty(elasticSearchIndexList)) {
            return "列表数据为空,无需进行batchInsert操作!";
        }
        BulkRequest request = new BulkRequest();
        if (refreshPolicy != null) {
            request.setRefreshPolicy(refreshPolicy);
        }
        for (int i = 0; i < elasticSearchIndexList.size(); i++) {
            ElasticSearchIndex ElasticSearchIndex = elasticSearchIndexList.get(i);
            request.add(new IndexRequest(ElasticSearchIndex.getIndexName()).id(ElasticSearchIndex.getId()).source(ElasticSearchIndex.getJsonData(), XContentType.JSON));
        }
        //批量提交到服务器
        BulkResponse bulkResponse = null;
        try {
            bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("Es批量插入报错：" + e.getMessage(), e);
        }
        //提交过程是否产生错误
        if (bulkResponse != null && bulkResponse.hasFailures()) {
            String failureMessage = "ES批量写入报错：" + bulkResponse.buildFailureMessage();
            logger.error(failureMessage);
            return failureMessage;
        }
        return "ES批量写入成功：" + elasticSearchIndexList.size();
    }

    /**
     * 作者：WangLei
     * 描述：批量删除
     * 时间：2019-10-17 10:55
     */
    public String batchDelete(String index, List<String> ids) {
        return batchDelete(index, ids, null);
    }

    public String batchDeleteImmediately(String index, List<String> ids) {
        return batchDelete(index, ids, WriteRequest.RefreshPolicy.WAIT_UNTIL);
    }

    public String batchDelete(String index, List<String> ids, WriteRequest.RefreshPolicy refreshPolicy) {
        if (CollectionUtils.isEmpty(ids)) {
            return "列表数据为空,无需进行batchDelete操作!";
        }
        BulkRequest request = new BulkRequest();
        if (refreshPolicy != null) {
            request.setRefreshPolicy(refreshPolicy);
        }
        for (int i = 0; i < ids.size(); i++) {
            request.add(new DeleteRequest(index, ids.get(i)));
        }
        //批量提交到服务器
        BulkResponse bulkResponse = null;
        try {
            bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("Es批量删除报错：" + e.getMessage(), e);
        }
        //提交过程是否产生错误
        if (bulkResponse != null && bulkResponse.hasFailures()) {
            return "Es批量删除报错：" + bulkResponse.buildFailureMessage();
        }
        return "ES批量删除成功：" + ids.size();

    }

    /**
     * 作者：WangLei
     * 描述：数据更新
     * 时间：2019-10-10 11:02
     */
    public String update(String indexName, String id, String jsonData) {
        int op = 99;
        try {
            UpdateResponse response = client.update(new UpdateRequest(indexName, id).doc(jsonData, XContentType.JSON), RequestOptions.DEFAULT);
            op = response.getResult().getOp();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return doOp(op);
    }

    public String update(String indexName, String id,long seqNo,long primaryTerm,String jsonData) {
        int op = 99;
        try {
            UpdateRequest updateRequest = new UpdateRequest(indexName, id).doc(jsonData, XContentType.JSON);
            updateRequest.setIfSeqNo(seqNo);
            updateRequest.setIfPrimaryTerm(primaryTerm);
            UpdateResponse response = client.update(updateRequest, RequestOptions.DEFAULT);
            op = response.getResult().getOp();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return doOp(op);
    }

    /**
     * 批量更新
     *
     * @param elasticSearchIndexList
     * @return
     */
    public String batchUpdate(List<ElasticSearchIndex> elasticSearchIndexList) {
        return batchUpdate(elasticSearchIndexList, null);
    }

    public String batchUpdateImmediately(List<ElasticSearchIndex> elasticSearchIndexList) {
        return batchUpdate(elasticSearchIndexList, WriteRequest.RefreshPolicy.IMMEDIATE);
    }

    public String batchUpdate(List<ElasticSearchIndex> elasticSearchIndexList, WriteRequest.RefreshPolicy refreshPolicy) {
        if (CollectionUtils.isEmpty(elasticSearchIndexList)) {
            return "列表数据为空,无需进行batchUpdate操作!";
        }
        BulkRequest request = new BulkRequest();
        if (refreshPolicy != null) {
            request.setRefreshPolicy(refreshPolicy);
        }
        for (int i = 0; i < elasticSearchIndexList.size(); i++) {
            ElasticSearchIndex ElasticSearchIndex = elasticSearchIndexList.get(i);
            request.add(new UpdateRequest(ElasticSearchIndex.getIndexName(), ElasticSearchIndex.getId()).doc(ElasticSearchIndex.getJsonData(), XContentType.JSON));
        }
        //批量提交到服务器
        BulkResponse bulkResponse = null;
        try {
            bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("Es批量更新报错：" + e.getMessage(), e);
        }
        //提交过程是否产生错误
        if (bulkResponse != null && bulkResponse.hasFailures()) {
            String failureMessage = "ES批量更新报错：" + bulkResponse.buildFailureMessage();
            logger.error(failureMessage);
            return failureMessage;
        }
        return "ES批量更新成功：" + elasticSearchIndexList.size();
    }

    /**
     * 作者：WangLei
     * 描述：数据删除
     * 时间：2019-10-10 11:02
     */
    public String delete(String indexName, String id) {
        int op = 99;
        try {
            DeleteResponse response = client.delete(new DeleteRequest(indexName, id), RequestOptions.DEFAULT);
            op = response.getResult().getOp();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return doOp(op);
    }

    //删除索引
    public boolean deleteIndex(String index) throws IOException {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(index);
        try{
            AcknowledgedResponse delete = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            return delete.isAcknowledged();
        }catch(IOException e){
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    //索引是否存在
    public boolean existsIndex(String index) throws IOException {
        try{
            GetIndexRequest request = new GetIndexRequest(index);
            return  client.indices().exists(request, RequestOptions.DEFAULT);
        }catch(IOException e){
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public <T> List<T> searchAll(String index,
                                 QueryBuilder queryBuilder,
                                 List<SortBuilder> sortBuilders,
                                 Class<T> clazz) {

        List<T> list = new ArrayList<>();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.size(Configurations.ES_MAX_SIZE);

        if (!CollectionUtils.isEmpty(sortBuilders)) {
            for (SortBuilder sortBuilder : sortBuilders) {
                searchSourceBuilder.sort(sortBuilder);
            }
        }

        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.source(searchSourceBuilder);

        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            for (SearchHit hit : searchHits) {
                String json = hit.getSourceAsString();
                list.add(JsonHelper.parseObject(json, clazz));
            }
        } catch (Exception e) {
            logger.error("es查询报错：" + e.getMessage(), e);
        }

        return list;
    }

    public <T> List<T> searchScroll(String index,
                                    QueryBuilder queryBuilder,
                                    Class<T> clazz) throws IOException {
        return searchScroll(index, queryBuilder, null, clazz);
    }

    public <T> List<T> searchScroll(String index,
                                    QueryBuilder queryBuilder,
                                    List<SortBuilder> sortBuilders,
                                    Class<T> clazz) throws IOException {

        List<T> list = new ArrayList<>();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.size(Configurations.SCROLL_STEP_SIZE);

        if (!CollectionUtils.isEmpty(sortBuilders)) {
            for (SortBuilder sortBuilder : sortBuilders) {
                searchSourceBuilder.sort(sortBuilder);
            }
        }

        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.source(searchSourceBuilder);

        //设定滚动时间间隔1分钟
        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1));
        searchRequest.scroll(scroll);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        assert searchResponse != null;

        String scrollId = searchResponse.getScrollId();
        while (searchResponse.getHits().getHits().length != 0) {
            for (SearchHit hit : searchResponse.getHits().getHits()) {
                String json = hit.getSourceAsString();
                list.add(JsonHelper.parseObject(json, clazz));
            }

            scrollId = searchResponse.getScrollId();
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
        }

        //清除滚屏
        this.clearScroll(scrollId);

        return list;
    }

    private boolean clearScroll(String scrollId) {
        //清除滚屏
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        try {
            ClearScrollResponse clearScrollResponse = client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
            if (clearScrollResponse != null) {
                return clearScrollResponse.isSucceeded();
            }
        } catch (Exception e) {
            logger.error("es清除滚屏报错：" + e.getMessage(), e);
        }
        return false;
    }

    /**
     * @Description: 适合实时翻页，但不适合批量查询（慎用，实时查询下一页，如果数据，一直有新增，do {}while可能死循环）
     * @Date: 2022/8/27 18:48
     */
    public <T> List<T> searchAfter(String index,
                                   QueryBuilder queryBuilder,
                                   List<SortBuilder> sortBuilders,
                                   Class<T> clazz) {

        List<T> list = new ArrayList<>();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.size(Configurations.ES_MAX_SIZE);

        if (!CollectionUtils.isEmpty(sortBuilders)) {
            for (SortBuilder sortBuilder : sortBuilders) {
                searchSourceBuilder.sort(sortBuilder);
            }
        }
        searchSourceBuilder.sort("_doc", SortOrder.ASC);//保证sort唯一,防止丢失数据

        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = null;
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("es查询报错：" + e.getMessage(), e);
        }

        assert searchResponse != null;

        SearchHit[] searchHits = searchResponse.getHits().getHits();
        if (searchHits.length == 0) {
            return list;
        }

        do {
            for (SearchHit hit : searchHits) {
                String json = hit.getSourceAsString();
                list.add(JsonHelper.parseObject(json, clazz));
            }

            searchRequest.source().searchAfter(searchHits[searchHits.length - 1].getSortValues());
            try {
                searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                searchHits = searchResponse.getHits().getHits();
            } catch (Exception e) {
                logger.error("es查询报错：" + e.getMessage(), e);
            }

        } while (searchHits.length != 0);

        return list;
    }


    private String doOp(int op) {
        String result;
        switch (op) {
            case 0:
                result = "CREATED success ！";
                break;
            case 1:
                result = "UPDATED success ！";
                break;
            case 2:
                result = "DELETED success ！";
                break;
            case 3:
                result = "NOT_FOUND ！";
                break;
            case 4:
                result = "NOOP ！";
                break;
            case 99:
                result = "OPERATE error !";
                break;
            default:
                result = "未知op：" + op;
        }
        return result;
    }

    @PostConstruct
    public void init() {
        // 初始化
        service = Executors.newScheduledThreadPool(1);
        // 客户端异常检测
        service.scheduleWithFixedDelay(this::check, 0, 5, TimeUnit.MINUTES);
    }

    private void initEsClient() {
        logger.info("ES客户端初始化...");
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        HttpHost[] hosts = new HttpHost[addresses.size()];
        for (int i = 0, j = addresses.size(); i < j; i++) {
            hosts[i] = new HttpHost(addresses.get(i).getIpAddress(), addresses.get(i).getPort());
        }

        client = new RestHighLevelClient(RestClient.builder(hosts).setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                httpClientBuilder.disableAuthCaching();
                httpClientBuilder.setMaxConnTotal(maxConnectTotal);
                httpClientBuilder.setMaxConnPerRoute(maxConnPerRoute);
                return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }));
    }

    private void check() {
        logger.info("ES客户端检测");
        if (client == null || !client.getLowLevelClient().isRunning()) {
            initEsClient();
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            if (client != null) {
                client.close();
            }
        } catch (IOException e) {
            logger.error("client close error ! ", e);
        }
    }


    public RestHighLevelClient getClient() {
        return client;
    }

    public void setClient(RestHighLevelClient client) {
        this.client = client;
    }

    public List<ElasticSearchAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<ElasticSearchAddress> addresses) {
        this.addresses = addresses;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void batchDeleteByQuery(String index, QueryBuilder queryBuilder) {
        try {
            DeleteByQueryRequest request = new DeleteByQueryRequest(index);
            request.setQuery(queryBuilder);
            //request.setBatchSize(10000);
            //request.setConflicts("proceed");
            BulkByScrollResponse response = client.deleteByQuery(request, RequestOptions.DEFAULT);
            logger.info("ES批量条件删除成功：" + response);
        } catch (IOException e) {
            logger.error("ES批量条件删除异常", e);
        }
    }

    /**
     * 描述：业务常量类定义
     */
    private interface Configurations {
        /**
         * 渡口
         */
        Long FERRY_SITE_ID = 1047725L;

        // ---------- ES 相关常量 ---------

        //中心时刻监控
        String CENTER_MONITOR_INDEX = "center_monitor_index";
        //批次变化
        String BATCH_CHANGE_INDEX = "batch_change_index";
        //批次变化别名
        String BATCH_CHANGE_INDEX_ALIAS = "batch_change_index_alias";
        // scroll查询size
        int SCROLL_STEP_SIZE = 10000;


        int ES_MAX_SIZE = 10000;

        int ES_MAX_SIZE_50000 = 50000;

    }
}
