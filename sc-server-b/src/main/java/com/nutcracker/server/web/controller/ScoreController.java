package com.nutcracker.server.web.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nutcracker.domain.dto.ScoreDTO;
import com.nutcracker.domain.dto.StudentDTO;
import com.nutcracker.server.properties.ServerBProperties;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * test api
 *
 * @author 胡桃夹子
 * @since 2025-12-19 16:38
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/score")
public class ScoreController {

    private final ServerBProperties serverBProperties;

    /**
     * 获得学生分数（POST请求） <a href="http://10.39.1.6:8080/server-b/score/calculate">
     * <a href="http://10.39.1.6:8082/score/calculate">
     *
     * @param student 学生
     * @return {@link ScoreDTO }
     */
    @RequestMapping(value = "/calculate", method = RequestMethod.POST)
    public ScoreDTO calculateByPost(@RequestBody StudentDTO student) {
        log.info("POST请求接收到学生信息: {}", student);
        return calculateScore(student);
    }

    /**
     * 获得学生分数（GET请求，使用查询参数）
     *
     * @param id   学生ID
     * @param name 学生姓名
     * @return {@link ScoreDTO }
     */
    @RequestMapping(value = "/calculate", method = RequestMethod.GET)
    public ScoreDTO calculateByGet(@RequestParam(required = false) Long id, @RequestParam(required = false) String name) {
        // 创建学生对象
        StudentDTO student = new StudentDTO();
        student.setId(id);
        student.setName(name);

        log.info("GET请求接收到学生信息: {}", student);
        return calculateScore(student);
    }

    /**
     * 计算学生分数
     *
     * @param student 学生
     * @return {@link ScoreDTO }
     */
    private ScoreDTO calculateScore(StudentDTO student) {
        ScoreDTO score = new ScoreDTO();
        score.setStudentId(student.getId());
        score.setStudentName(student.getName());

        // 模拟成绩计算
        score.setMath(90);
        score.setEnglish(88);
        score.setChinese(92);
        score.setTotal(90 + 88 + 92);
        score.setBaseScore(serverBProperties.getBaseScore());
        score.setGradingScore(serverBProperties.getGradingScale());
        log.info("计算完成: {},{}", student, score);
        return score;
    }
}
