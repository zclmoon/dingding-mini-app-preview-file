import { apiRequest } from "../../common/apiRequest";
import {GET_AND_AUTH_CUS_SPACE} from "../../common/config";

Page({
  onLoad(query) {
    // 页面加载
    console.info(`Page onLoad with query: ${JSON.stringify(query)}`);
  },
  onReady() {
    // 页面加载完成
  },
  onShow() {
    // 页面显示
  },
  onHide() {
    // 页面隐藏
  },
  onUnload() {

    // 页面被关闭
  },
  onTitleClick() {
    // 标题被点击
  },
  onPullDownRefresh() {
    // 页面被下拉

  },
  onReachBottom() {
    // 页面被拉到底部
  },
  onShareAppMessage() {
    // 返回自定义分享信息
    return {
      title: '预览文件Demo',
      desc: 'My App description',
      path: 'pages/index/index',
    };
  },

  onUploadFile() {
    console.log("开始上传文件");

    // let spaceId = "4662107042";
    // dd.uploadAttachmentToDingTalk({
    //                         image:{multiple:true,compress:false,max:9,spaceId: spaceId},
    //                         space:{spaceId:spaceId,isCopy:1 , max:9},
    //                         file: {spaceId:spaceId,max:1},
    //                         types:["photo","camera","file","space"],
    //                         success: (ures) => {
    //                           console.log(ures);
    //                         },
    //                         fail: (err) =>{
    //                             dd.alert({
    //                                 content:JSON.stringify(err)
    //                             })
    //                         }
    //                     });


    // dd.getAuthCode({
    //     success:function(res){
    //         const {authCode} = res;

    //             apiRequest("POST", GET_AND_AUTH_CUS_SPACE, JSON.stringify({authCode:authCode}))
    //               .then(apiRes => {

    //                   console.log(apiRes);

    //                   const {spaceId} = apiRes;

    //                   dd.uploadAttachmentToDingTalk({
    //                         image:{multiple:true,compress:false,max:9,spaceId: spaceId},
    //                         space:{spaceId:spaceId,isCopy:1 , max:9},
    //                         file: {spaceId:spaceId,max:1},
    //                         types:["photo","camera","file","space"],
    //                         success: (ures) => {
    //                           console.log(ures);
    //                         },
    //                         fail: (err) =>{
    //                             dd.alert({
    //                                 content:JSON.stringify(err)
    //                             })
    //                         }
    //                     })
    //               });
    //     },
    //     fail:function(err){
    //     }
    // });

  },
  onPreviewFile() {
    console.log("开始预览上传的文件");

    dd.previewFileInDingTalk({
        corpId:dd.corpId,
        spaceId:"4662107042",
        fileId:"33988180072",
        fileName:"test.pdf",
        fileSize:1024,
        fileType:"pdf",

    })
  }
});
