package com.nutcracker.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 学生DTO
 *
 * @author 胡桃夹子
 * @date 2025/12/26
 */
@Data
public class StudentDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 7340225826591380155L;

    private Long id;
    private String name;
    private String greetingMessage;

}
