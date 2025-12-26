package com.nutcracker.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分数DTO
 *
 * @author 胡桃夹子
 * @date 2025/12/26
 */
@Data
public class ScoreDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3974033349843582381L;

    private Long studentId;
    private String studentName;
    private Integer math;
    private Integer english;
    private Integer chinese;
    private Integer total;
    private Integer baseScore;
    private String gradingScore;

}
