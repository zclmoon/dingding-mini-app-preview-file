package com.codercms.DingDingMiniAppViewFileDemo.Controllers;

import com.alibaba.fastjson.JSON;
import com.codercms.DingDingMiniAppViewFileDemo.Models.DingPanFileInfo;
import com.codercms.DingDingMiniAppViewFileDemo.Models.DingPanSpaceInfo;
import com.codercms.DingDingMiniAppViewFileDemo.Models.DingPanSpaceRequest;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.*;
import com.dingtalk.api.response.*;
import com.taobao.api.FileItem;
import com.taobao.api.internal.util.WebUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * https://blog.csdn.net/Poison_AS/article/details/115322142
 * 1. 获取access token
 * 2. 获取space id
 * 3. 单步上传文件，返回media_id
 * 4. 获取自定义空间访问授权
 * 5. 转存到自定义空间并获取file id
 */
@RestController
public class TestController {

    @Value("${appsecret}")
    public String appsecret;

    @Value("${appkey}")
    public String appkey;

    @Value("${agentId}")
    public String agentId;

    @Value("${domain}")
    public String domain;

    @Value("${authSpaceType}")
    public String authSpaceType;

    @Value("${userId}")
    public String userId;

    // authCode：小程端获取: https://developers.dingtalk.com/document/app/mini-program-free-login?spm=ding_open_doc.document.0.0.3a2565739h7596#topic-2024721
    @PostMapping("/upload-file/{authCode}")
    public DingPanFileInfo uploadFile(@RequestParam("file") MultipartFile file, String authCode) throws IOException {

        System.out.println("auth code: " +authCode);

        DingPanFileInfo result = new DingPanFileInfo();

        // 获取上传的文件信息
        String fileName = file.getOriginalFilename();
        result.fileName = fileName;
        result.fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        result.fileSize = file.getSize();

        // 获取access token
        String accessToken = getAccessToken();

        // 获取space id
        result.spaceId = getDingPanSpaceId(accessToken);

        // 上传文件并获得media id
         result.mediaId = uploadFileAndGetMediaId(file, accessToken);

        // 授权用户访问企业的自定义空间
        // 注意： 这个方法里 不能 使用authCode获取userId；因为前段传过来的authCode只能用一次，如果用了，接下来的转存API就没法调用了。
        //  解决办法是：把userId配置在配置文件中，因为userId是不变的。
        authUserAccessCustomizeSpace(accessToken);

        // 转存上传的文件到自定义空间，并获取fileId
        result.fileId = saveToCSAndGetFileId(authCode, result.mediaId, result.spaceId, fileName, accessToken);

        return result;
    }

    @PostMapping("/GetAndAuthSpace")
    public DingPanSpaceInfo GetAndAuthCusDingSpace(@RequestBody DingPanSpaceRequest request) {
        DingPanSpaceInfo result = new DingPanSpaceInfo();

        // 获取access token
        String accessToken = getAccessToken();

        // 获取space id
        result.spaceId = getDingPanSpaceId(accessToken);

        // 授权用户访问企业的自定义空间
        authUserAccessCustomizeSpace(accessToken);

        return result;
    }

    // https://developers.dingtalk.com/document/app/obtain-orgapp-token
    private String getAccessToken(){
        String result = "";

        try {
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
            OapiGettokenRequest request = new OapiGettokenRequest();

            System.out.println("appkey: " + appkey + "; appsecret: " + appsecret);

            request.setAppkey(appkey);
            request.setAppsecret(appsecret);
            request.setHttpMethod("GET");
            OapiGettokenResponse response = client.execute(request);

            System.out.println(response.getBody());

            result = response.getAccessToken();
        } catch (Exception ex) {

        }

        return result;
    }

