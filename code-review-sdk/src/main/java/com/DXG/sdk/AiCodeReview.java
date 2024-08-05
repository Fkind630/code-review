package com.DXG.sdk;

import com.DXG.sdk.entity.CommitInfo;
import com.DXG.sdk.entity.request.AiRequest;
import com.DXG.sdk.entity.response.AiResponse;
import com.DXG.sdk.enums.ModelEnum;
import com.DXG.sdk.entity.request.Prompt;
import com.DXG.sdk.utils.BearerTokenUtil;
import com.alibaba.fastjson2.JSON;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

/**
 * @author: DXG
 * @description: TODO
 * @datetime: 2024/8/4 20:41
 **/

public class AiCodeReview {
    public static void main(String[] args) throws Exception {
        System.out.println("开始执行代码评审");

        //获取token
        String token = System.getenv("GITHUB_TOKEN");
        if (Objects.isNull(token) || " ".equals(token)){
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

        // 2. chatglm 代码评审
        AiResponse aiResponse = codeReview(diffCode.toString());
        System.out.println("code review：" + aiResponse.toString());

        try {
            writeLog(token,aiResponse.toString());
        }catch (Exception e){
            throw new RuntimeException("写入失败!",e);
        }

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
        list.add(new Prompt("user","你是一个顶级编程架构师，精通各类场景方案、架构设计和编程语言，请您根据git diff记录，对代码做出评审。代码为:" + code));
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
            String fileName = commitInfo.getCommitTime() + "-" + commitInfo.getAuthorName() + "-" + "代码审查结果.md";

            File file = new File(dir, fileName);
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(log);
            }

            git.add().addFilepattern(date + "/" + fileName).call();
            git.commit().setMessage("Add new file").call();
            git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, "")).call();

            return "https://github.com/Fkind630/code-review-log/blob/master/" + date + "/" + fileName;
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
                commitInfo.setCommitTime(new Date(latestCommit.getCommitTime() * 1000L));
            }
        } catch (Exception e) {
            System.err.println("Error accessing Git repository: " + e.getMessage());
            e.printStackTrace();
        }

        return commitInfo;
    }


}