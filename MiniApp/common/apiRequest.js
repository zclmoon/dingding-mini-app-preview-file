export function apiRequest(method = "POST", url, data) {
  var _this=this;

  const { baseUrl } = getApp().globalData;

  return new Promise((resolve, reject) => {
      dd.showLoading({
      content: '加载中...',
    });
    
    dd.httpRequest({
      url: baseUrl + url,
      method: method,
      data: data,
      dataType: 'json',
      headers:{
          "Content-Type": "application/json"
      },
      success: function (res) {
        console.log('success',res);
        resolve(res)
      },
      fail: function (res) {
        console.log('fail',res);
        reject(res)
      },
      complete: function (res) {
        console.log('complete',res);
        dd.hideLoading()
      }
    });
  })
}