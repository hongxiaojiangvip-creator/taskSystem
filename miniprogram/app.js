const api = require('./utils/api.js');

App({
  globalData: {
    userInfo: null
  },

  onLaunch() {
    // 已有 token 则直接拉用户信息,否则等用户在页面里点登录
    const token = wx.getStorageSync('token');
    if (token) {
      api.getProfile().then((user) => {
        this.globalData.userInfo = user;
      }).catch(() => {});
    }
  },

  // 静默登录:wx.login 拿 code 换 token
  login(profile) {
    return new Promise((resolve, reject) => {
      wx.login({
        success: (res) => {
          api.login({
            code: res.code,
            nickname: profile && profile.nickname,
            avatar: profile && profile.avatar
          }).then((data) => {
            wx.setStorageSync('token', data.token);
            wx.setStorageSync('userInfo', data.user);
            this.globalData.userInfo = data.user;
            resolve(data.user);
          }).catch(reject);
        },
        fail: reject
      });
    });
  }
});
