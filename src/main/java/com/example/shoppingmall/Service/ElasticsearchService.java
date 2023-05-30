package com.example.shoppingmall.Service;

import java.util.ArrayList;
import java.util.List;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.ValueCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import com.example.shoppingmall.Domain.Goods;
import com.example.shoppingmall.Domain.SearchDto;

@Service
public class ElasticsearchService {
    private final ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public ElasticsearchService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public List<Goods> getDataFromElasticsearch(SearchDto params) {
    	// BoolQueryBuilder 생성
    	BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

    	if(params.getSearchValue() != null && !params.getSearchValue().equals("")) {
	    	// MatchQueryBuilder를 사용하여 should 절에 조건 추가
	    	MatchQueryBuilder matchQuery1 = QueryBuilders.matchQuery("name", "틴캐시 5만");
	    	MatchQueryBuilder matchQuery2 = QueryBuilders.matchQuery("name.nori", "틴캐시 5만");
	    	MatchQueryBuilder matchQuery3 = QueryBuilders.matchQuery("name.ngram", "틴캐시 5만");
	
	    	boolQuery.should(matchQuery1);
	    	boolQuery.should(matchQuery2);
	    	boolQuery.should(matchQuery3);
    	}
    	boolQuery.filter(QueryBuilders.termQuery("is_deleted", 0));
    	if(params.getSearchMinPrice() != null && params.getSearchMinPrice() > 0) {
    		boolQuery.filter(QueryBuilders.rangeQuery("price").gt(params.getSearchMinPrice()));
    	}
    	if(params.getSearchMaxPrice() != null && params.getSearchMaxPrice() > 0) {
    		boolQuery.filter(QueryBuilders.rangeQuery("price").lt(params.getSearchMaxPrice()));
    	}

    	// NativeSearchQuery를 사용하여 쿼리 실행
    	NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder()
    	        .withQuery(boolQuery)
    	        .withPageable(PageRequest.of(params.getPagination().getLimitStart(), params.getRecordSize()));
    	
    	// ORDER BY 절 추가
    	if (params.getOrderBy() == null || params.getOrderBy().equals("")) {
    		searchQuery.withSort(Sort.by(Sort.Direction.DESC, "insertion_time"));
    	} else if (params.getOrderBy().equals("priceASC")) {
    		searchQuery.withSort(Sort.by(Sort.Direction.ASC, "price"));
    	} else if (params.getOrderBy().equals("priceDESC")) {
    		searchQuery.withSort(Sort.by(Sort.Direction.DESC, "price"));
    	} else if (params.getOrderBy().equals("dateASC")) {
    		searchQuery.withSort(Sort.by(Sort.Direction.ASC, "insertion_time"));
    	} else if (params.getOrderBy().equals("dateDESC")) {
    		searchQuery.withSort(Sort.by(Sort.Direction.DESC, "insertion_time"));
    	}

    	// NativeSearchQuery를 사용하여 쿼리 생성
    	NativeSearchQuery searchQueryComplete = searchQuery.build();
    	
        SearchHits<Goods> searchHits = elasticsearchOperations.search(searchQueryComplete, Goods.class);
        List<Goods> dataList = new ArrayList<>();
        for (SearchHit<Goods> searchHit : searchHits) {
            dataList.add(searchHit.getContent());
        }
        return dataList;
    }
    
    public int count(SearchDto params) {
    	// BoolQueryBuilder 생성
    	BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

    	if(params.getSearchValue() != null && !params.getSearchValue().equals("")) {
	    	// MatchQueryBuilder를 사용하여 should 절에 조건 추가
	    	MatchQueryBuilder matchQuery1 = QueryBuilders.matchQuery("name", "틴캐시 5만");
	    	MatchQueryBuilder matchQuery2 = QueryBuilders.matchQuery("name.nori", "틴캐시 5만");
	    	MatchQueryBuilder matchQuery3 = QueryBuilders.matchQuery("name.ngram", "틴캐시 5만");
	
	    	boolQuery.should(matchQuery1);
	    	boolQuery.should(matchQuery2);
	    	boolQuery.should(matchQuery3);
    	}
    	boolQuery.filter(QueryBuilders.termQuery("is_deleted", 0));
    	if(params.getSearchMinPrice() != null && params.getSearchMinPrice() > 0) {
    		boolQuery.filter(QueryBuilders.rangeQuery("price").gt(params.getSearchMinPrice()));
    	}
    	if(params.getSearchMaxPrice() != null && params.getSearchMaxPrice() > 0) {
    		boolQuery.filter(QueryBuilders.rangeQuery("price").lt(params.getSearchMaxPrice()));
    	}
    	
    	// NativeSearchQueryBuilder를 사용하여 쿼리 생성
    	NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder()
    	        .withQuery(boolQuery);

    	// Aggregation을 사용하여 COUNT 집계 생성
    	searchQuery.addAggregation(AggregationBuilders.count("count").field("id"));

    	// NativeSearchQuery를 사용하여 쿼리 생성
    	NativeSearchQuery searchQueryComplete = searchQuery.build();

    	// 쿼리 실행
    	SearchHits<Goods> searchHits = elasticsearchOperations.search(searchQueryComplete, Goods.class);

    	// COUNT 결과 가져오기
    	Aggregations aggregations = (Aggregations) searchHits.getAggregations();
    	ValueCount countAggregation = aggregations.get("count");
    	return (int) countAggregation.getValue();
    }
}