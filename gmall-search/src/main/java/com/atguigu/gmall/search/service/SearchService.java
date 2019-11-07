package com.atguigu.gmall.search.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.search.vo.GoodsVO;
import com.atguigu.gmall.search.vo.SearchParamVO;
import com.atguigu.gmall.search.vo.SearchResponse;
import com.atguigu.gmall.search.vo.SearchResponseAttrVO;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.ChildrenAggregation;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ZQ
 * @create 2019-11-06 10:44
 */
@Service
public class SearchService {
    @Autowired
    private JestClient jestClient;

    public SearchResponse search(SearchParamVO searchParamVO) {
        //构建查询条件和过滤条件
        try {
            String dsl = buildDsl(searchParamVO);
            Search search = new Search.Builder(dsl).addIndex("goods").addType("info").build();
            SearchResult searchResult = jestClient.execute(search);

            SearchResponse response = parseResult(searchResult);
            response.setPageSize(searchParamVO.getPageSize());
            response.setPageNum(searchParamVO.getPageNum());
            response.setTotal(searchResult.getTotal());
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
    return null;
    }

    private SearchResponse parseResult(SearchResult result) {
        SearchResponse response = new SearchResponse();
        //获取所有聚合
        MetricAggregation aggregations = result.getAggregations();

        //解析品牌的聚合结果集
        //获取品牌聚合
        TermsAggregation brandAgg = aggregations.getTermsAggregation("brandAgg");
        //获取品牌聚合的所有捅
        List<TermsAggregation.Entry> buckets = brandAgg.getBuckets();
//        判断品牌聚合是否为空
        if (!CollectionUtils.isEmpty(buckets)) {
//            初始化品牌vo对象
            SearchResponseAttrVO attrVO = new SearchResponseAttrVO();
//            写死品牌聚合名称
            attrVO.setName("品牌");
            List<String> brandValues = buckets.stream().map(bucket -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", bucket.getKeyAsString());
                TermsAggregation brandNameAgg = bucket.getTermsAggregation("brandNameAgg");//获取品牌id捅集合的子聚合（品牌名称）
                map.put("name", brandNameAgg.getBuckets().get(0).getKeyAsString());
                return JSON.toJSONString(map);
            }).collect(Collectors.toList());
            attrVO.setValue(brandValues);//设置品牌的所有聚合值
            response.setBrand(attrVO);
        }
//        解析分类的聚合结果集
        TermsAggregation categroyAgg = aggregations.getTermsAggregation("categroyAgg");
        List<TermsAggregation.Entry> categroyAggBuckets = categroyAgg.getBuckets();
        if (!CollectionUtils.isEmpty(categroyAggBuckets)) {
            SearchResponseAttrVO categoryVO = new SearchResponseAttrVO();
            categoryVO.setName("分类");
            List<String> categoryValues = categroyAggBuckets.stream().map(bucket -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", bucket.getKeyAsString());
                        TermsAggregation categoryNameAgg = bucket.getTermsAggregation("CategoryNameAgg");
                        map.put("name", categoryNameAgg.getBuckets().get(0).getKeyAsString());
                        return JSON.toJSONString(map);

                    }
            ).collect(Collectors.toList());
            response.setCatelog(categoryVO);
        }

//        即系搜索属性的聚合结果集
        ChildrenAggregation attrAgg = aggregations.getChildrenAggregation("attrAgg");
        TermsAggregation attrIdAgg = attrAgg.getTermsAggregation("attrIdAgg");
        List<TermsAggregation.Entry> attrBuckets = attrIdAgg.getBuckets();
        List<SearchResponseAttrVO> attrVOS = attrBuckets.stream().map(attrBucket -> {
            SearchResponseAttrVO attrVo = new SearchResponseAttrVO();
            attrVo.setProductAttributeId(Long.valueOf(attrBucket.getKeyAsString()));
//            获取搜索属性的子聚合（搜索属性名）
            TermsAggregation attrName = attrBucket.getTermsAggregation("attrName");
            attrVo.setName(attrName.getBuckets().get(0).getKeyAsString());
//            获取搜索属性的子聚合（搜索属性值）
            TermsAggregation attrValue = attrBucket.getTermsAggregation("attrValue");
            List<String> values = attrValue.getBuckets().stream().map(attrNamebucket -> {
                return attrNamebucket.getKeyAsString();
            }).collect(Collectors.toList());
            attrVo.setValue(values);
            return attrVo;
        }).collect(Collectors.toList());
        response.setAttrs(attrVOS);
//       解析商品列表对的结果集
        List<GoodsVO> goodsVOS = result.getSourceAsObjectList(GoodsVO.class, false);


