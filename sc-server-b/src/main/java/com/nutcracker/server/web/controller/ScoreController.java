package com.nutcracker.server.web.controller;

import com.nutcracker.domain.dto.ScoreDTO;
import com.nutcracker.domain.dto.StudentDTO;
import com.nutcracker.server.properties.ServerBProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * XXX
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
     * 获得学生分数
     * <a href="http://10.39.1.6:8080/server-b/score/calculate">
     * <a href="http://10.39.1.6:8082/score/calculate">
     *
     * @param student 学生
     * @return {@link ScoreDTO }
     */
    @RequestMapping(value = "/calculate", method = {RequestMethod.POST, RequestMethod.GET})
    public ScoreDTO calculate(@RequestBody StudentDTO student) {
        log.info("{}", student);
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
        log.info("{},{}", student, score);
        return score;
    }
}
