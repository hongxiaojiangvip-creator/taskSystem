const api = require('../../utils/api.js');
const app = getApp();

Page({
  data: {
    userInfo: null,
    overview: {}
  },

  onShow() {
    if (wx.getStorageSync('token')) {
      api.getProfile().then((u) => this.setData({ userInfo: u })).catch(() => {});
      api.overview().then((o) => this.setData({ overview: o })).catch(() => {});
    } else {
      this.setData({ userInfo: null });
    }
  },

  onLogin() {
    app.login().then((u) => this.setData({ userInfo: u })).catch(() => {});
  },

  // 更新头像
  onChooseAvatar(e) {
    const { uploadFile } = require('../../utils/request.js');
    wx.showLoading({ title: '上传中' });
    uploadFile(e.detail.avatarUrl).then((url) => {
      return api.updateProfile({ avatar: url });
    }).then((u) => {
      wx.hideLoading();
      this.setData({ userInfo: u });
    }).catch(() => wx.hideLoading());
  },

  // 修改昵称
  editNickname() {
    const that = this;
    wx.showModal({
      title: '修改昵称',
      editable: true,
      placeholderText: '请输入昵称',
      content: this.data.userInfo.nickname || '',
      success(res) {
        if (res.confirm && res.content) {
          api.updateProfile({ nickname: res.content }).then((u) => that.setData({ userInfo: u }));
        }
      }
    });
  },

  goGoal() {
    wx.navigateTo({ url: '/pages/goal/goal' });
  },
  goRecords() {
    wx.navigateTo({ url: '/pages/records/records' });
  },
  goAi() {
    wx.navigateTo({ url: '/pages/ai/ai' });
  },

  logout() {
    wx.showModal({
      title: '提示',
      content: '确定退出登录?',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          app.globalData.userInfo = null;
          this.setData({ userInfo: null, overview: {} });
        }
      }
    });
  }
});
