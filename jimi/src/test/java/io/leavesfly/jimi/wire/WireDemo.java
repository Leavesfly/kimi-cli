package io.leavesfly.jimi.wire;

import io.leavesfly.jimi.wire.message.*;
import org.junit.jupiter.api.*;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wire消息总线演示测试
 * 展示如何使用Wire进行Soul和UI之间的异步通信
 */
@DisplayName("Wire消息总线演示")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WireDemo {
    
    @Test
    @Order(1)
    @DisplayName("演示1：基础消息发送和接收")
    void demo1_basicSendAndReceive() throws Exception {
        System.out.println("\n=== 演示1：基础消息发送和接收 ===\n");
        
        Wire wire = new WireImpl();
        List<WireMessage> receivedMessages = new ArrayList<>();
        
        // 订阅消息流
        Disposable subscription = wire.asFlux()
                .subscribe(msg -> {
                    receivedMessages.add(msg);
                    System.out.println("✓ 接收到消息: " + msg.getMessageType());
                });
        
        // 发送几条消息
        System.out.println("\n发送消息：");
        wire.send(new StepBegin(1));
        System.out.println("  发送 StepBegin(1)");
        
        wire.send(new StepInterrupted());
        System.out.println("  发送 StepInterrupted");
        
        wire.send(new StepBegin(2));
        System.out.println("  发送 StepBegin(2)");
        
        // 等待异步处理
        Thread.sleep(100);
        
        System.out.println("\n接收结果：");
        System.out.println("  共接收到 " + receivedMessages.size() + " 条消息");
        
        Assertions.assertEquals(3, receivedMessages.size());
        Assertions.assertTrue(receivedMessages.get(0) instanceof StepBegin);
        Assertions.assertTrue(receivedMessages.get(1) instanceof StepInterrupted);
        
        subscription.dispose();
        System.out.println("\n✅ 演示1完成\n");
    }
    
    @Test
    @Order(2)
    @DisplayName("演示2：多订阅者（广播模式）")
    void demo2_multipleSubscribers() throws Exception {
        System.out.println("\n=== 演示2：多订阅者（广播模式）===\n");
        
        Wire wire = new WireImpl();
        List<WireMessage> subscriber1Messages = new ArrayList<>();
        List<WireMessage> subscriber2Messages = new ArrayList<>();
        List<WireMessage> subscriber3Messages = new ArrayList<>();
        
        // 第一个订阅者：收集所有消息
        Disposable sub1 = wire.asFlux()
                .subscribe(msg -> {
                    subscriber1Messages.add(msg);
                    System.out.println("  [订阅者1] 收到: " + msg.getMessageType());
                });
        
        // 第二个订阅者：只处理StepBegin
        Disposable sub2 = wire.asFlux()
                .filter(msg -> msg instanceof StepBegin)
                .subscribe(msg -> {
                    subscriber2Messages.add(msg);
                    System.out.println("  [订阅者2] 收到StepBegin: " + ((StepBegin) msg).getStepNumber());
                });
        
        // 第三个订阅者：计数器
        AtomicInteger counter = new AtomicInteger(0);
        Disposable sub3 = wire.asFlux()
                .subscribe(msg -> {
                    subscriber3Messages.add(msg);
                    int count = counter.incrementAndGet();
                    System.out.println("  [订阅者3] 消息计数: " + count);
                });
        
        System.out.println("\n发送消息：");
        wire.send(new StepBegin(1));
        wire.send(new CompactionBegin());
        wire.send(new CompactionEnd());
        wire.send(new StepBegin(2));
        wire.send(new StepInterrupted());
        
        Thread.sleep(100);
        
        System.out.println("\n接收统计：");
        System.out.println("  订阅者1（全部消息）: " + subscriber1Messages.size() + " 条");
        System.out.println("  订阅者2（仅StepBegin）: " + subscriber2Messages.size() + " 条");
        System.out.println("  订阅者3（计数器）: " + subscriber3Messages.size() + " 条");
        
        Assertions.assertEquals(5, subscriber1Messages.size());
        Assertions.assertEquals(2, subscriber2Messages.size());
        Assertions.assertEquals(5, counter.get());
        
        sub1.dispose();
        sub2.dispose();
        sub3.dispose();
        
        System.out.println("\n✅ 演示2完成\n");
    }
    
    @Test
    @Order(3)
    @DisplayName("演示3：模拟Agent运行流程")
    void demo3_simulateAgentFlow() throws Exception {
        System.out.println("\n=== 演示3：模拟Agent运行流程 ===\n");
        
        Wire wire = new WireImpl();
        CountDownLatch latch = new CountDownLatch(1);
        List<String> timeline = new ArrayList<>();
        
        // UI订阅者：监听并记录事件
        wire.asFlux()
                .doOnNext(msg -> {
                    String event = formatEvent(msg);
                    timeline.add(event);
                    System.out.println(event);
                })
                .doOnComplete(() -> {
                    System.out.println("\n[事件流完成]");
                    latch.countDown();
                })
                .subscribe();
        
        // 模拟Soul发送消息
        System.out.println("模拟Agent执行流程：\n");
        
        // Step 1
        wire.send(new StepBegin(1));
        Thread.sleep(50);
        wire.send(new StepInterrupted());
        Thread.sleep(50);
        
        // Step 2 with compaction
        wire.send(new StepBegin(2));
        Thread.sleep(50);
        wire.send(new CompactionBegin());
        Thread.sleep(30);
        wire.send(new CompactionEnd());
        Thread.sleep(50);
        wire.send(new StepInterrupted());
        Thread.sleep(50);
        
        // Status update
        Map<String, Object> status = new HashMap<>();
        status.put("messageCount", 15);
        status.put("tokenCount", 1234);
        wire.send(new StatusUpdate(status));
        
        // Complete
        wire.complete();
        
        // 等待完成
        boolean completed = latch.await(2, TimeUnit.SECONDS);
        
        System.out.println("\n执行时间线：");
        for (int i = 0; i < timeline.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + timeline.get(i));
        }
        
        Assertions.assertTrue(completed);
        Assertions.assertTrue(timeline.size() >= 7);
        
        System.out.println("\n✅ 演示3完成\n");
    }
    
    @Test
    @Order(4)
    @DisplayName("演示4：消息过滤和转换")
    void demo4_filterAndTransform() throws Exception {
        System.out.println("\n=== 演示4：消息过滤和转换 ===\n");
        
        Wire wire = new WireImpl();
        List<Integer> stepNumbers = new ArrayList<>();
        List<String> messageTypes = new ArrayList<>();
        
        // 订阅者1：提取所有StepBegin的步骤号
        wire.asFlux()
                .filter(msg -> msg instanceof StepBegin)
                .map(msg -> ((StepBegin) msg).getStepNumber())
                .subscribe(stepNumber -> {
                    stepNumbers.add(stepNumber);
                    System.out.println("  提取到步骤号: " + stepNumber);
                });
        
        // 订阅者2：转换为消息类型字符串
        wire.asFlux()
                .map(WireMessage::getMessageType)
                .subscribe(type -> {
                    messageTypes.add(type);
                    System.out.println("  消息类型: " + type);
                });
        
        System.out.println("\n发送混合消息：");
        for (int i = 1; i <= 5; i++) {
            wire.send(new StepBegin(i));
            if (i % 2 == 0) {
                wire.send(new CompactionBegin());
                wire.send(new CompactionEnd());
            }
            wire.send(new StepInterrupted());
        }
        
        Thread.sleep(100);
        
        System.out.println("\n过滤结果：");
        System.out.println("  提取的步骤号: " + stepNumbers);
        System.out.println("  所有消息类型: " + messageTypes);
        
        Assertions.assertEquals(5, stepNumbers.size());
        Assertions.assertTrue(messageTypes.size() >= 10);
        
        System.out.println("\n✅ 演示4完成\n");
    }
    
    @Test
    @Order(5)
    @DisplayName("演示5：背压处理")
    void demo5_backpressure() throws Exception {
        System.out.println("\n=== 演示5：背压处理 ===\n");
        
        Wire wire = new WireImpl();
        AtomicInteger processedCount = new AtomicInteger(0);
        
        // 慢速消费者（模拟UI处理）
        wire.asFlux()
                .delayElements(Duration.ofMillis(50))  // 模拟慢速处理
                .subscribe(msg -> {
                    int count = processedCount.incrementAndGet();
                    if (count % 10 == 0) {
                        System.out.println("  已处理 " + count + " 条消息");
                    }
                });
        
        // 快速生产者（模拟Soul发送）
        System.out.println("快速发送100条消息...");
        long startTime = System.currentTimeMillis();
        
        for (int i = 1; i <= 100; i++) {
            wire.send(new StepBegin(i));
        }
        
        long sendTime = System.currentTimeMillis() - startTime;
        System.out.println("✓ 发送完成，耗时: " + sendTime + "ms");
        
        // 等待处理
        System.out.println("\n等待消费者处理...");
        Thread.sleep(6000);  // 等待足够的时间处理所有消息
        
        System.out.println("\n处理统计：");
        System.out.println("  发送消息数: 100");
        System.out.println("  已处理数: " + processedCount.get());
        
        Assertions.assertTrue(processedCount.get() >= 90);  // 允许少量丢失
        
        System.out.println("\n✅ 演示5完成\n");
    }
    
    @Test
    @Order(6)
    @DisplayName("演示6：错误处理")
    void demo6_errorHandling() throws Exception {
        System.out.println("\n=== 演示6：错误处理 ===\n");
        
        Wire wire = new WireImpl();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        // 订阅者：模拟处理错误
        wire.asFlux()
                .doOnNext(msg -> {
                    if (msg instanceof StepBegin) {
                        int step = ((StepBegin) msg).getStepNumber();
                        if (step == 3) {
                            System.out.println("  [警告] 步骤3遇到问题，但继续处理");
                            errorCount.incrementAndGet();
                            // 注意：我们不抛出异常，只是记录
                        }
                    }
                    successCount.incrementAndGet();
                })
                .onErrorResume(e -> {
                    System.out.println("  [错误处理] 捕获异常: " + e.getMessage());
                    return Flux.empty();
                })
                .subscribe(
                    msg -> System.out.println("  处理: " + msg.getMessageType()),
                    error -> System.out.println("  错误: " + error.getMessage()),
                    () -> System.out.println("  完成")
                );
        
        System.out.println("发送消息（包含问题步骤）：");
        for (int i = 1; i <= 5; i++) {
            wire.send(new StepBegin(i));
        }
        
        Thread.sleep(100);
        
        System.out.println("\n处理结果：");
        System.out.println("  成功处理: " + successCount.get() + " 条");
        System.out.println("  遇到警告: " + errorCount.get() + " 次");
        
        Assertions.assertEquals(5, successCount.get());
        Assertions.assertEquals(1, errorCount.get());
        
        System.out.println("\n✅ 演示6完成\n");
    }
    
    @Test
    @Order(7)
    @DisplayName("演示7：消息分组处理")
    void demo7_messageGrouping() throws Exception {
        System.out.println("\n=== 演示7：消息分组处理 ===\n");
        
        Wire wire = new WireImpl();
        Map<String, Integer> typeCount = new HashMap<>();
        
        // 按消息类型分组统计
        wire.asFlux()
                .doOnNext(msg -> {
                    String type = msg.getMessageType();
                    typeCount.merge(type, 1, Integer::sum);
                })
                .buffer(Duration.ofMillis(200))  // 每200ms收集一批
                .subscribe(batch -> {
                    if (!batch.isEmpty()) {
                        System.out.println("  收到一批消息: " + batch.size() + " 条");
                    }
                });
        
        System.out.println("发送多种类型的消息：");
        
        // 模拟完整的执行周期
        for (int step = 1; step <= 3; step++) {
            wire.send(new StepBegin(step));
            wire.send(new CompactionBegin());
            wire.send(new CompactionEnd());
            wire.send(new StepInterrupted());
            Thread.sleep(100);
        }
        
        // 发送状态更新
        wire.send(new StatusUpdate(Map.of("status", "running")));
        
        Thread.sleep(300);
        
        System.out.println("\n消息类型统计：");
        typeCount.forEach((type, count) -> 
            System.out.println("  " + type + ": " + count + " 条")
        );
        
        Assertions.assertTrue(typeCount.size() >= 4);
        Assertions.assertEquals(3, typeCount.get("StepBegin"));
        
        System.out.println("\n✅ 演示7完成\n");
    }
    
    @Test
    @Order(8)
    @DisplayName("演示8：完整的UI监听场景")
    void demo8_completeUIScenario() throws Exception {
        System.out.println("\n=== 演示8：完整的UI监听场景 ===\n");
        
        Wire wire = new WireImpl();
        
        // UI状态
        class UIState {
            int currentStep = 0;
            boolean isCompacting = false;
            int messageCount = 0;
            int tokenCount = 0;
            List<String> events = new ArrayList<>();
        }
        
        UIState uiState = new UIState();
        
        // UI订阅Wire并更新状态
        wire.asFlux()
                .subscribe(msg -> {
                    if (msg instanceof StepBegin) {
                        uiState.currentStep = ((StepBegin) msg).getStepNumber();
                        uiState.events.add("进入步骤 " + uiState.currentStep);
                        System.out.println("  [UI] 步骤开始: " + uiState.currentStep);
                        
                    } else if (msg instanceof CompactionBegin) {
                        uiState.isCompacting = true;
                        uiState.events.add("开始上下文压缩");
                        System.out.println("  [UI] 压缩开始");
                        
                    } else if (msg instanceof CompactionEnd) {
                        uiState.isCompacting = false;
                        uiState.events.add("完成上下文压缩");
                        System.out.println("  [UI] 压缩完成");
                        
                    } else if (msg instanceof StatusUpdate) {
                        Map<String, Object> status = ((StatusUpdate) msg).getStatus();
                        uiState.messageCount = (int) status.getOrDefault("messageCount", 0);
                        uiState.tokenCount = (int) status.getOrDefault("tokenCount", 0);
                        uiState.events.add("状态更新");
                        System.out.println("  [UI] 状态更新: " + status);
                        
                    } else if (msg instanceof StepInterrupted) {
                        uiState.events.add("步骤 " + uiState.currentStep + " 完成");
                        System.out.println("  [UI] 步骤中断");
                    }
                });
        
        // 模拟Agent执行
        System.out.println("模拟Agent执行：\n");
        
        wire.send(new StepBegin(1));
        Thread.sleep(50);
        wire.send(new StepInterrupted());
        Thread.sleep(50);
        
        wire.send(new StepBegin(2));
        Thread.sleep(50);
        wire.send(new CompactionBegin());
        Thread.sleep(30);
        wire.send(new CompactionEnd());
        Thread.sleep(50);
        wire.send(new StepInterrupted());
        Thread.sleep(50);
        
        Map<String, Object> finalStatus = new HashMap<>();
        finalStatus.put("messageCount", 25);
        finalStatus.put("tokenCount", 3456);
        wire.send(new StatusUpdate(finalStatus));
        Thread.sleep(50);
        
        System.out.println("\nUI最终状态：");
        System.out.println("  当前步骤: " + uiState.currentStep);
        System.out.println("  是否压缩中: " + uiState.isCompacting);
        System.out.println("  消息数: " + uiState.messageCount);
        System.out.println("  Token数: " + uiState.tokenCount);
        System.out.println("  事件总数: " + uiState.events.size());
        
        System.out.println("\n事件时间线：");
        for (int i = 0; i < uiState.events.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + uiState.events.get(i));
        }
        
        Assertions.assertEquals(2, uiState.currentStep);
        Assertions.assertFalse(uiState.isCompacting);
        Assertions.assertEquals(25, uiState.messageCount);
        Assertions.assertTrue(uiState.events.size() >= 7);
        
        System.out.println("\n✅ 演示8完成\n");
    }
    
    /**
     * 格式化事件输出
     */
    private String formatEvent(WireMessage msg) {
        if (msg instanceof StepBegin) {
            return String.format("[步骤开始] 步骤 %d", ((StepBegin) msg).getStepNumber());
        } else if (msg instanceof StepInterrupted) {
            return "[步骤中断]";
        } else if (msg instanceof CompactionBegin) {
            return "[压缩开始]";
        } else if (msg instanceof CompactionEnd) {
            return "[压缩结束]";
        } else if (msg instanceof StatusUpdate) {
            return "[状态更新] " + ((StatusUpdate) msg).getStatus();
        }
        return "[未知消息] " + msg.getMessageType();
    }
}
