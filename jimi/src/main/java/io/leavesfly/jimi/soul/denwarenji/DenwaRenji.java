package io.leavesfly.jimi.soul.denwarenji;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * DenwaRenji - D-Mail 通信机制
 * 实现时间旅行功能，允许 Agent 向过去的检查点发送消息
 * 
 * 名称来源：《命运石之门》中的电话微波炉（电波Renji）
 */
@Slf4j
public class DenwaRenji {
    
    /**
     * 待处理的 D-Mail
     */
    private DMail pendingDMail;
    
    /**
     * 当前检查点数量
     */
    private int nCheckpoints;
    
    /**
     * 发送 D-Mail
     * 
     * @param checkpointId 目标检查点 ID
     * @param message D-Mail 消息内容
     * @return 是否成功发送
     */
    public boolean sendDMail(int checkpointId, String message) {
        // 验证检查点 ID
        if (checkpointId < 0) {
            log.warn("Invalid checkpoint ID: {}, must be >= 0", checkpointId);
            return false;
        }
        
        if (checkpointId >= nCheckpoints) {
            log.warn("Invalid checkpoint ID: {}, must be < {}", checkpointId, nCheckpoints);
            return false;
        }
        
        // 创建并存储 D-Mail
        pendingDMail = DMail.builder()
                           .checkpointId(checkpointId)
                           .message(message)
                           .build();
        
        log.info("D-Mail sent to checkpoint {}: {}", checkpointId, message.substring(0, Math.min(50, message.length())));
        return true;
    }
    
    /**
     * 获取并清除待处理的 D-Mail
     * 
     * @return 待处理的 D-Mail，如果没有则返回 empty
     */
    public Optional<DMail> fetchPendingDMail() {
        if (pendingDMail == null) {
            return Optional.empty();
        }
        
        DMail dmail = pendingDMail;
        pendingDMail = null;
        log.debug("Fetched pending D-Mail from checkpoint {}", dmail.getCheckpointId());
        return Optional.of(dmail);
    }
    
    /**
     * 设置当前检查点数量
     * 
     * @param count 检查点数量
     */
    public void setNCheckpoints(int count) {
        this.nCheckpoints = count;
    }
    
    /**
     * 获取当前检查点数量
     */
    public int getNCheckpoints() {
        return nCheckpoints;
    }
    
    /**
     * 清除待处理的 D-Mail
     */
    public void clearPendingDMail() {
        if (pendingDMail != null) {
            log.debug("Cleared pending D-Mail");
            pendingDMail = null;
        }
    }
}
