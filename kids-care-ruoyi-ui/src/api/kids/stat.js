import request from '@/utils/request'

// 数据看板
export function statOverview() {
  return request({ url: '/kids/stat/overview', method: 'get' })
}

// 近 7 天出勤趋势
export function statTrend() {
  return request({ url: '/kids/stat/trend', method: 'get' })
}

// 统计报表
export function statReport() {
  return request({ url: '/kids/stat/report', method: 'get' })
}