    // https://developers.dingtalk.com/document/app/obtain-user-space-under-the-enterprise
    private String getDingPanSpaceId(String accessToken) {
        String access_token = accessToken;

        String result = "";

        System.out.println("agentId: " + agentId + " domain: " + domain);

        try {
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/cspace/get_custom_space");
            OapiCspaceGetCustomSpaceRequest req = new OapiCspaceGetCustomSpaceRequest();
            req.setDomain(domain);
            req.setAgentId(agentId);
            req.setHttpMethod("GET");
            OapiCspaceGetCustomSpaceResponse rsp = client.execute(req, access_token);
            System.out.println(rsp.getBody());

            result = rsp.getSpaceid();
        } catch (Exception ex){

        }

        return result;
    }

    // https://developers.dingtalk.com/document/app/single-step-file-upload
    private String uploadFileAndGetMediaId(MultipartFile file, String accessToken){
        // 获取 mediaId
        String mediaId = "";
        try {
            OapiFileUploadSingleRequest request = new OapiFileUploadSingleRequest();
            request.setFileSize(45L);
            request.setAgentId(agentId);
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/file/upload/single?" + WebUtils.buildQuery(request.getTextParams(), "utf-8"));
            // 必须重新new一个请求
            request = new OapiFileUploadSingleRequest();
            request.setFile(new FileItem(file.getOriginalFilename(), file.getInputStream()));
            OapiFileUploadSingleResponse response = client.execute(request, accessToken);

            System.out.println(response.getBody());

            mediaId = response.getMediaId();

        } catch (Exception ex) {

        }

        return mediaId;
    }

    // 通过免登码获取用户信息、userId: https://developers.dingtalk.com/document/app/obtain-the-userid-of-a-user-by-using-the-log-free
    private String getUserId(String authCode, String accessToken){
        String userId = "";
        try {
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/user/getuserinfo");
            OapiUserGetuserinfoRequest req = new OapiUserGetuserinfoRequest();
            req.setCode(authCode);
            req.setHttpMethod("GET");
            OapiUserGetuserinfoResponse rsp = client.execute(req, accessToken);
            System.out.println(rsp.getBody());
            userId = rsp.getUserid();
        } catch (Exception ex) {

        }

        return userId;
    }

    // https://developers.dingtalk.com/document/app/authorize-a-user-to-access-a-custom-workspace-of-an
    private void authUserAccessCustomizeSpace(String accessToken) {

        // userId放在配置文件

        try {
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/cspace/grant_custom_space");
            OapiCspaceGrantCustomSpaceRequest req = new OapiCspaceGrantCustomSpaceRequest();
            req.setAgentId(agentId);
            req.setDomain(domain);
            req.setType(authSpaceType);
            req.setUserid(userId);
            req.setPath("/");
            //req.setFileids("123");
            req.setDuration(30L);
            req.setHttpMethod("GET");
            OapiCspaceGrantCustomSpaceResponse rsp = client.execute(req, accessToken);
            System.out.println("grant cusotm space result: " + rsp.getBody());

        } catch (Exception ex){

        }

        try {
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/cspace/get_custom_space");
            OapiCspaceGetCustomSpaceRequest req = new OapiCspaceGetCustomSpaceRequest();
            req.setDomain(domain);
            req.setAgentId(agentId);
            req.setHttpMethod("GET");
            OapiCspaceGetCustomSpaceResponse rsp = client.execute(req, accessToken);
            System.out.println("get custom space result: " + rsp.getBody());
        } catch (Exception ex){

        }
    }

    // https://developers.dingtalk.com/document/app/add-file-to-user-s-dingtalk-disk
    private String saveToCSAndGetFileId(String authCode, String mediaId, String spaceId, String fileName, String accessToken){
        String fileId = "";
        try {
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/cspace/add");
            OapiCspaceAddRequest req = new OapiCspaceAddRequest();
            req.setAgentId(agentId);
            req.setCode(authCode);
            req.setMediaId(mediaId);
            req.setSpaceId(spaceId);
            req.setName(fileName);
            req.setOverwrite(true);
            req.setHttpMethod("GET");
            OapiCspaceAddResponse rsp = client.execute(req, accessToken);

            System.out.println(rsp.getBody());

            fileId = JSON.parseObject(rsp.getDentry()).getString("id");
        } catch (Exception ex) {

        }

        return fileId;
    }

}
