package com.tradingsim.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradingsim.application.dto.CreateSessionRequest;
import com.tradingsim.application.dto.GameSessionResponse;
import com.tradingsim.application.dto.GameDecisionRequest;
import com.tradingsim.application.dto.GameDecisionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class GameIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCompleteGameFlow() throws Exception {
        // 1. 创建游戏会话
        CreateSessionRequest createRequest = new CreateSessionRequest();
        createRequest.setStockCode("AAPL");
        createRequest.setTimeframe("1D");
        createRequest.setInitialBalance(new BigDecimal("10000.00"));

        MvcResult createResult = mockMvc.perform(post("/api/game/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn();

        GameSessionResponse session = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                GameSessionResponse.class
        );

        assertNotNull(session.getSessionId());
        assertEquals("AAPL", session.getStockCode());

        String sessionId = session.getSessionId();

        // 2. 获取会话详情
        mockMvc.perform(get("/api/game/sessions/" + sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.stockCode").value("AAPL"));

        // 3. 提交决策
        GameDecisionRequest decisionRequest = new GameDecisionRequest();
        decisionRequest.setSessionId(sessionId);
        decisionRequest.setDecisionType("BUY");
        decisionRequest.setPrice(BigDecimal.valueOf(150.00));
        decisionRequest.setQuantity(100);
        decisionRequest.setResponseTimeMs(1500L);

        MvcResult decisionResult = mockMvc.perform(post("/api/game/sessions/" + sessionId + "/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(decisionRequest)))
                .andExpect(status().isOk())
                .andReturn();

        GameDecisionResponse decision = objectMapper.readValue(
                decisionResult.getResponse().getContentAsString(),
                GameDecisionResponse.class
        );

        assertNotNull(decision.getDecisionId());
        assertEquals(sessionId, decision.getSessionId());
        assertEquals("BUY", decision.getDecisionType());

        // 4. 获取会话决策列表
        mockMvc.perform(get("/api/game/sessions/" + sessionId + "/decisions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].decisionId").value(decision.getDecisionId()));

        // 5. 完成会话
        mockMvc.perform(post("/api/game/sessions/" + sessionId + "/finish"))
                .andExpect(status().isOk());

        // 6. 验证会话状态已更新
        mockMvc.perform(get("/api/game/sessions/" + sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // 7. 获取排行榜
        mockMvc.perform(get("/api/game/leaderboard")
                .param("stockCode", "AAPL")
                .param("difficulty", "EASY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 8. 获取用户游戏历史
        mockMvc.perform(get("/api/game/users/test-user-1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testInvalidSessionId() throws Exception {
        // 测试无效的会话ID
        mockMvc.perform(get("/api/game/sessions/invalid-session-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testInvalidDecisionRequest() throws Exception {
        // 创建有效会话
        CreateSessionRequest createRequest = new CreateSessionRequest();
        createRequest.setStockCode("GOOGL");
        createRequest.setTimeframe("1H");
        createRequest.setInitialBalance(new BigDecimal("5000.00"));

        MvcResult createResult = mockMvc.perform(post("/api/game/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn();

        GameSessionResponse session = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                GameSessionResponse.class
        );

        // 测试无效决策请求（缺少必要字段）
        GameDecisionRequest invalidRequest = new GameDecisionRequest();
        invalidRequest.setSessionId(session.getSessionId());
        // 缺少决策类型

        mockMvc.perform(post("/api/game/sessions/" + session.getSessionId() + "/decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLeaderboardFiltering() throws Exception {
        // 测试排行榜过滤功能
        mockMvc.perform(get("/api/game/leaderboard")
                .param("stockCode", "TSLA")
                .param("difficulty", "HARD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 测试无效参数
        mockMvc.perform(get("/api/game/leaderboard")
                .param("stockCode", "")
                .param("difficulty", "INVALID"))
                .andExpect(status().isBadRequest());
    }
}