import request from '@/utils/request'

export function listLeave(query) {
  return request({ url: '/kids/leave/list', method: 'get', params: query })
}

export function getLeave(id) {
  return request({ url: '/kids/leave/' + id, method: 'get' })
}

export function addLeave(data) {
  return request({ url: '/kids/leave', method: 'post', data: data })
}

export function updateLeave(data) {
  return request({ url: '/kids/leave', method: 'put', data: data })
}

export function delLeave(id) {
  return request({ url: '/kids/leave/' + id, method: 'delete' })
}

// 审批：pass=true 通过
export function approveLeave(id, pass) {
  return request({ url: '/kids/leave/approve/' + id, method: 'put', params: { pass } })
}
