package com.DXG.sdk.utils;

import org.eclipse.jgit.revwalk.RevCommit;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author: DXG
 * @description: TODO
 * @datetime: 2024/8/5 22:21
 **/
public class TimeUtil {
    /**
     * 用于转换Git的时间成yyyy-MM-dd-HH-mm-ss的格式
     * @param latestCommit
     * @return
     */
    public String timeFormatHelper( RevCommit latestCommit){
        int commitTime = latestCommit.getCommitTime();
        Instant instant = Instant.ofEpochSecond(commitTime);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }
}
