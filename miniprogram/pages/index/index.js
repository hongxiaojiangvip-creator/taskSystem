const api = require('../../utils/api.js');
const app = getApp();

Page({
  data: {
    userInfo: null,
    overview: {},
    checkedToday: false,
    year: 0,
    month: 0,
    days: [],          // 日历格子
    checkedDates: [],  // 本月已打卡日期
    weekTitles: ['一', '二', '三', '四', '五', '六', '日']
  },

  onShow() {
    const now = new Date();
    this.setData({ year: now.getFullYear(), month: now.getMonth() + 1 });
    if (wx.getStorageSync('token')) {
      this.loadAll();
    } else {
      this.setData({ userInfo: null });
    }
  },

  loadAll() {
    api.getProfile().then((u) => this.setData({ userInfo: u })).catch(() => {});
    api.overview().then((o) => this.setData({ overview: o })).catch(() => {});
    api.checkedToday().then((r) => this.setData({ checkedToday: r.checked })).catch(() => {});
    this.loadCalendar();
  },

  loadCalendar() {
    const { year, month } = this.data;
    api.calendar(year, month).then((dates) => {
      this.setData({ checkedDates: dates });
      this.buildCalendar();
    }).catch(() => this.buildCalendar());
  },

  buildCalendar() {
    const { year, month, checkedDates } = this.data;
    const firstDay = new Date(year, month - 1, 1);
    // 周一为一周起点
    let startWeekday = firstDay.getDay(); // 0=周日
    startWeekday = startWeekday === 0 ? 6 : startWeekday - 1;
    const daysInMonth = new Date(year, month, 0).getDate();
    const todayStr = this.fmt(new Date());

    const cells = [];
    for (let i = 0; i < startWeekday; i++) cells.push({ empty: true });
    for (let d = 1; d <= daysInMonth; d++) {
      const ds = `${year}-${String(month).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
      cells.push({
        day: d,
        date: ds,
        checked: checkedDates.indexOf(ds) > -1,
        isToday: ds === todayStr
      });
    }
    this.setData({ days: cells });
  },

  fmt(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  },

  prevMonth() {
    let { year, month } = this.data;
    month--;
    if (month < 1) { month = 12; year--; }
    this.setData({ year, month }, () => this.loadCalendar());
  },

  nextMonth() {
    let { year, month } = this.data;
    month++;
    if (month > 12) { month = 1; year++; }
    this.setData({ year, month }, () => this.loadCalendar());
  },

  onLogin() {
    app.login().then((u) => {
      this.setData({ userInfo: u });
      this.loadAll();
    }).catch(() => {});
  },

  goCheckin() {
    if (!wx.getStorageSync('token')) {
      return this.onLogin();
    }
    wx.navigateTo({ url: '/pages/checkin/checkin' });
  },

  goRecords() {
    wx.navigateTo({ url: '/pages/records/records' });
  }
});
