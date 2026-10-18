import request from '@/utils/request'

// 查询班级列表
export function listClazz(query) {
  return request({ url: '/kids/clazz/list', method: 'get', params: query })
}

// 班级概览：班级数 / 在园幼儿 / 满员率 / 超额班级
export function clazzOverview() {
  return request({ url: '/kids/clazz/overview', method: 'get' })
}

// 班级幼儿名单
export function clazzChildren(id) {
  return request({ url: '/kids/clazz/' + id + '/children', method: 'get' })
}

// 查询班级详情
export function getClazz(id) {
  return request({ url: '/kids/clazz/' + id, method: 'get' })
}

// 新增班级
export function addClazz(data) {
  return request({ url: '/kids/clazz', method: 'post', data: data })
}

// 修改班级
export function updateClazz(data) {
  return request({ url: '/kids/clazz', method: 'put', data: data })
}

// 删除班级
export function delClazz(id) {
  return request({ url: '/kids/clazz/' + id, method: 'delete' })
}
