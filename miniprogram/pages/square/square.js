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
    if (!wx.getStorageSync('token')) return;
    this.setData({ loading: true });
    api.square(this.data.page, this.data.size).then((res) => {
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

  toggleLike(e) {
    const id = e.currentTarget.dataset.id;
    const index = e.currentTarget.dataset.index;
    api.like(id).then((res) => {
      const list = this.data.list.slice();
      list[index].likeCount = res.likeCount;
      list[index].liked = !list[index].liked;
      this.setData({ list });
    });
  },

  goDetail(e) {
    wx.navigateTo({ url: '/pages/detail/detail?id=' + e.currentTarget.dataset.id });
  }
});
