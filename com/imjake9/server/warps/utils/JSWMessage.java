package com.imjake9.server.warps.utils;

import com.imjake9.server.lib.EconomyManager;
import com.imjake9.server.lib.MessageTemplate;
import com.imjake9.server.lib.Messaging;
import com.imjake9.server.lib.Messaging.MessageLevel;

public enum JSWMessage implements MessageTemplate {

    CANNOT_UNLOCK(MessageLevel.ERROR, "You cannot unlock any more warps."),
    HOME_POINT_NOT_SET(MessageLevel.ERROR, "Home point not set."),
    NOT_ENOUGH_MONEY(MessageLevel.ERROR, "You do not have enough money to unlock a new warp slot."),
    NO_WARP_NAMED(MessageLevel.ERROR, "No warp named <i>%i</i>"),
    OUT_OF_WARPS(MessageLevel.ERROR, "You are out of warps. Delete one to make room for another.") {
        
        @Override
        public String getMessage() {
            return Messaging.parseStyling(level.getOpeningTag() + (!EconomyManager.isUsingEconomy() ? "You are out of warps. Delete one to make room for another." : "You are out of warps. Delete one or purchase a new warp slot.") + level.getClosingTag());
        }
        
    },
    PRICE_TO_UNLOCK(MessageLevel.NORMAL, "Price to unlock warp: %1"),
    PURCHASED_SLOT(MessageLevel.SUCCESS, "New warp slot purchased."),
    SET_HOME_POINT(MessageLevel.SUCCESS, "Home point set."),
    WARP_CREATED(MessageLevel.SUCCESS, "Warp <i>%1</i> created successfully."),
    WARP_MOVED(MessageLevel.SUCCESS, "Warp <i>%1</i> moved."),
    WARP_REMOVED(MessageLevel.SUCCESS, "Warp <i>%1</i> removed."),
    WARPED_TO(MessageLevel.NORMAL, "Warped to %1."),
    WARPS_RATIO(MessageLevel.NORMAL, "Warps: %1/%2") {
        
        @Override
        public String getMessage() {
            return Messaging.parseStyling(level.getOpeningTag() + (!EconomyManager.isUsingEconomy() ? "Warps: %1/%2" : "Warps: %1/%2/%3") + level.getClosingTag());
        }
        
    };
    
    protected MessageLevel level;
    protected String format;

    JSWMessage(MessageLevel level, String format) {
        this.level = level;
        this.format = Messaging.parseStyling(level.getOpeningTag() + format + level.getClosingTag());
    }

    @Override
    public MessageLevel getLevel() {
        return level;
    }

    @Override
    public String getMessage() {
        return format;
    }
}
