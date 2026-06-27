const api = require('../../utils/api.js');
const { uploadFile } = require('../../utils/request.js');

Page({
  data: {
    sportTypes: [],
    selectedTypeId: null,
    duration: '',
    remark: '',
    images: [],      // 已上传的 url
    submitting: false
  },

  onLoad() {
    api.getSportTypes().then((list) => {
      this.setData({ sportTypes: list, selectedTypeId: list.length ? list[0].id : null });
    });
  },

  selectType(e) {
    this.setData({ selectedTypeId: e.currentTarget.dataset.id });
  },

  onDuration(e) {
    this.setData({ duration: e.detail.value });
  },

  onRemark(e) {
    this.setData({ remark: e.detail.value });
  },

  chooseImage() {
    if (this.data.images.length >= 6) {
      return wx.showToast({ title: '最多 6 张', icon: 'none' });
    }
    wx.chooseMedia({
      count: 6 - this.data.images.length,
      mediaType: ['image'],
      success: (res) => {
        wx.showLoading({ title: '上传中' });
        const tasks = res.tempFiles.map((f) => uploadFile(f.tempFilePath));
        Promise.all(tasks).then((urls) => {
          this.setData({ images: this.data.images.concat(urls) });
          wx.hideLoading();
        }).catch(() => {
          wx.hideLoading();
          wx.showToast({ title: '图片上传失败', icon: 'none' });
        });
      }
    });
  },

  removeImage(e) {
    const idx = e.currentTarget.dataset.index;
    const images = this.data.images.slice();
    images.splice(idx, 1);
    this.setData({ images });
  },

  submit() {
    const { selectedTypeId, duration, remark, images, submitting } = this.data;
    if (submitting) return;
    if (!selectedTypeId) return wx.showToast({ title: '请选择运动类型', icon: 'none' });
    if (!duration || Number(duration) <= 0) return wx.showToast({ title: '请填写运动时长', icon: 'none' });

    this.setData({ submitting: true });
    api.createCheckin({
      sportTypeId: selectedTypeId,
      duration: Number(duration),
      remark,
      images
    }).then(() => {
      wx.showToast({ title: '打卡成功 🎉' });
      setTimeout(() => wx.navigateBack(), 800);
    }).catch(() => {
      this.setData({ submitting: false });
    });
  }
});
