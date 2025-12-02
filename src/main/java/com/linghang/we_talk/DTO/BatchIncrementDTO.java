package com.linghang.we_talk.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BatchIncrementDTO {
    private Long articleId;
    private Integer increateCount;

    public BatchIncrementDTO(Long articleId,Integer increateCount){
        this.articleId=articleId;
        this.increateCount=increateCount;
    }
}
