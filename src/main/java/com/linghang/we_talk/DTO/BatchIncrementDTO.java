package com.linghang.we_talk.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BatchIncrementDTO {
    private Long articleId;
    private Long increateCount;

    public BatchIncrementDTO(Long articleId,Long increateCount){
        this.articleId=articleId;
        this.increateCount=increateCount;
    }
}
