const api = require('../../utils/api.js');

Page({
  data: {
    period: 'week',
    list: []
  },

  onShow() {
    if (!wx.getStorageSync('token')) return;
    this.load();
  },

  switchPeriod(e) {
    this.setData({ period: e.currentTarget.dataset.period }, () => this.load());
  },

  load() {
    api.rank(this.data.period).then((list) => this.setData({ list }));
  }
});
