const api = require('../../utils/api.js');

Page({
  data: {
    list: [],
    page: 1,
    size: 10,
    total: 0,
    loading: false,
    noMore: false
  },

  onShow() {
    this.setData({ list: [], page: 1, noMore: false }, () => this.load());
  },

  load() {
    if (this.data.loading || this.data.noMore) return;
    this.setData({ loading: true });
    api.myCheckins(this.data.page, this.data.size).then((res) => {
      const list = this.data.list.concat(res.list);
      this.setData({
        list,
        total: res.total,
        noMore: list.length >= res.total,
        loading: false
      });
    }).catch(() => this.setData({ loading: false }));
  },

  onReachBottom() {
    if (!this.data.noMore) {
      this.setData({ page: this.data.page + 1 }, () => this.load());
    }
  },

  goDetail(e) {
    wx.navigateTo({ url: '/pages/detail/detail?id=' + e.currentTarget.dataset.id });
  }
});