        response.setProducts(goodsVOS);


        return response;
    }

    private String buildDsl(SearchParamVO searchParamVO) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //1.完成构建和查询和过滤
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //构建查询条件
        String keyword = searchParamVO.getKeyword();
        if (!StringUtils.isEmpty(keyword)) {
            boolQuery.must(
                    QueryBuilders.matchQuery("name", keyword).operator(Operator.AND)
            );
        }
//        构建过滤条件
//        品牌
        String[] brand = searchParamVO.getBrand();
        if (ArrayUtils.isNotEmpty(brand)) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", brand));
        }
//        分类
        String[] catelog3 = searchParamVO.getCatelog3();
        if (ArrayUtils.isNotEmpty(catelog3)) {
            boolQuery.filter(QueryBuilders.termsQuery("productCategoryId", catelog3));
        }
//        搜索的规格属性过滤
        String[] props = searchParamVO.getProps();
        if (ArrayUtils.isNotEmpty(props)) {
            for (String prop : props) {
                String[] attr = StringUtils.split(prop, ":");
                if (attr != null && attr.length == 2) {
                    BoolQueryBuilder propBoolQuery = QueryBuilders.boolQuery();
                    propBoolQuery.must(QueryBuilders.termQuery("attrValueList.productAttributeId", attr[0]));
                    System.out.println(attr[1]);
                    String[] values = StringUtils.split(attr[1], "-");
                    System.out.println(values.toString());
                    propBoolQuery.must(QueryBuilders.termsQuery("attrValueList.value", values));
                    boolQuery.filter(QueryBuilders.nestedQuery("attrValueList", propBoolQuery, ScoreMode.None));
                }
            }
        }

        searchSourceBuilder.query(boolQuery);
//        2.完成分页的构建
        Integer pageNum = searchParamVO.getPageNum();
        Integer pageSize = searchParamVO.getPageSize();

        searchSourceBuilder.from((pageNum - 1) * pageSize);
        searchSourceBuilder.size(pageSize);
//        3.完成排序的构建
        String order = searchParamVO.getOrder();
        if (StringUtils.isNotEmpty(order)) {
            String[] orders = StringUtils.split(order, ":");
            if (orders != null && orders.length == 2) {
                SortOrder sortOrder = StringUtils.equals("asc", orders[1]) ? SortOrder.ASC : SortOrder.DESC;
                switch (orders[0]) {
                    case "0":
                        searchSourceBuilder.sort("_score", sortOrder);
                        break;
                    case "1":
                        searchSourceBuilder.sort("sale", sortOrder);
                        break;
                    case "2":
                        searchSourceBuilder.sort("price", sortOrder);
                        break;
                    default:
                        break;
                }
            }
        }
//        4.完成高亮的构建
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name");
        highlightBuilder.preTags("<font color='red'>");
        highlightBuilder.postTags("</font>");
        searchSourceBuilder.highlighter(highlightBuilder);
//        5完成聚合条件的构架
//         品牌的聚合
        searchSourceBuilder.aggregation(AggregationBuilders.terms("brandAgg").field("brandId").subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName")));


//        分类的聚合
        searchSourceBuilder.aggregation(AggregationBuilders.terms("categroyAgg").field("productCategoryId")
                .subAggregation(AggregationBuilders.terms("CategoryNameAgg").field("productCategoryName")));
//        属性的聚合
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "attrValueList")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrValueList.productAttributeId")
                        .subAggregation(AggregationBuilders.terms("attrName").field("attrValueList.name")).subAggregation(AggregationBuilders.terms("attrValue").field("attrValueList.value"))));
        System.out.println(searchSourceBuilder.toString());
        return searchSourceBuilder.toString();
    }
}
