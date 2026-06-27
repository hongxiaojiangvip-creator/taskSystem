// 网络请求封装,自动带上 token、统一处理返回体
const { BASE_URL } = require('../config.js');

function request(options) {
  const token = wx.getStorageSync('token');
  return new Promise((resolve, reject) => {
    wx.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: Object.assign(
        { 'content-type': 'application/json' },
        token ? { Authorization: 'Bearer ' + token } : {},
        options.header || {}
      ),
      success(res) {
        const body = res.data;
        if (res.statusCode === 200 && body && body.code === 0) {
          resolve(body.data);
        } else if (body && body.code === 401) {
          // 登录失效,清理并跳回授权
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          wx.showToast({ title: '请重新登录', icon: 'none' });
          reject(body);
        } else {
          wx.showToast({ title: (body && body.message) || '请求失败', icon: 'none' });
          reject(body || res);
        }
      },
      fail(err) {
        wx.showToast({ title: '网络异常', icon: 'none' });
        reject(err);
      }
    });
  });
}

// 文件上传
function uploadFile(filePath) {
  const token = wx.getStorageSync('token');
  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: BASE_URL + '/api/upload',
      filePath,
      name: 'file',
      header: token ? { Authorization: 'Bearer ' + token } : {},
      success(res) {
        try {
          const body = JSON.parse(res.data);
          if (body.code === 0) {
            resolve(body.data.url);
          } else {
            reject(body);
          }
        } catch (e) {
          reject(e);
        }
      },
      fail: reject
    });
  });
}

module.exports = { request, uploadFile };
