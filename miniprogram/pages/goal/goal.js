const api = require('../../utils/api.js');

Page({
  data: {
    weeklyDays: 5,
    weeklyMinutes: 150
  },

  onLoad() {
    api.getGoal().then((g) => {
      this.setData({ weeklyDays: g.weeklyDays, weeklyMinutes: g.weeklyMinutes });
    });
  },

  onDays(e) {
    this.setData({ weeklyDays: Number(e.detail.value) });
  },
  onMinutes(e) {
    this.setData({ weeklyMinutes: Number(e.detail.value) });
  },

  save() {
    const { weeklyDays, weeklyMinutes } = this.data;
    if (weeklyDays < 1 || weeklyDays > 7) {
      return wx.showToast({ title: '每周天数 1-7', icon: 'none' });
    }
    api.saveGoal({ weeklyDays, weeklyMinutes }).then(() => {
      wx.showToast({ title: '已保存' });
      setTimeout(() => wx.navigateBack(), 600);
    });
  }
});
