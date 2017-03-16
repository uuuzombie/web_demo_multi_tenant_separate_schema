package com.sky.demo.web_demo_multi_tenant_separate_schema.es.document;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.sky.demo.web_demo_multi_tenant_separate_schema.es.EsClient;
import com.sky.demo.web_demo_multi_tenant_separate_schema.es.dto.SearchCondition;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.util.CollectionUtil;
import org.elasticsearch.action.explain.ExplainResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by user on 16/11/29.
 */
@Service
public class SearchServiceImpl implements SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);

    @Resource
    private EsClient esClient;


    @Override
    public SearchResponse search(String index, String type, QueryBuilder queryBuilder, SortBuilder sortBuilder, int from, int size) {
        Preconditions.checkState(StringUtils.isNotBlank(index), "index is null!");
        Preconditions.checkState(StringUtils.isNotBlank(type), "type is null");
        Preconditions.checkNotNull(queryBuilder, "QueryBuilder is null");


        logger.info("QueryBuilder : {}", queryBuilder.toString());
        SearchResponse response = esClient.getTransportClient().prepareSearch(index)
                .setTypes(type)
                .setQuery(queryBuilder)
                .addSort(sortBuilder)
                .setFrom(from)
                .setSize(size)
                .get();

        return response;
    }

    @Override
    public SearchResponse search(SearchCondition searchCondition) {
        Preconditions.checkNotNull(searchCondition, "searchCondition is null!");
        SearchRequestBuilder builder = esClient.getTransportClient().prepareSearch();
        builder.setIndices((String[]) searchCondition.getIndexes().toArray())
                .setTypes((String[]) searchCondition.getTypes().toArray())
                .setQuery(searchCondition.getQueryBuilder())
                .setFrom(searchCondition.getFrom())
                .setSize(searchCondition.getSize())
                .setSearchType(searchCondition.getSearchType())
                .setExplain(searchCondition.getExplain());

        if (CollectionUtils.isNotEmpty(searchCondition.getSortBuilders())) {
            searchCondition.getSortBuilders().forEach(sortBuilder -> builder.addSort(sortBuilder));
        }

        SearchResponse response = builder.get();    //.execute().actionGet()

        return response;
    }

    @Override
    public long searchCount(SearchCondition searchCondition) {
        Preconditions.checkNotNull(searchCondition, "searchCondition is null!");
        SearchRequestBuilder builder = esClient.getTransportClient().prepareSearch();
        builder.setIndices((String[]) searchCondition.getIndexes().toArray())
                .setTypes((String[]) searchCondition.getTypes().toArray())
                .setQuery(searchCondition.getQueryBuilder())
                .setFrom(searchCondition.getFrom())
                .setSize(searchCondition.getSize())
                .setSearchType(searchCondition.getSearchType())
                .setExplain(searchCondition.getExplain());

        if (CollectionUtils.isNotEmpty(searchCondition.getSortBuilders())) {
            searchCondition.getSortBuilders().forEach(sortBuilder -> builder.addSort(sortBuilder));
        }

        SearchResponse response = builder.get();

        long count = 0;
        if (response != null) {
            count = response.getHits().getTotalHits();
        }
        logger.info("search count result : {}", count);
        return count;
    }

    @Override
    public SearchResponse searchUsingScroll(SearchCondition searchCondition) {
        Preconditions.checkNotNull(searchCondition, "searchCondition is null!");

        SearchRequestBuilder builder = esClient.getTransportClient().prepareSearch();
        builder.setIndices((String[]) searchCondition.getIndexes().toArray())
                .setTypes((String[]) searchCondition.getTypes().toArray())
                .setScroll(new TimeValue(60000))
                .setSize(searchCondition.getSize())          //max of SIZE hits will be returned for each scroll
                .setExplain(searchCondition.getExplain());


        SearchResponse response = builder.get();
        String scrollId = response.getScrollId();
        logger.info("scrollId={}, took time={} ms", scrollId, response.getTookInMillis());
        //Scroll until no hits are returned
        do {

            response = esClient.getTransportClient()
                    .prepareSearchScroll(scrollId)
                    .setScroll(new TimeValue(60000))
                    .get();   //.execute().actionGet();

            scrollId = response.getScrollId();  //maybe multi index
            logger.info("scrollId={}, took time={} ms", scrollId, response.getTookInMillis());

        } while (response.getHits().getHits().length != 0);


        ClearScrollResponse clearScrollResponse = esClient.getTransportClient()
                .prepareClearScroll().get();
        logger.debug("clear scroll , id={}, isSucceeded={}", scrollId, clearScrollResponse.isSucceeded());
        return response;
    }

    @Override
    public long searchCountUsingScroll(SearchCondition searchCondition) {
        Preconditions.checkNotNull(searchCondition, "searchCondition is null!");

        SearchRequestBuilder builder = esClient.getTransportClient().prepareSearch();
        builder.setIndices((String[]) searchCondition.getIndexes().toArray())
                .setTypes((String[]) searchCondition.getTypes().toArray())
                .setScroll(new TimeValue(60000))
                .setSize(searchCondition.getSize())          //max of SIZE hits will be returned for each scroll
                .setExplain(searchCondition.getExplain());


        SearchResponse response = builder.get();
        String scrollId = response.getScrollId();
        logger.info("scrollId={}, took time={} ms", scrollId, response.getTookInMillis());
        //Scroll until no hits are returned
        long count = 0;
        do {
            count += response.getHits().getHits().length;

            response = esClient.getTransportClient()
                    .prepareSearchScroll(scrollId)
                    .setScroll(new TimeValue(60000))
                    .get();   //.execute().actionGet();

            scrollId = response.getScrollId();  //maybe multi index
            logger.info("scrollId={}, took time={} ms", scrollId, response.getTookInMillis());

        } while (response.getHits().getHits().length != 0);


        ClearScrollResponse clearScrollResponse = esClient.getTransportClient()
                .prepareClearScroll().get();
        logger.debug("clear scroll , id={}, isSucceeded={}", scrollId, clearScrollResponse.isSucceeded());
        logger.info("search count result : {}", count);
        return count;
    }

    @Override
    public List<SearchHit> searchUsingScrollAllHits(SearchCondition searchCondition) {
        Preconditions.checkNotNull(searchCondition, "searchCondition is null!");

        List<SearchHit> result = Lists.newArrayList();

        SearchRequestBuilder builder = esClient.getTransportClient().prepareSearch();
        builder.setIndices((String[]) searchCondition.getIndexes().toArray())
                .setTypes((String[]) searchCondition.getTypes().toArray())
                .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                .setScroll(new TimeValue(60000))
                .setQuery(QueryBuilders.termQuery("", ""))
                .setExplain(searchCondition.getExplain());
        SearchResponse response = builder.get();

        String scrollId = response.getScrollId();
        logger.info("scrollId={}, took time={} ms", scrollId, response.getTookInMillis());
        //Scroll until no hits are returned
        do {
            for (SearchHit hit : response.getHits().getHits()) {
                //Handle the hit...
                result.add(hit);
            }

            response = esClient.getTransportClient()
                    .prepareSearchScroll(scrollId)
                    .setScroll(new TimeValue(60000))
                    .get();   //.execute().actionGet();

            scrollId = response.getScrollId();
            logger.info("scrollId={}, took time={} ms", scrollId, response.getTookInMillis());

        } while (response.getHits().getHits().length != 0);

        ClearScrollResponse clearScrollResponse = esClient.getTransportClient()
                .prepareClearScroll().get();
        logger.info("clear scroll , id={}, isSucceeded={}", scrollId, clearScrollResponse.isSucceeded());
        return result;
    }

    @Override
    public MultiSearchResponse multiSearch(List<SearchRequestBuilder> searchRequestBuilders) {

        MultiSearchRequestBuilder multiSearchRequestBuilder = esClient.getTransportClient().prepareMultiSearch();
        if (CollectionUtils.isNotEmpty(searchRequestBuilders)) {
            searchRequestBuilders.forEach(searchRequestBuilder -> multiSearchRequestBuilder.add(searchRequestBuilder));
        }

        MultiSearchResponse response = multiSearchRequestBuilder.get();

        return response;
    }


    @Override
    public ExplainResponse explain(String index, String type, String id, QueryBuilder queryBuilder) {
        Preconditions.checkState(StringUtils.isNotBlank(index), "index is null!");
        Preconditions.checkState(StringUtils.isNotBlank(type), "type is null");
        Preconditions.checkState(StringUtils.isNotBlank(id), "id is null");

        ExplainResponse response = esClient.getTransportClient().prepareExplain(index, type, id)
                .setQuery(queryBuilder)
                .get();

        return response;
    }

    @Override
    public SearchResponse aggregation(SearchCondition searchCondition) {
        Preconditions.checkNotNull(searchCondition, "searchCondition is null!");
        SearchRequestBuilder builder = esClient.getTransportClient().prepareSearch();
        builder.setIndices((String[]) searchCondition.getIndexes().toArray())
                .setTypes((String[]) searchCondition.getTypes().toArray())
                .setQuery(searchCondition.getQueryBuilder())
                .setFrom(searchCondition.getFrom())
                .setSize(searchCondition.getSize())
                .setSearchType(searchCondition.getSearchType())
                .setExplain(searchCondition.getExplain());

        if (CollectionUtils.isNotEmpty(searchCondition.getSortBuilders())) {
            searchCondition.getSortBuilders().forEach(sortBuilder -> builder.addSort(sortBuilder));
        }

        if (CollectionUtils.isNotEmpty(searchCondition.getAggregationBuilders())) {
            searchCondition.getAggregationBuilders().forEach(aggregationBuilder -> builder.addAggregation(aggregationBuilder));
        }

        SearchResponse response = builder.get();

        return response;
    }
}
