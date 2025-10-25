-- ================================================================
-- KawaiiChain Wallet - 通知服务数据库 (kawaii_notification_db)
-- 负责：消息通知、推送管理
--
-- 【时间字段约定】
-- 所有时间字段使用 TIMESTAMP 类型（不带时区），统一存储 UTC 时间
-- 应用层负责时区转换，确保写入数据库的时间都是 UTC
-- ================================================================


-- ================================================================
-- 1. 通知表 (notifications)
-- ================================================================
CREATE TABLE notifications (
    notification_id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,

    -- 通知内容
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL, -- transaction, system, security, promotion

    -- 通知渠道
    channels TEXT[] DEFAULT ARRAY['in_app'], -- in_app, email, sms, push

    -- 关联信息
    related_id BIGINT, -- 关联的交易ID、订单ID等（跨服务引用）
    related_type VARCHAR(50), -- transaction, payment_order, bill_payment

    -- 状态管理
    is_read BOOLEAN DEFAULT FALSE,
    is_sent BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP,

    -- 元数据
    metadata JSONB,

    created_at TIMESTAMP DEFAULT (NOW() AT TIME ZONE 'UTC'),
    updated_at TIMESTAMP DEFAULT (NOW() AT TIME ZONE 'UTC')
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(is_read);
CREATE INDEX idx_notifications_type ON notifications(notification_type);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

-- ================================================================
-- 自动更新时间戳触发器
-- ================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = (NOW() AT TIME ZONE 'UTC');
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_notifications_updated_at BEFORE UPDATE ON notifications
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ================================================================
-- 数据库初始化完成
-- ================================================================