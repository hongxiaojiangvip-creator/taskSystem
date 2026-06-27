const api = require('../../utils/api.js');

Page({
  data: {
    overview: {},
    days: 7,
    trend: []
  },

  onShow() {
    if (!wx.getStorageSync('token')) return;
    this.loadOverview();
    this.loadTrend();
  },

  loadOverview() {
    api.overview().then((o) => this.setData({ overview: o }));
  },

  switchRange(e) {
    const days = Number(e.currentTarget.dataset.days);
    this.setData({ days }, () => this.loadTrend());
  },

  loadTrend() {
    api.trend(this.data.days).then((trend) => {
      this.setData({ trend });
      this.drawChart(trend);
    });
  },

  drawChart(trend) {
    const query = wx.createSelectorQuery();
    query.select('#chart').fields({ node: true, size: true }).exec((res) => {
      if (!res || !res[0] || !res[0].node) return;
      const canvas = res[0].node;
      const ctx = canvas.getContext('2d');
      const dpr = wx.getSystemInfoSync().pixelRatio;
      const W = res[0].width;
      const H = res[0].height;
      canvas.width = W * dpr;
      canvas.height = H * dpr;
      ctx.scale(dpr, dpr);
      ctx.clearRect(0, 0, W, H);

      const padding = { left: 30, right: 10, top: 20, bottom: 30 };
      const chartW = W - padding.left - padding.right;
      const chartH = H - padding.top - padding.bottom;
      const max = Math.max(10, ...trend.map((t) => t.minutes));
      const barGap = chartW / trend.length;
      const barW = Math.min(28, barGap * 0.5);

      // 基线
      ctx.strokeStyle = '#eee';
      ctx.beginPath();
      ctx.moveTo(padding.left, padding.top + chartH);
      ctx.lineTo(padding.left + chartW, padding.top + chartH);
      ctx.stroke();

      trend.forEach((t, i) => {
        const x = padding.left + barGap * i + (barGap - barW) / 2;
        const h = (t.minutes / max) * chartH;
        const y = padding.top + chartH - h;
        ctx.fillStyle = '#4caf50';
        ctx.fillRect(x, y, barW, h);

        // x 轴标签:取日期的日
        ctx.fillStyle = '#999';
        ctx.font = '10px sans-serif';
        ctx.textAlign = 'center';
        const label = t.date.slice(5); // MM-DD
        if (trend.length <= 10 || i % 3 === 0) {
          ctx.fillText(label, x + barW / 2, H - 10);
        }
        // 数值
        if (t.minutes > 0) {
          ctx.fillStyle = '#4caf50';
          ctx.fillText(String(t.minutes), x + barW / 2, y - 4);
        }
      });
    });
  },

  goGoal() {
    wx.navigateTo({ url: '/pages/goal/goal' });
  },

  goAi() {
    wx.navigateTo({ url: '/pages/ai/ai' });
  }
});
