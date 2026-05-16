package cc.oofo.system.job.example;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 示例任务 Bean：用于测试定时任务。
 *
 * 使用方式：定时任务管理 → 新增任务，Bean 名填 "demoJob"，方法 "execute" 或 "sayHi"。
 *
 * @author Sir丶雨轩
 * @since 2025/05/16
 */
@Slf4j
@Component("demoJob")
public class DemoJob {

    /** 默认无参方法 */
    public void execute() {
        log.info("【DemoJob】定时任务执行中... time={}", System.currentTimeMillis());
    }

    /** 带 String 参数的方法 */
    public void sayHi(String params) {
        log.info("【DemoJob.sayHi】params={}", params);
    }

    /** 模拟失败 */
    public void failJob() {
        log.info("【DemoJob.failJob】即将抛异常...");
        throw new RuntimeException("故意失败的任务");
    }
}
