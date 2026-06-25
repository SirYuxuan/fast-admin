package cc.oofo.ai.agent.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 管理需要用户二次确认的工具调用。
 * 工具回调线程调用 waitForConfirmation 阻塞，前端收到 pending 事件后展示确认框，
 * 用户操作后调用 confirm 接口，respond 方法唤醒阻塞线程。
 */
@Slf4j
@Service
public class AiToolConfirmationService {

    private static final long TIMEOUT_SECONDS = 120;
    private final ConcurrentHashMap<String, CompletableFuture<Boolean>> pending = new ConcurrentHashMap<>();

    public boolean waitForConfirmation(String token) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        pending.put(token, future);
        try {
            return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.debug("Confirmation token {} expired or was interrupted", token);
            return false;
        } finally {
            pending.remove(token);
        }
    }

    public boolean respond(String token, boolean confirmed) {
        CompletableFuture<Boolean> future = pending.remove(token);
        if (future == null) {
            return false;
        }
        future.complete(confirmed);
        return true;
    }
}
