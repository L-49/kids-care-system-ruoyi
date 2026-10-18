import request from '@/utils/request'

export function listMenu(query) {
  return request({ url: '/kids/menu/list', method: 'get', params: query })
}

// 周视图：weekStart 为周一，格式 yyyy-MM-dd
export function menuWeek(weekStart) {
  return request({ url: '/kids/menu/week', method: 'get', params: { weekStart } })
}

// 复制上周食谱
export function copyLastWeek(weekStart) {
  return request({ url: '/kids/menu/copy-last-week', method: 'post', params: { weekStart } })
}

export function getMenu(id) {
  return request({ url: '/kids/menu/' + id, method: 'get' })
}

export function addMenu(data) {
  return request({ url: '/kids/menu', method: 'post', data: data })
}

export function updateMenu(data) {
  return request({ url: '/kids/menu', method: 'put', data: data })
}

export function delMenu(id) {
  return request({ url: '/kids/menu/' + id, method: 'delete' })
}
