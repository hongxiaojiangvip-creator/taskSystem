// 业务 API 封装
const { request } = require('./request.js');

module.exports = {
  // 鉴权
  login: (data) => request({ url: '/api/auth/login', method: 'POST', data }),

  // 用户
  getProfile: () => request({ url: '/api/user/profile' }),
  updateProfile: (data) => request({ url: '/api/user/profile', method: 'POST', data }),

  // 运动类型
  getSportTypes: () => request({ url: '/api/sport-types' }),

  // 打卡
  createCheckin: (data) => request({ url: '/api/checkins', method: 'POST', data }),
  checkedToday: () => request({ url: '/api/checkins/today' }),
  myCheckins: (page, size) => request({ url: `/api/checkins/mine?page=${page}&size=${size}` }),
  square: (page, size) => request({ url: `/api/checkins/square?page=${page}&size=${size}` }),
  calendar: (year, month) => request({ url: `/api/checkins/calendar?year=${year}&month=${month}` }),
  checkinDetail: (id) => request({ url: `/api/checkins/${id}` }),
  deleteCheckin: (id) => request({ url: `/api/checkins/${id}`, method: 'DELETE' }),
  like: (id) => request({ url: `/api/checkins/${id}/like`, method: 'POST' }),

  // 统计
  overview: () => request({ url: '/api/stats/overview' }),
  trend: (days) => request({ url: `/api/stats/trend?days=${days}` }),

  // 目标
  getGoal: () => request({ url: '/api/goal' }),
  saveGoal: (data) => request({ url: '/api/goal', method: 'POST', data }),

  // 排行榜
  rank: (period) => request({ url: `/api/rank?period=${period}` })
};
