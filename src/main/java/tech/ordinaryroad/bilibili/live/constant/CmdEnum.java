package tech.ordinaryroad.bilibili.live.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author mjz
 * @date 2023/1/6
 */
@Getter
@RequiredArgsConstructor
public enum CmdEnum {
    /**
     * 收到弹幕
     */
    DANMU_MSG,
    /**
     * 收到礼物
     */
    SEND_GIFT,
    /**
     * 有人上舰
     */
    GUARD_BUY,
    /**
     * 欢迎舰长
     */
    WELCOME_GUARD,
    WELCOME,
    COMBO_SEND,
    /**
     * 欢迎高能用户、(舰长?待验证)特殊消息
     */
    ENTRY_EFFECT,
    HOT_RANK_CHANGED,
    HOT_RANK_CHANGED_V2,
    INTERACT_WORD,
    /**
     * 开始直播
     */
    LIVE,
    LIVE_INTERACTIVE_GAME,
    NOTICE_MSG,
    ONLINE_RANK_COUNT,
    ONLINE_RANK_TOP3,
    ONLINE_RANK_V2,
    PK_BATTLE_END,
    PK_BATTLE_FINAL_PROCESS,
    PK_BATTLE_PROCESS,
    PK_BATTLE_PROCESS_NEW,
    PK_BATTLE_SETTLE,
    PK_BATTLE_SETTLE_USER,
    PK_BATTLE_SETTLE_V2,
    /**
     * 结束直播
     */
    PREPARING,
    ROOM_REAL_TIME_MESSAGE_UPDATE,
    STOP_LIVE_ROOM_LIST,
    /**
     * 醒目留言
     */
    SUPER_CHAT_MESSAGE,
    SUPER_CHAT_MESSAGE_JPN,
    /**
     * 删除醒目留言
     */
    SUPER_CHAT_MESSAGE_DELETE,
    WIDGET_BANNER,
    LIKE_INFO_V3_UPDATE,
    LIKE_INFO_V3_CLICK,
    HOT_ROOM_NOTIFY,
    WATCHED_CHANGE,
    ;
}
