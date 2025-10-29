package io.leavesfly.jimi.wire;

import io.leavesfly.jimi.wire.message.WireMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Wire 消息总线实现
 * 使用 Reactor Sinks 实现消息的异步传递
 */
public class WireImpl implements Wire {
    
    private final Sinks.Many<WireMessage> sink;
    
    public WireImpl() {
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
    }
    
    @Override
    public void send(WireMessage message) {
        sink.tryEmitNext(message);
    }
    
    @Override
    public Flux<WireMessage> asFlux() {
        return sink.asFlux();
    }
    
    @Override
    public void complete() {
        sink.tryEmitComplete();
    }
}
