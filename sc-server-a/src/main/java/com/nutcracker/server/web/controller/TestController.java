package com.nutcracker.server.web.controller;

import com.nutcracker.domain.dto.ScoreDTO;
import com.nutcracker.domain.dto.StudentDTO;
import com.nutcracker.server.properties.ServerAProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * XXX
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
     * 获得学生分数
     * <a href="http://10.39.1.6:8080/server-a/student/score">
     * <a href="http://10.39.1.6:8081/student/score">
     *
     * @param student 学生
     * @return {@link ScoreDTO }
     */
    @RequestMapping(value = "/score", method = {RequestMethod.POST, RequestMethod.GET})
    public ScoreDTO getScore(@RequestBody StudentDTO student) {
        // 调用 sc-server-b 的接口（通过服务名，Spring Cloud LoadBalancer 会自动负载均衡）
        student.setGreetingMessage(serverAProperties.getGreetingMessage());
        log.info("接收到学生信息: {}", student);
        String url = "http://sc-server-b/score/calculate";
        ScoreDTO score = restTemplate.postForObject(url, student, ScoreDTO.class);

        log.info("返回分数: {}", score);
        return score;
    }
}