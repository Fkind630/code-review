package com.DXG.sdk.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author: DXG
 * @description: TODO
 * @datetime: 2024/8/5 21:44
 **/
@Data
public class CommitInfo {
    private String authorName;
    private String commitTime;
    private String commitMessage;
    private String projectName;
}
