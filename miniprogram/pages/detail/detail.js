const api = require('../../utils/api.js');

Page({
  data: {
    item: null
  },

  onLoad(options) {
    this.id = options.id;
    this.load();
  },

  load() {
    api.checkinDetail(this.id).then((item) => this.setData({ item }));
  },

  previewImage(e) {
    const url = e.currentTarget.dataset.url;
    wx.previewImage({ current: url, urls: this.data.item.images });
  },

  toggleLike() {
    api.like(this.id).then((res) => {
      const item = this.data.item;
      item.likeCount = res.likeCount;
      item.liked = !item.liked;
      this.setData({ item });
    });
  },

  onDelete() {
    wx.showModal({
      title: '提示',
      content: '确定删除这条打卡记录?',
      success: (res) => {
        if (res.confirm) {
          api.deleteCheckin(this.id).then(() => {
            wx.showToast({ title: '已删除' });
            setTimeout(() => wx.navigateBack(), 600);
          });
        }
      }
    });
  }
});
