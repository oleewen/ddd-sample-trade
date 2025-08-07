package com.transformer.es.service.model;

import lombok.Data;
import org.elasticsearch.search.Scroll;

import java.io.Serializable;
import java.util.List;


@Data
public class DeepResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String scrollId;

    private Scroll scroll;

    private List<T> dataList;

    private boolean isLastPage;
}
