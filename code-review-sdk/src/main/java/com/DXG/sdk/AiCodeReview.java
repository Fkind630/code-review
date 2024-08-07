package com.DXG.sdk;

import com.DXG.sdk.entity.CommitInfo;
import com.DXG.sdk.entity.Message;
import com.DXG.sdk.entity.request.AiRequest;
import com.DXG.sdk.entity.response.AiResponse;
import com.DXG.sdk.enums.ModelEnum;
import com.DXG.sdk.entity.request.Prompt;
import com.DXG.sdk.utils.BearerTokenUtil;
import com.DXG.sdk.utils.TimeUtil;
import com.DXG.sdk.utils.WXAccessTokenUtil;
import com.alibaba.fastjson2.JSON;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: DXG
 * @description: TODO
 * @datetime: 2024/8/4 20:41
 **/

public class AiCodeReview {

    static String[] EXCLUDED_EXTENSIONS = {".yml", ".yaml", ".properties"};
    static String[] EXCLUDED_FILES = {"settings.gradle", "pom.xml"};
    static Pattern FILE_PATTERN = Pattern.compile("^diff --git a/(.+) b/(.+)$");

    public static void main(String[] args) throws Exception {
        System.out.println("开始执行代码评审");

        //获取token
        String token = System.getenv("GITHUB_TOKEN");
        if (Objects.isNull(token) || " ".equals(token)) {
            throw new RuntimeException("Token为空！请检查Token");
        }

        // 1. 获取更改的代码
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
        processBuilder.directory(new File("."));
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder diffCode = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            diffCode.append(line);
        }
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Git diff 命令执行失败，退出码: " + exitCode);
        }

        // 2. 过滤代码
        StringBuilder filteredDiff = new StringBuilder();
        String[] lines = diffCode.toString().split("\n");
        boolean includeFile = true;
        String currentFile = "";

        for (String l : lines) {
            Matcher matcher = FILE_PATTERN.matcher(l);
            if (matcher.find()) {
                currentFile = matcher.group(2);
                includeFile = true;
                for (String ext : EXCLUDED_EXTENSIONS) {
                    if (currentFile.endsWith(ext)) {
                        includeFile = false;
                        break;
                    }
                }
                if (includeFile) {
                    for (String file : EXCLUDED_FILES) {
                        if (currentFile.endsWith(file)) {
                            includeFile = false;
                            break;
                        }
                    }
                }
            }
            if (includeFile) {
                filteredDiff.append(line).append("\n");
            }
        }

        String filteredDiffCode = filteredDiff.toString();

        if (filteredDiffCode.trim().isEmpty()) {
            System.out.println("没有需要评审的代码变更");
            return;
        }


        // 2. chatglm 代码评审
        AiResponse aiResponse = codeReview(filteredDiffCode);
        System.out.println("code review：" + aiResponse.toString());

        String site = null;
        try {
            site = writeLog(token, aiResponse.toString());
        } catch (Exception e) {
            throw new RuntimeException("写入失败!", e);
        }

        pushMessageToWeiXin(site);

    }


    private static AiResponse codeReview(String code) throws IOException {
        String apiKeySecret = "03b8210062925b6ece762a596e50b38b.khzXJVdQOCm0soGO";
        String token = BearerTokenUtil.getToken(apiKeySecret);

        URL url = new URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);


        AiRequest aiRequest = new AiRequest();
        aiRequest.setModel(ModelEnum.GLM_4.getCode());
        ArrayList<Prompt> list = new ArrayList<>();
        list.add(new Prompt("user", "你是一个顶级编程架构师，精通各类场景方案、架构设计和编程语言，请您根据git diff记录，对代码做出评审。代码为:" + code));
        aiRequest.setMessages(list);


        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = JSON.toJSONString(aiRequest).getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }

        int responseCode = connection.getResponseCode();
        System.out.println(responseCode);

        //读取输出的结果
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        System.out.println(content);

        in.close();
        connection.disconnect();

        AiResponse aiResponse = JSON.parseObject(content.toString(), AiResponse.class);
        return aiResponse;

    }

    private static String writeLog(String token, String log) throws Exception {
        File repoDir = new File("temp_repo");
        Git git = null;
        try {
            git = Git.cloneRepository()
                    .setURI("https://github.com/Fkind630/code-review-log")
                    .setDirectory(repoDir)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, ""))
                    .call();

            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            File dir = new File(repoDir, date);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("Failed to create directory: " + dir);
            }

            CommitInfo commitInfo = getLatestCommitInfo(); // 确保这个方法在其他地方正确实现

            StringBuilder buildFileName = new StringBuilder();
            buildFileName.append("提交人:");
            buildFileName.append(commitInfo.getAuthorName());
            buildFileName.append("-提交时间:");
            buildFileName.append(commitInfo.getCommitTime());
            buildFileName.append("-代码审查结果.md");
            String fileName = buildFileName.toString();

            File file = new File(dir, fileName);
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(log);
            }

            git.add().addFilepattern(date + "/" + fileName).call();
            git.commit().setMessage("Add new file").call();
            git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, "")).call();

            return "https://github.com/Fkind630/code-review-log/tree/main/" + date + "/" + fileName;
        } finally {
            if (git != null) {
                git.close();
            }
        }
    }


    private static CommitInfo getLatestCommitInfo() {
        String currentDir = System.getProperty("user.dir");
        System.out.println("Current directory: " + currentDir);

        CommitInfo commitInfo = new CommitInfo();
        File gitDir = new File(currentDir, ".git");

        if (!gitDir.exists() || !gitDir.isDirectory()) {
            System.out.println("Not in a Git repository");
            return commitInfo;
        }

        try {
            Repository repository = new FileRepositoryBuilder()
                    .setGitDir(gitDir)
                    .build();

            try (Git git = new Git(repository)) {
                RevCommit latestCommit = git.log().setMaxCount(1).call().iterator().next();

                commitInfo.setAuthorName(latestCommit.getAuthorIdent().getName());
                commitInfo.setCommitTime(new TimeUtil().timeFormatHelper(latestCommit));
                commitInfo.setCommitMessage(latestCommit.getFullMessage());

                String projectName = getProjectName(git, repository);
                commitInfo.setProjectName(projectName);
            }
        } catch (Exception e) {
            System.err.println("Error accessing Git repository: " + e.getMessage());
            e.printStackTrace();
        }

        return commitInfo;
    }

    public static void pushMessageToWeiXin(String webSite){
        String access_Token = WXAccessTokenUtil.getAccessToken();

        CommitInfo commitInfo = getLatestCommitInfo();
        Message message = new Message();
        message.put("project",commitInfo.getProjectName());
        message.put("message",commitInfo.getCommitMessage());
        message.setUrl(webSite);
        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", access_Token);
        sendPostRequest(url, JSON.toJSONString(message));
    }

    private static void sendPostRequest(String urlString, String jsonBody) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
                String response = scanner.useDelimiter("\\A").next();
                System.out.println(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getProjectName(Git git, Repository repository) {
        String projectName = null;

        // 方法1：从远程仓库URL获取
        try {
            Config config = repository.getConfig();
            Set<String> remotes = config.getSubsections("remote");
            if (!remotes.isEmpty()) {
                String remoteName = remotes.iterator().next(); // 通常是 "origin"
                String remoteUrl = config.getString("remote", remoteName, "url");
                if (remoteUrl != null) {
                    projectName = extractProjectNameFromUrl(remoteUrl);
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting project name from remote URL: " + e.getMessage());
        }

        // 方法2：如果方法1失败，使用本地仓库的根目录名称
        if (projectName == null || projectName.isEmpty()) {
            File repoDir = repository.getWorkTree();
            projectName = repoDir.getName();
        }

        // 方法3：如果前两种方法都失败，尝试从 .git/description 文件读取
        if (projectName == null || projectName.isEmpty()) {
            File descriptionFile = new File(repository.getDirectory(), "description");
            if (descriptionFile.exists()) {
                try {
                    projectName = Files.readString(descriptionFile.toPath()).trim();
                } catch (IOException e) {
                    System.err.println("Error reading description file: " + e.getMessage());
                }
            }
        }

        // 如果所有方法都失败，使用默认值
        if (projectName == null || projectName.isEmpty()) {
            projectName = "UnknownProject";
        }

        return projectName;
    }

    private static String extractProjectNameFromUrl(String url) {
        // 移除 .git 后缀（如果有）
        url = url.replaceAll("\\.git$", "");
        // 获取最后一个 '/' 后的内容
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < url.length() - 1) {
            return url.substring(lastSlashIndex + 1);
        }
        return null;
    }


}