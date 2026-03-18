package com.nutcracker.server.web.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.nutcracker.domain.dto.ScoreDTO;
import com.nutcracker.domain.dto.StudentDTO;
import com.nutcracker.server.properties.ServerAProperties;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * test api
 *
 * @author 胡桃夹子
 * @since 2025-12-19 16:41
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/student")
public class TestController {

    private final RestTemplate restTemplate;
    private final ServerAProperties serverAProperties;

    /**
     * 获得学生分数 <br/>
     * <a href="http://127.0.0.1:8080/sc-server-a/student/score">
     * <a href="http://127.0.0.1:8081/student/score">
     *
     * @param student 学生
     * @return {@link ScoreDTO }
     */
    @RequestMapping(value = "/score", method = RequestMethod.POST)
    public ScoreDTO getScoreByPost(@RequestBody StudentDTO student) {
        // 调用 sc-server-b 的接口（通过服务名，Spring Cloud LoadBalancer 会自动负载均衡）
        student.setGreetingMessage(serverAProperties.getGreetingMessage());
        log.info("接收到学生信息: {}", student);
        String url = "http://sc-server-b/score/calculate";
        ScoreDTO score = restTemplate.postForObject(url, student, ScoreDTO.class);

        log.info("返回分数: {}", score);
        return score;
    }

    /**
     * 获得学生分数（GET请求，使用查询参数）
     *
     * @param id   学生ID
     * @param name 学生姓名
     * @return {@link ScoreDTO }
     */
    @RequestMapping(value = "/score", method = RequestMethod.GET)
    public ScoreDTO getScoreByGet(@RequestParam(required = false) Long id, @RequestParam(required = false) String name) {
        // 创建学生对象
        StudentDTO student = new StudentDTO();
        student.setId(id);
        student.setName(name);
        student.setGreetingMessage(serverAProperties.getGreetingMessage());

        log.info("接收到学生信息(GET): {}", student);
        String url = "http://sc-server-b/score/calculate";
        ScoreDTO score = restTemplate.postForObject(url, student, ScoreDTO.class);

        log.info("返回分数: {}", score);
        return score;
    }
}