const api = require('../../utils/api.js');

Page({
  data: {
    tab: 'report',        // report | plan | chat
    report: '',
    reportLoading: false,
    planGoal: '',
    plan: '',
    planLoading: false,
    chatList: [],         // {role, content}
    chatInput: '',
    chatLoading: false
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    this.setData({ tab });
    if (tab === 'report' && !this.data.report) {
      this.loadReport();
    }
  },

  onLoad() {
    this.loadReport();
  },

  // 周报
  loadReport() {
    if (this.data.reportLoading) return;
    this.setData({ reportLoading: true });
    api.aiWeeklyReport().then((res) => {
      this.setData({ report: res.report, reportLoading: false });
    }).catch(() => this.setData({ reportLoading: false }));
  },

  // 训练计划
  onPlanGoal(e) {
    this.setData({ planGoal: e.detail.value });
  },
  genPlan() {
    if (this.data.planLoading) return;
    this.setData({ planLoading: true });
    api.aiPlan(this.data.planGoal).then((res) => {
      this.setData({ plan: res.plan, planLoading: false });
    }).catch(() => this.setData({ planLoading: false }));
  },

  // 问答
  onChatInput(e) {
    this.setData({ chatInput: e.detail.value });
  },
  sendChat() {
    const text = this.data.chatInput.trim();
    if (!text || this.data.chatLoading) return;
    const list = this.data.chatList.concat({ role: 'user', content: text });
    this.setData({ chatList: list, chatInput: '', chatLoading: true });
    api.aiChat(list).then((res) => {
      this.setData({
        chatList: this.data.chatList.concat({ role: 'assistant', content: res.reply }),
        chatLoading: false
      });
    }).catch(() => this.setData({ chatLoading: false }));
  }
});
